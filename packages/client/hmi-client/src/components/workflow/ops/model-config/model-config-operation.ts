import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import type { ModelConfiguration } from '@/types/Types';
import { NotebookHistory } from '@/services/notebook';
import configureModel from '@assets/svg/operator-images/configure-model.svg';
import { cloneDeep, isEqual, omit, sortBy } from 'lodash';

export const name = 'ModelConfigOperation';

export interface ModelConfigOperationState extends BaseState {
	transientModelConfig: ModelConfiguration;
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
	datasetModelConfigTaskId: string;
	documentModelConfigTaskId: string;
}

export const blankModelConfig: ModelConfiguration = {
	id: '',
	modelId: '',
	name: '',
	description: '',
	simulationId: '',
	observableSemanticList: [],
	parameterSemanticList: [],
	initialSemanticList: [],
	inferredParameterList: []
};

export const ModelConfigOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_CONFIG,
	displayName: 'Configure model',
	description: 'Create model configurations.',
	imageUrl: configureModel,
	isRunnable: true,
	inputs: [
		{ type: 'modelId', label: 'Model' },
		{ type: 'documentId', label: 'Document', isOptional: true },
		{ type: 'datasetId', label: 'Dataset', isOptional: true }
	],
	outputs: [{ type: 'modelConfigId', label: 'Model configuration' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelConfigOperationState = {
			transientModelConfig: blankModelConfig,
			notebookHistory: [],
			hasCodeRun: false,
			datasetModelConfigTaskId: '',
			documentModelConfigTaskId: ''
		};
		return init;
	}
};

// check if model configs are equal
export const isModelConfigsEqual = (
	originalConfig: ModelConfiguration | null,
	newConfig: ModelConfiguration | null
): boolean => {
	if (!originalConfig || !newConfig) return false;
	originalConfig = cloneDeep(originalConfig);
	newConfig = cloneDeep(newConfig);

	// first check if the base keys are equal
	const keysToOmit = [
		'createdOn',
		'updatedOn',
		'initialSemanticList',
		'parameterSemanticList',
		'observableSemanticList',
		'inferredParameterList'
	];
	const baseKeysAreEqual = isEqual(omit(originalConfig, keysToOmit), omit(newConfig, keysToOmit));

	if (!baseKeysAreEqual) return false;

	// secondly check if the lists are equals
	const listKeysToOmit = ['createdOn', 'updatedOn', 'id'];
	const sortedOriginalConfig = {
		initialSemanticList: sortBy(
			originalConfig.initialSemanticList.map((item) => omit(item, listKeysToOmit)),
			['target']
		),
		parameterSemanticList: sortBy(
			originalConfig.parameterSemanticList.map((item) => omit(item, listKeysToOmit)),
			['referenceId']
		),
		observableSemanticList: sortBy(
			originalConfig.observableSemanticList.map((item) => omit(item, listKeysToOmit)),
			['referenceId']
		)
	};

	const sortedNewConfig = {
		initialSemanticList: sortBy(
			newConfig.initialSemanticList.map((item) => omit(item, listKeysToOmit)),
			['target']
		),
		parameterSemanticList: sortBy(
			newConfig.parameterSemanticList.map((item) => omit(item, listKeysToOmit)),
			['referenceId']
		),
		observableSemanticList: sortBy(
			newConfig.observableSemanticList.map((item) => omit(item, listKeysToOmit)),
			['referenceId']
		)
	};

	const listsAreEqual = isEqual(sortedOriginalConfig, sortedNewConfig);
	// compare initial values are the same

	if (!listsAreEqual) return false;

	return true;
};

// check if model config values are equal
export const areModelConfigValuesEqual = (
	originalConfig: ModelConfiguration | null,
	newConfig: ModelConfiguration | null
): boolean => {
	// if either of the configs is empty, return false
	if (!originalConfig || !newConfig) return false;
	// remove proxies
	originalConfig = cloneDeep(originalConfig);
	newConfig = cloneDeep(newConfig);

	const originalInitialList = originalConfig.initialSemanticList;
	const originalParameterList = originalConfig.parameterSemanticList;
	const originalObservableList = originalConfig.observableSemanticList;

	const newInitialList = newConfig.initialSemanticList;
	const newParameterList = newConfig.parameterSemanticList;
	const newObservableList = newConfig.observableSemanticList;

	const initialMap = new Map(newInitialList.map((item) => [item.target, item]));
	const parameterMap = new Map(newParameterList.map((item) => [item.referenceId, item]));
	const observableMap = new Map(newObservableList.map((item) => [item.referenceId, item]));

	// compare initial values are the same
	const initialValuesDifferent = originalInitialList.some((originalItem) => {
		const newItem = initialMap.get(originalItem.target);
		return (
			newItem &&
			(!isEqual(originalItem.expression, newItem.expression) ||
				!isEqual(originalItem.expressionMathml, newItem.expressionMathml))
		);
	});

	if (initialValuesDifferent) return false;

	// compare parameter values are the same
	const parameterValuesDifferent = originalParameterList.some((originalItem) => {
		const newItem = parameterMap.get(originalItem.referenceId);
		return newItem && !isEqual(originalItem.distribution, newItem.distribution);
	});

	if (parameterValuesDifferent) return false;

	// compare observable values are the same
	const observableValuesDifferent = originalObservableList.some((originalItem) => {
		const newItem = observableMap.get(originalItem.referenceId);
		return (
			newItem &&
			(!isEqual(originalItem.expression, newItem.expression) ||
				!isEqual(originalItem.expressionMathml, newItem.expressionMathml))
		);
	});

	if (observableValuesDifferent) return false;

	return true;
};
