import * as d3 from 'd3';
import { Renderer } from '../core';

const unHighlight = <V, E>(G: Renderer<V, E>, id: string): void => {
	const svg = d3.select(G.svgEl);
	svg.select(`#${id}`).remove();
	svg.selectAll(`.${id}`).style('filter', null);
};

export default unHighlight;
