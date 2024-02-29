<template>
	<!-- FIXME: Looks like petrinet and stockflow somewhat share some table values, just using the petrinet tables to show stockflow for now... -->
	<tera-petrinet-tables
		v-if="modelType === AMRSchemaNames.PETRINET || modelType === AMRSchemaNames.STOCKFLOW"
		:model="model"
		:modelConfigurations="modelConfigurations"
		:readonly="readonly"
		@update-model="(updatedModel: Model) => emit('update-model', updatedModel)"
	/>
	<tera-regnet-tables
		v-else-if="modelType === AMRSchemaNames.REGNET"
		:model="model"
		:modelConfigurations="modelConfigurations"
		:readonly="readonly"
		@update-model="(updatedModel: Model) => emit('update-model', updatedModel)"
	/>
</template>

<script setup lang="ts">
import type { Model, ModelConfiguration } from '@/types/Types';
import { computed } from 'vue';
import TeraPetrinetTables from '@/components/model/petrinet/tera-petrinet-tables.vue';
import TeraRegnetTables from '@/components/model/regnet/tera-regnet-tables.vue';

import { AMRSchemaNames } from '@/types/common';
import { getModelType } from '@/services/model';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model']);

const modelType = computed(() => getModelType(props.model));
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}
</style>
