<template>
	<div class="chatty-container" ref="containerElement">
		<label for="chatt-input"></label>
		<input
			ref="inputElement"
			id="chatty-input"
			v-model="queryString"
			type="text"
			:disabled="kernelState !== KernelState.idle"
			:style="inputStyle"
			:placeholder="placeholderMessage"
			@keydown.enter="onEnter"
		/>
		<div style="width: 3em; padding: 5%">
			<i v-if="kernelState !== KernelState.idle" class="pi pi-spin pi-spinner" />
		</div>
	</div>
</template>

<script setup lang="ts">
const emit = defineEmits(['newCodeCell', 'newMessage']);
import { computed, onMounted, ref } from 'vue';
import { SessionContext } from '@jupyterlab/apputils';
import { createMessage, IIOPubMessage } from '@jupyterlab/services/lib/kernel/messages';

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
		default: {}
	}
});
const llmContext = props.llmContext;
llmContext.kernelChanged.connect((_context, kernelInfo) => {
	const kernel = kernelInfo.newValue;
	if (kernel?.name === 'llmkernel') {
		setKernelContext(kernel, {
			context: props.context,
			context_info: props.context_info,
		});
	}
});


const inputStyle = computed(() => `--placeholder-color:${props.placeholderColor}`);

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);

// Lower case staes to match the naming in the messages.
enum KernelState {
	unknown,
	starting,
	idle,
	busy,
	terminating,
	restarting,
	autorestarting,
	dead
}

const queryString = ref('');
const kernelState = ref<KernelState>(KernelState.idle);
const messages = ref<Array<IIOPubMessage>>([]);


const onEnter = () => {
	submitQuery(inputElement.value?.value);
};

const setKernelContext = (kernel, context_info) => {
	// let kernel = sessionContext.session?.kernel;
	const message = createMessage({
		session: llmContext.session?.name,
		channel: 'shell',
		content: context_info,
		msgType: 'context_setup_request',
		msgId: `${kernel.id}-setcontext`,
	});
	kernel?.sendControlMessage(message);
	kernelState.value = KernelState.busy;
}

const iopubMessageHandler = (_session, message) => {
	if (message.msg_type === 'status') {
		const newState: KernelState = KernelState[message.content.execution_state];
		kernelState.value = newState;
		return;
	}
	emit('newMessage', message);
};
llmContext.iopubMessage.connect(iopubMessageHandler);

const submitQuery = (inputStr: string | undefined) => {
	if (inputStr !== undefined) {
		const kernel = llmContext.session?.kernel;
		if (kernel === undefined) {
			return;
		}
		kernelState.value = KernelState.busy;
		const message = createMessage({
			session: llmContext.session?.name,
			channel: 'shell',
			content: {"request": inputStr},
			msgType: 'llm_request',
			msgId: `${kernel.id}-setcontext`,
		});
		kernel?.sendControlMessage(message);
		emit('newMessage', {...message, msg_type: "llm_request"});
	}
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}
});

// llmContext.initialize();
</script>

<style scoped>
.chatty-container {
	display: flex;
	position: relative;
	flex-grow: 1;
	height: auto;
}

#chatty-input {
	flex-grow: 1;
	height: fit-content;
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
</style>
