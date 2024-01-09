<template>
	<Teleport to="body">
		<tera-modal
			v-if="isVisible"
			@modal-mask-clicked="emit('close-modal')"
			@modal-enter-press="createNewModel"
		>
			<template #header>
				<h4>New model</h4>
			</template>
			<template #default>
				<form @submit.prevent>
					<label for="new-model">Enter a unique name for your model</label>
					<InputText
						v-bind:class="invalidInputStyle"
						id="new-model"
						type="text"
						v-model="newModelName"
						placeholder="new model"
					/>
				</form>
			</template>
			<template #footer>
				<Button @click="createOrUpdateModel">Create model</Button>
				<Button class="p-button-secondary" @click="emit('close-modal')"> Cancel </Button>
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import { addNewModelToProject, getModel, updateModel, validateModelName } from '@/services/model';
import { useProjects } from '@/composables/project';
import { addAsset } from '@/services/project';
import { logger } from '@/utils/logger';
import { useToastService } from '@/services/toast';

const props = defineProps<{
	isVisible: boolean;
	modelId?: string;
}>();
const emit = defineEmits(['close-modal', 'update']);

// New Model Modal
const newModelName = ref<string>('');
const isValidName = ref<boolean>(true);
const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));

async function createNewModel() {
	isValidName.value = validateModelName(newModelName.value);
	if (!isValidName.value) return;
	const modelId = await addNewModelToProject(newModelName.value.trim());
	let newAsset;
	if (modelId) {
		newAsset = await useProjects().addAsset(AssetType.Model, modelId);
	}
	if (newAsset) {
		router.push({
			name: RouteName.Project,
			params: {
				pageType: AssetType.Model,
				assetId: modelId
			}
		});
	}
	emit('close-modal');
}

async function updateModelName() {
	if (!validateModelName(newModelName.value) || !props.modelId) return;

	// 1. Update model name
	const model = await getModel(props.modelId);
	if (!model) return;
	model.header.name = newModelName.value;
	const updateResponse = await updateModel(model);
	if (!updateResponse) return;

	// 2. Save asset to project
	const projectId = useProjects().activeProject.value?.id;
	if (!projectId) return;
	const response = await addAsset(projectId, AssetType.Models, props.modelId);
	await useProjects().refresh();

	if (!response) {
		logger.error('Could not save asset to project');
		return;
	}

	useToastService().success('', 'Model saved successfully');
	emit('update', newModelName.value);
	emit('close-modal');
}

async function createOrUpdateModel() {
	if (props.modelId) {
		updateModelName();
	} else {
		createNewModel();
	}
}
</script>

<style scoped></style>
