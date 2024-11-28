export enum DistributionType {
	Constant = 'Constant',
	Uniform = 'StandardUniform1',

	// Disabled until the backend is updated
	StandardNormal1 = 'StandardNormal1',
	Normal1 = 'Normal1',
	Normal2 = 'Normal2',
	Normal3 = 'Normal3',
	LogNormal1 = 'LogNormal1',
	LogNormal2 = 'LogNormal2',
	Bernoulli1 = 'Bernoulli1',
	Bernoulli2 = 'Bernoulli2',
	Beta1 = 'Beta1',
	BetaBinomial1 = 'BetaBinomial1',
	Binomial1 = 'Binomial1',
	Binomial2 = 'Binomial2',
	Cauchy1 = 'Cauchy1',
	ChiSquared1 = 'ChiSquared1',
	Dirichlet1 = 'Dirichlet1',
	Exponential1 = 'Exponential1',
	Exponential2 = 'Exponential2',
	Gamma1 = 'Gamma1',
	Gamma2 = 'Gamma2',
	InverseGamma1 = 'InverseGamma1',
	Gumbel1 = 'Gumbel1',
	Laplace1 = 'Laplace1',
	Laplace2 = 'Laplace2',
	ParetoTypeI1 = 'ParetoTypeI1',
	Poisson1 = 'Poisson1',
	StudentT1 = 'StudentT1',
	StudentT2 = 'StudentT2',
	StudentT3 = 'StudentT3',
	Weibull1 = 'Weibull1',
	Weibull2 = 'Weibull2'
}

export const DistributionInputOptions = {
	[DistributionType.Constant]: ['value'],
	[DistributionType.Uniform]: ['minimum', 'maximum'],

	// Disabled until the backend is updated
	[DistributionType.StandardNormal1]: ['mean', 'stdev'],
	[DistributionType.Normal1]: ['mean', 'stdev'],
	[DistributionType.Normal2]: ['mean', 'var'],
	[DistributionType.Normal3]: ['mean', 'precision'],
	[DistributionType.LogNormal1]: ['mean', 'stdevLog'],
	[DistributionType.LogNormal2]: ['meanLog', 'varLog'],
	[DistributionType.Bernoulli1]: ['probability'],
	[DistributionType.Bernoulli2]: ['logitProbability'],
	[DistributionType.Beta1]: ['alpha', 'beta'],
	[DistributionType.BetaBinomial1]: ['numberOfTrials', 'alpha', 'beta'],
	[DistributionType.Binomial1]: ['numberOfTrials', 'probability'],
	[DistributionType.Binomial2]: ['numberOfTrials', 'logitProbability'],
	[DistributionType.Cauchy1]: ['location', 'scale'],
	[DistributionType.ChiSquared1]: ['degreesOfFreedom'],
	[DistributionType.Dirichlet1]: ['concentration'],
	[DistributionType.Exponential1]: ['rate'],
	[DistributionType.Exponential2]: ['mean'],
	[DistributionType.Gamma1]: ['shape', 'scale'],
	[DistributionType.Gamma2]: ['shape', 'rate'],
	[DistributionType.InverseGamma1]: ['shape', 'scale'],
	[DistributionType.Gumbel1]: ['location', 'scale'],
	[DistributionType.Laplace1]: ['location', 'scale'],
	[DistributionType.Laplace2]: ['location', 'tau'],
	[DistributionType.ParetoTypeI1]: ['scale', 'shape'],
	[DistributionType.Poisson1]: ['rate'],
	[DistributionType.StudentT1]: ['degreesOfFreedom'],
	[DistributionType.StudentT2]: ['mean', 'scale', 'degreesOfFreedom'],
	[DistributionType.StudentT3]: ['degreesOfFreedom', 'location', 'scale'],
	[DistributionType.Weibull1]: ['scale', 'shape'],
	[DistributionType.Weibull2]: ['scale', 'shape']
};

export const DistributionInputLabels = {
	[DistributionType.Constant]: ['Constant'],
	[DistributionType.Uniform]: ['Min', 'Max'],

	// Disabled until the backend is updated
	[DistributionType.StandardNormal1]: ['mean', 'stdev'],
	[DistributionType.Normal1]: ['mean', 'stdev'],
	[DistributionType.Normal2]: ['mean', 'variance'],
	[DistributionType.Normal3]: ['mean', 'precision'],
	[DistributionType.LogNormal1]: ['mean', 'shape'],
	[DistributionType.LogNormal2]: ['mean of log(x)', 'shape'],
	[DistributionType.Bernoulli1]: ['probability'],
	[DistributionType.Bernoulli2]: ['logitProbability'],
	[DistributionType.Beta1]: ['alpha', 'beta'],
	[DistributionType.BetaBinomial1]: ['numberOfTrials', 'alpha', 'beta'],
	[DistributionType.Binomial1]: ['numberOfTrials', 'probability'],
	[DistributionType.Binomial2]: ['numberOfTrials', 'logitProbability'],
	[DistributionType.Cauchy1]: ['location', 'scale'],
	[DistributionType.ChiSquared1]: ['degreesOfFreedom'],
	[DistributionType.Dirichlet1]: ['concentration'],
	[DistributionType.Exponential1]: ['rate'],
	[DistributionType.Exponential2]: ['mean'],
	[DistributionType.Gamma1]: ['shape', 'scale'],
	[DistributionType.Gamma2]: ['shape', 'rate'],
	[DistributionType.InverseGamma1]: ['shape', 'scale'],
	[DistributionType.Gumbel1]: ['location', 'scale'],
	[DistributionType.Laplace1]: ['location', 'scale'],
	[DistributionType.Laplace2]: ['location', 'precision'],
	[DistributionType.ParetoTypeI1]: ['scale', 'shape'],
	[DistributionType.Poisson1]: ['rate'],
	[DistributionType.StudentT1]: ['degreesOfFreedom'],
	[DistributionType.StudentT2]: ['mean', 'scale', 'degreesOfFreedom'],
	[DistributionType.StudentT3]: ['degreesOfFreedom', 'location', 'scale'],
	[DistributionType.Weibull1]: ['scale', 'shape'],
	[DistributionType.Weibull2]: ['scale', 'shape']
};

export const DistributionTypeLabel: { [key in DistributionType]: string } = {
	[DistributionType.Constant]: 'Constant',
	[DistributionType.Uniform]: 'Uniform',

	// Disabled until the backend is updated
	[DistributionType.StandardNormal1]: 'Standard',
	[DistributionType.Normal1]: 'Normal 1',
	[DistributionType.Normal2]: 'Normal 2',
	[DistributionType.Normal3]: 'Normal 3',
	[DistributionType.LogNormal1]: 'LogNormal 1',
	[DistributionType.LogNormal2]: 'LogNormal 2',
	[DistributionType.Bernoulli1]: 'Bernoulli 1',
	[DistributionType.Bernoulli2]: 'Bernoulli 2',
	[DistributionType.Beta1]: 'Beta 1',
	[DistributionType.BetaBinomial1]: 'BetaBinomial 1',
	[DistributionType.Binomial1]: 'Binomial 1',
	[DistributionType.Binomial2]: 'Binomial 2',
	[DistributionType.Cauchy1]: 'Cauchy 1',
	[DistributionType.ChiSquared1]: 'ChiSquared 1',
	[DistributionType.Dirichlet1]: 'Dirichlet ',
	[DistributionType.Exponential1]: 'Exponential 1',
	[DistributionType.Exponential2]: 'Exponential 2',
	[DistributionType.Gamma1]: 'Gamma 1',
	[DistributionType.Gamma2]: 'Gamma 2',
	[DistributionType.InverseGamma1]: 'InverseGamma 1',
	[DistributionType.Gumbel1]: 'Gumbel 1',
	[DistributionType.Laplace1]: 'Laplace 1',
	[DistributionType.Laplace2]: 'Laplace 2',
	[DistributionType.ParetoTypeI1]: 'ParetoType I 1',
	[DistributionType.Poisson1]: 'Poisson 1',
	[DistributionType.StudentT1]: 'StudentT 1',
	[DistributionType.StudentT2]: 'StudentT 2',
	[DistributionType.StudentT3]: 'StudentT 3',
	[DistributionType.Weibull1]: 'Weibull 1',
	[DistributionType.Weibull2]: 'Weibull2'
};

export const distributionTypeOptions = (): { name: string; value: string }[] =>
	Object.values(DistributionType).map((dist) => ({
		name: DistributionTypeLabel[dist],
		value: dist
	}));

export const distributionDescription = {
	[DistributionType.Constant]: 'Constants description',
	[DistributionType.Uniform]:
		'The uniform distribution represents a range of equally likely values between a minimum and maximum.',

	// Disabled until the backend is updated
	[DistributionType.StandardNormal1]: 'StandardNormal1 description',
	[DistributionType.Normal1]: 'Normal1 description'
};

export const distributionParamDescription = {
	value: 'Constant',
	minimum: 'Lower bound of the interval',
	maximum: 'Upper bound of the interval',

	// Disabled until the backend is updated
	mean: 'mean',
	stdev: 'stdev',
	var: 'variance',
	precision: 'precision',
	stdevLog: 'shape',
	meanLog: 'mean of log(x)',
	probability: 'probability of success',
	logitProbability: 'logit of probability of success',
	alpha: 'alpha',
	beta: 'beta',
	numberOfTrials: 'number of trials',
	location: 'location',
	scale: 'scale',
	shape: 'shape',
	degreesOfFreedom: 'degrees of freedom',
	concentration: 'concentration',
	rate: 'rate or inverse scale'
};
