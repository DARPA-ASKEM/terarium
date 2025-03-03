import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { Intervention, InterventionSemanticType, InterventionPolicy, InterventionValueType } from '@/types/Types';
import { CiemssMethodOptions, getRunResult, getSimulation } from '@/services/models/simulation-service';
import { getModelIdFromModelConfigurationId } from '@/services/model-configurations';
import { createInterventionPolicy, blankIntervention } from '@/services/intervention-policy';
import optimizeModel from '@assets/svg/operator-images/optimize-model.svg';
import { ChartSetting } from '@/types/common';

const DOCUMENTATION_URL = 'https://documentation.terarium.ai/config-and-intervention/optimize-intervention-policy/';

export enum OptimizationInterventionObjective {
	startTime = 'start_time', // provide a parameter value to get a better start time.
	paramValue = 'param_value', // provide a start time to get a better parameter value.
	paramValueAndStartTime = 'start_time_param_value'
}

export enum InterventionObjectiveFunctions {
	lowerBound = 'lower_bound',
	upperbound = 'upper_bound',
	initialGuess = 'initial_guess'
}

export enum ContextMethods {
	day_average = 'day_average',
	max = 'max'
}

export interface InterventionPolicyGroupForm {
	// The ID of the InterventionPolicy this is portraying
	id?: string; // // FIXME: This will not be required when some init logic is moved from drilldown -> Node.
	startTime: number;
	endTime: number;
	startTimeGuess: number;
	lowerBoundValue: number;
	upperBoundValue: number;
	initialGuessValue: number;
	relativeImportance: number;
	optimizeFunction: {
		type: OptimizationInterventionObjective;
		timeObjectiveFunction: InterventionObjectiveFunctions;
		parameterObjectiveFunction: InterventionObjectiveFunctions;
	};
	intervention: Intervention;
}

export interface Criterion {
	name: string; // Title of the group
	targetVariable: string; // qoi context which will contain _state or _observable accordingly
	qoiMethod: ContextMethods;
	riskTolerance: number;
	threshold: number;
	isMinimized: boolean;
	isActive: boolean; // Denotes whether this should be used when user hits run.
}

export interface OptimizeCiemssOperationState extends BaseState {
	// Settings
	endTime: number;
	numSamples: number;
	solverStepSize: number;
	solverMethod: CiemssMethodOptions;
	maxiter: number;
	maxfeval: number;
	// Intervention policies
	interventionPolicyId: string; // Used to determine if we need to reset the InterventionPolicyGroupForm.
	interventionPolicyGroups: InterventionPolicyGroupForm[];
	// Constraints:
	constraintGroups: Criterion[];
	chartSettings: ChartSetting[] | null; // null indicates that the chart settings have not been set yet
	inProgressOptimizeId: string;
	inProgressPreForecastId: string;
	preForecastRunId: string;
	inProgressPostForecastId: string;
	postForecastRunId: string;
	optimizationRunId: string;
	optimizedInterventionPolicyId: string;
	optimizeErrorMessage: { name: string; value: string; traceback: string };
	simulateErrorMessage: { name: string; value: string; traceback: string };
	// Intermediate:
	currentProgress: number; // optimization run's 2 digit %
}

// This is used as a map between dropdown labels and the inner values used by pyciemss-service.
export const OPTIMIZATION_TYPE_MAP = [
	{ label: 'new value', value: OptimizationInterventionObjective.paramValue },
	{ label: 'new start time', value: OptimizationInterventionObjective.startTime },
	{
		label: 'new value and start time',
		value: OptimizationInterventionObjective.paramValueAndStartTime
	}
];

// This is used as a map between dropdown labels and the inner values used by pyciemss-service.
export const OBJECTIVE_FUNCTION_MAP = [
	{ label: 'initial guess', value: InterventionObjectiveFunctions.initialGuess },
	{ label: 'lower bound', value: InterventionObjectiveFunctions.lowerBound },
	{ label: 'upper bound', value: InterventionObjectiveFunctions.upperbound }
];

export const blankInterventionPolicyGroup: InterventionPolicyGroupForm = {
	startTime: 0,
	endTime: 0,
	startTimeGuess: 0,
	lowerBoundValue: 0,
	upperBoundValue: 0,
	initialGuessValue: 0,
	relativeImportance: 5,
	optimizeFunction: {
		type: OptimizationInterventionObjective.startTime,
		timeObjectiveFunction: InterventionObjectiveFunctions.initialGuess,
		parameterObjectiveFunction: InterventionObjectiveFunctions.initialGuess
	},
	intervention: blankIntervention
};

export const defaultCriterion: Criterion = {
	name: 'Criterion',
	qoiMethod: ContextMethods.max,
	targetVariable: '',
	riskTolerance: 95,
	threshold: 1,
	isMinimized: true,
	isActive: true
};

export const OptimizeCiemssOperation: Operation = {
	name: WorkflowOperationTypes.OPTIMIZE_CIEMSS,
	displayName: 'Optimize intervention policy',
	description: 'Optimize intervention policy',
	documentationUrl: DOCUMENTATION_URL,
	imageUrl: optimizeModel,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration' },
		{
			type: 'policyInterventionId',
			label: 'Intervention policy'
		}
	],
	outputs: [{ type: 'policyInterventionId|datasetId' }],
	isRunnable: true,

	initState: () => {
		const init: OptimizeCiemssOperationState = {
			endTime: 90,
			numSamples: 100,
			solverStepSize: 0.1,
			solverMethod: CiemssMethodOptions.dopri5,
			maxiter: 5,
			maxfeval: 25,
			interventionPolicyId: '',
			interventionPolicyGroups: [],
			constraintGroups: [defaultCriterion],
			chartSettings: null,
			inProgressOptimizeId: '',
			inProgressPostForecastId: '',
			inProgressPreForecastId: '',
			preForecastRunId: '',
			postForecastRunId: '',
			optimizationRunId: '',
			optimizedInterventionPolicyId: '',
			optimizeErrorMessage: { name: '', value: '', traceback: '' },
			simulateErrorMessage: { name: '', value: '', traceback: '' },
			currentProgress: 0
		};
		return init;
	}
};

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

/**
 *
 * 1) Get optimize interventions
 *
 *
 */
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
