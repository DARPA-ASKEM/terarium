import * as d3 from 'd3';
import { Renderer } from '../core';
import { INode, D3SelectionINode } from '../types';

/**
 * Centralize provided node in the SVG canvas
 *
 * FIXME: need offset multiplier depending on how nodes are drawn
 *
 * See: https://observablehq.com/@d3/programmatic-zoom
 */
export const moveTo = <V, E>(
	G: Renderer<V, E>,
	node: D3SelectionINode<V>,
	duration: number
): void => {
	const chart = G.chart;
	const chartSize = G.chartSize;
	const svg = d3.select(G.svgEl);

	const widthTransform = chartSize.width;
	const heightTransform = chartSize.height;

	if (!chart) return;

	// t.k = scale, t.x = translateX, t.y = translateY
	const t = d3.zoomTransform(chart.node() as Element);

	// const node = flatten(G.layout).nodes.find(n => n.id === nodeId);
	if (!node) return;

	const parentMap = G.parentMap;

	let temp = node.datum() as INode<V>;
	const nodeWidth = temp.width;
	const nodeHeight = temp.height;
	let globalX = temp.x;
	let globalY = temp.y;

	while (parentMap.has(temp.id) === true) {
		temp = parentMap.get(temp.id) as INode<V>;
		globalX += temp.x;
		globalY += temp.y;
	}

	const dx = globalX + 0.5 * nodeWidth;
	const dy = globalY + 0.5 * nodeHeight;

	// fixme div 2 (mult 0.5)
	svg
		.transition()
		.duration(duration)
		.call(
			G.zoom?.transform as any,
			d3.zoomIdentity
				.translate(0, 0)
				.scale(t.k)
				.translate(-dx + (0.5 * widthTransform) / t.k, -dy + (0.5 * heightTransform) / t.k)
		);
};

export const moveToLabel = <V, E>(G: Renderer<V, E>, label: string, duration: number): void => {
	const chart = G.chart;
	const node = chart?.selectAll<any, INode<V>>('.node').filter((d: INode<V>) => d.label === label);
	if (!node) return;
	moveTo(G, node as D3SelectionINode<V>, duration);
};
