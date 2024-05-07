<template>
	<tera-asset
		:name="model?.header.name"
		:feature-config="featureConfig"
		:is-naming-asset="isNaming"
		@close-preview="emit('close-preview')"
		:is-loading="isModelLoading"
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
		<template #edit-buttons>
			<span v-if="model" class="ml-auto">{{ model.header.schema_name }}</span>
			<template v-if="!featureConfig.isPreview">
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleOptionsMenu"
				/>
				<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
		</template>
		<tera-model-description
			v-if="model"
			:key="model?.id"
			:model="model"
			:model-configurations="modelConfigurations"
			:feature-config="featureConfig"
			:highlight="highlight"
			@model-updated="getModelWithConfigurations"
			@update-model="updateModelContent"
			@update-configuration="updateConfiguration"
			@fetch-model="fetchModel"
			class="pl-1 pr-1"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import ContextMenu from 'primevue/contextmenu';
import { addDefaultConfiguration, updateModelConfiguration } from '@/services/model-configurations';
import { getModel, getModelConfigurations, isModelEmpty, updateModel } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import { AssetType, type Model, type ModelConfiguration } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';

const props = defineProps({
	assetId: {
		type: String,
		default: ''
	},
	highlight: {
		type: String,
		default: ''
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const emit = defineEmits([
	'close-preview',
	'update-model-configuration',
	'new-model-configuration'
]);

const model = ref<Model | null>(null);
const modelConfigurations = ref<ModelConfiguration[]>([]);
const newName = ref('New Model');
const isRenaming = ref(false);
const isModelLoading = ref(false);

const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

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
				.allProjects.value?.filter(
					(project) => project.id !== useProjects().activeProject.value?.id
				)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(
							AssetType.Model,
							props.assetId,
							project.id
						);
						if (response) logger.info(`Added asset to ${project.name}`);
					}
				})) ?? []
	},
	{
		icon: 'pi pi-download',
		label: 'Download',
		command: async () => {
			if (model.value) {
				const data = `text/json;charset=utf-8,${encodeURIComponent(
					JSON.stringify(model.value, null, 2)
				)}`;
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

	// { icon: 'pi pi-clone', label: 'Make a copy', command: initiateModelDuplication }
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteModel }
]);

async function updateModelContent(updatedModel: Model) {
	await updateModel(updatedModel);
	await useProjects().refresh();
	setTimeout(async () => {
		await getModelWithConfigurations(); // elastic search might still not update in time
	}, 800);
}

async function updateModelName() {
	if (model.value && !isEmpty(newName.value)) {
		const modelClone = cloneDeep(model.value);
		modelClone.header.name = newName.value;
		await updateModelContent(modelClone);
	}
	isRenaming.value = false;
}

async function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	await updateModelConfiguration(updatedConfiguration, useProjects().activeProjectId.value);
	setTimeout(async () => {
		emit('update-model-configuration');
		const indexToUpdate = modelConfigurations.value.findIndex(
			({ id }) => id === updatedConfiguration.id
		);
		modelConfigurations.value[indexToUpdate] = updatedConfiguration; // Below line would be ideal but the order of the configs change after the refetch
		// await fetchConfigurations(); // elastic search might still not update in time
	}, 800);
}

async function fetchConfigurations() {
	if (model.value) {
		let tempConfigurations = await getModelConfigurations(model.value.id);

		// Ensure that we always have a "default config" model configuration
		if (
			(isEmpty(tempConfigurations) ||
				!tempConfigurations.find((d) => d.name === 'Default config')) &&
			!isModelEmpty(model.value)
		) {
			await addDefaultConfiguration(useProjects().activeProjectId.value, model.value);
			setTimeout(async () => {
				// elastic search might still not update in time
				tempConfigurations = await getModelConfigurations(model.value?.id!);
				modelConfigurations.value = tempConfigurations;
			}, 800);
			return;
		}
		modelConfigurations.value = tempConfigurations;
	}
}

async function fetchModel() {
	model.value = await getModel(props.assetId);
}

async function getModelWithConfigurations() {
	await fetchModel();
	await fetchConfigurations();
}

watch(
	() => [props.assetId],
	async () => {
		// Reset view of model page
		isRenaming.value = false;
		if (!isEmpty(props.assetId)) {
			isModelLoading.value = true;
			await getModelWithConfigurations();
			isModelLoading.value = false;
		}
	},
	{ immediate: true }
);
</script>
