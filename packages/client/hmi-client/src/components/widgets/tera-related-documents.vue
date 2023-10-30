<template>
	<main>
		<p>
			Terarium can extract information from documents to add relevant information to this resource.
		</p>
		<ul>
			<li v-for="document in relatedDocuments" :key="document.id">
				<tera-asset-link
					:label="document.name!"
					:asset-route="{ assetId: document.id!, pageType: AssetType.Documents }"
				/>
			</li>
		</ul>
		<Button label="Enrich description" text :loading="enriching" @click="dialogForEnrichment" />
		<Button label="Extract variables" text :loading="enriching" @click="dialogForExtraction" />
		<Button
			v-if="props.assetType === ResourceType.MODEL"
			label="Align extractions to model"
			text
			:loading="aligning"
			@click="dialogForAlignement"
		/>

		<Dialog
			v-model:visible="visible"
			modal
			:header="`Describe this ${assetType}`"
			:style="{ width: '50vw' }"
		>
			<p class="constrain-width">
				Terarium can extract information from artifacts to describe this
				{{ assetType }}. Select the documents you would like to use.
			</p>
			<DataTable
				v-if="documents && documents.length > 0"
				:value="documents"
				v-model:selection="selectedResources"
				tableStyle="min-width: 50rem"
				selection-mode="single"
			>
				<Column selectionMode="single" headerStyle="width: 3rem" />
				<Column field="name" sortable header="Name" />
			</DataTable>
			<div v-else>
				<div class="no-documents">
					<img class="no-documents-img" src="@assets/svg/plants.svg" alt="" />
					<div class="no-documents-text">
						You don't have any resources that can be used. Try adding some documents.
					</div>
					<div class="no-documents-text">
						Would you like to generate descriptions without attaching additional context?
					</div>
				</div>
			</div>
			<template #footer>
				<Button severity="secondary" outlined label="Cancel" @click="visible = false" />
				<Button
					:label="
						dialogType === 'enrich'
							? 'Use these resources to enrich descriptions'
							: 'Use these resources to align the model'
					"
					@click="
						dialogType === 'enrich' ? sendForEnrichments() : sendToAlignModel();
						visible = false;
					"
				/>
			</template>
		</Dialog>
	</main>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { ResourceType } from '@/types/common';
import {
	profileDataset,
	profileModel,
	fetchExtraction,
	alignModel,
	pdfExtractions
} from '@/services/knowledge';
import { PollerResult } from '@/api/api';
import { isEmpty } from 'lodash';
import { AssetType, DocumentAsset, ProvenanceType } from '@/types/Types';
import { getRelatedArtifacts, mapResourceTypeToProvenanceType } from '@/services/provenance';
import { isDocumentAsset } from '@/utils/data-util';
import TeraAssetLink from './tera-asset-link.vue';

const props = defineProps<{
	documents?: Array<{ name: string | undefined; id: string | undefined }>;
	assetType: ResourceType;
	assetId: string;
}>();

enum DialogType {
	ENRICH = 'enrich',
	EXTRACT = 'extract',
	ALIGN = 'align'
}

const emit = defineEmits(['enriched']);
const visible = ref(false);
const selectedResources = ref();
const dialogType = ref<DialogType>(DialogType.ENRICH);
const aligning = ref(false);
const enriching = ref(false);
const relatedDocuments = ref<Array<{ name: string | undefined; id: string | undefined }>>([]);

const dialogForEnrichment = () => {
	dialogType.value = DialogType.ENRICH;
	visible.value = true;
};

const dialogForExtraction = () => {
	dialogType.value = DialogType.EXTRACT;
	visible.value = true;
};

const dialogForAlignement = () => {
	dialogType.value = DialogType.ALIGN;
	visible.value = true;
};

const sendForEnrichments = async (/* _selectedResources */) => {
	const jobIds: (string | null)[] = [];
	const selectedResourceId = selectedResources.value?.id ?? null;
	const extractionList: Promise<PollerResult<any>>[] = [];

	enriching.value = true;
	// Build enrichment job ids list (profile asset, align model, etc...)
	if (props.assetType === ResourceType.MODEL) {
		const profileModelJobId = await profileModel(props.assetId, selectedResourceId);
		jobIds.push(profileModelJobId);
	} else if (props.assetType === ResourceType.DATASET) {
		const profileDatasetJobId = await profileDataset(props.assetId, selectedResourceId);
		jobIds.push(profileDatasetJobId);
	}

	// Create extractions list from job ids
	jobIds.forEach((jobId) => {
		if (jobId) {
			extractionList.push(fetchExtraction(jobId));
		}
	});

	if (isEmpty(extractionList)) return;

	// Poll all extractions
	await Promise.all(extractionList);

	enriching.value = false;
	emit('enriched');
	getRelatedDocuments();
};

const sendToAlignModel = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;
	if (props.assetType === ResourceType.MODEL && selectedResourceId) {
		// fetch pdf extractions and link amr synchronously
		aligning.value = true;
		const pdfExtractionsJobId = await pdfExtractions(selectedResourceId);
		if (!pdfExtractionsJobId) return;
		await fetchExtraction(pdfExtractionsJobId);

		const linkAmrJobId = await alignModel(props.assetId, selectedResourceId);
		if (!linkAmrJobId) return;
		await fetchExtraction(linkAmrJobId);

		aligning.value = false;
		emit('enriched');
		getRelatedDocuments();
	}
};

onMounted(() => {
	getRelatedDocuments();
});

watch(
	() => props.assetId,
	() => {
		getRelatedDocuments();
	}
);

async function getRelatedDocuments() {
	if (!props.assetType) return;
	const provenanceType = mapResourceTypeToProvenanceType(props.assetType);

	if (!provenanceType) return;

	const provenanceNodes = await getRelatedArtifacts(props.assetId, provenanceType, [
		ProvenanceType.Document
	]);

	relatedDocuments.value =
		(provenanceNodes.filter((node) => isDocumentAsset(node)) as DocumentAsset[]).map(
			(documentAsset) => ({
				name: documentAsset.name,
				id: documentAsset.id
			})
		) ?? [];
}
</script>

<style scoped>
ul {
	margin: 1rem 0;
}

.no-documents {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.no-documents-img {
	width: 30%;
	padding: 10px;
}

.no-documents-text {
	padding: 5px;
	font-size: var(--font-body-medium);
	font-family: var(--font-family);
	font-weight: 500;
	color: var(--text-color-secondary);
	text-align: left;
}
</style>
