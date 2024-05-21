<template>
	<header>
		<b>{{ initialId }}</b>
		<span v-if="name" class="name">{{ '| ' + name }}</span>
		<span v-if="unit" class="unit">({{ unit }})</span>
		<span v-if="description" class="description">{{ description }}</span>
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
import { ModelConfiguration } from '@/types/Types';
import {
	getInitialName,
	getInitialDescription,
	getInitialUnit,
	getInitialExpression,
	getInitialSource
} from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { computed, ref } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	initialId: string;
}>();

const emit = defineEmits(['update-expression', 'update-source']);

const name = computed(() => getInitialName(props.modelConfiguration, props.initialId));
const unit = computed(() => getInitialUnit(props.modelConfiguration, props.initialId));
const description = computed(() =>
	getInitialDescription(props.modelConfiguration, props.initialId)
);

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

.name {
	padding-left: var(--gap-xsmall);
}
.unit {
	padding-left: var(--gap-small);
}
.description {
	padding-left: var(--gap-large);
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
