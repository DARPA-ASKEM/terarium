<template>
	<section>
		<h6>
			<template v-if="variable.templateId">{{ variable.templateId }},</template> {{ variable.id }}
		</h6>
		<tera-input
			title="Name"
			placeholder="Add a name"
			:model-value="variable.name ?? ''"
			@update:model-value="$emit('update-variable', { key: 'name', value: $event })"
			:disabled="disabledInputs?.includes('name')"
		/>
		<div v-if="variable.input && variable.output" label="Unit">
			<span><span>Input:</span> {{ variable.input }}</span>
			<span><span>Output:</span> {{ variable.output }}</span>
		</div>
		<!--amr_to_mmt doesn't like unit expressions with spaces, removing them here before they are saved to the amr-->
		<tera-input
			v-else
			label="Unit"
			placeholder="Add a unit"
			:model-value="variable.unitExpression ?? ''"
			@update:model-value="
				($event) => {
					const value = $event; //.replace(/[\s.]+/g, '');
					$emit('update-variable', { key: 'unitExpression', value });
				}
			"
			:disabled="disabledInputs?.includes('unitExpression')"
		/>
		<!--TODO: Add support for editing concepts-->
		<tera-input
			label="Concept"
			placeholder="Select a concept"
			icon="pi pi-search"
			:disabled="disabledInputs?.includes('concept')"
			:model-value="''"
		/>
		<katex-element
			class="expression"
			v-if="variable.expression"
			:expression="variable.expression"
			:throw-on-error="false"
		/>
		<!--FIXME: description property should be added to the state type-->
		<tera-input
			title="Description"
			placeholder="Add a description"
			:model-value="variable.description ?? ''"
			@update:model-value="$emit('update-variable', { key: 'description', value: $event })"
			:disabled="disabledInputs?.includes('description')"
		/>
	</section>
</template>

<script setup lang="ts">
import TeraInput from '@/components/widgets/tera-input.vue';
import type { ModelVariable } from '@/types/Model';

// import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';
// getNameOfCurieCached(
// 		new Map<string, string>(),
// 		getCurieFromGroundingIdentifier(grounding.identifiers)
// 	)

defineProps<{
	variable: ModelVariable;
	disabledInputs?: string[];
}>();

defineEmits(['update-variable']);
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

:deep([label='Concept']) {
	grid-area: concept;
}

.expression {
	grid-area: expression;
}
</style>
