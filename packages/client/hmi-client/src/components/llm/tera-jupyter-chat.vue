<template>
	<div class="tera-jupyter-chat">
		<!-- Jupyter Response and Input -->
		<div
			ref="messageContainer"
			@keydown.prevent="onKeyPress"
			@keydown.esc.capture="messageContainer?.focus()"
			tabindex="0"
			class="message-container"
		>
			<tera-jupyter-response
				@keydown.stop
				v-for="(msg, index) in filteredNotebookItems"
				ref="notebookCells"
				:class="{ selected: msg.query_id === selectedCellId }"
				:key="msg.query_id"
				:index="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="msg"
				:is-executing-code="isExecutingCode"
				:show-chat-thoughts="props.showChatThoughts"
				:auto-expand-preview="autoExpandPreview"
				:language="language"
				@delete-message="handleDeleteMessage"
				@delete-prompt="handleDeletePrompt"
				@re-run-prompt="handleRerunPrompt"
				@edit-prompt="reRunPrompt"
				@click="selectedCellId = msg.query_id"
				@on-selected="handleUpdateSelectedOutput(msg.query_id)"
			/>

			<!-- Add a cell Button -->
			<Button icon="pi pi-plus" label="Add a cell" size="small" class="add-cell-button" text @click="addCodeCell()" />
		</div>
	</div>
</template>
<script setup lang="ts">
import { onUnmounted, ref, watch, computed, nextTick } from 'vue';
import { createMessageId, JupyterMessage, KernelState, INotebookItem } from '@/services/jupyter';
import type { NotebookSession } from '@/types/Types';
import { AssetType } from '@/types/Types';
import TeraJupyterResponse from '@/components/llm/tera-jupyter-response.vue';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { updateNotebookSession } from '@/services/notebook-session';
import { useProjects } from '@/composables/project';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';

const messagesHistory = ref<JupyterMessage[]>([]);
const isExecutingCode = ref(false);
const messageContainer = ref(<HTMLElement | null>null);

const notebookItems = ref(<INotebookItem[]>[]);

const notebookCells = ref<(typeof TeraJupyterResponse)[]>([]);
const selectedCellId = ref();

const filteredNotebookItems = computed<INotebookItem[]>(() =>
	notebookItems.value.filter((item) => !isEmpty(item.messages))
);

const emit = defineEmits([
	'new-message',
	'download-response',
	'update-kernel-status',
	'new-dataset-saved',
	'new-model-saved',
	'update-kernel-state',
	'update-language',
	'update-selected-outputs'
]);

const props = defineProps<{
	assetName?: string;
	assetId?: string;
	assetType?: AssetType;
	language: string;
	showHistory?: { value: boolean; default: false };
	showJupyterSettings?: boolean;
	showChatThoughts?: boolean;
	jupyterSession: SessionContext;
	kernelStatus: String;
	autoExpandPreview?: boolean;
	notebookSession?: NotebookSession;
	defaultPreview?: string;
}>();

const iopubMessageHandler = (_session, message) => {
	if (message.header.msg_type === 'status') {
		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
		updateKernelStatus(KernelState[newState]);
		return;
	}
	newJupyterMessage(message);
};
const onKeyPress = (event) => {
	switch (event.key) {
		case 'j':
		case 'ArrowUp':
			selectPreviousCell();
			break;
		case 'k':
		case 'ArrowDown':
			selectNextCell();
			break;
		case 'a':
			addCodeCell(false, false);
			nextTick(() => {
				// notebookCells.vla
				scrollToCell(notebookCells.value.find((item) => item.$props.msg.query_id === selectedCellId.value));
			});
			break;
		case 'b':
			addCodeCell();
			nextTick(() => {
				scrollToCell(notebookCells.value.find((item) => item.$props.msg.query_id === selectedCellId.value));
			});
			break;
		case 'Enter':
			enterCell();
			break;
		case 'Escape':
			messageContainer.value?.focus();
			break;
		default:
			break;
	}
};

const scrollToCell = (element) => {
	if (element) {
		element.$el.scrollIntoView({ block: 'start', inline: 'nearest', behavior: 'smooth' });
	}
};

const enterCell = () => {
	const notebookCell = notebookCells.value.find((item) => item.$props.msg.query_id === selectedCellId.value);
	if (notebookCell?.codeCell?.[0]) {
		notebookCell.codeCell[0].enter();
	}
	scrollToCell(notebookCell);
};

const selectPreviousCell = () => {
	const index = filteredNotebookItems.value.findIndex((item) => item.query_id === selectedCellId.value);
	if (index > 0) {
		selectedCellId.value = filteredNotebookItems.value[index - 1].query_id;
	}
	const notebookCell = notebookCells.value.find((item) => item.$props.msg.query_id === selectedCellId.value);
	scrollToCell(notebookCell);
};

const selectNextCell = () => {
	const index = filteredNotebookItems.value.findIndex((item) => item.query_id === selectedCellId.value);
	if (index < filteredNotebookItems.value.length - 1) {
		selectedCellId.value = filteredNotebookItems.value[index + 1].query_id;
	}
	const notebookCell = notebookCells.value.find((item) => item.$props.msg.query_id === selectedCellId.value);
	scrollToCell(notebookCell);
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

const handleUpdateSelectedOutput = (queryId: string) => {
	notebookItems.value = notebookItems.value.map((item) => {
		if (item.query_id === queryId) {
			item.selected = !item.selected;
		} else {
			// for now we have radio button-like behaviour, so we can only select one output at a time
			item.selected = false;
		}
		return item;
	});
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
		notebookItem.query = query;
	}
	kernel.sendJupyterMessage(llmRequestMsg);
	isExecutingCode.value = true;
};

const addCodeCell = (isDefaultCell: boolean = false, isNextCell: boolean = true) => {
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
			code: isDefaultCell ? props.defaultPreview : ''
		},
		channel: 'iopub'
	};
	messagesHistory.value.push(emptyCell);
	updateNotebookCells(emptyCell, isNextCell);
};

// const nestedMessages = computed(() => {
const updateNotebookCells = (message, isNextCell: boolean = true) => {
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
			executions: [],
			// auto run the code block when we send an llm request
			autoRun: message.header.msg_type === 'llm_request',
			selected: false
		};

		const index = notebookItems.value.findIndex((item) => item.query_id === selectedCellId.value);
		if (isNextCell) {
			if (index === -1) {
				notebookItems.value.push(notebookItem);
			} else {
				notebookItems.value.splice(index + 1, 0, notebookItem);
			}
		} else {
			notebookItems.value.splice(index, 0, notebookItem);
		}
		selectedCellId.value = parentId;
	}
	if (message.header.msg_type === 'dataset') {
		// If we get a new dataset, remove any old datasets
		notebookItem.messages = notebookItem.messages.filter((msg) => msg.header.msg_type !== 'dataset');
		notebookItem.resultingCsv = message.content;
		emit('update-kernel-state', message.content);
		return;
	}
	if (message.header.msg_type === 'model_preview') {
		// If we get a new model preview, remove any old previews
		notebookItem.messages = notebookItem.messages.filter((msg) => msg.header.msg_type !== 'model_preview');
		emit('update-kernel-state', message.content);
	} else if (message.header.msg_type === 'execute_input') {
		const executionParent = message.parent_header.msg_id;
		notebookItem.executions.push(executionParent);
		// add the latest message execution to the code cell, we need this in order to persist the latest code execution
		const codeCell = notebookItem.messages.find((m) => m.header.msg_type === 'code_cell');
		if (codeCell) {
			codeCell.content = message.content;
		}
		// clear the cell outputs when we get a new code execution
		notebookItem.messages = notebookItem.messages.filter(
			(msg) => !['stream', 'display_data', 'execute_result', 'error'].includes(msg.header.msg_type)
		);
		return;
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
			'display_data',
			'execute_result',
			'error'
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
		if (el.codeOutputCell) {
			for (let j = 0; j < el.codeOutputCell.length; j++) {
				el.codeOutputCell[j].clear();
			}
		}
	}
};

onUnmounted(() => {
	messagesHistory.value = [];
});

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
			if (isEmpty(notebookItems.value)) {
				addCodeCell(true);
			}
			messageContainer.value?.focus();
			selectedCellId.value = filteredNotebookItems.value[filteredNotebookItems.value.length - 1]?.query_id;
		}
	},
	{ immediate: true }
);

watch(
	() => notebookItems.value,
	async () => {
		if (props.notebookSession) {
			const selectedOutputs = notebookItems.value.filter((item) => item.selected);
			emit('update-selected-outputs', selectedOutputs);
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
	clearOutputs,
	submitQuery,
	addCodeCell
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
	isolation: isolate;
}

.selected {
	background-color: var(--surface-0);
	border: 1px solid var(--primary-color);
}

.add-cell-button {
	margin-left: var(--gap-4);
	width: calc(100% - 2rem);
}
.add-cell-button:deep(.p-button-label) {
	text-align: left;
}
.message-container {
	background: var(--surface-100);
}
</style>
