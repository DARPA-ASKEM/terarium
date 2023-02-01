<template>
	<section>
		<div id="experiment" style="width: 1500px; height: 960px"></div>
	</section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import * as d3 from 'd3';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge, pathFn } from '@/services/graph';
import comaprisonData from '@/views/simulation-run-data/comp-not-stratified.json';
import * as _ from 'lodash';

interface NodeData {
	name: string;
	displayName?: string;
	templateName?: string;
	models: string[];
}

interface EdgeData {
	val: number;
}

const palette = [
	'#89BEFF',
	'#B0E6D9',
	'#FDE267',
	'#FF90A9',
	'#B49CFD',
	'#FEAB79',
	'#78DDED',
	'#78E3AF',

	// New stuff
	'#F00',
	'#0F0',
	'#00F'
];

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [],
	edges: []
};

const data = comaprisonData.graph_comparison_data;

// Create modelMap
const modelsMap = {}; // data.nodes;

const templateKeys = Object.keys(data.template_nodes);
templateKeys.forEach((key) => {
	if (modelsMap[key]) {
		const nodeKeys = Object.keys(data.template_nodes[key]);
		nodeKeys.forEach((key2) => {
			modelsMap[key][key2] = data.template_nodes[key][key2];
		});
	} else {
		modelsMap[key] = data.template_nodes[key];
	}
});

const conceptKeys = Object.keys(data.concept_nodes);
conceptKeys.forEach((key) => {
	if (modelsMap[key]) {
		const nodeKeys = Object.keys(data.concept_nodes[key]);
		nodeKeys.forEach((key2) => {
			modelsMap[key][key2] = data.concept_nodes[key][key2];
		});
	} else {
		modelsMap[key] = data.concept_nodes[key];
	}
});

const interModelEdges = data.inter_model_edges;
const intraModelEdges = data.intra_model_edges;

const equalPools: string[][] = [];

// Find equality nodes, create a list-of-list of all nodes thare are equivalent, note we will
// double count, if a = b and b = c, we will get [a, b, b, c]
interModelEdges.forEach((edge) => {
	const [[sourceModelId, sourceNodeId], [targetModelId, targetNodeId], relation] = edge;
	const sourceHash = `${sourceModelId}:${sourceNodeId}`;
	const targetHash = `${targetModelId}:${targetNodeId}`;

	if (relation === 'is_equal') {
		const pool = equalPools.find((p) => p.includes(sourceHash) || p.includes(targetHash));
		if (!pool) {
			equalPools.push([sourceHash, targetHash]);
		} else {
			pool.push(sourceHash);
			pool.push(targetHash);
		}
	}
});

// Create nodes, segment nodes into nodes that are specific to a model, and nodes that are shared across models
const modelKeys = Object.keys(modelsMap);
modelKeys.forEach((modelKey) => {
	const nodeKeys = Object.keys(modelsMap[modelKey]);
	const modelGroup = {
		id: modelKey,
		label: 'p',
		data: { name: modelKey, models: [] },
		nodes: []
	};

	nodeKeys.forEach((nodeKey) => {
		const hash = `${modelKey}:${nodeKey}`;
		const pool = equalPools.find((p) => p.includes(hash));

		// console.log(modelsMap[modelKey][nodeKey]);

		const currentNode = modelsMap[modelKey][nodeKey];

		if (!pool) {
			modelGroup.nodes.push({
				id: hash,
				label: hash,
				x: 0,
				y: 0,
				height: 80,
				width: 80,
				data: {
					name: hash,
					displayName: currentNode.name || '',
					templateName: currentNode.type || '',
					models: [modelKey]
				},
				nodes: []
			});
		} else {
			const surrogateId = pool[0];
			if (!g.nodes.find((d) => d.id === surrogateId)) {
				g.nodes.push({
					id: surrogateId,
					label: surrogateId,
					x: 0,
					y: 0,
					height: 80,
					width: 80,
					data: {
						name: surrogateId,
						displayName: currentNode.name || '',
						templateName: currentNode.type || '',
						models: []
					},
					nodes: []
				});
			}

			const n = g.nodes.find((d) => d.id === surrogateId) as any;
			if (!n.data.models.includes(modelKey)) {
				n.data.models.push(modelKey);
			}
		}
	});
	if (modelGroup.nodes.length > 0) {
		g.nodes.push(modelGroup);
	}
});

let eCounter = 0;
intraModelEdges.forEach((edge) => {
	const [[sourceModelId, sourceNodeId], [targetModelId, targetNodeId]] = edge;
	let sourceHash = `${sourceModelId}:${sourceNodeId}`;
	let targetHash = `${targetModelId}:${targetNodeId}`;

	const sourcePool = equalPools.find((p) => p.includes(sourceHash));
	const targetPool = equalPools.find((p) => p.includes(targetHash));

	if (sourcePool) sourceHash = sourcePool[0];
	if (targetPool) targetHash = targetPool[0];

	g.edges.push({
		id: `${eCounter}`,
		source: sourceHash,
		target: targetHash,
		points: [],
		data: { val: 1 }
	});
	eCounter++;
});

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
		const children = selection.filter((d) => d.nodes.length === 0);
		const conceptNodes = children.filter((d) => d.data.displayName?.length > 0);
		const templateNodes = children.filter((d) => d.data.displayName?.length === 0);

		conceptNodes
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5 - 10)
			.attr('stroke', '#888')
			.attr('fill', (d) => {
				if (d.data.models.length > 1) {
					return '#DDD';
				}
				return palette[d.data.models[0]];
			});

		templateNodes
			.append('rect')
			.classed('shape', true)
			.attr('x', 10)
			.attr('y', 10)
			.attr('width', (d) => d.width - 20)
			.attr('height', (d) => d.height - 20)
			.style('stroke', '#222')
			.style('fill', 'transparent');

		// const cat10 = palette;
		children.each((d, i, g2) => {
			if (d.data.models.length === 1) return;

			const len = d.data.models.length;

			const innerR = d.data.displayName?.length > 0 ? 5 : 5;
			const outerR = d.data.displayName?.length > 0 ? 25 : 8;

			for (let idx = 0; idx < len; idx++) {
				const arcExample = d3
					.arc()
					.innerRadius(d.width * 0.5 + innerR)
					.outerRadius(d.width * 0.5 + outerR)
					.startAngle((idx * 2 * Math.PI) / len)
					.endAngle(((idx + 1) * 2 * Math.PI) / len)();

				d3.select(g2[i])
					.append('path')
					.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
					.attr('fill', palette[d.data.models[idx]])
					.attr('stroke', '#888')
					.attr('d', arcExample);
			}
		});

		conceptNodes
			.append('text')
			.attr('x', 0)
			.attr('y', -10)
			.style('font-size', 50)
			.style('text-anchor', 'middle')
			.text((d) => `${d.data.displayName}`);

		templateNodes
			.append('text')
			.attr('x', -20)
			.attr('y', 30)
			.style('font-size', 12)
			.text((d) => `${d.data.templateName}`);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#bbb')
			.style('stroke-width', 3)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
	}

	renderLegend() {
		for (let i = 0; i < comaprisonData.model_names.length; i++) {
			this.chart
				.append('rect')
				.attr('x', -145)
				.attr('y', 16 + i * 60)
				.attr('width', 20)
				.attr('height', 20)
				.attr('fill', palette[i]);

			this.chart
				.append('text')
				.attr('x', -120)
				.attr('y', 30 + i * 60)
				.style('font-size', 32)
				.text(comaprisonData.model_names[i]);
			console.log('!!!!!!!!!', comaprisonData.model_names[i]);
		}
	}
}

// Entry
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
	renderer.renderLegend();
});
</script>
