<template>
	<component
		:is="tables"
		:model="transientModel"
		:readonly="readonly"
		@update-model="$emit('update-model', $event)"
		@update-initial-metadata="
			($event: any) => {
				if ($event) {
					const { target, metadataKey, value } = $event;
					updateInitialMetadata(transientModel, target, metadataKey, value);
				}
			}
		"
		@update-parameter="
			($event: any) => {
				if ($event) {
					const { parameterId, key, value } = $event;
					updateParameter(transientModel, parameterId, key, value);
				}
			}
		"
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
import { updateInitialMetadata, updateParameter } from '@/model-representation/service';

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
