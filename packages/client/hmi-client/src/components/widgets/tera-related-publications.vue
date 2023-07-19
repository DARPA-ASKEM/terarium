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
					<a :href="publication.xdd_uri">{{ publication.name }}</a>
				</li>
				<li v-for="(artifact, index) in artifacts" :key="index">
					<span>{{ artifact.name }}</span>
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
						@click="sendForEnrichments(selectedResources)"
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

import { Artifact, DocumentAsset } from '@/types/Types';

const visible = ref(false);
const selectedResources = ref();

const allResources: ComputedRef<
	{ name: string; id: string | undefined; authors: string }[] | any[]
> = computed(() => {
	if (props.project?.assets) {
		const artifactResources = props.project?.assets.artifacts
			.filter((artifact: Artifact) =>
				[
					AcceptedExtensions.PDF,
					AcceptedExtensions.TXT,
					AcceptedExtensions.MD,
					AcceptedExtensions.PY
				].some((extension) => artifact.name.endsWith(extension))
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
	publications?: any[];
	artifacts?: any[];
	dialogFlavour: string;
	assetId?: string;
}>();
const emit = defineEmits(['extracted-metadata']);

const addResources = () => {
	visible.value = true;
	// do something
};

async function sendForEnrichments(_selectedResources) {
	console.log('sending these resources for enrichment:', _selectedResources);
	if (props.assetId && _selectedResources.length > 1) {
		// const metadata = await enrichModel(props.assetId, _selectedResources[0].id, _selectedResources[1].id);
		// mocked extraction
		const metadata = {
			DESCRIPTION:
				'Understanding the dynamics of SARS‑CoV‑2 variants of concern in Ontario, Canada: a modeling study',
			AUTHOR_INST: 'University of Waterloo',
			AUTHOR_AUTHOR: 'Anita T. Layton, Mehrshad Sadria',
			AUTHOR_EMAIL: 'anita.layton@uwaterloo.ca',
			DATE: 'UNKNOWN',
			SCHEMA: 'UNKNOWN',
			PROVENANCE:
				'The model was developed and applied to better understand the spread of multiple variants of concern (VOC) of SARS-CoV-2 in Ontario, Canada. The model incorporates competition among VOC and assesses the effectiveness of vaccination and non-pharmaceutical interventions (NPI) in controlling the spread of the virus.',
			DATASET: 'UNKNOWN',
			COMPLEXITY: 'The complexity of the model is not specified.',
			USAGE:
				'The model should be used to understand the dynamics of SARS-CoV-2 variants of concern and to assess the effectiveness of vaccination and NPI in controlling the spread of the virus.',
			LICENSE: 'UNKNOWN'
		};
		const selectedPublications = _selectedResources.filter((resource) =>
			props.project?.assets?.publications.find((doc) => doc.id === resource.id)
		);
		const selectedArtifacts = _selectedResources.filter((resource) =>
			props.project?.assets?.artifacts.find((a) => a.id === resource.id)
		);
		emit('extracted-metadata', {
			payload: metadata,
			publications: selectedPublications,
			artifacts: selectedArtifacts
		});
	}
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

ul {
	margin: 1rem 0;
}
</style>
