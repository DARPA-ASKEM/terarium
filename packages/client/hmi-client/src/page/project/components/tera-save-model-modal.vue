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
				<Button label="Save" size="large" @click="save" />
				<Button label="Cancel" class="p-button-secondary" size="large" outlined @click="cancel" />
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
	createModel,
	// getModel,
	updateModel
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
	},
	isUpdatingName: {
		type: Boolean,
		default: false
	}
});

// on-name-update, on-save-as
const emit = defineEmits(['close-modal', 'on-save', 'update']);

const newModelName = ref<string>('');

const projectResource = useProjects();

// TODO: Consider letting the user just know if what they are typing currently is a duplicate (do not prevent them from saving)
// const isValidName = ref<boolean>(true);
// const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));	v-bind:class="invalidInputStyle"

async function saveAs() {
	const newModel = cloneDeep(props.model);
	newModel.header.name = newModelName.value.trim();

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

async function updateName() {
	const modelToUpdate = cloneDeep(props.model);
	modelToUpdate.header.name = newModelName.value.trim();

	const response = await updateModel(modelToUpdate);
	if (!response) return;

	await projectResource.refresh();

	logger.info(`Updated model name to ${newModelName.value}.`);
	emit('update', newModelName.value);
	emit('close-modal');
}

function save() {
	if (props.isUpdatingName) {
		updateName();
	} else {
		saveAs();
	}
}

function cancel() {
	newModelName.value = '';
	emit('close-modal');
}
</script>

<style scoped>
.save-as-dialog:deep(section) {
	width: 40rem;
}

form {
	margin-top: var(--gap);
}
</style>
