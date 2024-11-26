<template>
	<tera-model-part
		:items="stateList"
		:collapsed-items="collapsedInitials"
		:feature-config="featureConfig"
		:filter="filter"
		@update-item="emit('update-state', $event)"
	/>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import { ModelPartItem } from '@/types/Model';
import { Model } from '@/types/Types';
import { getStates } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	featureConfig: FeatureConfig;
	filter?: string;
}>();

const emit = defineEmits(['update-state']);

const collapsedInitials = collapseInitials(props.mmt);
let stateList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = createStateList();

watch(
	() => props.model?.model?.states,
	() => {
		stateList = createStateList();
	}
);

function createStateList() {
	return Array.from(collapsedInitials.keys()).map((id) => {
		const childTargets = collapsedInitials.get(id) ?? [];
		const isParent = childTargets.length > 1;
		const states = getStates(props.model); // could be states, vertices, and stocks type
		const children = childTargets
			.map((childTarget) => {
				const s = states.find((state) => state.id === childTarget);
				if (!s) return null;
				return {
					id: s.id,
					name: s.name,
					description: s.description,
					grounding: s.grounding,
					unitExpression: s.units?.expression
				};
			})
			.filter(Boolean) as ModelPartItem[];

		const baseState: any = states.find((s) => s.id === id);
		const base: ModelPartItem =
			isParent || !baseState
				? { id }
				: {
						id,
						name: baseState.name,
						description: baseState.description,
						grounding: baseState.grounding,
						unitExpression: baseState.units?.expression
					};

		return { base, children, isParent };
	});
}
</script>
