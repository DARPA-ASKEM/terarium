<template>
	<!-- AI assistant -->
	<div v-if="showAssistant" class="ai-assistant">
		<Textarea
			v-if="hideAutoComplete || props.defaultOptions.length === 0"
			ref="textArea"
			class="text-area"
			v-model="questionString"
			@input="onInputChange"
			rows="1"
			autoResize
			:disabled="kernelStatus === KernelState.busy"
			placeholder="What do you want to do?"
		/>
		<AutoComplete
			v-else
			ref="autoComplete"
			v-model="questionString"
			class="auto-complete"
			:suggestions="filteredOptions"
			:completeOnFocus="true"
			@complete="searchOptions"
			@change="onInputChange"
			placeholder="What do you want to do?"
			emptySearchMessage="No suggestions"
			:showEmptyMessage="false"
			:disabled="kernelStatus === KernelState.busy"
		/>

		<Button
			v-if="questionString.length > 0"
			class="submit-button"
			severity="secondary"
			:icon="kernelStatus === KernelState.busy ? 'pi pi-spin pi-spinner' : 'pi pi-send'"
			@click="submitQuestion"
		/>
	</div>

	<tera-notebook-jupyter-thought-output :llm-thoughts="llmThoughts" :llm-query="llmQuery" />

	<!-- Toolbar -->
	<div class="notebook-toolbar">
		<div class="toolbar-left-side">
			<Dropdown :disabled="true" :model-value="contextLanguage" :options="contextLanguageOptions" />
			<!---<div class="flex gap-1 mr-2">
				<InputSwitch v-model="showAssistant" class="mr-1" />
				<img src="@assets/svg/icons/magic.svg" alt="Magic icon" />
				<span>AI assistant</span>
			</div>-->
		</div>
		<div class="toolbar-right-side">
			<slot name="toolbar-right-side" />
		</div>
	</div>
</template>

<script setup lang="ts">
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';
import Button from 'primevue/button';
import { ref, nextTick } from 'vue';
import { KernelState, KernelSessionManager } from '@/services/jupyter';
import Dropdown from 'primevue/dropdown';
import AutoComplete from 'primevue/autocomplete';
import Textarea from 'primevue/textarea';

const props = withDefaults(
	defineProps<{
		kernelManager: KernelSessionManager;
		defaultOptions?: string[];
		contextLanguage: string;
		maxChars?: number;
	}>(),
	{
		defaultOptions: () => [],
		maxChars: 75
	}
);

const emit = defineEmits(['question-asked', 'llm-output', 'llm-thought-output']);

const questionString = ref('');
const kernelStatus = ref<string>('');
const showAssistant = ref(true);
const thoughts = ref();
const llmThoughts = ref<any[]>([]);
const llmQuery = ref<string>('');
const filteredOptions = ref<any[]>([]);
const autoComplete = ref();
const textArea = ref();
const hideAutoComplete = ref(false);

// FIXME: If the language is changed here it should mutate the beaker instance in the parent component

const contextLanguageOptions = ref<string[]>(['python3', 'julia-1.10']);

const addQuestionToCode = (question, code) => `\n# Prompt: ${question}\n\n${code}\n# End: =================`;

const submitQuestion = () => {
	const message = props.kernelManager.sendMessage('llm_request', {
		request: questionString.value
	});
	llmThoughts.value = [];
	emit('question-asked', questionString.value);

	// May prefer to use a manual status rather than following this. TBD. Both options work for now
	message.register('status', (data) => {
		kernelStatus.value = data.content.execution_state;
	});
	message.register('code_cell', (data) => {
		data.content.code = addQuestionToCode(questionString.value, data.content.code);
		emit('llm-output', data);
	});
	message.register('llm_thought', (data) => {
		thoughts.value = data;
		llmThoughts.value.push(data);
		llmQuery.value = questionString.value;
		emit('llm-thought-output', data);
	});
	message.register('llm_response', (data) => {
		// Check if our llm_response is providing a response text to a user's question
		if (data.content.name === 'response_text') {
			llmThoughts.value = [];
			llmThoughts.value.push(data);
		}
		thoughts.value = data;
		emit('llm-thought-output', data);
	});
};

const searchOptions = () => {
	const query = questionString.value.toLowerCase();
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
	const numChars = questionString.value.length;
	if (numChars <= props.maxChars && numChars >= 0) {
		hideAutoComplete.value = false;
		await nextTick();
		const inputEl = autoComplete.value?.$el.querySelector('input');
		inputEl.focus();
	} else {
		hideAutoComplete.value = true;
		await nextTick();
		textArea.value?.$el.focus();
	}
};
</script>

<style scoped>
.ai-assistant {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
}

.notebook-toolbar {
	display: flex;
	flex-direction: row;
	gap: var(--gap-3);
	justify-content: space-between;
}

.toolbar-left-side,
.toolbar-right-side {
	display: flex;
	gap: var(--gap-2);
	align-items: center;
}

.auto-complete {
	width: 100%;
}

.auto-complete:deep(input),
.text-area {
	width: 100%;
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: var(--gap-2) 9px;
	background-repeat: no-repeat;
	padding-right: 2rem;
	padding-left: 2rem;
}

.submit-button {
	box-shadow: none;
	position: absolute;
	background: none;
	right: 0;
	top: 1px;
}
</style>
