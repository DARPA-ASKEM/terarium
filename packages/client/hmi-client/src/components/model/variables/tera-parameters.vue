<template>
	<tera-variables
		:variable-list="parameterList"
		:collapsed-variables="collapsedParameters"
		:disabled-inputs="['concept']"
		show-matrix
		@open-matrix="(id: string) => (matrixModalId = id)"
		@update-variable="emit('update-parameter', $event)"
	/>
	<teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalId"
			:id="matrixModalId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			is-read-only
			@close-modal="matrixModalId = ''"
		/>
	</teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { StratifiedMatrix, ModelVariable } from '@/types/Model';
import { Model } from '@/types/Types';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters } from '@/model-representation/mira/mira';
import TeraVariables from '@/components/model/variables/tera-variables.vue';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = computed(() => getParameters(props.model));
const collapsedParameters = computed(() => collapseParameters(props.mmt, props.mmtParams));
const parameterList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedParameters.value.keys())
		.flat()
		.map((id) => {
			const childIds = collapsedParameters.value.get(id) ?? [];
			const isParent = childIds.length > 1;
			const children = childIds
				.map((childId) => {
					const p = parameters.value.find((param) => param.id === childId);
					if (!p) return null;
					return {
						id: p.id,
						name: p.name,
						description: p.description,
						grounding: p.grounding,
						unitExpression: p.units?.expression
					};
				})
				.filter(Boolean) as ModelVariable[];

			const baseParameter = parameters.value.find((p) => p.id === id);
			const base: ModelVariable =
				isParent || !baseParameter
					? { id }
					: {
							id,
							name: baseParameter.name,
							description: baseParameter.description,
							grounding: baseParameter.grounding,
							unitExpression: baseParameter.units?.expression
						};

			return { base, children, isParent };
		})
);

const matrixModalId = ref('');
</script>
