// export enum DistributionType {
// 	Constant = 'Constant',
// 	Uniform = 'StandardUniform1'
// }

export enum DistributionType {
	Constant = 'Constant',
	Uniform = 'StandardUniform1',
	StandardNormal1 = 'StandardNormal1'
	// Normal1 = 'Normal1',
	// Normal2 = 'Normal2',
	// Normal3 = 'Normal3',
	// LogNormal1 = 'LogNormal1',
	// LogNormal2 = 'LogNormal2',
	// Bernoulli1 = 'Bernoulli1',
	// Bernoulli2 = 'Bernoulli2',
	// Beta1 = 'Beta1',
	// BetaBinomial1 = 'BetaBinomial1',
	// Binomial1 = 'Binomial1',
	// Binomial2 = 'Binomial2',
	// Cauchy1 = 'Cauchy1',
	// ChiSquared1 = 'ChiSquared1',
	// Dirichlet1 = 'Dirichlet1',
	// Exponential1 = 'Exponential1',
	// Exponential2 = 'Exponential2',
	// Gamma1 = 'Gamma1',
	// Gamma2 = 'Gamma2',
	// InverseGamma1 = 'InverseGamma1',
	// Gumbel1 = 'Gumbel1',
	// Laplace1 = 'Laplace1',
	// Laplace2 = 'Laplace2',
	// ParetoTypeI1 = 'ParetoTypeI1',
	// Poisson1 = 'Poisson1',
	// StudentT1 = 'StudentT1',
	// StudentT2 = 'StudentT2',
	// StudentT3 = 'StudentT3',
	// Weibull1 = 'Weibull1',DistributionInputLabels
	// Weibull2 = 'Weibull2'
}

// export const InputOptionLabel = {
// 	Value = 'Constant',
// 	mean = 'Mean',
// 	minimum = 'Min',
// 	maximum = 'Max',
// 	stdev = 'Standard Deviation'
// }

export const DistributionInputOptions = {
	[DistributionType.Constant]: ['value'],
	[DistributionType.Uniform]: ['minimum', 'maximum'],
	[DistributionType.StandardNormal1]: ['mean', 'stdev']
};

export const DistributionInputLabels = {
	[DistributionType.Constant]: ['Constant'],
	[DistributionType.Uniform]: ['Min', 'Max'],
	[DistributionType.StandardNormal1]: ['mean', 'stdev']
};

export const DistributionTypeLabel: { [key in DistributionType]: string } = {
	[DistributionType.Constant]: 'Constant',
	[DistributionType.Uniform]: 'Uniform',
	[DistributionType.StandardNormal1]: 'Standard'
};

export const distributionTypeOptions = (): { name: string; value: string }[] =>
	Object.values(DistributionType).map((dist) => ({
		name: DistributionTypeLabel[dist],
		value: dist
	}));
