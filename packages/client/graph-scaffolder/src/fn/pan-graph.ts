import * as d3 from 'd3';
import { Renderer } from '../core';

const panGraph = <V, E>(G: Renderer<V, E>, x: number, y: number, duration: number): void => {
	const chart = G.chart;
	const t = d3.zoomTransform(chart?.node() as Element);
	const svg = d3.select(G.svgEl);
	svg
		.transition()
		.duration(duration)
		.call(G.zoom?.transform as any, d3.zoomIdentity.translate(t.x, t.y).scale(t.k).translate(x, y));
};

export default panGraph;
