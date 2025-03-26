import _, { groupBy, max, minBy, maxBy, sum } from 'lodash';
import { computed, Ref } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import {
	DataArray,
	getRunResult,
	getSimulation,
	parsePyCiemssMap,
	processAndSortSamplesByTimepoint
} from '@/services/models/simulation-service';
import {
	DynamicIntervention,
	Intervention,
	InterventionPolicy,
	InterventionSemanticType,
	InterventionValueType,
	ModelConfiguration,
	StaticIntervention
} from '@/types/Types';
import { createInterventionPolicy, isInterventionStatic } from '@/services/intervention-policy';
import { mergeResults } from '@/services/dataset';
import { v4 as uuidv4 } from 'uuid';
import {
	getInferredParameter,
	getModelIdFromModelConfigurationId,
	getParameter,
	getParameterDistributionAverage
} from '@/services/model-configurations';
import { logger } from '@/utils/logger';
import {
	blankInterventionPolicyGroup,
	ContextMethods,
	Criterion,
	InterventionPolicyGroupForm,
	OptimizationInterventionObjective,
	OptimizeCiemssOperationState
} from './optimize-ciemss-operation';

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<OptimizeCiemssOperationState>;
	},
	runResults: Ref<{ [runId: string]: DataArray }>,
	runResultsSummary: Ref<{ [runId: string]: DataArray }>
) {
	const preForecastRunId = computed(() => props.node.state.preForecastRunId);
	const postForecastRunId = computed(() => props.node.state.postForecastRunId);
	return computed(() => {
		if (!postForecastRunId.value || !preForecastRunId.value) return null;
		const preResult = runResults.value[preForecastRunId.value];
		const preResultSummary = runResultsSummary.value[preForecastRunId.value];
		const postResult = runResults.value[postForecastRunId.value];
		const postResultSummary = runResultsSummary.value[postForecastRunId.value];

		if (!postResult || !postResultSummary || !preResultSummary || !preResult) return null;
		const pyciemssMap = parsePyCiemssMap(postResult[0]);
		if (_.isEmpty(pyciemssMap)) return null;

		// Merge before/after for chart
		const result = mergeResults(preResult, postResult);
		const resultSummary = mergeResults(preResultSummary, postResultSummary);
		const resultGroupByTimepoint = processAndSortSamplesByTimepoint(result);

		const translationMap: Record<string, string> = {};
		Object.keys(pyciemssMap).forEach((key) => {
			translationMap[`${pyciemssMap[key]}_mean`] = `${key} after optimization`;
			translationMap[`${pyciemssMap[key]}_mean:pre`] = `${key} before optimization`;
		});

		return {
			result,
			resultSummary,
			resultGroupByTimepoint,
			translationMap,
			pyciemssMap
		};
	});
}

export function setQoIData(resultData: DataArray, config: Criterion) {
	const targetVar = config.targetVariable;
	let data: number[] = [];
	let averageRisk = 0;
	// Set data:
	if (config.qoiMethod === ContextMethods.day_average) {
		// last timepoints
		// Filter for all values with timepoint = last timepoint
		const lastTimeId = max(resultData.map((ele) => ele.timepoint_id));
		data = resultData.filter((ele) => ele.timepoint_id === lastTimeId).map((ele) => ele[targetVar]);
	} else if (config.qoiMethod === ContextMethods.max) {
		// all timepoints
		// For each sample grab the min or max value for the given state:
		if (config.isMinimized) {
			// Grab the max:
			data = Object.entries(groupBy(resultData, 'sample_id')).map((ele) => maxBy(ele[1], targetVar)?.[targetVar]);
		} else {
			// Grab the min
			data = Object.entries(groupBy(resultData, 'sample_id')).map((ele) => minBy(ele[1], targetVar)?.[targetVar]);
		}
	} else {
		// should not be hit unless we add more available ContextMethods that we have yet to handle.
		console.error(`The following context method is not handled: ${config.qoiMethod}`);
	}
	// Set Risk:
	// Risk value is the average of the top or bottom X% where X is 100 - riskTolerance
	const amountOfRiskIndexes = Math.ceil(((100 - config.riskTolerance) / 100) * data.length);
	if (config.isMinimized) {
		// Get for the top X
		const topX = data.sort((n1, n2) => n1 - n2).slice(data.length - amountOfRiskIndexes, data.length);
		averageRisk = sum(topX) / topX.length;
	} else {
		// Get bottom X
		const bottomX = data.sort((n1, n2) => n1 - n2).slice(0, amountOfRiskIndexes);
		averageRisk = sum(bottomX) / bottomX.length;
	}

	return { data, risk: averageRisk };
}

export function policyGroupFormToIntervention(policyGroupForm: InterventionPolicyGroupForm) {
	const intervention: Intervention = {
		id: uuidv4(),
		name: policyGroupForm.interventionName,
		staticInterventions: [],
		dynamicInterventions: []
	};
	if (isInterventionStatic(policyGroupForm.individualIntervention)) {
		intervention.staticInterventions.push(policyGroupForm.individualIntervention as StaticIntervention);
	} else {
		intervention.dynamicInterventions.push(policyGroupForm.individualIntervention as DynamicIntervention);
	}
	return intervention;
}

// Get the simulation object that ran the optimize.
// Get the fixed static interventions from this, as well as some details about the optimization interevntions
// Get the optimization result file
// Concat the optimization result file with the optimization interventions from simulation object
export async function getOptimizedInterventions(optimizeRunId: string) {
	// This is a snake case version of types/Types OptimizeInterventions as we are reading from simulation execution payload
	interface OptimizeInterventionsSnakeCase {
		intervention_type: string;
		param_name: string;
		param_value?: number;
		start_time?: number;
		time_objective_function: string;
		parameter_objective_function: string;
		relative_importance: number;
		param_value_initial_guess?: number;
		parameter_value_lower_bound: number;
		parameter_value_upper_bound: number;
		start_time_initial_guess?: number;
		start_time_lower_bound: number;
		start_time_upper_bound: number;
	}

	const allInterventions: Intervention[] = [];
	// Get the interventionPolicyGroups from the simulation object.
	// This will prevent any inconsistencies being passed via knobs or state when matching with result file.
	const simulation = await getSimulation(optimizeRunId);

	const simulationStaticInterventions: any[] = simulation?.executionPayload.fixed_static_parameter_interventions ?? [];
	const optimizeInterventions: OptimizeInterventionsSnakeCase[] = simulation?.executionPayload?.optimize_interventions;

	// Add any static (not optimized) interventions to allInterventions
	simulationStaticInterventions.forEach((inter) => {
		const newIntervetion: Intervention = {
			id: uuidv4(),
			dynamicInterventions: inter.dynamic_interventions,
			name: inter.name,
			staticInterventions: inter.static_interventions
		};
		allInterventions.push(newIntervetion);
	});

	const policyResults: number[] = await getRunResult(optimizeRunId, 'policy.json');

	optimizeInterventions.forEach((optimizedIntervention) => {
		const interventionType = optimizedIntervention.intervention_type;
		const paramName: string = optimizedIntervention.param_name;
		const paramValue: number | undefined = optimizedIntervention.param_value;
		const startTime: number | undefined = optimizedIntervention.start_time;

		if (interventionType === OptimizationInterventionObjective.startTime) {
			const newTimestepAsList = policyResults.splice(0, 1);
			// If we our intervention type is param value our policyResult will provide a timestep.
			allInterventions.push({
				id: uuidv4(),
				name: `Optimized ${paramName}`,
				staticInterventions: [
					{
						appliedTo: paramName,
						type: InterventionSemanticType.Parameter,
						timestep: newTimestepAsList[0],
						value: paramValue as number,
						valueType: InterventionValueType.Value
					}
				],
				dynamicInterventions: []
			});
		} else if (interventionType === OptimizationInterventionObjective.paramValue) {
			const valueAsList = policyResults.splice(0, 1);
			// If we our intervention type is start time our policyResult will provide a parameter value.
			allInterventions.push({
				id: uuidv4(),
				name: `Optimized ${paramName}`,
				staticInterventions: [
					{
						timestep: startTime as number,
						value: valueAsList[0],
						appliedTo: paramName,
						type: InterventionSemanticType.Parameter,
						valueType: InterventionValueType.Value
					}
				],
				dynamicInterventions: []
			});
		} else if (interventionType === OptimizationInterventionObjective.paramValueAndStartTime) {
			// If our intervention type is start_time_param_value our policyResult will contain the timestep value, then the parameter value.
			// https://github.com/ciemss/pyciemss/blob/main/pyciemss/integration_utils/intervention_builder.py#L66
			const timeAndValueAsList = policyResults.splice(0, 2);
			allInterventions.push({
				id: uuidv4(),
				name: `Optimized ${paramName}`,
				staticInterventions: [
					{
						timestep: timeAndValueAsList[0],
						value: timeAndValueAsList[1],
						appliedTo: paramName,
						type: InterventionSemanticType.Parameter,
						valueType: InterventionValueType.Value
					}
				],
				dynamicInterventions: []
			});
		} else {
			// Should realistically not be hit unless we change the interface and do not update
			console.error(`Unable to find the intevention type for optimization run: ${optimizeRunId}`);
		}
	});
	return allInterventions;
}

export async function createInterventionPolicyFromOptimize(
	modelConfigId: string,
	optimizeRunId: string
): Promise<InterventionPolicy | null> {
	const modelId = await getModelIdFromModelConfigurationId(modelConfigId);
	const optimizedInterventions = await getOptimizedInterventions(optimizeRunId);

	const newIntervention: InterventionPolicy = {
		name: `Optimize run: ${optimizeRunId}`,
		modelId,
		temporary: true,
		interventions: optimizedInterventions
	};
	const newInterventionPolicy: InterventionPolicy | null = await createInterventionPolicy(newIntervention);
	return newInterventionPolicy;
}

// get the value of the intervention, taking into account the distribution if it is a percentage
export const resolveInterventionValue = (intervention: StaticIntervention, modelConfiguration: ModelConfiguration) => {
	if (
		intervention.type !== InterventionSemanticType.Parameter ||
		intervention.valueType !== InterventionValueType.Percentage
	) {
		return intervention.value;
	}

	// Get the parameter from the model configuration and return the "average" value of the distribution times the percentage.
	const parameter =
		getInferredParameter(modelConfiguration, intervention.appliedTo) ??
		getParameter(modelConfiguration, intervention.appliedTo);
	if (!parameter) {
		logger.error(`Parameter ${intervention.appliedTo} not found in model configuration`);
		return intervention.value;
	}
	return getParameterDistributionAverage(parameter) * (intervention.value / 100);
};

export function setInterventionPolicyGroups(
	state: OptimizeCiemssOperationState,
	interventionPolicy: InterventionPolicy,
	modelConfiguration: ModelConfiguration
) {
	// If already set + not changed since set, do not reset.
	if (state.interventionPolicyGroups.length > 0 && state.interventionPolicyGroups[0].id === interventionPolicy.id) {
		return state;
	}
	state.interventionPolicyId = interventionPolicy.id ?? '';

	state.interventionPolicyGroups = []; // Reset prior to populating.
	if (interventionPolicy.interventions && interventionPolicy.interventions.length > 0) {
		interventionPolicy.interventions.forEach((intervention) => {
			// Static:
			const newIntervention = _.cloneDeep(blankInterventionPolicyGroup);
			newIntervention.id = interventionPolicy.id;
			intervention.staticInterventions.forEach((staticIntervention) => {
				newIntervention.relativeImportance = 5;
				newIntervention.individualIntervention = staticIntervention;
				newIntervention.startTimeGuess = staticIntervention.timestep;
				newIntervention.initialGuessValue = resolveInterventionValue(staticIntervention, modelConfiguration!);
				state.interventionPolicyGroups.push(_.cloneDeep(newIntervention));
			});
			// Dynamic:
			intervention.dynamicInterventions.forEach((dynamicIntervention) => {
				newIntervention.relativeImportance = 0;
				newIntervention.individualIntervention = dynamicIntervention;
				state.interventionPolicyGroups.push(_.cloneDeep(newIntervention));
			});
		});
	}
	return state;
}
