import * as d3 from 'd3';
// Colour palette is from https://colorbrewer2.org/#type=qualitative&scheme=Paired&n=12
export const nodeTypeColors = [
	'#a6cee3',
	'#1f78b4',
	'#b2df8a',
	'#33a02c',
	'#fb9a99',
	'#e31a1c',
	'#fdbf6f',
	'#ff7f00',
	'#cab2d6',
	'#6a3d9a',
	'#ffff99',
	'#b15928'
];

export const nestedTypeColors = [
	'#c9e9a9',
	'#aedc30',
	'#53d25e',
	'#03c386',
	'#01a9a1',
	'#0098b3',
	'#007eb3',
	'#00659f',
	'#3e4d83'
];

let domain: string[] = [];

let scaleNodeType: d3.ScaleOrdinal<string, string, never> = d3.scaleOrdinal(nodeTypeColors).domain([]);

function getNodeTypeColor(id: string): string {
	return scaleNodeType(id);
}

function setNodeTypeColor(ids: string[]): void {
	ids.forEach((id) => {
		if (!domain.includes(id)) {
			domain.push(id);
		}
	});
	scaleNodeType = d3.scaleOrdinal(nodeTypeColors).domain(domain);
}

export function useNodeTypeColorPalette() {
	return { getNodeTypeColor, setNodeTypeColor };
}

let scaleNestedType = d3.scaleLinear().domain([0, 2]);

function getNestedTypeColor(id: string): string {
	const index = domain.indexOf(id);
	return d3.piecewise(d3.interpolateRgb.gamma(2.2), nestedTypeColors)(scaleNestedType(index === -1 ? 0 : index));
}

function setNestedTypeColor(ids: string[]): void {
	domain = ids;
	scaleNestedType = d3.scaleLinear().domain([0, ids.length]);
}

export function useNestedTypeColorPalette() {
	return { getNestedTypeColor, setNestedTypeColor };
}
