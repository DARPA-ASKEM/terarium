<template>
	<div class="container">
		<!-- <i class="pi pi-magic" /> -->
		<Dropdown
			v-if="defaultOptions"
			:editable="true"
			class="input"
			ref="inputElement"
			v-model="queryString"
			:options="props.defaultOptions"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuery"
		/>
		<InputText
			v-else
			class="input"
			ref="inputElement"
			v-model="queryString"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuery"
		/>
		<Dropdown :disabled="true" :model-value="contextLanguage" :options="contextLanguageOptions" />
		<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<Button v-else icon="pi pi-send" @click="submitQuery" />
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

const queryString = ref('');
const kernelStatus = ref<string>('');

// FIXME: If the language is changed here it should mutate the beaker instance in the parent component
const contextLanguage = ref<string>('python3');
const contextLanguageOptions = ref<string[]>(['python3']);

const submitQuery = () => {
	const message = props.kernelManager.sendMessage('llm_request', {
		request: queryString.value
	});
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
	width: 100%;
	gap: 0.5rem;
}
.input {
	flex: 1;
}
.p-dropdown {
	width: 8rem;
}
.p-button {
	background-color: var(--surface-200);
}
</style>
