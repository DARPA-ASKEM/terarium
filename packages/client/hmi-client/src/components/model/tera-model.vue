<template>
	<tera-asset
		v-bind="$attrs"
		:id="assetId"
		:name="model?.header.name"
		:feature-config="featureConfig"
		:is-naming-asset="isNaming"
		:is-loading="isModelLoading"
		show-table-of-contents
		@close-preview="emit('close-preview')"
	>
		<template #name-input>
			<tera-input-text
				v-if="isNaming"
				v-model.lazy="newName"
				placeholder="Title of new model"
				@keyup.enter="updateModelName"
				@keyup.esc="updateModelName"
				auto-focus
				class="w-4"
			/>
			<div v-if="isNaming" class="flex flex-nowrap ml-1 mr-3">
				<Button icon="pi pi-check" rounded text @click="updateModelName" />
			</div>
		</template>
		<template #edit-buttons v-if="!featureConfig.isPreview">
			<Button icon="pi pi-ellipsis-v" text rounded @click="toggleOptionsMenu" />
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" popup :pt="optionsMenuPt" />
			<aside class="btn-group">
				<tera-asset-enrichment :asset-type="AssetType.Model" :assetId="assetId" @finished-job="fetchModel" />
				<Button
					label="Reset"
					severity="secondary"
					outlined
					@click="onReset"
					:disabled="hasChanged || !hasEditPermission"
				/>
				<Button label="Save as" severity="secondary" outlined @click="onSaveAs" :disabled="!hasEditPermission" />
				<Button label="Save" @click="onSave" :disabled="hasChanged || !hasEditPermission" />
			</aside>
		</template>
		<section v-if="temporaryModel">
			<tera-model-description
				:model="temporaryModel"
				:feature-config="featureConfig"
				@update-model="updateModelContent"
			/>
			<tera-model-parts
				class="mt-0"
				:model="temporaryModel"
				:feature-config="featureConfig"
				@update-model="updateModelContent"
			/>
		</section>
	</tera-asset>
	<tera-save-asset-modal
		:asset="temporaryModel"
		:asset-type="AssetType.Model"
		:initial-name="model?.header.name"
		:is-visible="showSaveModal"
		@close-modal="showSaveModal = false"
		@on-save="onModalSave"
	/>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetEnrichment from '@/components/widgets/tera-asset-enrichment.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModelParts from '@/components/model/tera-model-parts.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import { getModel, updateModel } from '@/services/model';
import type { FeatureConfig } from '@/types/common';
import { AssetType, ClientEvent, ClientEventType, type Model, TaskResponse, TaskStatus } from '@/types/Types';
import { useClientEvent } from '@/composables/useClientEvent';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';

const props = defineProps({
	assetId: {
		type: String,
		default: ''
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const emit = defineEmits(['close-preview']);

// Listen for the task completion event
useClientEvent(ClientEventType.TaskGollmModelCard, (event: ClientEvent<TaskResponse>) => {
	if (
		!event.data ||
		event.data.status !== TaskStatus.Success ||
		event.data.additionalProperties.modelId !== props.assetId
	) {
		return;
	}
	fetchModel();
});

const model = ref<Model | null>(null);
const temporaryModel = ref<Model | null>(null);

const newName = ref('New Model');
const isRenaming = ref(false);
const isModelLoading = ref(false);
const showSaveModal = ref(false);
const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);
const hasChanged = computed(() => !isEqual(model.value, temporaryModel.value));
const hasEditPermission = computed(() => useProjects().hasEditPermission());

// Edit menu
function onReset() {
	temporaryModel.value = cloneDeep(model.value);
}
function onSave() {
	updateModelContent();
}
function onSaveAs() {
	showSaveModal.value = true;
}

// Save modal
function onModalSave() {
	showSaveModal.value = false;
}

// User menu
const toggleOptionsMenu = (event) => optionsMenu.value.toggle(event);
const optionsMenu = ref();
const optionsMenuItems = computed(() => [
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenaming.value = true;
			newName.value = model.value?.header.name ?? '';
		}
	},
	{
		icon: 'pi pi-plus',
		label: 'Add to project',
		items:
			useProjects()
				.allProjects.value?.filter((project) => project.id !== useProjects().activeProject.value?.id)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(AssetType.Model, props.assetId, project.id);
						if (response) logger.info(`Added asset to ${project.name}`);
					}
				})) ?? []
	},
	{
		icon: 'pi pi-download',
		label: 'Download',
		command: async () => {
			if (model.value) {
				const data = `text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(model.value, null, 2))}`;
				const a = document.createElement('a');
				a.href = `data:${data}`;
				a.download = `${model.value.header.name ?? 'model'}.json`;
				a.innerHTML = 'download JSON';
				a.click();
				a.remove();
			}
			emit('close-preview');
		}
	}
]);
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};

async function updateModelContent() {
	if (!hasEditPermission.value || !temporaryModel.value) return;
	await updateModel(temporaryModel.value);
	logger.info('Changes to the model has been saved.');
	await useProjects().refresh();
	await fetchModel();
}

async function updateModelName() {
	if (temporaryModel.value && !isEmpty(newName.value)) {
		temporaryModel.value.header.name = newName.value;
		await updateModelContent();
	}
	isRenaming.value = false;
}

async function fetchModel() {
	model.value = await getModel(props.assetId);
	temporaryModel.value = cloneDeep(model.value);
}

watch(
	() => [props.assetId],
	async () => {
		// Reset view of model page
		isRenaming.value = false;
		if (!isEmpty(props.assetId)) {
			isModelLoading.value = true;
			await fetchModel();
			isModelLoading.value = false;
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
	margin-left: auto;
}
</style>
