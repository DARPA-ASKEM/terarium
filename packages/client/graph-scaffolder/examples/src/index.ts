import _ from 'lodash';
import * as d3 from 'd3';
import BasicRenderer from '../../src/core/basic-renderer';
// import { panGraph } from '../../src/actions/pan-graph';
// import { moveTo } from '../../src/fn/move-to';
// import { highlight } from '../../src/fn/highlight';
import { group, ungroup } from '../../src/fn/group';
import { IGraph, INode, IEdge } from '../../src/types';

import { runLayout } from './dagre';

interface NodeData {
	type: string;
}

interface EdgeData {
	val: number;
}

type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

class SampleRenderer extends BasicRenderer<NodeData, EdgeData> {
	constructor(options: any) {
		super(options);
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === 'species');
		const transitions = selection.filter((d) => d.data.type === 'transition');

		transitions
			.append('rect')
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88C')
			.style('stroke', '#888');

		species
			.append('circle')
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#f80');

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
			.style('stroke', '#000');
	}
}

// Render
const div = document.createElement('div');
div.style.height = '800px';
div.style.width = '800px';
document.body.append(div);

const renderer = new SampleRenderer({
	el: div,
	useAStarRouting: true,
	runLayout: runLayout
});
renderer.on('node-click', () => {
	console.log('node click');
});
renderer.on('node-dbl-click', () => {
	console.log('node double click');
});
renderer.on(
	'node-mouse-enter',
	(_eventName: string, _evt: any, selection: D3SelectionINode<NodeData>) => {
		// selection.select('rect').style('fill', '#f80');
	}
);
renderer.on(
	'node-mouse-leave',
	(_eventName: string, _evt: any, selection: D3SelectionINode<NodeData>) => {
		// selection.select('rect').style('fill', '#eee');
	}
);

renderer.on('hello', (evtName: string, t: string) => {
	console.log(evtName, t);
});

// Graph data
// let g:IGraph<NodeData, EdgeData> = {
//   width: 800,
//   height: 400,
//   nodes: [
//     { id: '0', label: 'multinomial', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '1', label: 'multinomial', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '2', label: 'ptwisesum', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '3', label: 'P', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '4', label: 'solve', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '5', label: 'P', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '6', label: 'solve', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '7', label: 'measure', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '8', label: 'measure', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//     { id: '9', label: 'difference', x: 0, y: 0, height: 20, width: 20, data: null, nodes: [] },
//   ],
//   edges: [
//     { source: '3', target: '4', id: 'a', data: null, points: [] },
//     { source: '0', target: '2', id: 'b', data: null, points: [] },
//     { source: '2', target: '4', id: 'c', data: null, points: [] },
//     { source: '8', target: '9', id: 'd', data: null, points: [] },
//     { source: '4', target: '7', id: 'e', data: null, points: [] },
//     { source: '6', target: '8', id: 'f', data: null, points: [] },
//     { source: '5', target: '6', id: 'g', data: null, points: [] },
//     { source: '1', target: '2', id: 'h', data: null, points: [] },
//     { source: '2', target: '6', id: 'i', data: null, points: [] },
//     { source: '7', target: '9', id: 'j', data: null, points: [] }
//   ]
//
// };

let g: IGraph<NodeData, EdgeData> = {
	width: 500,
	height: 500,
	nodes: [
		{
			id: 'susceptible',
			label: 'susceptible',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'infected',
			label: 'infected',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'recovered',
			label: 'recovered',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'infection',
			label: 'infection',
			x: 0,
			y: 0,
			height: 40,
			width: 40,
			data: { type: 'transition' },
			nodes: []
		},
		{
			id: 'recovery',
			label: 'recovery',
			x: 0,
			y: 0,
			height: 40,
			width: 40,
			data: { type: 'transition' },
			nodes: []
		}
	],
	edges: [
		{ id: '1', source: 'susceptible', target: 'infection', points: [], data: null },
		{ id: '2', source: 'infection', target: 'infected', points: [], data: { val: 2 } },
		{ id: '3', source: 'infected', target: 'recovery', points: [], data: null },
		{ id: '4', source: 'recovery', target: 'recovered', points: [], data: null },
		{ id: '5', source: 'infected', target: 'infection', points: [], data: null }
	]
};

g = runLayout(_.cloneDeep(g));

const run = async () => {
	await renderer.setData(g);
	await renderer.render();
	// highlight(renderer, ['789'], [], {});
	// moveTo(renderer, '123', 2000);

	group(renderer, 'xyz', ['123', '456']);
	renderer.render();

	// ungroup(renderer, 'xyz');
	// renderer.setData(runLayout(renderer.graph));
	// renderer.render();
};

run();
