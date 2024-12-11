import { DistributionType } from '@/services/distribution';
import { ParameterSemantic } from '@/types/Types';
import { calculateUncertaintyRange } from '@/utils/math';

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
