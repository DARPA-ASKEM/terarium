import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { StaticIntervention, DynamicIntervention } from '@/types/Types';
import { CiemssMethodOptions } from '@/services/models/simulation-service';
import { blankIntervention } from '@/services/intervention-policy';
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
	interventionName: string;
	individualIntervention: StaticIntervention | DynamicIntervention;
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
	numberOfTimepoints: number;
	isNumberOfTimepointsManual: boolean;
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
	interventionName: blankIntervention.name,
	individualIntervention: blankIntervention.staticInterventions[0]
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
			numberOfTimepoints: 90,
			isNumberOfTimepointsManual: false,
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
