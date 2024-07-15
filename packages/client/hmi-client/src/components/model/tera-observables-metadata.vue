<template>
	<div></div>
	<!-- <tera-variables-metadata
		:model="model"
		:variable-list="observablesList"
		:collapsed-variables="collapsedObservables"
		:disabled-inputs="['concept', 'description']"
		@update-variable="emit('update-parameter', $event)"
	/> -->
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import { Model, Observable } from '@/types/Types';
import { ObservableSummary } from '@/model-representation/mira/mira-common';
import { collapseObservableReferences } from '@/model-representation/mira/mira';
// import TeraVariablesMetadata from './tera-variables-metadata.vue';

const props = defineProps<{
	model: Model;
	observables: Observable[];
	observableSummary: ObservableSummary;
}>();

// const emit = defineEmits(['update-parameter']);

const collapsedObservables = computed(() => collapseObservableReferences(props.observableSummary));
const observablesList: ModelVariable[] = [];

console.log(collapsedObservables.value, observablesList);
// = computed<
// 	{
// 		base: ModelVariable;
// 		children: ModelVariable[];
// 		isParent: boolean;
// 	}[]
// >(() =>
// 	Array.from(collapsedObservables.value.keys())
// 		.flat()
// 		.map((id) => {
// 			const childIds = collapsedObservables.value.get(id) ?? [];
// 			const isParent = childIds.length > 1;
// 			const children = childIds
// 				.map((childId) => {
// 					const p = parameters.value.find((param) => param.id === childId);
// 					// console.log(p, p?.units?.expression ?? '');
// 					return {
// 						id: p?.id ?? '',
// 						name: p?.name ?? '',
// 						description: p?.description ?? '',
// 						grounding: p?.grounding,
// 						unitExpression: p?.units?.expression ?? ''
// 					};
// 				})
// 				.filter(Boolean) as ModelVariable[];

// 			const baseParameter = isParent ? { id } : parameters.value.find((p) => p.id === id);
// 			const base = isParent
// 				? { id: baseParameter?.id ?? '' }
// 				: {
// 						id: baseParameter?.id ?? '',
// 						name: baseParameter?.name ?? '',
// 						description: baseParameter?.description ?? '',
// 						grounding: baseParameter?.grounding,
// 						unitExpression: baseParameter?.units?.expression ?? ''
// 					};

// 			return { base, children, isParent };
// 		})
// );
</script>
