import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { Intervention as SimulationIntervention } from '@/types/Types';
import { getRunResult, getSimulation } from '@/services/models/simulation-service';

const DOCUMENTATION_URL =
	'https://github.com/ciemss/pyciemss/blob/main/pyciemss/interfaces.py#L747';

export enum InterventionTypes {
	paramValue = 'param_value',
	startTime = 'start_time'
}

export enum ContextMethods {
	day_average = 'day_average',
	max = 'max'
}

export interface InterventionPolicyGroup {
	name: string;
	parameter: string;
	startTime: number;
	lowerBound: number;
	upperBound: number;
	initialGuess: number;
	isActive: boolean;
	paramValue: number;
}

export interface ConstraintGroup {
	name: string; // Title of the group
	targetVariable: string;
	qoiMethod: ContextMethods;
	riskTolerance: number;
	threshold: number;
	isMinimized: boolean;
	isActive: boolean; // Denotes whether or not this should be used when user hits run.
}

export interface OptimizeCiemssOperationState extends BaseState {
	// Settings
	endTime: number;
	numSamples: number;
	solverMethod: string;
	maxiter: number;
	maxfeval: number;
	// Intervention policies
	interventionType: InterventionTypes;
	interventionPolicyGroups: InterventionPolicyGroup[];
	// Constraints:
	constraintGroups: ConstraintGroup[];
	// Charts + Outputs:
	chartConfigs: string[][];
	inProgressOptimizeId: string;
	inProgressForecastId: string;
	forecastRunId: string;
	optimizationRunId: string;
	optimizeErrorMessage: { name: string; value: string; traceback: string };
	simulateErrorMessage: { name: string; value: string; traceback: string };
}

export const blankInterventionPolicyGroup: InterventionPolicyGroup = {
	name: 'Policy bounds',
	parameter: '',
	startTime: 0,
	lowerBound: 0,
	upperBound: 0,
	initialGuess: 0,
	isActive: true,
	paramValue: 0
};

export const defaultConstraintGroup: ConstraintGroup = {
	name: 'Constraint',
	qoiMethod: ContextMethods.max,
	targetVariable: '',
	riskTolerance: 5,
	threshold: 1,
	isMinimized: true,
	isActive: true
};

export const OptimizeCiemssOperation: Operation = {
	name: WorkflowOperationTypes.OPTIMIZE_CIEMSS,
	displayName: 'Optimize with PyCIEMSS',
	description: 'Optimize with PyCIEMSS',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false },
		{ type: 'calibrateSimulationId', label: 'Calibration', acceptMultiple: false, isOptional: true }
	],
	outputs: [{ type: 'simulationId' }],
	isRunnable: true,

	initState: () => {
		const init: OptimizeCiemssOperationState = {
			endTime: 90,
			numSamples: 100,
			solverMethod: 'dopri5',
			maxiter: 5,
			maxfeval: 25,
			interventionType: InterventionTypes.paramValue,
			interventionPolicyGroups: [blankInterventionPolicyGroup],
			constraintGroups: [defaultConstraintGroup],
			chartConfigs: [],
			inProgressOptimizeId: '',
			inProgressForecastId: '',
			forecastRunId: '',
			optimizationRunId: '',
			optimizeErrorMessage: { name: '', value: '', traceback: '' },
			simulateErrorMessage: { name: '', value: '', traceback: '' }
		};
		return init;
	}
};

// Get the intervention output from a given optimization run
export async function getOptimizedInterventions(optimizeRunId: string) {
	// Get the interventionPolicyGroups from the simulation object.
	// This will prevent any inconsistencies being passed via knobs or state when matching with result file.
	const simulation = await getSimulation(optimizeRunId);
	const simulationIntervetions: SimulationIntervention[] =
		simulation?.executionPayload.fixed_static_parameter_interventions ?? [];
	const policyInterventions = simulation?.executionPayload?.policy_interventions;
	const interventionType = policyInterventions.selection ?? '';
	const paramNames: string[] = policyInterventions.param_names ?? [];
	const paramValue: number[] = policyInterventions.param_values ?? [];
	const startTime: number[] = policyInterventions.start_time ?? [];

	const policyResult = await getRunResult(optimizeRunId, 'policy.json');

	if (interventionType === InterventionTypes.paramValue && startTime.length !== 0) {
		// intervention type == parameter value
		for (let i = 0; i < paramNames.length; i++) {
			// This is all index matching for optimizeInterventions.paramNames, optimizeInterventions.startTimes, and policyResult
			simulationIntervetions.push({
				name: paramNames[i],
				timestep: startTime[i],
				value: policyResult[i]
			});
		}
	} else if (interventionType === InterventionTypes.startTime && paramValue.length !== 0) {
		for (let i = 0; i < paramNames.length; i++) {
			// This is all index matching for optimizeInterventions.paramNames, optimizeInterventions.startTimes, and policyResult
			simulationIntervetions.push({
				name: paramNames[i],
				timestep: policyResult[i],
				value: paramValue[i]
			});
		}
	} else {
		// Should realistically not be hit unless we change the interface and do not update
		console.error(`Unable to find the intevention for optimization run: ${optimizeRunId}`);
	}

	return simulationIntervetions;
}
