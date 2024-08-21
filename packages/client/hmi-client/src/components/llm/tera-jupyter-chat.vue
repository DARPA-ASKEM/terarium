<template>
	<div class="tera-jupyter-chat">
		<!-- Jupyter Response and Input -->
		<div ref="messageContainer">
			<tera-jupyter-response
				v-for="(msg, index) in notebookItems"
				ref="notebookCells"
				:key="index"
				:index="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="msg"
				:is-executing-code="isExecutingCode"
				:show-chat-thoughts="props.showChatThoughts"
				:auto-expand-preview="autoExpandPreview"
				:default-preview="defaultPreview"
				@cell-updated="scrollToLastCell"
				@preview-selected="previewSelected"
				@delete-message="handleDeleteMessage"
				@delete-prompt="handleDeletePrompt"
				@re-run-prompt="handleRerunPrompt"
				@edit-prompt="reRunPrompt"
			/>
			<!-- spacer to prevent the floating input panel at the bottom of the screen from covering the bottom item -->
			<div style="height: 8rem"></div>

			<!-- Beaker Input -->
			<tera-beaker-input
				class="tera-beaker-input"
				:kernel-is-busy="props.kernelStatus !== KernelState.idle"
				context="dataset"
				@submitQuery="submitQuery"
				@add-code-cell="addCodeCell"
			/>
		</div>
	</div>
</template>
<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { createMessageId, getSessionManager, JupyterMessage, KernelState, INotebookItem } from '@/services/jupyter';
import type { NotebookSession } from '@/types/Types';
import { AssetType } from '@/types/Types';
import TeraBeakerInput from '@/components/llm/tera-beaker-input.vue';
import TeraJupyterResponse from '@/components/llm/tera-jupyter-response.vue';
import { IModel } from '@jupyterlab/services/lib/session/session';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { updateNotebookSession } from '@/services/notebook-session';
import { useProjects } from '@/composables/project';

const messagesHistory = ref<JupyterMessage[]>([]);
const isExecutingCode = ref(false);
const messageContainer = ref(<HTMLElement | null>null);
const activeSessions = ref(null);
const runningSessions = ref();
const notebookItems = ref(<INotebookItem[]>[]);
const notebookCells = ref<(typeof TeraJupyterResponse)[]>([]);

const emit = defineEmits([
	'new-message',
	'download-response',
	'update-kernel-status',
	'new-dataset-saved',
	'new-model-saved',
	'update-kernel-state',
	'update-language'
]);

const props = defineProps<{
	assetName?: string;
	assetId?: string;
	assetType?: AssetType;
	showHistory?: { value: boolean; default: false };
	showJupyterSettings?: boolean;
	showChatThoughts?: boolean;
	jupyterSession: SessionContext;
	kernelStatus: String;
	autoExpandPreview?: boolean;
	notebookSession?: NotebookSession;
}>();

onMounted(async () => {
	if (props.notebookSession) {
		notebookItems.value = props.notebookSession.data?.history;
	}
	activeSessions.value = getSessionManager().running();

	// Add a code cell if there are no cells present
	if (notebookItems.value.length === 0) {
		addCodeCell();
	}
});

const defaultPreview = ref('d1');

const iopubMessageHandler = (_session, message) => {
	if (message.header.msg_type === 'status') {
		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
		updateKernelStatus(KernelState[newState]);
		return;
	}
	newJupyterMessage(message);
};

const previewSelected = (selection) => {
	defaultPreview.value = selection;
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
		isExecutingCode.value = true;
	}
};

const handleDeleteMessage = (msgId: string) => {
	const beforeNumItems = notebookItems.value.length;
	// if msgId is a id of top level code cell, remove the entire cell
	notebookItems.value = notebookItems.value.filter((item) => item.query_id !== msgId);
	if (beforeNumItems === notebookItems.value.length) {
		// Iterate over notebookItems to find and remove the message with msgId
		notebookItems.value.forEach((item) => {
			const messageIndex = item.messages.findIndex((m) => m.header.msg_id === msgId);
			if (messageIndex > -1) {
				item.messages.splice(messageIndex, 1);
			}
		});
	}
};

const handleDeletePrompt = (queryId: string) => {
	notebookItems.value = notebookItems.value.filter((item) => item.query_id !== queryId);
};

const handleRerunPrompt = (queryId: string) => {
	reRunPrompt(queryId);
};

const reRunPrompt = (queryId: string, query?: string) => {
	const kernel = props.jupyterSession.session?.kernel as IKernelConnection;
	if (!kernel) return;
	updateKernelStatus(KernelState.busy);
	const notebookItem = notebookItems.value.find((item) => item.query_id === queryId);
	if (!notebookItem) return;
	const llmRequestMsg = notebookItem.messages.find((m) => m.header.msg_type === 'llm_request');
	if (!llmRequestMsg) return;
	notebookItem.executions = [];
	notebookItem.messages = [llmRequestMsg];
	if (query) {
		llmRequestMsg.content.request = query;
	}
	kernel.sendJupyterMessage(llmRequestMsg);
	isExecutingCode.value = true;
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
			code: defaultPreview.value
		},
		channel: 'iopub'
	};
	messagesHistory.value.push(emptyCell);
	updateNotebookCells(emptyCell);
	defaultPreview.value = ''; // reset the default preview
};

// const nestedMessages = computed(() => {
const updateNotebookCells = (message) => {
	// This computed property groups Jupyter messages into queries
	// and stores resulting csv after each query.
	let notebookItem: INotebookItem | undefined;
	const parentId: string | null =
		message.metadata?.notebook_item || message.parent_header?.msg_id || message.header?.msg_id || null;

	// Update existing cell
	notebookItem = notebookItems.value.find(
		(val) => val.executions.indexOf(message.parent_header.msg_id) > -1 || val.query_id === parentId
	);
	if (!notebookItem) {
		const query = message.header.msg_type === 'llm_request' ? message.content.request : null;
		// New cell
		notebookItem = {
			query_id: parentId ?? '',
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
		notebookItem.messages = notebookItem.messages.filter((msg) => msg.header.msg_type !== 'dataset');
		notebookItem.resultingCsv = message.content;
		emit('update-kernel-state', message.content);
	} else if (message.header.msg_type === 'model_preview') {
		// If we get a new model preview, remove any old previews
		notebookItem.messages = notebookItem.messages.filter((msg) => msg.header.msg_type !== 'model_preview');
		emit('update-kernel-state', message.content);
	} else if (message.header.msg_type === 'execute_input') {
		const executionParent = message.parent_header.msg_id;
		notebookItem.executions.push(executionParent);
		// add the latest message execution to the code cell, we need this in order to persist the latest code execution
		const codeCell = notebookItem.messages.find((m) => m.header.msg_type === 'code_cell');
		if (codeCell) {
			codeCell.content.code = message.content.code;
		}
		return;
	} else if (['stream', 'display_data', 'execute_result'].indexOf(message.header.msg_type) > -1) {
		// remove the old output cells if a new output type is received
		notebookItem.messages = notebookItem.messages.filter((msg) => msg.header.msg_type !== message.header.msg_type);
	}

	notebookItem.messages.push(message);
};

const updateKernelStatus = (kernelStatus) => {
	emit('update-kernel-status', kernelStatus);
};

const newJupyterMessage = (jupyterMessage) => {
	const msgType = jupyterMessage.header.msg_type;
	if (
		[
			'stream',
			'code_cell',
			'llm_request',
			'llm_thought',
			'llm_response',
			'beaker_response',
			'dataset',
			'display_data'
		].indexOf(msgType) > -1
	) {
		messagesHistory.value.push(jupyterMessage);
		updateNotebookCells(jupyterMessage);
		isExecutingCode.value = msgType === 'llm_request' || msgType === 'code_cell';
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
	} else if (jupyterMessage.header.msg_type === 'model_preview') {
		updateNotebookCells(jupyterMessage);
		isExecutingCode.value = false;
	} else if (jupyterMessage.header.msg_type === 'context_setup_response') {
		emit('update-language', jupyterMessage.content.subkernel);
	} else {
		console.log('Unknown Jupyter event', jupyterMessage);
	}
};

const clearHistory = () => {
	messagesHistory.value = [];
	notebookItems.value = [];
};

// Clear all the outputs in the chat, without clearing the code/prompts/etc.
const clearOutputs = () => {
	for (let i = 0; i < notebookItems.value.length; i++) {
		const item = notebookItems.value[i];
		for (let j = item.messages.length - 1; j >= 0; j--) {
			const message = item.messages[j];
			const msgType = message.header.msg_type;
			if (msgType === 'model_preview' || msgType === 'dataset') {
				item.messages.splice(j, 1);
			}
			if (msgType === 'code_cell') {
				console.log(message);
			}
		}
	}
	for (let i = 0; i < notebookCells.value.length; i++) {
		const el = notebookCells.value[i];
		if (el.codeCell) {
			for (let j = 0; j < el.codeCell.length; j++) {
				el.codeCell[j].clear();
			}
		}
	}
};

const scrollToLastCell = (element, msg) => {
	if (msg === notebookItems.value[notebookItems.value.length - 1]) {
		element.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' });
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
			runningSessions.value = results.reverse().map((r) => ({ kernelId: r.kernel?.id, value: r.id }));
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
		useProjects().activeProject.value
	],
	() => {
		console.log(useProjects().activeProject.value, props.assetId);
	}
);

// update the notebook history when we get the notebookSession
watch(
	() => props.notebookSession,
	() => {
		if (props.notebookSession) {
			notebookItems.value = props.notebookSession.data.history;
		}
	}
);

watch(
	() => notebookItems.value,
	async () => {
		if (props.notebookSession) {
			await updateNotebookSession({
				id: props.notebookSession.id,
				description: props.notebookSession.description,
				data: { history: notebookItems.value }
			});
		}
	},
	{ deep: true }
);

defineExpose({
	clearHistory,
	clearOutputs
});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}

.tera-jupyter-chat {
	display: flex;
	flex-direction: column;
	width: 100%;
}
</style>
