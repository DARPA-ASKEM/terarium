<template>
	<aside class="flex gap-2">
		<Button
			v-if="!isEmpty(getErrors())"
			size="small"
			:disabled="isFilterWarning"
			:severity="!isFilterError ? 'secondary' : 'danger'"
			@click.stop="toggleFilterSeverity(ModelErrorSeverity.ERROR)"
		>
			{{ getErrors().length }} Errors
		</Button>
		<Button
			v-if="!isEmpty(getWarnings())"
			size="small"
			:disabled="isFilterError"
			:severity="!isFilterWarning ? 'secondary' : 'warning'"
			@click.stop="toggleFilterSeverity(ModelErrorSeverity.WARNING)"
		>
			{{ getWarnings().length }} Warnings
		</Button>
		<tera-input-text placeholder="Filter" v-model="filter" />
	</aside>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Button from 'primevue/button';
import { ModelError, ModelErrorSeverity } from '@/model-representation/service';
import { isEmpty } from 'lodash';
import { computed } from 'vue';

const props = defineProps<{
	modelErrors: ModelError[];
}>();

const filter = defineModel<string>('filter', { required: true });
const filterSeverity = defineModel<ModelErrorSeverity | null>('filterSeverity', { required: true });

const isFilterWarning = computed(() => filterSeverity.value === ModelErrorSeverity.WARNING);
const isFilterError = computed(() => filterSeverity.value === ModelErrorSeverity.ERROR);

function toggleFilterSeverity(severity: ModelErrorSeverity) {
	filterSeverity.value = filterSeverity.value === severity ? null : severity;
}

function getWarnings() {
	return props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.WARNING);
}

function getErrors() {
	return props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.ERROR);
}
</script>
