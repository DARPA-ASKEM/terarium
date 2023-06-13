<template>
	<div class="chatty-container" ref="containerElement">
		<label for="chatt-input"></label>
		<InputText
			class="chatty"
			ref="inputElement"
			v-model="queryString"
			type="text"
			:disabled="kernelState !== KernelState.idle"
			:style="inputStyle"
			:placeholder="kernelState === KernelState.idle ? 'What do you want to do?' : 'Please wait...'"
			@keydown.enter="onEnter"
		/>
		<ProgressBar
			v-if="kernelState !== KernelState.idle"
			mode="indeterminate"
			style="height: 3px"
		></ProgressBar>
		<!-- <div>
			<i v-if="kernelState !== KernelState.idle" class="pi pi-spin pi-spinner" />
		</div> -->
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';
import { SessionContext } from '@jupyterlab/apputils';
import { JupyterMessage } from 'src/services/jupyter';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import InputText from 'primevue/inputtext';

const emit = defineEmits(['newCodeCell', 'newMessage']);

const props = defineProps({
	llmContext: {
		type: SessionContext,
		required: true,
		default: null
	},
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
	context: {
		type: String,
		default: null
	},
	context_info: {
		type: Object,
		default: () => {}
	}
});

props.llmContext.kernelChanged.connect((_context, kernelInfo) => {
	const kernel = kernelInfo.newValue;
	if (kernel?.name === 'llmkernel') {
		setKernelContext(kernel, {
			context: props.context,
			context_info: props.context_info
		});
	}
});

const inputStyle = computed(() => `--placeholder-color:${props.placeholderColor}`);

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);

// Lower case staes to match the naming in the messages.
enum KernelState {
	unknown = 'unknown',
	starting = 'starting',
	idle = 'idle',
	busy = 'busy',
	terminating = 'terminating',
	restarting = 'restarting',
	autorestarting = 'autorestarting',
	dead = 'dead'
}

const queryString = ref('');
const kernelState = ref<KernelState>(KernelState.idle);

const onEnter = () => {
	submitQuery(queryString.value);
};

const setKernelContext = (kernel: IKernelConnection, context_info) => {
	const messageBody = {
		session: props.llmContext.session?.name || '',
		channel: 'shell',
		content: context_info,
		msgType: 'context_setup_request',
		msgId: `${kernel.id}-setcontext`
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);
	kernelState.value = KernelState.busy;
};

const iopubMessageHandler = (_session, message) => {
	if (message.msg_type === 'status') {
		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
		kernelState.value = newState;
		return;
	}
	emit('newMessage', message);
};

props.llmContext.iopubMessage.connect(iopubMessageHandler);

const submitQuery = (inputStr: string | undefined) => {
	if (inputStr !== undefined) {
		const kernel = props.llmContext.session?.kernel;
		if (kernel === undefined || kernel === null) {
			return;
		}
		kernelState.value = KernelState.busy;
		const message: JupyterMessage = createMessage({
			session: props.llmContext.session?.name || '',
			channel: 'shell',
			content: { request: inputStr },
			msgType: 'llm_request',
			msgId: `${kernel.id}-setcontext`
		});
		kernel?.sendJupyterMessage(message);
		emit('newMessage', { ...message, msg_type: 'llm_request' });
	}
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}
});
</script>

<style scoped>
.chatty-container {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
}

#chatty-input {
	flex-grow: 1;
	color: black;
}

#chatty-history {
	height: auto;
	flex-direction: column;
	flex-basis: auto;
	overflow-y: scroll;
}

.query {
	color: green;
	white-space: pre;
}

.answer {
	color: darkblue;
	white-space: pre-wrap;
}

.thought {
	color: blueviolet;
	white-space: pre-wrap;
}

.error {
	color: darkred;
	white-space: pre-wrap;
}

.chatty {
	background-color: var(--gray-50);
	width: 100%;
	height: fit-content;
	color: black;
}
</style>
