import { select } from 'd3';
import { D3SelectionINode, IGraph } from '@graph-scaffolder/types';
import { Options } from '@graph-scaffolder/core/renderer';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

type AsyncFunction<A, O> = (args: A) => Promise<O>;
type AsyncLayoutFunction<V, E> = AsyncFunction<IGraph<V, E>, IGraph<V, E>>;
type LayoutFunction<V, E> = (args: IGraph<V, E>) => IGraph<V, E>;

export interface NestedPetrinetOptions {
	el?: HTMLDivElement | null;
	nestedMap?: { [baseNodeId: string]: string[] };
	runLayout: AsyncLayoutFunction<any, any> | LayoutFunction<any, any>;

	useZoom?: boolean;
	zoomRange?: [number, number];
	useStableLayout?: boolean;

	// Attempt to use the same set of zoom parameters across layout changes
	useStableZoomPan?: boolean;

	// This is getting around algorithms that do not provide stand-alone routing capabilities, in
	// which case we can internally route using A-star
	useAStarRouting?: boolean;

	// Whether to show grid
	useGrid?: boolean;

	// The css class for custom dragging event
	dragSelector?: string;
}

const CIRCLE_PACKING_CHILD_NORMALIZED_RADIUS = [
	0,
	1,
	0.5,
	0.464101615138,
	0.414213562373 // 0.370191908159, 0.333333333333, 0.333333333333, 0.302593388349, 0.276768653914, 0.262258924190
];

// [x, y]
const CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS = [
	[],
	[[0, 0]],
	[
		[0, 0.5],
		[0, -0.5]
	],
	[
		[0, 0.535898384862],
		[-0.46410161513, -0.26794919243],
		[0.46410161513, -0.26794919243]
	],
	[
		[0.414213562373, 0.414213562373],
		[-0.414213562373, 0.414213562373],
		[-0.414213562373, -0.414213562373],
		[0.414213562373, -0.414213562373]
	]
];

const CIRCLE_MARGIN_CONST = 1;

export class NestedPetrinetRenderer extends PetrinetRenderer {
	declare options: NestedPetrinetOptions;

	nestedMap: any;

	// override type of constructor argument
	constructor(options: NestedPetrinetOptions) {
		super(options as Options);
		this.nestedMap = options.nestedMap;
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		super.renderNodes(selection);

		const species = selection.filter((d) => d.data.type === NodeType.State);

		species.each((d, idx, g) => {
			const nestedNodes = this.nestedMap[d.id] ?? [];
			const nestedNodesLen = nestedNodes.length;
			for (let i = 0; i < nestedNodesLen; i++) {
				// skip nodes that can't be laid out for now
				if (nestedNodesLen >= CIRCLE_PACKING_CHILD_NORMALIZED_RADIUS.length) {
					continue;
				}
				const nodeId = nestedNodes[i];
				const parentRadius = 0.55 * d.width;
				const childRadius =
					CIRCLE_PACKING_CHILD_NORMALIZED_RADIUS[nestedNodesLen] *
					CIRCLE_MARGIN_CONST *
					parentRadius;
				select(g[idx])
					.append('circle')
					.classed('shape', true)
					.attr('r', () => childRadius) // FIXME: need to adjust edge from sqaure mapping to circle
					.attr(
						'cx',
						() => parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0]
					)
					.attr(
						'cy',
						() => parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1]
					)
					.attr('fill', () => 'transparent')
					.attr('stroke', '#999999')
					.attr('stroke-width', 1)
					.style('cursor', 'pointer');

				select(g[idx])
					.append('text')
					.attr(
						'x',
						() => parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0]
					)
					.attr(
						'y',
						() => 5 + parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1]
					)
					.style('text-anchor', 'middle')
					.style('paint-order', 'stroke')
					// .style('stroke', (d) =>
					// 	d.data.strataType ? strataTypeColors[strataTypes.indexOf(d.data.strataType)] : '#FFF'
					// )
					// .style('stroke-width', '3px')
					// .style('stroke-linecap', 'butt')
					.style('fill', 'var(--text-color-primary')
					.style('pointer-events', 'none')
					.html(() => nodeId ?? '');
			}
		});
	}
}
