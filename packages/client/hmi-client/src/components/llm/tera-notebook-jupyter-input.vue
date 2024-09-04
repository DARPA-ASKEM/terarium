<template>
	<!-- AI assistant -->
	<div v-if="showAssistant" class="ai-assistant">
		<!---<Dropdown
			v-if="defaultOptions"
			:editable="true"
			class="input"
			ref="inputElement"
			v-model="questionString"
			:options="props.defaultOptions"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuestion"
		/>
		<tera-input-text
			v-else
			class="input"
			ref="inputElement"
			v-model="questionString"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuestion"
		/>
		<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<Button v-else severity="secondary" icon="pi pi-send" @click="submitQuestion" />-->
		<AutoComplete
			v-model="questionString"
			class="auto-complete"
			:suggestions="filteredOptions"
			completeOnFocus="true"
			@complete="searchOptions"
			placeholder="What do you want to do?"
		/>
		<!--<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<Button  v-if="questionString !== ''" severity="secondary" icon="pi pi-send" @click="submitQuestion" />-->
		<!-- v-if="questionString !== ''"-->
		<Button
			v-if="questionString.length > 0"
			class="submit-button"
			severity="secondary"
			:icon="kernelStatus === KernelState.busy ? 'pi pi-spin pi-spinner' : 'pi pi-send'"
			@click="submitQuestion"
		/>
	</div>

	<tera-notebook-jupyter-thought-output :llm-thoughts="llmThoughts" :llm-query="questionString" />

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
import { ref } from 'vue';
import { KernelState, KernelSessionManager } from '@/services/jupyter';
import Dropdown from 'primevue/dropdown';
import AutoComplete from 'primevue/autocomplete';

const props = defineProps<{
	kernelManager: KernelSessionManager;
	defaultOptions?: string[];
	contextLanguage: string;
}>();

const emit = defineEmits(['question-asked', 'llm-output', 'llm-thought-output']);

const questionString = ref('');
const kernelStatus = ref<string>('');
const showAssistant = ref(true);
const thoughts = ref();
const llmThoughts = ref([]);
const filteredOptions = ref([]);

// FIXME: If the language is changed here it should mutate the beaker instance in the parent component

const contextLanguageOptions = ref<string[]>(['python3', 'julia-1.10']);

const submitQuestion = () => {
	const message = props.kernelManager.sendMessage('llm_request', {
		request: questionString.value
	});
	emit('question-asked', questionString.value);

	// May prefer to use a manual status rather than following this. TBD. Both options work for now
	message.register('status', (data) => {
		kernelStatus.value = data.content.execution_state;
	});
	message.register('code_cell', (data) => {
		emit('llm-output', data);
	});
	message.register('llm_thought', (data) => {
		thoughts.value = data;
		llmThoughts.value = []; // FIXME: When should this get reset?
		llmThoughts.value.push(data);
		emit('llm-thought-output', data);
	});
	message.register('llm_response', (data) => {
		thoughts.value = data;
		emit('llm-thought-output', data);
	});
};

const searchOptions = () => {
	const query = questionString.value.toLowerCase();
	filteredOptions.value = props.defaultOptions.filter((option) => option.toLowerCase().includes(query));
};
</script>

<style scoped>
.ai-assistant {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.notebook-toolbar {
	display: flex;
	flex-direction: row;
	margin-top: var(--gap-small);
	gap: var(--gap-3);
	justify-content: space-between;
}

.toolbar-left-side,
.toolbar-right-side {
	display: flex;
	gap: var(--gap-small);
	align-items: center;
}

.input {
	width: 100%;
	padding: var(--gap-xsmall);
}

.input:deep(input) {
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: var(--gap-small);
	background-repeat: no-repeat;
	text-indent: 24px;
}

.auto-complete {
	width: 100%;
}

.auto-complete:deep(input) {
	width: 100%;
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: var(--gap-small);
	background-repeat: no-repeat;
	text-indent: 24px;
}

.submit-button {
	box-shadow: none;
	position: absolute;
	background: none;
	right: 0;
}
</style>
