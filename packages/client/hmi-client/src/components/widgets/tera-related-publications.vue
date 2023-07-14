<template>
	<Accordion :activeIndex="0">
		<AccordionTab>
			<template #header> Related publications </template>
			<p>
				Terarium can extract information from papers and other resources to add relevant information
				to this resource.
			</p>
			<ul>
				<li v-for="(publication, index) in publications" :key="index">
					<a :href="publication.xdd_uri">{{ publication.title }}</a>
				</li>
			</ul>
			<Button icon="pi pi-plus" label="Add resources" text @click="addResources" />
			<Dialog
				v-model:visible="visible"
				modal
				header="Describe this dataset"
				:style="{ width: '50vw' }"
			>
				<p class="constrain-width">
					Terarium can extract information from papers and other resources to describe this dataset.
					Select the resources you would like to use.
				</p>
				<DataTable v-bind="resources" :selection="selectedResources" tableStyle="min-width: 50rem">
					<Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
					<Column field="name" sortable header="Name"></Column>
					<Column field="authors" sortable header="Authors"></Column>
				</DataTable>
				<template #footer>
					<Button class="secondary-button" label="Cancel" @click="visible = false" />
					<Button
						label="Use these resources to enrich descriptions"
						@click="
							sendForEnrichments(selectedResources);
							visible = false;
						"
					/>
				</template>
			</Dialog>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import { ref, onMounted } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { WASTE_WATER_SURVEILLANCE } from '@/temp/datasets/wasteWaterSurveillance';
import { DocumentAsset } from '@/types/Types';

const visible = ref(false);

const selectedResources = ref();

const props = defineProps<{ publications?: Array<DocumentAsset> }>();
const resources = ref(props.publications);
const emit = defineEmits(['extracted-metadata']);

const addResources = () => {
	visible.value = true;
	// do something
};

function sendForEnrichments(_selectedResources) {
	console.log('sending these resources for enrichment:', _selectedResources);

	emit('extracted-metadata', WASTE_WATER_SURVEILLANCE);
	/* TODO: send selected resources to backend for enrichment */
}

onMounted(() => {
	console.log('test');
	console.log(props.publications);
});
</script>

<style scoped>
.container {
	margin: 1rem;
	max-width: 50rem;
}
.container h5 {
	margin-bottom: 0.5rem;
}
.constrain-width {
	max-width: 50rem;
}

.secondary-button {
	color: var(--text-color-primary);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
}

.secondary-button:hover {
	color: var(--text-color-secondary) !important;
	background-color: var(--surface-highlight) !important;
}

ul {
	margin: 1rem 0;
}
</style>
