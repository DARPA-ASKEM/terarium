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
			.attr('fill', '#333333');
		species
			.append('svg:image')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('xlink:href', (d) => `/app/src/assets/images/${d.label}_thumb.png`)
			.on('error', function () {
				// set transparent empty svg image
				this.setAttribute(
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

// const emptyModel: PetriNet = { T: [], S: [], I: [], O: [] };

// Entry point
onMounted(async () => {
	const playground = document.getElementById('playground') as HTMLDivElement;
	renderer = new SampleRenderer({
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
		throw Error(e);
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
		moveTo(d, _renderer);
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
