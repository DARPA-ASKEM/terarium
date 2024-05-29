<template>
	<header>
		<strong>{{ initialId }}</strong>
		<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
		<span v-if="unit" class="ml-2">({{ unit }})</span>
		<span v-if="description" class="ml-4">{{ description }}</span>
	</header>
	<main>
		<span class="expression">
			<tera-input
				label="Expression"
				:model-value="getInitialExpression(modelConfiguration, initialId)"
				@update:model-value="emit('update-expression', { id: initialId, value: $event })"
			/>
		</span>
		<Button
			:label="getSourceLabel(initialId)"
			text
			size="small"
			@click="sourceOpen = !sourceOpen"
		/>
	</main>
	<footer v-if="sourceOpen">
		<tera-input
			label="Source / notes / etc"
			:model-value="getInitialSource(modelConfiguration, initialId)"
			@update:model-value="emit('update-source', { id: initialId, value: $event })"
		/>
	</footer>
</template>

<script setup lang="ts">
import { ModelConfigurationLegacy } from '@/types/Types';
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
	modelConfiguration: ModelConfigurationLegacy;
	initialId: string;
}>();

const emit = defineEmits(['update-expression', 'update-source']);

const name = getInitialName(props.modelConfiguration, props.initialId);
const unit = getInitialUnit(props.modelConfiguration, props.initialId);
const description = getInitialDescription(props.modelConfiguration, props.initialId);

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
	padding-bottom: var(--gap-small);
}
.expression {
	flex-grow: 1;
}
main {
	display: flex;
	justify-content: space-between;
	padding-bottom: var(--gap-small);
}
</style>
