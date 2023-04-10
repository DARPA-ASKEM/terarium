<template>
	<section>
		<h4>Workflow Experiment</h4>
		<div class="experiment-container" ref="graphElement"></div>
		<ContextMenu ref="menu" :model="contextMenuItems" />
	</section>
	<Teleport to="body">
		<ForecastLauncher
			v-if="showForecastLauncher && currentModel"
			:model="currentModel"
			@close="showForecastLauncher = false"
			@launch-forecast="saveSimulation"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import * as d3 from 'd3';
import { PetriNet } from '@/petrinet/petrinet-service';
import { ITypedModel } from '@/types/Model';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { BasicRenderer, IGraph, INode } from '@graph-scaffolder/index';
import ContextMenu from 'primevue/contextmenu';
import ForecastLauncher from '@/components/models/forecast-launcher.vue';
import { getRunStatus, getRunResult } from '@/services/models/simulation-service';

import { getModel } from '@/services/model';
import { parse } from 'path';

// SIR and SVIIvR
const MODEL_LIST = [2, 3];

interface NodeData {
	type: string;
	val: number;
	model?: ITypedModel<PetriNet>;
	forecastId?: any;
}

interface EdgeData {
	val: number;
}

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const EDGE_COLOR = 'var(--petri-lineColor)';
const EDGE_OPACITY = 0.5;
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

let counter = 0;
let forecastSelection: any = null;

class WorkflowRenderer extends BasicRenderer<NodeData, EdgeData> {
	parentNodes(id: any) {
		const edges = this.graph.edges.filter((d) => d.target === id);
		console.log('debug', edges);
		const sources = edges.map((e) => e.source);
		console.log('debug', sources);
		const nodes = this.graph.nodes.filter((d) => sources.includes(d.id));
		return nodes;
	}

	childrenNodes(id: any) {
		const edges = this.graph.edges.filter((d) => d.source === id);
		const targets = edges.map((e) => e.target);
		const nodes = this.graph.nodes.filter((d) => targets.includes(d.id));
		return nodes;
	}

	setupDefs() {
		const svg = d3.select(this.svgEl);
		svg.select('defs').selectAll('.edge-marker-end').remove();
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', 'arrowhead')
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 6)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 20)
			.attr('markerHeight', 20)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW)
			.style('fill', EDGE_COLOR)
			.style('fill-opacity', EDGE_OPACITY)
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		selection
			.append('rect')
			.attr('x', (d) => -d.width * 0.5)
			.attr('y', (d) => -d.height * 0.5)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('stroke', '#888')
			.attr('fill', '#EEE');

		selection
			.append('text')
			.attr('x', (d) => -d.width * 0.4)
			.text((d) => d.data.type);

		selection
			.append('circle')
			.attr('class', 'no-drag')
			.attr('cx', (d) => d.width * 0.5 + 10)
			.attr('cy', 0)
			.attr('r', 8)
			.attr('fill', '#345')
			.style('visibility', 'visible');
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 1)
			.attr('marker-end', 'url(#arrowhead)');
	}

	addNodeViz(pos: { x: number; y: number }) {
		this.graph.nodes.push({
			id: `s-${this.graph.nodes.length + 1}`,
			label: name,
			x: pos.x,
			y: pos.y,
			width: 200,
			height: 90,
			nodes: [],
			data: { type: 'visualization', val: 1 }
		});
		this.render();
	}

	addNodeModel(model: ITypedModel<PetriNet>, pos: { x: number; y: number }) {
		// FIXME: hardwired sizing
		this.graph.nodes.push({
			id: `s-${this.graph.nodes.length + 1}`,
			label: name,
			x: pos.x,
			y: pos.y,
			width: 100,
			height: 50,
			nodes: [],
			data: { type: 'model', val: 1, model: model }
		});
		this.render();
	}

	addNode(type: string, name: string, pos: { x: number; y: number }) {
		// FIXME: hardwired sizing
		this.graph.nodes.push({
			id: `s-${this.graph.nodes.length + 1}`,
			label: name,
			x: pos.x,
			y: pos.y,
			width: 100,
			height: 50,
			nodes: [],
			data: { type: type, val: 1 }
		});

		this.render();
	}

	addEdge(source: any, target: any) {
		const existingEdge = this.graph.edges.find(
			(edge) => edge.source === source.id && edge.target === target.id
		);
		if (existingEdge && existingEdge.data) {
			existingEdge.data.numEdges++;
		} else {
			this.graph.edges.push({
				id: `${source.id}_${target.id}`,
				source: source.id,
				target: target.id,
				points: [
					{ x: source.x, y: source.y },
					{ x: target.x, y: target.y }
				],
				data: { val: 1 }
			});
		}
		this.render();
	}

	postRenderProcess() {
		const chart = this.chart;
		const svg = d3.select(this.svgEl);
		const start: { x: number; y: number } = { x: 0, y: 0 };
		const end: { x: number; y: number } = { x: 0, y: 0 };
		let sourceData: INode<NodeData> | null = null;
		let targetData: INode<NodeData> | null = null;

		// Reset all
		this.removeAllEvents('node-drag-start');
		this.removeAllEvents('node-drag-move');
		this.removeAllEvents('node-drag-end');
		this.removeAllEvents('node-drag-end');

		// (Re)create dragging listeners
		this.on('node-drag-start', (_eventName, event, selection: D3SelectionINode<NodeData>) => {
			if (!this.isDragEnabled) return;
			sourceData = selection.datum();
			start.x = sourceData.x;
			start.y = sourceData.y;

			const targetSelection = d3.select(event.sourceEvent.target);
			start.x += +targetSelection.attr('cx');
			start.y += +targetSelection.attr('cy');
		});

		this.on('node-drag-move', (_eventName, event) => {
			if (!this.isDragEnabled) return;
			const pointerCoords = d3
				.zoomTransform(svg.node() as Element)
				.invert(d3.pointer(event, svg.node()));
			targetData = d3.select<SVGGElement, INode<NodeData>>(event.sourceEvent.target).datum();
			if (targetData) {
				end.x = targetData.x;
				end.y = targetData.y;
			} else {
				end.x = pointerCoords[0];
				end.y = pointerCoords[1];
			}
			chart?.selectAll('.new-edge').remove();

			const line = [
				{ x: start.x, y: start.y },
				{ x: end.x, y: end.y }
			];
			chart
				?.append('path')
				.classed('new-edge', true)
				.attr('d', pathFn(line))
				.attr('marker-end', 'url(#arrowhead)')
				.style('stroke-width', 3)
				.style('stroke', 'var(--primary-color)');
		});

		this.on('node-drag-end', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			chart?.selectAll('.new-edge').remove();

			if (!this.isDragEnabled) return;
			if (targetData && sourceData) {
				this.emit('add-edge', null, null, { target: targetData, source: sourceData });
				sourceData = null;
				targetData = null;
			}
		});

		this.on('node-click', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			console.log(selection.datum().data.type);
			const nodeType = selection.datum().data.type;
			if (nodeType === 'forecast') {
				const parentData = this.parentNodes(selection.datum().id);
				if (parentData) {
					currentModel.value = parentData[0].data.model;
					showForecastLauncher.value = true;
					forecastSelection = selection;
				}
			}
		});
	}
}

const WORKFLOW: IGraph<NodeData, EdgeData> = {
	nodes: [],
	edges: [],
	width: 1200,
	height: 500
};

const graphElement = ref<HTMLDivElement | null>(null);
const menu = ref();
let renderer: WorkflowRenderer | null = null;
let eventX = 0;
let eventY = 0;

const currentModel = ref<ITypedModel<PetriNet> | null>(null);
const showForecastLauncher = ref(false);

const run = async () => {
	renderer = new WorkflowRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});
	const x = runDagreLayout(WORKFLOW);
	await renderer.setData(x);
	await renderer.render();

	renderer.on('background-contextmenu', (_evtName, evt, _selection, _renderer, pos: any) => {
		eventX = pos.x;
		eventY = pos.y;
		menu.value.show(evt);
	});

	renderer.on('background-click', () => {
		if (menu.value) menu.value.hide();
	});

	renderer.on('add-edge', (_evtName, _evt, _selection, d) => {
		renderer?.addEdge(d.source, d.target);
	});
};

// Model editor context menu
const contextMenuItems = ref([
	{
		label: 'Add model',
		icon: 'pi pi-fw pi-circle',
		command: async () => {
			const model = await getModel(MODEL_LIST[counter % 2] + '');
			console.log('[add model]', model);
			counter++;

			if (renderer && model) {
				renderer.addNodeModel(model, { x: eventX, y: eventY });
			}
		}
	},
	{
		label: 'Add forecast',
		icon: 'pi pi-fw pi-stop',
		command: () => {
			if (renderer) {
				renderer.addNode('forecast', '?', { x: eventX, y: eventY });
			}
		}
	},
	{
		label: 'Add visualization',
		icon: 'pi pi-fw pi-eye',
		command: () => {
			if (renderer) {
				renderer.addNodeViz({ x: eventX, y: eventY });
			}
		}
	}
]);

const renderVisNode = async (nodeId: string, runId: number) => {
	console.log('render vis node', nodeId, runId);
	if (!renderer) return;

	const nodeUI = renderer.chart.selectAll('.node-ui').filter((d) => d.id === nodeId);
	nodeUI.selectAll('text').remove();
	nodeUI.selectAll('.temp').remove();

	setTimeout(async () => {
		const resultCSV = await getRunResult(runId);
		const parsedCSV = d3.csvParse(resultCSV);
		const temp = nodeUI.append('g').classed('temp', true);

		parsedCSV.forEach((d, i) => {
			const key = Object.keys(d)[2];
			const value = +(d[key] as string);
			console.log(key, value);
			temp
				.append('circle')
				.attr('cx', i * 2 - 0.5 * nodeUI.datum().width)
				.attr('cy', 80 - value - 0.5 * nodeUI.datum().height)
				.attr('r', 2)
				.attr('fill', 'red');
		});
	}, 1000);
};

const saveSimulation = (runId: number) => {
	showForecastLauncher.value = false;
	forecastSelection.datum().data.forecastId = runId;
	if (renderer) {
		const childrenNodes = renderer.childrenNodes(forecastSelection.datum().id);
		if (childrenNodes) {
			for (let i = 0; i < childrenNodes.length; i++) {
				const nodeId = childrenNodes[i].id;
				renderVisNode(nodeId, runId);
			}
		}
	}
};

// Entry point
onMounted(() => {
	run();
});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
}
.experiment-container {
	margin: 10px;
	width: 1200px;
	height: 500px;
	border: 1px solid #888;
	box-sizing: border-box;
}
</style>
