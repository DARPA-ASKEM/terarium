<template>
	<Teleport to="body">
		<tera-modal
			v-if="isVisible"
			class="save-as-dialog"
			@modal-mask-clicked="emit('close-modal')"
			@modal-enter-press="saveNewModel"
		>
			<template #header>
				<h4>{{ title }}</h4>
			</template>
			<template #default>
				<form @submit.prevent>
					<label for="new-model">What would you like to call it?</label>
					<InputText
						id="new-model"
						type="text"
						v-model="newModelName"
						placeholder="Enter a unique name for your model"
					/>
				</form>
			</template>
			<template #footer>
				<Button label="Save" size="large" @click="saveNewModel" />
				<Button
					label="Cancel"
					class="p-button-secondary"
					size="large"
					outlined
					@click="cancelNewModel"
				/>
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { ref, PropType } from 'vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import type { Model } from '@/types/Types';
import {
	createModel
	// getModel,
	// updateModel,
} from '@/services/model';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import { logger } from '@/utils/logger';

const props = defineProps({
	title: {
		type: String,
		default: 'Save as a new model'
	},
	isVisible: {
		type: Boolean,
		default: false
	},
	model: {
		type: Object as PropType<Model>,
		default: newAMR()
	},
	openOnSave: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['close-modal', 'on-save', 'update']);

const newModelName = ref<string>('');

// TODO: Consider letting the user just know if what they are typing currently is a duplicate (do not prevent them from saving)
// const isValidName = ref<boolean>(true);
// const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));	v-bind:class="invalidInputStyle"

async function saveNewModel() {
	const newModel = cloneDeep(props.model);
	newModel.header.name = newModelName.value.trim();

	const projectResource = useProjects();
	const modelData = await createModel(newModel);
	if (!modelData) return;

	const projectId = projectResource.activeProject.value?.id;
	await projectResource.addAsset(AssetType.Model, modelData.id, projectId);

	logger.info(
		`${modelData.name} saved successfully in project ${projectResource.activeProject.value?.name}.`
	);

	emit('on-save', modelData);
	if (props.openOnSave) {
		router.push({
			name: RouteName.Project,
			params: {
				pageType: AssetType.Model,
				assetId: modelData.id
			}
		});
	}
	emit('close-modal');
}

function cancelNewModel() {
	newModelName.value = '';
	emit('close-modal');
}
// async function updateModelName() {
// 	if (!validateModelName(newModelName.value) || !props.modelId) return;

// 	// 1. Update model name
// 	const model = await getModel(props.modelId);
// 	if (!model) return;
// 	model.header.name = newModelName.value;
// 	const updateResponse = await updateModel(model);
// 	if (!updateResponse) return;

// 	// 2. Save asset to project
// 	const projectId = useProjects().activeProject.value?.id;
// 	if (!projectId) return;
// 	const response = await addAsset(projectId, AssetType.Model, props.modelId);
// 	await useProjects().refresh();

// 	if (!response) {
// 		return;
// 	}

// 	useToastService().success('', 'Model saved successfully');
// 	emit('update', newModelName.value);
// 	emit('close-modal');
// }

// async function createOrUpdateModel() {
// 	if (props.modelId) {
// 		updateModelName();
// 	} else {
// 		createNewModel();
// 	}
// }
</script>

<style scoped>
.save-as-dialog:deep(section) {
	width: 40rem;
}

form {
	margin-top: var(--gap);
}
</style>
