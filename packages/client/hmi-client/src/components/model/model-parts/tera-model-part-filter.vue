<template>
	<aside class="flex gap-2">
		<Button
			v-if="!isEmpty(getErrors())"
			size="small"
			:disabled="filterType === ModelErrorSeverity.WARNING"
			:severity="!filterType ? 'danger' : 'secondary'"
			@click.stop="toggleFilterType(ModelErrorSeverity.ERROR)"
		>
			{{ !filterType ? 'Filter' : 'Unfilter' }} {{ getErrors().length }} errors
		</Button>
		<Button
			v-if="!isEmpty(getWarnings())"
			size="small"
			:disabled="filterType === ModelErrorSeverity.ERROR"
			:severity="!filterType ? 'warning' : 'secondary'"
			@click.stop="toggleFilterType(ModelErrorSeverity.WARNING)"
		>
			{{ !filterType ? 'Filter' : 'Unfilter' }} {{ getWarnings().length }} warnings
		</Button>
		<tera-input-text placeholder="Filter" v-model="filter" />
	</aside>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Button from 'primevue/button';
import { ModelError, ModelErrorSeverity } from '@/model-representation/service';
import { isEmpty } from 'lodash';

const props = defineProps<{
	modelErrors: ModelError[];
}>();

const filter = defineModel<string>('filter', { required: true });
const filterType = defineModel<ModelErrorSeverity | null>('filterType', { required: true });

function toggleFilterType(type: ModelErrorSeverity) {
	filterType.value = filterType.value === type ? null : type;
}

function getWarnings() {
	return props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.WARNING);
}

function getErrors() {
	return props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.ERROR);
}
</script>
