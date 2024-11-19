<template>
	<Accordion multiple :active-index="currentActiveIndicies">
		<AccordionTab header="Vertices">
			<DataTable
				v-if="!isEmpty(vertices)"
				data-key="id"
				:value="vertices"
				v-on:cell-edit-complete="onCellEditComplete"
				edit-mode="cell"
			>
				<Column field="id" header="Symbol" />
				<Column field="name" header="Name" />
				<Column field="rate_constant" header="Rate Constant" />
				<Column field="initial" header="Initial Value" />
			</DataTable>
		</AccordionTab>
		<AccordionTab header="Edges">
			<DataTable v-if="!isEmpty(edges)" data-key="id" :value="edges">
				<Column field="id" header="Symbol" />
				<Column field="source" header="Source" />
				<Column field="target" header="Target" />
				<Column field="properties.rate_constant" header="Rate Constant" />
			</DataTable>
		</AccordionTab>
		<AccordionTab header="Parameters">
			<tera-parameters
				v-if="!isEmpty(mmt.parameters)"
				:model="model"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:feature-config="featureConfig"
				@update-parameter="emit('update-parameter', $event)"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model } from '@/types/Types';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { MiraModel, MiraTemplateParams, ObservableSummary } from '@/model-representation/mira/mira-common';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	observableSummary: ObservableSummary;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-parameter']);
const currentActiveIndicies = ref([0, 1, 2]);
const vertices = computed(() => props.model?.model?.vertices ?? []);
const edges = computed(() => props.model.model?.edges ?? []);
const conceptSearchTerm = ref('');

function onCellEditComplete() {
	conceptSearchTerm.value = '';
}
</script>
