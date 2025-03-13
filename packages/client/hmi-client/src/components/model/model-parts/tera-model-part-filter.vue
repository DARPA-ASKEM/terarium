<template>
	<aside class="flex gap-2">
		<Button
			v-if="!isEmpty(getErrors())"
			size="small"
			:disabled="filterType === 'warn'"
			:severity="!filterType ? 'danger' : 'secondary'"
			@click.stop="toggleFilterType('error')"
		>
			{{ !filterType ? 'Filter' : 'Unfilter' }} {{ getErrors().length }} errors
		</Button>
		<Button
			v-if="!isEmpty(getWarnings())"
			size="small"
			:disabled="filterType === 'error'"
			:severity="!filterType ? 'warning' : 'secondary'"
			@click.stop="toggleFilterType('warn')"
		>
			{{ !filterType ? 'Filter' : 'Unfilter' }} {{ getWarnings().length }} warnings
		</Button>
		<tera-input-text placeholder="Filter" v-model="filter" />
	</aside>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Button from 'primevue/button';
import { ModelError } from '@/model-representation/service';
import { isEmpty } from 'lodash';

const props = defineProps<{
	modelErrors: ModelError[];
}>();

const filter = defineModel<string>('filter', { required: true });
const filterType = defineModel<'warn' | 'error' | null>('filterType', { required: true });

function toggleFilterType(type: 'warn' | 'error') {
	filterType.value = filterType.value === type ? null : type;
}

function getWarnings() {
	return props.modelErrors.filter(({ severity }) => severity === 'warn');
}

function getErrors() {
	return props.modelErrors.filter(({ severity }) => severity === 'error');
}
</script>

<style scoped></style>
