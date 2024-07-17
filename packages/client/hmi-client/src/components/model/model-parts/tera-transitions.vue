<template>
	<tera-model-part
		:items="transitionsList"
		:disabled-inputs="['concept']"
		show-matrix
		@open-matrix="(id: string) => (matrixModalId = id)"
		@update-item="$emit('update-transition', $event)"
	/>
	<teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalId"
			:id="matrixModalId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Rates"
			is-read-only
			@close-modal="matrixModalId = ''"
		/>
	</teleport>
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

const props = defineProps<{
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	transitions: Transition[];
}>();

defineEmits(['update-transition']);

const collapsedTemplates = computed(() => collapseTemplates(props.mmt));

const transitionsList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedTemplates.value.matrixMap.keys()).map((templateId) => {
		const referencedTransitions = collapsedTemplates.value.matrixMap.get(templateId) ?? [];
		const isParent = referencedTransitions.length > 1;
		const children = referencedTransitions
			.map((referencedTransition) => {
				const t = props.transitions.find(
					(transition) => transition.id === referencedTransition.name
				);
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

		const baseTransition = props.transitions.find((t) => t.id === referencedTransitions[0].name);
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
