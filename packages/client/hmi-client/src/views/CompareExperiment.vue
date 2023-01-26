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
	models: string[];
}

interface EdgeData {
	val: number;
}

const dataStr = `{"template_models": {"0": {"templates": [{"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}}, "outcome": {"name": "immune_population", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}, "1": {"templates": [{"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"location": "geonames:4930956"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"location": "geonames:4930956"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"location": "geonames:4930956"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}}, "outcome": {"name": "immune_population", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}}, "nodes": {"m0n0": {"node_type": "template", "model_id": 0, "type": "ControlledConversion", "rate_law": null, "initials": null, "provenance": []}, "m0n1": {"node_type": "concept", "model_id": 0, "name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}, "curie": "ido:0000511"}, "m0n2": {"node_type": "concept", "model_id": 0, "name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {}, "curie": "ido:0000514"}, "m0n3": {"node_type": "template", "model_id": 0, "type": "NaturalConversion", "rate_law": null, "initials": null, "provenance": []}, "m0n4": {"node_type": "concept", "model_id": 0, "name": "immune_population", "identifiers": {"ido": "0000592"}, "context": {}, "curie": "ido:0000592"}, "m1n0": {"node_type": "template", "model_id": 1, "type": "ControlledConversion", "rate_law": null, "initials": null, "provenance": []}, "m1n1": {"node_type": "concept", "model_id": 1, "name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"location": "geonames:4930956"}, "curie": "ido:0000511"}, "m1n2": {"node_type": "concept", "model_id": 1, "name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"location": "geonames:4930956"}, "curie": "ido:0000514"}, "m1n3": {"node_type": "template", "model_id": 1, "type": "NaturalConversion", "rate_law": null, "initials": null, "provenance": []}, "m1n4": {"node_type": "concept", "model_id": 1, "name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {}, "curie": "ido:0000511"}, "m1n5": {"node_type": "concept", "model_id": 1, "name": "immune_population", "identifiers": {"ido": "0000592"}, "context": {}, "curie": "ido:0000592"}}, "inter_model_edges": [["m1n0", "m0n0", "refinement_of"], ["m1n1", "m0n1", "refinement_of"], ["m0n1", "m1n4", "is_equal"], ["m1n2", "m0n2", "refinement_of"], ["m0n3", "m1n3", "is_equal"], ["m0n4", "m1n5", "is_equal"]], "intra_model_edges": [["m0n1", "m0n0", "controller"], ["m0n2", "m0n0", "subject"], ["m0n0", "m0n1", "outcome"], ["m0n1", "m0n3", "subject"], ["m0n3", "m0n4", "outcome"], ["m1n1", "m1n0", "controller"], ["m1n2", "m1n0", "subject"], ["m1n0", "m1n1", "outcome"], ["m1n4", "m1n3", "subject"], ["m1n3", "m1n5", "outcome"]]}`;

const data = JSON.parse(dataStr);

console.log(data.intra_model_edges);

// Consume equalities
const equalPools: string[][] = [];

for (let i = 0; i < data.inter_model_edges.length; i++) {
	const edge = data.inter_model_edges[i];
	if (edge[2] === 'is_equal') {
		const pool = equalPools.find((p) => p.includes(edge[0]) || p.includes(edge[1]));
		if (!pool) {
			equalPools.push([edge[0], edge[1]]);
		} else {
			pool.push(edge[0]);
			pool.push(edge[1]);
		}
	}
}
console.log(equalPools);
// FIXME

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [],
	edges: []
	/*
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
	*/
};

const nodeIds = Object.keys(data.nodes);

for (let i = 0; i < nodeIds.length; i++) {
	const nodeId = nodeIds[i];
	const pool = equalPools.find((p) => p.includes(nodeId));

	if (!pool) {
		const node = data.nodes[nodeId];
		g.nodes.push({
			id: nodeId,
			label: nodeId,
			x: 0,
			y: 0,
			height: 80,
			width: 80,
			data: { name: nodeId, models: [node.model_id] },
			nodes: []
		});
	} else {
		const surrogateId = pool[0];
		const node = data.nodes[nodeId];
		if (!g.nodes.find((d) => d.id === surrogateId)) {
			g.nodes.push({
				id: surrogateId,
				label: surrogateId,
				x: 0,
				y: 0,
				height: 80,
				width: 80,
				data: { name: surrogateId, models: [] },
				nodes: []
			});
		}
		g.nodes.find((d) => d.id === surrogateId)?.data.models.push(node.model_id);
	}
}

console.log(
	'!!!',
	g.nodes.map((d) => d.data)
);

for (let i = 0; i < data.intra_model_edges.length; i++) {
	const edge = data.intra_model_edges[i];
	let source = edge[0];
	let target = edge[1];

	const sourcePool = equalPools.find((pool) => pool.includes(source));
	const targetPool = equalPools.find((pool) => pool.includes(target));

	if (sourcePool) {
		source = sourcePool[0];
	}
	if (targetPool) {
		target = targetPool[0];
	}

	g.edges.push({
		id: `${i}`,
		source,
		target,
		points: [],
		data: { val: 1 }
	});
}

console.log(g.edges);

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

		selection
			.append('text')
			.attr('y', 30)
			.attr('x', 60)
			.text((d) => `${d.id}:${d.data.models}`);

		const cat10 = d3.schemeCategory10;
		selection.each((d, i, g2) => {
			if (d.data.models.length > 1) {
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
			}
			// else {
			// 	for (let idx = 0; idx < 2; idx++) {
			// 		const arcExample = d3
			// 			.arc()
			// 			.innerRadius(d.width * 0.5 + 5)
			// 			.outerRadius(d.width * 0.5 + 15)
			// 			.startAngle(idx * Math.PI)
			// 			.endAngle((idx + 1) * Math.PI)();
			// 		d3.select(g2[i])
			// 			.append('path')
			// 			.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
			// 			.attr('fill', cat10[idx])
			// 			.attr('stroke', '#888')
			// 			.attr('d', arcExample);
			// 	}
			// }
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
	const renderer = new ComparisonRenderer({
		el: experimentDiv as any,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	g = runDagreLayout(_.cloneDeep(g));
	await renderer.setData(g);
	await renderer.render();
});
</script>
