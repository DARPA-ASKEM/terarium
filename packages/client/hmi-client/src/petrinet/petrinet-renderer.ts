import * as d3 from 'd3';
import graphScaffolder, { INode, IEdge } from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { pointOnPath } from '@/utils/svg';
import { NodeData, EdgeData, NodeType } from './petrinet-service';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

const EDGE_COLOR = '#333333';
const EDGE_OPACITY = 0.5;

export class PetrinetRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
	nodeSelection: D3SelectionINode<NodeData> | null = null;

	edgeSelection: D3SelectionIEdge<EdgeData> | null = null;

	editMode: boolean = false;

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

	setEditMode(v: boolean) {
		this.editMode = v;

		const svg = d3.select(this.svgEl);
		if (this.editMode) {
			svg.style('background-color', '#EE8');
		} else {
			svg.style('background-color', null);
		}
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === 'S' || d.data.type === NodeType.State);
		const transitions = selection.filter(
			(d) => d.data.type === 'T' || d.data.type === NodeType.Transition
		);

		// transitions
		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5)
			.style('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2);

		// transitions drag handles
		const transitionsHandles = [
			...transitions
				.append('circle')
				.attr('cx', (d) => d.width)
				.attr('r', (d) => d.width * 0.2),
			...transitions
				.append('circle')
				.attr('cy', (d) => d.height)
				.attr('r', (d) => d.width * 0.2),
			...transitions
				.append('circle')
				.attr('cx', (d) => -d.width)
				.attr('r', (d) => d.width * 0.2),
			...transitions
				.append('circle')
				.attr('cy', (d) => -d.height)
				.attr('r', (d) => d.width * 0.2)
		];

		d3.selectAll(transitionsHandles)
			.classed('shape no-drag', true)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('cursor', 'pointer')
			.style('opacity', 0);

		// transitions text
		transitions
			.append('text')
			.attr('y', () => 4)
			.style('text-anchor', 'middle')
			.text((d) => d.label);

		// species
		species
			.append('circle')
			.classed('shape', true)
			.attr('r', (d) => 0.55 * d.width) // FIXME: need to adjust edge from sqaure mapping to circle
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2);

		// species drag handles
		const speciesHandles = [
			...species
				.append('circle')
				.attr('cx', (d) => d.width)
				.attr('r', (d) => d.width * 0.2),
			...species
				.append('circle')
				.attr('cy', (d) => d.height)
				.attr('r', (d) => d.width * 0.2),
			...species
				.append('circle')
				.attr('cx', (d) => -d.width)
				.attr('r', (d) => d.width * 0.2),
			...species
				.append('circle')
				.attr('cy', (d) => -d.height)
				.attr('r', (d) => d.width * 0.2)
		];

		d3.selectAll(speciesHandles)
			.classed('shape no-drag', true)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('cursor', 'pointer')
			.style('opacity', 0);

		// species text
		species
			.append('text')
			.attr('y', () => 2)
			.style('text-anchor', 'middle')
			.text((d) => d.label);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 2)
			.attr('marker-end', 'url(#arrowhead)');

		this.updateMultiEdgeLabels();
	}

	updateMultiEdgeLabels() {
		const selection = this.chart?.selectAll('.edge') as D3SelectionIEdge<EdgeData>;
		const multiEdges = selection.filter((d) => (d.data && d.data.numEdges > 1) as boolean);
		multiEdges.selectAll('.multi-edge-label').remove();

		multiEdges.each((_d, index, group) => {
			const edgeSelection = d3.select<any, IEdge<EdgeData>>(group[index]);
			const point = pointOnPath(edgeSelection.select('path').node() as any, 0.5);
			edgeSelection
				.append('text')
				.classed('multi-edge-label', true)
				.attr('x', point.x)
				.attr('y', point.y)
				.attr('stroke', null)
				.attr('fill', '#000')
				.style('font-size', 18)
				.text((d) => d.data?.numEdges as number);
		});
	}

	selectNode(selection: D3SelectionINode<NodeData>) {
		selection.selectAll('.no-drag').attr('stroke-width', 3);
	}

	deselectNode(selection: D3SelectionINode<NodeData>) {
		selection.selectAll('.no-drag').attr('stroke-width', 1);
	}

	selectEdge(selection: D3SelectionIEdge<EdgeData>) {
		selection.selectAll('path').style('stroke-width', 3);
	}

	deselectEdge(selection: D3SelectionIEdge<EdgeData>) {
		selection.selectAll('path').style('stroke-width', 2);
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

		// (Re)create dragging listeners
		this.on('node-drag-start', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			if (!this.isDragEnabled) return;
			sourceData = selection.datum();
			start.x = sourceData.x;
			start.y = sourceData.y;
		});

		this.on(
			'node-drag-move',
			(_eventName, event /* , _selection: D3SelectionINode<NodeData> */) => {
				this.updateMultiEdgeLabels();
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
					.style('stroke-dasharray', '4')
					.style('stroke-width', 3)
					.style('stroke', '#888');
			}
		);

		this.on('node-drag-end', (/* eventName, event, selection: D3SelectionINode<NodeData> */) => {
			chart?.selectAll('.new-edge').remove();
			if (!this.isDragEnabled) return;
			if (targetData && sourceData) {
				this.emit('add-edge', null, null, { target: targetData, source: sourceData });
				sourceData = null;
				targetData = null;
			}
		});

		this.on('node-click', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			if (!this.editMode) return;

			// hide any handles which are already open
			if (this.nodeSelection) {
				this.nodeSelection.selectAll('.no-drag').style('opacity', 0).style('visibility', 'hidden');
				this.deselectNode(this.nodeSelection);
			}
			if (this.edgeSelection) {
				this.deselectEdge(this.edgeSelection);
				this.edgeSelection = null;
			}

			selection
				.selectAll('.no-drag')
				.transition('ease-out')
				.duration(200)
				.style('opacity', 1)
				.style('visibility', 'visible');
			this.nodeSelection = selection;
			this.selectNode(this.nodeSelection);
		});

		this.on('edge-click', (_eventName, _event, selection: D3SelectionIEdge<EdgeData>) => {
			if (!this.editMode) return;

			if (this.edgeSelection) {
				this.deselectEdge(this.edgeSelection);
			}
			if (this.nodeSelection) {
				this.deselectNode(this.nodeSelection);
				this.nodeSelection.selectAll('.no-drag').style('opacity', 0).style('visibility', 'hidden');
				this.nodeSelection = null;
			}

			this.edgeSelection = selection;
			this.selectEdge(this.edgeSelection);
		});

		this.on('background-click', () => {
			if (this.edgeSelection) {
				this.deselectEdge(this.edgeSelection);
				this.edgeSelection = null;
			}
			if (this.nodeSelection) {
				this.deselectNode(this.nodeSelection);
				this.nodeSelection.selectAll('.no-drag').style('opacity', 0).style('visibility', 'hidden');
				this.nodeSelection = null;
			}
		});
	}

	getShapeOffset(node: any, angle: number) {
		switch (node.data.type) {
			case 'T': {
				// transitions -> squares
				return { x: node.x, y: node.y };
			}
			case 'S': {
				// species -> circles with multiplier
				const radius = node.width * 1.5;
				return { x: node.x + radius * Math.cos(angle), y: node.y + radius * Math.sin(angle) };
			}
			default:
				return { x: node.x, y: node.y };
		}
	}

	addNode(type: string, name: string, pos: { x: number; y: number }) {
		// FIXME: hardwired sizing
		const size = type === 'S' ? 60 : 30;
		this.graph.nodes.push({
			id: `s-${this.graph.nodes.length + 1}`,
			label: name,
			x: pos.x,
			y: pos.y,
			width: size,
			height: size,
			nodes: [],
			data: {
				type
			}
		});

		this.render();
	}

	addEdge(source: any, target: any) {
		// prevent nodes with same type from being linked with each other
		if (source.data.type === target.data.type) {
			return;
		}

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
				data: { numEdges: 1 }
			});
		}
		this.render();
	}

	// Test adding edge to one of four quadrants on the target
	addEdgeTest(source: any, target: any) {
		// prevent nodes with same type from being linked with each other
		if (source.data.type === target.data.type) {
			return;
		}

		let quadrant = 0;
		if (target.x - source.x < 0 && target.y - source.y > 0) {
			quadrant = 1;
		}
		if (target.x - source.x < 0 && target.y - source.y < 0) {
			quadrant = 2;
		}
		if (target.x - source.x > 0 && target.y - source.y < 0) {
			quadrant = 3;
		}

		const edgeVectorAngle =
			(quadrant * Math.PI) / 2 + Math.abs(Math.atan((target.y - source.y) / (target.x - source.x)));
		// console.log(edgeVectorAngle / 2 / Math.PI * 360);

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
					this.getShapeOffset(source, edgeVectorAngle),
					this.getShapeOffset(target, (edgeVectorAngle + Math.PI) % (Math.PI * 2))
				],
				data: { numEdges: 1 }
			});
		}
		this.render();
	}
}
