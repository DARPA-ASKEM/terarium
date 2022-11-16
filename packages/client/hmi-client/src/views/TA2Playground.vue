<script lang="ts">
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { petriNetValidator, PetriNet } from '@/utils/petri-net-validator';
import * as d3 from 'd3';
import _ from 'lodash';
import { defineComponent, ref } from 'vue';
import { fetchStratificationResult } from '@/services/models/stratification-service';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';

interface NodeData {
	type: string;
}

interface EdgeData {
	val: number;
}
enum NodeType {
	Species = 'S',
	Transition = 'T'
}

let g: IGraph<NodeData, EdgeData> = {
	width: 500,
	height: 500,
	nodes: [],
	edges: []
};

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';

class SampleRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', 'arrowhead')
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW)
			.style('fill', '#000')
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter(
			(d) => d.data.type === 'species' || d.data.type === NodeType.Species
		);
		const transitions = selection.filter(
			(d) => d.data.type === 'transition' || d.data.type === NodeType.Transition
		);

		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88C')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#f80');

		selection
			.append('text')
			.attr('y', -5)
			.text((d) => d.label);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 2)
			.attr('marker-end', 'url(#arrowhead)');
	}
}

let renderer: SampleRenderer | null = null;
g = runDagreLayout(_.cloneDeep(g));

let placeCounter = 0;
let transitionCounter = 0;
let modelId = ''; // The session model
let source: any = null;
let target: any = null;

let numRabbits = 100;
let numWolves = 10;

export default defineComponent({
	name: 'TA2Playground',
	async mounted() {
		console.log('TA2 Playground initialized');

		const playground = document.getElementById('playground') as HTMLDivElement;
		renderer = new SampleRenderer({
			el: playground ?? undefined,
			useAStarRouting: true,
			runLayout: runDagreLayout
		});

		renderer.on('node-click', (_evtName, evt, d) => {
			if (evt.shiftKey) {
				if (source) {
					target = d;
					target.select('.shape').style('stroke', '#000').style('stroke-width', 4);
				} else {
					source = d;
					source.select('.shape').style('stroke', '#000').style('stroke-width', 4);
				}
			} else {
				if (source) {
					source.select('.shape').style('stroke', null).style('stroke-width', null);
				}
				if (target) {
					target.select('.shape').style('stroke', null).style('stroke-width', null);
				}
				source = null;
				target = null;
			}

			if (source && target) {
				this.addEdge(source, target);
				source = null;
				target = null;
			}
		});

		// Test
		const test = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
		const testData = await test.json();
		modelId = testData.id;

		this.refresh();
		this.jsonOutput();
	},
	setup() {
		const loadModelID = ref('');
		const stratifyModelA = ref('');
		const stratifyModelB = ref('');
		const stratifyTypeModel = ref('');
		return { loadModelID, stratifyModelA, stratifyModelB, stratifyTypeModel };
	},
	methods: {
		async refresh() {
			await renderer?.setData(g);
			await renderer?.render();
		},
		async LotkaVolterra() {
			const test = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
			const testData = await test.json();
			modelId = testData.id;

			console.log('modle id is', modelId);

			// Reset
			g.nodes = [];
			g.edges = [];

			g.nodes.push({
				id: 'rabbits',
				label: 'rabbits',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'species' }
			});
			g.nodes.push({
				id: 'wolves',
				label: 'wolves',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'species' }
			});
			g.nodes.push({
				id: 'death',
				label: 'death',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});
			g.nodes.push({
				id: 'birth',
				label: 'birth',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});
			g.nodes.push({
				id: 'predation',
				label: 'predation',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});

			g.edges.push({ id: '1', source: 'wolves', target: 'death', points: [], data: { val: 1 } });
			g.edges.push({
				id: '2',
				source: 'predation',
				target: 'wolves',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({
				id: '3',
				source: 'wolves',
				target: 'predation',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({
				id: '4',
				source: 'rabbits',
				target: 'predation',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({ id: '5', source: 'rabbits', target: 'birth', points: [], data: { val: 1 } });
			g.edges.push({ id: '6', source: 'birth', target: 'rabbits', points: [], data: { val: 1 } });

			g = runDagreLayout(_.cloneDeep(g));

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{ name: 'rabbits', type: 'S' },
						{ name: 'wolves', type: 'S' },
						{ name: 'birth', type: 'T' },
						{ name: 'death', type: 'T' },
						{ name: 'predation', type: 'T' }
					],
					edges: [
						{ source: 'wolves', target: 'death' },
						{ source: 'predation', target: 'wolves' },
						{ source: 'predation', target: 'wolves' },
						{ source: 'wolves', target: 'predation' },
						{ source: 'rabbits', target: 'predation' },
						{ source: 'rabbits', target: 'birth' },
						{ source: 'birth', target: 'rabbits' },
						{ source: 'birth', target: 'rabbits' }
					]
				})
			});

			this.refresh();
			this.jsonOutput();
		},
		async jsonOutput() {
			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/json`, {
				method: 'GET'
			});
			const output = await resp.json();
			console.log(petriNetValidator(output));
			d3.select('#output').text(JSON.stringify(output, null, 2));
		},
		// eslint-disable-next-line
		async addEdge(source: any, target: any) {
			g.edges.push({
				id: `${source.datum().id}_${target.datum().id}`,
				source: source.datum().id,
				target: target.datum().id,
				points: [
					{
						x: source.datum().x + source.datum().width * 0.5,
						y: source.datum().y + source.datum().height * 0.5
					},
					{
						x: target.datum().x + target.datum().width * 0.5,
						y: target.datum().y + target.datum().height * 0.5
					}
				],
				data: { val: 1 }
			});

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					edges: [
						{
							source: source.datum().id,
							target: target.datum().id
						}
					]
				})
			});
		},
		async addPlace() {
			console.log('add place');
			placeCounter++;
			const id = `p-${placeCounter}`;

			g.nodes.push({
				id,
				label: id,
				x: Math.random() * 500,
				y: Math.random() * 500,
				height: 50,
				width: 50,
				data: { type: 'species' },
				nodes: []
			});
			this.refresh();

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{
							name: id,
							type: 'S'
						}
					]
				})
			});
			this.jsonOutput();
		},
		async addTransition() {
			console.log('add transition');
			transitionCounter++;
			const id = `t-${transitionCounter}`;

			g.nodes.push({
				id,
				label: id,
				x: Math.random() * 500,
				y: Math.random() * 500,
				height: 50,
				width: 50,
				data: { type: 'transition' },
				nodes: []
			});
			this.refresh();

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{
							name: id,
							type: 'T'
						}
					]
				})
			});
			this.jsonOutput();
		},
		// provide node details and a flag
		// createFlag: True = create new + draw, False = just draw
		async addNode(
			id: string,
			label: string,
			x: number,
			y: number,
			height: number,
			width: number,
			type: NodeType,
			createFlag: boolean
		) {
			g.nodes.push({
				id,
				label,
				x,
				y,
				height,
				width,
				data: { type },
				nodes: []
			});

			if (createFlag === true) {
				await fetch(`http://localhost:8888/api/models/${modelId}`, {
					method: 'POST',
					headers: {
						Accept: 'application/json',
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						nodes: [
							{
								name: label,
								type
							}
						]
					})
				});
			}
			this.jsonOutput();
		}, // end addNode
		// Not sure how to overload functions so here we are
		// createFlag: True - Create and draw, false - just draw
		async addEdgeID(sourceID: string, targetID: string, createFlag: boolean) {
			let sourceX;
			let sourceY;
			let targetX;
			let targetY;
			let sourceLabel;
			let targetLabel;
			// Find source and target's locations
			// there has to be a better way to get the source and target locations
			for (let i = 0; i < g.nodes.length; i++) {
				if (sourceLabel && targetLabel) {
					break;
				}
				if (g.nodes[i].id === sourceID) {
					sourceLabel = g.nodes[i].label;
					sourceX = g.nodes[i].x + g.nodes[i].width * 0.5;
					sourceY = g.nodes[i].y + g.nodes[i].height * 0.5;
				}
				if (g.nodes[i].id === targetID) {
					targetLabel = g.nodes[i].label;
					targetX = g.nodes[i].x + g.nodes[i].width * 0.5;
					targetY = g.nodes[i].y + g.nodes[i].height * 0.5;
				}
			}
			g.edges.push({
				source: sourceLabel,
				target: targetLabel,
				points: [
					{
						x: sourceX, // + source.datum().width * 0.5,
						y: sourceY // + source.datum().height * 0.5
					},
					{
						x: targetX, // + target.datum().width * 0.5,
						y: targetY // + target.datum().height * 0.5
					}
				]
			});

			if (createFlag === true) {
				await fetch(`http://localhost:8888/api/models/${modelId}`, {
					method: 'POST',
					headers: {
						Accept: 'application/json',
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						edges: [
							{
								source: sourceLabel,
								target: targetLabel
							}
						]
					})
				});
			}
		}, // end addEdge

		async simulate() {
			numWolves = +(Math.random() * 100).toFixed();
			numRabbits = +(Math.random() * 100).toFixed();

			// Run a simulation on LotkaVolterra with random values
			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/simulate`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					variables: {
						rabbits: numRabbits,
						wolves: numWolves
					},
					parameters: {
						birth: 0.3,
						predation: 0.015,
						death: 0.7
					}
				})
			});
			const output = await resp.json();
			this.renderResult(output);
		},
		renderResult(result: any) {
			const el = d3.select('#solution');
			el.selectAll('*').remove();
			console.log(result);

			const svg = el.append('svg').style('width', '100%').style('height', '100%');

			svg
				.append('text')
				.attr('x', 20)
				.attr('y', 35)
				.style('stroke', null)
				.style('fill', 'red')
				.text(`rabbits: ${numRabbits}`);

			svg
				.append('text')
				.attr('x', 120)
				.attr('y', 35)
				.style('stroke', null)
				.style('fill', 'blue')
				.text(`wolves: ${numWolves}`);

			for (let idx = 0; idx < result.u.length; idx++) {
				const bars = result.u[idx];

				svg
					.append('circle')
					.attr('cx', 6 * idx)
					.attr('cy', 200 - bars[0])
					.attr('r', 3)
					.style('stroke', null)
					.style('fill', 'red');

				svg
					.append('circle')
					.attr('cx', 6 * idx)
					.attr('cy', 200 - bars[1])
					.attr('r', 3)
					.style('stroke', null)
					.style('fill', 'blue');
			}
		},
		async mergePetrinets() {
			const modelA: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3'
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 2
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 1
					},
					{
						ot: 2,
						os: 3
					}
				]
			};
			const modelB: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3'
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 1
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 3
					}
				]
			};
			const modelC: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					},
					{
						tname: 't-1' // 3
					},
					{
						tname: 't-2' // 4
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3p-3'
					},
					{
						sname: 'p-1' // 4
					},
					{
						sname: 'p-2' // 5
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 2
					},
					{
						it: 3,
						is: 4
					},
					{
						it: 4,
						is: 4
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 1
					},
					{
						ot: 2,
						os: 3
					},
					{
						ot: 4,
						os: 3
					},
					{
						ot: 3,
						os: 5
					}
				]
			};
			// const modelC2: PetriNet = {
			// 	"S": [
			// 		{
			// 			"sname": "p-1"
			// 		},
			// 		{
			// 			"sname": "p-2p-1"
			// 		},
			// 		{
			// 			"sname": "p-3p-3"
			// 		},
			// 		{
			// 			"sname": "p-2"
			// 		}
			// 	],
			// 	"I": [
			// 		{
			// 			"is": 1,
			// 			"it": 1
			// 		},
			// 		{
			// 			"is": 2,
			// 			"it": 2
			// 		},
			// 		{
			// 			"is": 2,
			// 			"it": 3
			// 		},
			// 		{
			// 			"is": 2,
			// 			"it": 4
			// 		}
			// 	],
			// 	"T": [
			// 		{
			// 			"tname": "t-1"
			// 		},
			// 		{
			// 			"tname": "t-2"
			// 		},
			// 		{
			// 			"tname": "t-1"
			// 		},
			// 		{
			// 			"tname": "t-2"
			// 		}
			// 	],
			// 	"O": [
			// 		{
			// 			"ot": 1,
			// 			"os": 2
			// 		},
			// 		{
			// 			"ot": 2,
			// 			"os": 1
			// 		},
			// 		{
			// 			"ot": 2,
			// 			"os": 3
			// 		},
			// 		{
			// 			"ot": 3,
			// 			"os": 4
			// 		},
			// 		{
			// 			"ot": 4,
			// 			"os": 3
			// 		}
			// 	]
			// }
			const commonStates = [{ modelA: 'p-3', modelB: 'p-3' }];
			console.log('Should match', modelC);

			// const commonStates = [
			// 	{ modelA: 'p-3', modelB: 'p-3' },
			// 	{ modelA: 'p-2', modelB: 'p-1' }
			// ];
			// console.log('Should match', modelC2);

			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/model-composition`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					modelA,
					modelB,
					commonStates // or commonStates2
				})
			});
			const output = await resp.json();
			console.log('Merged petrinet', output);
		},
		// Pulls model ID from form and sends model to createModel function for the actual work
		async drawModel() {
			const resp = await fetch(`http://localhost:8888/api/models/${this.loadModelID}/json`, {
				method: 'GET'
			});
			const model: PetriNet = await resp.json();
			this.createModel(model, false);
		},
		// Expects a JSON of a model with labels T, S, I, O.
		// populates g + depending on provided flag POST changes to model ID
		// This is mostly done for stratification testing. Will require a deeper look in future
		// TODO: We know there are race errors here. We intend to make this service stateless so we wont need to add Edges and Nodes individually
		async createModel(model: PetriNet, createFlag = false) {
			// Flag is true so we need to call API PUT new model ID
			if (createFlag === true) {
				const newModel = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
				const modelData = await newModel.json();
				modelId = modelData.id;
				console.log(`Model ID: ${modelId}`); // currently required for testing
			}

			// Reset current nodes and edges
			g.nodes = [];
			g.edges = [];

			const nodeHeight = 20;
			const nodeWidth = 20;
			let nodeX = 0;
			let nodeY = 0;
			// Nodes
			for (let i = 0; i < model.S.length; i++) {
				const aNode = model.S[i];
				nodeX += 30;
				nodeY += 30;
				this.addNode(
					`s-${i + 1}`,
					aNode.sname.toString(),
					nodeX,
					nodeY,
					nodeHeight,
					nodeWidth,
					NodeType.Species,
					createFlag
				);
			}
			// Move Transitions 100 to the right of S
			nodeX = 100;
			nodeY = 0;
			for (let i = 0; i < model.T.length; i++) {
				const aTransition = model.T[i];
				nodeX += 30;
				nodeY += 30;
				this.addNode(
					`t-${i + 1}`,
					aTransition.tname.toString(),
					nodeX,
					nodeY,
					nodeHeight,
					nodeWidth,
					NodeType.Transition,
					createFlag
				);
			} // end T

			// Edges
			for (let i = 0; i < model.I.length; i++) {
				const iEdges = model.I[i];
				const sourceID = `s-${iEdges.is}`;
				const transitionID = `t-${iEdges.it}`;
				this.addEdgeID(sourceID, transitionID, createFlag);
			}
			for (let i = 0; i < model.O.length; i++) {
				const oEdges = model.O[i];
				const sourceID = `s-${oEdges.os}`;
				const transitionID = `t-${oEdges.ot}`;
				this.addEdgeID(transitionID, sourceID, createFlag);
			}

			// g = runLayout(_.cloneDeep(g));
			this.refresh();
			this.jsonOutput();
		}, // end createModel
		async stratify() {
			try {
				const outputModel = await fetchStratificationResult(
					this.stratifyModelA,
					this.stratifyModelB,
					this.stratifyTypeModel
				);
				this.createModel(outputModel, true);
			} catch (e: any) {
				console.error(e.message);
			}
		},
		// Used to create sample models for stratifying tests
		// Will not be requried in the long run as we will be moving to storing these in DB
		async createSampleModels() {
			const SIRDModel: PetriNet = {
				T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
				S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
				I: [
					{ it: 1, is: 1 },
					{ it: 1, is: 2 },
					{ it: 2, is: 2 },
					{ it: 3, is: 2 }
				],
				O: [
					{ ot: 1, os: 2 },
					{ ot: 1, os: 2 },
					{ ot: 2, os: 3 },
					{ ot: 3, os: 4 }
				]
			};
			await this.createModel(SIRDModel, true);

			const QNotQModel: PetriNet = {
				T: [{ tname: 'quarantine' }, { tname: 'unquarantine' }],
				S: [{ sname: 'Q' }, { sname: 'NQ' }],
				I: [
					{ it: 1, is: 2 },
					{ it: 2, is: 1 }
				],
				O: [
					{ ot: 1, os: 1 },
					{ ot: 2, os: 2 }
				]
			};
			await this.createModel(QNotQModel, true);

			const typeModel: PetriNet = {
				T: [{ tname: 'infect' }, { tname: 'disease' }, { tname: 'strata' }],
				S: [{ sname: 'Pop' }],
				I: [
					{ it: 1, is: 1 },
					{ it: 1, is: 1 },
					{ it: 2, is: 1 },
					{ it: 3, is: 1 }
				],
				O: [
					{ ot: 1, os: 1 },
					{ ot: 1, os: 1 },
					{ ot: 2, os: 1 },
					{ ot: 3, os: 1 }
				]
			};
			await this.createModel(typeModel, true);
		}
	}
});
</script>
<template>
	<section class="playground">
		<p>A playground for testing TA2 API integrations.</p>
		<button type="button" @click="addPlace">Add place</button>
		<button type="button" @click="addTransition">Add transition</button>
		<button type="button" @click="createSampleModels">Create Models</button>
		&nbsp;
		<button type="button" @click="LotkaVolterra">LotkaVolterra</button>
		<button type="button" @click="simulate">Simulate</button>
		<button type="button" @click="mergePetrinets">Merge petrinets</button>
		<form>
			<label for="loadModel">
				<input v-model="loadModelID" type="text" placeholder="Model ID" />
			</label>
			<button type="button" @click="drawModel">Load Model</button>
		</form>
		<form>
			<label for="stratify">
				<input v-model="stratifyModelA" type="text" placeholder="Model A ID" />
				<input v-model="stratifyModelB" type="text" placeholder="Model B" />
				<input v-model="stratifyTypeModel" type="text" placeholder="Type Model" />
			</label>
			<button type="button" @click="stratify">Stratify</button>
		</form>
		<div style="display: flex">
			<div id="playground" class="playground-panel"></div>
			<div id="solution" class="playground-panel"></div>
			<div id="output" class="playground-panel"></div>
		</div>
	</section>
</template>

<style scoped>
.playground {
	margin: 10px;
}

.playground-panel {
	width: 500px;
	height: 500px;
	border: 1px solid #888;
}
</style>
