<template>
	<tera-modal
		v-if="isVisible"
		class="w-4"
		@modal-mask-clicked="closeModal"
		@on-modal-open="newName = props.initialName ?? ''"
		@modal-enter-press="save"
	>
		<template #header>
			<h4>Save simulation assets</h4>
		</template>
		<template #default>
			<form @submit.prevent>
				<label for="new-name">What would you like to call it?</label>
				<tera-input-text id="new-name" v-model="newName" placeholder="Enter a unique name" />
			</form>
		</template>
		<template #footer>
			<Button label="Save" size="large" @click="save" />
			<Button label="Close" class="p-button-secondary" size="large" outlined @click="closeModal" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { SimulationType } from '@/types/Types';
import { ref } from 'vue';
import { useProjects } from '@/composables/project';
import { createSimulationAssets } from '@/services/models/simulation-service';

const props = defineProps<{
	initialName?: string;
	isVisible: boolean;
	simulationOptions: { id: string; type: SimulationType };
	assetId?: string;
}>();

const newName = ref('');

const emit = defineEmits(['close-modal', 'on-save']);

function save() {
	createSimulationAssets(
		useProjects().activeProjectId.value,
		props.simulationOptions.id,
		props.simulationOptions.type,
		newName.value,
		props.assetId
	).then((data) => {
		useProjects().refresh();
		closeModal();
		emit('on-save', data);
	});
}

function closeModal() {
	newName.value = '';
	emit('close-modal');
}
</script>
