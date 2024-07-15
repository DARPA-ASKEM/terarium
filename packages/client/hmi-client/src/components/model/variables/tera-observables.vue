<template>
	<tera-variables
		:variable-list="observablesList"
		:disabled-inputs="['concept', 'description', 'unitExpression']"
		@update-observable="emit('update-observable', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import { Model, Observable } from '@/types/Types';
import { MiraModel } from '@/model-representation/mira/mira-common';
import TeraVariables from '@/components/model/variables/tera-variables.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	observables: Observable[];
}>();

const emit = defineEmits(['update-observable']);

const observablesList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	props.observables.map((observable) => {
		const { id, name, expression } = observable;
		return {
			// Observables are missing units and description
			base: { id, name, expression, expression_mathml: observable.expression_mathml },
			// Observables can't be stratified therefore don't have children
			children: [],
			isParent: false
		};
	})
);
</script>
