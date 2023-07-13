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

let scale: d3.ScaleOrdinal<string, string, never> = d3.scaleOrdinal(nodeTypeColors).domain([]);
const domain: string[] = [];

function getNodeTypeColor(id: string): string {
	return scale(id);
}

function setNodeTypeColor(ids: string[]): void {
	ids.forEach((id) => {
		if (!domain.includes(id)) {
			domain.push(id);
		}
	});
	scale = d3.scaleOrdinal(nodeTypeColors).domain(domain);
}

export function useNodeTypeColorPalette() {
	return { getNodeTypeColor, setNodeTypeColor };
}
