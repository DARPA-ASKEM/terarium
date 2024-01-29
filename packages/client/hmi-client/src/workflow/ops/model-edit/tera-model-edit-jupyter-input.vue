<template>
	<span>
		<InputText
			class="input"
			ref="inputElement"
			v-model="queryString"
			type="text"
			:disabled="kernelStatus === KernelState.busy"
			:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
			@keydown.enter="submitQuery"
		></InputText>
		<i v-if="kernelStatus === KernelState.busy" class="pi pi-spin pi-spinner kernel-status" />
		<i v-else class="pi pi-send" />
	</span>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';
import { ref, onUnmounted, onMounted } from 'vue';
import { KernelState, KernelSessionManager } from '@/services/jupyter';
import { Model } from '@/types/Types';

const props = defineProps<{
	amr: Model | null;
}>();

const emit = defineEmits(['append-output']);

const queryString = ref('');
const kernelStatus = ref<string>('');

const manager = new KernelSessionManager();
// const jupyterSession = ref();

const submitQuery = async () => {
	console.log('Submit Query:');
	console.log(manager);
	console.log(queryString.value);
	const message = manager.sendMessage('llm_request', {
		request: queryString.value
	});
	console.log('Message:');
	console.log(message);
	message
		.register('llm_response', (data) => {
			console.log('llm_response', data);
			emit('append-output', { value: data });
		})
		.register('model_preview', (data) => {
			console.log('model_preview', data);
		});
};

onMounted(async () => {
	const context = {
		context: 'mira_model_edit',
		language: 'python3',
		context_info: {
			id: props.amr?.id
		}
	};

	await manager.init('beaker_kernel', 'Beaker Kernel', context);
	console.log('Done init');
});

onUnmounted(() => {
	manager.shutdown();
});
</script>

<style scoped></style>
