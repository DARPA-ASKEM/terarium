<template>
	<Accordion :activeIndex="0">
		<AccordionTab>
			<template #header>
				<header id="Description">Description</header>
			</template>
			<p>
				Terarium can extract information from papers and other resources to describe this model.
				Select the resources you would like to use.
			</p>
			<Button icon="pi pi-plus" label="Add resources" text @click="visible = true" />

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
				<DataTable :value="resources" :selection="selectedResources" tableStyle="min-width: 50rem">
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
import { ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const visible = ref(false);
const resources = ref();
const selectedResources = ref();

function sendForEnrichments(_selectedResources) {
	console.log('sending these resources for enrichment:', _selectedResources);
	/* TODO: send selected resources to backend for enrichment */
}
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
</style>
