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
				<template v-if="featureConfig.isPreview"><label>Data type</label>{{ column.dataType }}</template>
				<tera-input-text
					v-else
					label="Data type"
					placeholder="Add a data type"
					:characters-to-reject="[' ']"
					:model-value="column.dataType ?? ''"
					@update:model-value="$emit('update-column', { key: 'dataType', value: $event })"
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
					@item-select="$emit('update-column', { key: 'concept', value: $event.value.curie })"
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
import type { DKG } from '@/types/Types'; // DatasetColumn
import { getCurieFromGroundingIdentifier, getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	column: any;
	featureConfig: FeatureConfig;
}>();

defineEmits(['update-column']);

const query = ref('');
const results = ref<DKG[]>([]);

// If we are in preview mode and there is no content, show nothing
const showConcept = computed(() => !(props.featureConfig.isPreview && !query.value));

watch(
	() => props.column.grounding?.identifiers,
	async (identifiers) => {
		if (identifiers) query.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
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
.concept {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
}

:deep(.p-autocomplete-input) {
	padding: var(--gap-1) var(--gap-2);
}
</style>
