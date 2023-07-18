const strat = {
	id: 'd549be6b-b276-430c-8762-6404aa72d785',
	timestamp: '2023-07-18 17:16:00',
	name: 'SIRs + Location-travel strata model',
	description: 'SIR model ; Location-travel strata model',
	username: null,
	model: {
		states: [
			{
				id: 'S_L1',
				name: 'S_L1',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			},
			{
				id: 'S_L2',
				name: 'S_L2',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			},
			{
				id: 'I_L1',
				name: 'I_L1',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			},
			{
				id: 'I_L2',
				name: 'I_L2',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			},
			{
				id: 'R_L1',
				name: 'R_L1',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			},
			{
				id: 'R_L2',
				name: 'R_L2',
				grounding: {
					identifiers: {},
					modifiers: {}
				}
			}
		],
		transitions: [
			{
				id: 't12SS_t12',
				input: ['S_L1'],
				output: ['S_L2'],
				properties: {
					name: 't12SS_t12'
				}
			},
			{
				id: 't12II_t12',
				input: ['I_L1'],
				output: ['I_L2'],
				properties: {
					name: 't12II_t12'
				}
			},
			{
				id: 't12RR_t12',
				input: ['R_L1'],
				output: ['R_L2'],
				properties: {
					name: 't12RR_t12'
				}
			},
			{
				id: 't12SS_t21',
				input: ['S_L2'],
				output: ['S_L1'],
				properties: {
					name: 't12SS_t21'
				}
			},
			{
				id: 't12II_t21',
				input: ['I_L2'],
				output: ['I_L1'],
				properties: {
					name: 't12II_t21'
				}
			},
			{
				id: 't12RR_t21',
				input: ['R_L2'],
				output: ['R_L1'],
				properties: {
					name: 't12RR_t21'
				}
			},
			{
				id: 'inf_infL1L1',
				input: ['I_L1', 'S_L1'],
				output: ['I_L1', 'I_L1'],
				properties: {
					name: 'inf_infL1L1'
				}
			},
			{
				id: 'inf_infL2L2',
				input: ['I_L2', 'S_L2'],
				output: ['I_L2', 'I_L2'],
				properties: {
					name: 'inf_infL2L2'
				}
			},
			{
				id: 'rec_recL1L1',
				input: ['I_L1'],
				output: ['R_L1'],
				properties: {
					name: 'rec_recL1L1'
				}
			},
			{
				id: 'rec_recL2L2',
				input: ['I_L2'],
				output: ['R_L2'],
				properties: {
					name: 'rec_recL2L2'
				}
			}
		]
	},
	schema_name: 'petrinet',
	model_version: '0.1',
	semantics: {
		ode: {
			rates: [
				{
					target: 't12SS_t12',
					expression: 'S_L1*p_1_t12SS*tau12',
					expression_mathml: '<apply><times/><ci>S_L1</ci><ci>p_1_t12SS</ci><ci>tau12</ci></apply>'
				},
				{
					target: 't12II_t12',
					expression: 'I_L1*p_1_t12II*tau12',
					expression_mathml: '<apply><times/><ci>I_L1</ci><ci>p_1_t12II</ci><ci>tau12</ci></apply>'
				},
				{
					target: 't12RR_t12',
					expression: 'R_L1*p_1_t12RR*tau12',
					expression_mathml: '<apply><times/><ci>R_L1</ci><ci>p_1_t12RR</ci><ci>tau12</ci></apply>'
				},
				{
					target: 't12SS_t21',
					expression: 'S_L2*p_1_t12SS*tau21',
					expression_mathml: '<apply><times/><ci>S_L2</ci><ci>p_1_t12SS</ci><ci>tau21</ci></apply>'
				},
				{
					target: 't12II_t21',
					expression: 'I_L2*p_1_t12II*tau21',
					expression_mathml: '<apply><times/><ci>I_L2</ci><ci>p_1_t12II</ci><ci>tau21</ci></apply>'
				},
				{
					target: 't12RR_t21',
					expression: 'R_L2*p_1_t12RR*tau21',
					expression_mathml: '<apply><times/><ci>R_L2</ci><ci>p_1_t12RR</ci><ci>tau21</ci></apply>'
				},
				{
					target: 'inf_infL1L1',
					expression: 'I_L1*S_L1*beta*p_2_infL1L1',
					expression_mathml:
						'<apply><times/><ci>I_L1</ci><ci>S_L1</ci><ci>beta</ci><ci>p_2_infL1L1</ci></apply>'
				},
				{
					target: 'inf_infL2L2',
					expression: 'I_L2*S_L2*beta*p_2_infL2L2',
					expression_mathml:
						'<apply><times/><ci>I_L2</ci><ci>S_L2</ci><ci>beta</ci><ci>p_2_infL2L2</ci></apply>'
				},
				{
					target: 'rec_recL1L1',
					expression: 'I_L1*gamma*p_2_recL1L1',
					expression_mathml:
						'<apply><times/><ci>I_L1</ci><ci>gamma</ci><ci>p_2_recL1L1</ci></apply>'
				},
				{
					target: 'rec_recL2L2',
					expression: 'I_L2*gamma*p_2_recL2L2',
					expression_mathml:
						'<apply><times/><ci>I_L2</ci><ci>gamma</ci><ci>p_2_recL2L2</ci></apply>'
				}
			],
			initials: [
				{
					target: 'S_L1',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				},
				{
					target: 'S_L2',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				},
				{
					target: 'I_L1',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				},
				{
					target: 'I_L2',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				},
				{
					target: 'R_L1',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				},
				{
					target: 'R_L2',
					expression: '0.16666666666666666',
					expression_mathml: '<cn>0.16666666666666666002</cn>'
				}
			],
			parameters: [
				{
					id: 'p_1_t12SS',
					value: 1
				},
				{
					id: 'tau12',
					value: 0.5
				},
				{
					id: 'p_1_t12II',
					value: 1
				},
				{
					id: 'p_1_t12RR',
					value: 1
				},
				{
					id: 'tau21',
					value: 0.5
				},
				{
					id: 'beta',
					value: 2.7e-7,
					distribution: {
						type: 'Uniform1',
						parameters: {
							minimum: 2.6e-7,
							maximum: 2.8e-7
						}
					}
				},
				{
					id: 'p_2_infL1L1',
					value: 1
				},
				{
					id: 'p_2_infL2L2',
					value: 1
				},
				{
					id: 'gamma',
					value: 0.14,
					distribution: {
						type: 'Uniform1',
						parameters: {
							minimum: 0.1,
							maximum: 0.18
						}
					}
				},
				{
					id: 'p_2_recL1L1',
					value: 1
				},
				{
					id: 'p_2_recL2L2',
					value: 1
				}
			],
			observables: [
				{
					id: 'noninf',
					name: 'noninf',
					expression: 'R_L1 + R_L2 + S_L1 + S_L2',
					expression_mathml:
						'<apply><plus/><ci>R_L1</ci><ci>R_L2</ci><ci>S_L1</ci><ci>S_L2</ci></apply>'
				}
			],
			time: {
				id: 't',
				units: {
					expression: 'day',
					expression_mathml: '<ci>day</ci>'
				}
			}
		},
		span: [
			{
				map: [
					['S_L1', 'S'],
					['I_L1', 'I'],
					['R_L1', 'R'],
					['S_L2', 'S'],
					['I_L2', 'I'],
					['R_L2', 'R'],
					['t12SS_t12', 't12SS'],
					['t12II_t12', 't12II'],
					['t12RR_t12', 't12RR'],
					['t12SS_t21', 't12SS'],
					['t12II_t21', 't12II'],
					['t12RR_t21', 't12RR'],
					['inf_infL1L1', 'inf'],
					['inf_infL2L2', 'inf'],
					['rec_recL1L1', 'rec'],
					['rec_recL2L2', 'rec']
				],
				system: {
					name: 'SIRs',
					semantics: {
						typing: {
							map: [
								['S', 'Pop'],
								['I', 'Pop'],
								['R', 'Pop'],
								['inf', 'Infection'],
								['rec', 'Recovery'],
								['t12SS', 'Strata'],
								['t12II', 'Strata'],
								['t12RR', 'Strata']
							],
							system: {
								name: 'SIRs',
								model_version: '0.1',
								model: {
									transitions: [
										{
											output: ['Pop', 'Pop'],
											properties: {
												name: 'Infection',
												description: 'Infection'
											},
											id: 'Infection',
											input: ['Pop', 'Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Recovery',
												description: 'Recovery'
											},
											id: 'Recovery',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Strata',
												description:
													'1-to-1 process that represents a change in the demographic division of a human individual.'
											},
											id: 'Strata',
											input: ['Pop']
										}
									],
									states: [
										{
											name: 'Pop',
											id: 'Pop',
											description: 'Compartment of individuals in a human population.'
										}
									]
								},
								description: 'SIR model',
								schema:
									'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json'
							}
						},
						ode: {
							initials: [
								{
									expression: 'S0',
									expression_mathml: '<ci>S0</ci>',
									target: 'S'
								},
								{
									expression: 'I0',
									expression_mathml: '<ci>I0</ci>',
									target: 'I'
								},
								{
									expression: 'R0',
									expression_mathml: '<ci>R0</ci>',
									target: 'R'
								}
							],
							parameters: [
								{
									id: 'beta',
									distribution: {
										parameters: {
											minimum: 2.6e-7,
											maximum: 2.8e-7
										},
										type: 'Uniform1'
									},
									value: 2.7e-7,
									description: 'infection rate'
								},
								{
									id: 'gamma',
									grounding: {
										identifiers: {
											askemo: '0000013'
										}
									},
									distribution: {
										parameters: {
											minimum: 0.1,
											maximum: 0.18
										},
										type: 'Uniform1'
									},
									value: 0.14,
									description: 'recovery rate'
								},
								{
									id: 'S0',
									value: 1000,
									description: 'Total susceptible population at timestep 0'
								},
								{
									id: 'I0',
									value: 1,
									description: 'Total infected population at timestep 0'
								},
								{
									id: 'R0',
									value: 0,
									description: 'Total recovered population at timestep 0'
								},
								{
									name: '',
									id: 't12SSParam',
									value: 1,
									description: ''
								},
								{
									name: '',
									id: 't12IIParam',
									value: 1,
									description: ''
								},
								{
									name: '',
									id: 't12RRParam',
									value: 1,
									description: ''
								}
							],
							rates: [
								{
									expression: 'S*I*beta',
									expression_mathml: '<apply><times/><ci>S</ci><ci>I</ci><ci>beta</ci></apply>',
									target: 'inf'
								},
								{
									expression: 'I*gamma',
									expression_mathml: '<apply><times/><ci>I</ci><ci>gamma</ci></apply>',
									target: 'rec'
								},
								{
									expression: 'S*S*t12SSParam',
									expression_mathml:
										'<apply><times/><ci>S</ci><ci>S</ci><ci>t12SSParam</ci></apply>',
									target: 't12SS'
								},
								{
									expression: 'I*t12IIParam',
									expression_mathml: '<apply><times/><ci>I</ci><ci>t12IIParam</ci></apply>',
									target: 't12II'
								},
								{
									expression: 'R*t12RRParam',
									expression_mathml: '<apply><times/><ci>R</ci><ci>t12RRParam</ci></apply>',
									target: 't12RR'
								}
							],
							time: {
								units: {
									expression: 'day',
									expression_mathml: '<ci>day</ci>'
								},
								id: 't'
							},
							observables: [
								{
									name: 'Non-infectious',
									expression: 'S+R',
									expression_mathml: '<apply><plus/><ci>S</ci><ci>R</ci></apply>',
									id: 'noninf',
									states: ['S', 'R']
								}
							]
						}
					},
					model_version: '0.1',
					model: {
						transitions: [
							{
								output: ['I', 'I'],
								properties: {
									name: 'Infection',
									description: 'Infective process between individuals'
								},
								id: 'inf',
								input: ['I', 'S']
							},
							{
								output: ['R'],
								properties: {
									name: 'Recovery',
									description: 'Recovery process of a infected individual'
								},
								id: 'rec',
								input: ['I']
							},
							{
								output: ['S', 'S'],
								properties: {
									name: 't12SS',
									description: ''
								},
								id: 't12SS',
								input: ['S', 'S']
							},
							{
								output: ['I'],
								properties: {
									name: 't12II',
									description: ''
								},
								id: 't12II',
								input: ['I']
							},
							{
								output: ['R'],
								properties: {
									name: 't12RR',
									description: ''
								},
								id: 't12RR',
								input: ['R']
							}
						],
						states: [
							{
								units: {
									expression: 'person',
									expression_mathml: '<ci>person</ci>'
								},
								name: 'Susceptible',
								id: 'S',
								grounding: {
									identifiers: {
										ido: '0000514'
									}
								},
								description: "Number of individuals that are 'susceptible' to a disease infection"
							},
							{
								units: {
									expression: 'person',
									expression_mathml: '<ci>person</ci>'
								},
								name: 'Infected',
								id: 'I',
								grounding: {
									identifiers: {
										ido: '0000511'
									}
								},
								description: "Number of individuals that are 'infected' by a disease"
							},
							{
								units: {
									expression: 'person',
									expression_mathml: '<ci>person</ci>'
								},
								name: 'Recovered',
								id: 'R',
								grounding: {
									identifiers: {
										ido: '0000592'
									}
								},
								description: "Number of individuals that have 'recovered' from a disease infection"
							}
						]
					},
					id: '7dde01ea-ec6e-4d09-9e49-55fddb5187a5',
					metadata: {
						attributes: [
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974474'
											},
											name: 'Bucky',
											id: {
												id: 'T:-1709799622'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 738,
												page: 0,
												block: 0,
												char_end: 743
											}
										}
									],
									groundings: [],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974474'
											},
											source: 'time',
											id: {
												id: 'T:-486841659'
											},
											grounding: [
												{
													provenance: {
														method: 'SKEMA-TR-Embedding',
														timestamp: '2023-06-15T22:59:11.974644'
													},
													grounding_text: 'time since time scale zero',
													score: 0.8945620059967041,
													source: [],
													grounding_id: 'apollosv:00000272'
												}
											],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 732,
												page: 0,
												block: 0,
												char_end: 736
											}
										}
									],
									value_specs: [],
									id: {
										id: 'R:190348269'
									}
								},
								type: 'anchored_extraction'
							},
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974780'
											},
											name: 'SEIR',
											id: {
												id: 'T:2131207786'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 56,
												page: 0,
												block: 0,
												char_end: 60
											}
										}
									],
									groundings: [],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974780'
											},
											source: 'spatially distributed',
											id: {
												id: 'T:-1520869470'
											},
											grounding: [],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 34,
												page: 0,
												block: 0,
												char_end: 55
											}
										}
									],
									value_specs: [],
									id: {
										id: 'R:159895595'
									}
								},
								type: 'anchored_extraction'
							},
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974931'
											},
											name: 'S',
											id: {
												id: 'T:1326919589'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 562,
												page: 0,
												block: 0,
												char_end: 563
											}
										}
									],
									groundings: [
										{
											provenance: {
												method: 'SKEMA-TR-Embedding',
												timestamp: '2023-06-15T22:59:11.974960'
											},
											grounding_text: 'Meruvax I',
											score: 0.7847759127616882,
											source: [],
											grounding_id: 'vo:0003109'
										}
									],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.974931'
											},
											source: 'fraction of the population',
											id: {
												id: 'T:1687413640'
											},
											grounding: [
												{
													provenance: {
														method: 'SKEMA-TR-Embedding',
														timestamp: '2023-06-15T22:59:11.975009'
													},
													grounding_text: 'count of simulated population',
													score: 0.8330355286598206,
													source: [],
													grounding_id: 'apollosv:00000022'
												}
											],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 570,
												page: 0,
												block: 0,
												char_end: 596
											}
										}
									],
									value_specs: [],
									id: {
										id: 'E:-337831219'
									}
								},
								type: 'anchored_extraction'
							},
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975127'
											},
											name: 'asym frac',
											id: {
												id: 'T:-24678027'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 142,
												page: 0,
												block: 0,
												char_end: 151
											}
										},
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											name: 'α',
											id: {
												id: 'v10'
											}
										}
									],
									groundings: [
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											grounding_text: 'Van',
											score: 1,
											source: [],
											grounding_id: 'geonames:298117'
										},
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											grounding_text: 'Sanaa',
											score: 1,
											source: [],
											grounding_id: 'geonames:71137'
										}
									],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975127'
											},
											source: 'percentage of infections',
											id: {
												id: 'T:1244663286'
											},
											grounding: [
												{
													provenance: {
														method: 'SKEMA-TR-Embedding',
														timestamp: '2023-06-15T22:59:11.975201'
													},
													grounding_text: 'percentage of cases',
													score: 0.8812347650527954,
													source: [],
													grounding_id: 'cemo:percentage_of_cases'
												}
											],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 94,
												page: 0,
												block: 0,
												char_end: 118
											}
										},
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											source: ' Rate of infections that are asymptomatic',
											id: {
												id: 'v10'
											}
										}
									],
									value_specs: [],
									id: {
										id: 'E:-1921441554'
									}
								},
								type: 'anchored_extraction'
							},
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975270'
											},
											name: 'asym frac',
											id: {
												id: 'T:-24678027'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 142,
												page: 0,
												block: 0,
												char_end: 151
											}
										},
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											name: 'asym_frac',
											id: {
												id: 'v18'
											}
										}
									],
									groundings: [],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975270'
											},
											source: 'percentage of infections',
											id: {
												id: 'T:1244663286'
											},
											grounding: [
												{
													provenance: {
														method: 'SKEMA-TR-Embedding',
														timestamp: '2023-06-15T22:59:11.975340'
													},
													grounding_text: 'percentage of cases',
													score: 0.8812347650527954,
													source: [],
													grounding_id: 'cemo:percentage_of_cases'
												}
											],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 94,
												page: 0,
												block: 0,
												char_end: 118
											}
										},
										{
											provenance: {
												method:
													'MIT extractor V1.0 - text, dataset, formula annotation (chunwei@mit.edu)',
												timestamp: '2023-06-15T22:59:13.177022'
											},
											source: ' Fraction of infections that are asymptomatic',
											id: {
												id: 'v18'
											}
										}
									],
									value_specs: [],
									id: {
										id: 'E:392549189'
									}
								},
								type: 'anchored_extraction'
							},
							{
								payload: {
									names: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975409'
											},
											name: 'asym frac',
											id: {
												id: 'T:-24678027'
											},
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 142,
												page: 0,
												block: 0,
												char_end: 151
											}
										}
									],
									groundings: [],
									descriptions: [
										{
											provenance: {
												method: 'Skema TR Pipeline rules',
												timestamp: '2023-06-15T22:59:11.975409'
											},
											source: 'percentage of infections',
											id: {
												id: 'T:1244663286'
											},
											grounding: [
												{
													provenance: {
														method: 'SKEMA-TR-Embedding',
														timestamp: '2023-06-15T22:59:11.975479'
													},
													grounding_text: 'percentage of cases',
													score: 0.8812347650527954,
													source: [],
													grounding_id: 'cemo:percentage_of_cases'
												}
											],
											extraction_source: {
												document_reference: {
													id: 'buckymodel_webdocs.pdf'
												},
												char_start: 94,
												page: 0,
												block: 0,
												char_end: 118
											}
										}
									],
									value_specs: [],
									id: {
										id: 'E:-1790112729'
									}
								},
								type: 'anchored_extraction'
							}
						]
					},
					description: 'SIR model',
					schema:
						'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
					schema_name: 'petrinet'
				}
			},
			{
				map: [
					['S_L1', 'L1'],
					['I_L1', 'L1'],
					['R_L1', 'L1'],
					['S_L2', 'L2'],
					['I_L2', 'L2'],
					['R_L2', 'L2'],
					['t12SS_t12', 't12'],
					['t12II_t12', 't12'],
					['t12RR_t12', 't12'],
					['t12SS_t21', 't21'],
					['t12II_t21', 't21'],
					['t12RR_t21', 't21'],
					['inf_infL1L1', 'infL1L1'],
					['inf_infL2L2', 'infL2L2'],
					['rec_recL1L1', 'recL1L1'],
					['rec_recL2L2', 'recL2L2']
				],
				system: {
					name: 'Location-travel strata model',
					semantics: {
						typing: {
							map: [
								['L1', 'Pop'],
								['L2', 'Pop'],
								['t12', 'Strata'],
								['t21', 'Strata'],
								['infL1L1', 'Infection'],
								['infL2L2', 'Infection'],
								['recL1L1', 'Recovery'],
								['recL2L2', 'Recovery']
							],
							system: {
								name: 'Location-travel strata model',
								model_version: '0.1',
								model: {
									transitions: [
										{
											output: ['Pop', 'Pop'],
											properties: {
												name: 'Infection',
												description: 'Infection'
											},
											id: 'Infection',
											input: ['Pop', 'Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Recovery',
												description: 'Recovery'
											},
											id: 'Recovery',
											input: ['Pop']
										},
										{
											output: ['Pop'],
											properties: {
												name: 'Strata',
												description:
													'1-to-1 process that represents a change in the demographic division of a human individual.'
											},
											id: 'Strata',
											input: ['Pop']
										}
									],
									states: [
										{
											name: 'Pop',
											id: 'Pop',
											description: 'Compartment of individuals in a human population.'
										}
									]
								},
								description: 'Location-travel strata model',
								schema:
									'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json'
							}
						},
						ode: {
							initials: [
								{
									expression: 'L1init',
									expression_mathml: '<ci>L1init</ci>',
									target: 'L1'
								},
								{
									expression: 'L2init',
									expression_mathml: '<ci>L2init</ci>',
									target: 'L2'
								}
							],
							parameters: [
								{
									id: 'tau12',
									value: 0.5,
									description: "travel rate from location 'nyc' to ' tor'"
								},
								{
									id: 'tau21',
									value: 0.5,
									description: "travel rate from location ' tor' to 'nyc'"
								},
								{
									id: 'L1init',
									value: 0.5,
									description: "Proportion of population in location 'nyc' at timestep 0"
								},
								{
									id: 'L2init',
									value: 0.5,
									description: "Proportion of population in location ' tor' at timestep 0"
								},
								{
									name: '',
									id: 'infL1L1Param',
									value: 1,
									description: ''
								},
								{
									name: '',
									id: 'infL2L2Param',
									value: 1,
									description: ''
								},
								{
									name: '',
									id: 'recL1L1Param',
									value: 1,
									description: ''
								},
								{
									name: '',
									id: 'recL2L2Param',
									value: 1,
									description: ''
								}
							],
							rates: [
								{
									expression: 'L1*tau12',
									expression_mathml: '<apply><times/><ci>L1</ci><ci>tau12</ci></apply>',
									target: 't12'
								},
								{
									expression: 'L2*tau21',
									expression_mathml: '<apply><times/><ci>L2</ci><ci>tau21</ci></apply>',
									target: 't21'
								},
								{
									expression: 'L1*L1*infL1L1Param',
									expression_mathml:
										'<apply><times/><ci>L1</ci><ci>L1</ci><ci>infL1L1Param</ci></apply>',
									target: 'infL1L1'
								},
								{
									expression: 'L2*L2*infL2L2Param',
									expression_mathml:
										'<apply><times/><ci>L2</ci><ci>L2</ci><ci>infL2L2Param</ci></apply>',
									target: 'infL2L2'
								},
								{
									expression: 'L1*recL1L1Param',
									expression_mathml: '<apply><times/><ci>L1</ci><ci>recL1L1Param</ci></apply>',
									target: 'recL1L1'
								},
								{
									expression: 'L2*recL2L2Param',
									expression_mathml: '<apply><times/><ci>L2</ci><ci>recL2L2Param</ci></apply>',
									target: 'recL2L2'
								}
							]
						}
					},
					model_version: '0.1',
					model: {
						transitions: [
							{
								output: ['L2'],
								properties: {
									name: 't&#8321&#8322',
									description: 'Travel of an individual from location L1 and L2.'
								},
								id: 't12',
								input: ['L1']
							},
							{
								output: ['L1'],
								properties: {
									name: 't&#8322&#8321',
									description: 'Travel of an individual from location L2 and L1.'
								},
								id: 't21',
								input: ['L2']
							},
							{
								output: ['L1', 'L1'],
								properties: {
									name: 'infL1L1',
									description: ''
								},
								id: 'infL1L1',
								input: ['L1', 'L1']
							},
							{
								output: ['L2', 'L2'],
								properties: {
									name: 'infL2L2',
									description: ''
								},
								id: 'infL2L2',
								input: ['L2', 'L2']
							},
							{
								output: ['L1'],
								properties: {
									name: 'recL1L1',
									description: ''
								},
								id: 'recL1L1',
								input: ['L1']
							},
							{
								output: ['L2'],
								properties: {
									name: 'recL2L2',
									description: ''
								},
								id: 'recL2L2',
								input: ['L2']
							}
						],
						states: [
							{
								units: {
									expression: 'person',
									expression_mathml: '<ci>person</ci>'
								},
								name: 'nyc',
								id: 'L1',
								description:
									'Number of individuals relative to the total population that are in location L1.'
							},
							{
								units: {
									expression: 'person',
									expression_mathml: '<ci>person</ci>'
								},
								name: ' tor',
								id: 'L2',
								description:
									'Number of individuals relative to the total population that are in location L2.'
							}
						]
					},
					id: 'location-travel',
					metadata: {
						variable_statements: [],
						annotations: {},
						attributes: [],
						processed_by: '',
						processed_at: 0
					},
					description: 'Location-travel strata model',
					schema:
						'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
					schema_name: 'petrinet'
				}
			}
		],
		typing: {
			map: [
				['S_L1', 'Pop'],
				['I_L1', 'Pop'],
				['R_L1', 'Pop'],
				['S_L2', 'Pop'],
				['I_L2', 'Pop'],
				['R_L2', 'Pop'],
				['t12SS_t12', 'Strata'],
				['t12II_t12', 'Strata'],
				['t12RR_t12', 'Strata'],
				['t12SS_t21', 'Strata'],
				['t12II_t21', 'Strata'],
				['t12RR_t21', 'Strata'],
				['inf_infL1L1', 'Infection'],
				['inf_infL2L2', 'Infection'],
				['rec_recL1L1', 'Recovery'],
				['rec_recL2L2', 'Recovery']
			],
			system: {
				name: 'SIRs',
				model_version: '0.1',
				model: {
					transitions: [
						{
							output: ['Pop', 'Pop'],
							properties: {
								name: 'Infection',
								description: 'Infection'
							},
							id: 'Infection',
							input: ['Pop', 'Pop']
						},
						{
							output: ['Pop'],
							properties: {
								name: 'Recovery',
								description: 'Recovery'
							},
							id: 'Recovery',
							input: ['Pop']
						},
						{
							output: ['Pop'],
							properties: {
								name: 'Strata',
								description:
									'1-to-1 process that represents a change in the demographic division of a human individual.'
							},
							id: 'Strata',
							input: ['Pop']
						}
					],
					states: [
						{
							name: 'Pop',
							id: 'Pop',
							description: 'Compartment of individuals in a human population.'
						}
					]
				},
				description: 'SIR model',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json'
			}
		}
	},
	metadata: {
		annotations: {
			authors: [],
			references: [],
			locations: [],
			pathogens: [],
			diseases: [],
			hosts: [],
			model_types: []
		}
	},
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json'
};
