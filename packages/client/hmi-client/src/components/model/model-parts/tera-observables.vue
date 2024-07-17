<template>
	<tera-model-part
		:model-part-items="observablesList"
		:disabled-inputs="['concept']"
		@update-observable="emit('update-observable', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelPartItem } from '@/types/Model';
import { Model, Observable } from '@/types/Types';
import { MiraModel } from '@/model-representation/mira/mira-common';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	observables: Observable[];
}>();

const emit = defineEmits(['update-observable']);

const observablesList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[]
>(() =>
	props.observables.map((observable) => {
		const { id, name, expression, description, units } = observable;
		return {
			base: {
				id,
				name,
				description,
				unitExpression: units?.expression,
				expression,
				expression_mathml: observable.expression_mathml
			},
			// Observables can't be stratified therefore don't have children
			children: [],
			isParent: false
		};
	})
);
</script>
