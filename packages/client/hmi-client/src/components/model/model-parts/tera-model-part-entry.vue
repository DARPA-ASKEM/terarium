<template>
	<section>
		<h6>
			<template v-if="item.templateId">{{ item.templateId }},</template> {{ item.id }}
		</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="item.name ?? ''"
			:feature-config="featureConfig"
			@update:model-value="$emit('update-item', { key: 'name', value: $event })"
			:disabled="disabledInputs?.includes('name')"
		/>
		<div v-if="item.input && item.output" label="Unit">
			<span><label>Input:</label> {{ item.input }}</span>
			<span><label>Output:</label> {{ item.output }}</span>
		</div>
		<!--amr_to_mmt doesn't like unit expressions with spaces, removing them here before they are saved to the amr-->
		<tera-input
			v-else
			label="Unit"
			placeholder="Add a unit"
			:model-value="item.unitExpression ?? ''"
			:feature-config="featureConfig"
			@update:model-value="
				($event) => {
					const value = $event.replace(/[\s.]+/g, '');
					$emit('update-item', { key: 'unitExpression', value });
				}
			"
			:disabled="disabledInputs?.includes('unitExpression')"
			@focusout="($event) => ($event.target.value = $event.target.value.replace(/[\s.]+/g, ''))"
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
				:disabled="disabledInputs?.includes('concept')"
				@complete="async () => (results = await searchCuriesEntities(query))"
				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"
			/>
		</span>
		<katex-element
			class="expression"
			:expression="item.expression && stringToLatexExpression(item.expression)"
			:throw-on-error="false"
		/>
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="item.description ?? ''"
			:feature-config="featureConfig"
			@update:model-value="$emit('update-item', { key: 'description', value: $event })"
			:disabled="disabledInputs?.includes('description')"
		/>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import TeraInput from '@/components/widgets/tera-input.vue';
import AutoComplete from 'primevue/autocomplete';
import type { ModelPartItem } from '@/types/Model';
import { stringToLatexExpression } from '@/services/model';
import type { DKG } from '@/types/Types';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	item: ModelPartItem;
	featureConfig: FeatureConfig;
	disabledInputs?: string[];
}>();

defineEmits(['update-item']);

const query = ref('');
const results = ref<DKG[]>([]);

// If we are in preview mode and there is no content, show nothing
const showConcept = computed(() => !(props.featureConfig.isPreview && !query.value));

watch(
	() => props.item.grounding?.identifiers,
	async (identifiers) => {
		if (identifiers) query.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: grid;
	grid-template-areas:
		'symbol name unit . concept'
		'expression expression expression expression expression'
		'description description description description description';
	grid-template-columns: max-content max-content max-content auto max-content;
	gap: var(--gap-2);
	align-items: center;
	font-size: var(--font-caption);
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

div {
	display: flex;
	gap: var(--gap-2);
}

:deep([title='Name']) {
	grid-area: name;
}

:deep([title='Description']) {
	grid-area: description;
}

:deep([title='Unit']) {
	grid-area: unit;
}

label {
	color: var(--text-color-subdued);
}

.concept {
	grid-area: concept;
	display: flex;
	align-items: center;
	gap: var(--gap-1);
}

:deep(.p-autocomplete-input) {
	padding: var(--gap-1) var(--gap-2);
}

.expression {
	grid-area: expression;
}
</style>
