<template>
	<Teleport to="body">
		<tera-modal
			v-if="isVisible"
			class="save-as-dialog"
			@modal-mask-clicked="emit('close-modal')"
			@modal-enter-press="save"
		>
			<template #header>
				<h4>{{ title }}</h4>
			</template>
			<template #default>
				<form @submit.prevent>
					<label for="new-name">What would you like to call it?</label>
					<InputText
						id="new-name"
						type="text"
						v-model="newName"
						placeholder="Enter a unique name"
					/>
					<slot name="extra-input-fields" />
				</form>
			</template>
			<template #footer>
				<Button label="Save" size="large" @click="save" />
				<Button label="Cancel" class="p-button-secondary" size="large" outlined @click="cancel" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, PropType, computed } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { AssetType } from '@/types/Types';
import type { Model } from '@/types/Types';
import type { Workflow } from '@/types/workflow';
import { emptyWorkflow, createWorkflow } from '@/services/workflow';
import { createModel, updateModel } from '@/services/model';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import { logger } from '@/utils/logger';
import router from '@/router';
import { RouteName } from '@/router/routes';

const props = defineProps({
	initialName: {
		type: String,
		default: ''
	},
	isVisible: {
		type: Boolean,
		default: false
	},
	asset: {
		type: Object as PropType<Model> | Object as PropType<Workflow> | null,
		default: null
	},
	assetType: {
		type: String as PropType<AssetType>,
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
const newName = ref<string>(props.initialName);

const title = computed(() => {
	if (!props.asset) return `Create new ${props.assetType}`;
	if (props.isUpdatingName) return `Update ${props.assetType} name`;
	return `Save as a new ${props.assetType}`;
});

async function saveAs() {
	let response: any = null;

	switch (props.assetType) {
		case AssetType.Model:
			response = await createModel(newAsset);
			break;
		case AssetType.Workflow:
			response = await createWorkflow(newAsset);
			break;
		default:
			break;
	}

	if (!response?.id) return;

	const projectId = projectResource.activeProject.value?.id;
	await projectResource.addAsset(props.assetType, response.id, projectId);

	// After saving notify the user and do any necessary actions
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

function save() {
	switch (props.assetType) {
		case AssetType.Model:
			(newAsset as Model).header.name = newName.value;
			break;
		case AssetType.Workflow:
			(newAsset as Workflow).name = newName.value;
			break;
		default:
			break;
	}

	if (props.isUpdatingName) {
		updateName();
	} else {
		saveAs();
	}

	emit('close-modal');
}

function cancel() {
	newName.value = '';
	emit('close-modal');
}

onMounted(() => {
	// If an asset is passed, clone it for saving
	if (props.asset) {
		newAsset = cloneDeep(props.asset);
		return;
	}

	console.log(0);

	// Prepares an empty version of the asset if there no asset passed
	switch (props.assetType) {
		case AssetType.Model:
			newAsset = newAMR() as Model;
			break;
		case AssetType.Workflow:
			if (isEmpty(newName.value)) {
				const workflows = useProjects().getActiveProjectAssets(AssetType.Workflow);
				newName.value = `workflow ${workflows.length + 1}`;
			}
			newAsset = emptyWorkflow();
			break;
		default:
			break;
	}
});
</script>

<style scoped>
.save-as-dialog:deep(section) {
	width: 40rem;
}

form {
	margin-top: var(--gap);
}
</style>
