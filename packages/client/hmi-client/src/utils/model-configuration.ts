import { ModelConfiguration } from '@/types/Types';
import { cloneDeep, isEqual } from 'lodash';

export const areModelConfigValuesEqual = (
	originalConfig: ModelConfiguration | null,
	newConfig: ModelConfiguration | null
) => {
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
	const initialValuesSame = originalInitialList.every((originalItem) => {
		const newItem = initialMap.get(originalItem.target);
		return (
			!newItem ||
			(isEqual(originalItem.expression, newItem.expression) &&
				isEqual(originalItem.expressionMathml, newItem.expressionMathml))
		);
	});

	if (!initialValuesSame) return false;

	// compare parameter values are the same
	const parameterValuesSame = originalParameterList.every((originalItem) => {
		const newItem = parameterMap.get(originalItem.referenceId);
		return !newItem || isEqual(originalItem.distribution, newItem.distribution);
	});

	if (!parameterValuesSame) return false;

	// compare observable values are the same
	const observableValuesSame = originalObservableList.every((originalItem) => {
		const newItem = observableMap.get(originalItem.referenceId);
		return (
			!newItem ||
			(isEqual(originalItem.expression, newItem.expression) &&
				isEqual(originalItem.expressionMathml, newItem.expressionMathml))
		);
	});

	if (!observableValuesSame) return false;

	return true;
};
