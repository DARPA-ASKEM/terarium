<template>
	<div class="container">
		<!-- <i class="pi pi-magic" /> -->
		<Dropdown
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
		<InputText
			v-else
			class="input"
			ref="inputElement"
			v-model="questionString"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuestion"
		/>
		<!-- <Dropdown :disabled="true" :model-value="contextLanguage" :options="contextLanguageOptions" /> -->
		<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<Button v-else severity="secondary" icon="pi pi-send" @click="submitQuestion" />
	</div>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import { ref } from 'vue';
import { KernelState, KernelSessionManager } from '@/services/jupyter';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	kernelManager: KernelSessionManager;
	defaultOptions?: string[];
}>();

const emit = defineEmits(['llm-output']);

const questionString = ref('');
const kernelStatus = ref<string>('');

// FIXME: If the language is changed here it should mutate the beaker instance in the parent component
// const contextLanguage = ref<string>('python3');
// const contextLanguageOptions = ref<string[]>(['python3']);

const submitQuestion = () => {
	const message = props.kernelManager.sendMessage('llm_request', {
		request: questionString.value
	});
	// May prefer to use a manual status rather than following this. TBD. Both options work for now
	message.register('status', (data) => {
		kernelStatus.value = data.content.execution_state;
	});
	message.register('code_cell', (data) => {
		emit('llm-output', data);
	});
};
</script>

<style scoped>
.container {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.input {
	width: 100%;
}
.p-button {
	width: 2rem;
}
</style>
