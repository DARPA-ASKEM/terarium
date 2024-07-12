<template>
	<tera-variables-metadata
		:model="model"
		:variable-list="parameterList"
		:collapsed-variables="collapsedParameters"
		@open-matrix="(id) => (matrixModalId = id)"
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
import { Model, ModelParameter } from '@/types/Types';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters } from '@/model-representation/mira/mira';
import TeraVariablesMetadata from './tera-variables-metadata.vue';
import TeraStratifiedMatrixModal from './petrinet/model-configurations/tera-stratified-matrix-modal.vue';

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
			const children = childIds
				.map((childId) => parameters.value.find((p) => p.id === childId))
				.filter(Boolean) as ModelParameter[];
			const isParent = childIds.length > 1;

			// If the parameter is virtual, we need to get the parameter data from model.metadata
			const base = isParent
				? props.model.metadata?.parameters?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
				: parameters.value.find((p) => p.id === id);

			return { base, children, isParent };
		})
);

const matrixModalId = ref('');
</script>
