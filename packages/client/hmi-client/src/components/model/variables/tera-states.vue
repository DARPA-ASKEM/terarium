<template>
	<tera-variables
		:variable-list="stateList"
		:collapsed-variables="collapsedInitials"
		:disabled-inputs="['concept', 'description']"
		@update-variable="emit('update-state', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import { Model } from '@/types/Types';
import { getStates } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraVariables from '@/components/model/variables/tera-variables.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

const emit = defineEmits(['update-state']);

const states = computed(() => getStates(props.model)); // could be states, vertices, and stocks type
const collapsedInitials = computed(() => collapseInitials(props.mmt));
const stateList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedInitials.value.keys())
		.flat()
		.map((id) => {
			const childTargets = collapsedInitials.value.get(id) ?? [];
			const isParent = childTargets.length > 1;
			const children = childTargets
				.map((childTarget) => {
					const s = states.value.find((state) => state.id === childTarget);
					if (!s) return null;
					return {
						id: s.id,
						name: s.name,
						description: '', // s.description doesn't exist yet
						grounding: s.grounding,
						unitExpression: s.initial?.expression
					};
				})
				.filter(Boolean) as ModelVariable[];

			const baseState = states.value.find((s) => s.id === id);
			const base: ModelVariable =
				isParent || !baseState
					? { id }
					: {
							id,
							name: baseState.name,
							description: '', // baseState.description doesn't exist yet
							grounding: baseState.grounding,
							unitExpression: baseState.initial?.expression
						};

			return { base, children, isParent };
		})
);
</script>
