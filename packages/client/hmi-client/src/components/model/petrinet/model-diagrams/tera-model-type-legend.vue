<template>
	<section v-if="stateTypes || transitionTypes">
		<ul class="flex flex-row mr-3">
			<li v-for="(type, i) in stateTypes" :key="i" class="legend-item">
				<div class="legend-key-circle" :style="getLegendKeyStyle(type)" />
				{{ type }}
			</li>
		</ul>
		<ul class="flex flex-row">
			<li v-for="(type, i) in transitionTypes" :key="i" class="legend-item">
				<div class="legend-key-square" :style="getLegendKeyStyle(type)" />
				{{ type }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import type { Model } from '@/types/Types';
import { useNodeTypeColorPalette } from '@/utils/petrinet-color-palette';
import { computed } from 'vue';

const props = defineProps<{
	model: Model;
}>();

const { getNodeTypeColor } = useNodeTypeColorPalette();

const stateTypes = computed<string[]>(
	() => props.model.semantics?.typing?.system?.model.states.map((s) => s.name)
);
const transitionTypes = computed<string[]>(
	() => props.model.semantics?.typing?.system?.model.transitions.map((t) => t.properties?.name)
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
.legend-item {
	display: flex;
	align-items: center;
	gap: var(--gap-xsmall);
	margin-right: var(--gap-small);
	font-size: var(--font-caption);
}
.legend-key-circle {
	height: 1rem;
	width: 1rem;
	border-radius: 12px;
}

.legend-key-square {
	height: 1rem;
	width: 1rem;
	border-radius: 2px;
}
</style>
