<template>
	<section>
		<div id="experiment" style="width: 800px; height: 500px"></div>
	</section>
</template>

<script setup lang="ts">
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { onMounted } from 'vue';
import * as d3 from 'd3';

import { runDagreLayout, D3SelectionINode, D3SelectionIEdge, pathFn } from '@/services/graph';

import * as _ from 'lodash';

interface NodeData {
	name: string;
}

interface EdgeData {
	val: number;
}

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [
		{ id: '0', label: '0', x: 0, y: 0, height: 80, width: 80, data: { name: 'abc' }, nodes: [] },
		{ id: '1', label: '1', x: 0, y: 0, height: 80, width: 80, data: { name: 'abc' }, nodes: [] },
		{ id: '2', label: '2', x: 0, y: 0, height: 80, width: 80, data: { name: 'abc' }, nodes: [] },
		{ id: '3', label: '3', x: 0, y: 0, height: 80, width: 80, data: { name: 'abc' }, nodes: [] }
	],
	edges: [
		{ id: '1', source: '0', target: '1', points: [], data: { val: 1 } },
		{ id: '2', source: '1', target: '2', points: [], data: { val: 1 } },
		{ id: '3', source: '2', target: '0', points: [], data: { val: 1 } },
		{ id: '4', source: '2', target: '3', points: [], data: { val: 1 } }
	]
};

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW_PATH = 'M 0,-3.25 L 5 ,0 L 0,3.25';

class ComparisonRenderer<V, E> extends graphScaffolder.BasicRenderer<V, E> {
	readonly EDGE_ARROW_ID = 'edge-arrowhead';

	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', this.EDGE_ARROW_ID)
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW_PATH)
			.style('fill', '#000')
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		selection
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#CCC');

		const cat10 = d3.schemeCategory10;
		selection.each((d, i, g2) => {
			if (Math.random() < 0.5) {
				for (let idx = 0; idx < 3; idx++) {
					const arcExample = d3
						.arc()
						.innerRadius(d.width * 0.5 + 5)
						.outerRadius(d.width * 0.5 + 15)
						.startAngle((idx * 2 * Math.PI) / 3)
						.endAngle(((idx + 1) * 2 * Math.PI) / 3)();

					d3.select(g2[i])
						.append('path')
						.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
						.attr('fill', cat10[idx])
						.attr('stroke', '#888')
						.attr('d', arcExample);
				}
			} else {
				for (let idx = 0; idx < 2; idx++) {
					const arcExample = d3
						.arc()
						.innerRadius(d.width * 0.5 + 5)
						.outerRadius(d.width * 0.5 + 15)
						.startAngle(idx * Math.PI)
						.endAngle((idx + 1) * Math.PI)();

					d3.select(g2[i])
						.append('path')
						.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
						.attr('fill', cat10[idx])
						.attr('stroke', '#888')
						.attr('d', arcExample);
				}
			}
		});
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 5)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
	}
}

onMounted(async () => {
	const experimentDiv = document.getElementById('experiment');
	console.log('experimentDiv', experimentDiv);
	const renderer = new ComparisonRenderer({
		el: experimentDiv as any,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	g = runDagreLayout(_.cloneDeep(g));
	console.log(g);
	await renderer.setData(g);
	await renderer.render();
});
</script>
