<template>
	<section>
		<div style="display: flex">
			<div id="playground"></div>
			<div id="parameters"></div>
		</div>
		<Button @click="runPetri()">Run simulation</Button>
	</section>
</template>

<script setup lang="ts">
import * as d3 from 'd3';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { onMounted } from 'vue';
import { PetriNet } from '@/utils/petri-net-validator';
import { parsePetriNet2IGraph } from '@/services/model';
import Button from 'primevue/button';

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

const pathFn = d3
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
			.style('fill', '#89BEFF')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.8)
			.attr('fill', '#FF90A9');

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

	addEdge(source: any, target: any) {
		this.graph.edges.push({
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
		this.render();
	}
}

let renderer: SampleRenderer | null = null;

const renderParameterInput = () => {
	const el = d3.select('#parameters');
	el.selectAll('*').remove();

	if (!renderer) return;

	const graph = renderer.graph;

	el.append('div').text('Species');
	const speciesEl = el.append('div').style('display', 'flex').style('flex-direction', 'column');

	const speciesNodes = graph.nodes.filter((d) => d.data.type === 'S');
	for (let i = 0; i < speciesNodes.length; i++) {
		const node = speciesNodes[i];
		const div = speciesEl.append('div').style('display', 'flex');
		div
			.append('div')
			.style('width', '80px')
			.style('text-align', 'end')
			.style('padding-right', '5px')
			.text(node.label);
		div
			.append('input')
			.datum({ name: node.label })
			.classed('initial-value', true)
			.property('value', 0);
	}

	el.append('hr');

	el.append('div').text('Transitions');
	const transitionsEL = el.append('div').style('display', 'flex').style('flex-direction', 'column');
	const transitionsNodes = graph.nodes.filter((d) => d.data.type === 'T');
	for (let i = 0; i < transitionsNodes.length; i++) {
		const node = transitionsNodes[i];
		const div = transitionsEL.append('div').style('display', 'flex');
		div
			.append('div')
			.style('width', '80px')
			.style('text-align', 'end')
			.style('padding-right', '5px')
			.text(node.label);
		div.append('input').datum({ name: node.label }).classed('parameter', true).property('value', 0);
	}
};

// const emptyModel: PetriNet = { T: [], S: [], I: [], O: [] };

// SIRD
const SIRD: PetriNet = {
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

const graph2petri = (graph: IGraph<NodeData, EdgeData>) => {
	const petri: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};

	const nodes = graph.nodes;
	for (let i = 0; i < nodes.length; i++) {
		const node = nodes[i];
		if (node.data.type === 'S') {
			petri.S.push({ sname: node.id });
		} else {
			petri.T.push({ tname: node.id });
		}
	}

	const edges = graph.edges;
	for (let i = 0; i < edges.length; i++) {
		const edge = edges[i];
		const source = edge.source;
		const target = edge.target;

		const S = petri.S.map((s) => s.sname);
		const T = petri.T.map((t) => t.tname);

		if (S.includes(source)) {
			const is = S.indexOf(source) + 1;
			const it = T.indexOf(target) + 1;
			petri.I.push({ is, it });
		} else {
			const ot = T.indexOf(source) + 1;
			const os = S.indexOf(target) + 1;
			petri.O.push({ os, ot });
		}
		// console.log('!!', edge);
	}

	petri.S = [];
	petri.T = [];

	for (let i = 0; i < nodes.length; i++) {
		const node = nodes[i];
		if (node.data.type === 'S') {
			petri.S.push({ sname: node.label });
		} else {
			petri.T.push({ tname: node.label });
		}
	}
	return petri;
};

// console.log(graph2petri(parsePetriNet2IGraph(SIRD)));

// Tracking variables
let source: any = null;
let target: any = null;

const runPetri = () => {
	const parameterData = d3.selectAll('.parameter');
	const initData = d3.selectAll('.initial-value');

	const parameters = {};
	const initials = {};

	parameterData.each((d: any, i, g) => {
		const value = (d3.select(g[i]).node() as HTMLInputElement).value;
		parameters[d.name] = +value;
	});
	initData.each((d: any, i, g) => {
		const value = (d3.select(g[i]).node() as HTMLInputElement).value;
		initials[d.name] = +value;
	});

	const payload = {
		initial_values: initials,
		parameters
	};

	const final = {
		petri: graph2petri(renderer?.graph as any),
		payload
	};

	console.log(final);
	console.log(JSON.stringify(final));

	return final;
};

// Entry point
onMounted(async () => {
	const playground = document.getElementById('playground') as HTMLDivElement;
	renderer = new SampleRenderer({
		el: playground,
		useAStarRouting: true,
		runLayout: runDagreLayout,
		useStableZoomPan: true
	});

	renderer.on('background-click', () => {
		source = null;
		target = null;
		renderer?.render();
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
			renderer.addEdge(source, target);
			source = null;
			target = null;
		}
	});

	const g = parsePetriNet2IGraph(SIRD);
	await renderer.setData(g);
	await renderer.render();
	renderParameterInput();
});
</script>

<style>
#playground {
	width: 1000px;
	height: 400px;
	border: 1px solid #bbb;
}
</style>
