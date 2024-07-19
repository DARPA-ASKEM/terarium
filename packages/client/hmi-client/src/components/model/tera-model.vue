<template>
	<tera-asset
		:id="assetId"
		:name="model?.header.name"
		:feature-config="featureConfig"
		:is-naming-asset="isNaming"
		@close-preview="emit('close-preview')"
		:is-loading="isModelLoading"
		show-table-of-contents
	>
		<template #name-input>
			<InputText
				v-if="isNaming"
				v-model.lazy="newName"
				placeholder="Title of new model"
				@keyup.enter="updateModelName"
				@keyup.esc="updateModelName"
				v-focus
			/>
			<div v-if="isNaming" class="flex flex-nowrap ml-1 mr-3">
				<Button icon="pi pi-check" rounded text @click="updateModelName" />
			</div>
		</template>
		<template #edit-buttons v-if="!featureConfig.isPreview">
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleOptionsMenu"
			/>
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			<div class="btn-group">
				<!-- TODO: Reset and Save as buttons
				<Button label="Reset" severity="secondary" outlined />
				<Button label="Save as..." severity="secondary" outlined /> -->
				<Button label="Save" @click="teraModelPartsRef?.saveChanges()" />
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
				@update-model="updateModelContent"
				:readonly="featureConfig?.isPreview"
			/>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModelParts from '@/components/model/tera-model-parts.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import ContextMenu from 'primevue/contextmenu';
import { getModel, updateModel } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import { AssetType, type Model } from '@/types/Types';
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

const teraModelPartsRef = ref();

const model = ref<Model | null>(null);
const newName = ref('New Model');
const isRenaming = ref(false);
const isModelLoading = ref(false);

const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);

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

async function updateModelContent(updatedModel: Model) {
	if (!useProjects().hasEditPermission()) {
		return;
	}
	await updateModel(updatedModel);
	await useProjects().refresh();
	await fetchModel();
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
