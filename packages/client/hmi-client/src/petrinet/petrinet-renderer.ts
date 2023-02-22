import * as d3 from 'd3';
import graphScaffolder, { INode } from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { pointOnPath } from '@/utils/svg';
import { NodeData, EdgeData, NodeType } from './petrinet-service';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';
const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

export class PetrinetRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
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

		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#89BEFF')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.8)
			.attr('fill', '#FF90A9');

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

		const multiEdges = selection.filter((d) => d.data?.numEdges > 1);
		multiEdges.each((_d, index, group) => {
			const edgeSelection = d3.select(group[index]);
			const point = pointOnPath(edgeSelection.select('path').node() as any, 0.5);
			edgeSelection
				.append('text')
				.attr('x', point.x)
				.attr('y', point.y)
				.attr('stroke', null)
				.attr('fill', '#000')
				.text((d) => d.data.numEdges);
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
			sourceData = selection.datum();
			start.x = sourceData.x + 0.5 * sourceData.width;
			start.y = sourceData.y + 0.5 * sourceData.height;
		});

		this.on(
			'node-drag-move',
			(_eventName, event /* , _selection: D3SelectionINode<NodeData> */) => {
				const pointerCoords = d3
					.zoomTransform(svg.node() as Element)
					.invert(d3.pointer(event, svg.node()));
				targetData = d3.select<SVGGElement, INode<NodeData>>(event.sourceEvent.target).datum();
				if (targetData) {
					end.x = targetData.x + 0.5 * targetData.width;
					end.y = targetData.y + 0.5 * targetData.height;
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

		this.on('node-drag-end', (/* _eventName, _event, _selection: D3SelectionINode<NodeData> */) => {
			chart?.selectAll('.new-edge').remove();
			if (targetData && sourceData) {
				this.emit('add-edge', null, null, { target: targetData, source: sourceData });
				sourceData = null;
				targetData = null;
			}
		});
	}

	addEdge(source: any, target: any) {
		this.graph.edges.push({
			id: `${source.datum().id}_${target.datum().id}`,
			source: source.datum().id,
			target: target.datum().id,
			points: [
				{
					x: source.datum().x + source.datum().width * 0.5,
					y: source.datum().y + source.datum().height * 0.5
				},
				{
					x: target.datum().x + target.datum().width * 0.5,
					y: target.datum().y + target.datum().height * 0.5
				}
			],
			data: { val: 1 }
		});
		this.render();
	}
}
