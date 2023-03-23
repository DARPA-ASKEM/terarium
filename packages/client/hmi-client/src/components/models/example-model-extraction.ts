export const example = {
	'1': {
		type: 'variable',
		name: 'S',
		id: 'v0',
		text_annotations: [' Susceptible (uninfected)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ido:0000514', 'susceptible population']
		],
		data_annotations: [
			['covid_tracking.csv', 'negative'],
			['covid_tracking.csv', 'totalTestResults']
		],
		equation_annotations: { '\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['S'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'2': {
		type: 'variable',
		name: 'I',
		id: 'v1',
		text_annotations: [' Infected (asymptomatic or pauci-symptomatic infected, undetected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		data_annotations: [
			['covid_tracking.csv', 'positive'],
			['covid_tracking.csv', 'probableCases']
		],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['I'],
			'{\\dot{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['I']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'3': {
		type: 'variable',
		name: 'D',
		id: 'v2',
		text_annotations: [' Diagnosed (asymptomatic infected, detected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		data_annotations: [
			['covid_tracking.csv', 'positive'],
			['covid_tracking.csv', 'totalTestResults']
		],
		equation_annotations: {
			'{\\dot{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['D'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['D']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'4': {
		type: 'variable',
		name: 'A',
		id: 'v3',
		text_annotations: [' Ailing (symptomatic infected, undetected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		data_annotations: [
			['covid_tracking.csv', 'hospitalizedCurrently'],
			['covid_tracking.csv', 'hospitalizedCumulative']
		],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['A'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['A'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['A']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'5': {
		type: 'variable',
		name: 'R',
		id: 'v4',
		text_annotations: [' Recognized (symptomatic infected, detected)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ncit:C28554', 'Dead']
		],
		data_annotations: [
			['covid_tracking.csv', 'positive'],
			['covid_tracking.csv', 'death']
		],
		equation_annotations: { '{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': [] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'6': {
		type: 'variable',
		name: 'T',
		id: 'v5',
		text_annotations: [' Threatened (infected with life-threatening symptoms, detected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		data_annotations: [
			['covid_tracking.csv', 'hospitalizedCurrently'],
			['covid_tracking.csv', 'hospitalizedCumulative']
		],
		equation_annotations: { '{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': [] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'7': {
		type: 'variable',
		name: 'H',
		id: 'v6',
		text_annotations: [' Healed (recovered)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ncit:C28554', 'Dead']
		],
		data_annotations: [
			['covid_tracking.csv', 'recovered'],
			['nychealth.csv', 'recovered']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'8': {
		type: 'variable',
		name: 'E',
		id: 'v7',
		text_annotations: [' Extinct (dead)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		data_annotations: [
			['covid_tracking.csv', 'death'],
			['nychealth.csv', 'DEATH_COUNT']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'10': {
		type: 'variable',
		name: 'α',
		id: 'v8',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'positiveIncrease'],
			['covid_tracking.csv', 'negativeIncrease']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'11': {
		type: 'variable',
		name: 'β',
		id: 'v9',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['doid:0080928', 'dialysis-related amyloidosis'],
			['vo:0005114', 'β-propiolactone-inactivated SARS-CoV vaccine']
		],
		data_annotations: [
			['covid_tracking.csv', 'positiveIncrease'],
			['covid_tracking.csv', 'negativeIncrease']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'12': {
		type: 'variable',
		name: 'γ',
		id: 'v10',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['askemo:0000013', 'recovery rate'],
			['vo:0004915', 'vaccine specific interferon-γ immune response']
		],
		data_annotations: [
			['covid_tracking.csv', 'totalTestResultsSource'],
			['covid_tracking.csv', 'totalTestResults']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'13': {
		type: 'variable',
		name: 'δ',
		id: 'v11',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)ε'
		],
		dkg_annotations: [
			['askemo:0000011', 'progression rate'],
			['vo:0005123', 'VSVΔG-MERS vaccine']
		],
		data_annotations: [
			['covid_tracking.csv', 'totalTestResultsSource'],
			['covid_tracking.csv', 'totalTestResults']
		],
		equation_annotations: { '\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['δ'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'14': {
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'15': {
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'16': {
		type: 'variable',
		name: 'ζ',
		id: 'v13',
		text_annotations: [
			' Probability rate at which an infected subject not aware of being infected develops clinically relevant symptoms'
		],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'positiveIncrease'],
			['covid_tracking.csv', 'negativeIncrease']
		],
		equation_annotations: { '{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['ζ'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'17': {
		type: 'variable',
		name: 'λ',
		id: 'v17',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'recovered'],
			['Hospitals_and_Medical_Centers.csv', 'dis_status']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'18': {
		type: 'variable',
		name: 'η',
		id: 'v14',
		text_annotations: [
			' Probability rate at which an infected subject aware of being infected develops clinically relevant symptomsμ'
		],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'dateModified'],
			['covid_tracking.csv', 'checkTimeEt']
		],
		equation_annotations: {
			'{\\dot{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['η'],
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['η']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'19': {
		type: 'variable',
		name: 'ρ',
		id: 'v20',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		data_annotations: [['covid_tracking.csv', 'recovered'], ''],
		equation_annotations: { '{\\dot{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['ρ'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'20': {
		type: 'variable',
		name: 'θ',
		id: 'v12',
		text_annotations: [' Probability rate of detection relative to symptomatic cases'],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'dateModified'],
			['covid_tracking.csv', 'checkTimeEt']
		],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['θ'],
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['θ'],
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['θ'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['θ']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'21': {
		type: 'variable',
		name: 'κ',
		id: 'v18',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		data_annotations: [['covid_tracking.csv', 'recovered'], ''],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['κ'],
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['κ']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'22': {
		type: 'variable',
		name: 'ν',
		id: 'v15',
		text_annotations: [
			' Rate at which detected infected subjects develop life-threatening symptoms'
		],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'hospitalizedCurrently'],
			['covid_tracking.csv', 'hospitalizedCumulative']
		],
		equation_annotations: {
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['ν'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['ν']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'23': {
		type: 'variable',
		name: 'ξ',
		id: 'v19',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'recovered'],
			['Hospitals_and_Medical_Centers.csv', 'dis_status']
		],
		equation_annotations: {
			'\\scriptstyle I_{\\theta\\theta}=\\delta\\theta\\theta\\theta+I_{\\theta\\theta}+\\gamma\\xi\\theta_{1}+\\delta\\theta_{1})-\\epsilon+\\xi+\\delta I_{\\theta}':
				['ξ'],
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['ξ']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'24': {
		type: 'variable',
		name: 'σ',
		id: 'v21',
		text_annotations: [' Rate of recovery for infected subjectsNone'],
		dkg_annotations: [],
		data_annotations: ['', ''],
		equation_annotations: {
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['σ'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['σ']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'25': {
		type: 'variable',
		name: 'τ',
		id: 'v16',
		text_annotations: [' Mortality rate for infected subjects with life-threatening symptoms'],
		dkg_annotations: [],
		data_annotations: [
			['covid_tracking.csv', 'death'],
			['nychealth.csv', 'DEATH_COUNT']
		],
		equation_annotations: {
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['τ']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	}
};
