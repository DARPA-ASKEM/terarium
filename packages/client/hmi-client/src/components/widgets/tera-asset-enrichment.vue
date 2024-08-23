<template>
	<Button
		label="Enrich metadata with AI assistant"
		:loading="isLoading"
		severity="secondary"
		outlined
		@click="isModalVisible = true"
	/>
	<tera-modal v-if="isModalVisible" @modal-mask-clicked="isModalVisible = false">
		<template #header>
			<h4>Enrich metadata</h4>
		</template>
		<p>
			The AI assistant can enrich the metadata of this {{ assetType }}. Select a document or generate the information
			without additional context.
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
		<template #footer>
			<Button label="Enrich" :disabled="isDialogDisabled" @click="confirm" />
			<Button label="Cancel" severity="secondary" outlined @click="closeDialog" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { extractPDF, extractVariables, profileDataset } from '@/services/knowledge';
import {
	RelationshipType,
	createProvenance,
	getRelatedArtifacts,
	mapAssetTypeToProvenanceType
} from '@/services/provenance';
import type { DocumentAsset, TerariumAsset, ProjectAsset } from '@/types/Types';
import { AssetType, ProvenanceType } from '@/types/Types';
import { isDocumentAsset } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { computed, ref, watch } from 'vue';
import { logger } from '@/utils/logger';
import { modelCard } from '@/services/goLLM';
import { useProjects } from '@/composables/project';
import TeraModal from '@/components/widgets/tera-modal.vue';

const props = defineProps<{
	assetType: AssetType;
	assetId: TerariumAsset['id'];
}>();

enum DialogType {
	ENRICH,
	EXTRACT
}

const emit = defineEmits(['finished-job']);
const isModalVisible = ref(false);
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

// const dialogActionCopy = computed(() => {
// 	let result: string = '';
// 	if (dialogType.value === DialogType.ENRICH) {
// 		result = props.assetType === AssetType.Model ? 'Enrich description' : 'Generate descriptions';
// 	} else if (dialogType.value === DialogType.EXTRACT) {
// 		result = 'Extract variables';
// 	}
// 	if (isEmpty(selectedResources.value)) {
// 		return result;
// 	}
// 	return `Use Document to ${result.toLowerCase()}`;
// });

const documents = computed<{ name: string; id: string }[]>(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.map((projectAsset: ProjectAsset) => ({
				name: projectAsset.assetName,
				id: projectAsset.assetId
			})) ?? []
);

function closeDialog() {
	isModalVisible.value = false;
}

const confirm = async () => {
	const selectedResourceId = selectedResources.value?.id ?? null;
	isLoading.value = true;

	if (dialogType.value === DialogType.ENRICH) {
		sendForEnrichment(selectedResourceId);
	} else if (dialogType.value === DialogType.EXTRACT) {
		sendForExtractions(selectedResourceId);
	}

	isLoading.value = false;
	emit('finished-job');
	await getRelatedDocuments();
	closeDialog();
};

const sendForEnrichment = async (selectedResourceId) => {
	// Build enrichment job ids list (profile asset, align model, etc...)
	if (props.assetType === AssetType.Model) {
		await modelCard(selectedResourceId);
	} else if (props.assetType === AssetType.Dataset) {
		await profileDataset(props.assetId, selectedResourceId);
	}
};

const sendForExtractions = async (selectedResourceId) => {
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
	}

	// Model extraction
	if (props.assetType === AssetType.Model && selectedResourceId) {
		await extractVariables(selectedResourceId, [props.assetId]);
	}
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

watch(
	() => props.assetId,
	() => getRelatedDocuments(),
	{ immediate: true }
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
