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
// createMessageId, JupyterMessage, newSession
// import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
// import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
// import { createMessage } from '@jupyterlab/services/lib/kernel/messages';

const props = defineProps<{
	amr: Model | null;
}>();

const emit = defineEmits(['append-output']);

const queryString = ref('');
const kernelStatus = ref<string>('');

const manager = new KernelSessionManager();
// const jupyterSession = ref();

const submitQuery = async () => {
	const message = manager.sendMessage('llm_request', {
		request: queryString.value
	});
	message
		.register('llm_response', (data) => {
			console.log('llm_response', data);
			emit('append-output', { value: data });
		})
		.register('model_preview', (data) => {
			console.log('model_preview', data);
		});
};

// const updateKernelStatus = (statusString: string) => {
// 	kernelStatus.value = statusString;
// };

// const iopubMessageHandler = (_session, message) => {
// 	console.log(message);
// 	if (message.header.msg_type === 'status') {
// 		console.log("msg type = status");
// 		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
// 		updateKernelStatus(KernelState[newState]);
// 		return;
// 	}
// 	console.log("Else in handler");
// 	// newJupyterMessage(message);
// };

// const submitQuery = async () => {
// 	console.log("Submit query:");
// 	// Should have this on mount and use Suspense
// 	// const jupyterSession: SessionContext = await newSession('beaker_kernel', 'Beaker Kernel');
// 	console.log(jupyterSession);
// 	console.log(queryString.value);
// 	if (jupyterSession && queryString.value !== undefined) {
// 		console.log("Within If");

// 		const kernel = jupyterSession.session?.kernel as IKernelConnection;

// 		if (kernel === undefined || kernel === null) {
// 			console.log("No kernel, returning early:");
// 			return;
// 		}
// 		updateKernelStatus(KernelState.busy);
// 		const msgId = createMessageId('mira_model_edit');
// 		const message: JupyterMessage = createMessage({
// 			session: jupyterSession.session?.name || '',
// 			channel: 'shell',
// 			content: { context: context, request: queryString.value },
// 			msgType: 'llm_request',
// 			msgId
// 		});
// 		kernel.sendJupyterMessage(message);
// 		jupyterSession.iopubMessage.connect(iopubMessageHandler);
// 		console.log(kernel);
// 		console.log(jupyterSession);
// 		// newJupyterMessage(message);
// 		// isExecutingCode.value = true;
// 		queryString.value = '';
// 	}
// };

onMounted(async () => {
	const context = {
		context: 'mira_model_edit',
		language: 'python3',
		context_info: {
			id: props.amr
		}
	};

	// jupyterSession.value = await newSession('beaker_kernel', 'Beaker Kernel');
	await manager.init('beaker_kernel', 'Beaker Kernel', context);
});

onUnmounted(() => {
	// jupyterSession.value.shutdown();
	manager.shutdown();
});
</script>

<style scoped></style>
