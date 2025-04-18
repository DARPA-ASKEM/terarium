<template>
	<Button
		label="Enrich metadata"
		icon="pi pi-sparkles"
		:class="enrichStatus === OperatorStatus.ERROR ? 'error' : ''"
		:disabled="isLoading"
		:loading="isLoading"
		severity="secondary"
		outlined
		@click="isModalVisible = true"
	/>
	<tera-modal v-if="isModalVisible" @modal-mask-clicked="isModalVisible = false">
		<template #header>
			<h4>Enrich metadata</h4>
		</template>
		<p class="mb-2">The AI assistant can enrich the metadata of this {{ assetType }}.</p>
		<p>Select a document or generate the information without additional context.</p>
		<ul>
			<li class="mb-0">
				<label for="no-document">
					<RadioButton inputId="no-document" name="no-document" v-model="selectedResourceId" value="" />
					Generate information without context
				</label>
			</li>
			<li v-for="document in documents" :key="document.id" :class="document.id ? '' : 'mb-3'">
				<label :for="document.id">
					<RadioButton :inputId="document.id" name="document.id" v-model="selectedResourceId" :value="document.id" />
					{{ document.name }}
				</label>
			</li>
		</ul>
		<template #footer>
			<div class="btn-group">
				<Button label="Cancel" severity="secondary" outlined @click="closeDialog" />
				<Button label="Enrich" @click="confirm" />
			</div>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import RadioButton from 'primevue/radiobutton';
import { datasetCard, enrichModelMetadata } from '@/services/goLLM';
import { AssetType, ClientEventType, ProjectAsset, TerariumAsset } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { createEnrichClientEventHandler, useClientEvent } from '@/composables/useClientEvent';
import { OperatorStatus } from '@/types/workflow';

const emit = defineEmits(['finished-job']);

const props = defineProps<{
	assetType: AssetType.Model | AssetType.Dataset;
	assetId: TerariumAsset['id'];
}>();

const isLoading = computed(() => enrichStatus.value === OperatorStatus.IN_PROGRESS);
const isModalVisible = ref(false);
const enrichStatus = ref<string>('');
useClientEvent(
	[ClientEventType.TaskGollmEnrichDataset, ClientEventType.TaskGollmEnrichModel],
	createEnrichClientEventHandler(enrichStatus, props.assetId || null, emit)
);

const selectedResourceId = ref<string>('');

const documents = computed<{ name: string; id: string }[]>(() =>
	useProjects()
		.getActiveProjectAssets(AssetType.Document)
		.map((projectAsset: ProjectAsset) => ({
			name: projectAsset.assetName,
			id: projectAsset.assetId
		}))
);

function closeDialog() {
	isModalVisible.value = false;
}

function confirm() {
	closeDialog();
	sendForEnrichment();
}

async function sendForEnrichment() {
	if (props.assetId) {
		if (props.assetType === AssetType.Model) {
			// Build enrichment job ids list (profile asset, align model, etc...)
			await enrichModelMetadata(props.assetId, selectedResourceId.value, true);
		} else {
			await datasetCard(props.assetId, selectedResourceId.value);
		}
		enrichStatus.value = OperatorStatus.IN_PROGRESS;
	}
}
</script>

<style scoped>
main {
	display: flex;
	gap: var(--gap-2);
	flex-direction: column;
}

ul {
	display: flex;
	flex-direction: column;
	margin-top: var(--gap-4);
	gap: var(--gap-2);
	padding: var(--gap-4) 0;

	li {
		display: flex;
		align-items: center;
	}

	li:first-child {
		margin-bottom: var(--gap-2);
	}

	label {
		cursor: pointer;
	}

	/* Add margin between the input and the copy */
	label > :last-child {
		margin-right: var(--gap-2);
	}
}

.p-dialog aside > * {
	margin-top: var(--gap-4);
}

.p-dialog aside label {
	margin: 0 var(--gap-4) 0 var(--gap-2);
}

.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
	margin-left: auto;
}

.error {
	border-color: var(--error-border-color);
	color: var(--error-message-color);
}
</style>
