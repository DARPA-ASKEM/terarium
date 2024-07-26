<template>
	<tera-model-part
		:items="stateList"
		:collapsed-items="collapsedInitials"
		:feature-config="featureConfig"
		@update-item="emit('update-state', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
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
}>();

const emit = defineEmits(['update-state']);

const states = computed(() => getStates(props.model)); // could be states, vertices, and stocks type
const collapsedInitials = computed(() => collapseInitials(props.mmt));
const stateList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedInitials.value.keys()).map((id) => {
		const childTargets = collapsedInitials.value.get(id) ?? [];
		const isParent = childTargets.length > 1;
		const children = childTargets
			.map((childTarget) => {
				const s = states.value.find((state) => state.id === childTarget);
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

		const baseState: any = states.value.find((s) => s.id === id);
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
	})
);
</script>
