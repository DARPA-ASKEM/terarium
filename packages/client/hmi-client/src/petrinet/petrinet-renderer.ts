import * as d3 from 'd3';
import { BasicRenderer, INode, IEdge } from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { pointOnPath } from '@/utils/svg';
import { NodeData, EdgeData, NodeType } from './petrinet-service';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

const EDGE_COLOR = 'var(--petri-lineColor)';
const EDGE_OPACITY = 0.5;

const HANDLE_SIZE = 4;
const HANDLE_SIZE_HOVER = 8;

export class PetrinetRenderer extends BasicRenderer<NodeData, EdgeData> {
	nodeSelection: D3SelectionINode<NodeData> | null = null;

	edgeSelection: D3SelectionIEdge<EdgeData> | null = null;

	editMode: boolean = false;

	initialize(element: HTMLDivElement): void {
		super.initialize(element);

		d3.select(this.svgEl)
			.style('border', '2px solid transparent')
			.style('border-radius', '8px 0px 0px 8px');
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

	setEditMode(v: boolean) {
		this.editMode = v;

		const svg = d3.select(this.svgEl);
		if (this.editMode) {
			svg.style('border', '2px solid var(--primary-color)');
		} else {
			svg.style('border', '2px solid transparent');
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
			.attr('rx', '6')
			.attr('ry', '6')
			.style('fill', '#FFF')
			.style('cursor', 'pointer')
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1);

		function handleMouseOver(evt: MouseEvent) {
			d3.select(evt.target as any)
				.transition()
				.duration(100)
				.attr('r', HANDLE_SIZE_HOVER);
		}

		function handleMouseOut(evt: MouseEvent) {
			d3.select(evt.target as any)
				.transition()
				.duration(100)
				.attr('r', HANDLE_SIZE);
		}

		// transitions drag handles
		const transitionsHandles = [
			...transitions.append('circle').attr('cx', (d) => d.width * 0.75),
			...transitions.append('circle').attr('cy', (d) => d.height * 0.75),
			...transitions.append('circle').attr('cx', (d) => -d.width * 0.75),
			...transitions.append('circle').attr('cy', (d) => -d.height * 0.75)
		];

		d3.selectAll(transitionsHandles)
			.classed('shape no-drag', true)
			.attr('r', HANDLE_SIZE)
			.attr('fill', 'var(--primary-color)')
			.attr('stroke', 'none')
			.style('cursor', 'pointer')
			.style('opacity', 0)
			.on('mouseover', handleMouseOver)
			.on('mouseout', handleMouseOut);

		// transitions text
		transitions
			.append('text')
			.attr('y', () => 5)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '6px')
			.style('stroke-linecap', 'butt')
			.style('stroke-linejoin', 'matter')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.text((d) => d.label);

		// species
		species
			.append('circle')
			.classed('shape', true)
			.attr('r', (d) => 0.55 * d.width) // FIXME: need to adjust edge from sqaure mapping to circle
			.attr('fill', '#FFF')
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

		// species drag handles
		const speciesHandles = [
			...species.append('circle').attr('cx', (d) => d.width * 0.75),
			...species.append('circle').attr('cy', (d) => d.height * 0.75),
			...species.append('circle').attr('cx', (d) => -d.width * 0.75),
			...species.append('circle').attr('cy', (d) => -d.height * 0.75)
		];

		d3.selectAll(speciesHandles)
			.classed('shape no-drag', true)
			.attr('r', HANDLE_SIZE)
			.attr('fill', 'var(--primary-color)')
			.attr('stroke', 'none')
			.style('cursor', 'pointer')
			.style('opacity', 0)
			.on('mouseover', handleMouseOver)
			.on('mouseout', handleMouseOut);

		// species text
		species
			.append('text')
			.attr('y', () => 5)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '6px')
			.style('stroke-linecap', 'butt')
			.style('stroke-linejoin', 'matter')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.text((d) => d.label);
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
				.style('font-size', 18)
				.style('paint-order', 'stroke')
				.style('stroke', 'var(--gray-50)')
				.style('stroke-width', '6px')
				.style('stroke-linecap', 'butt')
				.style('stroke-linejoin', 'matter')
				.style('fill', 'var(--text-color-primary')
				.text((d) => d.data?.numEdges as number);
		});
	}

	selectNode(selection: D3SelectionINode<NodeData>) {
		selection.selectAll('.no-drag').attr('stroke-width', 3);
		selection.select('text').style('fill-opacity', 0);

		// Add in a input textbox to change the name
		const w = selection.datum().width;
		const h = selection.datum().width;

		selection
			.append('foreignObject')
			.attr('x', -0.5 * w)
			.attr('y', -0.5 * h)
			.attr('width', w)
			.attr('height', h)
			.attr('style', 'position:relative')
			.append('xhtml:input')
			.attr('type', 'text')
			.attr(
				'style',
				`width:${
					w * 0.8
				}px; height:30px; background: var(--petri-inputBox); border-radius:var(--border-radius); border: 2px solid transparent; text-align:center; position: absolute; top:50%; left:50%; transform: translate(-50%, -50%);`
			)
			.attr('value', selection.datum().label);
	}

	deselectNode(selection: D3SelectionINode<NodeData>) {
		selection.selectAll('.no-drag').attr('stroke-width', 1);
		const newLabel = (selection.select('input').node() as HTMLInputElement).value;
		selection.datum().label = newLabel;
		selection.select('text').text(newLabel).style('fill-opacity', 1.0);
		selection.select('foreignObject').remove();
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
					.style('stroke-width', 3)
					.style('stroke', 'var(--primary-color)');
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
			if (this.nodeSelection && this.nodeSelection.datum().id === selection.datum().id) return;

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

		// restore prior selection state, if applicable
		if (this.nodeSelection && this.chart) {
			const id = this.nodeSelection.datum().id;
			this.nodeSelection = this.chart
				.selectAll('.node-ui')
				.filter((d: any) => d.id === id) as D3SelectionINode<NodeData>;

			if (this.nodeSelection) {
				this.selectNode(this.nodeSelection);
			}
		}
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
