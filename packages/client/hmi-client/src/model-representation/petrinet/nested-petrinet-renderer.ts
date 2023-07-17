import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

// packing data sourced from https://hydra.nat.uni-magdeburg.de/packing/cci for up to n=200
import CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS from '@/model-representation/petrinet/circle-packing-vectors.json';
import CIRCLE_PACKING_CHILD_NORMALIZED_RADII from '@/model-representation/petrinet/circle-packing-radii.json';

export interface NestedPetrinetOptions extends Options {
	nestedMap?: { [baseNodeId: string]: string[] };
}

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
				if (nestedNodesLen >= CIRCLE_PACKING_CHILD_NORMALIZED_RADII.length) {
					continue;
				}
				const nodeId = nestedNodes[i];
				const parentRadius = 0.55 * d.width;
				const childRadius =
					CIRCLE_PACKING_CHILD_NORMALIZED_RADII[nestedNodesLen] *
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
