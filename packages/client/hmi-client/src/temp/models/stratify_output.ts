// @ts-nocheck
/* eslint-disable */
import { Model } from '@/types/Types';

export const stratify_output: Model = {
	name: 'SIR Model + Two City Stratification Model',
	semantics: {
		span: [
			{
				map: [
					['S_Rgn_1', 'S'],
					['I_Rgn_1', 'I'],
					['R_Rgn_1', 'R'],
					['S_Rgn_2', 'S'],
					['I_Rgn_2', 'I'],
					['R_Rgn_2', 'R'],
					['rec_Rgn1_dis', 'rec'],
					['inf_Rgn1_inf', 'inf'],
					['rec_Rgn2_dis', 'rec'],
					['inf_Rgn2_inf', 'inf'],
					['S_strata_travel_1_2', 'S_strata'],
					['I_strata_travel_1_2', 'I_strata'],
					['R_strata_travel_1_2', 'R_strata'],
					['S_strata_travel_2_1', 'S_strata'],
					['I_strata_travel_2_1', 'I_strata'],
					['R_strata_travel_2_1', 'R_strata']
				],
				system: {
					name: 'SIR Model',
					semantics: {
						typing: {
							map: [
								['S', 'Pop'],
								['I', 'Pop'],
								['R', 'Pop'],
								['inf', 'Infect'],
								['rec', 'Disease'],
								['S_strata', 'Strata'],
								['I_strata', 'Strata'],
								['R_strata', 'Strata']
							],
							system: {
								name: 'Ontology Model w/ Pop and Vax States',
								semantics: {},
								model_version: '0.1',
								model: {
									transitions: [
										{
											output: ['Pop', 'Pop'],
											properties: {
												name: 'Infect',
												description:
													'2-to-2 interaction that represents infectious contact between two human individuals'
											},
											id: 'Infect',
											input: ['Pop', 'Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Disease',
												description:
													'1-to-1 interaction that represents a change in th edisease status of a human individual.'
											},
											id: 'Disease',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Strata',
												description:
													'1-to-1 interaction that represents a change in the demographic division of a human individual.'
											},
											id: 'Strata',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Vaccinate',
												description:
													'2-to-1 interaction that represents an human individual receiving a vaccine dose.'
											},
											id: 'Vaccinate',
											input: ['Pop', 'Vaccine']
										},
										{
											output: ['Vaccine'],
											properties: {
												name: 'Produce Vaccine',
												description:
													'0-to-1 interaction that represents the production of a single vaccine dose.'
											},
											id: 'Produce_Vaccine',
											input: []
										}
									],
									states: [
										{
											name: 'Pop',
											id: 'Pop',
											description: 'Compartment of individuals in a human population'
										},
										{
											name: 'Vaccine',
											id: 'Vaccine',
											description:
												'Compartment of vaccine doses available for use in a vaccination campaign'
										}
									]
								},
								metadata: {},
								schema:
									'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
								description:
									'Ontology Model w/ Population and Vaccine-Unit States. File created by Patrick based ontology created by Nelson'
							}
						}
					},
					model_version: '0.1',
					model: {
						transitions: [
							{
								output: ['I', 'I'],
								properties: {
									name: 'Infection',
									description: 'Infective interaction between individuals'
								},
								id: 'inf',
								input: ['S', 'I']
							},
							{
								output: ['R'],
								properties: {
									name: 'Recovery',
									description: 'Recovery interaction of a infected individual'
								},
								id: 'rec',
								input: ['I']
							},
							{
								output: ['S'],
								properties: {
									name: 'S_strata'
								},
								id: 'S_strata',
								input: ['S']
							},
							{
								output: ['I'],
								properties: {
									name: 'I_strata'
								},
								id: 'I_strata',
								input: ['I']
							},
							{
								output: ['R'],
								properties: {
									name: 'R_strata'
								},
								id: 'R_strata',
								input: ['R']
							}
						],
						states: [
							{
								name: 'Susceptible',
								id: 'S',
								grounding: {
									identifiers: {
										ido: '0000514'
									}
								},
								description:
									'Number of individuals relative to the total population that are susceptible to a disease infection'
							},
							{
								name: 'Infected',
								id: 'I',
								grounding: {
									identifiers: {
										ido: '0000511'
									}
								},
								description:
									'Number of individuals relative to the total population that are infected by a disease'
							},
							{
								name: 'Recovered',
								id: 'R',
								grounding: {
									identifiers: {
										ido: '0000592'
									}
								},
								description:
									'Number of individuals relative to the total population that have recovered from a disease infection'
							}
						]
					},
					metadata: {},
					schema:
						'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
					description:
						'Typed SIR model created by Nelson, derived from the one by Ben, Micah, Brandon'
				}
			},
			{
				map: [
					['S_Rgn_1', 'Rgn_1'],
					['I_Rgn_1', 'Rgn_1'],
					['R_Rgn_1', 'Rgn_1'],
					['S_Rgn_2', 'Rgn_2'],
					['I_Rgn_2', 'Rgn_2'],
					['R_Rgn_2', 'Rgn_2'],
					['rec_Rgn1_dis', 'Rgn1_dis'],
					['inf_Rgn1_inf', 'Rgn1_inf'],
					['rec_Rgn2_dis', 'Rgn2_dis'],
					['inf_Rgn2_inf', 'Rgn2_inf'],
					['S_strata_travel_1_2', 'travel_1_2'],
					['I_strata_travel_1_2', 'travel_1_2'],
					['R_strata_travel_1_2', 'travel_1_2'],
					['S_strata_travel_2_1', 'travel_2_1'],
					['I_strata_travel_2_1', 'travel_2_1'],
					['R_strata_travel_2_1', 'travel_2_1']
				],
				system: {
					name: 'Two City Stratification Model',
					semantics: {
						typing: {
							map: [
								['Rgn_1', 'Pop'],
								['Rgn_2', 'Pop'],
								['Rgn1_dis', 'Disease'],
								['Rgn1_inf', 'Infect'],
								['Rgn2_dis', 'Disease'],
								['Rgn2_inf', 'Infect'],
								['travel_1_2', 'Strata'],
								['travel_2_1', 'Strata']
							],
							system: {
								name: 'Ontology Model w/ Pop and Vax States',
								semantics: {},
								model_version: '0.1',
								model: {
									transitions: [
										{
											output: ['Pop', 'Pop'],
											properties: {
												name: 'Infect',
												description:
													'2-to-2 interaction that represents infectious contact between two human individuals'
											},
											id: 'Infect',
											input: ['Pop', 'Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Disease',
												description:
													'1-to-1 interaction that represents a change in th edisease status of a human individual.'
											},
											id: 'Disease',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Strata',
												description:
													'1-to-1 interaction that represents a change in the demographic division of a human individual.'
											},
											id: 'Strata',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Vaccinate',
												description:
													'2-to-1 interaction that represents an human individual receiving a vaccine dose.'
											},
											id: 'Vaccinate',
											input: ['Pop', 'Vaccine']
										},
										{
											output: ['Vaccine'],
											properties: {
												name: 'Produce Vaccine',
												description:
													'0-to-1 interaction that represents the production of a single vaccine dose.'
											},
											id: 'Produce_Vaccine',
											input: []
										}
									],
									states: [
										{
											name: 'Pop',
											id: 'Pop',
											description: 'Compartment of individuals in a human population'
										},
										{
											name: 'Vaccine',
											id: 'Vaccine',
											description:
												'Compartment of vaccine doses available for use in a vaccination campaign'
										}
									]
								},
								metadata: {},
								schema:
									'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
								description:
									'Ontology Model w/ Population and Vaccine-Unit States. File created by Patrick based ontology created by Nelson'
							}
						},
						ode: {}
					},
					model_version: '0.1',
					model: {
						transitions: [
							{
								output: ['Rgn_1'],
								properties: {
									name: 'Rgn1_disease'
								},
								id: 'Rgn1_dis',
								input: ['Rgn_1']
							},
							{
								output: ['Rgn_1', 'Rgn_1'],
								properties: {
									name: 'Rgn1_infection'
								},
								id: 'Rgn1_inf',
								input: ['Rgn_1', 'Rgn_1']
							},
							{
								output: ['Rgn_2'],
								properties: {
									name: 'Rgn2_disease'
								},
								id: 'Rgn2_dis',
								input: ['Rgn_2']
							},
							{
								output: ['Rgn_2', 'Rgn_2'],
								properties: {
									name: 'Rgn2_infection'
								},
								id: 'Rgn2_inf',
								input: ['Rgn_2', 'Rgn_2']
							},
							{
								output: ['Rgn_2'],
								properties: {
									name: 'Traveling 1 -> 2',
									description: 'Individuals traveling from region 1 to region 2'
								},
								id: 'travel_1_2',
								input: ['Rgn_1']
							},
							{
								output: ['Rgn_1'],
								properties: {
									name: 'Traveling 2 -> 1',
									description: 'Individuals traveling from region 2 to region 1'
								},
								id: 'travel_2_1',
								input: ['Rgn_2']
							}
						],
						states: [
							{
								name: 'Region 1',
								id: 'Rgn_1',
								description:
									'Number of individuals physically located in region 1 relative to the total population'
							},
							{
								name: 'Region 2',
								id: 'Rgn_2',
								description:
									'Number of individuals physically located in region 2 relative to the total population'
							}
						]
					},
					metadata: {},
					schema:
						'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
					description: 'Stratification spatially between two cities model created by Micah'
				}
			}
		],
		typing: {
			map: [
				['S_Rgn_1', 'Pop'],
				['I_Rgn_1', 'Pop'],
				['R_Rgn_1', 'Pop'],
				['S_Rgn_2', 'Pop'],
				['I_Rgn_2', 'Pop'],
				['R_Rgn_2', 'Pop'],
				['rec_Rgn1_dis', 'Disease'],
				['inf_Rgn1_inf', 'Infect'],
				['rec_Rgn2_dis', 'Disease'],
				['inf_Rgn2_inf', 'Infect'],
				['S_strata_travel_1_2', 'Strata'],
				['I_strata_travel_1_2', 'Strata'],
				['R_strata_travel_1_2', 'Strata'],
				['S_strata_travel_2_1', 'Strata'],
				['I_strata_travel_2_1', 'Strata'],
				['R_strata_travel_2_1', 'Strata']
			],
			system: {
				name: 'Ontology Model w/ Pop and Vax States',
				semantics: {},
				model_version: '0.1',
				model: {
					transitions: [
						{
							output: ['Pop', 'Pop'],
							properties: {
								name: 'Infect',
								description:
									'2-to-2 interaction that represents infectious contact between two human individuals'
							},
							id: 'Infect',
							input: ['Pop', 'Pop']
						},
						{
							output: ['Pop'],
							properties: {
								name: 'Disease',
								description:
									'1-to-1 interaction that represents a change in th edisease status of a human individual.'
							},
							id: 'Disease',
							input: ['Pop']
						},
						{
							output: ['Pop'],
							properties: {
								name: 'Strata',
								description:
									'1-to-1 interaction that represents a change in the demographic division of a human individual.'
							},
							id: 'Strata',
							input: ['Pop']
						},
						{
							output: ['Pop'],
							properties: {
								name: 'Vaccinate',
								description:
									'2-to-1 interaction that represents an human individual receiving a vaccine dose.'
							},
							id: 'Vaccinate',
							input: ['Pop', 'Vaccine']
						},
						{
							output: ['Vaccine'],
							properties: {
								name: 'Produce Vaccine',
								description:
									'0-to-1 interaction that represents the production of a single vaccine dose.'
							},
							id: 'Produce_Vaccine',
							input: []
						}
					],
					states: [
						{
							name: 'Pop',
							id: 'Pop',
							description: 'Compartment of individuals in a human population'
						},
						{
							name: 'Vaccine',
							id: 'Vaccine',
							description:
								'Compartment of vaccine doses available for use in a vaccination campaign'
						}
					]
				},
				metadata: {},
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
				description:
					'Ontology Model w/ Population and Vaccine-Unit States. File created by Patrick based ontology created by Nelson'
			}
		}
	},
	model_version: '0.1',
	model: {
		transitions: [
			{
				output: ['R_Rgn_1'],
				id: 'rec_Rgn1_dis',
				input: ['I_Rgn_1']
			},
			{
				output: ['I_Rgn_1', 'I_Rgn_1'],
				id: 'inf_Rgn1_inf',
				input: ['S_Rgn_1', 'I_Rgn_1']
			},
			{
				output: ['R_Rgn_2'],
				id: 'rec_Rgn2_dis',
				input: ['I_Rgn_2']
			},
			{
				output: ['I_Rgn_2', 'I_Rgn_2'],
				id: 'inf_Rgn2_inf',
				input: ['S_Rgn_2', 'I_Rgn_2']
			},
			{
				output: ['S_Rgn_2'],
				id: 'S_strata_travel_1_2',
				input: ['S_Rgn_1']
			},
			{
				output: ['I_Rgn_2'],
				id: 'I_strata_travel_1_2',
				input: ['I_Rgn_1']
			},
			{
				output: ['R_Rgn_2'],
				id: 'R_strata_travel_1_2',
				input: ['R_Rgn_1']
			},
			{
				output: ['S_Rgn_1'],
				id: 'S_strata_travel_2_1',
				input: ['S_Rgn_2']
			},
			{
				output: ['I_Rgn_1'],
				id: 'I_strata_travel_2_1',
				input: ['I_Rgn_2']
			},
			{
				output: ['R_Rgn_1'],
				id: 'R_strata_travel_2_1',
				input: ['R_Rgn_2']
			}
		],
		states: [
			{
				id: 'S_Rgn_1'
			},
			{
				id: 'I_Rgn_1'
			},
			{
				id: 'R_Rgn_1'
			},
			{
				id: 'S_Rgn_2'
			},
			{
				id: 'I_Rgn_2'
			},
			{
				id: 'R_Rgn_2'
			}
		]
	},
	description:
		'Typed SIR model created by Nelson, derived from the one by Ben, Micah, Brandon ; Stratification spatially between two cities model created by Micah',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json'
};
