import { DistributionType } from '@/services/distribution';
import { ParameterSemantic } from '@/types/Types';
import { calculateUncertaintyRange } from '@/utils/math';
import { v4 as uuidv4 } from 'uuid';

export const displayParameter = (parameters: ParameterSemantic[], parameterName: string) => {
	let value = '';
	const parameter = parameters.find((p) => p.referenceId === parameterName);
	switch (parameter?.distribution.type) {
		case DistributionType.Constant:
			value = `${parameter.distribution.parameters.value}`;
			break;
		case DistributionType.Uniform:
			value = `${parameter.distribution.parameters.minimum} - ${parameter.distribution.parameters.maximum}`;
			break;
		default:
			return '';
	}
	return `${parameterName}  [${value}]`;
};

/**
 * Generates the Cartesian product of the provided arrays.
 *
 * The Cartesian product of multiple arrays is a set of all possible combinations
 * where each combination contains one element from each array.
 *
 * @param arrays - An array of arrays, where each inner array contains elements to combine.
 * @returns An array of arrays, where each inner array is a combination of elements from the input arrays.
 *
 * Example:
 * const arrays = [
 *   ['low', 'high'],
 *   ['A', 'B'],
 *   [1, 2]
 * ];
 *
 * const result = cartesianProduct(arrays);
 * console.log(result);
 * // Output:
 * // [
 * //   ['low', 'A', 1],
 * //   ['low', 'A', 2],
 * //   ['low', 'B', 1],
 * //   ['low', 'B', 2],
 * //   ['high', 'A', 1],
 * //   ['high', 'A', 2],
 * //   ['high', 'B', 1],
 * //   ['high', 'B', 2]
 * // ]
 */
export function cartesianProduct<T>(arrays: T[][]): T[][] {
	return arrays.reduce((acc, curr) => acc.flatMap((a) => curr.map((b) => [...a, b])), [[]] as T[][]);
}

export const switchToUniformDistribution = (parameter: ParameterSemantic) => {
	if (parameter.distribution.type !== DistributionType.Uniform) {
		let minimum = 0;
		let maximum = 1;
		if (parameter.distribution.type === DistributionType.Constant) {
			// +10% and -10% of the constant value
			const { min, max } = calculateUncertaintyRange(parameter.distribution.parameters.value, 10);
			minimum = min;
			maximum = max;
		}
		parameter.distribution.type = DistributionType.Uniform;
		parameter.distribution.parameters = { minimum, maximum };
	}
};

export function usePolicyModel(props, interventionDropdowns, policyModalContext, isPolicyModalVisible) {
	const onOpenPolicyModel = (index: number) => {
		interventionDropdowns.value[index].hide();
		policyModalContext.value = index;
		isPolicyModalVisible.value = true;
	};

	const addNewPolicy = (newPolicyName: string) => {
		if (newPolicyName && policyModalContext.value !== null) {
			const id = uuidv4();
			props.scenario.setNewInterventionSpec(id, newPolicyName);
			props.scenario.setInterventionSpec(id, policyModalContext.value);
			isPolicyModalVisible.value = false;
			policyModalContext.value = null;
		}
	};

	return {
		onOpenPolicyModel,
		addNewPolicy
	};
}
