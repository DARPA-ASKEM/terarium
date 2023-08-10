import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

// packing data sourced from https://hydra.nat.uni-magdeburg.de/packing/cci for up to n=200
import CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS from '@/model-representation/petrinet/circle-packing-vectors.json';
import CIRCLE_PACKING_CHILD_NORMALIZED_RADII from '@/model-representation/petrinet/circle-packing-radii.json';

export interface NestedPetrinetOptions extends Options {
	nestedMap?: { [baseNodeId: string]: any };
	transitionMatrices?: { [baseTransitionId: string]: any[] };
}

const CIRCLE_MARGIN_CONST = 1;
const { getNodeTypeColor } = useNodeTypeColorPalette();

export class NestedPetrinetRenderer extends PetrinetRenderer {
	nestedMap: any;

	transitionMatrices: any;

	// override type of constructor argument
	constructor(options: NestedPetrinetOptions) {
		super(options as Options);
		this.nestedMap = options.nestedMap;
		this.transitionMatrices = options.transitionMatrices;
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === NodeType.State);
		const transitions = selection.filter((d) => d.data.type === NodeType.Transition);

		const strataTypes: string[] = [];
		selection.each((d) => {
			const strataType = d.data.strataType;
			if (strataType && !strataTypes.includes(strataType)) {
				strataTypes.push(strataType as string);
			}
		});
		// transitions
		transitions
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5)
			.attr('rx', '6')
			.attr('ry', '6')
			.style('fill', (d) =>
				d.data.strataType ? getNodeTypeColor(d.data.strataType) : 'var(--petri-nodeFill'
			)
			.style('cursor', 'pointer')
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1);

		// species
		species
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', (d) => 0.55 * d.width) // FIXME: need to adjust edge from sqaure mapping to circle
			.attr('fill', (d) =>
				d.data.strataType ? getNodeTypeColor(d.data.strataType) : 'var(--petri-nodeFill)'
			)
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

		const renderNestedNodes = (node, parentRadius, parentX, parentY, g, idx) => {
			if (Object.keys(node).length === 0) {
				return;
			}

			const nestedNodesLen = Object.keys(node).length;

			Object.entries(node).forEach(([key, value], i) => {
				const childRadius =
					CIRCLE_PACKING_CHILD_NORMALIZED_RADII[nestedNodesLen] *
					CIRCLE_MARGIN_CONST *
					parentRadius;

				const xPos =
					parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0] + parentX;
				const yPos =
					parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1] + parentY;

				renderNestedNodes(value, childRadius, xPos, yPos, g, idx);

				select(g[idx])
					.append('circle')
					.classed('shape', true)
					.attr('r', () => childRadius)
					.attr('cx', xPos)
					.attr('cy', yPos)
					.attr('fill', () => '#ffffffaa')
					.style('cursor', 'pointer');

				select(g[idx])
					.append('text')
					.attr('x', xPos)
					.attr('y', 5 + yPos)
					.style('text-anchor', 'middle')
					.style('paint-order', 'stroke')
					.style('fill', 'var(--text-color-subdued')
					.style('pointer-events', 'none')
					.html(() => key ?? '');
			});
		};

		species.each((d, idx, g) => {
			const nestedMap = this.nestedMap?.[d.id] ?? {};
			const parentRadius = 0.55 * d.width;
			renderNestedNodes(nestedMap, parentRadius, 0, 0, g, idx);
		});

		transitions.each((d, idx, g) => {
			const transitionMatrix = this.transitionMatrices?.[d.id] ?? [];
			const transitionMatrixLen = transitionMatrix.length;
			const transitionNode = select(g[idx]);

			for (let i = 1; i < transitionMatrixLen; i++) {
				const position = (d.width / transitionMatrixLen) * i;

				transitionNode
					.append('line')
					.attr('class', 'gridline')
					.attr('x1', -d.width * 0.5)
					.attr('y1', -d.width * 0.5 + position)
					.attr('x2', d.width * 0.5)
					.attr('y2', -d.width * 0.5 + position)
					.attr('stroke', '#ffffffcf');
				transitionNode
					.append('line')
					.attr('class', 'gridline')
					.attr('y1', -d.width * 0.5)
					.attr('x1', -d.width * 0.5 + position)
					.attr('y2', d.width * 0.5)
					.attr('x2', -d.width * 0.5 + position)
					.attr('stroke', '#ffffffcf');
			}

			transitionMatrix.forEach((row) => {
				row.forEach((col) => {
					if (col.value) {
						transitionNode
							.append('rect')
							.attr('width', d.width / transitionMatrixLen)
							.attr('height', d.width / transitionMatrixLen)
							.attr('x', -d.width * 0.5 + (d.width / transitionMatrixLen) * col.row)
							.attr('y', -d.width * 0.5 + (d.width / transitionMatrixLen) * col.col)
							.attr('rx', '2')
							.attr('ry', '2')
							.style('fill', d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#7fffd4aa')
							.style('cursor', 'pointer')
							.attr('stroke', '#ffffffcf')
							.attr('stroke-width', 1);
					}
				});
			});
		});

		// transitions label text
		transitions
			.append('text')
			.attr('y', () => 5)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.html((d) => d.id);

		// transitions expression text
		transitions
			.append('text')
			.attr('y', (d) => -d.height / 2 - 5)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '3px')
			.style('stroke-linecap', 'butt')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.html((d) => {
				const rate = this.graph.amr.semantics.ode?.rates?.find((r) => r.target === d.id);
				if (rate) {
					return rate.expression;
				}
				return '';
			});

		// species text
		species
			.append('text')
			.attr('y', () => 5)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary)')
			.style('pointer-events', 'none')
			.text((d) => d.id);
	}
}
