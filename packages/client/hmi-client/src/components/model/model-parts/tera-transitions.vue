<template>
	<tera-model-part
		:items="transitionsList"
		:collapsed-items="collapsedTemplates"
		:feature-config="featureConfig"
		:filter="filter"
		show-matrix
		@open-matrix="(id: string) => (matrixModalId = id)"
		@update-item="$emit('update-transition', $event)"
	/>
	<tera-stratified-matrix-modal
		v-if="matrixModalId"
		:id="matrixModalId"
		:mmt="mmt"
		:mmt-params="mmtParams"
		:stratified-matrix-type="StratifiedMatrix.Rates"
		is-read-only
		@close-modal="matrixModalId = ''"
	/>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { StratifiedMatrix } from '@/types/Model';
import type { ModelPartItem } from '@/types/Model';
import type { Transition } from '@/types/Types';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseTemplates } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	transitions: Transition[];
	featureConfig: FeatureConfig;
	filter?: string;
}>();

defineEmits(['update-transition']);

// Convert templateId: string => templates: MiraTemplate[] map to templateId: string => childIds: string[] map
const collapsedTemplates = computed(() => {
	const templateMap = new Map<string, string[]>();
	Array.from(collapseTemplates(props.mmt).matrixMap.keys()).forEach((templateId) => {
		templateMap.set(
			templateId,
			Array.from(collapseTemplates(props.mmt).matrixMap.get(templateId) ?? []).map(({ name }) => name)
		);
	});
	return templateMap;
});

const transitionsList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedTemplates.value.keys()).map((templateId) => {
		const childIds = collapsedTemplates.value.get(templateId) ?? [];
		const isParent = childIds.length > 1;
		const children = childIds
			.map((childId) => {
				const t = props.transitions.find((transition) => transition.id === childId);
				if (!t) return null;
				return {
					id: t.id,
					name: t.name,
					description: t.description,
					grounding: t.grounding,
					expression: t.expression,
					input: t.input.join(', '),
					output: t.output.join(', ')
				};
			})
			.filter(Boolean) as ModelPartItem[];

		// There is only one "child" if there is no parent in the UI it's displayed like: template-X, transitionId
		const baseTransition = props.transitions.find((t) => t.id === childIds[0]);
		const base: ModelPartItem =
			isParent || !baseTransition
				? { id: templateId }
				: {
						id: baseTransition.id,
						name: baseTransition.name,
						description: baseTransition.description,
						grounding: baseTransition.grounding,
						expression: baseTransition.expression,
						templateId,
						input: baseTransition.input.join(', '),
						output: baseTransition.output.join(', ')
					};

		return { base, children, isParent };
	})
);

const matrixModalId = ref('');
</script>
