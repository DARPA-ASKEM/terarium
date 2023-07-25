<template>
	<Teleport to="body">
		<tera-modal v-if="isVisible" class="modal" @modal-mask-clicked="emit('close-modal')">
			<template #header>
				<h4>New model</h4>
			</template>
			<template #default>
				<form>
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
				<Button @click="createNewModel">Create model</Button>
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
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import { createModel } from '@/services/model';
import router from '@/router';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';

const props = defineProps<{
	project: IProject;
	isVisible: boolean;
}>();
const emit = defineEmits(['close-modal']);

// New Model Modal
const newModelName = ref<string>('');
const isValidName = ref<boolean>(true);
const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));

const existingModelNames = computed(() => {
	const modelNames: string[] = [];
	props.project.assets?.models.forEach((item) => {
		modelNames.push(item.name);
	});
	return modelNames;
});

function createNewModel() {
	if (newModelName.value.trim().length === 0) {
		isValidName.value = false;
		logger.info('Model name cannot be empty - please enter a different name');
		return;
	}
	if (existingModelNames.value.includes(newModelName.value.trim())) {
		isValidName.value = false;
		logger.info('Duplicate model name - please enter a different name');
		return;
	}
	isValidName.value = true;
	newModel(newModelName.value.trim());
	emit('close-modal');
}

const newModel = async (modelName: string) => {
	// 1. Load an empty AMR
	const amr = newAMR(modelName);
	(amr as any).id = undefined; // FIXME: id hack

	const response = await createModel(amr);
	const modelId = response?.id;

	// 2. Add the model to the project
	if (modelId) {
		await ProjectService.addAsset(props.project.id, ProjectAssetTypes.MODELS, modelId);
		// 3. Reroute
		router.push({
			name: RouteName.ProjectRoute,
			params: {
				assetName: 'Model',
				pageType: ProjectAssetTypes.MODELS,
				assetId: modelId
			}
		});
	}
};
</script>

<style>
.modal main {
	width: 50rem;
}
</style>
