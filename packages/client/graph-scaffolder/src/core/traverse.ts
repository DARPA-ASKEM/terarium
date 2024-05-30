import BinaryHeap from '../utils/binary-heap';
import { IEdge, IGraph, INode, IPoint } from '../types';

// Functions to walk the graph
export const traverseNode = <T>(node: INode<T>, callback: (node: INode<T>) => void): void => {
	callback(node);
	for (let i = 0; i < node.nodes.length; i++) {
		traverseNode(node.nodes[i], callback);
	}
};
export const traverseGraph = <V, E>(
	graph: IGraph<V, E>,
	callback: (node: INode<V>) => void
): void => {
	for (let i = 0; i < graph.nodes.length; i++) {
		traverseNode(graph.nodes[i], callback);
	}
};

/**
 * Returns a flat representation of all nodes and edges.
 */
export const flattenGraph = <V, E>(
	graph: IGraph<V, E>
): { nodes: INode<V>[]; edges: IEdge<E>[] } => {
	let nodes: INode<V>[] = [];
	traverseGraph(graph, (node) => {
		nodes = nodes.concat(node);
	});

	return {
		nodes,
		edges: graph.edges
	};
};

/**
 * AStar path finding algorithm.
 */
export interface IGrid {
	w: number;
	h: number;
}
type ColliderFn = (p: IPoint) => boolean;

export const getAStarPath = (
	start: IPoint,
	goal: IPoint,
	collider: ColliderFn,
	gridCell: IGrid = { w: 10, h: 10 },
	searchLimit = 7000
): IPoint[] => {
	// Encoding helpers. We encode the XY coordinate as a single number to allow fast heuristic score lookup without
	// incurring heavy memory cost of building a world-grid (we only store points we have visisted). Interestingly
	// off hand testing using more native memory data structures (DataView, ArrayBuffer) to do high/low bit-mask encoding
	// is not any faster than this scheme...though it probably worth a more thorough investigation if time allows as
	// the memory-buffer method would be a more generalized solution - DC Feb 2023.
	const PRIME = 756065179;
	const SIZE = 8000;
	const pAsKey = (p: IPoint): number => (Math.round(p.x) + SIZE) * PRIME + (p.y + SIZE);
	const keyAsP = (v: number): IPoint => {
		const y = v % PRIME;
		const x = (v - y) / PRIME;
		return { x: x - SIZE, y: y - SIZE };
	};

	// Math helpers
	const sqDifference = (a: number, b: number) => (a - b) * (a - b);
	const sqDistance = (p1: IPoint, p2: IPoint) =>
		sqDifference(p1.x, p2.x) + sqDifference(p1.y, p2.y);

	const heuristic = (p: IPoint) => sqDistance(p, goal) * 1.2;
	const pEqual = (p1: IPoint, p2: IPoint) => p1.x === p2.x && p1.y === p2.y;

	const nearestValue = (a: number, v: number) => Math.round(a / v) * v;
	const nearestOnGrid = (a: IPoint) => ({
		x: nearestValue(a.x, gridCell.w),
		y: nearestValue(a.y, gridCell.h)
	});
	const startOnGrid = nearestOnGrid(start);
	const goalOnGrid = nearestOnGrid(goal);

	// AStar starts
	if (pEqual(startOnGrid, goalOnGrid)) {
		return [start, goal];
	}

	const cameFrom: number[] = [];
	const gScore: { [key: string]: number } = {};
	const fScore: { [key: string]: number } = {};
	const heap = new BinaryHeap<number>(9999, (e) => fScore[e]);

	const getNeighbours = (p: IPoint) => [
		// orthogonals
		{ x: p.x + gridCell.w, y: p.y },
		{ x: p.x - gridCell.w, y: p.y },
		{ x: p.x, y: p.y - gridCell.h },
		{ x: p.x, y: p.y + gridCell.h },

		// diagonals
		{ x: p.x + gridCell.w, y: p.y + gridCell.h },
		{ x: p.x + gridCell.w, y: p.y - gridCell.h },
		{ x: p.x - gridCell.w, y: p.y - gridCell.h },
		{ x: p.x - gridCell.w, y: p.y + gridCell.h }
	];

	// TOOD: consider replacing pAsKey/keyAsP/gScore/fScore with a js Map or something with a cleaner feeling interface
	heap.insert(pAsKey(startOnGrid));
	gScore[pAsKey(startOnGrid)] = 0;
	fScore[pAsKey(startOnGrid)] = heuristic(startOnGrid);

	let count = 0;
	while (heap.size > 0) {
		count += 1;
		if (count > searchLimit) {
			break;
		}

		const currentKey = heap.pop();
		const current = keyAsP(currentKey);

		const neighbours = getNeighbours(current);
		// TODO: getJumpPoints used to be here but not anymore no one knows where it went, i guess it went home

		for (let i = 0; i < neighbours.length; i++) {
			const neighbour = neighbours[i];
			const neighbourKey = pAsKey(neighbour);

			if (pEqual(neighbour, goalOnGrid)) {
				const path = [pAsKey(goal), currentKey];
				while (cameFrom[path[path.length - 1]] !== undefined) {
					path.push(cameFrom[path[path.length - 1]]);
				}
				path.push(pAsKey(start));
				return path.map(keyAsP).reverse();
			}

			if (collider(neighbour)) continue;

			const tentativeScore = gScore[pAsKey(current)] + sqDistance(current, neighbour);
			if (gScore[neighbourKey] === undefined || tentativeScore < gScore[neighbourKey]) {
				cameFrom[neighbourKey] = currentKey;
				gScore[neighbourKey] = tentativeScore;
				fScore[neighbourKey] = tentativeScore + heuristic(neighbour);
				if (heap.data.indexOf(neighbourKey) === -1) {
					heap.insert(neighbourKey);
				}
			}
		}
	}
	return [start, goal];
};
