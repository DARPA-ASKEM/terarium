<template>
	<tera-model-part
		:items="parameterList"
		:collapsed-items="collapsedParameters"
		:feature-config="featureConfig"
		show-matrix
		:filter="filter"
		@open-matrix="(id: string) => (matrixModalId = id)"
		@update-item="emit('update-parameter', $event)"
	/>
	<tera-stratified-matrix-modal
		v-if="matrixModalId"
		:id="matrixModalId"
		:mmt="mmt"
		:mmt-params="mmtParams"
		:stratified-matrix-type="StratifiedMatrix.Parameters"
		is-read-only
		@close-modal="matrixModalId = ''"
	/>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { StratifiedMatrix } from '@/types/Model';
import type { ModelPartItem } from '@/types/Model';
import { Model } from '@/types/Types';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	featureConfig: FeatureConfig;
	filter?: string;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = computed(() => getParameters(props.model));
const collapsedParameters = computed(() => collapseParameters(props.mmt, props.mmtParams));
const parameterList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[]
>(() =>
	Array.from(collapsedParameters.value.keys()).map((id) => {
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
			.filter(Boolean) as ModelPartItem[];

		const baseParameter = parameters.value.find((p) => p.id === id);
		const base: ModelPartItem =
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
