<template>
	<component
		:is="tables"
		:model="transientModel"
		:readonly="readonly"
		@update-model="$emit('update-model', $event)"
		@update-state-metadata="onUpdateState"
		@update-parameter="onUpdateParameter"
	/>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { computed, ref, onMounted, onUnmounted } from 'vue';
import type { Model } from '@/types/Types';
import TeraPetrinetTables from '@/components/model/petrinet/tera-petrinet-tables.vue';
import TeraRegnetTables from '@/components/model/regnet/tera-regnet-tables.vue';
import TeraStockflowTables from '@/components/model/stockflow/tera-stockflow-tables.vue';
import { AMRSchemaNames } from '@/types/common';
import { getModelType } from '@/services/model';
import {
	updateState,
	updateInitialMetadata,
	updateParameter,
	updateParameterMetadata
} from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model']);

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

function onUpdateState(event: any) {
	const { id, key, value, isMetadata } = event;
	if (isMetadata) {
		updateInitialMetadata(transientModel.value, id, key, value); // For now we only have metadata for initials so just save it there
	} else {
		updateState(transientModel.value, id, key, value);
	}
}

function onUpdateParameter(event: any) {
	const { parameterId, key, value, isMetadata } = event;
	if (isMetadata) {
		updateParameterMetadata(transientModel.value, parameterId, key, value);
	} else {
		updateParameter(transientModel.value, parameterId, key, value);
	}
}

// Apply changes to the model when the component unmounts or the user navigates away
function saveChanges() {
	emit('update-model', transientModel.value);
}

onMounted(() => window.addEventListener('beforeunload', saveChanges));

onUnmounted(() => {
	saveChanges();
	window.removeEventListener('beforeunload', saveChanges);
});
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}
</style>
