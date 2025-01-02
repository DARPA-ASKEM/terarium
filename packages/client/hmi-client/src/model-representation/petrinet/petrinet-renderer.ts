import * as d3 from 'd3';
import { isEmpty } from 'lodash';
import { BasicRenderer, INode, IEdge } from '@graph-scaffolder/index';
import type { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { NodeType } from '@/services/graph';
import { pointOnPath, partialPath } from '@/utils/svg';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';

export interface NodeData {
	type: string;
	expression?: string;
	strataType?: string;
	isStratified?: boolean;
	references?: string[];
}

export interface EdgeData {
	numEdges: number;
	isController?: boolean;
	isObservable?: boolean;
}

const FONT_SIZE_SMALL = 20;
const FONT_SIZE_REGULAR = 24;

function setFontSize(label: string) {
	if (label.length < 3) {
		return FONT_SIZE_REGULAR;
	}
	return FONT_SIZE_SMALL;
}

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

const TEXT_COLOR = 'rgb(16, 24, 40)';
const EDGE_COLOR = '#616161';
const NODE_COLOR = '#E0E0E0';
const HIGHLIGHTEDSTROKECOLOUR = '#1B8073';
const EDGE_OPACITY = 0.5;

const { getNodeTypeColor } = useNodeTypeColorPalette();

const RENDER_EDGE_OFFSET = 15;
const adjustedPathPoints = (pathNode: SVGPathElement, offset: number) => {
	const len = pathNode.getTotalLength();
	const end = len - offset;
	return partialPath(pathNode, 0, end, 20);
};

export class PetrinetRenderer extends BasicRenderer<NodeData, EdgeData> {
	nodeSelection: D3SelectionINode<NodeData> | null = null;

	edgeSelection: D3SelectionIEdge<EdgeData> | null = null;

	draggingNode: D3SelectionINode<NodeData> | null = null;

	initialize(element: HTMLDivElement): void {
		super.initialize(element);

		d3.select(this.svgEl)
			.style('border', '4px solid transparent')
			.style('background', '#ffffff')
			.style('border-radius', '4px 0px 0px 4px');
	}

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
			.attr('refX', 0)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 30)
			.attr('markerHeight', 30)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW)
			.style('fill', EDGE_COLOR)
			.style('fill-opacity', EDGE_OPACITY)
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === NodeType.State);
		const transitions = selection.filter((d) => d.data.type === NodeType.Transition);
		const observables = selection.filter((d) => d.data.type === NodeType.Observable);

		// species
		species
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', (d) => 0.55 * d.width)
			.attr('fill', (d) => (d.data.strataType ? getNodeTypeColor(d.data.strataType) : NODE_COLOR))
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

		// species text
		species
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.text((d) => d.id);

		// transitions
		const transitionRadius = 5;
		transitions
			.append('path')
			.attr('d', (d) =>
				pathFn([
					{ x: -0.5 * d.width, y: 0 },
					{ x: -(transitionRadius + 1), y: 0 }
				])
			)
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 3);
		transitions
			.append('path')
			.attr('d', (d) =>
				pathFn([
					{ x: transitionRadius + 1, y: 0 },
					{ x: 0.5 * d.width, y: 0 }
				])
			)
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 3);
		transitions
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', transitionRadius)
			.style('fill', EDGE_COLOR)
			.style('fill-opacity', EDGE_OPACITY)
			.style('cursor', 'pointer')
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1);

		// transitions label text
		transitions
			.append('text')
			.attr('y', (d) => setFontSize(d.id))
			.style('text-anchor', 'middle')
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.html((d) => d.id);

		// transitions expression text
		transitions
			.append('text')
			.attr('y', () => -8)
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', FONT_SIZE_SMALL)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '3px')
			.style('stroke-linecap', 'butt')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.html((d) => {
				if (d.data.expression) return d.data.expression;
				return '';
			});

		// observables
		observables
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5)
			.attr('rx', '6')
			.attr('ry', '6')
			.style('fill', NODE_COLOR)
			.style('cursor', 'pointer')
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1);

		// observables text
		observables
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.text((d) => d.id);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection.style('display', (d) => (d.data?.isObservable ? 'none' : 'block'));

		const transitionNodeIds = this.graph.nodes.filter((n) => n.data.type === 'transition').map((n) => n.id);

		const originNodeIds = this.graph.edges.map((edge) => edge.source);
		const terminalTransitionNodeIds = transitionNodeIds.filter((id) => !originNodeIds.includes(id));

		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 3)
			.style('stroke-dasharray', (d) => {
				if (d.data?.isController || d.data?.isObservable) {
					return 4;
				}
				return null;
			})
			.attr('marker-end', (d) => {
				if (d.data?.isController || d.data?.isObservable) return null;

				// TODO: should check if the transition node is terminal
				if (transitionNodeIds.includes(d.target as string) && !terminalTransitionNodeIds.includes(d.target))
					return null;
				return 'url(#arrowhead)';
			});

		// Re-adjust edges
		selection.selectAll('path').each(function (d: any) {
			if (d.data?.isController || d.data?.isObservable) return;
			if (transitionNodeIds.includes(d.target as string)) return;

			const selectItem = d3.select(this);
			const pathNode = selectItem.node();
			const newPathPoints = adjustedPathPoints(pathNode as SVGPathElement, RENDER_EDGE_OFFSET);

			// Apply new points
			d3.select(this).attr('d', pathFn(newPathPoints));
		});

		selection.append('text').each(function (d) {
			if (d.id && !isEmpty(d.points) && d.data?.isObservable) {
				d3.select(this)
					.classed('latex-font', true)
					.attr('x', (d.points[1].x + d.points[2].x) / 2)
					.attr('y', (d.points[1].y + d.points[2].y) / 2 - 30)
					.style('font-style', 'italic')
					.style('font-size', FONT_SIZE_REGULAR)
					.style('paint-order', 'stroke')
					.style('stroke', '#fafafa')
					.style('stroke-width', '6px')
					.style('stroke-linecap', 'butt')
					.style('fill', TEXT_COLOR)
					.text(d.id);
			}
		});

		this.updateMultiEdgeLabels();
	}

	/* @Override */
	updateEdgePoints(): void {
		// The edge rendering here is customized and more expensive, we will null-out
		// the general update mechanism and instead do updates upon emitted dragging
		// events where we can do more customized control/filtering
		//
		// See updateEdgePointsCustom
	}

	updateEdgePointsCustom(nodeId: string): void {
		const chart = this.chart;
		const transitionNodeIds = this.graph.nodes.filter((n) => n.data.type === 'transition').map((n) => n.id);

		// At this point, the edge points have been updated, but we need to recompute the paths
		chart!
			.selectAll('.edge')
			.filter((d: any) => d.target === nodeId || d.source === nodeId)
			.selectAll('path')
			.attr('d', (d: any) => {
				if (transitionNodeIds.includes(d.target as string)) return pathFn(d.points);

				const virtualPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
				virtualPath.setAttribute('d', pathFn((d as IEdge<any>).points) as string);
				const newPathPoints = adjustedPathPoints(virtualPath, RENDER_EDGE_OFFSET);
				return pathFn(newPathPoints);
			});
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
				.attr('y', point.y + 6)
				.classed('latex-font', true)
				.style('font-style', 'italic')
				.style('font-size', FONT_SIZE_REGULAR)
				.style('paint-order', 'stroke')
				.style('stroke', '#fafafa)')
				.style('stroke-width', '6px')
				.style('stroke-linecap', 'butt')
				.style('fill', TEXT_COLOR)
				.text((d) => d.data?.numEdges as number);
		});
	}

	resetOpacity() {
		this?.chart?.selectAll('.node-ui, .edge').style('opacity', 1);
	}

	castTransparency() {
		this?.chart?.selectAll('.node-ui, .edge').style('opacity', 0.3);
	}

	toggleNodeSelection(selection: D3SelectionINode<NodeData>) {
		if (this.nodeSelection && this.nodeSelection.datum().id === selection.datum().id) {
			this.resetOpacity();
			this.nodeSelection = null;
		} else {
			// Set focus on node:
			this.castTransparency();
			selection.style('opacity', 1);
			this.nodeSelection = selection;
		}
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
		this.on('node-drag-start', (_eventName, event, selection: D3SelectionINode<NodeData>) => {
			// set colour on drag
			selection.selectAll('.selectableNode').attr('stroke', HIGHLIGHTEDSTROKECOLOUR);
			this.draggingNode = selection;

			if (!this.isDragEnabled) return;
			sourceData = selection.datum();
			start.x = sourceData.x;
			start.y = sourceData.y;

			const targetSelection = d3.select(event.sourceEvent.target);
			start.x += +targetSelection.attr('cx');
			start.y += +targetSelection.attr('cy');
		});

		this.on('node-drag-move', (_eventName, event /* , _selection: D3SelectionINode<NodeData> */) => {
			this.updateMultiEdgeLabels();
			this.updateEdgePointsCustom(this.draggingNode!.datum().id);

			if (!this.isDragEnabled) return;
			const pointerCoords = d3.zoomTransform(svg.node() as Element).invert(d3.pointer(event, svg.node()));
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
				.style('stroke', EDGE_COLOR);
		});

		this.on('node-drag-end', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			this.updateEdgePointsCustom(this.draggingNode!.datum().id);
			this.draggingNode = null;
			chart?.selectAll('.new-edge').remove();
			// reset colour after drag
			selection.selectAll('.selectableNode').attr('stroke', NODE_COLOR);

			if (!this.isDragEnabled) return;
			if (targetData && sourceData) {
				this.emit('add-edge', null, null, { target: targetData, source: sourceData });
				sourceData = null;
				targetData = null;
			}
		});

		// Observable edge appears on observable node hover
		this.on('node-mouse-enter', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			const { data } = selection.datum();
			const { type, expression, references } = data;
			if (type === NodeType.Observable && expression && references) {
				this.castTransparency();
				// Only show nodes and edges related to the observable
				this?.chart
					?.selectAll('.node-ui')
					.filter((d: any) => references.includes(d.id))
					.style('opacity', 1);
				this?.chart
					?.selectAll('.edge')
					.filter((d: any) => d.id === expression)
					.style('display', 'block')
					.style('opacity', 1);
				selection.style('opacity', 1);
			}
		});

		this.on('node-mouse-leave', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			const { data } = selection.datum();
			if (data.type === NodeType.Observable && data.expression) {
				this.resetOpacity();
				this?.chart
					?.selectAll('.edge')
					.filter((d: any) => d.id === data.expression)
					.style('display', 'none');
			}
		});

		this.on('node-click', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			this.toggleNodeSelection(selection);
		});

		this.on('background-click', () => {
			this.resetOpacity();

			if (this.nodeSelection) {
				this.nodeSelection = null;
			}
		});

		// restore prior selection state, if applicable
		if (this.nodeSelection && this.chart) {
			const id = this.nodeSelection.datum().id;
			this.nodeSelection = this.chart
				.selectAll('.node-ui')
				.filter((d: any) => d.id === id) as D3SelectionINode<NodeData>;
		}
	}

	getShapeOffset(node: any, angle: number) {
		switch (node.data.type) {
			case NodeType.Transition: {
				// transitions -> squares
				return { x: node.x, y: node.y };
			}
			case NodeType.State: {
				// species -> circles with multiplier
				const radius = node.width * 1.5;
				return { x: node.x + radius * Math.cos(angle), y: node.y + radius * Math.sin(angle) };
			}
			default:
				return { x: node.x, y: node.y };
		}
	}
}
