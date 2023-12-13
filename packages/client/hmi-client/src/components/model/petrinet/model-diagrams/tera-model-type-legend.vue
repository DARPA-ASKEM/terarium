<template>
	<section v-if="stateTypes || transitionTypes">
		<ul>
			<li v-for="(type, i) in stateTypes" :key="i">
				<div class="legend-key-circle" :style="getLegendKeyStyle(type)" />
				{{ type }}
			</li>
		</ul>
		<ul>
			<li v-for="(type, i) in transitionTypes" :key="i">
				<div class="legend-key-square" :style="getLegendKeyStyle(type)" />
				{{ type }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Model } from '@/types/Types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';

const props = defineProps<{
	model: Model;
}>();

const { getNodeTypeColor } = useNodeTypeColorPalette();

const stateTypes = computed<string[]>(() =>
	props.model.semantics?.typing?.system?.model.states.map((s) => s.name)
);
const transitionTypes = computed<string[]>(() =>
	props.model.semantics?.typing?.system?.model.transitions.map((t) => t.properties?.name)
);

function getLegendKeyStyle(id: string) {
	if (!id) {
		return {
			backgroundColor: 'var(--petri-nodeFill)'
		};
	}
	return {
		backgroundColor: getNodeTypeColor(id)
	};
}
</script>

<style scoped>
.legend-key-circle {
	height: 24px;
	width: 24px;
	border-radius: 12px;
}

.legend-key-square {
	height: 24px;
	width: 24px;
	border-radius: 4px;
}

section.legend ul {
	display: flex;
	gap: 0.5rem;
	list-style-type: none;
	flex-direction: row;
}

section.legend li {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
</style>
