<template>
	<div class="flex gap-3">
		<section class="entries">
			<h6>{{ column.symbol }}</h6>
			<span class="name">
				<template v-if="featureConfig.isPreview">{{ column.name }}</template>
				<tera-input-text
					v-else
					placeholder="Add a name"
					:model-value="column.name ?? ''"
					@update:model-value="$emit('update-column', { key: 'name', value: $event })"
				/>
			</span>
			<span class="unit">
				<template v-if="featureConfig.isPreview"><label>Unit</label>{{ column.unit }}</template>
				<tera-input-text
					v-else
					label="Unit"
					placeholder="Add a unit"
					:characters-to-reject="[' ']"
					:model-value="column.unit ?? ''"
					@update:model-value="$emit('update-column', { key: 'unit', value: $event })"
				/>
			</span>
			<span class="data-type">
				<label>Data type</label>
				<template v-if="featureConfig.isPreview">{{ column.dataType }}</template>
				<Dropdown
					v-else
					placeholder="Select a data type"
					:model-value="column.dataType ?? ''"
					@update:model-value="$emit('update-column', { key: 'dataType', value: $event })"
					:options="Object.values(ColumnType)"
				/>
			</span>
			<span v-if="showConcept" class="concept">
				<label>Concept</label>
				<template v-if="featureConfig.isPreview">{{ query }}</template>
				<AutoComplete
					v-else
					size="small"
					placeholder="Search concepts"
					v-model="query"
					:suggestions="results"
					optionLabel="name"
					@complete="async () => (results = await searchCuriesEntities(query))"
					@item-select="
						$emit('update-column', { key: 'concept', value: { curie: $event.value.curie, name: $event.value.name } })
					"
					@keyup.enter="applyValidConcept"
					@blur="applyValidConcept"
				/>
			</span>
			<span class="description">
				<template v-if="featureConfig.isPreview">{{ column.description }}</template>
				<tera-input-text
					v-else
					placeholder="Add a description"
					:model-value="column.description ?? ''"
					@update:model-value="$emit('update-column', { key: 'description', value: $event })"
				/>
			</span>
		</section>
		<tera-boxplot class="flex-1" v-if="column.stats" :stats="column.stats" />
	</div>
</template>

<script setup lang="ts">
/* Copied the structure of tera-model-parts.vue */
import { ref, computed, watch } from 'vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraBoxplot from '@/components/widgets/tera-boxplot.vue';
import AutoComplete from 'primevue/autocomplete';
import Dropdown from 'primevue/dropdown';
import { type DKG, ColumnType, type Grounding } from '@/types/Types';
import { searchCuriesEntities } from '@/services/concept';
import type { FeatureConfig } from '@/types/common';

type ColumnInfo = {
	symbol: string;
	dataType?: ColumnType;
	description?: string;
	grounding?: Grounding;
	// Metadata
	unit?: string;
	name?: string;
	stats?: any;
};

const props = defineProps<{
	column: ColumnInfo;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-column']);

const query = ref('');
const results = ref<DKG[]>([]);

// If we are in preview mode and there is no content, show nothing
const showConcept = computed(() => !(props.featureConfig.isPreview && !query.value));

// Used if an option isn't selected from the Autocomplete suggestions but is typed in regularly
function applyValidConcept() {
	// Allows to empty the concept
	if (query.value === '') {
		emit('update-column', { key: 'concept', value: { curie: '', name: '' } });
	}
	// If what was typed was one of the results then choose that result
	else {
		const concept = results.value.find((result) => result.name === query.value);
		if (concept) {
			emit('update-column', { key: 'concept', value: { curie: concept.curie, name: concept.name } });
		}
	}
}

watch(
	() => props.column.grounding?.identifiers,
	(identifiers) => {
		// console.log(identifiers); // FIXME: Multiple identifiers are held in here after enrichment! Designs have to be updated to handle more.
		query.value = identifiers?.[0].name ?? ''; // Just show first one for now.
	},
	{ immediate: true }
);
</script>

<style scoped>
section.entries {
	display: grid;
	grid-template-areas:
		'symbol name unit data-type . concept'
		'expression expression expression expression expression expression'
		'description description description description description description';
	grid-template-columns: max-content max-content max-content auto max-content;
	grid-auto-flow: dense;
	overflow: hidden;
	gap: var(--gap-2);
	align-items: center;
	font-size: var(--font-caption);
	overflow: auto;
	width: 85%;

	& > *:empty {
		display: none;
	}
}

label {
	color: var(--text-color-subdued);
	text-wrap: nowrap;
}

h6 {
	grid-area: symbol;
	justify-self: center;
	&::after {
		content: '|';
		color: var(--text-color-light);
		margin-left: var(--gap-2);
	}
}

.name {
	grid-area: name;
}

.description {
	grid-area: description;
	color: var(--text-color-subdued);
}

.unit {
	grid-area: unit;
}

.data-type {
	grid-area: data-type;
}

.unit,
.data-type {
	max-width: 15rem;
}

.expression {
	grid-area: expression;
	font-size: var(--font-body-small);
}

.concept {
	grid-area: concept;
	margin-left: auto;
}

.unit,
.data-type,
.concept {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
}

:deep(.p-dropdown > span),
:deep(.p-autocomplete-input) {
	padding: var(--gap-1) var(--gap-2);
}
</style>
