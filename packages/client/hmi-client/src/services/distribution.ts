export enum DistributionType {
	Constant = 'Constant',
	Uniform = 'StandardUniform1'

	// Disabled until the backend is updated
	// StandardNormal1 = 'StandardNormal1',
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
	// Weibull1 = 'Weibull1',
	// Weibull2 = 'Weibull2'
}

// JSON Object Params
// [DistributionType.StandardNormal1]: ['mean', 'stdev'],
// [DistributionType.Normal1]: ['mean', 'stdev'],
// [DistributionType.Normal2]: ['mean', 'var'],
// [DistributionType.Normal3]: ['mean', 'precision'],
// [DistributionType.LogNormal1]: ['mean', 'stdevLog'],
// [DistributionType.LogNormal2]: ['meanLog', 'varLog'],
// [DistributionType.Bernoulli1]: ['probability'],
// [DistributionType.Bernoulli2]: ['logitProbability'],
// [DistributionType.Beta1]: ['alpha', 'beta'],
// [DistributionType.BetaBinomial1]: ['numberOfTrials', 'alpha', 'beta'],
// [DistributionType.Binomial1]: ['numberOfTrials', 'probability'],
// [DistributionType.Binomial2]: ['numberOfTrials', 'logitProbability'],
// [DistributionType.Cauchy1]: ['location', 'scale'],
// [DistributionType.ChiSquared1]: ['degreesOfFreedom'],
// [DistributionType.Dirichlet1]: ['concentration'],
// [DistributionType.Exponential1]: ['rate'],
// [DistributionType.Exponential2]: ['mean'],
// [DistributionType.Gamma1]: ['shape', 'scale'],
// [DistributionType.Gamma2]: ['shape', 'rate'],
// [DistributionType.InverseGamma1]: ['shape', 'scale'],
// [DistributionType.Gumbel1]: ['location', 'scale'],
// [DistributionType.Laplace1]: ['location', 'scale'],
// [DistributionType.Laplace2]: ['location', 'tau'],
// [DistributionType.ParetoTypeI1]: ['scale', 'shape'],
// [DistributionType.Poisson1]: ['rate'],
// [DistributionType.StudentT1]: ['degreesOfFreedom'],
// [DistributionType.StudentT2]: ['mean', 'scale', 'degreesOfFreedom'],
// [DistributionType.StudentT3]: ['degreesOfFreedom', 'location', 'scale'],
// [DistributionType.Weibull1]: ['scale', 'shape'],
// [DistributionType.Weibull2]: ['scale', 'shape']

export const externaDoclLink = 'https://pytorch.org/docs/stable/distributions.html';

export interface ProbOntoDistribution {
	label: string;
	parameters: {
		label: string;
		name: string;
		description: string;
	}[];
}

export const distributions: { [key in DistributionType]: ProbOntoDistribution } = {
	[DistributionType.Constant]: {
		label: 'Constant',
		parameters: [
			{
				label: 'Value',
				name: 'value',
				description: 'value description'
			}
		]
	},
	[DistributionType.Uniform]: {
		label: 'Uniform',
		parameters: [
			{
				label: 'Min',
				name: 'minimum',
				description: 'Lower bound of the interval'
			},
			{
				label: 'Max',
				name: 'maximum',
				description: 'Upper bound of the interval'
			}
		]
	}
};

export const distributionTypeOptions = (): { name: string; value: string }[] =>
	Object.values(DistributionType).map((dist) => ({
		name: distributions[dist].label,
		value: dist
	}));
