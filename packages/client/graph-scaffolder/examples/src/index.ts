import * as _ from 'lodash';
import * as d3 from 'd3';
import BasicRenderer from '../../src/core/basic-renderer';
// import { panGraph } from '../../src/actions/pan-graph';
// import { moveTo } from '../../src/fn/move-to';
// import { highlight } from '../../src/fn/highlight';
import { group, ungroup } from '../../src/fn/group';
import { IEdge, IGraph, INode } from '../../src/types';

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
		const species = selection.filter((d) => d.type !== 'custom' && d.data.type === 'species');
		const transitions = selection.filter(
			(d) => d.type !== 'custom' && d.data.type === 'transition'
		);
		const customs = selection.filter((d) => d.type === 'custom');

		customs
			.append('rect')
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#eee')
			.style('stroke', '#888');

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
//
const groupBtn = document.createElement('button');
groupBtn.innerHTML = 'Group test';
groupBtn.addEventListener('click', groupTest);
document.body.append(groupBtn);

const unGroupBtn = document.createElement('button');
unGroupBtn.innerHTML = 'Ungroup test';
unGroupBtn.addEventListener('click', ungroupTest);
document.body.append(unGroupBtn);

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
			type: 'normal',
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
			type: 'normal',
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
			type: 'normal',
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
			type: 'normal',
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
			type: 'normal',
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

	// group(renderer, 'xyz', ['susceptible', 'infected', 'recovered']);
	// renderer.render();

	// ungroup(renderer, 'xyz');
	// renderer.setData(runLayout(renderer.graph));
	// renderer.render();
};

function groupTest() {
	if (renderer) {
		group(renderer, 'A', ['susceptible', 'infected', 'recovered']);
		group(renderer, 'B', ['infection', 'recovery']);
		renderer.render();
	}
}

function ungroupTest() {
	if (renderer) {
		ungroup(renderer, 'A');
		ungroup(renderer, 'B');
		renderer.render();
	}
}

run();
