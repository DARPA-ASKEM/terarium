import _ from 'lodash';
import { Renderer } from '../core';
import { INode } from '../types';
import { flattenGraph } from '../core/traverse';

export const group = <V, E>(G: Renderer<V, E>, groupName: string, nodeIds: string[]): void => {
	// 0) check parent
	const nodesData = flattenGraph(G.graph).nodes.filter((d) => nodeIds.includes(d.id));

	const parentId = (id: string): string => {
		if (G.parentMap.has(id)) {
			return G.parentMap.get(id)?.id || '';
		}
		return '';
	};

	if (_.uniq(nodesData.map((d) => parentId(d.id))).length !== 1) {
		console.log('Cannot group across different levels');
		return;
	}

	const groupNode: INode<V> = {
		id: groupName,
		label: groupName,
		type: 'custom',
		nodes: [],
		/* @ts-ignore */
		data: null,
		x: 0,
		y: 0,
		width: 0,
		height: 0
	};

	if (G.parentMap.has(nodesData[0].id)) {
		// 1) Move nodes to new group
		const parentData = G.parentMap.get(nodesData[0].id);
		if (!parentData) return;

		nodeIds.forEach((nodeId) => {
			const temp = _.remove(parentData.nodes, (node: INode<V>) => node.id === nodeId)[0];
			temp.x = 0;
			temp.y = 0;

			// Need to create a new node wrapper to avoid double pointers problem
			const newNode = { ...temp };
			groupNode.nodes.push(newNode);
		});

		// 2) Add new gruop node
		parentData.nodes.push(groupNode);
	} else {
		// Top level
		// 1) Move nodes to new group
		nodeIds.forEach((nodeId) => {
			const temp = _.remove(G.graph.nodes, (node: INode<V>) => node.id === nodeId)[0];
			temp.x = 0;
			temp.y = 0;

			// Need to create a new node wrapper to avoid double pointers problem
			const newNode = { ...temp };
			groupNode.nodes.push(newNode);
		});

		// 2) Add new gruop node
		G.graph.nodes.push(groupNode);
	}
	G.isGraphDirty = true;
};

export const ungroup = <V, E>(G: Renderer<V, E>, groupName: string): void => {
	const groupData = flattenGraph(G.graph).nodes.filter((d) => d.id === groupName)[0];

	if (G.parentMap.has(groupName)) {
		const parentData = G.parentMap.get(groupName);

		if (!parentData) return;

		// 0) Remove group
		_.remove(parentData.nodes, (n) => n.id === groupName);

		// 1) Add group children back into group parent
		groupData.nodes.forEach((node) => {
			const temp = { ...node };
			parentData.nodes.push(temp);
		});
	} else {
		_.remove(G.graph.nodes, (n) => n.id === groupName);
		groupData.nodes.forEach((node) => {
			const temp = { ...node };
			G.graph.nodes.push(temp);
		});
	}
	// delete groupData.nodes;
	groupData.nodes = [];
	G.isGraphDirty = true;
};
