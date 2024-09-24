<template>
	<component
		:is="partsComponent"
		:model="transientModel"
		:mmt="mmt"
		:mmt-params="mmtParams"
		:feature-config="featureConfig"
		@update-state="(e: any) => onUpdate('state', e)"
		@update-parameter="(e: any) => onUpdate('parameter', e)"
		@update-observable="(e: any) => onUpdate('observable', e)"
		@update-transition="(e: any) => onUpdate('transition', e)"
		@update-time="(e: any) => onUpdate('time', e)"
	/>
</template>

<script setup lang="ts">
import { cloneDeep, isEqual } from 'lodash';
import { computed, ref, onMounted, watch, PropType } from 'vue';
import type { Model } from '@/types/Types';
import TeraPetrinetParts from '@/components/model/petrinet/tera-petrinet-parts.vue';
import TeraRegnetParts from '@/components/model/regnet/tera-regnet-parts.vue';
import TeraStockflowParts from '@/components/model/stockflow/tera-stockflow-parts.vue';
import { AMRSchemaNames } from '@/types/common';
import { getModelType, getMMT } from '@/services/model';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import {
	updateState,
	updateParameter,
	updateObservable,
	updateTransition,
	updateTime
} from '@/model-representation/service';
import type { FeatureConfig } from '@/types/common';

const props = defineProps({
	model: {
		type: Object as PropType<Model>,
		required: true
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const emit = defineEmits(['update-model']);

const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const transientModel = ref<Model>(cloneDeep(props.model));

const modelType = computed(() => getModelType(props.model));

const partsComponent = computed(() => {
	switch (modelType.value) {
		case AMRSchemaNames.PETRINET:
			return TeraPetrinetParts;
		case AMRSchemaNames.REGNET:
			return TeraRegnetParts;
		case AMRSchemaNames.STOCKFLOW:
			return TeraStockflowParts;
		default:
			return TeraPetrinetParts;
	}
});

function onUpdate(property: string, event: any) {
	const { id, key, value } = event;
	switch (property) {
		case 'state':
			updateState(transientModel.value, id, key, value);
			break;
		case 'parameter':
			updateParameter(transientModel.value, id, key, value);
			break;
		case 'observable':
			updateObservable(transientModel.value, id, key, value);
			break;
		case 'transition':
			updateTransition(transientModel.value, id, key, value);
			break;
		case 'time':
			updateTime(transientModel.value, key, value);
			break;
		default:
			break;
	}
	emit('update-model', transientModel.value);
}

function updateMMT() {
	getMMT(props.model).then((response) => {
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	});
}

onMounted(() => {
	transientModel.value = cloneDeep(props.model);
	updateMMT();
});

// Only update the MMT when the semantics of the model changes outside this component.
watch(
	() => props.model.semantics,
	(newSemantics) => {
		if (!isEqual(newSemantics, transientModel.value.semantics)) {
			transientModel.value = cloneDeep(props.model);
			updateMMT();
		}
	},
	{ deep: true }
);
</script>

<style scoped>
:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}
:deep(.p-accordion-content) {
	margin-bottom: var(--gap-3);
}
:deep(.p-accordion-content:empty::before) {
	content: 'None';
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	margin-left: var(--gap-6);
}
</style>
