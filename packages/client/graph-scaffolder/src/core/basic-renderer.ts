import * as d3 from 'd3';
import { Renderer } from './renderer';
import { translate } from '../utils/svg-util';
import { INode, D3Selection, D3SelectionINode, D3SelectionIEdge } from '../types';

export default abstract class BasicRenderer<V, E> extends Renderer<V, E> {
	/**
	 * Basic nodes setup - redraws everything and invoke callback
	 */
	setupNodes(): void {
		const chart = this.chart?.select('.nodes-layer');
		chart?.selectAll('*').remove();

		const recursiveBuild = (selection: D3Selection, childrenNodes: INode<V>[]) => {
			if (!childrenNodes) return;
			const nodesGroup = selection
				.selectAll('.node')
				.data(childrenNodes)
				.enter()
				.append('g')
				.classed('node', true)
				.attr('transform', (d) => translate(d.x, d.y));

			nodesGroup.each(function (d) {
				const s = d3.select(this);
				s.append('g').classed('node-ui', true);
				recursiveBuild(s.append('g') as any, d.nodes);
			});
		};
		recursiveBuild(chart as any, this.graph?.nodes || []);
		this.renderNodes(chart?.selectAll('.node-ui') as D3SelectionINode<V>);
	}

	/**
	 * Basic edge setup - redraws everything and invoke callback
	 */
	setupEdges(): void {
		const chart = this.chart;
		chart?.selectAll('.edge').remove();

		const edges = this.graph?.edges || [];

		chart?.selectAll('.edge').data(edges).enter().append('g').classed('edge', true);

		this.renderEdges(chart?.selectAll('.edge') as D3SelectionIEdge<E>);
	}

	abstract renderNodes(selection: D3SelectionINode<V>): void;
	abstract renderEdges(selection: D3SelectionIEdge<E>): void;
}
