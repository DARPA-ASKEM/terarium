<template>
	<div class="tera-jupyter-chat">
		<!-- Jupyter Response and Input -->
		<div ref="messageContainer">
			<tera-jupyter-response
				v-for="(msg, index) in notebookItems"
				ref="notebookCells"
				:key="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="msg"
				:has-been-drawn="renderedMessages.has(msg.query_id)"
				:is-executing-code="isExecutingCode"
				:show-chat-thoughts="props.showChatThoughts"
				@has-been-drawn="hasBeenDrawn(msg.query_id)"
				@is-typing="emit('is-typing')"
				@cell-updated="scrollToLastCell"
			/>

			<!-- Chatty Input -->
			<tera-chatty-input
				class="tera-chatty-input"
				:kernel-is-busy="props.kernelStatus !== KernelState.idle"
				context="dataset"
				@submitQuery="submitQuery"
				@add-code-cell="addCodeCell"
			/>
		</div>
	</div>
</template>
<script setup lang="ts">
import { ref, watch, onUnmounted, onMounted } from 'vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import {
	getSessionManager,
	JupyterMessage,
	KernelState,
	createMessageId
} from '@/services/jupyter';
import { CsvAsset } from '@/types/Types';
import TeraChattyInput from '@/components/llm/tera-chatty-input.vue';
import TeraJupyterResponse from '@/components/llm/tera-jupyter-response.vue';
import { IModel } from '@jupyterlab/services/lib/session/session';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';

const messagesHistory = ref<JupyterMessage[]>([]);
const isExecutingCode = ref(false);
const renderedMessages = ref(new Set<any>());
const messageContainer = ref(<HTMLElement | null>null);
const activeSessions = ref(null);
const runningSessions = ref();
const notebookItems = ref(
	<
		{
			query_id: string;
			query: string | null;
			timestamp: string;
			messages: JupyterMessage[];
			resultingCsv: CsvAsset | null;
			executions: any[];
		}[]
	>[]
);
const notebookCells = ref([]);

const emit = defineEmits([
	'new-message',
	'download-response',
	'update-kernel-status',
	'new-dataset-saved',
	'new-model-saved',
	'is-typing'
]);

const props = defineProps<{
	project?: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
	showHistory?: { value: boolean; default: false };
	showJupyterSettings?: boolean;
	showChatThoughts?: boolean;
	jupyterSession: SessionContext;
	kernelStatus: String;
}>();

onMounted(() => {
	activeSessions.value = getSessionManager().running();
});

const hasBeenDrawn = (message_id: string) => {
	renderedMessages.value.add(message_id);
	messageContainer.value?.scrollIntoView({ behavior: 'smooth' });
};

const queryString = ref('');

const iopubMessageHandler = (_session, message) => {
	if (message.header.msg_type === 'status') {
		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
		updateKernelStatus(KernelState[newState]);
		return;
	}
	newJupyterMessage(message);
};

props.jupyterSession.iopubMessage.connect(iopubMessageHandler);

const submitQuery = (inputStr: string | undefined) => {
	if (inputStr !== undefined) {
		const kernel = props.jupyterSession.session?.kernel as IKernelConnection;
		if (kernel === undefined || kernel === null) {
			return;
		}
		updateKernelStatus(KernelState.busy);
		const msgId = createMessageId('llm_request');
		const message: JupyterMessage = createMessage({
			session: props.jupyterSession.session?.name || '',
			channel: 'shell',
			content: { request: inputStr },
			msgType: 'llm_request',
			msgId
		});
		kernel?.sendJupyterMessage(message);
		newJupyterMessage(message);
		queryString.value = '';
	}
};

const addCodeCell = () => {
	const msgId = createMessageId('code_cell');
	const date = new Date().toISOString();
	const emptyCell: JupyterMessage = {
		header: {
			msg_id: msgId,
			msg_type: 'code_cell',
			username: 'username',
			session: 'c4af9869-23efbe216d9848478c1651fd',
			date,
			version: '5.3'
		},
		parent_header: {},
		metadata: {},
		content: {
			language: 'python',
			code: ''
		},
		channel: 'iopub'
	};
	messagesHistory.value.push(emptyCell);
	updateNotebookCells(emptyCell);
	hasBeenDrawn(msgId);
};

// const nestedMessages = computed(() => {
const updateNotebookCells = (message) => {
	// This computed property groups Jupyter messages into queries
	// and stores resulting csv after each query.
	let notebookItem;
	const parentId: String | null =
		message.metadata?.notebook_item ||
		message.parent_header?.msg_id ||
		message.header?.msg_id ||
		null;

	// Update existing cell
	notebookItem = notebookItems.value.find(
		(val) => val.executions.indexOf(message.parent_header.msg_id) > -1 || val.query_id === parentId
	);
	if (!notebookItem) {
		const query = message.header.msg_type === 'llm_request' ? message.content.request : null;
		// New cell
		notebookItem = {
			query_id: parentId,
			query,
			timestamp: message.parent_header.date,
			messages: [],
			resultingCsv: null,
			executions: []
		};
		notebookItems.value.push(notebookItem);
	}
	if (message.header.msg_type === 'dataset') {
		// If we get a new dataset, remove any old datasets
		notebookItem.messages = notebookItem.messages.filter(
			(msg) => msg.header.msg_type !== 'dataset'
		);
		notebookItem.resultingCsv = message.content;
	} else if (message.header.msg_type === 'execute_input') {
		const executionParent = message.parent_header.msg_id;
		notebookItem.executions.push(executionParent);
		return;
	}

	notebookItem.messages.push(message);
};

const updateKernelStatus = (kernelStatus) => {
	emit('update-kernel-status', kernelStatus);
};

const newJupyterMessage = (jupyterMessage) => {
	if (
		['stream', 'code_cell', 'llm_request', 'chatty_response', 'dataset'].indexOf(
			jupyterMessage.header.msg_type
		) > -1
	) {
		messagesHistory.value.push(jupyterMessage);
		updateNotebookCells(jupyterMessage);
		isExecutingCode.value = false;
		emit('new-message', messagesHistory.value);
	} else if (jupyterMessage.header.msg_type === 'save_dataset_response') {
		emit('new-dataset-saved', jupyterMessage.content);
		isExecutingCode.value = false;
	} else if (jupyterMessage.header.msg_type === 'save_model_response') {
		emit('new-model-saved', jupyterMessage.content);
		isExecutingCode.value = false;
	} else if (jupyterMessage.header.msg_type === 'download_response') {
		emit('download-response', jupyterMessage.content);
		isExecutingCode.value = false;
	} else if (jupyterMessage.header.msg_type === 'execute_input') {
		updateNotebookCells(jupyterMessage);
		isExecutingCode.value = true;
	} else if (jupyterMessage.header.msg_type === 'model_preview') {
		updateNotebookCells(jupyterMessage);
	} else {
		console.log('Unknown Jupyter event', jupyterMessage);
	}
};

const scrollToLastCell = (element, msg) => {
	if (msg === notebookItems.value[notebookItems.value.length - 1]) {
		element.scrollIntoView(false);
	}
};

onUnmounted(() => {
	messagesHistory.value = [];
});

watch(
	() => [activeSessions.value],
	() => {
		if (props.jupyterSession.session) {
			const sessions = getSessionManager().running();
			const results: IModel[] = [];
			let result = sessions.next();
			while (result) {
				results.push(result);
				result = sessions.next();
			}
			runningSessions.value = results
				.reverse()
				.map((r) => ({ kernelId: r.kernel?.id, value: r.id }));
		}
		return [];
	},
	{ deep: true }
);

watch(
	() => messageContainer.value,
	() => {
		if (messageContainer.value) {
			messageContainer.value?.scrollIntoView({ behavior: 'smooth' });
		}
	},
	{ deep: true } // enable deep watching in case msg.messages is an array of objects
);

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}

.jupyter-settings {
	display: flex;
	flex-direction: row;
	width: 100%;
}

.kernel-dropdown {
	flex-grow: 10;
}

.settings-title {
	color: var(--gray-500);
	font-size: 12px;
	font-family: monospace;
	padding-bottom: 5px;
}

.tera-jupyter-chat {
	display: flex;
	flex-direction: column;
	width: 100%;
}
</style>
