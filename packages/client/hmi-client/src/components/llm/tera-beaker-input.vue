<template>
	<div class="tera-beaker-container" ref="containerElement">
		<div class="chat-input-container">
			<div class="top-row">
				<InputText
					class="input"
					ref="inputElement"
					v-model="queryString"
					type="text"
					:disabled="props.kernelIsBusy"
					:placeholder="props.kernelIsBusy ? 'Please wait...' : 'What do you want to do?'"
					@keydown.enter="submitQuery"
				/>
				<Button
					text
					:icon="props.kernelIsBusy ? 'pi pi-spin pi-spinner' : 'pi pi-send'"
					rounded
					size="large"
					class="kernel-status"
					:disabled="queryString.length === 0"
					@click="submitQuery"
				></Button>
			</div>
			<div class="bottom-row">
				<Button
					ref="addCodeCellButton"
					severity="secondary"
					outlined
					:disabled="props.kernelIsBusy"
					class="white-space-nowrap"
					title="Add a code cell to the notebook"
					@click="addCodeCell"
				>
					<span class="pi pi-plus p-button-icon p-button-icon-left"></span>
					<span class="p-button-text">Add a cell</span>
				</Button>
				<ProgressBar v-if="props.kernelIsBusy" mode="indeterminate" class="busy-bar"></ProgressBar>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
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
	EventService.create(EventType.TransformPrompt, useProjects().activeProject.value?.id, queryString.value);
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
			fixedDivWidth.value = containerElement.value.offsetWidth - addCodeCellButton.value.offsetWidth - 25;
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
	color: var(--text-color-subdued);
}
.tera-beaker-container {
	display: relative;
	padding-top: 5px;
}

.chat-input-container {
	position: fixed;
	bottom: 0px;
	height: 10rem;
	padding: var(--gap);
	padding-bottom: 1.5rem;
	z-index: 200;
	border-top: 1px solid var(--surface-border-light);
	background-color: var(--surface-transparent);
	backdrop-filter: blur(10px);
	width: calc(100% - 6 * var(--gap-small));
	display: flex;
	flex-direction: column;
	flex-wrap: nowrap;
	align-items: start;
	justify-content: space-between;
}

.top-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}
.input {
	color: var(--text-color);
	background-color: var(--surface-0);
	border: 2px solid var(--primary-color);
	padding: var(--gap);
	padding-left: 3rem;
	width: 100%;
	/* Add ai-assistant icon */
	background-image: url('@assets/svg/icons/message.svg');
	background-repeat: no-repeat;
	background-position: var(--gap) center; /* Adjust 10px according to your icon size and position */
}

.kernel-status {
	position: absolute;
	right: 2rem;
}

.input:disabled {
	color: black;
	opacity: 100;
	background-color: var(--gray-300);
}
.busy-bar {
	position: relative;
	top: -4px;
}
</style>
