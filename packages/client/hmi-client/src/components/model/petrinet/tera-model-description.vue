<template>
	<section>
		<Accordion multiple :active-index="[0, 1, 2, 3]" v-bind:lazy="true" class="mb-0">
			<AccordionTab header="Description">
				<tera-progress-spinner v-if="isGeneratingCard" is-centered> Generating description... </tera-progress-spinner>
				<Editor
					v-else
					v-model="editorContent"
					:class="{ readonly: !hasEditPermission }"
					:readonly="!hasEditPermission"
				/>
			</AccordionTab>
			<AccordionTab header="Diagram">
				<tera-model-diagram ref="teraModelDiagramRef" :model="model" :feature-config="featureConfig" />
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation :model="model" :is-editable="false" @model-updated="emit('model-updated')" />
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
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { FeatureConfig } from '@/types/common';
import type { Dataset, Model } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import { isDataset, isModel, type Asset } from '@/utils/asset';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import Editor from 'primevue/editor';
import { useProjects } from '@/composables/project';

const props = defineProps<{
	model: Model;
	featureConfig?: FeatureConfig;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model', 'model-updated']);
const teraModelDiagramRef = ref();

const card = computed<any>(() => props.model.metadata?.gollmCard ?? null);

const relatedTerariumArtifacts = ref<Asset[]>([]);
const relatedTerariumModels = computed(() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]);
const relatedTerariumDatasets = computed(() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]);

// Editor for the description
const { activeProject } = useProjects();
const hasEditPermission = computed(() => ['creator', 'writer'].includes(activeProject.value?.userPermission ?? ''));
const editorContent = ref(props.model.description ?? card.value ?? '');

watch(editorContent, () => {
	if (editorContent.value !== props.model.description) {
		emit('update-model', { ...props.model, description: editorContent.value });
	}
});
</script>

<style scoped>
/* add space beneath when accordion content is visible*/
:deep(.p-toggleable-content) {
	padding-bottom: var(--gap-3);
}
</style>
