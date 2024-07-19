<template>
	<section>
		<h6>
			<template v-if="item.templateId">{{ item.templateId }},</template> {{ item.id }}
		</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="item.name ?? ''"
			@update:model-value="$emit('update-item', { key: 'name', value: $event })"
			:disabled="disabledInputs?.includes('name')"
		/>
		<div v-if="item.input && item.output" label="Unit">
			<span><span>Input:</span> {{ item.input }}</span>
			<span><span>Output:</span> {{ item.output }}</span>
		</div>
		<!--amr_to_mmt doesn't like unit expressions with spaces, removing them here before they are saved to the amr-->
		<tera-input
			v-else
			label="Unit"
			placeholder="Add a unit"
			:model-value="item.unitExpression ?? ''"
			@update:model-value="
				($event) => {
					const value = $event.replace(/[\s.]+/g, '');
					$emit('update-item', { key: 'unitExpression', value });
				}
			"
			:disabled="disabledInputs?.includes('unitExpression')"
			@focusout="($event) => ($event.target.value = $event.target.value.replace(/[\s.]+/g, ''))"
		/>
		<span class="concept">
			<label>Concept</label>
			<!--icon="pi pi-search"-->
			<AutoComplete
				label="Concept"
				size="small"
				placeholder="Select a concept"
				v-model="conceptQuery"
				:suggestions="results"
				optionLabel="name"
				:disabled="disabledInputs?.includes('concept')"
				@complete="searchConcepts"
				@item-select="$emit('update-item', { key: 'concept', value: $event.value.curie })"
			/>
		</span>
		<katex-element class="expression" v-if="item.expression" :expression="item.expression" :throw-on-error="false" />
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="item.description ?? ''"
			@update:model-value="$emit('update-item', { key: 'description', value: $event })"
			:disabled="disabledInputs?.includes('description')"
		/>
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import TeraInput from '@/components/widgets/tera-input.vue';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import type { ModelPartItem } from '@/types/Model';
import type { DKG } from '@/types/Types';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached, searchCuriesEntities } from '@/services/concept';

const props = defineProps<{
	item: ModelPartItem;
	disabledInputs?: string[];
}>();

defineEmits(['update-item']);

const conceptQuery = ref('');
const results = ref<DKG[]>([]);
const nameOfCurieCache = ref(new Map<string, string>());

async function searchConcepts(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) results.value = await searchCuriesEntities(query);
}

onMounted(() => {
	const identifiers = props.item.grounding?.identifiers;
	if (identifiers) {
		console.log(identifiers, props.item.grounding);
		conceptQuery.value = getNameOfCurieCached(nameOfCurieCache.value, getCurieFromGroundingIdentifier(identifiers));
		console.log(conceptQuery.value);
	}
});
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
	font-size: var(--font-caption);
	& > span > span {
		color: var(--text-color-subdued);
	}
}

:deep([title='Name']) {
	grid-area: name;
}

:deep([title='Description']) {
	grid-area: description;
}

:deep([label='Unit']) {
	grid-area: unit;
}

.concept {
	grid-area: concept;
	display: flex;
	align-items: center;
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	gap: var(--gap-1);
}

:deep(.p-autocomplete-input) {
	padding: var(--gap-1) var(--gap-2);
}

.expression {
	grid-area: expression;
}
</style>
