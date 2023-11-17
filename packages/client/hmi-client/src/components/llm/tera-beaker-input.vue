<template>
	<div class="tera-beaker-container" ref="containerElement">
		<div class="chat-input-container">
			<span class="p-input-icon-right">
				<div>
					<span>
						<InputText
							:style="{ width: fixedDivWidth + 'px' }"
							class="input"
							ref="inputElement"
							v-model="queryString"
							type="text"
							:disabled="props.kernelIsBusy"
							:placeholder="props.kernelIsBusy ? 'Please wait...' : 'What do you want to do?'"
							@keydown.enter="submitQuery"
						></InputText>
						<i class="pi pi-spin pi-spinner kernel-status" v-if="props.kernelIsBusy" />
						<i class="pi pi-send kernel-status" v-else />
					</span>
					<Button
						ref="addCodeCellButton"
						class="p-button p-button-secondary p-button-sm"
						title="Add a code cell to the notebook"
						@click="addCodeCell"
					>
						<span class="pi pi-plus-circle p-button-icon p-button-icon-left"></span>
						<span class="p-button-text">Add Code Cell</span>
					</Button>
				</div>
			</span>
			<ProgressBar v-if="props.kernelIsBusy" mode="indeterminate" style="height: 3px"></ProgressBar>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';
import InputText from 'primevue/inputtext';
import * as EventService from '@/services/event';
import { EventType } from '@/types/Types';
import { useProjects } from '@/composables/project';

const emit = defineEmits(['submit-query', 'add-code-cell']);

const props = defineProps({
	placeholderMessage: {
		type: String,
		required: false,
		default: () => ''
	},
	focusInput: {
		type: Boolean,
		default: false
	},
	styleResults: {
		type: Boolean,
		default: false
	},
	placeholderColor: {
		type: String,
		default: 'black'
	},
	kernelIsBusy: Boolean
});

const queryString = ref('');

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);
const addCodeCellButton = ref<HTMLElement | null>(null);
const fixedDivWidth = ref(0);

const submitQuery = () => {
	EventService.create(
		EventType.TransformPrompt,
		useProjects().activeProject.value?.id,
		queryString.value
	);
	emit('submit-query', queryString.value);
	queryString.value = '';
};

const addCodeCell = () => {
	emit('add-code-cell');
	EventService.create(EventType.AddCodeCell, useProjects().activeProject.value?.id);
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}

	const updateWidth = () => {
		if (containerElement.value && addCodeCellButton.value) {
			fixedDivWidth.value =
				containerElement.value.offsetWidth - addCodeCellButton.value.offsetWidth - 25;
		}
	};

	updateWidth(); // Update once on mount
	window.addEventListener('resize', updateWidth); // Update on window resize

	onBeforeUnmount(() => {
		window.removeEventListener('resize', updateWidth); // Clean up listener
	});
});
</script>

<style scoped>
::placeholder {
	color: var(--gray-700);
}
.tera-beaker-container {
	display: relative;
	padding-top: 5px;
	padding-bottom: 35px;
}

.chat-input-container {
	position: fixed;
	bottom: 50px;
	height: fit-content;
	z-index: 30000;
}

.input {
	color: black;
	background-color: var(--gray-300);
}

.kernel-status {
	position: relative;
	right: 2rem;
}

.input:disabled {
	color: black;
	opacity: 100;
	background-color: var(--gray-300);
}
</style>
