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
	</div>
	<div id="chatty-history">
		<div v-for="message in messages" :key="message.msg_id">
			<div v-if="message.msg_type === 'execute_input'" class="query">
				Query: {{ message.content?.code }}
			</div>
			<div
				v-else-if="
					message.msg_type === 'chatty_response' && message.content.name === 'response_text'
				"
				class="answer"
			>
				Response: {{ message.content?.text }}
			</div>
			<div
				v-else-if="message.msg_type === 'stream' && message.content.name === 'stdout'"
				class="thought"
			>
				{{ message.content?.text }}
			</div>
			<div
				v-else-if="message.msg_type === 'stream' && message.content.name === 'stderr'"
				class="error"
			>
				Error: {{ message.content?.text }}
			</div>
			<div v-else>Other: {{ message }}</div>
			<hr />
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { SessionContext } from '@jupyterlab/apputils';
import {
	ServerConnection,
	KernelManager,
	KernelSpecManager,
	SessionManager
} from '@jupyterlab/services';
import { IIOPubMessage } from '@jupyterlab/services/lib/kernel/messages';

// TODO: These settings should be pulled from the environment variables or appropriate config setup.
const serverSettings = ServerConnection.makeSettings({
	baseUrl: '/chatty/',
	appUrl: 'http://localhost:8078/chatty/',
	wsUrl: 'ws://localhost:8078/chatty_ws/',
	token: import.meta.env.VITE_JUPYTER_TOKEN
});

const kernelManager = new KernelManager({
	serverSettings
});
const specsManager = new KernelSpecManager({
	serverSettings
});
const sessionManager = new SessionManager({
	kernelManager,
	serverSettings
});
const sessionContext = new SessionContext({
	sessionManager,
	specsManager,
	name: 'ChattyNode',
	kernelPreference: {
		name: 'llmkernel'
		// name: "python3",
	}
});
specsManager.refreshSpecs();

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

const iopubMessageHandler = (_, message: IIOPubMessage) => {
	if (message.msg_type === 'status') {
		const newState: KernelState = KernelState[message.content.execution_state];
		kernelState.value = newState;
		return;
	}
	console.log(message);
	messages.value.push(message);
};
sessionContext.iopubMessage.connect(iopubMessageHandler);

const submitQuery = (inputStr: string | undefined) => {
	if (inputStr !== undefined) {
		sessionContext.session?.kernel?.requestExecute({
			code: inputStr,
			silent: false
		});
	}
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}
});

sessionContext.initialize();
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
}

.query {
	color: green;
	white-space: pre;
}

.answer {
	color: darkblue;
	white-space: pre;
}

.thought {
	color: blueviolet;
	white-space: pre;
}

.error {
	color: darkred;
	white-space: pre;
}
</style>
