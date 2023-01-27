<template>
	<section>
		<div style="display: flex">
			<div id="playground"></div>
		</div>
	</section>
</template>

<script setup lang="ts">
/* eslint-disable */
import * as d3 from 'd3';
import graphScaffolder from '@graph-scaffolder/index';
import { moveTo } from '@graph-scaffolder/fn/move-to';
import { runDagreLayout2, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { onMounted, ref } from 'vue';
import { parsePetriNet2IGraph } from '@/services/model';

const variablesRef = ref<any[]>([]);

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

class SampleRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
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
			.attr('fill', '#333333');
		species
			.append('svg:image')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('xlink:href', (d) => `/app/src/assets/images/${d.label}_thumb.png`)
			.on('error', function () {
				// set transparent empty svg image
				(this as any).setAttribute(
					'href',
					'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=='
				);
			});

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
		(this as any).graph.edges.push({
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
		(this as any).render();
	}
}

let renderer: any = null;

// const emptyModel: PetriNet = { T: [], S: [], I: [], O: [] };

// Entry point
onMounted(async () => {
	const playground = document.getElementById('playground') as HTMLDivElement;
	renderer = new (SampleRenderer as any)({
		el: playground,
		useAStarRouting: true,
		runLayout: runDagreLayout2,
		useStableZoomPan: true
	});

	try {
		const simInputDataString = JSON.parse(localStorage.getItem('sim-input-data') as string);
		const g = parsePetriNet2IGraph(simInputDataString);
		await renderer.setData(g);
		await renderer.render();
	} catch (e) {
		throw Error();
	}

	renderer.graph.nodes.forEach((n) => {
		variablesRef.value.push({
			id: n.id,
			name: n.label,
			type: n.data.type,
			description: '',
			concept: '',
			value: 0
		});
	});

	renderer.on('node-click', (_evtName, _evt, _renderer, d) => {
		moveTo(d, _renderer, 0);
		const svg = d3.select(d.svgEl);
		d3.zoom().scaleTo(svg.transition(), 20);
	});
});
</script>

<style>
#playground {
	width: 1000px;
	height: 700px;
	border: 1px solid #bbb;
}
</style>
