import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';
import { useNodeTypeColorPalette, useNestedTypeColorPalette } from '@/utils/petrinet-color-palette';

import { NodeType, PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NodeData } from '@/model-representation/petrinet/petrinet-service';

// packing data sourced from https://hydra.nat.uni-magdeburg.de/packing/cci for up to n=200
import CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS from '@/model-representation/petrinet/circle-packing-vectors.json';
import CIRCLE_PACKING_CHILD_NORMALIZED_RADII from '@/model-representation/petrinet/circle-packing-radii.json';

const FONT_SIZE_SMALL = 18;
const FONT_SIZE_REGULAR = 24;
const FONT_SIZE_LARGE = 36;

function setFontSize(label: string) {
	if (label.length < 3) {
		return FONT_SIZE_LARGE;
	}
	if (label.length < 10) {
		return FONT_SIZE_REGULAR;
	}
	return FONT_SIZE_SMALL;
}

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

		// Calculate aspect ratio for each node based on the transition matrix
		selection.each((d) => {
			const transitionMatrix = this.transitionMatrices?.[d.id] ?? [];
			const matrixRowLen = transitionMatrix?.length ?? 0;
			const matrixColLen = transitionMatrix[0]?.length ?? 0;

			// Initialize aspectRatio to 1 in case the matrix is square or empty
			d.aspectRatio = 1;

			// Check and set the aspect ratio based on the dimensions of the matrix
			if (matrixRowLen > matrixColLen) {
				d.aspectRatio = matrixColLen / matrixRowLen;
			} else if (matrixColLen > matrixRowLen) {
				d.aspectRatio = matrixRowLen / matrixColLen;
			}

			// If either dimension is 0, it could mean that the matrix is not properly formed
			if (matrixRowLen === 0 || matrixColLen === 0) {
				d.aspectRatio = 1;
			}
		});

		// transitions
		transitions
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => d.width * d.aspectRatio!)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5 * d.aspectRatio!)
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

			const matrixRowLen = transitionMatrix.length;
			const matrixColLen = transitionMatrix[0].length;
			const transitionNode = select(g[idx]);

			transitionMatrix.forEach((row, ridx) => {
				const rowIdx = ridx;
				row.forEach((col, cidx) => {
					const colIdx = cidx;
					if (col.content && col.content.value) {
						transitionNode
							.append('rect')
							.attr('width', (d.width / matrixColLen) * d.aspectRatio!)
							.attr('height', d.height / matrixRowLen)
							.attr(
								'x',
								-d.width * 0.5 * d.aspectRatio! + (d.width / matrixColLen) * d.aspectRatio! * colIdx
							)
							.attr('y', -d.height * 0.5 + (d.height / matrixRowLen) * rowIdx)
							.attr('rx', 2)
							.attr('ry', 2)
							.style('fill', d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#8692a4')
							.style('cursor', 'pointer')
							.attr('stroke', '#ffffff')
							.attr('stroke-width', 1);
					}
					// Draw label for number of columns
					transitionNode
						.append('text')
						.attr('x', 0)
						.attr('y', -d.height * 0.6)
						.attr('text-anchor', 'middle') // This will center-align the text horizontally
						.text(matrixColLen)
						.style('fill', '#cccccc')
						.style('font-size', '7px');

					// Draw label for number of rows
					transitionNode
						.append('text')
						.attr('x', (-d.width * d.aspectRatio!) / 2 - 8)
						.attr('y', (-d.height * d.aspectRatio!) / 2 + 12)
						.attr('text-anchor', 'right') // This will center-align the text horizontally
						.text(matrixRowLen)
						.style('fill', '#cccccc')
						.style('font-size', '7px');
				});
			});
		});

		/* Don't show transition labels because we're showing matrices here */
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
			.attr('y', (d) => -d.height / 2 - 8)
			.style('font-family', 'STIX Two Text, serif')
			.style('font-style', 'italic')
			.style('font-size', FONT_SIZE_SMALL)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '3px')
			.style('stroke-linecap', 'butt')
			.style('fill', 'var(--text-color-primary')
			.style('pointer-events', 'none')
			.html((d) => {
				if (!this.graph.amr) return '';
				const rate = this.graph.amr.semantics.ode?.rates?.find((r) => r.target === d.id);
				if (rate) {
					return rate.expression;
				}
				return '';
			});

		// species text
		species
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('font-family', 'STIX Two Text, serif')
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.attr('stroke-width', '0.5px')
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('fill', 'var(--text-color-primary)')
			.style('pointer-events', 'none')
			.style('text-shadow', '1px 0 0 #fff, 0 -1px 0 #fff, -1px 0 0 #fff, 0 1px 0 #fff')
			.text((d) => d.id);
	}
}
