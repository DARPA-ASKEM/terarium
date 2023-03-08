import * as d3 from 'd3';
import graphScaffolder, { INode, IEdge } from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge, D3SelectionHandles } from '@/services/graph';
import { pointOnPath } from '@/utils/svg';
import { NodeData, EdgeData, NodeType } from './petrinet-service';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

export class PetrinetRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
	displayedHandles: D3SelectionHandles<NodeData> | null = null;

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
		transitions
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cx', (d) => d.width * 1.2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		transitions
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cy', (d) => d.height * 1.2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		transitions
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cx', (d) => -d.width * 1.2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		// transitions text
		transitions
			.append('text')
			.attr('y', (d) => -d.height * 0.5 - 5)
			.attr('x', (d) => -d.width * 0.5)
			.text((d) => d.label);

		// species
		species
			.append('circle')
			.classed('shape', true)
			.attr('r', (d) => d.width * 1.4)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2);

		// species drag handles
		species
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cx', (d) => d.width * 2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		species
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cy', (d) => d.height * 2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		species
			.append('circle')
			.classed('shape', true)
			.classed('no-drag', true)
			.attr('cx', (d) => -d.width * 2)
			.attr('r', (d) => d.width * 0.2)
			.attr('fill', '#FFF')
			.attr('stroke', '#AAA')
			.attr('stroke-width', 2)
			.style('opacity', 0);

		// species text
		species
			.append('text')
			.attr('y', (d) => -d.height * 1.4 - 5)
			.attr('x', (d) => -d.width * 1.4)
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
				.text((d) => d.data?.numEdges as number);
		});
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
					.style('stroke', '#000');
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
			// hide any handles which are already open
			this.displayedHandles?.style('opacity', 0).style('visibility', 'hidden');

			const handles = selection.selectAll('.no-drag');
			handles
				.transition('ease-out')
				.duration(200)
				.style('opacity', 1)
				.style('visibility', 'visible');
			this.displayedHandles = handles;
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

	addEdge(source: any, target: any) {
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
		this.render();
	}
}
