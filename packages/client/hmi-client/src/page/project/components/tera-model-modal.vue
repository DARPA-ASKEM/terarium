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
import { logger } from '@/utils/logger';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import { addNewModelToProject } from '@/services/model';
import { useProjects } from '@/composables/project';

defineProps<{
	isVisible: boolean;
}>();
const emit = defineEmits(['close-modal']);

// New Model Modal
const newModelName = ref<string>('');
const isValidName = ref<boolean>(true);
const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));

const existingModelNames = computed(() => {
	const modelNames: string[] = [];
	useProjects().activeProject.value?.assets?.models.forEach((item) => {
		modelNames.push(item.header.name);
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
	const modelId = await addNewModelToProject(newModelName.value.trim());
	let newAsset;
	if (modelId) {
		newAsset = await useProjects().addAsset(AssetType.Models, modelId);
	}
	if (newAsset) {
		router.push({
			name: RouteName.Project,
			params: {
				pageType: AssetType.Models,
				assetId: modelId
			}
		});
	}
	emit('close-modal');
}
</script>

<style scoped></style>
