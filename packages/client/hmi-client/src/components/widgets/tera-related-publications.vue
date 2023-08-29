<template>
	<main>
		<p>
			Terarium can extract information from papers and other artifacts to add relevant information
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
				Terarium can extract information from papers and other artifacts to describe this
				{{ dialogFlavour }}. Select the resources you would like to use.
			</p>
			<DataTable
				v-if="allResources.length > 0"
				:value="allResources"
				v-model:selection="selectedResources"
				tableStyle="min-width: 50rem"
				selection-mode="single"
			>
				<Column selectionMode="single" headerStyle="width: 3rem"></Column>
				<Column field="name" sortable header="Name"></Column>
				<Column field="authors" sortable header="Authors"></Column>
			</DataTable>
			<div v-else>
				<div class="no-artifacts">
					<img class="no-artifacts-img" src="@assets/svg/plants.svg" alt="" />
					<div class="no-artifacts-text">
						You don't have any resources that can be used. Try adding some artifacts.
					</div>
					<div class="no-artifacts-text">
						Would you like to generate descriptions without attaching additional context?
					</div>
				</div>
			</div>
			<template #footer>
				<Button class="secondary-button" label="Cancel" @click="visible = false" />
				<Button
					label="Use these resources to enrich descriptions"
					@click="
						sendForEnrichments();
						visible = false;
					"
				/>
			</template>
		</Dialog>
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import { computed, ComputedRef, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { IProject } from '@/types/Project';
import { AcceptedExtensions } from '@/types/common';

import { Artifact, AssetType, DocumentAsset } from '@/types/Types';
import { profileDataset, fetchExtraction } from '@/services/models/extractions';

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
				type: AssetType.Artifacts
			}));

		const documentResources = props.project?.assets.publications.map((document: DocumentAsset) => ({
			name: document.title,
			authors: '',
			id: document.id,
			type: AssetType.Publications
		}));

		return [...documentResources, ...artifactResources];
	}
	return [];
});

const props = defineProps<{
	project?: IProject;
	publications?: Array<DocumentAsset>;
	dialogFlavour: string;
	assetId: string;
}>();
const emit = defineEmits(['extracted-metadata']);

const addResources = () => {
	visible.value = true;
	// do something
};

const sendForEnrichments = async (/* _selectedResources */) => {
	// 1. Send dataset profile request
	const resp = await profileDataset(props.assetId, selectedResources?.value?.id ?? null);

	// 2. Poll
	const pollResult = await fetchExtraction(resp);
	console.log('enrichment poll', pollResult);

	emit('extracted-metadata', pollResult);
};
</script>

<style scoped>
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

.no-artifacts {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.no-artifacts-img {
	width: 30%;
	padding: 10px;
}

.no-artifacts-text {
	padding: 5px;
	font-size: var(--font-body);
	font-family: var(--font-family);
	font-weight: 500;
	color: var(--text-color-secondary);
	text-align: left;
}
</style>
