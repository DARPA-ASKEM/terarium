import { select } from 'd3';
import { D3SelectionINode, Options } from '@graph-scaffolder/types';
import { useNodeTypeColorPalette, useNestedTypeColorPalette } from '@/utils/petrinet-color-palette';
import { NodeType } from '@/services/graph';
import { PetrinetRenderer, NodeData } from '@/model-representation/petrinet/petrinet-renderer';

// packing data sourced from https://hydra.nat.uni-magdeburg.de/packing/cci for up to n=200
import CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS from '@/model-representation/petrinet/circle-packing-vectors.json';
import CIRCLE_PACKING_CHILD_NORMALIZED_RADII from '@/model-representation/petrinet/circle-packing-radii.json';

const FONT_SIZE_SMALL = 18;
const FONT_SIZE_REGULAR = 24;
const FONT_SIZE_LARGE = 36;

const NODE_COLOR = '#E0E0E0';
const TEXT_COLOR = 'rgb(16, 24, 40)';

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
		const strataTypes: string[] = [];
		selection.each((d) => {
			const strataType = d.data.strataType;
			if (strataType && !strataTypes.includes(strataType)) {
				strataTypes.push(strataType as string);
			}

			// Calculate aspect ratio for each node based on the transition matrix
			const BASE_SIZE = 50;

			const transitionMatrix = this.transitionMatrices?.[d.id] ?? [];
			const matrixRowLen = transitionMatrix?.length ?? 0;
			const matrixColLen = transitionMatrix[0]?.length ?? 0;

			d.matrixRows = matrixRowLen;
			d.matrixCols = matrixColLen;

			if (matrixRowLen > 1 || matrixColLen > 1) d.data.isStratified = true;

			// Initialize aspectRatio to 1 in case the matrix is square or empty
			d.aspectRatio = 1;
			// Check and set the aspect ratio based on the dimensions of the matrix
			if (matrixRowLen > matrixColLen) {
				d.aspectRatio = matrixColLen / matrixRowLen;
				d.width = BASE_SIZE * d.aspectRatio;
				d.height = BASE_SIZE;
			} else if (matrixRowLen < matrixColLen) {
				d.aspectRatio = matrixColLen / matrixRowLen;
				d.width = BASE_SIZE;
				d.height = BASE_SIZE / d.aspectRatio;
			}

			// If either dimension is 0, it could mean that the matrix is not properly formed
			if (matrixRowLen === 0 || matrixColLen === 0 || d.aspectRatio === 1) {
				d.width = BASE_SIZE;
				d.height = BASE_SIZE;
			}
		});

		const species = selection.filter((d) => d.data.type === NodeType.State);
		const stratifiedTransitions = selection.filter(
			(d) => d.data.type === NodeType.Transition && d.data.isStratified === true
		);
		const transitions = selection.filter((d) => d.data.type === NodeType.Transition && !d.data.isStratified);
		const observables = selection.filter((d) => d.data.type === NodeType.Observable);

		// species
		species
			.append('circle')
			.classed('shape selectableNode', true)
			.attr('r', (d) => 0.55 * d.width)
			.attr('fill', (d) => (d.data.strataType ? getNodeTypeColor(d.data.strataType) : getNestedTypeColor('base')))
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1)
			.style('cursor', 'pointer');

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
			.style('fill', (d) => (d.data.strataType ? getNodeTypeColor(d.data.strataType) : NODE_COLOR))
			.style('cursor', 'pointer')
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1);

		// transitions label text
		transitions
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.html((d) => d.id);

		// transitions expression text
		transitions
			.append('text')
			.attr('y', (d) => -d.height / 2 - 8)
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', FONT_SIZE_SMALL)
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('stroke', '#FFF')
			.style('stroke-width', '3px')
			.style('stroke-linecap', 'butt')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.html((d) => {
				if (d.data.expression) return d.data.expression;
				return '';
			});

		// stratified transitions
		stratifiedTransitions
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => ((d.aspectRatio ?? 1) >= 1 ? d.width : d.width))
			.attr('height', (d) => ((d.aspectRatio ?? 1) >= 1 ? d.height : d.height))
			.attr('x', (d) => ((d.aspectRatio ?? 1) >= 1 ? -d.width * 0.5 : -d.width * 0.5))
			.attr('y', (d) => -d.height * 0.5)
			// .attr('rx', 6)
			// .attr('ry', 6)
			.style('fill', (d) => (d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#ffffff'))
			.style('cursor', 'pointer')
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1);

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
			if (nestedNodesLen >= CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS.length) {
				select(g[idx])
					.append('text')
					.classed('latex-font', true)
					.attr('y', parentRadius)
					.style('font-size', 0.6 * parentRadius)
					.attr('stroke-width', '0.5px')
					.style('text-anchor', 'middle')
					.style('paint-order', 'stroke')
					.style('fill', TEXT_COLOR)
					.style('pointer-events', 'none')
					.text(`${nestedNodesLen} groups`);

				return;
			}

			Object.entries(node).forEach((kvPair, i) => {
				if (kvPair[0] === '_key') return;
				const value = kvPair[1];
				const margin = parentRadius * 0.03;
				const childRadius = CIRCLE_PACKING_CHILD_NORMALIZED_RADII[nestedNodesLen] * parentRadius - margin;

				const xPos = parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][0] + parentX;
				const yPos = parentRadius * CIRCLE_PACKING_CHILD_NORMALIZED_VECTORS[nestedNodesLen][i][1] + parentY;

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

		stratifiedTransitions.each((d, idx, g) => {
			const transitionMatrix = this.transitionMatrices?.[d.id] ?? [];

			const matrixRowLen = transitionMatrix.length;
			const matrixColLen = transitionMatrix[0].length;
			const transitionNode = select(g[idx]);

			transitionMatrix.forEach((row, ridx) => {
				const rowIdx = ridx;
				row.forEach((col, cidx) => {
					const colIdx = cidx;
					if (col.content && col.content.value) {
						const w = d.width / matrixColLen;
						const h = d.height / matrixRowLen;

						transitionNode
							.append('rect')
							.attr('width', w)
							.attr('height', h)
							.attr('x', -d.width * 0.5 + w * colIdx)
							.attr('y', -d.height * 0.5 + h * rowIdx)
							.style('fill', d.data.strataType ? getNodeTypeColor(d.data.strataType) : '#8692a4')
							.style('cursor', 'pointer')
							.attr('stroke', w > 1 && h > 1 ? '#ffffff' : null)
							.attr('stroke-width', 1);
					}
				});
			});
		});

		// species text
		species
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.attr('stroke-width', '0.5px')
			.style('text-anchor', 'middle')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.style('text-shadow', '1px 0 0 #fff, 0 -1px 0 #fff, -1px 0 0 #fff, 0 1px 0 #fff')
			.text((d) => d.id);

		// observables
		observables
			.append('rect')
			.classed('shape selectableNode', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.attr('y', (d) => -d.height * 0.5)
			.attr('x', (d) => -d.width * 0.5)
			.attr('rx', '6')
			.attr('ry', '6')
			.style('fill', NODE_COLOR)
			.style('cursor', 'pointer')
			.attr('stroke', NODE_COLOR)
			.attr('stroke-width', 1);

		// observables text
		observables
			.append('text')
			.attr('y', (d) => setFontSize(d.id) / 4)
			.style('text-anchor', 'middle')
			.classed('latex-font', true)
			.style('font-style', 'italic')
			.style('font-size', (d) => setFontSize(d.id))
			.style('stroke', '#FFF')
			.style('paint-order', 'stroke')
			.style('fill', TEXT_COLOR)
			.style('pointer-events', 'none')
			.text((d) => d.id);
	}
}
