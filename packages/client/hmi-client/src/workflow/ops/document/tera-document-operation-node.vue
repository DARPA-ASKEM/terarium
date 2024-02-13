<template>
	<main v-if="!fetchingDocument">
		<template v-if="document">
			<h6>
				<span class="truncate-after-three-lines">{{ document?.name }}</span>
			</h6>
			<tera-operator-placeholder :operation-type="node.operationType" />
			<Button label="Open document" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				:options="documents"
				option-label="assetName"
				placeholder="Select document"
				@update:model-value="onDocumentChange"
			/>
			<tera-operator-placeholder :operation-type="node.operationType" />
		</template>
	</main>
	<tera-progress-spinner v-else is-centered :font-size="2" />
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { AssetBlock, WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import type { DocumentAsset, DocumentExtraction, ProjectAsset } from '@/types/Types';
import { AssetType, ExtractionAssetType } from '@/types/Types';
import { onMounted, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { cloneDeep, isEmpty } from 'lodash';
import { getDocumentAsset } from '@/services/document-assets';
import teraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { DocumentOperationState } from './document-operation';

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output-port']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const documents = useProjects().getActiveProjectAssets(AssetType.Document);
const document = ref<DocumentAsset | null>(null);
const fetchingDocument = ref(false);

onMounted(async () => {
	if (props.node.state.documentId) {
		fetchingDocument.value = true;
		document.value = await getDocumentAsset(props.node.state.documentId);
		fetchingDocument.value = false;
	}
});

async function onDocumentChange(chosenProjectDocument: ProjectAsset) {
	if (chosenProjectDocument?.assetId) {
		fetchingDocument.value = true;
		document.value = await getDocumentAsset(chosenProjectDocument.assetId);
		fetchingDocument.value = false;
	}
}

watch(
	() => document.value,
	async () => {
		if (document.value?.id) {
			const figures: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Figure)
					.map((asset, i) => ({
						name: `Figure ${i + 1}`,
						includeInProcess: false,
						asset
					})) || [];
			const tables: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Table)
					.map((asset, i) => ({
						name: `Table ${i + 1}`,
						includeInProcess: false,
						asset
					})) || [];
			const equations: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Equation)
					.map((asset, i) => ({
						name: `Equation ${i + 1}`,
						includeInProcess: true,
						asset
					})) || [];

			const state = cloneDeep(props.node.state);
			state.documentId = document.value.id;
			if (isEmpty(state.equations)) state.equations = equations;
			if (isEmpty(state.figures)) state.figures = figures;
			if (isEmpty(state.tables)) state.tables = tables;
			emit('update-state', state);

			if (!props.node.outputs.find((port) => port.type === 'documentId')) {
				emit('append-output-port', {
					type: 'documentId',
					label: `${document.value.name}`,
					value: [document.value.id]
				});
			}

			if (!props.node.outputs.find((port) => port.type === 'equations') && !isEmpty(equations)) {
				const selected = equations.filter((e) => e.includeInProcess);
				emit('append-output-port', {
					type: 'equations',
					label: `equations (${selected.length}/${equations.length})`,
					value: [
						{
							documentId: document.value.id,
							equations
						}
					]
				});
			}

			if (!props.node.outputs.find((port) => port.type === 'figures') && !isEmpty(figures)) {
				const selected = figures.filter((f) => f.includeInProcess);
				emit('append-output-port', {
					type: 'figures',
					label: `figures (${selected.length}/${figures.length})`,
					value: [
						{
							documentId: document.value.id,
							figures
						}
					]
				});
			}

			if (!props.node.outputs.find((port) => port.type === 'tables') && !isEmpty(tables)) {
				const selected = tables.filter((t) => t.includeInProcess);
				emit('append-output-port', {
					type: 'tables',
					label: `tables (${selected.length}/${tables.length})`,
					value: [
						{
							documentId: document.value.id,
							tables
						}
					]
				});
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
/* Supported by Chromium, Safari, Webkit, Edge and others. Not supported by IE and Opera Mini */
.truncate-after-three-lines {
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}
</style>
