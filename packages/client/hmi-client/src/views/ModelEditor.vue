<template>
	<section>
		<div style="display: flex">
			<div id="playground"></div>
			<div id="parameters"></div>
		</div>
	</section>
</template>

<script setup lang="ts">
import * as d3 from 'd3';
import graphScaffolder from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { onMounted } from 'vue';
import { PetriNet } from '@/utils/petri-net-validator';
import { parsePetriNet2IGraph } from '@/services/model';

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
		div.append('input');
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
		div.append('input');
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

// Tracking variables
let source: any = null;
let target: any = null;

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
