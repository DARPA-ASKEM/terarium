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
	expression?: string;
	x: number;
	y: number;
	width: number;
	height: number;
	data: T;
	nodes: INode<T>[];
	aspectRatio?: number;
	matrixCols?: number;
	matrixRows?: number;
}

export interface IEdge<T> {
	id?: string;
	source: string;
	target: string;
	points: IPoint[];
	state?: string;
	data?: T;
}

export interface IGraph<V, E> {
	nodes: INode<V>[];
	edges: IEdge<E>[];
	width?: number;
	height?: number;

	// keep an AMR copy
	amr?: any;
}

export type D3Selection = d3.Selection<d3.BaseType, any, null, any>;
export type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
export type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

type AsyncFunction<A, O> = (args: A) => Promise<O>;
type AsyncLayoutFunction<V, E> = AsyncFunction<IGraph<V, E>, IGraph<V, E>>;
type LayoutFunction<V, E> = (args: IGraph<V, E>) => IGraph<V, E>;

export interface Options {
	el?: HTMLDivElement | null;
	runLayout: AsyncLayoutFunction<any, any> | LayoutFunction<any, any>;

	useZoom?: boolean;
	zoomRange?: [number, number];
	zoomModifier?: string;
	useStableLayout?: boolean;

	// Attempt to use the same set of zoom parameters across layout changes
	useStableZoomPan?: boolean;

	// This is getting around algorithms that do not provide stand-alone routing capabilities, in
	// which case we can internally route using A-star
	useAStarRouting?: {
		collisionFn?: (p: IPoint, objects: IRect[]) => boolean;
	};

	// Whether to show grid
	useGrid?: boolean;

	// The css class for custom dragging event
	dragSelector?: string;
}
