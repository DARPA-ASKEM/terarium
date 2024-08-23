<template>
	<!-- <p v-if="isEmpty(relatedDocuments)">
			Terarium can extract information from documents to add relevant information to this resource.
		</p>
		<template v-else>
			<p>
				Related publications, documents, and other resources that are relevant to this
				{{ assetType }}.
			</p>
			<ul>
				<li v-for="document in relatedDocuments" :key="document.id">
					<tera-asset-link
						:label="document.name"
						:asset-route="{ assetId: document.id, pageType: AssetType.Document }"
						show-icon
					/>
				</li>
			</ul>
		</template> -->
	<Button
		label="Enrich metadata with AI assistant"
		:loading="isLoading"
		severity="secondary"
		outlined
		@click="dialogForEnrichment"
	/>
	<!-- dialogForExtraction v-if="assetType === AssetType.Model"-->
	<Dialog modal v-model:visible="visible" :header="`Describe this ${assetType}`" :style="{ width: '50vw' }">
		<p class="constrain-width mt-2 mb-4">
			Terarium can extract information from documents to describe this
			{{ assetType }}.<br />Select a document you would like to use.
		</p>
		<DataTable
			v-if="!isEmpty(documents)"
			:value="documents"
			v-model:selection="selectedResources"
			selection-mode="single"
		>
			<Column selectionMode="single" headerStyle="width: 3rem" />
			<Column field="name" sortable header="Name" />
		</DataTable>
		<div v-else>
			<div class="no-documents">
				<img class="no-documents-img" src="@assets/svg/plants.svg" alt="" />
				<div class="no-documents-text">You don't have any resources that can be used. Try adding some documents.</div>
				<div class="no-documents-text">
					Would you like to generate descriptions without attaching additional context?
				</div>
			</div>
		</div>
		<template #footer>
			<Button severity="secondary" outlined label="Cancel" @click="closeDialog" />
			<Button :label="dialogActionCopy" :disabled="isDialogDisabled" @click="acceptDialog" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { extractPDF, extractVariables, profileDataset } from '@/services/knowledge';
import {
	RelationshipType,
	createProvenance,
	getRelatedArtifacts,
	mapAssetTypeToProvenanceType
} from '@/services/provenance';
import type { DocumentAsset, TerariumAsset } from '@/types/Types';
import { AssetType, ProvenanceType } from '@/types/Types';
import { isDocumentAsset } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Dialog from 'primevue/dialog';
import { computed, onMounted, ref, watch } from 'vue';
import { logger } from '@/utils/logger';
import { modelCard } from '@/services/goLLM';
// import TeraAssetLink from './tera-asset-link.vue';

const props = defineProps<{
	documents: { name: string; id: string }[];
	assetType: AssetType;
	assetId: TerariumAsset['id'];
}>();

enum DialogType {
	ENRICH,
	EXTRACT
}

const emit = defineEmits(['enriched', 'extracted']);
const visible = ref(false);
const selectedResources = ref();
const dialogType = ref<DialogType>(DialogType.ENRICH);
const isLoading = ref(false);
const relatedDocuments = ref<Array<{ name: string; id: string }>>([]);

// Disable the dialog action button if no resources are selected
// and the dialog type is not enrichment
const isDialogDisabled = computed(() => {
	if (dialogType.value === DialogType.ENRICH) return false;
	return !selectedResources.value;
});

const dialogActionCopy = computed(() => {
	let result: string = '';
	if (dialogType.value === DialogType.ENRICH) {
		result = props.assetType === AssetType.Model ? 'Enrich description' : 'Generate descriptions';
	} else if (dialogType.value === DialogType.EXTRACT) {
		result = 'Extract variables';
	}
	if (isEmpty(selectedResources.value)) {
		return result;
	}
	return `Use Document to ${result.toLowerCase()}`;
});

function openDialog() {
	visible.value = true;
}
function closeDialog() {
	visible.value = false;
}

function dialogForEnrichment() {
	dialogType.value = DialogType.ENRICH;
	openDialog();
}

// function dialogForExtraction() {
// 	dialogType.value = DialogType.EXTRACT;
// 	openDialog();
// }

const acceptDialog = () => {
	if (dialogType.value === DialogType.ENRICH) {
		sendForEnrichment();
	} else if (dialogType.value === DialogType.EXTRACT) {
		sendForExtractions();
	}
	closeDialog();
};

const sendForEnrichment = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;

	isLoading.value = true;
	// Build enrichment job ids list (profile asset, align model, etc...)
	if (props.assetType === AssetType.Model) {
		await modelCard(selectedResourceId);
	} else if (props.assetType === AssetType.Dataset) {
		await profileDataset(props.assetId, selectedResourceId);
	}

	isLoading.value = false;
	emit('enriched');
	await getRelatedDocuments();
};

const sendForExtractions = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;
	isLoading.value = true;

	// Dataset extraction
	if (props.assetType === AssetType.Dataset) {
		await extractPDF(selectedResourceId);
		await createProvenance({
			relation_type: RelationshipType.EXTRACTED_FROM,
			left: props.assetId!,
			left_type: mapAssetTypeToProvenanceType(props.assetType),
			right: selectedResourceId,
			right_type: ProvenanceType.Document
		});

		logger.info('Provenance created after extraction', { showToast: false });
		emit('extracted');
	}

	// Model extraction
	if (props.assetType === AssetType.Model && selectedResourceId) {
		await extractVariables(selectedResourceId, [props.assetId]);
		emit('enriched');
	}

	isLoading.value = false;
	await getRelatedDocuments();
};

async function getRelatedDocuments() {
	if (!props.assetType) return;

	const provenanceType = mapAssetTypeToProvenanceType(props.assetType);
	if (!provenanceType) return;

	await getRelatedArtifacts(props.assetId, provenanceType, [ProvenanceType.Document]).then((nodes) => {
		const provenanceNodes = nodes ?? [];
		relatedDocuments.value =
			(provenanceNodes.filter((node) => isDocumentAsset(node)) as DocumentAsset[]).map(({ id, name }) => ({
				id: id ?? '',
				name: name ?? ''
			})) ?? [];
	});
}

onMounted(() => {
	getRelatedDocuments();
});

watch(
	() => props.assetId,
	() => getRelatedDocuments()
);
</script>

<style scoped>
main {
	display: flex;
	gap: var(--gap-small);
	flex-direction: column;
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
