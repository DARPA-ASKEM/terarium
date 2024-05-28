<template>
	<component :is="tables" :model="transientModel" :readonly="readonly" />
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

// const updateInitial = (inits: Initial[]) => {
// 	const modelInitials = transientModel.value.semantics?.ode.initials ?? [];
// 	for (let i = 0; i < modelInitials.length; i++) {
// 		const foundInitial = inits.find((init) => init.target === modelInitials![i].target);
// 		if (foundInitial) {
// 			modelInitials[i] = foundInitial;
// 		}
// 	}
// };

// const updateParam = (params: ModelParameter[]) => {
// 	const modelParameters = transientModel.value.semantics?.ode?.parameters ?? [];
// 	for (let i = 0; i < modelParameters.length; i++) {
// 		const foundParam = params.find((p) => p.id === modelParameters![i].id);
// 		if (foundParam) {
// 			modelParameters[i] = foundParam;
// 		}
// 	}
// 	// FIXME: Sometimes auxiliaries can share the same ids as parameters so for now both are be updated in that case
// 	const modelAuxiliaries = transientModel.value.model?.auxiliaries ?? [];
// 	for (let i = 0; i < modelAuxiliaries.length; i++) {
// 		const foundParam = params.find((p) => p.id === modelAuxiliaries![i].id);
// 		if (foundParam) {
// 			modelAuxiliaries[i] = foundParam;
// 		}
// 	}
// };

// Apply changes to the model when the component unmounts or the user navigates away
function applyChanges() {
	emit('update-model', transientModel.value);
}

onMounted(() => window.addEventListener('beforeunload', applyChanges));

onUnmounted(() => {
	applyChanges();
	window.removeEventListener('beforeunload', applyChanges);
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
