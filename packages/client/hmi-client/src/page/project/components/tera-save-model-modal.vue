<template>
	<tera-save-asset-modal
		:title="title"
		:is-visible="isVisible"
		:open-on-save="openOnSave"
		@close-modal="emit('close-modal')"
		@save="save"
	/>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { PropType } from 'vue';
import { AssetType } from '@/types/Types';
import type { Model } from '@/types/Types';
import { createModel, updateModel } from '@/services/model';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import { logger } from '@/utils/logger';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraSaveAssetModal from './tera-save-asset-modal.vue';

const props = defineProps({
	title: {
		type: String,
		default: 'Save as a new model'
	},
	initialName: {
		type: String,
		default: ''
	},
	isVisible: {
		type: Boolean,
		default: false
	},
	asset: {
		type: Object as PropType<Model>,
		default: newAMR()
	},
	assetType: {
		type: Object as PropType<AssetType>,
		default: AssetType.Model
	},
	openOnSave: {
		type: Boolean,
		default: false
	},
	isUpdatingName: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['close-modal', 'on-save']);

let newAsset: any = null;
const projectResource = useProjects();

async function saveAs() {
	const response = await createModel(newAsset);

	if (!response?.id) return;

	const projectId = projectResource.activeProject.value?.id;
	await projectResource.addAsset(props.assetType, response.id, projectId);

	logger.info(
		`${response.name} saved successfully in project ${projectResource.activeProject.value?.name}.`
	);

	if (props.openOnSave) {
		router.push({
			name: RouteName.Project,
			params: {
				pageType: props.assetType,
				assetId: response.id
			}
		});
	}

	emit('on-save', response);
}

async function updateName() {
	let response: any = null;

	switch (props.assetType) {
		case AssetType.Model:
			response = await updateModel(newAsset);
			break;
		default:
			break;
	}

	if (!response) return;

	// TODO: Consider calling this refresh within the update functions in the services themselves
	projectResource.refresh();

	logger.info(`Updated ${props.assetType} name to ${response.name}.`);
	emit('on-save', response.name);
}

function save(newName: string) {
	newAsset = cloneDeep(props.asset);

	if (props.assetType === AssetType.Model) {
		newAsset = newAsset as Model;
		newAsset.header.name = newName;
	}

	if (props.isUpdatingName) {
		updateName(newAsset);
	} else {
		saveAs(newAsset);
	}

	emit('close-modal');
}
</script>
