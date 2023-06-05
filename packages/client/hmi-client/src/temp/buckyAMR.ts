import { Model } from '@/types/Types';

export const bucky: Model = {
	name: 'Bucky',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.2/petrinet/petrinet_schema.json',
	description:
		'The JHUAPL-Bucky model is a COVID-19 metapopulation compartment model initially designed to estimate medium-term (on the order of weeks) case incidence and healthcare usage at the second administrative (admin-2, ADM2) level (counties in the United States; cities or districts in various countries). It is documented at https://docs.buckymodel.com/en/latest/.',
	model_version: '0.1',
	model: {
		states: [
			{
				id: 'S',
				name: 'S',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'E',
				name: 'E',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'I_asym',
				name: 'I_asym',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'I_mild',
				name: 'I_mild',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'I_hosp',
				name: 'I_hosp',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'R',
				name: 'R',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'R_hosp',
				name: 'R_hosp',
				grounding: {
					identifiers: {},
					context: {}
				}
			},
			{
				id: 'D',
				name: 'D',
				grounding: {
					identifiers: {},
					context: {}
				}
			}
		],
		transitions: [
			{
				id: 't1',
				input: ['E', 'S'],
				output: ['E', 'E'],
				properties: {
					name: 't1',
					rate: {
						expression: 'E*S*beta',
						expression_mathml: '<apply><times/><ci>E</ci><ci>S</ci><ci>beta</ci></apply>'
					}
				}
			},
			{
				id: 't2',
				input: ['E'],
				output: ['I_asym'],
				properties: {
					name: 't2',
					rate: {
						expression: 'E*delta_1',
						expression_mathml: '<apply><times/><ci>E</ci><ci>delta_1</ci></apply>'
					}
				}
			},
			{
				id: 't3',
				input: ['E'],
				output: ['I_mild'],
				properties: {
					name: 't3',
					rate: {
						expression: 'E*delta_2',
						expression_mathml: '<apply><times/><ci>E</ci><ci>delta_2</ci></apply>'
					}
				}
			},
			{
				id: 't4',
				input: ['E'],
				output: ['I_hosp'],
				properties: {
					name: 't4',
					rate: {
						expression: 'E*delta_3',
						expression_mathml: '<apply><times/><ci>E</ci><ci>delta_3</ci></apply>'
					}
				}
			},
			{
				id: 't5',
				input: ['I_asym'],
				output: ['R'],
				properties: {
					name: 't5',
					rate: {
						expression: 'I_asym*gamma_1',
						expression_mathml: '<apply><times/><ci>I_asym</ci><ci>gamma_1</ci></apply>'
					}
				}
			},
			{
				id: 't6',
				input: ['I_mild'],
				output: ['R'],
				properties: {
					name: 't6',
					rate: {
						expression: 'I_mild*gamma_2',
						expression_mathml: '<apply><times/><ci>I_mild</ci><ci>gamma_2</ci></apply>'
					}
				}
			},
			{
				id: 't7',
				input: ['I_hosp'],
				output: ['R_hosp'],
				properties: {
					name: 't7',
					rate: {
						expression: 'I_hosp*delta_4',
						expression_mathml: '<apply><times/><ci>I_hosp</ci><ci>delta_4</ci></apply>'
					}
				}
			},
			{
				id: 't8',
				input: ['R_hosp'],
				output: ['R'],
				properties: {
					name: 't8',
					rate: {
						expression: 'R_hosp*tau',
						expression_mathml: '<apply><times/><ci>R_hosp</ci><ci>tau</ci></apply>'
					}
				}
			},
			{
				id: 't9',
				input: ['R_hosp'],
				output: ['D'],
				properties: {
					name: 't9',
					rate: {
						expression: 'R_hosp*delta_5',
						expression_mathml: '<apply><times/><ci>R_hosp</ci><ci>delta_5</ci></apply>'
					}
				}
			}
		],
		parameters: [
			{
				id: 'beta',
				value: 1.0
			},
			{
				id: 'delta_1',
				value: 1.0
			},
			{
				id: 'delta_2',
				value: 1.0
			},
			{
				id: 'delta_3',
				value: 1.0
			},
			{
				id: 'gamma_1',
				value: 1.0
			},
			{
				id: 'gamma_2',
				value: 1.0
			},
			{
				id: 'delta_4',
				value: 1.0
			},
			{
				id: 'tau',
				value: 1.0
			},
			{
				id: 'delta_5',
				value: 1.0
			}
		]
	},
	metadata: {
		variable_statements: [
			{
				id: 'R:190348269',
				variable: {
					id: 'Bucky',
					name: 'Bucky',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C67271',
							name: 'Bahamian',
							score: 0.7038567066192627
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'time',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'time, Bucky'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'R:159895595',
				variable: {
					id: 'SEIR',
					name: 'SEIR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000553',
							name: 'two-strain Susceptible-Infectious-Resistant model',
							score: 0.6902380585670471
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'spatially distributed',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'spatially distributed SEIR'
					}
				]
			},
			{
				id: 'E:-337831219',
				variable: {
					id: 'S',
					name: 'S',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.7847759127616882
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'S): the fraction of the population that could be potentially'
					}
				]
			},
			{
				id: 'E:-1921441554',
				variable: {
					id: 'asym frac',
					name: 'asym frac',
					metadata: [
						{
							type: '\u03b1',
							value: ' Rate of infections that are asymptomatic'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003118',
							name: 'M-M-R I',
							score: 0.5871734023094177
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'percentage of infections',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The percentage of infections that are asymptomatic, asym frac'
					}
				]
			},
			{
				id: 'E:392549189',
				variable: {
					id: 'asym frac',
					name: 'asym frac',
					metadata: [
						{
							type: 'asym_frac',
							value: ' Fraction of infections that are asymptomatic'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003118',
							name: 'M-M-R I',
							score: 0.5871734023094177
						}
					],
					column: [
						{
							id: '3-18',
							name: 'BX_ALL_CASE_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-21',
							name: 'BX_ALL_DEATH_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'percentage of infections',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The percentage of infections that are asymptomatic, asym'
					}
				]
			},
			{
				id: 'E:-1790112729',
				variable: {
					id: 'asym frac',
					name: 'asym frac',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003118',
							name: 'M-M-R I',
							score: 0.5871734023094177
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'percentage of infections',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The percentage of infections that are asymptomatic, asym frac'
					}
				]
			},
			{
				id: 'R:1852131141',
				variable: {
					id: 'frac',
					name: 'frac',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C43943',
							name: 'Mowa Band of Choctaw',
							score: 0.524211049079895
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'onset',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'onset frac'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					}
				]
			},
			{
				id: 'E:-1699720124',
				variable: {
					id: 'sym',
					name: 'sym',
					metadata: [
						{
							type: 'frac_trans_before_s',
							value: ' Percentage of transmission prior to symptom onset'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.6507763862609863
						}
					],
					column: [
						{
							id: '3-6',
							name: 'CASE_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-7',
							name: 'ALL_CASE_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of transmission prior to symptom onset frac trans before sym',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the fraction of transmission prior to symptom onset frac trans before sym which corresponds to the percentage of new cases that were caused by transmission from an individual before they become symptomatic'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					}
				]
			},
			{
				id: 'E:1004272435',
				variable: {
					id: 'OT20',
					name: 'OT20',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'upper bound of [PTC',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the upper bound of [PTC+20], which corresponds to the estimates from [OT20'
					}
				]
			},
			{
				id: 'E:-919484541',
				variable: {
					id: 'OT20',
					name: 'OT20',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'midpoint between the lower bound of [BCB',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'for this parameter is the midpoint between the lower bound of [BCB+20], the upper bound of [PTC+20], which corresponds to the estimates from [OT20'
					}
				]
			},
			{
				id: 'E:-1986634077',
				variable: {
					id: 'OT20',
					name: 'OT20',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'midpoint between the lower bound of [BCB',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the upper bound of [PTC+20], which corresponds to the estimates from [OT20'
					}
				]
			},
			{
				id: 'E:2056796685',
				variable: {
					id: 'PTC',
					name: 'PTC',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0004449',
							name: 'Prostate cancer DNA vaccine psig-3P-Fc encoding 3P(hPSM, hPAP, or hPSA)-Fc fusion protein',
							score: 0.5465795993804932
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'midpoint between the lower bound of [BCB',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the midpoint between the lower bound of [BCB+20], the upper bound of [PTC+20], which corresponds to the estimates'
					}
				]
			},
			{
				id: 'E:908668097',
				variable: {
					id: 'PTC',
					name: 'PTC',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0004449',
							name: 'Prostate cancer DNA vaccine psig-3P-Fc encoding 3P(hPSM, hPAP, or hPSA)-Fc fusion protein',
							score: 0.5465795993804932
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'upper bound of [PTC',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the upper bound of [PTC+20], which corresponds to the estimates'
					}
				]
			},
			{
				id: 'E:-778648429',
				variable: {
					id: 'PTC',
					name: 'PTC',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0004449',
							name: 'Prostate cancer DNA vaccine psig-3P-Fc encoding 3P(hPSM, hPAP, or hPSA)-Fc fusion protein',
							score: 0.5465795993804932
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'upper bound of [PTC',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the midpoint between the lower bound of [BCB+20], the upper bound of [PTC+20], which corresponds to the estimates'
					}
				]
			},
			{
				id: 'E:794209735',
				variable: {
					id: 'BCB',
					name: 'BCB',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011079',
							name: 'LpnA',
							score: 0.5031335353851318
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'midpoint between the lower bound of [BCB',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'for this parameter is the midpoint between the lower bound of [BCB'
					}
				]
			},
			{
				id: 'E:-1015169584',
				variable: {
					id: 'BCB',
					name: 'BCB',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011079',
							name: 'LpnA',
							score: 0.5031335353851318
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'upper bound of [PTC',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'BCB+20], the upper bound of [PTC+20], which corresponds to the estimates'
					}
				]
			},
			{
				id: 'E:1628974361',
				variable: {
					id: 'BCB',
					name: 'BCB',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011079',
							name: 'LpnA',
							score: 0.5031335353851318
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'upper bound of [PTC',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'for this parameter is the midpoint between the lower bound of [BCB'
					}
				]
			},
			{
				id: 'E:-154444734',
				variable: {
					id: 'asym',
					name: 'asym',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011206',
							name: 'Gnd',
							score: 0.5487262010574341
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'relative infectiousness of asymptomatic individuals',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'The relative infectiousness of asymptomatic individuals compared to symptomatic individuals rel inf asym'
					}
				]
			},
			{
				id: 'R:-1574038365',
				variable: {
					id: 'asym',
					name: 'asym',
					metadata: [
						{
							type: 'rel_inf_asym',
							value: ' Relative infectiousness of asymptomatic individuals'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0011206',
							name: 'Gnd',
							score: 0.5487262010574341
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'rel inf',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'rel inf asym'
					}
				]
			},
			{
				id: 'E:-792400196',
				variable: {
					id: 'asym',
					name: 'asym',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011206',
							name: 'Gnd',
							score: 0.5487262010574341
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'rel inf',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'The relative infectiousness of asymptomatic individuals compared to symptomatic individuals rel inf asym'
					}
				]
			},
			{
				id: 'E:1705111281',
				variable: {
					id: 'Ts',
					name: 'Ts',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0004615',
							name: 'pcDNA3-MCC/ST',
							score: 0.5528414845466614
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'time in days from exposure to onset of symptoms',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'Ts, is the time in days from exposure to onset of symptoms'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					}
				]
			},
			{
				id: 'E:1933623000',
				variable: {
					id: 'Severity',
					name: 'Severity',
					metadata: [
						{
							type: 'I_to_H_time',
							value: ' Time it takes from symptom onset to hospitalization (in days)'
						}
					],
					dkg_groundings: [
						{
							id: 'ncit:C168970',
							name: 'Death due to Disease Progression',
							score: 0.6269740462303162
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'time in days from exposure to onset of symptoms',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'Severity The mean serial interval, Ts, is the time in days from exposure to onset of symptoms'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					}
				]
			},
			{
				id: 'E:-870819115',
				variable: {
					id: 'D REPORT TIME',
					name: 'D REPORT TIME',
					metadata: [
						{
							type: 'D_i, j',
							value: ' Proportion of individuals who have succumbed as a direct result of the virus'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000272',
							name: 'time since time scale zero',
							score: 0.7092307209968567
						}
					],
					column: [
						{
							id: '3-4',
							name: 'DEATH_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'number of days between death and reporting',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'number of days between death and reporting is D REPORT TIME'
					}
				]
			},
			{
				id: 'E:-1832746855',
				variable: {
					id: 'Tg',
					name: 'Tg',
					metadata: [
						{
							type: 'D_REPORT_TIME',
							value: ' Time between death and reporting'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0011290',
							name: 'WD',
							score: 0.5086898803710938
						}
					],
					column: [
						{
							id: '3-4',
							name: 'DEATH_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'period of time (in days',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'Tg, is the period of time (in days) between symptom onset for one individual and symptom onset for a person'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'E:1065347612',
				variable: {
					id: 'Tg',
					name: 'Tg',
					metadata: [
						{
							type: 'Tg',
							value: ' Mean generation interval (in days)'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0011290',
							name: 'WD',
							score: 0.5086898803710938
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'mean generation interval',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The mean generation interval, Tg'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'E:149379061',
				variable: {
					id: 'Tg',
					name: 'Tg',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011290',
							name: 'WD',
							score: 0.5086898803710938
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'mean generation interval',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'Tg, is the period of time (in days) between symptom onset for one individual and symptom onset for a person'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'R:1017467073',
				variable: {
					id: 'Response (DCIPHER)',
					name: 'Response (DCIPHER)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0005308',
							name: 'immune response profile',
							score: 0.8189837336540222
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: "CDC's Data Collation and Integration for Public Health Event Response",
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: "CDC's Data Collation and Integration for Public Health Event Response (DCIPHER)"
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:371302679',
				variable: {
					id: 'H TIME',
					name: 'H TIME',
					metadata: [
						{
							type: 'H_TIME',
							value: ' Duration of hospitalization'
						},
						{
							type: 'H_TIME',
							value: ' Number of days anfrac'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000272',
							name: 'time since time scale zero',
							score: 0.7241774201393127
						},
						{
							id: 'pr:Q9UNS1',
							name: 'protein timeless homolog (human)',
							score: null
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'number of days',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'number of days an individual will be hospitalized is H TIME'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:352362914',
				variable: {
					id: 'HLW',
					name: 'HLW',
					metadata: [
						{
							type: 'I_TO_H_TIME',
							value: ' Time from symptom onset to hospitalization'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0011206',
							name: 'Gnd',
							score: 0.6764112114906311
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'value',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'value is from [HLW'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'E:1806172056',
				variable: {
					id: 'value',
					name: 'value',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000433',
							name: 'maximum value',
							score: 0.8762068152427673
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: '+20',
					type: 'value',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'value is from [HLW+20'
					},
					{
						type: 'scenario_location',
						value: 'CGM'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'E:-358159169',
				variable: {
					id: 'CHR',
					name: 'CHR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0010879',
							name: 'GatA',
							score: 0.6231651306152344
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'number of individuals',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the number of individuals who will die of the disease; the case hospitalization-severity ratio (CHR) corresponds to the number of cases that are severe and necessitate hospitalization'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'R:-1701961284',
				variable: {
					id: 'CHR',
					name: 'CHR',
					metadata: [
						{
							type: 'H',
							value: ' Case hospitalization ratio'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0010879',
							name: 'GatA',
							score: 0.6231651306152344
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'hospitalization-severity ratio',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'hospitalization-severity ratio (CHR)'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:1875304339',
				variable: {
					id: 'CHR',
					name: 'CHR',
					metadata: [
						{
							type: 'CHR',
							value: ' Case hospitalization-severity ratio'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0010879',
							name: 'GatA',
							score: 0.6231651306152344
						},
						{
							id: 'hp:0011010',
							name: 'Chronic',
							score: null
						},
						{
							id: 'ncbitaxon:27458',
							name: 'Chrysops',
							score: null
						}
					],
					column: [
						{
							id: '3-8',
							name: 'HOSP_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-3',
							name: 'HOSPITALIZED_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'case',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the case hospitalization-severity ratio (CHR'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:-348336884',
				variable: {
					id: 'CHR',
					name: 'CHR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0010879',
							name: 'GatA',
							score: 0.6231651306152344
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'case',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the number of individuals who will die of the disease; the case hospitalization-severity ratio (CHR) corresponds to the number of cases that are severe and necessitate hospitalization'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:1881460677',
				variable: {
					id: 'CFR',
					name: 'CFR',
					metadata: [
						{
							type: 'F',
							value: ' Case fatality ratio'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000445',
							name: 'mortality rate',
							score: 0.6657055020332336
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'case fatality ratio',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The case fatality ratio (CFR'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:1234594209',
				variable: {
					id: 'CFR',
					name: 'CFR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000445',
							name: 'mortality rate',
							score: 0.6657055020332336
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'number of individuals',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'CFR) is the number of individuals who will die of the disease; the case hospitalization-severity ratio (CHR) corresponds to the number of cases that are severe and necessitate hospitalization'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'E:-1776223755',
				variable: {
					id: 'CFR',
					name: 'CFR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000445',
							name: 'mortality rate',
							score: 0.6657055020332336
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'number of individuals',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The case fatality ratio (CFR'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					},
					{
						type: 'scenario_location',
						value: 'Data Collation and Integration for Public Health Event Response'
					},
					{
						type: 'scenario_location',
						value: 'CDC'
					},
					{
						type: 'scenario_location',
						value: 'DCIPHER'
					},
					{
						type: 'scenario_location',
						value: "CDC's Data Collation and Integration for Public Health Event Response"
					}
				]
			},
			{
				id: 'R:332363993',
				variable: {
					id: 'Mobility',
					name: 'Mobility',
					metadata: [
						{
							type: 'Mobility Based NPI',
							value:
								' NPIs that lead to changes in mobility/movement between administrative districts.'
						}
					],
					dkg_groundings: [
						{
							id: 'cemo:per_capita_mobility',
							name: 'per capita mobility',
							score: 0.6922377943992615
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'country',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'country Mobility'
					},
					{
						type: 'scenario_location',
						value: 'Monte Carlo ID'
					}
				]
			},
			{
				id: 'E:-1974911268',
				variable: {
					id: 'matrix',
					name: 'matrix',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0011170',
							name: 'M matrix protein',
							score: 0.7306210398674011
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: '16 x 16',
					type: 'value',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'matrix must be of size 16 x 16'
					},
					{
						type: 'scenario_location',
						value: 'Afghanistan'
					},
					{
						type: 'scenario_location',
						value: 'Pakistan'
					}
				]
			},
			{
				id: 'E:1791754575',
				variable: {
					id: 'NPIs',
					name: 'NPIs',
					metadata: [
						{
							type: 'Contact-Matrix Based NPIs',
							value: ' NPIs that effect certain age groups within the total population.'
						}
					],
					dkg_groundings: [
						{
							id: 'ncit:C173636',
							name: 'Social Distancing',
							score: 0.6251343488693237
						}
					],
					column: [
						{
							id: '4-1',
							name: 'demographic_value',
							dataset: {
								id: '4',
								name: 'StatewideCOVID-19CasesDeathsDemographics.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'effect of NPIs',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the effect of NPIs'
					},
					{
						type: 'scenario_time',
						value: 'February 2020'
					}
				]
			},
			{
				id: 'R:622447771',
				variable: {
					id: 'NPIs',
					name: 'NPIs',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C173636',
							name: 'Social Distancing',
							score: 0.6251343488693237
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'model',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'model NPIs'
					},
					{
						type: 'scenario_time',
						value: 'February 2020'
					}
				]
			},
			{
				id: 'R:-1225449041',
				variable: {
					id: 'Reduction',
					name: 'Reduction',
					metadata: [
						{
							type: 'Reproduction Number Based NPI',
							value: ' NPIs that have an effect on the overall scaling of transmissibility.'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0000242',
							name: 'CFU reduction assay',
							score: 0.7115188837051392
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'mask wearing  Reduction of size of public gatherings Closing businesses',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'mask wearing  Reduction of size of public gatherings Closing businesses'
					},
					{
						type: 'scenario_location',
						value: 'NPI'
					}
				]
			},
			{
				id: 'E:-366044198',
				variable: {
					id: 'Hopkins University',
					name: 'Hopkins University',
					metadata: [
						{
							type: '$1 / \tau_{i}$',
							value: ' Rate of recovery after period of illness'
						}
					],
					dkg_groundings: [
						{
							id: 'ncit:C48647',
							name: 'University',
							score: 0.7616034746170044
						}
					],
					column: [
						{
							id: '3-19',
							name: 'BX_HOSPITALIZED_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-30',
							name: 'BK_HOSPITALIZED_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Center for Systems Science and Engineering',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'CSSE'
					},
					{
						type: 'scenario_location',
						value: 'Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'Center for Systems Science and Engineering'
					},
					{
						type: 'scenario_location',
						value: 'Reds'
					}
				]
			},
			{
				id: 'E:-485241326',
				variable: {
					id: 'Hopkins University',
					name: 'Hopkins University',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C48647',
							name: 'University',
							score: 0.7616034746170044
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Center for Systems Science and Engineering',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the Center for Systems Science and Engineering (CSSE) at Johns Hopkins'
					},
					{
						type: 'scenario_location',
						value: 'CSSE'
					},
					{
						type: 'scenario_location',
						value: 'Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'Center for Systems Science and Engineering'
					},
					{
						type: 'scenario_location',
						value: 'Reds'
					}
				]
			},
			{
				id: 'E:-1080012907',
				variable: {
					id: 'Hopkins University',
					name: 'Hopkins University',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C48647',
							name: 'University',
							score: 0.7616034746170044
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Center for Systems Science and Engineering',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'CSSE'
					},
					{
						type: 'scenario_location',
						value: 'Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'Center for Systems Science and Engineering'
					},
					{
						type: 'scenario_location',
						value: 'Reds'
					}
				]
			},
			{
				id: 'E:-1334255455',
				variable: {
					id: 'Engineering (CSSE)',
					name: 'Engineering (CSSE)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0005218',
							name: 'Center for Genetic Engineering and Biotechnology',
							score: 0.5307190418243408
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Center for Systems Science and Engineering',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							"the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University'' https://github.com/ CSSEGISandData"
					},
					{
						type: 'scenario_location',
						value: 'CSSE'
					},
					{
						type: 'scenario_location',
						value: 'Johns Hopkins University'
					},
					{
						type: 'scenario_location',
						value: 'Center for Systems Science and Engineering'
					},
					{
						type: 'scenario_location',
						value: 'Reds'
					}
				]
			},
			{
				id: 'E:1360514627',
				variable: {
					id: 'E',
					name: 'E',
					metadata: [
						{
							type: '\u03bd_i',
							value:
								' Fraction of individuals in age group i who are susceptible to the virusI^{\text {asym }}'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0010921',
							name: 'LolC/E',
							score: 1.0000001192092896
						}
					],
					column: [
						{
							id: '4-3',
							name: 'percent_cases',
							dataset: {
								id: '4',
								name: 'StatewideCOVID-19CasesDeathsDemographics.csv',
								metadata: null
							}
						},
						{
							id: '4-5',
							name: 'percent_deaths',
							dataset: {
								id: '4',
								name: 'StatewideCOVID-19CasesDeathsDemographics.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the fraction of the population that could be potentially subjected to the infection; exposed (E): the fraction of the population that has been infected'
					}
				]
			},
			{
				id: 'E:-1448492043',
				variable: {
					id: 'E',
					name: 'E',
					metadata: [
						{
							type: 'I_i, j^asym',
							value: ' Proportion of individuals who are infected but asymptomatic'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0010921',
							name: 'LolC/E',
							score: 1.0000001192092896
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'E): the fraction of the population that has been infected'
					}
				]
			},
			{
				id: 'R:-506465753',
				variable: {
					id: 'E',
					name: 'E',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0010921',
							name: 'LolC/E',
							score: 1.0000001192092896
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'exposed',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'exposed (E)'
					}
				]
			},
			{
				id: 'E:1491480210',
				variable: {
					id: 'E',
					name: 'E',
					metadata: [
						{
							type: 'E_i j',
							value: ' Proportion of individuals who have been exposed to the virus'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0010921',
							name: 'LolC/E',
							score: 1.0000001192092896
						},
						{
							id: 'vsmo:0000766',
							name: 'Anopheles annularis',
							score: null
						}
					],
					column: [
						{
							id: '3-2',
							name: 'PROBABLE_CASE_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-3',
							name: 'HOSPITALIZED_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'exposed',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the fraction of the population that could be potentially subjected to the infection; exposed (E): the fraction of the population that has been infected'
					}
				]
			},
			{
				id: 'E:-754456012',
				variable: {
					id: 'S',
					name: 'S',
					metadata: [
						{
							type: 'S_i j',
							value: ' Proportion of individuals who are susceptible to the virus'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.7847759127616882
						},
						{
							id: 'vsmo:0002217',
							name: 'Oropsylla bruneri',
							score: null
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'S): the fraction of the population that could be potentially subjected to the infection; exposed (E): the fraction of the population that has been infected'
					}
				]
			},
			{
				id: 'E:1082544296',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [
						{
							type: 'I_i, j^mild',
							value: ' Proportion of individuals that are exhibiting mild disease symptoms'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [
						{
							id: '1-4',
							name: 'cases',
							dataset: {
								id: '1',
								name: 'StatewideCOVID-19CasesDeathsTests.csv',
								metadata: null
							}
						},
						{
							id: '1-5',
							name: 'cumulative_cases',
							dataset: {
								id: '1',
								name: 'StatewideCOVID-19CasesDeathsTests.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'I): the fraction of the population that is infective'
					}
				]
			},
			{
				id: 'R:1173208709',
				variable: {
					id: 'E',
					name: 'E',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0010921',
							name: 'LolC/E',
							score: 1.0000001192092896
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'exposed',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'exposed (E)'
					}
				]
			},
			{
				id: 'E:-1853256299',
				variable: {
					id: 'R',
					name: 'R',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the fraction of the population that is infective after the latent period; recovered (R'
					}
				]
			},
			{
				id: 'E:-662247115',
				variable: {
					id: 'R',
					name: 'R',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'R): The fraction of the population that have been infected'
					}
				]
			},
			{
				id: 'E:812107482',
				variable: {
					id: 'R',
					name: 'R',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the fraction of the population that is infective after the latent period; recovered (R'
					}
				]
			},
			{
				id: 'E:1572319907',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'I): the fraction of the population that is infective after the latent period; recovered (R): The fraction of the population that have been infected'
					}
				]
			},
			{
				id: 'E:1453214595',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'I): the fraction of the population that is infective after the latent period'
					}
				]
			},
			{
				id: 'E:-1443404403',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'I): the fraction of the population that is infective after the latent period; recovered (R): The fraction of the population that have been infected'
					}
				]
			},
			{
				id: 'E:-653097281',
				variable: {
					id: 'R',
					name: 'R',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'fraction of the population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'R): The fraction of the population that have been infected and recovered from the infection'
					}
				]
			},
			{
				id: 'E:-605902488',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [
						{
							type: 'R_i, j',
							value:
								' Proportion of individuals who have recovered from the virus and are no longer capable of infecting other individuals'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [
						{
							id: '5-14',
							name: 'recovered',
							dataset: {
								id: '5',
								name: 'covid_tracking.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'compartments E',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'The compartments E, I asym, I mild, I hosp, Rhosp are gamma-distributed with shape parameters specified in the configuration file'
					}
				]
			},
			{
				id: 'R:1214187488',
				variable: {
					id: 'sigma alpha',
					name: 'sigma alpha',
					metadata: [
						{
							type: '\u03c3',
							value: ' Viral latent period'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000078',
							name: 'beta distribution',
							score: 0.5320796370506287
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Viral latent period',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'Viral latent period sigma alpha'
					}
				]
			},
			{
				id: 'R:-499529494',
				variable: {
					id: 'SEIR',
					name: 'SEIR',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000553',
							name: 'two-strain Susceptible-Infectious-Resistant model',
							score: 0.6902380585670471
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'stratified',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'stratified SEIR'
					}
				]
			},
			{
				id: 'R:855559718',
				variable: {
					id: 'j betai',
					name: 'j betai',
					metadata: [
						{
							type: '\u03b2_i, j',
							value: ' Force of infection on a member of age group i in location j'
						}
					],
					dkg_groundings: [],
					column: [
						{
							id: '0-9',
							name: 'Column Level Metadata',
							dataset: {
								id: '0',
								name: 'COVID-19_Reported_Patient_Impact_and_Hospital_Capacity_by_State_Archive_Repository.csv',
								metadata: null
							}
						},
						{
							id: '0-10',
							name: 'Column Level Metadata Updates',
							dataset: {
								id: '0',
								name: 'COVID-19_Reported_Patient_Impact_and_Hospital_Capacity_by_State_Archive_Repository.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'location j',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'location j betai'
					}
				]
			},
			{
				id: 'R:960004975',
				variable: {
					id: 'gamma',
					name: 'gamma',
					metadata: [
						{
							type: '$\\gamma$',
							value: ' Rate of recovery from virus'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000255',
							name: 'gamma distribution',
							score: 0.8174800872802734
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'mild symptoms recover from the virus at a rate gamma',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'mild symptoms recover from the virus at a rate gamma'
					}
				]
			},
			{
				id: 'R:2082097276',
				variable: {
					id: 'gamma',
					name: 'gamma',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000255',
							name: 'gamma distribution',
							score: 0.8174800872802734
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'virus at a rate',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'virus at a rate gamma'
					}
				]
			},
			{
				id: 'E:-2117473741',
				variable: {
					id: 'gamma',
					name: 'gamma',
					metadata: [
						{
							type: 'I^{\text {hosp }}',
							value: ' Severe infected state'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000255',
							name: 'gamma distribution',
							score: 0.8174800872802734
						}
					],
					column: [
						{
							id: '3-3',
							name: 'HOSPITALIZED_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the population who are either asymptomatic or exhibit mild symptoms recover from the virus at a rate gamma'
					}
				]
			},
			{
				id: 'R:-1775407602',
				variable: {
					id: 'gamma',
					name: 'gamma',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000255',
							name: 'gamma distribution',
							score: 0.8174800872802734
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'population',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'mild symptoms recover from the virus at a rate gamma'
					}
				]
			},
			{
				id: 'E:415297888',
				variable: {
					id: 'Modeling',
					name: 'Modeling',
					metadata: [
						{
							type: 'R_i, j^hosp',
							value:
								' Proportion of individuals who have recovered from the virus after a period of time in a hospital'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000565',
							name: 'computational modeling toolkit',
							score: 0.807548999786377
						}
					],
					column: [
						{
							id: '9-57',
							name: 'previous_day_admission_adult_covid_confirmed',
							dataset: {
								id: '9',
								name: 'Reported_State_Hospital_Capacity_and_COVID19_Patient_Impact.csv',
								metadata: null
							}
						},
						{
							id: '9-58',
							name: 'previous_day_admission_adult_covid_confirmed_coverage',
							dataset: {
								id: '9',
								name: 'Reported_State_Hospital_Capacity_and_COVID19_Patient_Impact.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'important component in accurately modeling non-pharmaceutical Interventions',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'Modeling of the overall interaction rates between geographic locations and age groups is an important component in accurately modeling non-pharmaceutical Interventions'
					}
				]
			},
			{
				id: 'R:1972177188',
				variable: {
					id: 'Interventions (NPIs)',
					name: 'Interventions (NPIs)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C113362',
							name: 'Findings About Events or Interventions Evaluator',
							score: 0.6719470024108887
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'accurately modeling non-pharmaceutical Interventions',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'accurately modeling non-pharmaceutical Interventions (NPIs)'
					}
				]
			},
			{
				id: 'R:-805734678',
				variable: {
					id: 'Interventions (NPIs)',
					name: 'Interventions (NPIs)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C113362',
							name: 'Findings About Events or Interventions Evaluator',
							score: 0.6719470024108887
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'modeling non-pharmaceutical',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'modeling non-pharmaceutical Interventions (NPIs)'
					}
				]
			},
			{
				id: 'R:-1355572949',
				variable: {
					id: 'Interventions (NPIs)',
					name: 'Interventions (NPIs)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C113362',
							name: 'Findings About Events or Interventions Evaluator',
							score: 0.6719470024108887
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'modeling non-pharmaceutical',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'modeling non-pharmaceutical Interventions'
					}
				]
			},
			{
				id: 'R:1741682726',
				variable: {
					id: 'Interventions (NPIs)',
					name: 'Interventions (NPIs)',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C113362',
							name: 'Findings About Events or Interventions Evaluator',
							score: 0.6719470024108887
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'modeling non-pharmaceutical',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'accurately modeling non-pharmaceutical Interventions (NPIs)'
					}
				]
			},
			{
				id: 'E:592467200',
				variable: {
					id: 'COVID',
					name: 'COVID',
					metadata: [
						{
							type: '$\\phi_{i} \\gamma$',
							value: ' Rate of expiration from virus'
						}
					],
					dkg_groundings: [
						{
							id: 'ncit:C171133',
							name: 'COVID-19 Infection',
							score: 0.7247472405433655
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'spatial dynamics of COVID spread',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'the spatial dynamics of COVID spread'
					}
				]
			},
			{
				id: 'E:737294122',
				variable: {
					id: 'Bucky',
					name: 'Bucky',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C67271',
							name: 'Bahamian',
							score: 0.7038567066192627
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'parameterization of the model',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'Bucky model is the parameterization of the model'
					}
				]
			},
			{
				id: 'R:-1676483649',
				variable: {
					id: 'j.',
					name: 'j.',
					metadata: [],
					dkg_groundings: [],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'index',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'index j.'
					}
				]
			},
			{
				id: 'R:1340323145',
				variable: {
					id: 'i',
					name: 'i',
					metadata: [],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'index',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'index i'
					}
				]
			},
			{
				id: 'E:-79999377',
				variable: {
					id: 'Bucky',
					name: 'Bucky',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C67271',
							name: 'Bahamian',
							score: 0.7038567066192627
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value:
						'susceptible and exposed populations, followed by one of three possible infected states',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'Bucky models the susceptible and exposed populations, followed by one of three possible infected states'
					}
				]
			},
			{
				id: 'E:-1397447272',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [
						{
							type: 'I_i, j^hosp',
							value:
								' Proportion of individuals that are exhibiting severe disease symptoms and are in need of hospitalization'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [
						{
							id: '3-3',
							name: 'HOSPITALIZED_COUNT',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						},
						{
							id: '3-8',
							name: 'HOSP_COUNT_7DAY_AVG',
							dataset: {
								id: '3',
								name: 'nychealth.csv',
								metadata: null
							}
						}
					],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value:
						'susceptible and exposed populations, followed by one of three possible infected states',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the susceptible and exposed populations, followed by one of three possible infected states: asymptomatic (I asym), mild (I mild), and severe (I'
					}
				]
			},
			{
				id: 'E:-2087133106',
				variable: {
					id: 'I',
					name: 'I',
					metadata: [
						{
							type: 'I^{\text {mild }}',
							value: ' Mild infected state'
						}
					],
					dkg_groundings: [
						{
							id: 'vo:0003109',
							name: 'Meruvax I',
							score: 0.994598925113678
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value:
						'susceptible and exposed populations, followed by one of three possible infected states',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value:
							'the susceptible and exposed populations, followed by one of three possible infected states: asymptomatic (I'
					}
				]
			},
			{
				id: 'R:1218866417',
				variable: {
					id: 'R0',
					name: 'R0',
					metadata: [
						{
							type: '$R_{0}$',
							value: ' Basic reproductive number'
						},
						{
							type: '$R_{0}$',
							value: ' Reproduction number-based reduction in overall community transmission'
						}
					],
					dkg_groundings: [
						{
							id: 'apollosv:00000404',
							name: 'infectiousness',
							score: 0.6776172518730164
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'basic reproductive number',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'basic reproductive number, R0'
					}
				]
			},
			{
				id: 'E:-2072614022',
				variable: {
					id: 'NPI',
					name: 'NPI',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000531',
							name: 'epidemic threshold',
							score: 0.596771776676178
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'ratios relating the components of the contact matrices',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'NPI effect the ratios relating the components of the contact matrices'
					}
				]
			},
			{
				id: 'E:1772689243',
				variable: {
					id: 'NPI',
					name: 'NPI',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000531',
							name: 'epidemic threshold',
							score: 0.596771776676178
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'School Closure',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'NPI that fall under this category are: School Closure'
					}
				]
			},
			{
				id: 'E:1511867659',
				variable: {
					id: 'NPI',
					name: 'NPI',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000531',
							name: 'epidemic threshold',
							score: 0.596771776676178
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'classification',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'classification is for those NPI'
					},
					{
						type: 'scenario_location',
						value: 'NPI'
					}
				]
			},
			{
				id: 'E:182588837',
				variable: {
					id: 'NPI',
					name: 'NPI',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000531',
							name: 'epidemic threshold',
							score: 0.596771776676178
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'classification',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'classification is for those NPI'
					},
					{
						type: 'scenario_location',
						value: 'NPI'
					}
				]
			},
			{
				id: 'E:930780346',
				variable: {
					id: 'NPI',
					name: 'NPI',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000531',
							name: 'epidemic threshold',
							score: 0.596771776676178
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Social distancing',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'NPI that fall under this category are : Social distancing'
					}
				]
			},
			{
				id: 'E:1197920639',
				variable: {
					id: 'California',
					name: 'California',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C43915',
							name: 'California Tribes',
							score: 0.836229145526886
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'are shown below for the United States, California',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'are shown below for the United States, California,'
					},
					{
						type: 'scenario_location',
						value: 'Riverside county'
					},
					{
						type: 'scenario_location',
						value: 'California'
					},
					{
						type: 'scenario_location',
						value: 'United States'
					}
				]
			},
			{
				id: 'R:1733093926',
				variable: {
					id: 'Dingel',
					name: 'Dingel',
					metadata: [],
					dkg_groundings: [
						{
							id: 'ncit:C43914',
							name: 'Torres-Martinez',
							score: 0.7248883247375488
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'Green',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'Dingel, Green,'
					},
					{
						type: 'scenario_location',
						value: 'Michael S. & Skillman'
					},
					{
						type: 'scenario_location',
						value: 'AR'
					},
					{
						type: 'scenario_location',
						value: 'Prem K'
					}
				]
			},
			{
				id: 'E:-82795796',
				variable: {
					id: 'Infection',
					name: 'Infection',
					metadata: [],
					dkg_groundings: [
						{
							id: 'apollosv:00000114',
							name: 'infection',
							score: 0.9989686012268066
						}
					],
					column: [],
					paper: {
						id: '066aede4-bda8-407e-934f-574b6fc59788',
						name: 'buckymodel_webdocs.pdf',
						md5: '55f3c2bed6646cf8439a3c72a45bfb20',
						file_directory: 'https://docs.buckymodel.com/en/latest/',
						doi: ''
					},
					equations: []
				},
				value: {
					value: 'journal of Infection',
					type: 'description',
					dkg_grounding: null
				},
				metadata: [
					{
						type: 'text_span',
						value: 'The journal of Infection'
					}
				]
			}
		]
	}
};
