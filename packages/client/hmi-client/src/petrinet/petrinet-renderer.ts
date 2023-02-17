import * as d3 from 'd3';
import graphScaffolder from '@graph-scaffolder/index';
import { D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { NodeData, EdgeData } from './petrinet-service';

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';

export class SampleRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
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
