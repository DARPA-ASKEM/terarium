<template>
	<tera-variables-metadata
		:model="model"
		:variable-list="stateList"
		:collapsed-variables="collapsedInitials"
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
import TeraVariablesMetadata from './tera-variables-metadata.vue';

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
			const children = childTargets
				.map((childTarget) => states.value.find((i: any) => i.id === childTarget))
				.filter(Boolean);
			const isParent = childTargets.length > 1;

			// If the initial is virtual, we need to get it from model.metadata
			const base = isParent
				? props.model.metadata?.initials?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
				: states.value.find((s: any) => s.id === id);

			return { base, children, isParent };
		})
);
</script>
