import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';
import { useNodeTypeColorPalette, useNestedTypeColorPalette } from '@/utils/petrinet-color-palette';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

// packing data sourced from https://hydra.nat.uni-magdeburg.de/packing/cci for up to n=200
import CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS from '@/model-representation/petrinet/circle-packing-vectors.json';
import CIRCLE_PACKING_CHILD_NORMALIZED_RADII from '@/model-representation/petrinet/circle-packing-radii.json';

export interface NestedPetrinetOptions extends Options {
	nestedMap?: { [baseNodeId: string]: any };
	transitionMatrices?: { [baseTransitionId: string]: any[] };
	dims?: string[];
}

const CIRCLE_MARGIN = 2;
const { getNodeTypeColor } = useNodeTypeColorPalette();
const { getNestedTypeColor, setNestedTypeColor } = useNestedTypeColorPalette();

export class NestedPetrinetRenderer extends PetrinetRenderer {
	nestedMap?: { [baseNodeId: string]: any };

	transitionMatrices?: { [baseTransitionId: string]: any[] };

	dims?: string[];

	// override type of constructor argument
	constructor(options: NestedPetrinetOptions) {
		super(options as Options);
		this.nestedMap = options.nestedMap;
		this.transitionMatrices = options.transitionMatrices;
		this.dims = options.dims ?? [];
		setNestedTypeColor(this.dims ?? []);
	}

	get depthColorList() {
		return this.dims?.map((v) => getNestedTypeColor(v)) ?? [];
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
			.attr('rx', 6)
			.attr('ry', 6)
			.style('fill', (d) => (d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#ffffff'))
			.style('cursor', 'pointer')
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1);

		// species
		species
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', (d) => 0.55 * d.width) // FIXME: need to adjust edge from sqaure mapping to circle
			.attr('fill', (d) =>
				d.data.strataType ? getNodeTypeColor(d.data.strataType) : getNestedTypeColor('base')
			)
			.attr('stroke', 'var(--petri-nodeBorder)')
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

		const renderNestedNodes = (
			node: { [baseNodeId: string]: any },
			parentRadius: number,
			parentX: number,
			parentY: number,
			g: any[] | ArrayLike<any>,
			idx: number,
			depth: number
		) => {
			// this function recursively iterates through the nested object representation of the
			// stratified model states and draws the corresponding circles in post-order

			if (Object.keys(node).length === 0) {
				return;
			}

			const nestedNodesLen = Object.keys(node).filter((d) => d !== '_key').length;

			Object.entries(node).forEach((kvPair, i) => {
				if (kvPair[0] === '_key') return;
				const value = kvPair[1];
				const childRadius =
					CIRCLE_PACKING_CHILD_NORMALIZED_RADII[nestedNodesLen] * parentRadius - CIRCLE_MARGIN;

				const xPos =
					parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0] + parentX;
				const yPos =
					parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1] + parentY;

				select(g[idx])
					.append('circle')
					.classed('shape', true)
					.attr('r', () => childRadius)
					.attr('cx', xPos)
					.attr('cy', yPos)
					.attr('fill', () => getNestedTypeColor(node._key))
					.style('cursor', 'pointer');

				renderNestedNodes(value, childRadius, xPos, yPos, g, idx, depth + 1);
			});
		};

		species.each((d, idx, g) => {
			const nestedMap = this.nestedMap?.[d.id] ?? {};
			const parentRadius = 0.55 * d.width;
			renderNestedNodes(nestedMap, parentRadius, 0, 0, g, idx, 1);
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
					if (col.content) {
						transitionNode
							.append('rect')
							.attr('width', d.width / transitionMatrixLen)
							.attr('height', d.width / transitionMatrixLen)
							.attr('x', -d.width * 0.5 + (d.width / transitionMatrixLen) * col.col)
							.attr('y', -d.width * 0.5 + (d.width / transitionMatrixLen) * col.row)
							.attr('rx', 2)
							.attr('ry', 2)
							.style('fill', d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#8692a4')
							.style('cursor', 'pointer')
							.attr('stroke', '#ffffff')
							.attr('stroke-width', 1);
					}
				});
			});
		});

		// transitions label text
		// transitions
		// 	.append('text')
		// 	.attr('y', () => 5)
		// 	.style('text-anchor', 'middle')
		// 	.style('paint-order', 'stroke')
		// 	.style('fill', 'var(--text-color-primary')
		// 	.style('pointer-events', 'none')
		// 	.html((d) => d.id);

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
			.attr('y', () => 8)
			.style('font-size', '24px')
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary)')
			.style('pointer-events', 'none')
			.style('text-shadow', '1px 0 0 #fff, 0 -1px 0 #fff, -1px 0 0 #fff, 0 1px 0 #fff')
			.text((d) => d.id);
	}
}
