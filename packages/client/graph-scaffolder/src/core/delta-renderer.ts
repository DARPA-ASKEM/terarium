import * as d3 from 'd3';
import { translate } from '../utils/svg-util';
import { Renderer } from './renderer';
import { INode, IEdge, D3Selection, D3SelectionINode, D3SelectionIEdge } from '../types';

export default abstract class DeltaRenderer<V, E> extends Renderer<V, E> {
	setupNodes(): void {
		const chart = this.chart?.select('.nodes-layer');

		const recursiveBuild = (selection: D3Selection, childrenNodes: INode<V>[]) => {
			if (!childrenNodes) return;

			const nodesGroup = selection
				.selectAll('.node')
				.filter(function () {
					return (this as any).parentNode === selection.node(); // FIXME any?
				})
				.data(childrenNodes, ((d: INode<V>) => d.id) as any);

			const newNodes = nodesGroup.enter().append('g').classed('node', true);

			// nodesGroup.exit().remove();
			nodesGroup.exit().each(((d: INode<V>) => {
				d.state = 'removed';
			}) as any);
			newNodes.each((d: INode<V>) => {
				d.state = 'new';
			});
			nodesGroup.each((d: INode<V>) => {
				d.state = 'updated';
			});

			[newNodes, nodesGroup].forEach((g) => {
				g.each(function (d) {
					const selection2 = d3.select(this);

					// Allocate for the node itself
					if (selection2.select('.node-ui').size() === 0) {
						selection2.append('g').classed('node-ui', true);
					}

					selection2.select('.node-ui').datum(d);

					// Allocate for the node's children
					if (selection2.select('.node-children').size() === 0) {
						selection2.append('g').classed('node-children', true);
					}
					recursiveBuild(selection2.select('.node-children'), d.nodes);
				});

				g.attr('transform', (d) => translate(d.x, d.y));
			});
		};
		recursiveBuild(chart as any, this.graph.nodes);

		this.renderNodesAdded(
			chart
				?.selectAll<any, INode<V>>('.node-ui')
				.filter((d) => d.state === 'new') as D3SelectionINode<V>
		);
		this.renderNodesUpdated(
			chart
				?.selectAll<any, INode<V>>('.node-ui')
				.filter((d) => d.state === 'updated') as D3SelectionINode<V>
		);
		this.renderNodesRemoved(
			chart
				?.selectAll<any, INode<V>>('.node-ui')
				.filter((d) => d.state === 'removed') as D3SelectionINode<V>
		);
	}

	setupEdges(): void {
		const chart = this.chart;
		if (!chart) return;

		const allEdges = this.graph.edges;

		const edgesGroup = chart?.selectAll('.edge').data(allEdges, ((d: IEdge<V>) => d.id) as any);

		// Scaffold added/updated/removed
		const newEdges = edgesGroup?.enter().append('g').classed('edge', true);

		edgesGroup?.exit().each(((d: IEdge<E>) => {
			d.state = 'removed';
		}) as any);
		newEdges?.each((d: IEdge<E>) => {
			d.state = 'new';
		});
		edgesGroup?.each((d: IEdge<E>) => {
			d.state = 'updated';
		});

		// Rebind because children point to different reference
		chart
			.selectAll('.edge')
			.filter((d: any) => (d as IEdge<E>).state === 'updated')
			.each(function (d) {
				d3.select(this).selectAll('.edge-path').datum(d);
			});

		this.renderEdgesAdded(
			chart
				.selectAll<any, IEdge<E>>('.edge')
				.filter((d) => d.state === 'new') as D3SelectionIEdge<E>
		);
		this.renderEdgesUpdated(
			chart
				.selectAll<any, IEdge<E>>('.edge')
				.filter((d) => d.state === 'updated') as D3SelectionIEdge<E>
		);
		this.renderEdgesRemoved(
			chart
				.selectAll<any, IEdge<E>>('.edge')
				.filter((d) => d.state === 'removed') as D3SelectionIEdge<E>
		);
	}

	abstract renderNodesAdded(selection: D3SelectionINode<V>): void;
	abstract renderNodesUpdated(selection: D3SelectionINode<V>): void;
	abstract renderNodesRemoved(selection: D3SelectionINode<V>): void;

	abstract renderEdgesAdded(selection: D3SelectionIEdge<E>): void;
	abstract renderEdgesUpdated(selection: D3SelectionIEdge<E>): void;
	abstract renderEdgesRemoved(selection: D3SelectionIEdge<E>): void;
}
