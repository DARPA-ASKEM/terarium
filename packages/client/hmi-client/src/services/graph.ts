/**
 * Provides graph rendering utilities for computational-like graphs
 */
import * as d3 from 'd3';
import { IGraph, INode, IEdge } from '@graph-scaffolder/index';
import { layoutInstance } from '@/layout/controller';

export type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
export type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

export enum NodeType {
	State = 'state',
	Transition = 'transition',
	Observable = 'observable'
}

export const runDagreLayout = async <V, E>(graphData: IGraph<V, E>): Promise<IGraph<V, E>> => {
	const graphLayout = await layoutInstance.runLayout(graphData);
	return graphLayout as any;
};
