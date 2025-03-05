<template>
	<Accordion multiple :active-index="currentActiveIndexes" v-bind:lazy="true" class="mb-0">
		<AccordionTab header="Description">
			<tera-progress-spinner v-if="isGeneratingCard" is-centered> Generating description... </tera-progress-spinner>
			<Editor v-else v-model="editorContent" />
		</AccordionTab>
		<AccordionTab header="Diagram">
			<tera-model-diagram :model="model" :mmt-data="mmtData" />
		</AccordionTab>
		<AccordionTab header="Model equations">
			<tera-model-equation :model="model" :is-editable="false" @model-updated="emit('update-model')" />
		</AccordionTab>
		<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
			<DataTable :value="relatedTerariumModels">
				<Column field="name" header="Models" />
			</DataTable>
			<DataTable :value="relatedTerariumDatasets">
				<Column field="name" header="Datasets" />
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Editor from 'primevue/editor';
import type { Dataset, Model } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { isDataset, isModel, type Asset } from '@/utils/asset';
import { b64DecodeUnicode, b64EncodeUnicode } from '@/utils/binary';
import type { MMT } from '@/model-representation/mira/mira-common';

const props = defineProps<{
	model: Model;
	mmtData: MMT;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model']);

const currentActiveIndexes = ref([0, 1, 2, 3]);
const relatedTerariumArtifacts = ref<Asset[]>([]);
const relatedTerariumModels = computed(() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]);
const relatedTerariumDatasets = computed(() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]);

// Editor for the description
const editorContent = ref(b64DecodeUnicode(props.model?.metadata?.description ?? ''));

watch(editorContent, () => {
	if (editorContent.value !== props.model?.metadata?.description) {
		const updatedModel: Model = {
			...props.model,
			metadata: { ...props.model.metadata, description: b64EncodeUnicode(editorContent.value) }
		};
		emit('update-model', updatedModel);
	}
});

watch(
	() => props.model?.metadata?.description,
	(newDescription) => {
		if (newDescription !== editorContent.value) {
			editorContent.value = b64DecodeUnicode(newDescription ?? '');
		}
	}
);
</script>

<style scoped>
/* add space beneath when accordion content is visible*/
:deep(.p-toggleable-content) {
	padding-bottom: var(--gap-3);
}
</style>
