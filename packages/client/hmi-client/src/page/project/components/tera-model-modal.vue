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
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import { addNewModelToProject, validateModelName } from '@/services/model';
import { useProjects } from '@/composables/project';

defineProps<{
	isVisible: boolean;
}>();
const emit = defineEmits(['close-modal']);

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
</script>

<style scoped></style>
