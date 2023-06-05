import { Model } from '@/types/Types';

export const sir: Model = {
	name: 'SIR Model',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
	description: 'SIR model created by Ben, Micah, Brandon',
	model_version: '0.1',
	model: {
		states: [
			{
				id: 'S',
				name: 'Susceptible',
				grounding: {
					identifiers: {
						ido: '0000514'
					}
				}
			},
			{
				id: 'I',
				name: 'Infected',
				grounding: {
					identifiers: {
						ido: '0000511'
					}
				}
			},
			{
				id: 'R',
				name: 'Recovered',
				grounding: {
					identifiers: {
						ido: '0000592'
					}
				}
			}
		],
		transitions: [
			{
				id: 'inf',
				input: ['S', 'I'],
				output: ['I', 'I'],
				properties: {
					name: 'Infection'
				}
			},
			{
				id: 'rec',
				input: ['I'],
				output: ['R'],
				properties: {
					name: 'Recovery'
				}
			}
		]
	},
	semantics: {
		ode: {
			rates: [
				{
					target: 'inf',
					expression: 'S*I*beta',
					expression_mathml: '<apply><times/><ci>S</ci><ci>I</ci><ci>beta</ci></apply>'
				},
				{
					target: 'rec',
					expression: 'I*gamma',
					expression_mathml: '<apply><times/><ci>I</ci><ci>gamma</ci></apply>'
				}
			],
			initials: [
				{
					target: 'S',
					expression: 'S0',
					expression_mathml: '<ci>S0</ci>'
				},
				{
					target: 'I',
					expression: 'I0',
					expression_mathml: '<ci>I0</ci>'
				},
				{
					target: 'R',
					expression: 'R0',
					expression_mathml: '<ci>R0</ci>'
				}
			],
			parameters: [
				{
					id: 'beta',
					description: 'infection rate',
					value: 0.027,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.026,
							maximum: 0.028
						}
					}
				},
				{
					id: 'gamma',
					description: 'recovery rate',
					grounding: {
						identifiers: {
							askemo: '0000013'
						}
					},
					value: 0.14,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.1,
							maximum: 0.18
						}
					}
				},
				{
					id: 'S0',
					description: 'Total susceptible population at timestep 0',
					value: 1000
				},
				{
					id: 'I0',
					description: 'Total infected population at timestep 0',
					value: 1
				},
				{
					id: 'R0',
					description: 'Total recovered population at timestep 0',
					value: 0
				}
			]
		}
	},
	metadata: {
		processed_at: 1682964953,
		processed_by: 'mit:process-node1',
		variable_statements: [
			{
				id: 'v0',
				variable: {
					id: 'v0',
					name: 'VE',
					metadata: [
						{
							type: 'text_annotation',
							value: ' Vaccine Effectiveness'
						},
						{
							type: 'text_annotation',
							value: ' Vaccine Effectiveness'
						}
					],
					dkg_groundings: [],
					column: [
						{
							id: '9-2',
							name: 'new_persons_vaccinated',
							dataset: {
								id: '9',
								name: 'usa-vaccinations.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-vaccinations.csv'
							}
						},
						{
							id: '9-3',
							name: 'cumulative_persons_vaccinated',
							dataset: {
								id: '9',
								name: 'usa-vaccinations.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-vaccinations.csv'
							}
						}
					],
					paper: {
						id: 'COVID-19 Vaccine Effectiveness by Product and Timing in New York State',
						file_directory: 'https://www.medrxiv.org/content/10.1101/2021.10.08.21264595v1',
						doi: '10.1101/2021.10.08.21264595'
					},
					equations: []
				},
				metadata: [],
				provenance: {
					method: 'MIT annotation',
					description: 'text, dataset, formula annotation (chunwei@mit.edu)'
				}
			}
		]
	}
};

export const typedSir: Model = {
	name: 'SIR Model',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
	description: 'Typed SIR model created by Nelson, derived from the one by Ben, Micah, Brandon',
	model_version: '0.1',
	model: {
		states: [
			{
				id: 'S',
				name: 'Susceptible',
				description:
					"Number of individuals relative to the total population that are 'susceptible' to a disease infection",
				grounding: {
					identifiers: {
						ido: '0000514'
					}
				}
			},
			{
				id: 'I',
				name: 'Infected',
				description:
					"Number of individuals relative to the total population that are 'infected' by a disease",
				grounding: {
					identifiers: {
						ido: '0000511'
					}
				}
			},
			{
				id: 'R',
				name: 'Recovered',
				description:
					"Number of individuals relative to the total population that have 'recovered' from a disease infection",
				grounding: {
					identifiers: {
						ido: '0000592'
					}
				}
			}
		],
		transitions: [
			{
				id: 'inf',
				input: ['S', 'I'],
				output: ['I', 'I'],
				properties: {
					name: 'Infection',
					description: 'Infective interaction between individuals'
				}
			},
			{
				id: 'rec',
				input: ['I'],
				output: ['R'],
				properties: {
					name: 'Recovery',
					description: 'Recovery interaction of a infected individual'
				}
			}
		]
	},
	semantics: {
		ode: {
			rates: [
				{
					target: 'inf',
					expression: 'S*I*beta',
					expression_mathml: '<apply><times/><ci>S</ci><ci>I</ci><ci>beta</ci></apply>'
				},
				{
					target: 'rec',
					expression: 'I*gamma',
					expression_mathml: '<apply><times/><ci>I</ci><ci>gamma</ci></apply>'
				}
			],
			initials: [
				{
					target: 'S',
					expression: 'S0',
					expression_mathml: '<ci>S0</ci>'
				},
				{
					target: 'I',
					expression: 'I0',
					expression_mathml: '<ci>I0</ci>'
				},
				{
					target: 'R',
					expression: 'R0',
					expression_mathml: '<ci>R0</ci>'
				}
			],
			parameters: [
				{
					id: 'beta',
					description: 'infection rate',
					value: 0.027,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.026,
							maximum: 0.028
						}
					}
				},
				{
					id: 'gamma',
					description: 'recovery rate',
					grounding: {
						identifiers: {
							askemo: '0000013'
						}
					},
					value: 0.14,
					distribution: {
						type: 'StandardUniform1',
						parameters: {
							minimum: 0.1,
							maximum: 0.18
						}
					}
				},
				{
					id: 'S0',
					description: 'Total susceptible population at timestep 0',
					value: 1000
				},
				{
					id: 'I0',
					description: 'Total infected population at timestep 0',
					value: 1
				},
				{
					id: 'R0',
					description: 'Total recovered population at timestep 0',
					value: 0
				}
			]
		},
		typing: {
			type_system: {
				states: [
					{
						id: 'Pop',
						name: 'Pop',
						description: 'Compartment of individuals in a human population'
					},
					{
						id: 'Vaccine',
						name: 'Vaccine',
						description: 'Compartment of vaccine doses available for use in a vaccination campaign'
					}
				],
				transitions: [
					{
						id: 'Infect',
						input: ['Pop', 'Pop'],
						output: ['Pop', 'Pop'],
						properties: {
							name: 'Infect',
							description:
								'2-to-2 interaction that represents infectious contact between two human individuals'
						}
					},
					{
						id: 'Disease',
						input: ['Pop'],
						output: ['Pop'],
						properties: {
							name: 'Disease',
							description:
								'1-to-1 interaction that represents a change in th edisease status of a human individual.'
						}
					},
					{
						id: 'Strata',
						input: ['Pop'],
						output: ['Pop'],
						properties: {
							name: 'Strata',
							description:
								'1-to-1 interaction that represents a change in the demographic division of a human individual.'
						}
					},
					{
						id: 'Vaccinate',
						input: ['Pop', 'Vaccine'],
						output: ['Pop'],
						properties: {
							name: 'Vaccinate',
							description:
								'2-to-1 interaction that represents an human individual receiving a vaccine dose.'
						}
					},
					{
						id: 'Produce_Vaccine',
						input: [],
						output: ['Vaccine'],
						properties: {
							name: 'Produce Vaccine',
							description:
								'0-to-1 interaction that represents the production of a single vaccine dose.'
						}
					}
				]
			},
			type_map: [
				['S', 'Pop'],
				['I', 'Pop'],
				['R', 'Pop'],
				['inf', 'Infect'],
				['rec', 'Disease']
			]
		}
	},
	metadata: {
		processed_at: 1682964953,
		processed_by: 'mit:process-node1',
		variable_statements: [
			{
				id: 'v0',
				variable: {
					id: 'v0',
					name: 'VE',
					metadata: [
						{
							type: 'text_annotation',
							value: ' Vaccine Effectiveness'
						},
						{
							type: 'text_annotation',
							value: ' Vaccine Effectiveness'
						}
					],
					dkg_groundings: [],
					column: [
						{
							id: '9-2',
							name: 'new_persons_vaccinated',
							dataset: {
								id: '9',
								name: 'usa-vaccinations.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-vaccinations.csv'
							}
						},
						{
							id: '9-3',
							name: 'cumulative_persons_vaccinated',
							dataset: {
								id: '9',
								name: 'usa-vaccinations.csv',
								metadata:
									'https://github.com/DARPA-ASKEM/program-milestones/blob/main/6-month-milestone/evaluation/scenario_3/ta_1/google-health-data/usa-vaccinations.csv'
							}
						}
					],
					paper: {
						id: 'COVID-19 Vaccine Effectiveness by Product and Timing in New York State',
						file_directory: 'https://www.medrxiv.org/content/10.1101/2021.10.08.21264595v1',
						doi: '10.1101/2021.10.08.21264595'
					},
					equations: []
				},
				metadata: [],
				provenance: {
					method: 'MIT annotation',
					description: 'text, dataset, formula annotation (chunwei@mit.edu)'
				}
			}
		]
	}
};
