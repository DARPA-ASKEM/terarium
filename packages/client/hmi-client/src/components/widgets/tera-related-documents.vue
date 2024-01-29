<template>
	<main>
		<section>
			<h5>Related publications</h5>
			<p>
				Terarium can extract information from documents to add relevant information to this
				resource.
			</p>
			<ul>
				<li v-for="document in relatedDocuments" :key="document.id">
					<tera-asset-link
						:label="document.name!"
						:asset-route="{ assetId: document.id!, pageType: AssetType.Document }"
					/>
				</li>
			</ul>
			<div class="extraction-commands">
				<Button text label="Enrich description" :loading="isLoading" @click="dialogForEnrichment" />
				<Button text label="Extract variables" :loading="isLoading" @click="dialogForExtraction" />
				<Button
					text
					:disabled="props.assetType != AssetType.Model"
					:label="`Align extractions to ${assetType}`"
					:loading="isLoading"
					@click="dialogForAlignment"
				/>
			</div>
		</section>
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
			<aside v-if="dialogType === DialogType.EXTRACT && assetType === AssetType.Model">
				<p>Which extraction service would you like to use?</p>
				<RadioButton
					v-model="extractionService"
					inputId="extractionServiceSkema"
					name="skema"
					:value="Extractor.SKEMA"
				/>
				<label for="extractionServiceSkema">SKEMA</label>
				<RadioButton
					v-model="extractionService"
					inputId="extractionServiceMit"
					name="mit"
					:value="Extractor.MIT"
				/>
				<label for="extractionServiceMit">MIT</label>
			</aside>
			<template #footer>
				<Button severity="secondary" outlined label="Cancel" @click="closeDialog" />
				<Button :label="dialogActionCopy" :disabled="isDialogDisabled" @click="acceptDialog" />
			</template>
		</Dialog>
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import RadioButton from 'primevue/radiobutton';
import {
	alignModel,
	Extractor,
	fetchExtraction,
	pdfExtractions,
	profileDataset,
	profileModel
} from '@/services/knowledge';
import { PollerResult } from '@/api/api';
import { isEmpty } from 'lodash';
import type { DocumentAsset, TerariumAsset } from '@/types/Types';
import { AssetType, ProvenanceType } from '@/types/Types';
import {
	createProvenance,
	getRelatedArtifacts,
	mapAssetTypeToProvenanceType,
	RelationshipType
} from '@/services/provenance';
import { isDocumentAsset } from '@/utils/data-util';
import TeraAssetLink from './tera-asset-link.vue';

const props = defineProps<{
	documents?: Array<{ name: string | undefined; id: string | undefined }>;
	assetType: AssetType;
	assetId: TerariumAsset['id'];
}>();

enum DialogType {
	ENRICH,
	EXTRACT,
	ALIGN
}

const emit = defineEmits(['enriched']);
const visible = ref(false);
const selectedResources = ref();
const dialogType = ref<DialogType>(DialogType.ENRICH);
const extractionService = ref<Extractor>(Extractor.SKEMA);
const isLoading = ref(false);
const relatedDocuments = ref<Array<{ name: string | undefined; id: string | undefined }>>([]);

const dialogActionCopy = ref('');
function openDialog() {
	visible.value = true;
}
function closeDialog() {
	visible.value = false;
}

function dialogForEnrichment() {
	dialogType.value = DialogType.ENRICH;
	dialogActionCopy.value = 'Use this resource to enrich descriptions';
	openDialog();
}

function dialogForExtraction() {
	dialogType.value = DialogType.EXTRACT;
	dialogActionCopy.value = 'Use this resource to extract variables';
	openDialog();
}

function dialogForAlignment() {
	dialogType.value = DialogType.ALIGN;
	dialogActionCopy.value = `Use this resource to align the ${props.assetType}`;
	openDialog();
}

const acceptDialog = () => {
	if (dialogType.value === DialogType.ENRICH) {
		sendForEnrichment();
	} else if (dialogType.value === DialogType.EXTRACT) {
		sendForExtractions();
	} else if (dialogType.value === DialogType.ALIGN) {
		sendToAlignModel();
	}
	closeDialog();
};

// Disable the dialog action button if no resources are selected
// and the dialog type is not enrichment
const isDialogDisabled = computed(() => {
	if (dialogType.value === DialogType.ENRICH) return false;
	return !selectedResources.value;
});

const sendForEnrichment = async () => {
	const jobIds: (string | null)[] = [];
	const selectedResourceId = selectedResources.value?.id ?? null;
	const extractionList: Promise<PollerResult<any>>[] = [];

	isLoading.value = true;
	// Build enrichment job ids list (profile asset, align model, etc...)
	if (props.assetType === AssetType.Model) {
		const profileModelJobId = await profileModel(props.assetId, selectedResourceId);
		jobIds.push(profileModelJobId);
	} else if (props.assetType === AssetType.Dataset) {
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

	isLoading.value = false;
	emit('enriched');
	await getRelatedDocuments();
};

const sendForExtractions = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;
	isLoading.value = true;

	const pdfExtractionsJobId = await pdfExtractions(selectedResourceId, extractionService.value);
	if (!pdfExtractionsJobId || !props.assetId) return;
	await createProvenance({
		relation_type: RelationshipType.EXTRACTED_FROM,
		left: props.assetId,
		left_type: mapAssetTypeToProvenanceType(props.assetType),
		right: selectedResourceId,
		right_type: ProvenanceType.Document
	});
	await fetchExtraction(pdfExtractionsJobId);

	isLoading.value = false;
	emit('enriched');
	await getRelatedDocuments();
};

const sendToAlignModel = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;
	if (props.assetType === AssetType.Model && selectedResourceId) {
		isLoading.value = true;

		const linkAmrJobId = await alignModel(props.assetId, selectedResourceId);
		if (!linkAmrJobId) return;
		await fetchExtraction(linkAmrJobId);

		isLoading.value = false;
		emit('enriched');
		await getRelatedDocuments();
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
	const provenanceType = mapAssetTypeToProvenanceType(props.assetType);

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
main {
	background-color: var(--surface-highlight);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	padding: var(--gap-small) var(--gap);

	& > section {
		display: flex;
		gap: var(--gap-small);
		flex-direction: column;
	}
}

ul {
	margin: var(--gap) 0;
	list-style: none;
}

ul:empty {
	display: none;
}

.no-documents {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.extraction-commands > .p-button {
	padding: 0.25rem var(--gap-small);
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

.p-dialog aside > * {
	margin-top: var(--gap);
}
.p-dialog aside label {
	margin: 0 var(--gap) 0 var(--gap-small);
}
</style>
