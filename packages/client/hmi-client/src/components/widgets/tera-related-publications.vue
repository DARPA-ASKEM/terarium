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
				:header="`Describe this ${dialogFlavour}`"
				:style="{ width: '50vw' }"
			>
				<p class="constrain-width">
					Terarium can extract information from papers and other resources to describe this
					{{ dialogFlavour }}. Select the resources you would like to use.
				</p>
				<DataTable
					:value="allResources"
					v-model:selection="selectedResources"
					tableStyle="min-width: 50rem"
				>
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
import { computed, ComputedRef, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { AcceptedExtensions } from '@/types/common';
import { WASTE_WATER_SURVEILLANCE } from '@/temp/datasets/wasteWaterSurveillance';

import { Artifact, DocumentAsset } from '@/types/Types';

const visible = ref(false);
const selectedResources = ref();

const allResources: ComputedRef<
	{ name: string; id: string | undefined; authors: string }[] | any[]
> = computed(() => {
	if (props.project?.assets) {
		const artifactResources = props.project?.assets.artifacts
			.filter((artifact: Artifact) =>
				[AcceptedExtensions.PDF, AcceptedExtensions.TXT, AcceptedExtensions.MD].some((extension) =>
					artifact.name.endsWith(extension)
				)
			)
			.map((artifact: Artifact) => ({
				name: artifact.name,
				authors: '',
				id: artifact.id,
				type: ProjectAssetTypes.ARTIFACTS
			}));

		const documentResources = props.project?.assets.publications.map((document: DocumentAsset) => ({
			name: document.title,
			authors: '',
			id: document.id,
			type: ProjectAssetTypes.DOCUMENTS
		}));

		return [...documentResources, ...artifactResources];
	}
	return [];
});

const props = defineProps<{
	project?: IProject;
	publications?: Array<DocumentAsset>;
	dialogFlavour: string;
}>();
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

/* TODO: Create a proper secondary outline button in PrimeVue theme */
.secondary-button {
	color: var(--text-color-primary);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
}

.secondary-button:enabled:hover {
	color: var(--text-color-secondary);
	background-color: var(--surface-highlight);
}

ul {
	margin: 1rem 0;
}
</style>
