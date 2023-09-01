<template>
	<main>
		<p>
			Terarium can extract information from artifacts to add relevant information to this resource.
		</p>
		<ul>
			<li v-for="publication in relatedPublications" :key="publication.id">
				{{ publication.name }}
			</li>
		</ul>
		<Button icon="pi pi-plus" label="Add resources" text @click="visible = true" />
		<Dialog
			v-model:visible="visible"
			modal
			:header="`Describe this ${assetType}`"
			:style="{ width: '50vw' }"
		>
			<p class="constrain-width">
				Terarium can extract information from artifacts to describe this
				{{ assetType }}. Select the artifacts you would like to use.
			</p>
			<DataTable
				v-if="publications && publications.length > 0"
				:value="publications"
				v-model:selection="selectedResources"
				tableStyle="min-width: 50rem"
				selection-mode="single"
			>
				<Column selectionMode="single" headerStyle="width: 3rem" />
				<Column field="name" sortable header="Name" />
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
import { ref } from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { ResourceType } from '@/types/common';
import { profileDataset, profileModel, fetchExtraction } from '@/services/knowledge';

const props = defineProps<{
	publications?: Array<{ name: string; id: string | undefined }>;
	relatedPublications?: Array<{ name: string; id: string | undefined }>;
	assetType: ResourceType;
	assetId: string;
}>();

const emit = defineEmits(['enriched']);
const visible = ref(false);
const selectedResources = ref();

const sendForEnrichments = async (/* _selectedResources */) => {
	// 1. Send asset profile request
	let resp = null;

	if (props.assetType === ResourceType.MODEL) {
		resp = await profileModel(props.assetId, selectedResources?.value?.id ?? null);
	} else if (props.assetType === ResourceType.DATASET) {
		resp = await profileDataset(props.assetId, selectedResources?.value?.id ?? null);
	}
	if (!resp) return;

	// 2. Poll
	const pollResult = await fetchExtraction(resp);
	console.log('enrichment poll', pollResult);
	emit('enriched');
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
