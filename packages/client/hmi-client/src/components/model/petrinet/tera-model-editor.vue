<template>
	<Accordion multiple :active-index="[0, 1, 2, 3]" v-bind:lazy="true">
		<AccordionTab header="Model diagram">
			<tera-model-diagram
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="!featureConfig.isPreview"
				:model-configuration="modelConfigurations[0]"
				@update-model="updateModelContent"
				@update-configuration="updateConfiguration"
			/>
		</AccordionTab>
		<AccordionTab header="Model equations">
			<tera-model-equation
				:model="model"
				:is-editable="!featureConfig.isPreview"
				@model-updated="emit('model-updated')"
			/>
		</AccordionTab>
		<AccordionTab header="Model observables">
			<tera-model-observable
				:model="model"
				:is-editable="!featureConfig.isPreview"
				@update-model="updateModelContent"
			/>
		</AccordionTab>
		<AccordionTab header="Model configurations">
			<tera-model-configurations
				:model="model"
				:model-configurations="modelConfigurations"
				:feature-config="featureConfig"
				@update-model="updateModelContent"
				@update-configuration="updateConfiguration"
				@add-configuration="addConfiguration"
		/></AccordionTab>
		<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
			<DataTable :value="relatedTerariumModels">
				<Column field="name" header="Models" />
			</DataTable>
			<DataTable :value="relatedTerariumDatasets">
				<Column field="name" header="Datasets" />
			</DataTable>
			<DataTable :value="relatedTerariumDocuments">
				<Column field="name" header="Documents" />
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, computed } from 'vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelObservable from '@/components/model/petrinet/tera-model-observable.vue';
import TeraModelConfigurations from '@/components/model/petrinet/tera-model-configurations.vue';
import { FeatureConfig, ResultType } from '@/types/common';
import { Document, Dataset, Model, ModelConfiguration } from '@/types/Types';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { isModel, isDataset, isDocument } from '@/utils/data-util';

defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	featureConfig: FeatureConfig;
}>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

const emit = defineEmits([
	'model-updated',
	'update-model',
	'update-configuration',
	'add-configuration'
]);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

const teraModelDiagramRef = ref();

function updateModelContent(updatedModel: Model) {
	emit('update-model', updatedModel);
}

function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	emit('update-configuration', updatedConfiguration);
}

function addConfiguration(configuration: ModelConfiguration) {
	emit('add-configuration', configuration);
}
</script>
