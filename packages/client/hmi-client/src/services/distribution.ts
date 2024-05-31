export enum DistributionType {
	Constant = 'Constant',
	Uniform = 'StandardUniform1'
}

export const DistributionTypeLabel: { [key in DistributionType]: string } = {
	[DistributionType.Constant]: 'Constant',
	[DistributionType.Uniform]: 'Uniform'
};

export const DistributionTypeDescription: { [key in DistributionType]: string } = {
	[DistributionType.Constant]: 'value is the constant value.',
	[DistributionType.Uniform]: 'low is the lower range, high is the upper range.'
};

export const distributionTypeOptions = (): { name: string; value: DistributionType }[] =>
	Object.values(DistributionType).map((dist) => ({
		name: DistributionTypeLabel[dist],
		value: dist
	}));
