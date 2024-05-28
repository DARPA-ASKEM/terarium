export enum DistributionType {
	Constant = 'Constant',
	Uniform = 'StandardUniform1'
}

export const DistributionTypeLabel: { [key in DistributionType]: string } = {
	[DistributionType.Constant]: 'Constant',
	[DistributionType.Uniform]: 'Uniform'
};

export const distributionTypeOptions = (): { name: string; value: string }[] =>
	Object.values(DistributionType).map((dist) => ({
		name: DistributionTypeLabel[dist],
		value: dist
	}));
