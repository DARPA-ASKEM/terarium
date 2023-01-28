import * as d3 from 'd3';
import EventEmitter from './event-emitter';
import removeChildren from '../utils/dom-util';
import { traverseGraph, getAStarPath } from './traverse';
import { translate } from '../utils/svg-util';

import { INode, IEdge, IGraph, IRect, IPoint, D3Selection, D3SelectionINode } from '../types';

type AsyncFunction<A, O> = (args: A) => Promise<O>;
type AsyncLayoutFunction<V, E> = AsyncFunction<IGraph<V, E>, IGraph<V, E>>;
type LayoutFunction<V, E> = (args: IGraph<V, E>) => IGraph<V, E>;

interface Options {
	el?: HTMLDivElement | null;
	runLayout: AsyncLayoutFunction<any, any> | LayoutFunction<any, any>;

	useZoom?: boolean;
	useStableLayout?: boolean;

	// Attempt to use the same set of zoom parameters across layout changes
	useStableZoomPan?: boolean;

	// This is getting around algorithms that do not provide stand-alone routing capabilities, in
	// which case we can internally route using A-star
	useAStarRouting?: boolean;
}

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis); // FIXME: temp hack

export abstract class Renderer<V, E> extends EventEmitter {
	options: Options;

	parentMap: Map<string, INode<V>>;

	oldNodeMap: Map<string, IRect> = new Map();

	oldEdgeMap: Map<string, { points: IPoint[] }> = new Map();

	svgEl: SVGElement | null = null;

	chart: D3Selection | null = null;

	chartSize: { width: number; height: number } = { width: 1, height: 1 };

	graph: IGraph<V, E> = {
		nodes: [],
		edges: [],
		width: 1,
		height: 1
	};

	// misc
	isGraphDirty: boolean = true; // Graph layout has changed

	canLeverageStableLayout: boolean = false;

	clickTimer: any;

	zoom: d3.ZoomBehavior<Element, unknown> | null = null;

	zoomTransformObject: d3.ZoomTransform | null = null;

	constructor(options: Options) {
		super(); // Event emitter
		this.parentMap = new Map();
		this.options = options;

		if (this.options.el) {
			this.initalize(this.options.el);
		} else {
			throw new Error('options must provide an element for graph rendering');
		}
	}

	initalize(element: HTMLDivElement): void {
		this.chartSize.width = element.clientWidth;
		this.chartSize.height = element.clientHeight;
		this.svgEl = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
		this.svgEl.style.userSelect = 'none';
		removeChildren(element).appendChild(this.svgEl);
	}

	async setData(graph: IGraph<V, E>): Promise<void> {
		this.graph = graph;
		this.calculateMaps();
	}

	calculateMaps(): void {
		this.parentMap.clear();
		if (this.graph) {
			traverseGraph(this.graph, (node) => {
				if (node.nodes.length > 0) {
					node.nodes.forEach((n) => {
						this.parentMap.set(n.id, node);
					});
				}
			});
		}
	}

	getBoundary(): { x1: number; y1: number; x2: number; y2: number } {
		const t = d3.zoomTransform(this.chart?.node() as Element);
		const x1 = (0 - t.x) / t.k;
		const y1 = (0 - t.y) / t.k;
		const x2 = this.chartSize.width / t.k;
		const y2 = this.chartSize.height / t.k;
		return { x1, y1, x2, y2 };
	}

	createChartLayers(): void {
		const { width, height } = this.chartSize;
		const svg = d3.select(this.svgEl);
		svg.selectAll('*').remove();
		svg.attr('width', `${width}px`);
		svg.attr('height', `${height}px`);
		svg.append('defs');

		svg.append('g').classed('background-layer', true);
		svg.append('g').classed('data-layer', true);
		svg.append('g').classed('foreground-layer', true);

		// Add a nodes layer
		svg.select('.data-layer').append('g').classed('nodes-layer', true);
		this.chart = svg.select('.data-layer');
	}

	async render(): Promise<void> {
		this.oldNodeMap.clear();
		this.oldEdgeMap.clear();

		if (this.chart && this.options.useStableLayout === true) {
			const saveNodeMap = (d: INode<V>) => {
				this.oldNodeMap.set(d.id, {
					x: d.x,
					y: d.y,
					width: d.width,
					height: d.height
				});
			};

			const saveEdgeMap = (d: IEdge<E>) => {
				this.oldEdgeMap.set(d.id ?? '', {
					points: d.points
				});
			};

			this.chart.selectAll('.node').each(saveNodeMap as any);
			this.chart.selectAll('.edge').each(saveEdgeMap as any);
		}

		// Check if we need to re-run layout
		if (this.isGraphDirty === true) {
			this.graph = await this.options.runLayout(this.graph as IGraph<V, E>);
			this.calculateMaps();
		}

		if (!this.chart) {
			this.createChartLayers();
		}

		this.setupDefs();
		this.setupNodes();
		this.setupEdges();

		// Enable various interactions and emitter events
		this.chart?.selectAll('.edge').call(this.enableEdgeInteraction as any, this);
		this.chart?.selectAll('.node-ui').call(this.enableNodeInteraction as any, this);
		this.enableSVGInteraction(this);

		// Enable dragging nodes
		this.enableNodeDragging(this);

		this.isGraphDirty = false;
	}

	updateEdgePoints(): void {
		const chart = this.chart;
		chart
			?.selectAll('.edge')
			.selectAll('path')
			.attr('d', (d: any) => pathFn((d as IEdge<E>).points));
	}

	/**
	 * Edge interactions
	 */
	enableEdgeInteraction(selection: D3Selection, renderer: Renderer<V, E>): void {
		const setupEmitters = (_unused: any, edgeIndex: number, edges: SVGGElement[]) => {
			const edge = d3.select(edges[edgeIndex]);
			const emit = renderer.emit.bind(renderer);

			edge.on('click', function (evt) {
				evt.stopPropagation();
				emit('edge-click', evt, d3.select(this), renderer);
			});

			edge.on('mouseenter', function (evt) {
				evt.stopPropagation();
				emit('edge-mouse-enter', evt, d3.select(this), renderer);
			});

			edge.on('mouseleave', function (evt) {
				evt.stopPropagation();
				emit('edge-mouse-leave', evt, d3.select(this), renderer);
			});
		};

		selection.each(setupEmitters as any);
	}

	/**
	 * Node interactions
	 */
	enableNodeInteraction(selection: D3Selection, renderer: Renderer<V, E>): void {
		selection.each((_, nodeIndex, nodes) => {
			const node = d3.select(nodes[nodeIndex]);
			const emit = renderer.emit.bind(renderer);

			node.on('dblclick', function (evt) {
				evt.stopPropagation();
				window.clearTimeout(renderer.clickTimer);
				emit('node-dbl-click', evt, d3.select(this), renderer);
			});

			node.on('click', function (evt, d) {
				evt.stopPropagation();
				const e = d3.select(this);
				window.clearTimeout(renderer.clickTimer);
				renderer.clickTimer = window.setTimeout(() => {
					emit('node-click', evt, e, renderer, d);
				}, 200);
			});

			node.on('mouseenter', function (evt) {
				// Put the active element on top
				const nodeElement = (node.node() as SVGGElement).parentNode;
				const nodesContainer = nodeElement?.parentNode;
				nodesContainer?.appendChild(nodeElement as Element);

				evt.stopPropagation();
				emit('node-mouse-enter', evt, d3.select(this), renderer);
			});

			node.on('mouseleave', function (evt) {
				evt.stopPropagation();
				emit('node-mouse-leave', evt, d3.select(this), renderer);
			});
		});
	}

	/**
	 * Setup background/canvas interactions
	 */
	enableSVGInteraction(renderer: Renderer<V, E>): void {
		const chart = this.chart;
		const emit = renderer.emit.bind(renderer);
		const svg = d3.select(this.svgEl);
		this.clickTimer = null;

		svg.on('click', function (evt) {
			evt.stopPropagation();
			const pointerCoords = d3.zoomTransform(svg.node() as SVGGElement).invert(d3.pointer(evt));
			emit('background-click', evt, d3.select(this), renderer, {
				x: pointerCoords[0],
				y: pointerCoords[1]
			});
		});

		svg.on('dblclick', function (evt) {
			evt.stopPropagation();
			const pointerCoords = d3.zoomTransform(svg.node() as SVGGElement).invert(d3.pointer(evt));
			emit('background-dbl-click', evt, d3.select(this), renderer, {
				x: pointerCoords[0],
				y: pointerCoords[1]
			});
		});

		// Zoom control
		// FIXME: evt type
		const zoomed = (evt: any) => {
			if (this.options.useZoom === false) return;
			if (chart) chart.attr('transform', evt.transform);
		};
		const zoomEnd = () => {
			if (!this.graph || !chart) return;
			this.zoomTransformObject = d3.zoomTransform(chart.node() as Element);
			emit('zoom-end', {});
		};

		const minZoom = 0.05;
		const maxZoom = Math.max(2, Math.floor((this.graph.width as number) / this.chartSize.width));
		let zoomLevel = Math.min(1, 1 / ((this.graph.height as number) / this.chartSize.height));
		this.zoom = d3.zoom().scaleExtent([minZoom, maxZoom]).on('zoom', zoomed).on('end', zoomEnd);
		svg.call(this.zoom as any).on('dblclick.zoom', null);

		let zoomX =
			(-((this.graph.width as number) * zoomLevel * 0.5) + 0.5 * this.chartSize.width) / zoomLevel;
		let zoomY =
			(-((this.graph.height as number) * zoomLevel * 0.5) + 0.5 * this.chartSize.height) /
			zoomLevel;

		if (this.options.useStableZoomPan === true && this.zoomTransformObject !== null) {
			zoomLevel = this.zoomTransformObject.k;
			zoomX = this.zoomTransformObject.x / zoomLevel;
			zoomY = this.zoomTransformObject.y / zoomLevel;
		}
		svg.call(
			this.zoom.transform as any,
			d3.zoomIdentity.translate(0, 0).scale(zoomLevel).translate(zoomX, zoomY)
		);
	}

	enableNodeDragging(renderer: Renderer<V, E>): void {
		const options = this.options;
		const edges = this.graph.edges;
		const nodes = this.graph.nodes;
		const updateEdgePoints = this.updateEdgePoints.bind(this);
		const emitWrapper = renderer.emit.bind(renderer);

		let node: D3SelectionINode<V> | null = null;
		let nodeDraggingIds: string[] = [];

		let sufficientlyMoved = false;

		function collisionFn(p: IPoint) {
			const buffer = 10;
			for (let i = 0; i < nodes.length; i++) {
				const checkingNode = nodes[i];
				// FIXME: Thi is  a hack to get around hierarhical geometries, will need to
				// relax this guard.
				// if (node.nodes && node.nodes.length > 0) continue;
				if (p.x >= checkingNode.x - buffer && p.x <= checkingNode.x + checkingNode.width + buffer) {
					if (
						p.y >= checkingNode.y - buffer &&
						p.y <= checkingNode.y + checkingNode.height + buffer
					) {
						return true;
					}
				}
			}
			return false;
		}

		function nodeDragStart(evt: any): void {
			evt.sourceEvent.stopPropagation();

			// @ts-ignore: D3 "this"
			node = d3.select(this) as D3SelectionINode<V>;
			const childrenNodes = node.selectAll('.node') as D3SelectionINode<V>;
			nodeDraggingIds = [node.datum().id, ...childrenNodes.data().map((d) => d.id)];

			sufficientlyMoved = false;
			emitWrapper('node-drag-start', evt, node, renderer);
		}

		function nodeDragMove(evt: any) {
			const dx = evt.dx;
			const dy = evt.dy;

			if (!node) return;

			sufficientlyMoved = true;

			node.datum().x += dx;
			node.datum().y += dy;
			node.attr('transform', translate(node.datum().x, node.datum().y));

			for (let i = 0; i < edges.length; i++) {
				const edge = edges[i];
				const source = edge.source;
				const target = edge.target;

				if (nodeDraggingIds.includes(source) && nodeDraggingIds.includes(target)) {
					edge.points.forEach((p: any) => {
						p.x += dx;
						p.y += dy;
					});
				} else if (nodeDraggingIds.includes(source)) {
					edge.points[0].x += dx;
					edge.points[0].y += dy;
				} else if (nodeDraggingIds.includes(target)) {
					edge.points[edge.points.length - 1].x += dx;
					edge.points[edge.points.length - 1].y += dy;
				}
			}
			updateEdgePoints();
			emitWrapper('node-drag-move', evt, node, renderer);
		}

		function nodeDragEnd(evt: any): void {
			if (options.useAStarRouting && sufficientlyMoved) {
				for (let i = 0; i < edges.length; i++) {
					const edge = edges[i];
					const source = edge.source;
					const target = edge.target;

					if (nodeDraggingIds.includes(source) || nodeDraggingIds.includes(target)) {
						const points = edge.points;
						const start = points[0];
						const end = points[points.length - 1];
						if (edge.source === edge.target) continue;
						edge.points = getAStarPath(start, end, collisionFn, { w: 20, h: 20 });
					}
				}
				updateEdgePoints();
			}

			// Clean up
			nodeDraggingIds = [];
			emitWrapper('node-drag-end', evt, node, renderer);
		}

		const nodeDrag = d3
			.drag()
			.on('start', nodeDragStart)
			.on('end', nodeDragEnd)
			.on('drag', nodeDragMove);
		this.chart?.selectAll('.node').call(nodeDrag as any);
	}

	/* eslint-disable */
	setupDefs(): void {}

	// Need to implement
	abstract setupNodes(): void;
	abstract setupEdges(): void;
}
