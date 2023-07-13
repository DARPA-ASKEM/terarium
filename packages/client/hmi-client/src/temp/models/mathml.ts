/* @ts-nocheck
/* eslint-disable */
import { Model } from '@/types/Types';

export const MATHMLMODEL: Model = {
	name: 'mathml model',
	schema:
		'https://github.com/DARPA-ASKEM/Model-Representations/blob/main/petrinet/petrinet_schema.json',
	schema_name: 'PetriNet',
	description: 'This is a model from mathml equations',
	model_version: '0.1',
	model: {
		states: [
			{
				id: 'D',
				name: 'D'
			},
			{
				id: 'E',
				name: 'E'
			},
			{
				id: 'I',
				name: 'I'
			},
			{
				id: 'R',
				name: 'R'
			},
			{
				id: 'S',
				name: 'S'
			}
		],
		transitions: [
			{
				id: 'β',
				input: ['I', 'S'],
				output: ['E', 'I'],
				grounding: null
			},
			{
				id: 'γ',
				input: ['I'],
				output: ['R'],
				grounding: null
			},
			{
				id: 'δ',
				input: ['E'],
				output: ['I'],
				grounding: null
			},
			{
				id: 'ρ',
				input: ['I'],
				output: ['D'],
				grounding: null
			}
		]
	},
	semantics: {
		ode: {
			rates: [
				{
					target: 'β',
					expression: 'β*I*S',
					expression_mathml: null
				},
				{
					target: 'γ',
					expression: 'γ*I',
					expression_mathml: null
				},
				{
					target: 'δ',
					expression: 'δ*E',
					expression_mathml: null
				},
				{
					target: 'ρ',
					expression: 'ρ*I',
					expression_mathml: null
				}
			],
			initials: [
				{
					target: 'D',
					expression: 'D0',
					expression_mathml: ''
				},
				{
					target: 'E',
					expression: 'E0',
					expression_mathml: ''
				},
				{
					target: 'I',
					expression: 'I0',
					expression_mathml: ''
				},
				{
					target: 'R',
					expression: 'R0',
					expression_mathml: ''
				},
				{
					target: 'S',
					expression: 'S0',
					expression_mathml: ''
				}
			],
			parameters: [
				{
					id: 'D0',
					name: 'D0',
					description: 'The total D population at timestep 0'
				},
				{
					id: 'E0',
					name: 'E0',
					description: 'The total E population at timestep 0'
				},
				{
					id: 'I0',
					name: 'I0',
					description: 'The total I population at timestep 0'
				},
				{
					id: 'R0',
					name: 'R0',
					description: 'The total R population at timestep 0'
				},
				{
					id: 'S0',
					name: 'S0',
					description: 'The total S population at timestep 0'
				},
				{
					id: 'β',
					name: 'β',
					description: 'β rate'
				},
				{
					id: 'γ',
					name: 'γ',
					description: 'γ rate'
				},
				{
					id: 'δ',
					name: 'δ',
					description: 'δ rate'
				},
				{
					id: 'ρ',
					name: 'ρ',
					description: 'ρ rate'
				}
			]
		}
	},
	metadata: {
		attributes: [
			{
				type: 'anchored_extraction',
				amr_element_id: 'E',
				payload: {
					id: {
						id: 'E:-1621000196'
					},
					names: [
						{
							id: {
								id: 'T:-2105346093'
							},
							name: 'potential prevention of up',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 524,
								char_end: 550,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.887148'
							}
						},
						{
							id: {
								id: 'v1'
							},
							name: 'E(t)',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v1'
							},
							source: ' Number of people exposed on day t',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-202544802'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 556,
									char_end: 557,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.887148'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Centers for Disease Control and Prevention',
							grounding_id: 'ncit:C16408',
							source: [],
							score: 0.7546381950378418,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.887183'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '5-2'
							},
							name: 'new_confirmed_age_0',
							dataset: {
								id: {
									id: '5'
								},
								name: 'usa-cases-hospitalized-by-age.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-hospitalized-by-age.csv'
							}
						},
						{
							id: {
								id: '5-3'
							},
							name: 'new_confirmed_age_1',
							dataset: {
								id: {
									id: '5'
								},
								name: 'usa-cases-hospitalized-by-age.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-hospitalized-by-age.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:-1621439126'
					},
					names: [
						{
							id: {
								id: 'T:816130011'
							},
							name: 't. beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 149,
								char_end: 156,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888156'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:254088697'
							},
							source: 'day t.',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 145,
								char_end: 151,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888156'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1984431110'
					},
					names: [
						{
							id: {
								id: 'T:920174442'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 303,
								char_end: 308,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888266'
							}
						},
						{
							id: {
								id: 'v15'
							},
							name: 'α',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1889939553'
							},
							source: 'fatality rate',
							grounding: [
								{
									grounding_text: 'case fatality rate',
									grounding_id: 'cemo:case_fatality_rate',
									source: [],
									score: 0.928100049495697,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.888321'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 316,
								char_end: 329,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888266'
							}
						},
						{
							id: {
								id: 'v15'
							},
							source: ' Fatality rate due to the infection',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v15'
							},
							source:
								' Fatality rate is defined as the percentage of deaths among all previously infected individuals',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Van',
							grounding_id: 'geonames:298117',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Sfax',
							grounding_id: 'geonames:2467454',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'δ',
				payload: {
					id: {
						id: 'E:-582843307'
					},
					names: [
						{
							id: {
								id: 'T:1139102624'
							},
							name: 'delta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 271,
								char_end: 276,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888375'
							}
						},
						{
							id: {
								id: 'v14'
							},
							name: 'δ',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:122388655'
							},
							source: 'incubation period',
							grounding: [
								{
									grounding_text: 'incubation period',
									grounding_id: 'apollosv:00000317',
									source: [],
									score: 0.9989535808563232,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.888428'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 284,
								char_end: 301,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888375'
							}
						},
						{
							id: {
								id: 'v14'
							},
							source: ' Incubation period',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v14'
							},
							source: ' The SARS-CoV2 virus has an incubation period of about 5 days',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Gent',
							grounding_id: 'geonames:2797656',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Adana',
							grounding_id: 'geonames:325363',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'γ',
				payload: {
					id: {
						id: 'E:1549850624'
					},
					names: [
						{
							id: {
								id: 'T:-588734649'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 226,
								char_end: 231,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888482'
							}
						},
						{
							id: {
								id: 'v13'
							},
							name: 'γ',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-596460119'
							},
							source: 'proportion of recovery per day',
							grounding: [
								{
									grounding_text: 'average daily number of new infections generated per case (rt)',
									grounding_id: 'cemo:average_daily_number_of_new_infections_generated_per_case_rt',
									source: [],
									score: 0.8275450468063354,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.888535'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 239,
								char_end: 269,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888482'
							}
						},
						{
							id: {
								id: 'v13'
							},
							source: ' Proportion of recovery per day',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v13'
							},
							source: ' rate of recovery',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'gamma distribution',
							grounding_id: 'apollosv:00000255',
							source: [],
							score: 0.8174800872802734,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.888503'
							}
						},
						{
							grounding_text: 'Vigo',
							grounding_id: 'geonames:3105976',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Riga',
							grounding_id: 'geonames:456172',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'S',
				payload: {
					id: {
						id: 'E:-695141294'
					},
					names: [
						{
							id: {
								id: 'T:204344588'
							},
							name: 'N',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 6,
								char_end: 7,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888590'
							}
						},
						{
							id: {
								id: 'v0'
							},
							name: 'S(t)',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-200627336'
							},
							source: 'total population',
							grounding: [
								{
									grounding_text: 'Standard Million Population',
									grounding_id: 'ncit:C71557',
									source: [],
									score: 0.8033140897750854,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.888643'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 31,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888590'
							}
						},
						{
							id: {
								id: 'v0'
							},
							source: ' Number of people susceptible on day t',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'V-LSG/N',
							grounding_id: 'vo:0004083',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.888611'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '0-6'
							},
							name: 'cumulative_confirmed',
							dataset: {
								id: {
									id: '0'
								},
								name: 'usa-cases-deaths.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-deaths.csv'
							}
						},
						{
							id: {
								id: '5-12'
							},
							name: 'cumulative_confirmed_age_0',
							dataset: {
								id: {
									id: '5'
								},
								name: 'usa-cases-hospitalized-by-age.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-hospitalized-by-age.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:1863362492'
					},
					names: [
						{
							id: {
								id: 'T:1753779778'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 919,
								char_end: 926,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888807'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-815101101'
							},
							value: {
								source: 'm(t',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 945,
									char_end: 948,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888807'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:-2131818727'
					},
					names: [
						{
							id: {
								id: 'T:1753779778'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 919,
								char_end: 926,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888993'
							}
						},
						{
							id: {
								id: 'v12'
							},
							name: 'β',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:565214554'
							},
							source: 'beta',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.889046'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 919,
								char_end: 923,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.888993'
							}
						},
						{
							id: {
								id: 'v12'
							},
							source: ' Expected number of people an infected person infects per day',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Lviv',
							grounding_id: 'geonames:702550',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Havana',
							grounding_id: 'geonames:3553478',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '4-28'
							},
							name: 'H2_Testing policy',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:-136675206'
					},
					names: [
						{
							id: {
								id: 'T:1753779778'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 919,
								char_end: 926,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889174'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:565214554'
							},
							source: 'beta',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.889228'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 919,
								char_end: 923,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889174'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-815101101'
							},
							value: {
								source: 'm(t',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 945,
									char_end: 948,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889174'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-375195508'
					},
					names: [
						{
							id: {
								id: 'T:-1521295218'
							},
							name: 'betac',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1224,
								char_end: 1229,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889401'
							}
						},
						{
							id: {
								id: 'v4'
							},
							name: 'SEIS',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v4'
							},
							source:
								' Compartmental model used to investigate the impact of the delay in compulsory mask wearing on the spread of COVID-19 in the community',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-917512268'
							},
							value: {
								source: '0.4',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1232,
									char_end: 1235,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889401'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Chongjin',
							grounding_id: 'geonames:2044757',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Morón',
							grounding_id: 'geonames:3430545',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-227734974'
					},
					names: [
						{
							id: {
								id: 'T:-100782741'
							},
							name: 'betas',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1210,
								char_end: 1215,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889547'
							}
						},
						{
							id: {
								id: 'v19'
							},
							name: 'βs',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v19'
							},
							source: ' infection rate before masking enforcement',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:875393014'
							},
							value: {
								source: '1',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1218,
									char_end: 1219,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889547'
							}
						}
					],
					groundings: [],
					data_columns: [
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-33'
							},
							name: 'H6M_Flag',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:2011356353'
					},
					names: [
						{
							id: {
								id: 'T:854706656'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 958,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889693'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-2070956792'
							},
							source: 'beta',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.889746'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 955,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889693'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'E:1157924531'
					},
					names: [
						{
							id: {
								id: 'T:854706656'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 958,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889932'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-668996858'
							},
							source: 'beta',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.889987'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 955,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.889932'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'R:526915523'
					},
					names: [
						{
							id: {
								id: 'T:854706656'
							},
							name: 'beta(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 958,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890112'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-668996858'
							},
							source: 'beta',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.890164'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 951,
								char_end: 955,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890112'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'I',
				payload: {
					id: {
						id: 'R:-328739983'
					},
					names: [
						{
							id: {
								id: 'T:-778701288'
							},
							name: 'I(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 100,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890288'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1898529592'
							},
							source: 'infection rate',
							grounding: [
								{
									grounding_text: 'Infection Fatality Rate',
									grounding_id: 'ncit:C173780',
									source: [],
									score: 0.9256949424743652,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.890340'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 81,
								char_end: 95,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890288'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.9162705540657043,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.890308'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1248386289'
					},
					names: [
						{
							id: {
								id: 'T:297276415'
							},
							name: 't0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1250,
								char_end: 1252,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890446'
							}
						},
						{
							id: {
								id: 'v22'
							},
							name: 't0',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1110367501'
							},
							source: 'number of days after the first case',
							grounding: [
								{
									grounding_text: 'number of cases hospitalized this week',
									grounding_id: 'cemo:number_of_cases_hospitalized_this_week',
									source: [],
									score: 0.8328390717506409,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.890501'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1260,
								char_end: 1295,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890446'
							}
						},
						{
							id: {
								id: 'v22'
							},
							source: ' number of days after first case where masking wearing is enforced',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'DeltaFTT0918',
							grounding_id: 'vo:0011346',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'VAT00002',
							grounding_id: 'vo:0005085',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-33'
							},
							name: 'H6M_Flag',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:46512816'
					},
					names: [
						{
							id: {
								id: 'T:1372637564'
							},
							name: 'policy of compulsory mask wearing',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1443,
								char_end: 1476,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890627'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:504003322'
							},
							value: {
								source: '89',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1429,
									char_end: 1431,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890627'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-407044703'
					},
					names: [
						{
							id: {
								id: 'T:-1337030228'
							},
							name: 'example of such logistic function',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1349,
								char_end: 1382,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890751'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1178444117'
							},
							value: {
								source: '1',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1338,
									char_end: 1339,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890751'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Semantic_Type',
							grounding_id: 'ncit:P106',
							source: [],
							score: 0.7590150833129883,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.890773'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1111531285'
					},
					names: [
						{
							id: {
								id: 'T:-1193111762'
							},
							name: '47%',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 453,
								char_end: 456,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890923'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1656041964'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 485,
									char_end: 486,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.890923'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-445420568'
					},
					names: [
						{
							id: {
								id: 'T:900109584'
							},
							name: 't1',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 739,
								char_end: 741,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891244'
							}
						},
						{
							id: {
								id: 'v23'
							},
							name: 't1',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v23'
							},
							source: ' number of days after first case where noncompliance begins',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:914267078'
							},
							value: {
								source: '154',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 744,
									char_end: 747,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891244'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'BT1',
							grounding_id: 'vo:0011160',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Wishart1',
							grounding_id: 'probonto:k0000082',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'E:-1296333892'
					},
					names: [
						{
							id: {
								id: 'T:-1184616859'
							},
							name: 'betanc',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 722,
								char_end: 728,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891360'
							}
						},
						{
							id: {
								id: 'v21'
							},
							name: 'βnc',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v21'
							},
							source: ' infection rate due to noncompliance',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v21'
							},
							source: ' Rate of noncompliance',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-589212944'
							},
							value: {
								source: '0.5',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 731,
									char_end: 734,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891360'
							}
						}
					],
					groundings: [],
					data_columns: [
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-33'
							},
							name: 'H6M_Flag',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-2064009007'
					},
					names: [
						{
							id: {
								id: 'T:1820404733'
							},
							name: 'k1',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 430,
								char_end: 432,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891455'
							}
						},
						{
							id: {
								id: 'v18'
							},
							name: 'κ',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1768086066'
							},
							source: 'arbitrary constants',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 410,
								char_end: 429,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891455'
							}
						},
						{
							id: {
								id: 'v18'
							},
							source: ' arbitrary constant',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Cork',
							grounding_id: 'geonames:2965140',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Omsk',
							grounding_id: 'geonames:1496153',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'γ',
				payload: {
					id: {
						id: 'E:-98926741'
					},
					names: [
						{
							id: {
								id: 'T:1904676976'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 0,
								char_end: 5,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891823'
							}
						},
						{
							id: {
								id: 'v25'
							},
							name: 'k2',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v25'
							},
							source: ' arbitrary constant',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-561918051'
							},
							value: {
								source: '1/11',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 8,
									char_end: 12,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891823'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'gamma distribution',
							grounding_id: 'apollosv:00000255',
							source: [],
							score: 0.8174800872802734,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.891841'
							}
						},
						{
							grounding_text: 'Hŭngnam',
							grounding_id: 'geonames:1877030',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'ubiquilin-2 (human)',
							grounding_id: 'pr:Q9UHD9',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1374298884'
					},
					names: [
						{
							id: {
								id: 'T:-1139982725'
							},
							name: 'delta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 197,
								char_end: 202,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891989'
							}
						},
						{
							id: {
								id: 'v17'
							},
							name: '\u0001',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v17'
							},
							source: ' Rate at which a recovered person becomes susceptible again',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:1628287856'
							},
							value: {
								source: '1/5',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 205,
									char_end: 208,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.891989'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1028710751'
					},
					names: [
						{
							id: {
								id: 'T:-1364489025'
							},
							name: 'incubation period',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 237,
								char_end: 254,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892153'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-518683018'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 266,
									char_end: 270,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892153'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'incubation period',
							grounding_id: 'apollosv:00000317',
							source: [],
							score: 0.9989535808563232,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.892173'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'β',
				payload: {
					id: {
						id: 'E:1157335987'
					},
					names: [
						{
							id: {
								id: 'T:1819937082'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 277,
								char_end: 282,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892315'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:929673194'
							},
							value: {
								source: '0.000064',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 285,
									char_end: 293,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892315'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'D',
				payload: {
					id: {
						id: 'E:910795188'
					},
					names: [
						{
							id: {
								id: 'T:1137842641'
							},
							name: 'number of deaths',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 432,
								char_end: 448,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892454'
							}
						},
						{
							id: {
								id: 'v11'
							},
							name: 'D(t)',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v11'
							},
							source: ' Number of people dead on day t',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:200829870'
							},
							value: {
								source: '26',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 466,
									char_end: 468,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892454'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'number of deaths new this week',
							grounding_id: 'cemo:number_of_deaths_new_this_week',
							source: [],
							score: 0.849774956703186,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.892474'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '0-7'
							},
							name: 'cumulative_deceased',
							dataset: {
								id: {
									id: '0'
								},
								name: 'usa-cases-deaths.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-deaths.csv'
							}
						},
						{
							id: {
								id: '5-22'
							},
							name: 'new_deceased_age_0',
							dataset: {
								id: {
									id: '5'
								},
								name: 'usa-cases-hospitalized-by-age.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-cases-hospitalized-by-age.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1286804828'
					},
					names: [
						{
							id: {
								id: 'T:-1406321319'
							},
							name: 'total number of Recovered and Dead compartments',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 478,
								char_end: 525,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892595'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-997103511'
							},
							value: {
								source: '40,625',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 530,
									char_end: 536,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892595'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'total number of cases removed from isolation',
							grounding_id: 'cemo:total_number_of_cases_removed_from_isolation',
							source: [],
							score: 0.8561632037162781,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.892615'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-325311557'
					},
					names: [
						{
							id: {
								id: 'T:-381982534'
							},
							name: 'rho',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 538,
								char_end: 541,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892735'
							}
						},
						{
							id: {
								id: 'v3'
							},
							name: 'SEIR',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v3'
							},
							source: ' Compartmental model for modelling the spread of COVID-19',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v3'
							},
							source: ' Compartmentalized model',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-1057154576'
							},
							value: {
								source: '1/9',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 544,
									char_end: 547,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892735'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Rho',
							grounding_id: 'vo:0011064',
							source: [],
							score: 0.9999998807907104,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.892754'
							}
						},
						{
							grounding_text: 'Salem',
							grounding_id: 'geonames:5750162',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Visalia',
							grounding_id: 'geonames:5406567',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-262180774'
					},
					names: [
						{
							id: {
								id: 'T:1705025731'
							},
							name: 'case',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1483,
								char_end: 1487,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892978'
							}
						},
						{
							id: {
								id: 'v5'
							},
							name: 'SEIRS',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v5'
							},
							source:
								' Variant of SEIR to include effects of age-structure and different social settings',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v5'
							},
							source: ' Compartmentalized model with time-limited immunity',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:1759035755'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1501,
									char_end: 1505,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.892978'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'case series',
							grounding_id: 'apollosv:00000558',
							source: [],
							score: 0.8418106436729431,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.892996'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:307660388'
					},
					names: [
						{
							id: {
								id: 'T:1342805111'
							},
							name: 'case of a (b) 50 days delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1361,
								char_end: 1388,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893087'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:54218065'
							},
							value: {
								source: '10.479',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1434,
									char_end: 1440,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893087'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'time delay to case detection',
							grounding_id: 'apollosv:00000267',
							source: [],
							score: 0.7793136835098267,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893109'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-306701002'
					},
					names: [
						{
							id: {
								id: 'T:1342805111'
							},
							name: 'case of a (b) 50 days delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1361,
								char_end: 1388,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893179'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:490022350'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1440,
									char_end: 1441,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893179'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'time delay to case detection',
							grounding_id: 'apollosv:00000267',
							source: [],
							score: 0.7793136835098267,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893200'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1711474770'
					},
					names: [
						{
							id: {
								id: 'T:1342805111'
							},
							name: 'case of a (b) 50 days delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1361,
								char_end: 1388,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893271'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:54218065'
							},
							value: {
								source: '10.479',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1434,
									char_end: 1440,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893271'
							}
						},
						{
							id: {
								id: 'T:490022350'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1440,
									char_end: 1441,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893271'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'time delay to case detection',
							grounding_id: 'apollosv:00000267',
							source: [],
							score: 0.7793136835098267,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893292'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1134010888'
					},
					names: [
						{
							id: {
								id: 'T:2057170385'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 499,
								char_end: 500,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893421'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:2144634561'
							},
							source: 'be enforced',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 486,
								char_end: 497,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893421'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1856653836'
					},
					names: [
						{
							id: {
								id: 'T:977224411'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1710,
								char_end: 1732,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893559'
							}
						},
						{
							id: {
								id: 'v6'
							},
							name: 'Maximum Infected Values',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v6'
							},
							source:
								' Maximum number of people infected with COVID-19 in the community, as a result of the delay in compulsory mask wearing',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-1826550746'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1756,
									char_end: 1757,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893559'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893581'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-223017254'
					},
					names: [
						{
							id: {
								id: 'T:977224411'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1710,
								char_end: 1732,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893653'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2013554441'
							},
							value: {
								source: '31.422',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1750,
									char_end: 1756,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893653'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893674'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1463565746'
					},
					names: [
						{
							id: {
								id: 'T:977224411'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1710,
								char_end: 1732,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893746'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2013554441'
							},
							value: {
								source: '31.422',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1750,
									char_end: 1756,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893746'
							}
						},
						{
							id: {
								id: 'T:-1826550746'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1756,
									char_end: 1757,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893746'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893769'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-823594056'
					},
					names: [
						{
							id: {
								id: 'T:-1655177158'
							},
							name: 'case of (a) 0 days of delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1185,
								char_end: 1212,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893863'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1040754711'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1197,
									char_end: 1198,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893863'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'probability of entry into a country is denied',
							grounding_id: 'apollosv:00000278',
							source: [],
							score: 0.7874587178230286,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893885'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-232718024'
					},
					names: [
						{
							id: {
								id: 'T:-384187929'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1238,
								char_end: 1260,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893958'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1882725728'
							},
							value: {
								source: '10.453',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1278,
									char_end: 1284,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.893958'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.893980'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-429168824'
					},
					names: [
						{
							id: {
								id: 'T:-384187929'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1238,
								char_end: 1260,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894050'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-524556619'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1284,
									char_end: 1285,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894050'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894072'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1232232041'
					},
					names: [
						{
							id: {
								id: 'T:-384187929'
							},
							name: 'maximum infected value',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1238,
								char_end: 1260,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894142'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1882725728'
							},
							value: {
								source: '10.453',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1278,
									char_end: 1284,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894142'
							}
						},
						{
							id: {
								id: 'T:-524556619'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1284,
									char_end: 1285,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894142'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'maximum value',
							grounding_id: 'apollosv:00000433',
							source: [],
							score: 0.9049829840660095,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894164'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1306756795'
					},
					names: [
						{
							id: {
								id: 'T:-1292766255'
							},
							name: 'case',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1185,
								char_end: 1189,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894257'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-7729983'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1199,
									char_end: 1203,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894257'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'case series',
							grounding_id: 'apollosv:00000558',
							source: [],
							score: 0.8418106436729431,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894279'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1697521080'
					},
					names: [
						{
							id: {
								id: 'T:1901943977'
							},
							name: 'delay of 80',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 159,
								char_end: 170,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894593'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-574956053'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 178,
									char_end: 182,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894593'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1619111231'
					},
					names: [
						{
							id: {
								id: 'T:510890493'
							},
							name: 'high maximum infection',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 79,
								char_end: 101,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894701'
							}
						},
						{
							id: {
								id: 'v31'
							},
							name: 'Delay in Public Mask Enforcement',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v31'
							},
							source: ' Delay in implementation of compulsory mask wearing',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-2107934969'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 113,
									char_end: 114,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894701'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'initial infection case',
							grounding_id: 'cemo:initial_infection_case',
							source: [],
							score: 0.7537267208099365,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894722'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-33'
							},
							name: 'H6M_Flag',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:5976644'
					},
					names: [
						{
							id: {
								id: 'T:809457271'
							},
							name: 'low maximum infection',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 36,
								char_end: 57,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894842'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2005233352'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 72,
									char_end: 73,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894842'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'initial infection case',
							grounding_id: 'cemo:initial_infection_case',
							source: [],
							score: 0.7639009356498718,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894864'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-208609464'
					},
					names: [
						{
							id: {
								id: 'T:-268534747'
							},
							name: 'takes place at about the same 80 to 100 days of delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1087,
								char_end: 1140,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894934'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:909237295'
							},
							value: {
								source: '80',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1117,
									char_end: 1119,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.894934'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Sponsor-Investigator',
							grounding_id: 'ncit:C142695',
							source: [],
							score: 0.8060052990913391,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.894956'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1457212500'
					},
					names: [
						{
							id: {
								id: 'T:1633812708'
							},
							name: 't1',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2649,
								char_end: 2651,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895053'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1303145619'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2658,
									char_end: 2662,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895053'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-2113012858'
					},
					names: [
						{
							id: {
								id: 'T:1633812708'
							},
							name: 't1',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2649,
								char_end: 2651,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895323'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-454782056'
							},
							value: {
								source: '154',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2654,
									char_end: 2657,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895323'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1048219199'
					},
					names: [
						{
							id: {
								id: 'T:1633812708'
							},
							name: 't1',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2649,
								char_end: 2651,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895424'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-454782056'
							},
							value: {
								source: '154',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2654,
									char_end: 2657,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895424'
							}
						},
						{
							id: {
								id: 'T:-1303145619'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2658,
									char_end: 2662,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895424'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1889118797'
					},
					names: [
						{
							id: {
								id: 'T:-1776882638'
							},
							name: 'occurring at about 100 days of delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 269,
								char_end: 305,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895562'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-2012470579'
							},
							value: {
								source: '100',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 288,
									char_end: 291,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895562'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'rate of cases with at least one comorbidity',
							grounding_id: 'cemo:rate_of_cases_with_at_least_one_comorbidity',
							source: [],
							score: 0.7907813787460327,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.895583'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1976612459'
					},
					names: [
						{
							id: {
								id: 'T:1390128890'
							},
							name: '100 days of delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1168,
								char_end: 1185,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895682'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-686780907'
							},
							value: {
								source: '100',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1168,
									char_end: 1171,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895682'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-328410532'
					},
					names: [
						{
							id: {
								id: 'T:811296232'
							},
							name: 'potential window to take action',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1869,
								char_end: 1900,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895801'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-2081340955'
							},
							value: {
								source: '3',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1910,
									char_end: 1911,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895801'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'simulation time step action specification',
							grounding_id: 'apollosv:00000068',
							source: [],
							score: 0.7523066997528076,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.895822'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1560241998'
					},
					names: [
						{
							id: {
								id: 'T:811296232'
							},
							name: 'potential window to take action',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1869,
								char_end: 1900,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895896'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:148982384'
							},
							value: {
								source: 'months',
								grounding: [
									{
										grounding_text: 'persons aged 6 months--24 years',
										grounding_id: 'vo:0001100',
										source: [],
										score: 0.7775040864944458,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-07-04 01:15:56.895951'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1912,
									char_end: 1918,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.895896'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'simulation time step action specification',
							grounding_id: 'apollosv:00000068',
							source: [],
							score: 0.7523066997528076,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.895918'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:508161263'
					},
					names: [
						{
							id: {
								id: 'T:811296232'
							},
							name: 'potential window to take action',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1869,
								char_end: 1900,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896013'
							}
						},
						{
							id: {
								id: 'v8'
							},
							name: 'Noncompliance',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v8'
							},
							source:
								' Lack of medical knowledge, wishful thinking, selﬁsh behaviour, pandemic fatigue',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							id: {
								id: 'v8'
							},
							source: ' Level of people not following the policy.None',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-2081340955'
							},
							value: {
								source: '3',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1910,
									char_end: 1911,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896013'
							}
						},
						{
							id: {
								id: 'T:148982384'
							},
							value: {
								source: 'months',
								grounding: [
									{
										grounding_text: 'persons aged 6 months--24 years',
										grounding_id: 'vo:0001100',
										source: [],
										score: 0.7775040864944458,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-07-04 01:15:56.896089'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1912,
									char_end: 1918,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896013'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'simulation time step action specification',
							grounding_id: 'apollosv:00000068',
							source: [],
							score: 0.7523066997528076,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.896034'
							}
						},
						{
							grounding_text: 'diet noncompliance AE',
							grounding_id: 'oae:0006383',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:2114716604'
					},
					names: [
						{
							id: {
								id: 'T:403589220'
							},
							name: '90 days of time-limited immunity',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1399,
								char_end: 1431,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896153'
							}
						},
						{
							id: {
								id: 'v9'
							},
							name: 'Time-limited immunity',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v9'
							},
							source: ' Recovered individuals become susceptible again after a period of time',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-23771472'
							},
							value: {
								source: '90',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1399,
									char_end: 1401,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896153'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:591214166'
					},
					names: [
						{
							id: {
								id: 'T:828578762'
							},
							name: 'delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2314,
								char_end: 2319,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896931'
							}
						},
						{
							id: {
								id: 'v26'
							},
							name: 'Delay',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v26'
							},
							source: ' Amount of time between public masking enforcement and the first case.',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:1820127059'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2326,
									char_end: 2330,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.896931'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'ascertainment delay',
							grounding_id: 'apollosv:00000134',
							source: [],
							score: 0.8018620610237122,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.896949'
							}
						},
						{
							grounding_text: 'adermatoglyphia',
							grounding_id: 'doid:0111357',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'SADDAN',
							grounding_id: 'doid:0111158',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-457015428'
					},
					names: [
						{
							id: {
								id: 'T:325916915'
							},
							name: 'delay',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 407,
								char_end: 412,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897309'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-252038506'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 420,
									char_end: 424,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897309'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'ascertainment delay',
							grounding_id: 'apollosv:00000134',
							source: [],
							score: 0.8018620610237122,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.897327'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1100301483'
					},
					names: [
						{
							id: {
								id: 'T:-743119050'
							},
							name: 'Laydon',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1468,
								char_end: 1474,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897563'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1134338057'
							},
							source: 'D.J.; Dabrera',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1476,
								char_end: 1489,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897563'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1903420941'
					},
					names: [
						{
							id: {
								id: 'T:2120170772'
							},
							name: 'Chand',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1398,
								char_end: 1403,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897694'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:2044144852'
							},
							source: 'm.; Barrett',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1405,
								char_end: 1416,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897694'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Zia',
							grounding_id: 'ncit:C44848',
							source: [],
							score: 0.792691707611084,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.897715'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1117338965'
					},
					names: [
						{
							id: {
								id: 'T:2083120324'
							},
							name: 'T.K',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1656,
								char_end: 1659,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897828'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1178846842'
							},
							source: 'Tatapudi',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1628,
								char_end: 1636,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897828'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-302419759'
					},
					names: [
						{
							id: {
								id: 'T:-1741898511'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897975'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:389472793'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.897975'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1784148891'
					},
					names: [
						{
							id: {
								id: 'T:-1741898511'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898181'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:394151550'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.898233'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898181'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:459537029'
					},
					names: [
						{
							id: {
								id: 'T:-1741898511'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898382'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:394151550'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.898438'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898382'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:73791469'
					},
					names: [
						{
							id: {
								id: 'T:1617721042'
							},
							name: 'Rigler',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 20,
								char_end: 26,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898606'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1147340771'
							},
							source: 'J.; Robinson',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 28,
								char_end: 40,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898606'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:885080843'
					},
					names: [
						{
							id: {
								id: 'T:369500773'
							},
							name: 'Livar',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 59,
								char_end: 64,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898846'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:394151550'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.898879'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.898846'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:193482118'
					},
					names: [
						{
							id: {
								id: 'T:-374982932'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899032'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:394151550'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.899085'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899032'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.899052'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:262749454'
					},
					names: [
						{
							id: {
								id: 'T:-374982932'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899239'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:389472793'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899239'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.899259'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-236825794'
					},
					names: [
						{
							id: {
								id: 'T:-374982932'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899439'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:389472793'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899439'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.899459'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1846198673'
					},
					names: [
						{
							id: {
								id: 'T:-959173159'
							},
							name: 'Korber',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1085,
								char_end: 1091,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899672'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:394238695'
							},
							source: 'B.; Fischer',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1093,
								char_end: 1104,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899672'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1747703505'
					},
					names: [
						{
							id: {
								id: 'T:689408371'
							},
							name: 'Giorgi',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1183,
								char_end: 1189,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899807'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-324565088'
							},
							source: 'E.E.; Bhattacharya',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1191,
								char_end: 1209,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899807'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'R0',
				payload: {
					id: {
						id: 'R:1390890090'
					},
					names: [
						{
							id: {
								id: 'T:-1614273080'
							},
							name: 'Yoon',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1128,
								char_end: 1132,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899958'
							}
						},
						{
							id: {
								id: 'v30'
							},
							name: 'R0',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:365091242'
							},
							source: 'h.; Theiler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1134,
								char_end: 1145,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.899958'
							}
						},
						{
							id: {
								id: 'v30'
							},
							source: ' Reproduction number of B117 variant',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Almansour-001',
							grounding_id: 'vo:0005393',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'Betacoronavirus BtCoV/Rhi_hip/R07-09/SPA/2010',
							grounding_id: 'ncbitaxon:1346307',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-114592353'
					},
					names: [
						{
							id: {
								id: 'T:-1186590224'
							},
							name: 'Lyu',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2015,
								char_end: 2018,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900091'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1116483647'
							},
							source: 'W.; Wehby',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2020,
								char_end: 2029,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900091'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:697989061'
					},
					names: [
						{
							id: {
								id: 'T:1151178142'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 652,
								char_end: 654,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900201'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:127756748'
							},
							source: 'Reich',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 630,
								char_end: 635,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900201'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1658505651'
					},
					names: [
						{
							id: {
								id: 'T:1151178142'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 652,
								char_end: 654,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900378'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-743732172'
							},
							source: 'N.G.; Lessler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 637,
								char_end: 650,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900378'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1679128071'
					},
					names: [
						{
							id: {
								id: 'T:1151178142'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 652,
								char_end: 654,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900555'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-743732172'
							},
							source: 'N.G.; Lessler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 637,
								char_end: 650,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900555'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1255848250'
					},
					names: [
						{
							id: {
								id: 'T:-189971178'
							},
							name: 'Zheng',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 590,
								char_end: 595,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900733'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:2101355726'
							},
							source: 'Q.; Meredith',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 597,
								char_end: 609,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900733'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Yao Chinese',
							grounding_id: 'ncit:C158170',
							source: [],
							score: 0.771730363368988,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.900752'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1043198099'
					},
					names: [
						{
							id: {
								id: 'T:1003337774'
							},
							name: 'Bi',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 569,
								char_end: 571,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900970'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1195298001'
							},
							source: 'Q.; Jones',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 573,
								char_end: 582,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.900970'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1349342732'
					},
					names: [
						{
							id: {
								id: 'T:-988027874'
							},
							name: 'delay threshold',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 738,
								char_end: 753,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.901580'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:296548306'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 767,
									char_end: 771,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.901580'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'epidemic threshold',
							grounding_id: 'apollosv:00000531',
							source: [],
							score: 0.7768451571464539,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.901611'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1997357042'
					},
					names: [
						{
							id: {
								id: 'T:974670954'
							},
							name: '9027 2 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902125'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1831305098'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902125'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-372736162'
					},
					names: [
						{
							id: {
								id: 'T:-1753111254'
							},
							name: '9027 3 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902310'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-40214986'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902310'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-62253497'
					},
					names: [
						{
							id: {
								id: 'T:1881573139'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 18,
								char_end: 19,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902493'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1161401641'
							},
							source: 'alpharhoI',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 8,
								char_end: 17,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902493'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-166887039'
					},
					names: [
						{
							id: {
								id: 'T:1977239128'
							},
							name: 'dD(T)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 0,
								char_end: 5,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902638'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1161401641'
							},
							source: 'alpharhoI',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 8,
								char_end: 17,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902638'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-954851593'
					},
					names: [
						{
							id: {
								id: 'T:1085496056'
							},
							name: 'dR(T)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 0,
								char_end: 5,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902744'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1305401405'
							},
							value: {
								source: '- Alpha',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 11,
									char_end: 18,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902744'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-330794225'
					},
					names: [
						{
							id: {
								id: 'T:832012800'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 53,
								char_end: 54,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902905'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:116263679'
							},
							source: 'alpharhoI',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 43,
								char_end: 52,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902905'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-258647165'
					},
					names: [
						{
							id: {
								id: 'T:-1036090467'
							},
							name: 'dR(T)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 0,
								char_end: 5,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902991'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1829991090'
							},
							value: {
								source: '- Alpha',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 11,
									char_end: 18,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.902991'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:966372473'
					},
					names: [
						{
							id: {
								id: 'T:-757585405'
							},
							name: 'm(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 87,
								char_end: 91,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903101'
							}
						},
						{
							id: {
								id: 'v7'
							},
							name: 'Compliance',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-737244778'
							},
							source: 'compulsory mask wearing m',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 63,
								char_end: 88,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903101'
							}
						},
						{
							id: {
								id: 'v7'
							},
							source: ' Complete compliance and gradual noncompliance of masking enforcement',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'antiviral compliance',
							grounding_id: 'apollosv:00000050',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						},
						{
							grounding_text: 'vaccination compliance',
							grounding_id: 'apollosv:00000024',
							source: [],
							score: 1.0,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					data_columns: [
						{
							id: {
								id: '4-32'
							},
							name: 'H6M_Facial Coverings',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						},
						{
							id: {
								id: '4-33'
							},
							name: 'H6M_Flag',
							dataset: {
								id: {
									id: '4'
								},
								name: 'OxCGRT_nat_latest.csv',
								metadata:
									'https://raw.githubusercontent.com/OxCGRT/covid-policy-tracker/master/data/OxCGRT_nat_latest.csv'
							}
						}
					]
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:509396441'
					},
					names: [
						{
							id: {
								id: 'T:-1745274392'
							},
							name: '9027 5 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903220'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-791605677'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903220'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1741398158'
					},
					names: [
						{
							id: {
								id: 'T:-1864370720'
							},
							name: 'm(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 87,
								char_end: 91,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903322'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1709180673'
							},
							source: 'compulsory mask wearing m',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 63,
								char_end: 88,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903322'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1265006642'
					},
					names: [
						{
							id: {
								id: 'T:664078474'
							},
							name: '9027 6 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903527'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-822735574'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903527'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-1755777161'
					},
					names: [
						{
							id: {
								id: 'T:1019304466'
							},
							name: 'c',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 61,
								char_end: 62,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903631'
							}
						},
						{
							id: {
								id: 'v27'
							},
							name: 'Scenario 1',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-211262112'
							},
							source: 'SEIR Plots for delays',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 10,
								char_end: 31,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903631'
							}
						},
						{
							id: {
								id: 'v27'
							},
							source: ' SEIR with no noncompliance',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'BoNT/C',
							grounding_id: 'vo:0004004',
							source: [],
							score: 0.7589514851570129,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.903651'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:674716792'
					},
					names: [
						{
							id: {
								id: 'T:-196066382'
							},
							name: 'b',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 48,
								char_end: 49,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903738'
							}
						},
						{
							id: {
								id: 'v28'
							},
							name: 'Scenario 2',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-990885316'
							},
							source: 'Plots for delays of (a) 0 days',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 45,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903738'
							}
						},
						{
							id: {
								id: 'v28'
							},
							source: ' SEIR with gradual noncompliance',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'AIDSVAX B/B',
							grounding_id: 'vo:0000405',
							source: [],
							score: 0.8989673256874084,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.903759'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-509339032'
					},
					names: [
						{
							id: {
								id: 'T:565392455'
							},
							name: 'Plots for delays',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 31,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903847'
							}
						},
						{
							id: {
								id: 'v29'
							},
							name: 'Scenario 3',
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'v29'
							},
							source: ' SEIRS with time-limited immunity',
							grounding: null,
							extraction_source: null,
							provenance: {
								method: 'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
								timestamp: '2023-07-10 19:37:36.065099'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-233939232'
							},
							value: {
								source: 'days',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 41,
									char_end: 45,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.903847'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-2143043347'
					},
					names: [
						{
							id: {
								id: 'T:141611462'
							},
							name: 'results of different enforcement delay values and their corresponding maximum infected values',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 243,
								char_end: 336,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904018'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1789777770'
							},
							value: {
								source: '4',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 231,
									char_end: 232,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904018'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'average daily number of new infections generated per case (rt)',
							grounding_id: 'cemo:average_daily_number_of_new_infections_generated_per_case_rt',
							source: [],
							score: 0.816648542881012,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.904037'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1149068522'
					},
					names: [
						{
							id: {
								id: 'T:-1333322047'
							},
							name: '9027 7 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904225'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1841565533'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904225'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1827577805'
					},
					names: [
						{
							id: {
								id: 'T:-666880397'
							},
							name: '9027 8 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 58,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904397'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1881433500'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904397'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1783882418'
					},
					names: [
						{
							id: {
								id: 'T:539075481'
							},
							name: '9027 10 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 59,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904601'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-61529253'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904601'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-719696986'
					},
					names: [
						{
							id: {
								id: 'T:1485825475'
							},
							name: '9027 11 of 11',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 46,
								char_end: 59,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904701'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1390059971'
							},
							value: {
								source: '18',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 42,
									char_end: 44,
									document_reference: {
										id: 'ijerph-18-09027.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904701'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-102847579'
					},
					names: [
						{
							id: {
								id: 'T:-588045234'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904899'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1829482409'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.904899'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-728230378'
					},
					names: [
						{
							id: {
								id: 'T:-588045234'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905075'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:592812818'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.905127'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905075'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-1879785839'
					},
					names: [
						{
							id: {
								id: 'T:-588045234'
							},
							name: 'C.M',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 116,
								char_end: 119,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905254'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:592812818'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.905306'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905254'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:2018394268'
					},
					names: [
						{
							id: {
								id: 'T:-1901259997'
							},
							name: 'Livar',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 59,
								char_end: 64,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905432'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:592812818'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.905466'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905432'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1563442282'
					},
					names: [
						{
							id: {
								id: 'T:612539566'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905594'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:592812818'
							},
							source: 'Gallaway',
							grounding: [
								{
									grounding_text: 'Abdala',
									grounding_id: 'vo:0005082',
									source: [],
									score: 0.764650285243988,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-07-04 01:15:56.905647'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 12,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905594'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.905614'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1696945602'
					},
					names: [
						{
							id: {
								id: 'T:612539566'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905809'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1829482409'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905809'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.905830'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:469395013'
					},
					names: [
						{
							id: {
								id: 'T:612539566'
							},
							name: 'Cunico',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905993'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1829482409'
							},
							source: 'J.; Christ',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 114,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.905993'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Saxman',
							grounding_id: 'ncit:C44726',
							source: [],
							score: 0.8499902486801147,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.906013'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-2147315940'
					},
					names: [
						{
							id: {
								id: 'T:-1541904158'
							},
							name: 'Rigler',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 20,
								char_end: 26,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906175'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:633224182'
							},
							source: 'J.; Robinson',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 28,
								char_end: 40,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906175'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:1394798046'
					},
					names: [
						{
							id: {
								id: 'T:1973270799'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 114,
								char_end: 116,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906353'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-226455607'
							},
							source: 'Reich',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 92,
								char_end: 97,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906353'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-420570018'
					},
					names: [
						{
							id: {
								id: 'T:1973270799'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 114,
								char_end: 116,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906529'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-635682871'
							},
							source: 'N.G.; Lessler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 99,
								char_end: 112,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906529'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'E:-97894767'
					},
					names: [
						{
							id: {
								id: 'T:1973270799'
							},
							name: 'J.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 114,
								char_end: 116,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906709'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-635682871'
							},
							source: 'N.G.; Lessler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 99,
								char_end: 112,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906709'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:731501372'
					},
					names: [
						{
							id: {
								id: 'T:189626744'
							},
							name: 'Zheng',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 52,
								char_end: 57,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906948'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-930326429'
							},
							source: 'Q.; Meredith',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 59,
								char_end: 71,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.906948'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Yao Chinese',
							grounding_id: 'ncit:C158170',
							source: [],
							score: 0.771730363368988,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.906968'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1299812587'
					},
					names: [
						{
							id: {
								id: 'T:172707489'
							},
							name: 'Bi',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 31,
								char_end: 33,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907150'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1157958033'
							},
							source: 'Q.; Jones',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 35,
								char_end: 44,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907150'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-521953121'
					},
					names: [
						{
							id: {
								id: 'T:-1765836795'
							},
							name: 'Korber',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 10,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907394'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-848707910'
							},
							source: 'B.; Fischer',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 12,
								char_end: 23,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907394'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-992248734'
					},
					names: [
						{
							id: {
								id: 'T:1487029779'
							},
							name: 'Giorgi',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 102,
								char_end: 108,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907512'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:44727886'
							},
							source: 'E.E.; Bhattacharya',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 110,
								char_end: 128,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907512'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-2051698457'
					},
					names: [
						{
							id: {
								id: 'T:1089702946'
							},
							name: 'Yoon',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 47,
								char_end: 51,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907646'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1315025306'
							},
							source: 'h.; Theiler',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 53,
								char_end: 64,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907646'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:-735404315'
					},
					names: [
						{
							id: {
								id: 'T:-1888291588'
							},
							name: 'Laydon',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 96,
								char_end: 102,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907758'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1887895213'
							},
							source: 'D.J.; Dabrera',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 117,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907758'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1497961882'
					},
					names: [
						{
							id: {
								id: 'T:-357419283'
							},
							name: 'Chand',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 26,
								char_end: 31,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907889'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1372797310'
							},
							source: 'm.; Barrett',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 33,
								char_end: 44,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.907889'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Zia',
							grounding_id: 'ncit:C44848',
							source: [],
							score: 0.792691707611084,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-07-04 01:15:56.907909'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:1093468671'
					},
					names: [
						{
							id: {
								id: 'T:-6606364'
							},
							name: 'Lyu',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 7,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.908021'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1537885333'
							},
							source: 'W.; Wehby',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 9,
								char_end: 18,
								document_reference: {
									id: 'ijerph-18-09027.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-07-04 01:15:56.908021'
							}
						}
					],
					value_specs: [],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'document_collection',
				amr_element_id: null,
				payload: {
					documents: [
						{
							id: {
								id: 'ijerph-18-09027.pdf'
							},
							source_file: 'ijerph-18-09027.pdf',
							doi: ''
						},
						{
							id: {
								id: '1'
							},
							source_file: 'be122a7b0834dea144f41dbd06e7045a__text_ijerph-18-09027',
							doi: ''
						}
					]
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8191676147039559327'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: null,
					time: {
						datetime: '14 April 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887296'
						},
						grounding: null
					}
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887331'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3171073294860552492'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'Taiwan',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887358'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '2814658218382375354'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'New York',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887381'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8704112631809980577'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887404'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '2030053119592084747'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'Washington',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887426'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1262721062878511358'
					},
					extractions: [
						{
							id: 'E:-1621000196'
						}
					],
					location: {
						location: 'USA',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.887448'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:1863362492'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.888921'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:1863362492'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.888948'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1863362492'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.888972'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:-2131818727'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889104'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-2131818727'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889129'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-2131818727'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889152'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:-136675206'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889331'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-136675206'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889356'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-136675206'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889379'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'E:-375195508'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889476'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:-375195508'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889502'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-375195508'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889525'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'E:-227734974'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889623'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:-227734974'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889648'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-227734974'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889671'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:2011356353'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889860'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:2011356353'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889886'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:2011356353'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.889910'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'E:1157924531'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890044'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:1157924531'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890069'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1157924531'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890091'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:526915523'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890221'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:526915523'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890245'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:526915523'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890268'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:-328739983'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890399'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-328739983'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890424'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'E:1248386289'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890557'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:1248386289'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890582'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1248386289'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890606'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:46512816'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890723'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-407044703'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.890900'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3501912739117260156'
					},
					extractions: [
						{
							id: 'R:-1111531285'
						}
					],
					location: {
						location: 'Germany',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891015'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-1111531285'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891040'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-445420568'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891337'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-1296333892'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891432'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-2064009007'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891664'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-98926741'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891940'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:-98926741'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.891966'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1374298884'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892106'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:1374298884'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892131'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1028710751'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892268'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:1028710751'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892293'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1157335987'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892408'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:1157335987'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892433'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:910795188'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892549'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:910795188'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892573'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'E:-1286804828'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892689'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-1286804828'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892714'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:-325311557'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.892848'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1134010888'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.893535'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-208609464'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.895029'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1889118797'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.895658'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1976612459'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.895777'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:2114716604'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.896248'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:591214166'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.897044'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6997079734403945466'
					},
					extractions: [
						{
							id: 'R:1100301483'
						}
					],
					location: {
						location: 'England',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.897670'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6997079734403945466'
					},
					extractions: [
						{
							id: 'R:-1903420941'
						}
					],
					location: {
						location: 'England',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.897805'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6997079734403945466'
					},
					extractions: [
						{
							id: 'E:1117338965'
						}
					],
					location: {
						location: 'England',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.897917'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-302419759'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898088'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-302419759'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898114'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'R:-302419759'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898137'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-302419759'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898160'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:1784148891'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898291'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:1784148891'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898315'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'E:1784148891'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898338'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:1784148891'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898361'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:459537029'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898509'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:459537029'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898536'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'R:459537029'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898560'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:459537029'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898584'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:73791469'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898713'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:73791469'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898738'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'R:73791469'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898786'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:73791469'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898825'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:885080843'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898937'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:885080843'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898963'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'E:885080843'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.898987'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:885080843'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899010'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:193482118'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899147'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:193482118'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899172'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'E:193482118'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899196'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:193482118'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899218'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:262749454'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899348'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:262749454'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899372'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'R:262749454'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899395'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:262749454'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899418'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:-236825794'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899581'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:-236825794'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899606'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'E:-236825794'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899630'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'E:-236825794'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899652'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1846198673'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899761'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8191676147039559327'
					},
					extractions: [
						{
							id: 'R:1846198673'
						}
					],
					location: null,
					time: {
						datetime: '29 January 2021',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899785'
						},
						grounding: null
					}
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1747703505'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899912'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8191676147039559327'
					},
					extractions: [
						{
							id: 'R:-1747703505'
						}
					],
					location: null,
					time: {
						datetime: '29 January 2021',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.899937'
						},
						grounding: null
					}
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1390890090'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900044'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8191676147039559327'
					},
					extractions: [
						{
							id: 'R:1390890090'
						}
					],
					location: null,
					time: {
						datetime: '29 January 2021',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900069'
						},
						grounding: null
					}
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5245253148795271646'
					},
					extractions: [
						{
							id: 'R:-114592353'
						}
					],
					location: {
						location: 'US',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900179'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '344867900633503813'
					},
					extractions: [
						{
							id: 'E:697989061'
						}
					],
					location: {
						location: 'Academy of Medicine',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900288'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-1833220793972450760'
					},
					extractions: [
						{
							id: 'E:697989061'
						}
					],
					location: {
						location: 'National Centre for Infectious Diseases',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900312'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:697989061'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900335'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:697989061'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900358'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '344867900633503813'
					},
					extractions: [
						{
							id: 'R:-1658505651'
						}
					],
					location: {
						location: 'Academy of Medicine',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900466'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-1833220793972450760'
					},
					extractions: [
						{
							id: 'R:-1658505651'
						}
					],
					location: {
						location: 'National Centre for Infectious Diseases',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900490'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1658505651'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900513'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:-1658505651'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900535'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '344867900633503813'
					},
					extractions: [
						{
							id: 'E:1679128071'
						}
					],
					location: {
						location: 'Academy of Medicine',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900642'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-1833220793972450760'
					},
					extractions: [
						{
							id: 'E:1679128071'
						}
					],
					location: {
						location: 'National Centre for Infectious Diseases',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900667'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1679128071'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900690'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:1679128071'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900712'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '344867900633503813'
					},
					extractions: [
						{
							id: 'R:1255848250'
						}
					],
					location: {
						location: 'Academy of Medicine',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900862'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-1833220793972450760'
					},
					extractions: [
						{
							id: 'R:1255848250'
						}
					],
					location: {
						location: 'National Centre for Infectious Diseases',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900888'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1255848250'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900919'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:1255848250'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.900942'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '344867900633503813'
					},
					extractions: [
						{
							id: 'R:1043198099'
						}
					],
					location: {
						location: 'Academy of Medicine',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901126'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-1833220793972450760'
					},
					extractions: [
						{
							id: 'R:1043198099'
						}
					],
					location: {
						location: 'National Centre for Infectious Diseases',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901165'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1043198099'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901203'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:1043198099'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901238'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'E:1349342732'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901778'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '307865811701786012'
					},
					extractions: [
						{
							id: 'E:1349342732'
						}
					],
					location: {
						location: 'D.Y',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.901821'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:1741398158'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.903429'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '6910792101217074898'
					},
					extractions: [
						{
							id: 'R:1741398158'
						}
					],
					location: {
						location: 'National Centre of Disease Control Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.903456'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '1016400811037600199'
					},
					extractions: [
						{
							id: 'R:-1149068522'
						}
					],
					location: {
						location: 'Singapore',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.904324'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-719696986'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.904792'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-719696986'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.904819'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '8108698055925566728'
					},
					extractions: [
						{
							id: 'R:-719696986'
						}
					],
					location: {
						location: 'January 22-August 7',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.904843'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-6115423083651620858'
					},
					extractions: [
						{
							id: 'R:-719696986'
						}
					],
					location: {
						location: 'Arizona',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.904866'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-102847579'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905006'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-102847579'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905031'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:-102847579'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905054'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:-728230378'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905184'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:-728230378'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905209'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:-728230378'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905233'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-1879785839'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905362'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-1879785839'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905387'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:-1879785839'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905411'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:2018394268'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905523'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:2018394268'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905549'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:2018394268'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905572'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:1563442282'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905706'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:1563442282'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905731'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:1563442282'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905786'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:1696945602'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905923'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:1696945602'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905948'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:1696945602'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.905972'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:469395013'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906103'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:469395013'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906128'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:469395013'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906152'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-2147315940'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906284'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-2147315940'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906309'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:-2147315940'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906332'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:1394798046'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906439'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:1394798046'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906464'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:1394798046'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906486'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'E:1394798046'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906509'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:-420570018'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906617'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:-420570018'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906641'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:-420570018'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906665'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:-420570018'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906688'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'E:-97894767'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906858'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'E:-97894767'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906882'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'E:-97894767'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906905'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'E:-97894767'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.906928'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:731501372'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907057'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:731501372'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907082'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:731501372'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907106'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:731501372'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907129'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-8019536423268241387'
					},
					extractions: [
						{
							id: 'R:1299812587'
						}
					],
					location: {
						location: 'Komatsu',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907268'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-5329764265752713300'
					},
					extractions: [
						{
							id: 'R:1299812587'
						}
					],
					location: {
						location: 'K.K',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907293'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '579567977094200065'
					},
					extractions: [
						{
							id: 'R:1299812587'
						}
					],
					location: {
						location: 'Meredith',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907316'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:1299812587'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907370'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:-521953121'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907488'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:-992248734'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907622'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '5184780106981504324'
					},
					extractions: [
						{
							id: 'R:-2051698457'
						}
					],
					location: {
						location: 'H.M',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907736'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3143768132050437202'
					},
					extractions: [
						{
							id: 'R:-735404315'
						}
					],
					location: {
						location: 'M.C',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907866'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-3143768132050437202'
					},
					extractions: [
						{
							id: 'R:1497961882'
						}
					],
					location: {
						location: 'M.C',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.907998'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '3450612483642983903'
					},
					extractions: [
						{
							id: 'R:1093468671'
						}
					],
					location: {
						location: 'S.Y',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-07-04 01:15:56.908109'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			}
		]
	}
};
