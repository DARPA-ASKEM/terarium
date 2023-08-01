<template>
	<section class="legend" v-if="stateTypes || transitionTypes">
		<ul>
			<li v-for="(type, i) in stateTypes" :key="i">
				<div class="legend-key-circle" :style="getLegendKeyStyle(type ?? '')" />
				{{ type }}
			</li>
		</ul>
		<ul>
			<li v-for="(type, i) in transitionTypes" :key="i">
				<div class="legend-key-square" :style="getLegendKeyStyle(type ?? '')" />
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

const stateTypes = computed(() =>
	props.model.semantics?.typing?.system?.model.states.map((s) => s.name)
);
const transitionTypes = computed(() =>
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
.legend {
	position: absolute;
	bottom: 0;
	z-index: 1;
	margin-bottom: 1rem;
	margin-left: 1rem;
	display: flex;
	gap: 1rem;
	background-color: var(--surface-section);
	border-radius: 0.5rem;
	padding: 0.5rem;
}

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
