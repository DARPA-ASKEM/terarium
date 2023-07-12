// @ts-nocheck
/* eslint-disable */
import { Model } from '@/types/Types';

export const flux_typed_aug: Model = {
	name: 'Two City Stratification Model',
	schema:
		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
	description: 'Stratification spatially between two cities model created by Micah',
	model_version: '0.1',
	model: {
		states: [
			{
				id: 'Rgn_1',
				name: 'Region 1',
				description:
					'Number of individuals physically located in region 1 relative to the total population'
			},
			{
				id: 'Rgn_2',
				name: 'Region 2',
				description:
					'Number of individuals physically located in region 2 relative to the total population'
			}
		],
		transitions: [
			{
				id: 'Rgn1_dis',
				input: ['Rgn_1'],
				output: ['Rgn_1'],
				properties: {
					name: 'Rgn1_disease'
				}
			},
			{
				id: 'Rgn1_inf',
				input: ['Rgn_1', 'Rgn_1'],
				output: ['Rgn_1', 'Rgn_1'],
				properties: {
					name: 'Rgn1_infection'
				}
			},
			{
				id: 'Rgn2_dis',
				input: ['Rgn_2'],
				output: ['Rgn_2'],
				properties: {
					name: 'Rgn2_disease'
				}
			},
			{
				id: 'Rgn2_inf',
				input: ['Rgn_2', 'Rgn_2'],
				output: ['Rgn_2', 'Rgn_2'],
				properties: {
					name: 'Rgn2_infection'
				}
			},
			{
				id: 'travel_1_2',
				input: ['Rgn_1'],
				output: ['Rgn_2'],
				properties: {
					name: 'Traveling 1 -> 2',
					description: 'Individuals traveling from region 1 to region 2'
				}
			},
			{
				id: 'travel_2_1',
				input: ['Rgn_2'],
				output: ['Rgn_1'],
				properties: {
					name: 'Traveling 2 -> 1',
					description: 'Individuals traveling from region 2 to region 1'
				}
			}
		]
	},
	semantics: {
		ode: {},
		typing: {
			system: {
				name: 'Ontology Model w/ Pop and Vax States',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
				description:
					'Ontology Model w/ Population and Vaccine-Unit States. File created by Patrick based ontology created by Nelson',
				model_version: '0.1',
				model: {
					states: [
						{
							id: 'Pop',
							name: 'Pop',
							description: 'Compartment of individuals in a human population'
						},
						{
							id: 'Vaccine',
							name: 'Vaccine',
							description:
								'Compartment of vaccine doses available for use in a vaccination campaign'
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
				semantics: {},
				metadata: {}
			},
			map: [
				['Rgn_1', 'Pop'],
				['Rgn_2', 'Pop'],
				['Rgn1_dis', 'Disease'],
				['Rgn1_inf', 'Infect'],
				['Rgn2_dis', 'Disease'],
				['Rgn2_inf', 'Infect'],
				['travel_1_2', 'Strata'],
				['travel_2_1', 'Strata']
			]
		}
	},
	metadata: {}
};
