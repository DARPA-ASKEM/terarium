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
			<div class="btn-group">
				<Button label="Reset" severity="secondary" outlined @click="teraModelPartsRef?.reset()" :disabled="isSaved" />
				<Button label="Save as..." severity="secondary" outlined @click="showSaveModal = true" />
				<Button label="Save" @click="updateModelContent(teraModelPartsRef?.transientModel)" :disabled="isSaved" />
			</div>
		</template>
		<section v-if="model">
			<tera-model-description
				:model="model"
				:feature-config="featureConfig"
				@model-updated="fetchModel"
				@update-model="updateModelContent"
			/>
			<tera-model-parts
				ref="teraModelPartsRef"
				class="mt-0"
				:model="model"
				:feature-config="featureConfig"
				@update-model="updateModelContent"
			/>
		</section>
	</tera-asset>
	<tera-save-asset-modal
		:initial-name="model?.header.name"
		:is-visible="showSaveModal"
		:asset="teraModelPartsRef?.transientModel"
		:asset-type="AssetType.Model"
		@close-modal="showSaveModal = false"
		@on-save="showSaveModal = false"
	/>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModelParts from '@/components/model/tera-model-parts.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import { getModel, updateModel } from '@/services/model';
import type { FeatureConfig } from '@/types/common';
import { AssetType, type Model } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import TeraInputText from '@/components/widgets/tera-input-text.vue';

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

const teraModelPartsRef = ref();

const model = ref<Model | null>(null);
const newName = ref('New Model');
const isRenaming = ref(false);
const isModelLoading = ref(false);
const showSaveModal = ref(false);

const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);
const isSaved = computed(() => isEqual(model.value, teraModelPartsRef.value?.transientModel));

const toggleOptionsMenu = (event) => optionsMenu.value.toggle(event);

// User menu
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

async function updateModelContent(updatedModel: Model) {
	if (!useProjects().hasEditPermission()) {
		logger.error('You do not have permission to edit this model.');
		return;
	}
	await updateModel(updatedModel);
	logger.info('Saved changes.');
	await useProjects().refresh();
	fetchModel();
}

async function updateModelName() {
	if (model.value && !isEmpty(newName.value)) {
		const modelClone = cloneDeep(model.value);
		modelClone.header.name = newName.value;
		await updateModelContent(modelClone);
	}
	isRenaming.value = false;
}

async function fetchModel() {
	model.value = await getModel(props.assetId);
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
