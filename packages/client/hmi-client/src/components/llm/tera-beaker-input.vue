<template>
	<div class="tera-beaker-container" ref="containerElement">
		<div class="chat-input-container">
			<div class="chat-input-elements">
				<textarea
					v-if="hideAutoComplete || props.defaultOptions.length === 0"
					class="input"
					ref="inputElement"
					v-model="queryString"
					type="text"
					:disabled="props.kernelIsBusy"
					:placeholder="props.kernelIsBusy ? 'Please wait...' : 'What do you want to do?'"
					@keydown.enter="submitQuery"
					@input="autoGrow"
					rows="1"
				/>
				<AutoComplete
					v-else
					ref="autoComplete"
					v-model="queryString"
					class="auto-complete"
					:suggestions="filteredOptions"
					:completeOnFocus="true"
					@complete="searchOptions"
					@change="onInputChange"
					placeholder="What do you want to do?"
					emptySearchMessage="No suggestions"
					:disabled="props.kernelIsBusy"
				/>
				<Button
					v-if="queryString.length !== 0"
					text
					:icon="props.kernelIsBusy ? 'pi pi-spin pi-spinner' : 'pi pi-send'"
					rounded
					class="kernel-status"
					@click="submitQuery"
				></Button>
				<span v-else style="width: 2.25rem"></span>

				<Button
					ref="addCodeCellButton"
					severity="secondary"
					size="small"
					outlined
					:disabled="props.kernelIsBusy"
					class="white-space-nowrap action-button"
					title="Add a code cell to the notebook"
					@click="addCodeCell"
				>
					<span class="pi pi-plus p-button-icon p-button-icon-left"></span>
					<span class="p-button-text">Add a cell</span>
				</Button>
				<Button
					ref="rerunCellsButton"
					severity="secondary"
					size="small"
					outlined
					:disabled="props.kernelIsBusy"
					class="white-space-nowrap action-button"
					style="width: 13rem"
					title="Rerun all cells"
					@click="runAllCells"
				>
					<span class="pi pi-refresh p-button-icon p-button-icon-left"></span>
					<span class="p-button-text">Rerun all cells</span>
				</Button>
				<ProgressBar v-if="props.kernelIsBusy" mode="indeterminate" class="busy-bar"></ProgressBar>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';
import Button from 'primevue/button';
import * as EventService from '@/services/event';
import { EventType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import AutoComplete from 'primevue/autocomplete';

const emit = defineEmits(['submit-query', 'add-code-cell', 'run-all-cells']);

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
	kernelIsBusy: Boolean,
	defaultOptions: {
		type: Array<String>,
		default: () => []
	},
	maxChars: {
		type: Number,
		default: 75
	}
});

const queryString = ref('');

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);
const addCodeCellButton = ref<HTMLElement | null>(null);
const fixedDivWidth = ref(0);
const hideAutoComplete = ref(false);
const filteredOptions = ref<any[]>([]);
const autoComplete = ref();

const submitQuery = () => {
	EventService.create(EventType.TransformPrompt, useProjects().activeProject.value?.id, queryString.value);
	emit('submit-query', queryString.value);
	queryString.value = '';
	setTimeout(() => {
		autoGrow(); // Reset height after clearing
	}, 50);
};

const addCodeCell = () => {
	emit('add-code-cell');
	EventService.create(EventType.AddCodeCell);
};

const runAllCells = () => {
	emit('run-all-cells');
};

const searchOptions = () => {
	const query = queryString.value.toLowerCase();
	filteredOptions.value = props.defaultOptions.filter((option) => option.toLowerCase().includes(query));

	// Hide the panel if there are no suggestions
	if (filteredOptions.value.length === 0) {
		autoComplete.value?.hide();
	}
};

const onInputChange = async () => {
	// If there are no search suggestions to show, the auto complete element is never shown
	if (props.defaultOptions.length === 0) return;

	// If the number of characters entered exceeds the maximum, switch from auto complete to text area
	// Force focus for a smoother transition
	const numChars = queryString.value.length;
	if (numChars <= props.maxChars && numChars >= 0) {
		hideAutoComplete.value = false;
		await nextTick();
		const inputEl = autoComplete.value?.$el.querySelector('input');
		inputEl.focus();
	} else {
		hideAutoComplete.value = true;
		await nextTick();
		inputElement.value?.focus();
		autoGrow();
	}
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

const autoGrow = () => {
	if (inputElement.value) {
		inputElement.value.style.height = 'auto'; // Reset height to recalculate
		inputElement.value.style.height = `${inputElement.value.scrollHeight}px`;
	}
};
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
	display: flex;
	flex-direction: row;
	width: 50rem;
}

.chat-input-elements {
	display: flex;
	flex-direction: row;
	align-items: top;
	justify-content: space-between;
	width: 100%;
}
.input {
	color: var(--text-color);
	background-color: var(--surface-0);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
	padding: var(--gap-2);
	padding-left: 3rem;
	padding-right: 3rem;
	padding-top: 5px;
	line-height: 1.5;
	min-height: 2.5rem;
	width: 100%;
	resize: none;
	overflow: hidden;
	box-sizing: border-box;
	font-family: var(--font-family);
	/* Add ai-assistant icon */
	background-image: url('@assets/svg/icons/message.svg');
	background-repeat: no-repeat;
	background-position: var(--gap-4) var(--gap-2);
}

.kernel-status {
	position: relative;
	right: 2.5rem;
	margin-top: 3px;
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
.action-button {
	margin-left: var(--gap-2);
	margin-top: 1px;
	height: 31px;
	width: 10rem;
}
.auto-complete {
	width: 100%;
	position: relative;
}

.auto-complete:deep(input),
.text-area {
	width: 100%;
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: var(--gap-2-5) 9px;
	background-repeat: no-repeat;
	padding-right: 2rem;
	padding-left: 2.25rem;
}
</style>
