// @ts-nocheck
/* eslint-disable */
import { Model } from '@/types/Types';

export const SIDARTHE: Model = {
	id: 'biomd0000000955-model-id',
	name: 'Giordano2020 - SIDARTHE model of COVID-19 spread in Italy',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
	schema_name: 'petrinet',
	description: 'Giordano2020 - SIDARTHE model of COVID-19 spread in Italy',
	model_version: '0.1',
	properties: {},
	model: {
		states: [
			{
				id: 'Susceptible',
				name: 'Susceptible',
				grounding: {
					identifiers: {
						ido: '0000514'
					},
					modifiers: {}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Diagnosed',
				name: 'Diagnosed',
				grounding: {
					identifiers: {
						ido: '0000511'
					},
					modifiers: {
						diagnosis: 'ncit:C15220'
					}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Infected',
				name: 'Infected',
				grounding: {
					identifiers: {
						ido: '0000511'
					},
					modifiers: {}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Ailing',
				name: 'Ailing',
				grounding: {
					identifiers: {
						ido: '0000511'
					},
					modifiers: {
						disease_severity: 'ncit:C25269',
						diagnosis: 'ncit:C113725'
					}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Recognized',
				name: 'Recognized',
				grounding: {
					identifiers: {
						ido: '0000511'
					},
					modifiers: {
						diagnosis: 'ncit:C15220'
					}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Healed',
				name: 'Healed',
				grounding: {
					identifiers: {
						ido: '0000592'
					},
					modifiers: {}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Threatened',
				name: 'Threatened',
				grounding: {
					identifiers: {
						ido: '0000511'
					},
					modifiers: {
						disease_severity: 'ncit:C25467'
					}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			},
			{
				id: 'Extinct',
				name: 'Extinct',
				grounding: {
					identifiers: {
						ncit: 'C28554'
					},
					modifiers: {}
				},
				units: {
					expression: '1',
					expression_mathml: '<cn>1</cn>'
				}
			}
		],
		transitions: [
			{
				id: 't1',
				input: ['Diagnosed', 'Susceptible'],
				output: ['Diagnosed', 'Infected'],
				properties: {
					name: 't1'
				}
			},
			{
				id: 't2',
				input: ['Ailing', 'Susceptible'],
				output: ['Ailing', 'Infected'],
				properties: {
					name: 't2'
				}
			},
			{
				id: 't3',
				input: ['Recognized', 'Susceptible'],
				output: ['Recognized', 'Infected'],
				properties: {
					name: 't3'
				}
			},
			{
				id: 't4',
				input: ['Infected', 'Susceptible'],
				output: ['Infected', 'Infected'],
				properties: {
					name: 't4'
				}
			},
			{
				id: 't5',
				input: ['Infected'],
				output: ['Diagnosed'],
				properties: {
					name: 't5'
				}
			},
			{
				id: 't6',
				input: ['Infected'],
				output: ['Ailing'],
				properties: {
					name: 't6'
				}
			},
			{
				id: 't7',
				input: ['Infected'],
				output: ['Healed'],
				properties: {
					name: 't7'
				}
			},
			{
				id: 't8',
				input: ['Diagnosed'],
				output: ['Recognized'],
				properties: {
					name: 't8'
				}
			},
			{
				id: 't9',
				input: ['Diagnosed'],
				output: ['Healed'],
				properties: {
					name: 't9'
				}
			},
			{
				id: 't10',
				input: ['Ailing'],
				output: ['Recognized'],
				properties: {
					name: 't10'
				}
			},
			{
				id: 't11',
				input: ['Ailing'],
				output: ['Healed'],
				properties: {
					name: 't11'
				}
			},
			{
				id: 't12',
				input: ['Ailing'],
				output: ['Threatened'],
				properties: {
					name: 't12'
				}
			},
			{
				id: 't13',
				input: ['Recognized'],
				output: ['Threatened'],
				properties: {
					name: 't13'
				}
			},
			{
				id: 't14',
				input: ['Recognized'],
				output: ['Healed'],
				properties: {
					name: 't14'
				}
			},
			{
				id: 't15',
				input: ['Threatened'],
				output: ['Extinct'],
				properties: {
					name: 't15'
				}
			},
			{
				id: 't16',
				input: ['Threatened'],
				output: ['Healed'],
				properties: {
					name: 't16'
				}
			}
		]
	},
	semantics: {
		ode: {
			rates: [
				{
					target: 't1',
					expression: 'Diagnosed*Susceptible*beta',
					expression_mathml:
						'<apply><times/><ci>Diagnosed</ci><ci>Susceptible</ci><ci>beta</ci></apply>'
				},
				{
					target: 't2',
					expression: 'Ailing*Susceptible*gamma',
					expression_mathml:
						'<apply><times/><ci>Ailing</ci><ci>Susceptible</ci><ci>gamma</ci></apply>'
				},
				{
					target: 't3',
					expression: 'Recognized*Susceptible*delta',
					expression_mathml:
						'<apply><times/><ci>Recognized</ci><ci>Susceptible</ci><ci>delta</ci></apply>'
				},
				{
					target: 't4',
					expression: 'Infected*Susceptible*alpha',
					expression_mathml:
						'<apply><times/><ci>Infected</ci><ci>Susceptible</ci><ci>alpha</ci></apply>'
				},
				{
					target: 't5',
					expression: 'Infected*epsilon',
					expression_mathml: '<apply><times/><ci>Infected</ci><ci>epsilon</ci></apply>'
				},
				{
					target: 't6',
					expression: 'Infected*zeta',
					expression_mathml: '<apply><times/><ci>Infected</ci><ci>zeta</ci></apply>'
				},
				{
					target: 't7',
					expression: 'Infected*lambda',
					expression_mathml: '<apply><times/><ci>Infected</ci><ci>lambda</ci></apply>'
				},
				{
					target: 't8',
					expression: 'Diagnosed*eta',
					expression_mathml: '<apply><times/><ci>Diagnosed</ci><ci>eta</ci></apply>'
				},
				{
					target: 't9',
					expression: 'Diagnosed*rho',
					expression_mathml: '<apply><times/><ci>Diagnosed</ci><ci>rho</ci></apply>'
				},
				{
					target: 't10',
					expression: 'Ailing*theta',
					expression_mathml: '<apply><times/><ci>Ailing</ci><ci>theta</ci></apply>'
				},
				{
					target: 't11',
					expression: 'Ailing*kappa',
					expression_mathml: '<apply><times/><ci>Ailing</ci><ci>kappa</ci></apply>'
				},
				{
					target: 't12',
					expression: 'Ailing*mu',
					expression_mathml: '<apply><times/><ci>Ailing</ci><ci>mu</ci></apply>'
				},
				{
					target: 't13',
					expression: 'Recognized*nu',
					expression_mathml: '<apply><times/><ci>Recognized</ci><ci>nu</ci></apply>'
				},
				{
					target: 't14',
					expression: 'Recognized*xi',
					expression_mathml: '<apply><times/><ci>Recognized</ci><ci>xi</ci></apply>'
				},
				{
					target: 't15',
					expression: 'Threatened*tau',
					expression_mathml: '<apply><times/><ci>Threatened</ci><ci>tau</ci></apply>'
				},
				{
					target: 't16',
					expression: 'Threatened*sigma',
					expression_mathml: '<apply><times/><ci>Threatened</ci><ci>sigma</ci></apply>'
				}
			],
			initials: [
				{
					target: 'Susceptible',
					expression: '0.999996300000000',
					expression_mathml: '<cn>0.99999629999999995</cn>'
				},
				{
					target: 'Diagnosed',
					expression: '3.33333333000000e-7',
					expression_mathml: '<cn>3.33333333e-7</cn>'
				},
				{
					target: 'Infected',
					expression: '3.33333333000000e-6',
					expression_mathml: '<cn>3.3333333299999999e-6</cn>'
				},
				{
					target: 'Ailing',
					expression: '1.66666666000000e-8',
					expression_mathml: '<cn>1.6666666599999999e-8</cn>'
				},
				{
					target: 'Recognized',
					expression: '3.33333333000000e-8',
					expression_mathml: '<cn>3.33333333e-8</cn>'
				},
				{
					target: 'Healed',
					expression: '0.0',
					expression_mathml: '<cn>0.0</cn>'
				},
				{
					target: 'Threatened',
					expression: '0.0',
					expression_mathml: '<cn>0.0</cn>'
				},
				{
					target: 'Extinct',
					expression: '0.0',
					expression_mathml: '<cn>0.0</cn>'
				}
			],
			parameters: [
				{
					id: 'beta',
					value: 0.011,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.008799999999999999,
							maximum: 0.0132
						}
					}
				},
				{
					id: 'gamma',
					value: 0.456,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.3648,
							maximum: 0.5472
						}
					}
				},
				{
					id: 'delta',
					value: 0.011,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.008799999999999999,
							maximum: 0.0132
						}
					}
				},
				{
					id: 'alpha',
					value: 0.57,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.45599999999999996,
							maximum: 0.6839999999999999
						}
					}
				},
				{
					id: 'epsilon',
					value: 0.171,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.1368,
							maximum: 0.20520000000000002
						}
					}
				},
				{
					id: 'zeta',
					value: 0.125,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.1,
							maximum: 0.15
						}
					}
				},
				{
					id: 'lambda',
					value: 0.034,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.027200000000000002,
							maximum: 0.0408
						}
					}
				},
				{
					id: 'eta',
					value: 0.125,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.1,
							maximum: 0.15
						}
					}
				},
				{
					id: 'rho',
					value: 0.034,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.027200000000000002,
							maximum: 0.0408
						}
					}
				},
				{
					id: 'theta',
					value: 0.371,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.2968,
							maximum: 0.4452
						}
					}
				},
				{
					id: 'kappa',
					value: 0.017,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.013600000000000001,
							maximum: 0.0204
						}
					}
				},
				{
					id: 'mu',
					value: 0.017,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.013600000000000001,
							maximum: 0.0204
						}
					}
				},
				{
					id: 'nu',
					value: 0.027,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.0216,
							maximum: 0.0324
						}
					}
				},
				{
					id: 'xi',
					value: 0.017,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.013600000000000001,
							maximum: 0.0204
						}
					}
				},
				{
					id: 'tau',
					value: 0.01,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.008,
							maximum: 0.012
						}
					}
				},
				{
					id: 'sigma',
					value: 0.017,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.013600000000000001,
							maximum: 0.0204
						}
					}
				}
			],
			observables: [
				{
					id: 'Cases',
					name: 'Cases',
					expression: 'Diagnosed + Recognized + Threatened',
					expression_mathml:
						'<apply><plus/><ci>Diagnosed</ci><ci>Recognized</ci><ci>Threatened</ci></apply>'
				},
				{
					id: 'Hospitalizations',
					name: 'Hospitalizations',
					expression: 'Recognized + Threatened',
					expression_mathml: '<apply><plus/><ci>Recognized</ci><ci>Threatened</ci></apply>'
				},
				{
					id: 'Deaths',
					name: 'Deaths',
					expression: 'Extinct',
					expression_mathml: '<ci>Extinct</ci>'
				}
			],
			time: {
				id: 't',
				units: {
					expression: 'day',
					expression_mathml: '<ci>day</ci>'
				}
			}
		}
	},
	metadata: {
		annotations: {
			license: 'CC0',
			authors: [],
			references: ['pubmed:32322102'],
			time_scale: null,
			time_start: null,
			time_end: null,
			locations: [],
			pathogens: ['ncbitaxon:2697049'],
			diseases: ['doid:0080600'],
			hosts: ['ncbitaxon:9606'],
			model_types: ['mamo:0000028']
		},
		attributes: [
			{
				type: 'anchored_extraction',
				amr_element_id: null,
				payload: {
					id: {
						id: 'R:41741717'
					},
					names: [
						{
							id: {
								id: 'T:-1583913404'
							},
							name: 'Wuhan',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 152,
								char_end: 157,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.059131'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:852682657'
							},
							source: 'Hubei Province',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 159,
								char_end: 173,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.059131'
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
						id: 'E:-1572834827'
					},
					names: [
						{
							id: {
								id: 'T:1083612600'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 252,
								char_end: 254,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.061894'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-227920565'
							},
							source:
								'expected number of new cases directly generated by a single infected subject',
							grounding: [
								{
									grounding_text: 'average daily number of new infections generated per case (rt)',
									grounding_id: 'cemo:average_daily_number_of_new_infections_generated_per_case_rt',
									source: [],
									score: 0.8982234597206116,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.061932'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 269,
								char_end: 345,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.061894'
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
						id: 'E:1626461541'
					},
					names: [
						{
							id: {
								id: 'T:1083612600'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 252,
								char_end: 254,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062039'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:111450861'
							},
							source: 'concept of the basic reproduction number',
							grounding: [
								{
									grounding_text: 'basic reproduction number',
									grounding_id: 'apollosv:00000002',
									source: [],
									score: 0.8319894671440125,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.062074'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 210,
								char_end: 250,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062039'
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
						id: 'E:-642689719'
					},
					names: [
						{
							id: {
								id: 'T:1083612600'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 252,
								char_end: 254,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062179'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:111450861'
							},
							source: 'concept of the basic reproduction number',
							grounding: [
								{
									grounding_text: 'basic reproduction number',
									grounding_id: 'apollosv:00000002',
									source: [],
									score: 0.8319894671440125,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.062212'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 210,
								char_end: 250,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062179'
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
				amr_element_id: 'Susceptible',
				payload: {
					id: {
						id: 'E:529212984'
					},
					names: [
						{
							id: {
								id: 'T:-1369779807'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 83,
								char_end: 84,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062381'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-21262210'
							},
							source: 'Susceptible',
							grounding: [
								{
									grounding_text: 'susceptible population',
									grounding_id: 'apollosv:00000234',
									source: [],
									score: 0.8291598558425903,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.062414'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 86,
								char_end: 97,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062381'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.062393'
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
						id: 'R:-1203025862'
					},
					names: [
						{
							id: {
								id: 'T:-844285788'
							},
							name: 's(0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 686,
								char_end: 690,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062533'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1686133646'
							},
							source: 'initial condition',
							grounding: [
								{
									grounding_text: 'initial infection case',
									grounding_id: 'cemo:initial_infection_case',
									source: [],
									score: 0.8012566566467285,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.062565'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 668,
								char_end: 685,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062533'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.8177804350852966,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.062545'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:794618'
					},
					names: [
						{
							id: {
								id: 'T:-1928620769'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 828,
								char_end: 829,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062604'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1457423605'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 832,
									char_end: 833,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062604'
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
						id: 'E:-880374368'
					},
					names: [
						{
							id: {
								id: 'T:1266732625'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 814,
								char_end: 815,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062666'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1915764244'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 818,
									char_end: 819,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062666'
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
						id: 'E:899442783'
					},
					names: [
						{
							id: {
								id: 'T:-1335001936'
							},
							name: 'i',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 800,
								char_end: 801,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062727'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1710058671'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 804,
									char_end: 805,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062727'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.062740'
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
						id: 'E:-375334702'
					},
					names: [
						{
							id: {
								id: 'T:1678260844'
							},
							name: 'R',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 821,
								char_end: 822,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062787'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-300750834'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 825,
									char_end: 826,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062787'
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
						id: 'R:-1658364485'
					},
					names: [
						{
							id: {
								id: 'T:-603614429'
							},
							name: 's',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 792,
								char_end: 793,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062833'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:998779292'
							},
							source: 'equilibrium',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 780,
								char_end: 791,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062833'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.062844'
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
						id: 'E:93386137'
					},
					names: [
						{
							id: {
								id: 'T:-1074718039'
							},
							name: 'E',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 843,
								char_end: 844,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062964'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:216273710'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 848,
									char_end: 849,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.062964'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.062977'
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
						id: 'E:860578991'
					},
					names: [
						{
							id: {
								id: 'T:-318156286'
							},
							name: 'D',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063025'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1300477676'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 811,
									char_end: 812,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063025'
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
						id: 'E:1476596957'
					},
					names: [
						{
							id: {
								id: 'T:-1148839619'
							},
							name: 'tau',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 492,
								char_end: 495,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063083'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1998169796'
							},
							source: 'mortality rate (for infected subjects with life-threatening symptoms',
							grounding: [
								{
									grounding_text: 'number of cases with mild symptoms',
									grounding_id: 'cemo:number_of_cases_with_mild_symptoms',
									source: [],
									score: 0.8557804226875305,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.063115'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 508,
								char_end: 576,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063083'
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
						id: 'E:-1504202527'
					},
					names: [
						{
							id: {
								id: 'T:1087366121'
							},
							name: '\n(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 473,
								char_end: 477,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063152'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1998169796'
							},
							source: 'mortality rate (for infected subjects with life-threatening symptoms',
							grounding: [
								{
									grounding_text: 'number of cases with mild symptoms',
									grounding_id: 'cemo:number_of_cases_with_mild_symptoms',
									source: [],
									score: 0.8557804226875305,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.063184'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 508,
								char_end: 576,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063152'
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
						id: 'R:1897451676'
					},
					names: [
						{
							id: {
								id: 'T:1341233044'
							},
							name: '\n(T)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 410,
								char_end: 414,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063218'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1279959608'
							},
							source: 'lambdaI',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 417,
								char_end: 424,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063218'
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
						id: 'R:1749087011'
					},
					names: [
						{
							id: {
								id: 'T:468089121'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 83,
								char_end: 84,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063289'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1304989986'
							},
							source: 'constant value',
							grounding: [
								{
									grounding_text: 'maximum value',
									grounding_id: 'apollosv:00000433',
									source: [],
									score: 0.8471194505691528,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.063334'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 68,
								char_end: 82,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063289'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063308'
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
						id: 'R:-1449089506'
					},
					names: [
						{
							id: {
								id: 'T:-338420285'
							},
							name: 'S.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 170,
								char_end: 172,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063369'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-537739019'
							},
							source: 'asymptotic feedback gain',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 145,
								char_end: 169,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063369'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'S. typimurium Vi4072',
							grounding_id: 'vo:0004186',
							source: [],
							score: 0.9999999403953552,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063382'
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
						id: 'R:1775598786'
					},
					names: [
						{
							id: {
								id: 'T:-1574729319'
							},
							name: 'S(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 37,
								char_end: 41,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063436'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-617619221'
							},
							source: 'feedback gain',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 23,
								char_end: 36,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063436'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.812811553478241,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063448'
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
						id: 'R:-970667920'
					},
					names: [
						{
							id: {
								id: 'T:517017992'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 322,
								char_end: 323,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063502'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-385536011'
							},
							source: 'feedback',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 313,
								char_end: 321,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063502'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063514'
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
						id: 'R:-1046475552'
					},
					names: [
						{
							id: {
								id: 'T:660436122'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 14,
								char_end: 15,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063602'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:812676225'
							},
							source: 'threshold',
							grounding: [
								{
									grounding_text: 'epidemic threshold',
									grounding_id: 'apollosv:00000531',
									source: [],
									score: 0.7888381481170654,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.063645'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 13,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063602'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063613'
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
						id: 'E:1937623524'
					},
					names: [
						{
							id: {
								id: 'T:-395681841'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 703,
								char_end: 705,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063684'
							}
						}
					],
					descriptions: [],
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
						id: 'R:-2047922106'
					},
					names: [
						{
							id: {
								id: 'T:-296139727'
							},
							name: 's',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 701,
								char_end: 702,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063731'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1101770343'
							},
							source: 'feedback action',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 685,
								char_end: 700,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063731'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063743'
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
						id: 'E:1133084574'
					},
					names: [
						{
							id: {
								id: 'T:-1885700653'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 491,
								char_end: 492,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063797'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:35272411'
							},
							source: 'fraction of population',
							grounding: [
								{
									grounding_text: 'count of susceptible population',
									grounding_id: 'apollosv:00000087',
									source: [],
									score: 0.8512455224990845,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.063829'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 508,
								char_end: 530,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063797'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063809'
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
						id: 'R:1907386256'
					},
					names: [
						{
							id: {
								id: 'T:-1885700653'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 491,
								char_end: 492,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063865'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1584006225'
							},
							source: 'limit',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 485,
								char_end: 490,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063865'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063876'
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
						id: 'E:1099082189'
					},
					names: [
						{
							id: {
								id: 'T:-1885700653'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 491,
								char_end: 492,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063931'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1584006225'
							},
							source: 'limit',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 485,
								char_end: 490,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.063931'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.063942'
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
						id: 'R:-270810242'
					},
					names: [
						{
							id: {
								id: 'T:-1616326269'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 615,
								char_end: 620,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064001'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-58276768'
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
										timestamp: '2023-06-22 23:48:08.064033'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 622,
								char_end: 626,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064001'
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
						id: 'R:-1144539399'
					},
					names: [
						{
							id: {
								id: 'T:1037661599'
							},
							name: 'S(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 376,
								char_end: 380,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064094'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1697350447'
							},
							source: 'infinity',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 367,
								char_end: 375,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064094'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.812811553478241,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.064106'
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
						id: 'R:2009255294'
					},
					names: [
						{
							id: {
								id: 'T:1444399640'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 354,
								char_end: 355,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064193'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1056342975'
							},
							source: 'steady-state value',
							grounding: [
								{
									grounding_text: 'maximum value',
									grounding_id: 'apollosv:00000433',
									source: [],
									score: 0.8147097826004028,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064224'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 335,
								char_end: 353,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064193'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.064204'
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
						id: 'R:1176055633'
					},
					names: [
						{
							id: {
								id: 'T:1887279101'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 323,
								char_end: 325,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064258'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:2120610242'
							},
							source: 'coefficient',
							grounding: [
								{
									grounding_text: 'transmission coefficient',
									grounding_id: 'apollosv:00000191',
									source: [],
									score: 0.7554299831390381,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064291'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 311,
								char_end: 322,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064258'
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
						id: 'R:-1887152667'
					},
					names: [
						{
							id: {
								id: 'T:764038711'
							},
							name: 'F',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 143,
								char_end: 144,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064348'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-493798553'
							},
							source: 'matrix',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 136,
								char_end: 142,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064348'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'rFP-RPV-H/F',
							grounding_id: 'vo:0004752',
							source: [],
							score: 1.0,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.064359'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'R:1578323627'
					},
					names: [
						{
							id: {
								id: 'T:-1485955818'
							},
							name: 't0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 219,
								char_end: 221,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064413'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-986766166'
							},
							source: 'time',
							grounding: [
								{
									grounding_text: 'time since time scale zero',
									grounding_id: 'apollosv:00000272',
									source: [],
									score: 0.8945620059967041,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064445'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 214,
								char_end: 218,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064413'
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
						id: 'R:-1596390550'
					},
					names: [
						{
							id: {
								id: 'T:1526462732'
							},
							name: 'S(t0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 515,
								char_end: 520,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064478'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-808396907'
							},
							source: 'new S',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 511,
								char_end: 516,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064478'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7809536457061768,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.064490'
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
						id: 'E:480122080'
					},
					names: [
						{
							id: {
								id: 'T:-40228838'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 611,
								char_end: 614,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064543'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1110404107'
							},
							source: 'ratio between number of deaths and number of infected',
							grounding: [
								{
									grounding_text: 'number of cases with exposure events',
									grounding_id: 'cemo:number_of_cases_with_exposure_events',
									source: [],
									score: 0.8618428707122803,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064575'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 629,
								char_end: 682,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064543'
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
						id: 'E:-1665374482'
					},
					names: [
						{
							id: {
								id: 'T:-40228838'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 611,
								char_end: 614,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064608'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-530805187'
							},
							source: 'case fatality rate',
							grounding: [
								{
									grounding_text: 'case fatality rate',
									grounding_id: 'cemo:case_fatality_rate',
									source: [],
									score: 1.000000238418579,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064639'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 591,
								char_end: 609,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064608'
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
						id: 'E:-1355066161'
					},
					names: [
						{
							id: {
								id: 'T:-40228838'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 611,
								char_end: 614,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064673'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-530805187'
							},
							source: 'case fatality rate',
							grounding: [
								{
									grounding_text: 'case fatality rate',
									grounding_id: 'cemo:case_fatality_rate',
									source: [],
									score: 1.000000238418579,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.064704'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 591,
								char_end: 609,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064673'
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
						id: 'R:1515895266'
					},
					names: [
						{
							id: {
								id: 'T:-1957427680'
							},
							name: 'CFR M (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 738,
								char_end: 747,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064741'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-59807266'
							},
							source: 'actual CFR',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 731,
								char_end: 741,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064741'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:1805061264'
					},
					names: [
						{
							id: {
								id: 'T:-1957427680'
							},
							name: 'CFR M (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 738,
								char_end: 747,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064820'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064820'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:-1377266171'
					},
					names: [
						{
							id: {
								id: 'T:-1957427680'
							},
							name: 'CFR M (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 738,
								char_end: 747,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064898'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064898'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'R:-69688551'
					},
					names: [
						{
							id: {
								id: 'T:-1957427680'
							},
							name: 'CFR M (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 738,
								char_end: 747,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064975'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.064975'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:-971223536'
					},
					names: [
						{
							id: {
								id: 'T:-1785509785'
							},
							name: 'CFR P (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 766,
								char_end: 775,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065050'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065050'
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
						id: 'R:1910908209'
					},
					names: [
						{
							id: {
								id: 'T:-1785509785'
							},
							name: 'CFR P (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 766,
								char_end: 775,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065130'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:15546313'
							},
							source: 'perceived CFR',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 756,
								char_end: 769,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065130'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:-1310588745'
					},
					names: [
						{
							id: {
								id: 'T:-1785509785'
							},
							name: 'CFR P (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 766,
								char_end: 775,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065236'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065236'
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
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:1912456373'
					},
					names: [
						{
							id: {
								id: 'T:-1785509785'
							},
							name: 'CFR P (t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 766,
								char_end: 775,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065312'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1108966641'
							},
							source: 't',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 807,
								char_end: 808,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065312'
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
						id: 'E:565093588'
					},
					names: [
						{
							id: {
								id: 'T:-1851595288'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 829,
								char_end: 831,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065437'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-632949433'
							},
							value: {
								source: '2.38',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 834,
									char_end: 838,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065437'
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
						id: 'E:-1867668878'
					},
					names: [
						{
							id: {
								id: 'T:-1851595288'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 829,
								char_end: 831,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065506'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:843172514'
							},
							source: 'resulting basic reproduction number',
							grounding: [
								{
									grounding_text: 'basic reproduction number',
									grounding_id: 'apollosv:00000002',
									source: [],
									score: 0.9381847381591797,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.065537'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 790,
								char_end: 825,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065506'
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
						id: 'E:-983492190'
					},
					names: [
						{
							id: {
								id: 'T:-1851595288'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 829,
								char_end: 831,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065583'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:843172514'
							},
							source: 'resulting basic reproduction number',
							grounding: [
								{
									grounding_text: 'basic reproduction number',
									grounding_id: 'apollosv:00000002',
									source: [],
									score: 0.9381847381591797,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.065614'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 790,
								char_end: 825,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065583'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-632949433'
							},
							value: {
								source: '2.38',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 834,
									char_end: 838,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065583'
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
						id: 'E:-1996931464'
					},
					names: [
						{
							id: {
								id: 'T:1406158569'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1351,
								char_end: 1353,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065676'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:355459980'
							},
							value: {
								source: '1.66',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1356,
									char_end: 1360,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065676'
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
						id: 'E:1072365935'
					},
					names: [
						{
							id: {
								id: 'T:1622862489'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1281,
								char_end: 1285,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065746'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-2000313959'
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
										timestamp: '2023-06-22 23:48:08.065777'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1281,
								char_end: 1285,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065746'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.065757'
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
						id: 'E:155764309'
					},
					names: [
						{
							id: {
								id: 'T:2083265388'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1263,
								char_end: 1268,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065825'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1756273236'
							},
							value: {
								source: '0.285',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1271,
									char_end: 1276,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065825'
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
								timestamp: '2023-06-22 23:48:08.065836'
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
						id: 'E:-2021602221'
					},
					names: [
						{
							id: {
								id: 'T:1662178745'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1248,
								char_end: 1253,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065895'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1453445665'
							},
							value: {
								source: '0.422',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1256,
									char_end: 1261,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065895'
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
						id: 'R:516434346'
					},
					names: [
						{
							id: {
								id: 'T:2133818557'
							},
							name: 'consequence of basic social distancing measures',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 933,
								char_end: 980,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065968'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1272896259'
							},
							value: {
								source: '4',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 925,
									char_end: 926,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.065968'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Social Distancing',
							grounding_id: 'ncit:C173636',
							source: [],
							score: 0.8482367992401123,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.065979'
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
						id: 'E:655813297'
					},
					names: [
						{
							id: {
								id: 'T:1349982364'
							},
							name: 'delta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1288,
								char_end: 1293,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066041'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2120684233'
							},
							value: {
								source: '0.0057',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1296,
									char_end: 1302,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066041'
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
						id: 'R:52866099'
					},
					names: [
						{
							id: {
								id: 'T:-217647733'
							},
							name: 'b',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2087,
								char_end: 2088,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066109'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-132225396'
							},
							source: 'Figure 3',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2077,
								char_end: 2085,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066109'
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
								timestamp: '2023-06-22 23:48:08.066120'
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
						id: 'R:1275271689'
					},
					names: [
						{
							id: {
								id: 'T:1726628926'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1830,
								char_end: 1831,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066210'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:221585350'
							},
							source: 'Figure 3',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1820,
								char_end: 1828,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066210'
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
						id: 'E:309602204'
					},
					names: [
						{
							id: {
								id: 'T:939711242'
							},
							name: 'D',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 479,
								char_end: 480,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066287'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2098211608'
							},
							value: {
								source: '20/60',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 483,
									char_end: 488,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066287'
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
						id: 'E:1403320282'
					},
					names: [
						{
							id: {
								id: 'T:337923970'
							},
							name: 'H',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 523,
								char_end: 524,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066380'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:769070552'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 527,
									char_end: 528,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066380'
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
						id: 'E:1814788090'
					},
					names: [
						{
							id: {
								id: 'T:-1849118411'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 537,
								char_end: 538,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066462'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:983490999'
							},
							value: {
								source: '1',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 541,
									char_end: 542,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066462'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.066473'
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
						id: 'E:-1773271836'
					},
					names: [
						{
							id: {
								id: 'T:-614286799'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 492,
								char_end: 493,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066545'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1247333891'
							},
							value: {
								source: '1/60',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 496,
									char_end: 500,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066545'
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
						id: 'E:-448855073'
					},
					names: [
						{
							id: {
								id: 'T:-614286799'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 492,
								char_end: 493,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066639'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1037251210'
							},
							value: {
								source: 'E6',
								grounding: [
									{
										grounding_text: 'HPV E6',
										grounding_id: 'vo:0011107',
										source: [],
										score: 0.8621531128883362,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-06-22 23:48:08.066669'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 500,
									char_end: 502,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066639'
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
						id: 'E:-471656782'
					},
					names: [
						{
							id: {
								id: 'T:-614286799'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 492,
								char_end: 493,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066732'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1247333891'
							},
							value: {
								source: '1/60',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 496,
									char_end: 500,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066732'
							}
						},
						{
							id: {
								id: 'T:1037251210'
							},
							value: {
								source: 'E6',
								grounding: [
									{
										grounding_text: 'HPV E6',
										grounding_id: 'vo:0011107',
										source: [],
										score: 0.8621531128883362,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-06-22 23:48:08.066788'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 500,
									char_end: 502,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066732'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:1085386362'
					},
					names: [
						{
							id: {
								id: 'T:454304639'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 516,
								char_end: 517,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066852'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-49953727'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 520,
									char_end: 521,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066852'
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
						id: 'E:701994054'
					},
					names: [
						{
							id: {
								id: 'T:1034753969'
							},
							name: 'i',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 465,
								char_end: 466,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066934'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-714197498'
							},
							value: {
								source: '200/60',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 469,
									char_end: 475,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.066934'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.066950'
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
						id: 'E:656388145'
					},
					names: [
						{
							id: {
								id: 'T:484964733'
							},
							name: 'E',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 530,
								char_end: 531,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067027'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1941583775'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 534,
									char_end: 535,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067027'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.067038'
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
						id: 'E:-1037765850'
					},
					names: [
						{
							id: {
								id: 'T:-1955937928'
							},
							name: 'R',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 504,
								char_end: 505,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067139'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-2095731320'
							},
							value: {
								source: '2/60',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 508,
									char_end: 512,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067139'
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
						id: 'E:-150819206'
					},
					names: [
						{
							id: {
								id: 'T:1872801930'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 640,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067224'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1094631673'
							},
							source: 'alpha = 0.570',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 611,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067224'
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
								timestamp: '2023-06-22 23:48:08.067235'
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
						id: 'E:1839186941'
					},
					names: [
						{
							id: {
								id: 'T:1872801930'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 640,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067315'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-582570475'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.067345'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 640,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067315'
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
								timestamp: '2023-06-22 23:48:08.067325'
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
						id: 'E:2035882106'
					},
					names: [
						{
							id: {
								id: 'T:1872801930'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 640,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067406'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-582570475'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.067437'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 640,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067406'
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
								timestamp: '2023-06-22 23:48:08.067417'
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
						id: 'E:-1900111227'
					},
					names: [
						{
							id: {
								id: 'T:-874987909'
							},
							name: 'sigma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 771,
								char_end: 776,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067498'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1618961540'
							},
							value: {
								source: '0.017',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 779,
									char_end: 784,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067498'
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
						id: 'E:731098284'
					},
					names: [
						{
							id: {
								id: 'T:-514589809'
							},
							name: 'theta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 666,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067583'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-166237200'
							},
							value: {
								source: '0.371',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 669,
									char_end: 674,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067583'
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
						id: 'E:-869515938'
					},
					names: [
						{
							id: {
								id: 'T:-514589809'
							},
							name: 'theta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 666,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067665'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-2083866507'
							},
							source: 'theta',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 666,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067665'
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
						id: 'E:-1543854713'
					},
					names: [
						{
							id: {
								id: 'T:-514589809'
							},
							name: 'theta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 666,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067733'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1094631673'
							},
							source: 'alpha = 0.570',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 611,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067733'
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
						id: 'E:-1015596706'
					},
					names: [
						{
							id: {
								id: 'T:-514589809'
							},
							name: 'theta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 666,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067815'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1094631673'
							},
							source: 'alpha = 0.570',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 611,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067815'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-166237200'
							},
							value: {
								source: '0.371',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 669,
									char_end: 674,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067815'
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
						id: 'E:982082149'
					},
					names: [
						{
							id: {
								id: 'T:-684788638'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 613,
								char_end: 617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067913'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1913423711'
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
										timestamp: '2023-06-22 23:48:08.067944'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 613,
								char_end: 617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.067913'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.067924'
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
						id: 'E:639958349'
					},
					names: [
						{
							id: {
								id: 'T:-684788638'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 613,
								char_end: 617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068031'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1529471482'
							},
							source: 'alpha = 0.570, beta = delta = 0.011, gamma = 0.456, \u0001 =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 653,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068031'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.068043'
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
						id: 'E:-1141093632'
					},
					names: [
						{
							id: {
								id: 'T:-684788638'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 613,
								char_end: 617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068128'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1529471482'
							},
							source: 'alpha = 0.570, beta = delta = 0.011, gamma = 0.456, \u0001 =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 653,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068128'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.068139'
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
						id: 'E:-337625904'
					},
					names: [
						{
							id: {
								id: 'T:-1346029037'
							},
							name: 'micro',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 696,
								char_end: 701,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068226'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1327197507'
							},
							value: {
								source: '0.012',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 704,
									char_end: 709,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068226'
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
						id: 'E:842581186'
					},
					names: [
						{
							id: {
								id: 'T:217232145'
							},
							name: 'eta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 683,
								char_end: 686,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068316'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1300661898'
							},
							value: {
								source: '0.125',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 689,
									char_end: 694,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068316'
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
						id: 'E:59384549'
					},
					names: [
						{
							id: {
								id: 'T:-198435099'
							},
							name: 'zeta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 676,
								char_end: 680,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068403'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-901363481'
							},
							source: 'zeta',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 676,
								char_end: 680,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068403'
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
						id: 'E:-1807936182'
					},
					names: [
						{
							id: {
								id: 'T:-198435099'
							},
							name: 'zeta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 676,
								char_end: 680,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068498'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1094631673'
							},
							source: 'alpha = 0.570',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 611,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068498'
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
						id: 'E:-1144064762'
					},
					names: [
						{
							id: {
								id: 'T:-198435099'
							},
							name: 'zeta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 676,
								char_end: 680,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068594'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1094631673'
							},
							source: 'alpha = 0.570',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 611,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068594'
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
						id: 'E:-162125520'
					},
					names: [
						{
							id: {
								id: 'T:-707171401'
							},
							name: 'rho',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 745,
								char_end: 748,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068690'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1304782420'
							},
							value: {
								source: '0.034',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 751,
									char_end: 756,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068690'
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
								timestamp: '2023-06-22 23:48:08.068702'
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
						id: 'E:-1611099537'
					},
					names: [
						{
							id: {
								id: 'T:-1521570306'
							},
							name: 'lambda',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 736,
								char_end: 742,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068777'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1677274350'
							},
							source: 'lambda',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 736,
								char_end: 742,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068777'
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
						id: 'E:-1749692150'
					},
					names: [
						{
							id: {
								id: 'T:-2116594380'
							},
							name: 'nu',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 711,
								char_end: 713,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068869'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1207688676'
							},
							value: {
								source: '0.027',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 716,
									char_end: 721,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068869'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Nu Chinese',
							grounding_id: 'ncit:C158198',
							source: [],
							score: 0.834415853023529,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.068890'
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
						id: 'E:1476771929'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068971'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1164930662'
							},
							value: {
								source: '0.171',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 654,
									char_end: 659,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.068971'
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
						id: 'E:-537166879'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069082'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:66126763'
							},
							source: 'beta =',
							grounding: [
								{
									grounding_text: 'beta distribution',
									grounding_id: 'apollosv:00000078',
									source: [],
									score: 0.807852566242218,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.069114'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 613,
								char_end: 619,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069082'
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
						id: 'E:-2110254694'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069175'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:478662827'
							},
							source: 'gamma =',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.069207'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 635,
								char_end: 642,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069175'
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
						id: 'E:486120709'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069272'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:652598983'
							},
							source: 'alpha =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 605,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069272'
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
						id: 'E:1370870177'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069367'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1968064238'
							},
							source: 'zeta =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 676,
								char_end: 682,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069367'
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
						id: 'E:-1898900327'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069459'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-103118656'
							},
							source: 'theta =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 668,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069459'
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
						id: 'E:-1064027010'
					},
					names: [
						{
							id: {
								id: 'T:1318270317'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 598,
								char_end: 603,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069541'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-103118656'
							},
							source: 'theta =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 661,
								char_end: 668,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069541'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-1164930662'
							},
							value: {
								source: '0.171',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 654,
									char_end: 659,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069541'
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
						id: 'E:867390604'
					},
					names: [
						{
							id: {
								id: 'T:1060055657'
							},
							name: 'tau',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 723,
								char_end: 726,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069647'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:483191385'
							},
							value: {
								source: '0.003',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 729,
									char_end: 734,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069647'
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
						id: 'R:1412329811'
					},
					names: [
						{
							id: {
								id: 'T:-1191187700'
							},
							name: 'c',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 295,
								char_end: 296,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069905'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:968932598'
							},
							source: 'Figure 3',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 285,
								char_end: 293,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069905'
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
								timestamp: '2023-06-22 23:48:08.069916'
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
						id: 'E:-164304490'
					},
					names: [
						{
							id: {
								id: 'T:-782102941'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3529,
								char_end: 3532,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069997'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-767157625'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3557,
									char_end: 3558,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.069997'
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
						id: 'E:285558751'
					},
					names: [
						{
							id: {
								id: 'T:-782102941'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3529,
								char_end: 3532,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070055'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-388178501'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3554,
									char_end: 3557,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070055'
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
						id: 'E:1278791469'
					},
					names: [
						{
							id: {
								id: 'T:-782102941'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3529,
								char_end: 3532,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070114'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-388178501'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3554,
									char_end: 3557,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070114'
							}
						},
						{
							id: {
								id: 'T:-767157625'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3557,
									char_end: 3558,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070114'
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
						id: 'E:-1595612742'
					},
					names: [
						{
							id: {
								id: 'T:1083586557'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2789,
								char_end: 2794,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070221'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1255786877'
							},
							value: {
								source: '0.200',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2797,
									char_end: 2802,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070221'
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
						id: 'E:1442555840'
					},
					names: [
						{
							id: {
								id: 'T:283611718'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2807,
								char_end: 2812,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070279'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-526348368'
							},
							value: {
								source: '0.086',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2815,
									char_end: 2820,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070279'
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
								timestamp: '2023-06-22 23:48:08.070291'
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
						id: 'E:-1152275028'
					},
					names: [
						{
							id: {
								id: 'T:-761919821'
							},
							name: 'hence R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2822,
								char_end: 2830,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070339'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:961919322'
							},
							value: {
								source: '0.787',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2833,
									char_end: 2838,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070339'
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
						id: 'R:-1554905995'
					},
					names: [
						{
							id: {
								id: 'T:1185877618'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3944,
								char_end: 3945,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070398'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:815287967'
							},
							source: 'model',
							grounding: [
								{
									grounding_text: 'Susceptible-Infected-Resistant model',
									grounding_id: 'apollosv:00000549',
									source: [],
									score: 0.9977449774742126,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.070430'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3937,
								char_end: 3942,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070398'
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
						id: 'R:-916144941'
					},
					names: [
						{
							id: {
								id: 'T:100319117'
							},
							name: 'e',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3929,
								char_end: 3930,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070464'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:677429010'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.070496'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3919,
								char_end: 3927,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070464'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.070476'
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
						id: 'E:932202167'
					},
					names: [
						{
							id: {
								id: 'T:-1916480943'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3812,
								char_end: 3817,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070533'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:75383108'
							},
							value: {
								source: '0.057',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3820,
									char_end: 3825,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070533'
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
								timestamp: '2023-06-22 23:48:08.070544'
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
						id: 'E:-1923005480'
					},
					names: [
						{
							id: {
								id: 'T:-1221030314'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3833,
								char_end: 3835,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070592'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1486976605'
							},
							source: 'alpha =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3811,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070592'
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
						id: 'E:-2110401870'
					},
					names: [
						{
							id: {
								id: 'T:-114276190'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3809,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070657'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1088132821'
							},
							source: 'hence R0 =',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3827,
								char_end: 3837,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070657'
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
						id: 'E:1924537591'
					},
					names: [
						{
							id: {
								id: 'T:-114276190'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3809,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070723'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1999865981'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3809,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070723'
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
						id: 'E:706631770'
					},
					names: [
						{
							id: {
								id: 'T:-114276190'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3809,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070787'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1999865981'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3804,
								char_end: 3809,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070787'
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
						id: 'E:-1084305333'
					},
					names: [
						{
							id: {
								id: 'T:-507776082'
							},
							name: 'hence R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3827,
								char_end: 3835,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070856'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1220358926'
							},
							value: {
								source: '0.329',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 3838,
									char_end: 3843,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070856'
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
						id: 'E:1901934478'
					},
					names: [
						{
							id: {
								id: 'T:-606000886'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1650,
								char_end: 1652,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070914'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-198957171'
							},
							value: {
								source: '1.13',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1655,
									char_end: 1659,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070914'
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
						id: 'E:-499016613'
					},
					names: [
						{
							id: {
								id: 'T:1523833260'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1612,
								char_end: 1617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070972'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-2050162743'
							},
							value: {
								source: '0.285',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1620,
									char_end: 1625,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.070972'
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
						id: 'E:1467291745'
					},
					names: [
						{
							id: {
								id: 'T:1523833260'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1612,
								char_end: 1617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071031'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1762833634'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1612,
								char_end: 1617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071031'
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
						id: 'E:685291813'
					},
					names: [
						{
							id: {
								id: 'T:1523833260'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1612,
								char_end: 1617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071097'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1762833634'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1612,
								char_end: 1617,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071097'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-2050162743'
							},
							value: {
								source: '0.285',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1620,
									char_end: 1625,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071097'
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
						id: 'E:189007010'
					},
					names: [
						{
							id: {
								id: 'T:855851760'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1630,
								char_end: 1635,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071178'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1029849785'
							},
							value: {
								source: '0.171',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1638,
									char_end: 1643,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071178'
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
								timestamp: '2023-06-22 23:48:08.071190'
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
						id: 'E:-1209446131'
					},
					names: [
						{
							id: {
								id: 'T:855851760'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1630,
								char_end: 1635,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071265'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:702313054'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071298'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1630,
								char_end: 1635,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071265'
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
								timestamp: '2023-06-22 23:48:08.071277'
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
						id: 'E:781230004'
					},
					names: [
						{
							id: {
								id: 'T:855851760'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1630,
								char_end: 1635,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071336'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:702313054'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071369'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1630,
								char_end: 1635,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071336'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:1029849785'
							},
							value: {
								source: '0.171',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1638,
									char_end: 1643,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071336'
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
								timestamp: '2023-06-22 23:48:08.071349'
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
						id: 'R:-605739704'
					},
					names: [
						{
							id: {
								id: 'T:1148332390'
							},
							name: 'b',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2513,
								char_end: 2514,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071420'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-882740915'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071454'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2503,
								char_end: 2511,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071420'
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
								timestamp: '2023-06-22 23:48:08.071433'
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
						id: 'R:449712399'
					},
					names: [
						{
							id: {
								id: 'T:-556733357'
							},
							name: 'c',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3063,
								char_end: 3064,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071494'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1204401021'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071527'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3053,
								char_end: 3061,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071494'
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
								timestamp: '2023-06-22 23:48:08.071506'
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
						id: 'R:-808337145'
					},
					names: [
						{
							id: {
								id: 'T:437240923'
							},
							name: 'd',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3647,
								char_end: 3648,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071570'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-141476256'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071602'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 3637,
								char_end: 3645,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071570'
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
						id: 'E:-1441874775'
					},
					names: [
						{
							id: {
								id: 'T:1221359455'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1994,
								char_end: 1995,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071636'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1447922491'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.071668'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1984,
								char_end: 1992,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071636'
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
						id: 'R:1693417853'
					},
					names: [
						{
							id: {
								id: 'T:647521361'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 774,
								char_end: 777,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071701'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1494810029'
							},
							source: 'perceived',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 764,
								char_end: 773,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071701'
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
						id: 'E:793459689'
					},
					names: [
						{
							id: {
								id: 'T:-1576527514'
							},
							name: 'perceived CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 764,
								char_end: 777,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071784'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2841217'
							},
							value: {
								source: '9.0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 788,
									char_end: 791,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071784'
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
						id: 'E:1964966967'
					},
					names: [
						{
							id: {
								id: 'T:-1576527514'
							},
							name: 'perceived CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 764,
								char_end: 777,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071858'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:935906754'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 791,
									char_end: 792,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071858'
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
						id: 'E:176422678'
					},
					names: [
						{
							id: {
								id: 'T:-1576527514'
							},
							name: 'perceived CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 764,
								char_end: 777,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071939'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2841217'
							},
							value: {
								source: '9.0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 788,
									char_end: 791,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071939'
							}
						},
						{
							id: {
								id: 'T:935906754'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 791,
									char_end: 792,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.071939'
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
						id: 'E:-365122890'
					},
					names: [
						{
							id: {
								id: 'T:1365486531'
							},
							name: 'actual CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 722,
								char_end: 732,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072032'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1196723365'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 748,
									char_end: 751,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072032'
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
						id: 'E:502104994'
					},
					names: [
						{
							id: {
								id: 'T:1365486531'
							},
							name: 'actual CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 722,
								char_end: 732,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072103'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:230372928'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 751,
									char_end: 752,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072103'
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
						id: 'E:-99107917'
					},
					names: [
						{
							id: {
								id: 'T:1365486531'
							},
							name: 'actual CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 722,
								char_end: 732,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072179'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1196723365'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 748,
									char_end: 751,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072179'
							}
						},
						{
							id: {
								id: 'T:230372928'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 751,
									char_end: 752,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072179'
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
						id: 'R:-190532779'
					},
					names: [
						{
							id: {
								id: 'T:700244759'
							},
							name: 'd',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 892,
								char_end: 893,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072303'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-794089561'
							},
							source: 'Figure 3',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 882,
								char_end: 890,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072303'
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
						id: 'E:541428091'
					},
					names: [
						{
							id: {
								id: 'T:916306735'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2395,
								char_end: 2398,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072381'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:619949824'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2423,
									char_end: 2424,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072381'
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
						id: 'E:1710112592'
					},
					names: [
						{
							id: {
								id: 'T:916306735'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2395,
								char_end: 2398,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072439'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2079051331'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2420,
									char_end: 2423,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072439'
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
						id: 'E:-1292945892'
					},
					names: [
						{
							id: {
								id: 'T:916306735'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 2395,
								char_end: 2398,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072496'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2079051331'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2420,
									char_end: 2423,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072496'
							}
						},
						{
							id: {
								id: 'T:619949824'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 2423,
									char_end: 2424,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072496'
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
						id: 'E:-1832637322'
					},
					names: [
						{
							id: {
								id: 'T:-182274081'
							},
							name: 'RTPCR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1899,
								char_end: 1904,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072819'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:436092260'
							},
							source: 'aspecific real-time reverse transcriptase polymerase chain reaction',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1830,
								char_end: 1897,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072819'
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
						id: 'R:-1402677534'
					},
					names: [
						{
							id: {
								id: 'T:694767838'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 514,
								char_end: 517,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072878'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1426198528'
							},
							source: 'perceived',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 504,
								char_end: 513,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.072878'
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
						id: 'E:409377216'
					},
					names: [
						{
							id: {
								id: 'T:934612484'
							},
							name: 'alpha S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 97,
								char_end: 104,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073250'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1147671990'
							},
							source: 'dynamical matrix of the linearised system around the equilibrium',
							grounding: [
								{
									grounding_text: 'Semantic_Type',
									grounding_id: 'ncit:P106',
									source: [],
									score: 0.8126084208488464,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.073280'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 68,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073250'
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
						id: 'E:520353751'
					},
					names: [
						{
							id: {
								id: 'T:-1995875541'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 99,
								char_end: 100,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073321'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-2010315210'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 104,
									char_end: 105,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073321'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073332'
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
						id: 'R:664696261'
					},
					names: [
						{
							id: {
								id: 'T:-1995875541'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 99,
								char_end: 100,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073378'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:744142845'
							},
							source: 'limit',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 93,
								char_end: 98,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073378'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073389'
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
						id: 'E:181413096'
					},
					names: [
						{
							id: {
								id: 'T:-1995875541'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 99,
								char_end: 100,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073441'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:744142845'
							},
							source: 'limit',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 93,
								char_end: 98,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073441'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-2010315210'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 104,
									char_end: 105,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073441'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073452'
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
						id: 'R:-1459106808'
					},
					names: [
						{
							id: {
								id: 'T:-1250845592'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 698,
								char_end: 699,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073521'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:723261549'
							},
							source: 'non-negative value',
							grounding: [
								{
									grounding_text: 'predictive value positive (pvp)',
									grounding_id: 'cemo:predictive_value_positive',
									source: [],
									score: 0.7932042479515076,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.073552'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 679,
								char_end: 697,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073521'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073533'
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
						id: 'R:-1692706449'
					},
					names: [
						{
							id: {
								id: 'T:-1226000216'
							},
							name: 'S.',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 220,
								char_end: 222,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073615'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1834844667'
							},
							source: 'static feedback',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 204,
								char_end: 219,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073615'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'S. typimurium Vi4072',
							grounding_id: 'vo:0004186',
							source: [],
							score: 0.9999999403953552,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073626'
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
						id: 'R:-187567099'
					},
					names: [
						{
							id: {
								id: 'T:-285368909'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 245,
								char_end: 246,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073678'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:53615255'
							},
							source: 'contradiction',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 230,
								char_end: 243,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073678'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073690'
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
						id: 'R:181411702'
					},
					names: [
						{
							id: {
								id: 'T:932647536'
							},
							name: 'F',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 322,
								char_end: 323,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073748'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-423242395'
							},
							source: 'matrix',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 315,
								char_end: 321,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073748'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'rFP-RPV-H/F',
							grounding_id: 'vo:0004752',
							source: [],
							score: 1.0,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.073759'
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
						id: 'R:2094634333'
					},
					names: [
						{
							id: {
								id: 'T:738991714'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1264,
								char_end: 1267,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073860'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:661471688'
							},
							source: 'reported',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1255,
								char_end: 1263,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.073860'
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
						id: 'R:-1156681184'
					},
					names: [
						{
							id: {
								id: 'T:-1543370431'
							},
							name: 'IRCCS',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 50,
								char_end: 55,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074209'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-585021925'
							},
							source: 'whole COVID19',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 36,
								char_end: 49,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074209'
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
						id: 'E:-394710046'
					},
					names: [
						{
							id: {
								id: 'T:1077748546'
							},
							name: 'Wuhan',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 152,
								char_end: 157,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074357'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-269826086'
							},
							source: 'China',
							grounding: [
								{
									grounding_text: 'Chinese',
									grounding_id: 'ncit:C43391',
									source: [],
									score: 0.7607220411300659,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.074388'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 175,
								char_end: 180,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074357'
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
						id: 'R:-1235282259'
					},
					names: [
						{
							id: {
								id: 'T:1077748546'
							},
							name: 'Wuhan',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 152,
								char_end: 157,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074476'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1618012989'
							},
							source: 'Hubei Province',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 159,
								char_end: 173,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074476'
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
						id: 'E:1492964080'
					},
					names: [
						{
							id: {
								id: 'T:1077748546'
							},
							name: 'Wuhan',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 152,
								char_end: 157,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074595'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1618012989'
							},
							source: 'Hubei Province',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 159,
								char_end: 173,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074595'
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
						id: 'E:-1367776669'
					},
					names: [
						{
							id: {
								id: 'T:-664405266'
							},
							name: 'delta respectively',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 242,
								char_end: 260,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074782'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-262124120'
							},
							source: 'transmission rate',
							grounding: [
								{
									grounding_text: 'transmission probability',
									grounding_id: 'apollosv:00000211',
									source: [],
									score: 0.8650991916656494,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.074813'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 272,
								char_end: 289,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074782'
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
						id: 'R:782330885'
					},
					names: [
						{
							id: {
								id: 'T:-664405266'
							},
							name: 'delta respectively',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 242,
								char_end: 260,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074846'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1926783538'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.074877'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 235,
								char_end: 240,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074846'
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
						id: 'E:-1320767190'
					},
					names: [
						{
							id: {
								id: 'T:-664405266'
							},
							name: 'delta respectively',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 242,
								char_end: 260,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074915'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1926783538'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.074947'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 235,
								char_end: 240,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074915'
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
						id: 'R:-1769863706'
					},
					names: [
						{
							id: {
								id: 'T:-2124864976'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 222,
								char_end: 227,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074979'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-727003121'
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
										timestamp: '2023-06-22 23:48:08.075010'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 229,
								char_end: 233,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.074979'
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
						id: 'R:1140900403'
					},
					names: [
						{
							id: {
								id: 'T:-603927318'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 229,
								char_end: 233,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075041'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1926783538'
							},
							source: 'gamma',
							grounding: [
								{
									grounding_text: 'gamma distribution',
									grounding_id: 'apollosv:00000255',
									source: [],
									score: 0.8174800872802734,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.075074'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 235,
								char_end: 240,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075041'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.075053'
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
						id: 'R:-1107189402'
					},
					names: [
						{
							id: {
								id: 'T:-603927318'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 229,
								char_end: 233,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075108'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:365681332'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 222,
								char_end: 227,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075108'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.075120'
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
						id: 'R:-19417290'
					},
					names: [
						{
							id: {
								id: 'T:-603927318'
							},
							name: 'beta',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 229,
								char_end: 233,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075173'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:365681332'
							},
							source: 'alpha',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 222,
								char_end: 227,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075173'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'beta distribution',
							grounding_id: 'apollosv:00000078',
							source: [],
							score: 0.807852566242218,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.075186'
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
						id: 'R:-1454116348'
					},
					names: [
						{
							id: {
								id: 'T:-1114453814'
							},
							name: 'gamma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 235,
								char_end: 240,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075240'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-727003121'
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
										timestamp: '2023-06-22 23:48:08.075270'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 229,
								char_end: 233,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075240'
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
								timestamp: '2023-06-22 23:48:08.075251'
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
						id: 'E:-786619791'
					},
					names: [
						{
							id: {
								id: 'T:1707193398'
							},
							name: 'nu respectively',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 12,
								char_end: 27,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075708'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1505109352'
							},
							source:
								'rate at which undetected and detected infected subjects develop life-threatening symptoms',
							grounding: [
								{
									grounding_text: 'number of cases with mild symptoms',
									grounding_id: 'cemo:number_of_cases_with_mild_symptoms',
									source: [],
									score: 0.7863930463790894,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.075741'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 39,
								char_end: 128,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075708'
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
						id: 'E:1986004233'
					},
					names: [
						{
							id: {
								id: 'T:-1076318996'
							},
							name: 'tau',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 393,
								char_end: 396,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075773'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-315179847'
							},
							source: 'mortality rate (for infected subjects with life-threatening symptoms',
							grounding: [
								{
									grounding_text: 'number of cases with mild symptoms',
									grounding_id: 'cemo:number_of_cases_with_mild_symptoms',
									source: [],
									score: 0.8557804226875305,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.075804'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 409,
								char_end: 477,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075773'
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
						id: 'E:314251800'
					},
					names: [
						{
							id: {
								id: 'T:-899622500'
							},
							name: 'H(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 222,
								char_end: 226,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075890'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1977946204'
							},
							source: 'cumulative variables',
							grounding: [
								{
									grounding_text: 'cumulative cases',
									grounding_id: 'cemo:cumulative_cases',
									source: [],
									score: 0.7815257906913757,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.075921'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 240,
								char_end: 260,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075890'
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
						id: 'E:-490005798'
					},
					names: [
						{
							id: {
								id: 'T:1531314518'
							},
							name: 'E(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 231,
								char_end: 235,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075955'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1977946204'
							},
							source: 'cumulative variables',
							grounding: [
								{
									grounding_text: 'cumulative cases',
									grounding_id: 'cemo:cumulative_cases',
									source: [],
									score: 0.7815257906913757,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.075986'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 240,
								char_end: 260,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.075955'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 0.7558534741401672,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.075967'
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
						id: 'R:-26505195'
					},
					names: [
						{
							id: {
								id: 'T:-764572357'
							},
							name: 'sum of the states',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 510,
								char_end: 527,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076024'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-427509146'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 497,
									char_end: 498,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076024'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Component of the Office of the Director',
							grounding_id: 'ncit:C20169',
							source: [],
							score: 0.7936972975730896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.076038'
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
						id: 'E:-685281417'
					},
					names: [
						{
							id: {
								id: 'T:-332390493'
							},
							name: 'R',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 29,
								char_end: 30,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076146'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2006821737'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 33,
									char_end: 34,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076146'
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
						id: 'E:-1423274836'
					},
					names: [
						{
							id: {
								id: 'T:-1411557663'
							},
							name: 'D',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 16,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076211'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:2016731416'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 19,
									char_end: 20,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076211'
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
						id: 'E:1002363864'
					},
					names: [
						{
							id: {
								id: 'T:-1680969068'
							},
							name: 'i',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 8,
								char_end: 9,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076283'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1877658221'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 12,
									char_end: 13,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076283'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.076296'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:1384246621'
					},
					names: [
						{
							id: {
								id: 'T:1700423499'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 36,
								char_end: 37,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076360'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:176628772'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 40,
									char_end: 41,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076360'
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
						id: 'E:335068517'
					},
					names: [
						{
							id: {
								id: 'T:1932856936'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 22,
								char_end: 23,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076436'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-69112034'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 26,
									char_end: 27,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076436'
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
						id: 'E:-307493232'
					},
					names: [
						{
							id: {
								id: 'T:1555097109'
							},
							name: 'E',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 0,
								char_end: 1,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076512'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:41096397'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 5,
									char_end: 6,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076512'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.076523'
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
						id: 'E:1698674450'
					},
					names: [
						{
							id: {
								id: 'T:1343425438'
							},
							name: 'D',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 16,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076588'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1697694077'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 19,
									char_end: 20,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076588'
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
						id: 'E:-1785399098'
					},
					names: [
						{
							id: {
								id: 'T:2058852478'
							},
							name: 'a',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 22,
								char_end: 23,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076646'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1354482909'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 26,
									char_end: 27,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076646'
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
						id: 'E:653602149'
					},
					names: [
						{
							id: {
								id: 'T:965714716'
							},
							name: 'i',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 8,
								char_end: 9,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076706'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1056303516'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 12,
									char_end: 13,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076706'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.076718'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 't1',
				payload: {
					id: {
						id: 'E:-4341478'
					},
					names: [
						{
							id: {
								id: 'T:2139207946'
							},
							name: 'T',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 36,
								char_end: 37,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076792'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-539101108'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 40,
									char_end: 41,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076792'
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
						id: 'E:1040513311'
					},
					names: [
						{
							id: {
								id: 'T:1637044147'
							},
							name: 'R',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 29,
								char_end: 30,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076853'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-466976847'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 33,
									char_end: 34,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076853'
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
						id: 'E:48974076'
					},
					names: [
						{
							id: {
								id: 'T:671637507'
							},
							name: 'E',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 13,
								char_end: 14,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076903'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1659023953'
							},
							value: {
								source: '1',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 17,
									char_end: 18,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076903'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.076916'
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
						id: 'R:-1018052126'
					},
					names: [
						{
							id: {
								id: 'T:-458496317'
							},
							name: 'H',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 862,
								char_end: 863,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076961'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1406529153'
							},
							source: 'asymptotic values',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 844,
								char_end: 861,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.076961'
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
						id: 'E:874921230'
					},
					names: [
						{
							id: {
								id: 'T:1475790671'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 297,
								char_end: 298,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077032'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-596228942'
							},
							source: 'second including variables H',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 347,
								char_end: 375,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077032'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077043'
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
						id: 'R:-109365585'
					},
					names: [
						{
							id: {
								id: 'T:-322511070'
							},
							name: 'x',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 1162,
								char_end: 1163,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077098'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1589383106'
							},
							value: {
								source: 'I D A R T',
								grounding: [
									{
										grounding_text: 'Meruvax I',
										grounding_id: 'vo:0003109',
										source: [],
										score: 0.861896276473999,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-06-22 23:48:08.077130'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 1167,
									char_end: 1176,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077098'
							}
						}
					],
					groundings: [],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'Infected',
				payload: {
					id: {
						id: 'E:2003446033'
					},
					names: [
						{
							id: {
								id: 'T:-212538109'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 736,
								char_end: 737,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077167'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1954183869'
							},
							source: 'infected individuals',
							grounding: [
								{
									grounding_text: 'exposed individuals',
									grounding_id: 'cemo:exposed_individuals',
									source: [],
									score: 0.8635380864143372,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.077200'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 663,
								char_end: 683,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077167'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077178'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'anchored_extraction',
				amr_element_id: 'Infected',
				payload: {
					id: {
						id: 'E:633593518'
					},
					names: [
						{
							id: {
								id: 'T:2093205208'
							},
							name: 'E',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 745,
								char_end: 746,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077234'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1954183869'
							},
							source: 'infected individuals',
							grounding: [
								{
									grounding_text: 'exposed individuals',
									grounding_id: 'cemo:exposed_individuals',
									source: [],
									score: 0.8635380864143372,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.077266'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 663,
								char_end: 683,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077234'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 1.0000001192092896,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077246'
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
						id: 'E:1429249585'
					},
					names: [
						{
							id: {
								id: 'T:-459741036'
							},
							name: 'I',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 684,
								char_end: 685,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077300'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-294082052'
							},
							value: {
								source: 'zero',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 706,
									char_end: 710,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077300'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077314'
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
						id: 'E:1973701948'
					},
					names: [
						{
							id: {
								id: 'T:-459741036'
							},
							name: 'I',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 684,
								char_end: 685,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077372'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-294082052'
							},
							source: 'zero',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 706,
								char_end: 710,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077372'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077384'
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
						id: 'E:-91995909'
					},
					names: [
						{
							id: {
								id: 'T:-459741036'
							},
							name: 'I',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 684,
								char_end: 685,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077438'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-294082052'
							},
							source: 'zero',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 706,
								char_end: 710,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077438'
							}
						}
					],
					value_specs: [
						{
							id: {
								id: 'T:-294082052'
							},
							value: {
								source: 'zero',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 706,
									char_end: 710,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077438'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.994598925113678,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077450'
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
						id: 'E:1490776763'
					},
					names: [
						{
							id: {
								id: 'T:1041165587'
							},
							name: 'D',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 688,
								char_end: 689,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077531'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-294082052'
							},
							source: 'zero',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 706,
								char_end: 710,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077531'
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
				amr_element_id: 'Infected',
				payload: {
					id: {
						id: 'E:1283524308'
					},
					names: [
						{
							id: {
								id: 'T:-1931326589'
							},
							name: 'H',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 739,
								char_end: 740,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077602'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1954183869'
							},
							source: 'infected individuals',
							grounding: [
								{
									grounding_text: 'exposed individuals',
									grounding_id: 'cemo:exposed_individuals',
									source: [],
									score: 0.8635380864143372,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.077634'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 663,
								char_end: 683,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077602'
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
						id: 'E:170114179'
					},
					names: [
						{
							id: {
								id: 'T:1553496250'
							},
							name: 'A',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 692,
								char_end: 693,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077669'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-294082052'
							},
							source: 'zero',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 706,
								char_end: 710,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077669'
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
						id: 'E:661175780'
					},
					names: [
						{
							id: {
								id: 'T:-17564791'
							},
							name: 'Sigma',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 73,
								char_end: 78,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077810'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:589830780'
							},
							source: 'IDART subsystem',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 56,
								char_end: 71,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077810'
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
						id: 'E:1353177963'
					},
					names: [
						{
							id: {
								id: 'T:1516847754'
							},
							name: 'r5',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 88,
								char_end: 90,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077914'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-176712960'
							},
							source: 'zeta',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 19,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077914'
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
						id: 'R:1030052105'
					},
					names: [
						{
							id: {
								id: 'T:-1366187241'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 249,
								char_end: 250,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077982'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-309581222'
							},
							source: 'final value',
							grounding: [
								{
									grounding_text: 'maximum value',
									grounding_id: 'apollosv:00000433',
									source: [],
									score: 0.8341392874717712,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.078017'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 237,
								char_end: 248,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.077982'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.077995'
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
						id: 'R:-1576717640'
					},
					names: [
						{
							id: {
								id: 'T:150472627'
							},
							name: 'S',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 172,
								char_end: 173,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078051'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1538475535'
							},
							source: 'equilibrium',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 160,
								char_end: 171,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078051'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.078063'
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
						id: 'R:-503053756'
					},
					names: [
						{
							id: {
								id: 'T:313507350'
							},
							name: 'E(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 143,
								char_end: 147,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078128'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:242647070'
							},
							source: 'infinity',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 134,
								char_end: 142,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078128'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'LolC/E',
							grounding_id: 'vo:0010921',
							source: [],
							score: 0.7558534741401672,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.078139'
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
						id: 'R:1716242736'
					},
					names: [
						{
							id: {
								id: 'T:1868161912'
							},
							name: 'S(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 88,
								char_end: 92,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078194'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1370565746'
							},
							source: 'infinity',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 79,
								char_end: 87,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078194'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.812811553478241,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.078206'
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
						id: 'R:1765320258'
					},
					names: [
						{
							id: {
								id: 'T:-863633939'
							},
							name: 'H(t)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 114,
								char_end: 118,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078261'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-1490044870'
							},
							source: 'infinity',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 105,
								char_end: 113,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078261'
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
						id: 'E:-542191878'
					},
					names: [
						{
							id: {
								id: 'T:-1101209064'
							},
							name: 'R(0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 179,
								char_end: 183,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078337'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1174345978'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 186,
									char_end: 187,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078337'
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
						id: 'E:1263225135'
					},
					names: [
						{
							id: {
								id: 'T:1090925148'
							},
							name: 'A(0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 106,
								char_end: 110,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078395'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1161637923'
							},
							value: {
								source: '0 0 0 0',
								grounding: [
									{
										grounding_text: 'SG33-VP2/5',
										grounding_id: 'vo:0004706',
										source: [],
										score: 0.8210175633430481,
										provenance: {
											method: 'SKEMA-TR-Embedding',
											timestamp: '2023-06-22 23:48:08.078438'
										}
									}
								],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 113,
									char_end: 120,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078395'
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
						id: 'E:882617620'
					},
					names: [
						{
							id: {
								id: 'T:1651606117'
							},
							name: 'R(0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 139,
								char_end: 143,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078475'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1354695866'
							},
							value: {
								source: '0',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 146,
									char_end: 147,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078475'
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
						id: 'E:-1775493576'
					},
					names: [
						{
							id: {
								id: 'T:1407041549'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 455,
								char_end: 458,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078560'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1694865735'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 480,
									char_end: 483,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078560'
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
						id: 'E:-1396396525'
					},
					names: [
						{
							id: {
								id: 'T:1407041549'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 455,
								char_end: 458,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078624'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:427918785'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 483,
									char_end: 484,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078624'
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
						id: 'E:86284981'
					},
					names: [
						{
							id: {
								id: 'T:1407041549'
							},
							name: 'CFR',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 455,
								char_end: 458,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078683'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1694865735'
							},
							value: {
								source: '7.2',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 480,
									char_end: 483,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078683'
							}
						},
						{
							id: {
								id: 'T:427918785'
							},
							value: {
								source: '%',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 483,
									char_end: 484,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078683'
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
						id: 'R:303076049'
					},
					names: [
						{
							id: {
								id: 'T:-1727791013'
							},
							name: 'f',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 573,
								char_end: 574,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078758'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-947156097'
							},
							source: 'Figure 4',
							grounding: [
								{
									grounding_text: 'CEL-170/4',
									grounding_id: 'vo:0011326',
									source: [],
									score: 0.8024071455001831,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.078789'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 563,
								char_end: 571,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.078758'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'rFP-RPV-H/F',
							grounding_id: 'vo:0004752',
							source: [],
							score: 1.0,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.078770'
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
						id: 'R:-781706139'
					},
					names: [
						{
							id: {
								id: 'T:-1880791586'
							},
							name: 'SIDARTHE',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 112,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079037'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:578540955'
							},
							source: 'calibrated',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 93,
								char_end: 103,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079037'
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
						id: 'R:-707640004'
					},
					names: [
						{
							id: {
								id: 'T:898560301'
							},
							name: 'alpha',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 82,
								char_end: 87,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079173'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1419526944'
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
										timestamp: '2023-06-22 23:48:08.079204'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 89,
								char_end: 93,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079173'
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
						id: 'E:-1141763443'
					},
					names: [
						{
							id: {
								id: 'T:-853889885'
							},
							name: 'r5',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 88,
								char_end: 90,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079335'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:258667011'
							},
							source: 'zeta',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 15,
								char_end: 19,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079335'
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
						id: 'E:-1498045512'
					},
					names: [
						{
							id: {
								id: 'T:-839842415'
							},
							name: 'G(s)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 50,
								char_end: 54,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079426'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-341118712'
							},
							source: 'transfer function from u to yS in Figure',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 4,
								char_end: 44,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079426'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Fluviral S/F',
							grounding_id: 'vo:0000866',
							source: [],
							score: 0.818644642829895,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.079438'
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
						id: 'R:784224106'
					},
					names: [
						{
							id: {
								id: 'T:-873485113'
							},
							name: 'G(0)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 154,
								char_end: 158,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079493'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:439754017'
							},
							source: 'static gain',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 142,
								char_end: 153,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079493'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Education-Subcommittee G',
							grounding_id: 'ncit:C19510',
							source: [],
							score: 0.7553320527076721,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.079505'
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
						id: 'R:1750766702'
					},
					names: [
						{
							id: {
								id: 'T:1831024053'
							},
							name: 'iff',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 329,
								char_end: 332,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079558'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1055569225'
							},
							source: 'lefthand plane)',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 313,
								char_end: 328,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079558'
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
						id: 'R:612777737'
					},
					names: [
						{
							id: {
								id: 'T:-1852834191'
							},
							name: 'G(s)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 247,
								char_end: 251,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079624'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1851322257'
							},
							source: 'positive system',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 231,
								char_end: 246,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079624'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Fluviral S/F',
							grounding_id: 'vo:0000866',
							source: [],
							score: 0.818644642829895,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.079636'
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
						id: 'E:1298048515'
					},
					names: [
						{
							id: {
								id: 'T:867254926'
							},
							name: 'S*',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 429,
								char_end: 431,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079691'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:-1105992771'
							},
							value: {
								source: '1',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 434,
									char_end: 435,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079691'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Meruvax I',
							grounding_id: 'vo:0003109',
							source: [],
							score: 0.7847759127616882,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.079703'
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
						id: 'R:-381869604'
					},
					names: [
						{
							id: {
								id: 'T:-1326143750'
							},
							name: 'G(s)',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 728,
								char_end: 732,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079750'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:169424737'
							},
							source: 'transfer function',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 710,
								char_end: 727,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079750'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'Fluviral S/F',
							grounding_id: 'vo:0000866',
							source: [],
							score: 0.818644642829895,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.079761'
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
						id: 'E:-1070740482'
					},
					names: [
						{
							id: {
								id: 'T:1991672203'
							},
							name: 'R0',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 671,
								char_end: 673,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079816'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:420069150'
							},
							source: 'Hinfinity norm of the transfer function G(s',
							grounding: [
								{
									grounding_text: 'Semantic_Type',
									grounding_id: 'ncit:P106',
									source: [],
									score: 0.7755292057991028,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.079849'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 688,
								char_end: 731,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079816'
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
						id: 'R:-657315310'
					},
					names: [
						{
							id: {
								id: 'T:-2061688367'
							},
							name: 'SIDARTHE',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 104,
								char_end: 112,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079960'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1987978817'
							},
							source: 'calibrated',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 93,
								char_end: 103,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.079960'
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
						id: 'E:-1931270365'
					},
					names: [
						{
							id: {
								id: 'T:-1190704443'
							},
							name: 'Sanit',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 40,
								char_end: 45,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080102'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:1961326094'
							},
							source: 'Tamponi Solo',
							grounding: [
								{
									grounding_text: 'Barrio Libre',
									grounding_id: 'ncit:C44236',
									source: [],
									score: 0.7714061737060547,
									provenance: {
										method: 'SKEMA-TR-Embedding',
										timestamp: '2023-06-22 23:48:08.080137'
									}
								}
							],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 49,
								char_end: 61,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080102'
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
						id: 'R:1568652268'
					},
					names: [
						{
							id: {
								id: 'T:-2142962137'
							},
							name: 'Chronology of main steps and legal acts taken by the Italian Government for the containment of the COVID-19 epidemiological emergency',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 119,
								char_end: 252,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080270'
							}
						}
					],
					descriptions: [],
					value_specs: [
						{
							id: {
								id: 'T:1092367872'
							},
							value: {
								source: '10',
								grounding: [],
								extraction_source: {
									page: 0,
									block: 0,
									char_start: 113,
									char_end: 115,
									document_reference: {
										id: 'sidarthe.pdf'
									}
								}
							},
							units: null,
							type: null,
							bounds: null,
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080270'
							}
						}
					],
					groundings: [
						{
							grounding_text: 'Component of the National Institutes of Health',
							grounding_id: 'ncit:C20172',
							source: [],
							score: 0.8479864597320557,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.080282'
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
						id: 'R:145606444'
					},
					names: [
						{
							id: {
								id: 'T:838720632'
							},
							name: 'AIDS',
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 118,
								char_end: 122,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080457'
							}
						}
					],
					descriptions: [
						{
							id: {
								id: 'T:-234053045'
							},
							source: 'science',
							grounding: [],
							extraction_source: {
								page: 0,
								block: 0,
								char_start: 110,
								char_end: 117,
								document_reference: {
									id: 'sidarthe.pdf'
								}
							},
							provenance: {
								method: 'Skema TR Pipeline rules',
								timestamp: '2023-06-22 23:48:08.080457'
							}
						}
					],
					value_specs: [],
					groundings: [
						{
							grounding_text: 'AIDS Population',
							grounding_id: 'ncit:C18620',
							source: [],
							score: 0.7728735208511353,
							provenance: {
								method: 'SKEMA-TR-Embedding',
								timestamp: '2023-06-22 23:48:08.080468'
							}
						}
					],
					data_columns: null
				}
			},
			{
				type: 'scenario_context',
				amr_element_id: null,
				payload: {
					id: {
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'R:41741717'
						}
					],
					location: null,
					time: {
						datetime: 'December 2019',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061705'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'R:41741717'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061726'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'R:41741717'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061742'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:41741717'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061757'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'E:-1572834827'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061970'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1572834827'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061985'
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
						id: '-5741660826627597942'
					},
					extractions: [
						{
							id: 'E:-1572834827'
						}
					],
					location: {
						location: 'northern Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.061999'
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
						id: '3064278613871743271'
					},
					extractions: [
						{
							id: 'E:-1572834827'
						}
					],
					location: {
						location: 'Lombardy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062013'
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
						id: '7689853742937516830'
					},
					extractions: [
						{
							id: 'E:-1572834827'
						}
					],
					location: {
						location: 'Lodi',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062026'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'E:1626461541'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062111'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1626461541'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062125'
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
						id: '-5741660826627597942'
					},
					extractions: [
						{
							id: 'E:1626461541'
						}
					],
					location: {
						location: 'northern Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062139'
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
						id: '3064278613871743271'
					},
					extractions: [
						{
							id: 'E:1626461541'
						}
					],
					location: {
						location: 'Lombardy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062153'
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
						id: '7689853742937516830'
					},
					extractions: [
						{
							id: 'E:1626461541'
						}
					],
					location: {
						location: 'Lodi',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062166'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'E:-642689719'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062248'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-642689719'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062262'
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
						id: '-5741660826627597942'
					},
					extractions: [
						{
							id: 'E:-642689719'
						}
					],
					location: {
						location: 'northern Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062276'
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
						id: '3064278613871743271'
					},
					extractions: [
						{
							id: 'E:-642689719'
						}
					],
					location: {
						location: 'Lombardy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062289'
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
						id: '7689853742937516830'
					},
					extractions: [
						{
							id: 'E:-642689719'
						}
					],
					location: {
						location: 'Lodi',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062311'
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
						id: '-5741660826627597942'
					},
					extractions: [
						{
							id: 'E:529212984'
						}
					],
					location: {
						location: 'northern Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062452'
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
						id: '3064278613871743271'
					},
					extractions: [
						{
							id: 'E:529212984'
						}
					],
					location: {
						location: 'Lombardy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062466'
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
						id: '7689853742937516830'
					},
					extractions: [
						{
							id: 'E:529212984'
						}
					],
					location: {
						location: 'Lodi',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062479'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:529212984'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.062493'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:1515895266'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.064807'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1805061264'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.064886'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1377266171'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.064962'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:-69688551'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065038'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-971223536'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065117'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:1910908209'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065223'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1310588745'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065300'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1912456373'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065376'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:565093588'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065494'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1867668878'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065570'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-983492190'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065664'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1996931464'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065733'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1072365935'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065812'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:155764309'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065882'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-2021602221'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.065956'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'R:516434346'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066028'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:655813297'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066097'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:52866099'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066174'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:1275271689'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066274'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:309602204'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066354'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:309602204'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066368'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1403320282'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066436'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1403320282'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066449'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1814788090'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066519'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1814788090'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066533'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1773271836'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066613'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1773271836'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066627'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-448855073'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066707'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-448855073'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066720'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-471656782'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066826'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-471656782'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066839'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1085386362'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066908'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1085386362'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.066921'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:701994054'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067001'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:701994054'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067015'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:656388145'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067089'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:656388145'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067102'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1037765850'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067197'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1037765850'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067211'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-150819206'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067289'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-150819206'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067303'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1839186941'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067380'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1839186941'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067393'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:2035882106'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067472'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:2035882106'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067486'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1900111227'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067556'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1900111227'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067571'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:731098284'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067639'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:731098284'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067653'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-869515938'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067708'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-869515938'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067721'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1543854713'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067786'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1543854713'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067803'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1015596706'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067887'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1015596706'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067901'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:982082149'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067981'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:982082149'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.067995'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:639958349'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068102'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:639958349'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068115'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1141093632'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068197'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1141093632'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068212'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-337625904'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068289'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-337625904'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068303'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:842581186'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068373'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:842581186'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068390'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:59384549'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068471'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:59384549'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068486'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1807936182'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068566'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1807936182'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068582'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1144064762'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068661'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1144064762'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068676'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-162125520'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068748'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-162125520'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068763'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1611099537'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068843'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1611099537'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068857'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1749692150'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068945'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1749692150'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.068959'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1476771929'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069054'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1476771929'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069068'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-537166879'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069149'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-537166879'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069163'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-2110254694'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069246'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-2110254694'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069260'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:486120709'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069340'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:486120709'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069354'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1370870177'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069432'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:1370870177'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069447'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1898900327'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069514'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1898900327'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069527'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1064027010'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069612'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:-1064027010'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069635'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:867390604'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069708'
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
						id: '1375867976442204267'
					},
					extractions: [
						{
							id: 'E:867390604'
						}
					],
					location: {
						location: 'D - A - R - T - H - E.',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069722'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:1412329811'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.069981'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'R:1693417853'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.071769'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:793459689'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.071845'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:1964966967'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.071926'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:176422678'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.072019'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:-365122890'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.072090'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:502104994'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.072166'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'E:-99107917'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.072282'
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
						id: '-2138123413839607498'
					},
					extractions: [
						{
							id: 'R:-190532779'
						}
					],
					location: {
						location: 'Intensive Care',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.072369'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:-1459106808'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.073588'
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
						id: '824753694743662471'
					},
					extractions: [
						{
							id: 'R:-1459106808'
						}
					],
					location: {
						location: 'Protezione Civile',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.073602'
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
						id: '869711368414832393'
					},
					extractions: [
						{
							id: 'R:-1156681184'
						}
					],
					location: {
						location: 'San Matteo Pavia Task Force',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074278'
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
						id: '-3666550398413641930'
					},
					extractions: [
						{
							id: 'R:-1156681184'
						}
					],
					location: {
						location: 'ID',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074291'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-394710046'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074422'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'E:-394710046'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074435'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'E:-394710046'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074450'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'E:-394710046'
						}
					],
					location: null,
					time: {
						datetime: 'December 2019',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074464'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:-1235282259'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074542'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'R:-1235282259'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074555'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'R:-1235282259'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074569'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'R:-1235282259'
						}
					],
					location: null,
					time: {
						datetime: 'December 2019',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074582'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:1492964080'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074660'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'E:1492964080'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074674'
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
						id: '-5059565402432922833'
					},
					extractions: [
						{
							id: 'E:1492964080'
						}
					],
					location: {
						location: 'Asia',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074687'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'E:1492964080'
						}
					],
					location: null,
					time: {
						datetime: 'December 2019',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.074700'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'R:-26505195'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076087'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:-685281417'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076199'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:-1423274836'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076271'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:1002363864'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076347'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:1384246621'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076423'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:335068517'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076498'
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
						id: '4494094035577600188'
					},
					extractions: [
						{
							id: 'E:-307493232'
						}
					],
					location: {
						location: 'sum of the states',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.076575'
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
						id: '-3666550398413641930'
					},
					extractions: [
						{
							id: 'R:-657315310'
						}
					],
					location: {
						location: 'ID',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080025'
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
						id: '-4111314428562428742'
					},
					extractions: [
						{
							id: 'R:-657315310'
						}
					],
					location: {
						location: 'P.C',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080039'
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
						id: '-2074929421536569874'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: {
						location: 'Lancet',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080173'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080187'
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
						id: '-8482579264735037989'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: {
						location: 'ANSA',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080200'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080214'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: null,
					time: {
						datetime: '26 February 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080228'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: null,
					time: {
						datetime: '16 March 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080243'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'E:-1931270365'
						}
					],
					location: null,
					time: {
						datetime: '12 March 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080257'
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
						id: '-8413777610933145554'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: {
						location: 'Italy',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080329'
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
						id: '-8482579264735037989'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: {
						location: 'ANSA',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080343'
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
						id: '7669941226990414426'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: {
						location: 'China',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080356'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: null,
					time: {
						datetime: '26 February 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080371'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: null,
					time: {
						datetime: '16 March 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080385'
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
						id: '-7205018114114334462'
					},
					extractions: [
						{
							id: 'R:1568652268'
						}
					],
					location: null,
					time: {
						datetime: '12 March 2020',
						start_datetime: null,
						end_datetime: null,
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080399'
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
						id: '-1644472984303587271'
					},
					extractions: [
						{
							id: 'R:145606444'
						}
					],
					location: {
						location: 'Cell Research',
						provenance: {
							method: 'SKEMA-TR-Context-1.0',
							timestamp: '2023-06-22 23:48:08.080529'
						},
						grounding: null,
						extraction_source: null
					},
					time: null
				}
			},
			{
				type: 'document_collection',
				amr_element_id: null,
				payload: {
					documents: [
						{
							id: {
								id: 'sidarthe.pdf'
							},
							source_file: 'sidarthe.pdf',
							doi: ''
						}
					]
				}
			}
		]
	}
};
