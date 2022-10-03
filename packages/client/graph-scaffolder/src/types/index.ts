// Not sure why I can't name this index.d.ts, when I do tsc skips it from generating in the build
export type EventCallback = (type: string | symbol, ...args: any[]) => void;
export type EventName = string | symbol;

export interface IRect {
	x: number;
	y: number;
	width: number;
	height: number;
}

export interface IPoint {
	x: number;
	y: number;
}

export interface INode<T> {
	id: string;
	label: string;
	type?: string;
	state?: string;
	x: number;
	y: number;
	width: number;
	height: number;
	data: T;
	nodes: INode<T>[];
}

export interface IEdge<T> {
	id: string;
	source: string;
	target: string;
	points: IPoint[];
	state?: string;
	data: T;
}

export interface IGraph<V, E> {
	nodes: INode<V>[];
	edges: IEdge<E>[];
	width?: number;
	height?: number;
}

export type D3Selection = d3.Selection<d3.BaseType, any, null, any>;
export type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
export type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;
