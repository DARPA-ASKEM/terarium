import { ref } from 'vue';
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

const nodeTypeColorMap = ref<{ [id: string]: string }>({});
const count = ref(0);

function getNodeTypeColor(id: string): string {
	if (!id) {
		return '#FFF';
	}
	const color = nodeTypeColorMap.value[id];
	if (!color) {
		setNodeTypeColor(id);
	}
	return nodeTypeColorMap.value[id];
}

function setNodeTypeColor(id: string): void {
	if (!id) {
		return;
	}
	if (nodeTypeColorMap.value[id]) {
		return;
	}
	nodeTypeColorMap.value[id] = nodeTypeColors[count.value];
	if (count.value >= nodeTypeColors.length) {
		count.value = 0;
	} else {
		count.value++;
	}
}

export function useNodeTypeColorMap() {
	return { getNodeTypeColor, setNodeTypeColor };
}
