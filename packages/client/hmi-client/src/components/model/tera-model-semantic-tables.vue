<template>
	<component
		:is="tables"
		:model="transientModel"
		:mmt="mmt"
		:mmt-params="mmtParams"
		:readonly="readonly"
		@update-model="$emit('update-model', $event)"
		@update-state="(e: any) => onUpdate('state', e)"
		@update-parameter="(e: any) => onUpdate('parameter', e)"
		@update-observable="(e: any) => onUpdate('observable', e)"
		@update-transition="(e: any) => onUpdate('transition', e)"
		@update-time="(e: any) => onUpdate('time', e)"
	/>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import type { Model } from '@/types/Types';
import TeraPetrinetTables from '@/components/model/petrinet/tera-petrinet-tables.vue';
import TeraRegnetTables from '@/components/model/regnet/tera-regnet-tables.vue';
import TeraStockflowTables from '@/components/model/stockflow/tera-stockflow-tables.vue';
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

const props = defineProps<{
	model: Model;
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model']);

const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const transientModel = ref(cloneDeep(props.model));

const modelType = computed(() => getModelType(props.model));

const tables = computed(() => {
	switch (modelType.value) {
		case AMRSchemaNames.PETRINET:
			return TeraPetrinetTables;
		case AMRSchemaNames.REGNET:
			return TeraRegnetTables;
		case AMRSchemaNames.STOCKFLOW:
			return TeraStockflowTables;
		default:
			return TeraPetrinetTables;
	}
});

// states don't have units yet and don't worry about concept yet
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
			updateTime(transientModel.value, id, key, value);
			break;
		default:
			break;
	}
}

function updateMMT() {
	getMMT(props.model).then((response) => {
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	});
}

// Apply changes to the model when the component unmounts or the user navigates away
function saveChanges() {
	emit('update-model', transientModel.value);
}

onMounted(() => {
	window.addEventListener('beforeunload', saveChanges);
	updateMMT();
});

onUnmounted(() => {
	saveChanges();
	window.removeEventListener('beforeunload', saveChanges);
});

// Meant to run when we want to view a different model, like when we swtich outputs in the stratify operator
// This is not to be used a refresh when saving a model
watch(
	() => props.model,
	() => {
		transientModel.value = cloneDeep(props.model);
		updateMMT();
	},
	{ deep: true }
);
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}

:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}
</style>
