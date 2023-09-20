<template>
	<tera-asset
		v-if="model"
		:name="model.header.name"
		:feature-config="featureConfig"
		:is-naming-asset="isNaming"
		:stretch-content="view === ModelView.MODEL"
		@close-preview="emit('close-preview')"
	>
		<template #name-input>
			<InputText
				v-if="isNaming"
				v-model.lazy="newName"
				placeholder="Title of new model"
				@keyup.enter="updateModelName"
			/>
		</template>
		<template #edit-buttons>
			<span class="p-buttonset">
				<Button
					class="p-button-secondary p-button-sm"
					label="Description"
					icon="pi pi-list"
					@click="view = ModelView.DESCRIPTION"
					:active="view === ModelView.DESCRIPTION"
				/>
				<Button
					class="p-button-secondary p-button-sm"
					label="Model"
					icon="pi pi-share-alt"
					@click="view = ModelView.MODEL"
					:active="view === ModelView.MODEL"
				/>
			</span>
			<template v-if="!featureConfig.isPreview">
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleOptionsMenu"
				/>
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
		</template>
		<tera-model-description
			v-if="view === ModelView.DESCRIPTION"
			:model="model"
			:model-configurations="modelConfigurations"
			:highlight="highlight"
			:project="project"
			@update-model="updateModelContent"
			@fetch-model="fetchModel"
		/>
		<tera-model-editor
			v-else-if="view === ModelView.MODEL"
			:model="model"
			:model-configurations="modelConfigurations"
			:feature-config="featureConfig"
			@update-model="updateModelContent"
			@update-configuration="updateConfiguration"
			@add-configuration="addConfiguration"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { watch, PropType, ref, computed } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModelEditor from '@/components/model/petrinet/tera-model-editor.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import useResourcesStore from '@/stores/resources';
import {
	createModelConfiguration,
	updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import { getModel, updateModel, getModelConfigurations } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import { IProject } from '@/types/Project';
import { Model, ModelConfiguration } from '@/types/Types';
import * as ProjectService from '@/services/project';

enum ModelView {
	DESCRIPTION,
	MODEL
}

const props = defineProps({
	project: {
		type: Object as PropType<IProject> | null,
		default: null
	},
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
const view = ref(ModelView.DESCRIPTION);
const newName = ref('New Model');
const isRenaming = ref(false);

const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

// User menu
const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenaming.value = true;
			newName.value = model.value?.header.name ?? '';
		}
	}
	// { icon: 'pi pi-clone', label: 'Make a copy', command: initiateModelDuplication }
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteModel }
]);

async function updateModelContent(updatedModel: Model) {
	await updateModel(updatedModel);
	setTimeout(async () => {
		await fetchModel(); // elastic search might still not update in time
		useResourcesStore().setActiveProject(await ProjectService.get(props.project.id, true));
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

async function updateConfiguration(updatedConfiguration: ModelConfiguration, index: number) {
	await updateModelConfiguration(updatedConfiguration);
	setTimeout(async () => {
		emit('update-model-configuration');
		modelConfigurations.value[index] = updatedConfiguration; // Below line would be ideal but the order of the configs change after the refetch
		// await fetchConfigurations(); // elastic search might still not update in time
	}, 800);
}

async function addConfiguration(configuration: ModelConfiguration) {
	if (model.value) {
		await createModelConfiguration(
			model.value.id,
			`Copy of ${configuration.name}`,
			configuration.description as string,
			configuration.configuration
		);
		setTimeout(() => {
			emit('new-model-configuration');
			fetchConfigurations(); // elastic search might still not update in time
		}, 800);
	}
}

async function fetchConfigurations() {
	if (model.value) {
		let tempConfigurations = await getModelConfigurations(model.value.id);

		// Ensure that we always have a "default config" model configuration
		if (
			isEmpty(tempConfigurations) ||
			!tempConfigurations.find((d) => d.name === 'Default config')
		) {
			await addDefaultConfiguration(model.value);
			tempConfigurations = await getModelConfigurations(model.value.id);
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
		view.value = ModelView.DESCRIPTION;
		if (!isEmpty(props.assetId)) await getModelWithConfigurations();
	},
	{ immediate: true }
);
</script>
