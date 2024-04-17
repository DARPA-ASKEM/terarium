import * as d3 from 'd3';
import { BasicRenderer, INode, IEdge } from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { pointOnPath } from '@/utils/svg';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';

export interface NodeData {
	type: string;
	expression?: string;
	strataType?: string;
}

export interface EdgeData {
	numEdges: number;
	isController?: boolean;
}

export enum NodeType {
	State = 'state',
	Transition = 'transition'
}

const FONT_SIZE_SMALL = 18;
const FONT_SIZE_REGULAR = 24;
const FONT_SIZE_LARGE = 36;

function setFontSize(label: string) {
	if (label.length < 3) {
		return FONT_SIZE_LARGE;
	}
	if (label.length < 10) {
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

const EDGE_COLOR = 'var(--petri-lineColor)';
const HIGHLIGHTEDSTROKECOLOUR = 'var(--primary-color)';
const EDGE_OPACITY = 0.5;

const { getNodeTypeColor } = useNodeTypeColorPalette();

export class PetrinetRenderer extends BasicRenderer<NodeData, EdgeData> {
	nodeSelection: D3SelectionINode<NodeData> | null = null;

	edgeSelection: D3SelectionIEdge<EdgeData> | null = null;

	initialize(element: HTMLDivElement): void {
		super.initialize(element);

		d3.select(this.svgEl)
			.style('border', '4px solid transparent')
			.style('background', 'var(--surface-0')
			.style('border-radius', 'var(--border-radius) 0px 0px var(--border-radius)');
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
		const species = selection.filter((d) => d.data.type === NodeType.State);
		const transitions = selection.filter((d) => d.data.type === NodeType.Transition);

		// transitions
		transitions
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5)
			.attr('rx', '6')
			.attr('ry', '6')
			.style('fill', (d) =>
				d.data.strataType ? getNodeTypeColor(d.data.strataType) : 'var(--petri-nodeFill'
			)
			.style('cursor', 'pointer')
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1);

		// transitions label text
		transitions
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latin-modern-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.html((d) => d.id);

		// transitions expression text
		transitions
			.append('text')
			.attr('y', (d) => -d.height / 2 - 8)
			.classed('latin-modern-font', true)
			.style('font-style', 'italic')
			.style('font-size', FONT_SIZE_SMALL)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '3px')
			.style('stroke-linecap', 'butt')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.html((d) => {
				if (d.data.expression) return d.data.expression;
				return '';
			});

		// species
		species
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', (d) => 0.55 * d.width) // FIXME: need to adjust edge from sqaure mapping to circle
			.attr('fill', (d) =>
				d.data.strataType ? getNodeTypeColor(d.data.strataType) : 'var(--petri-nodeFill)'
			)
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

		// species text
		species
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latin-modern-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.text((d) => d.id);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', EDGE_COLOR)
			.style('stroke-opacity', EDGE_OPACITY)
			.style('stroke-width', 3)
			.style('stroke-dasharray', (d) => {
				if (d.data && d.data.isController === true) {
					return 4;
				}
				return null;
			})
			.attr('marker-end', (d) => {
				if (d.data && d.data.isController) return null;
				return 'url(#arrowhead)';
			});

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
				.attr('y', point.y + 6)
				.classed('latin-modern-font', true)
				.style('font-style', 'italic')
				.style('font-size', FONT_SIZE_REGULAR)
				.style('paint-order', 'stroke')
				.style('stroke', 'var(--gray-50)')
				.style('stroke-width', '6px')
				.style('stroke-linecap', 'butt')
				.style('fill', 'var(--text-color-primary')
				.text((d) => d.data?.numEdges as number);
		});
	}

	selectEdge(selection: D3SelectionIEdge<EdgeData>) {
		selection.selectAll('path').style('stroke-width', 3);
	}

	deselectEdge(selection: D3SelectionIEdge<EdgeData>) {
		selection.selectAll('path').style('stroke-width', 2);
	}

	toggoleNodeSelectionByLabel(label: string) {
		const selection = this.chart?.selectAll('.node-ui').filter((d: any) => d.label === label);
		if (selection && selection.size() === 1) {
			this.toggleNodeSelection(selection as D3SelectionINode<NodeData>);
		}
	}

	toggleNodeSelection(selection: D3SelectionINode<NodeData>) {
		if (this.nodeSelection && this.nodeSelection.datum().id === selection.datum().id) {
			this?.chart?.selectAll('.node-ui').style('opacity', 1);
			this?.chart?.selectAll('.edge').style('opacity', 1);
			this.nodeSelection = null;
		} else {
			// Set focus on node:
			this?.chart?.selectAll('.node-ui').style('opacity', 0.3);
			this?.chart?.selectAll('.edge').style('opacity', 0.3);
			selection.style('opacity', 1);
			this.nodeSelection = selection;
		}

		if (this.edgeSelection) {
			this.deselectEdge(this.edgeSelection);
			this.edgeSelection = null;
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

			if (!this.isDragEnabled) return;
			sourceData = selection.datum();
			start.x = sourceData.x;
			start.y = sourceData.y;

			const targetSelection = d3.select(event.sourceEvent.target);
			start.x += +targetSelection.attr('cx');
			start.y += +targetSelection.attr('cy');
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
					.style('stroke-width', 3)
					.style('stroke', 'var(--primary-color)');
			}
		);

		this.on('node-drag-end', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			chart?.selectAll('.new-edge').remove();
			// reset colour after drag
			selection.selectAll('.selectableNode').attr('stroke', 'var(--petri-nodeBorder)');

			if (!this.isDragEnabled) return;
			if (targetData && sourceData) {
				this.emit('add-edge', null, null, { target: targetData, source: sourceData });
				sourceData = null;
				targetData = null;
			}
		});

		this.on('node-click', (_eventName, _event, selection: D3SelectionINode<NodeData>) => {
			this.toggleNodeSelection(selection);
		});

		this.on('edge-click', (_eventName, _event, selection: D3SelectionIEdge<EdgeData>) => {
			if (this.edgeSelection) {
				this.deselectEdge(this.edgeSelection);
			}
			if (this.nodeSelection) {
				this.nodeSelection = null;
			}

			this.edgeSelection = selection;
			this.selectEdge(this.edgeSelection);
		});

		this.on('background-click', () => {
			// Reset opacity from focus:
			this?.chart?.selectAll('.node-ui').style('opacity', 1);
			this?.chart?.selectAll('.edge').style('opacity', 1);

			if (this.edgeSelection) {
				this.deselectEdge(this.edgeSelection);
				this.edgeSelection = null;
			}
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
