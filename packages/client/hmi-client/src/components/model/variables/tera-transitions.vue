<template>
	<tera-variables
		:variable-list="transitionsList"
		:disabled-inputs="['concept', 'description', 'name', 'unitExpression']"
		show-matrix
		@open-matrix="(id: string) => (matrixModalId = id)"
		@update-variable="$emit('update-transition', $event)"
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
import type { ModelVariable } from '@/types/Model';
// import type { Transition } from '@/types/Types';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseTemplates } from '@/model-representation/mira/mira';
import TeraVariables from '@/components/model/variables/tera-variables.vue';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	transitions: any[]; // Support for Transition type needs work, missing expression, description and name
}>();

defineEmits(['update-transition']);

const collapsedTemplates = computed(() => collapseTemplates(props.mmt));

const transitionsList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedTemplates.value.matrixMap.keys())
		.flat()
		.map((templateId) => {
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
						name: t.id,
						description: '', // t.description doesn't exist yet
						grounding: t.grounding,
						expression: t.expression,
						input: t.input,
						output: t.output
					};
				})
				.filter(Boolean) as ModelVariable[];

			console.log(referencedTransitions);
			console.log(props.transitions);
			const baseTransition = props.transitions.find((t) => t.id === referencedTransitions[0].name);
			const base: ModelVariable =
				isParent || !baseTransition
					? { id: templateId }
					: {
							id: baseTransition.id,
							name: baseTransition.id, // baseTransition.name appears in the data but Transition type doesn't support it yet
							description: '', // baseTransition.description doesn't exist yet
							grounding: baseTransition.grounding,
							expression: baseTransition.expression,
							templateId,
							input: baseTransition.input,
							output: baseTransition.output
						};

			return { base, children, isParent };
		})
);

const matrixModalId = ref('');
</script>
