<template>
	<section>
		<div id="playground"></div>
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

const emptyModel: PetriNet = { T: [], S: [], I: [], O: [] };

// Entry point
onMounted(async () => {
	const playground = document.getElementById('playground') as HTMLDivElement;
	const renderer: SampleRenderer = new SampleRenderer({
		el: playground,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	const g = parsePetriNet2IGraph(emptyModel);
	await renderer.setData(g);
});
</script>

<style></style>
