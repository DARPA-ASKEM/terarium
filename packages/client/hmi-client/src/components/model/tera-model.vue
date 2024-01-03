<template>
	<tera-asset
		:name="model?.header.name"
		:feature-config="featureConfig"
		:is-naming-asset="isNaming"
		:stretch-content="view === ModelView.MODEL"
		@close-preview="emit('close-preview')"
		:is-loading="isModelLoading"
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
			<SelectButton
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
				option-value="value"
			>
				<template #option="slotProps">
					<i :class="`${slotProps.option.icon} p-button-icon-left`" />
					<span class="p-button-label">{{ slotProps.option.value }}</span>
				</template>
			</SelectButton>
			<template v-if="!featureConfig.isPreview">
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleOptionsMenu"
				/>
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
		</template>
		<template v-if="model">
			<tera-model-editor
				:model="model"
				:model-configurations="modelConfigurations"
				:feature-config="featureConfig"
				@model-updated="getModelWithConfigurations"
				@update-model="updateModelContent"
				@update-configuration="updateConfiguration"
				@add-configuration="addConfiguration"
			/>
			<tera-model-description
				:model="model"
				:model-configurations="modelConfigurations"
				:highlight="highlight"
				@update-model="updateModelContent"
				@fetch-model="fetchModel"
				:key="model?.id"
			/>
		</template>
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
import {
	createModelConfiguration,
	updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import { getModel, updateModel, getModelConfigurations, isModelEmpty } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import { Model, ModelConfiguration } from '@/types/Types';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';

enum ModelView {
	DESCRIPTION = 'Description',
	MODEL = 'Model'
}

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

const view = ref(ModelView.DESCRIPTION);
const viewOptions = ref([
	{ value: ModelView.DESCRIPTION, icon: 'pi pi-list' },
	{ value: ModelView.MODEL, icon: 'pi pi-share-alt' }
]);

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
		await getModelWithConfigurations(); // elastic search might still not update in time
		useProjects().refresh();
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
	await updateModelConfiguration(updatedConfiguration);
	setTimeout(async () => {
		emit('update-model-configuration');
		const indexToUpdate = modelConfigurations.value.findIndex(
			({ id }) => id === updatedConfiguration.id
		);
		modelConfigurations.value[indexToUpdate] = updatedConfiguration; // Below line would be ideal but the order of the configs change after the refetch
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
			(isEmpty(tempConfigurations) ||
				!tempConfigurations.find((d) => d.name === 'Default config')) &&
			!isModelEmpty(model.value)
		) {
			await addDefaultConfiguration(model.value);
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
		view.value = ModelView.DESCRIPTION;
		if (!isEmpty(props.assetId)) {
			isModelLoading.value = true;
			await getModelWithConfigurations();
			isModelLoading.value = false;
		}
	},
	{ immediate: true }
);
</script>
