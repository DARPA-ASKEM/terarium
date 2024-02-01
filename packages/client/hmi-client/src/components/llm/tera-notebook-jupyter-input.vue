<template>
	<div>
		<!-- <i class="pi pi-magic" /> -->
		<InputText
			class="input"
			ref="inputElement"
			v-model="queryString"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuery"
		></InputText>
		<Dropdown :disabled="true" :model-value="contextLanguage" :options="contextLanguageOptions" />
		<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<i v-else class="pi pi-send" />
	</div>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';
import { ref, onUnmounted, onMounted } from 'vue';
import { KernelState, KernelSessionManager } from '@/services/jupyter';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	context: string;
	contextInfo: any;
}>();

const emit = defineEmits(['append-output']);

const queryString = ref('');
const kernelStatus = ref<string>('');
const contextLanguage = ref<string>('python3');
const contextLanguageOptions = ref<string[]>(['python3']);

const manager = new KernelSessionManager();

const submitQuery = async () => {
	const message = manager.sendMessage('llm_request', {
		request: queryString.value
	});
	message.register('code_cell', (data) => {
		emit('append-output', { value: data });
	});
};

onMounted(async () => {
	const context = {
		context: props.context,
		language: contextLanguage.value,
		context_info: props.contextInfo
	};

	await manager.init('beaker_kernel', 'Beaker Kernel', context);
});

onUnmounted(() => {
	manager.shutdown();
});
</script>

<style scoped></style>
