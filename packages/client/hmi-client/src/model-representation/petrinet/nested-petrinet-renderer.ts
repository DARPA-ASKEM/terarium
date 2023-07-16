import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

export interface NestedPetrinetOptions extends Options {
	nestedMap?: { [baseNodeId: string]: string[] };
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
	nestedMap: any;

	// override type of constructor argument
	constructor(options: NestedPetrinetOptions) {
		super(options as Options);
		this.nestedMap = options.nestedMap;
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		super.renderNodes(selection);

		const states = selection.filter((d) => d.data.type === NodeType.State);
		states.each((d, idx, g) => {
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

				const xPos = parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0];
				const yPos = parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1];

				select(g[idx])
					.append('circle')
					.classed('shape', true)
					.attr('r', () => childRadius) // FIXME: need to adjust edge from sqaure mapping to circle
					.attr('cx', xPos)
					.attr('cy', yPos)
					.attr('fill', () => 'transparent')
					.attr('stroke', '#999999')
					.attr('stroke-width', 1)
					.style('cursor', 'pointer');

				select(g[idx])
					.append('text')
					.attr('x', xPos)
					.attr('y', 5 + yPos)
					.style('text-anchor', 'middle')
					.style('paint-order', 'stroke')
					.style('fill', 'var(--text-color-primary')
					.style('pointer-events', 'none')
					.html(() => nodeId ?? '');
			}
		});
	}
}
