<template>
	<section class="flex flex-column gap-3">
		<span class="flex align-items-center gap-3">
			<h6>{{ symbol }}</h6>
			<span class="name">
				<template v-if="featureConfig.isPreview">{{ item.name }}</template>
				<tera-input-text
					v-else
					placeholder="Add a name"
					:model-value="item.name ?? ''"
					@update:model-value="$emit('update-item', { key: 'name', value: $event })"
				/>
			</span>
			<span class="unit">
				<template v-if="item.input && item.output">
					<span><label>Input:</label> {{ item.input }}</span>
					<span class="ml-"><label>Output:</label> {{ item.output }}</span>
				</template>
				<!--amr_to_mmt doesn't like unit expressions with spaces, removing them here before they are saved to the amr-->
				<template v-else-if="showUnit">
					<template v-if="featureConfig.isPreview"><label>Unit</label>{{ item.unitExpression }}</template>
					<!-- we use a dropdown for units with time semantic-->
					<Dropdown
						v-else-if="isTimePart"
						:model-value="item.unitExpression"
						option-label="label"
						option-value="value"
						:options="[
							{ label: 'Days', value: 'days' },
							{ label: 'Months', value: 'months' },
							{ label: 'Years', value: 'years' }
						]"
						@change="$emit('update-item', { key: 'unitExpression', value: $event.value })"
					/>
					<tera-input-text
						v-else
						label="Unit"
						placeholder="Add a unit"
						:characters-to-reject="[' ']"
						:model-value="item.unitExpression ?? ''"
						@update:model-value="$emit('update-item', { key: 'unitExpression', value: $event })"
					/>
				</template>
			</span>

			<span v-if="!featureConfig.isPreview" class="flex ml-auto gap-3">
				<!-- Three states of description buttons: Hide / Show / Add description -->
				<Button
					v-if="(item.description && showDescription) || (!item.description && showDescription)"
					text
					size="small"
					label="Hide description"
					@click="showDescription = false"
				/>
				<Button
					v-else-if="!showDescription"
					text
					size="small"
					:label="item.description ? 'Show description' : 'Add description'"
					@click="showDescription = true"
				/>
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
						@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"
						@keyup.enter="applyValidConcept"
						@blur="applyValidConcept"
					/>
				</span>
			</span>
		</span>
		<katex-element
			v-if="item.expression"
			class="expression"
			:expression="stringToLatexExpression(item.expression)"
			:throw-on-error="false"
		/>
		<span class="description">
			<template v-if="featureConfig.isPreview">{{ item.description }}</template>
			<tera-input-text
				v-if="showDescription"
				placeholder="Add a description"
				:model-value="item.description ?? ''"
				@update:model-value="$emit('update-item', { key: 'description', value: $event })"
			/>
		</span>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import AutoComplete from 'primevue/autocomplete';
import Button from 'primevue/button';
import type { ModelPartItem } from '@/types/Model';
import { stringToLatexExpression } from '@/services/model';
import type { DKG } from '@/types/Types';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';
import type { FeatureConfig } from '@/types/common';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	item: ModelPartItem;
	featureConfig: FeatureConfig;
	isTimePart?: boolean;
}>();

const emit = defineEmits(['update-item']);

const query = ref('');
const results = ref<DKG[]>([]);

const symbol = computed(() => (props.item.templateId ? `${props.item.templateId}, ${props.item.id}` : props.item.id));

// If we are in preview mode and there is no content, show nothing
const showUnit = computed(() => !(props.featureConfig.isPreview && !props.item.unitExpression));
const showConcept = computed(() => !(props.featureConfig.isPreview && !query.value));

// Used if an option isn't selected from the Autocomplete suggestions but is typed in regularly
function applyValidConcept() {
	// Allows to empty the concept
	if (query.value === '') {
		emit('update-item', { key: 'concept', value: '' });
	}
	// If what was typed was one of the results then choose that result
	else {
		const concept = results.value.find((result) => result.name === query.value);
		if (concept) {
			emit('update-item', { key: 'concept', value: concept.curie });
		}
	}
}

watch(
	() => props.item.grounding?.identifiers,
	async (identifiers) => {
		if (identifiers) query.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
	},
	{ immediate: true }
);

const showDescription = ref(false);
if (props.item.description) showDescription.value = true;
</script>

<style scoped>
section {
	font-size: var(--font-caption);
}

label,
.description {
	color: var(--text-color-subdued);
}

h6::after {
	content: '|';
	color: var(--text-color-light);
	margin-left: var(--gap-2);
}

.unit {
	max-width: 20rem;
	overflow: auto;
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
