<template>
	<header>
		<div>
			<b>{{ initialId }}</b>
			<span v-if="getInitialName(modelConfiguration, initialId)">{{
				' | ' + getInitialName(modelConfiguration, initialId)
			}}</span>
			<span v-if="getInitialUnit(modelConfiguration, initialId)" class="unit"
				>({{ getInitialUnit(modelConfiguration, initialId) }})</span
			>
		</div>
		<span>{{ getInitialDescription(modelConfiguration, initialId) }}</span>
	</header>
	<main>
		<tera-input
			label="Expression"
			:model-value="getInitialExpression(modelConfiguration, initialId)"
			@update:model-value="emit('update-expression')"
		/>
		<Button :label="getSourceLabel(initialId)" text @click="sourceOpen = !sourceOpen" />
		<div>test</div>
	</main>
	<footer v-if="sourceOpen">
		<tera-input
			label="Source / notes / etc"
			:model-value="getInitialSource(modelConfiguration, initialId)"
			@update:model-value="emit('update-source')"
		/>
	</footer>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import {
	getInitialName,
	getInitialDescription,
	getInitialUnit,
	getInitialExpression,
	getInitialSource
} from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { ref } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	initialId: string;
}>();

const emit = defineEmits(['update-expression', 'update-source']);

const sourceOpen = ref(false);

function getSourceLabel(initialId) {
	if (sourceOpen.value) return 'Hide source';
	if (!getInitialSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}
</script>

<style scoped>
header {
	display: flex;
	justify-content: space-between;
}

.unit {
	padding-left: var(--gap-small);
}

main {
	display: flex;
}
</style>
