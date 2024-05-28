<template>
	<component
		:is="tables"
		:model="model"
		:readonly="readonly"
		@update-model="$emit('update-model', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Model } from '@/types/Types';
import TeraPetrinetTables from '@/components/model/petrinet/tera-petrinet-tables.vue';
import TeraRegnetTables from '@/components/model/regnet/tera-regnet-tables.vue';
import TeraStockflowTables from '@/components/model/stockflow/tera-stockflow-tables.vue';
// import { cleanModel } from '@/model-representation/service';
import { AMRSchemaNames } from '@/types/common';
import { getModelType } from '@/services/model';

const props = defineProps<{
	model: Model;
	readonly?: boolean;
}>();

defineEmits(['update-model']);

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

// function cleanAndEmit() {
// 	const modelToClean = cloneDeep(transientModel.value);
// 	cleanModel(modelToClean);
// 	emit('update-model', modelToClean);
// }

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
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}
</style>
