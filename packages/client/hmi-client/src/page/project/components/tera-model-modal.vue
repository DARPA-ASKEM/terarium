<template>
	<Teleport to="body">
		<tera-modal v-if="isVisible" class="modal" @modal-mask-clicked="emit('close-modal')">
			<template #header>
				<h4>New model</h4>
			</template>
			<template #default>
				<form id="createModelForm" @onSubmit.prevent="createNewModel()">
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
				<Button type="submit" form="createModelForm" @click="createNewModel">Create model</Button>
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
import { IProject } from '@/types/Project';
import { logger } from '@/utils/logger';
import { addNewModelToProject } from '@/services/model';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';

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

async function createNewModel() {
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
	const modelId = await addNewModelToProject(newModelName.value.trim(), props.project);
	if (modelId) {
		router.push({
			name: RouteName.ProjectRoute,
			params: {
				pageType: AssetType.Models,
				assetId: modelId
			}
		});
	}
	emit('close-modal');
}
</script>

<style></style>
