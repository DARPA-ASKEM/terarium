<template>
	<div class="tera-jupyter-chat">
		<ConfirmDialog></ConfirmDialog>
		<!-- Jupyter Kernel Settings -->
		<div class="settings-title" v-if="props.showJupyterSettings">Kernel Settings</div>
		<div class="jupyter-settings" v-if="props.showJupyterSettings">
			<!-- Kernel Dropdown Selector -->
			<div class="kernel-dropdown">
				<Dropdown
					v-model="selectedKernel"
					:options="runningSessions"
					filter
					optionLabel="kernelId"
					optionsValue="value"
					:disabled="runningSessions.length === 0"
					style="min-width: 100%; height: 30px; margin-bottom: 10px"
				/>
			</div>

			<!-- Kernel Control Buttons -->
			<Button
				style="flex-grow: 0.2; height: 30px; margin: 0px 0px 10px 10px"
				@click="confirmDelete"
				:disabled="runningSessions.length <= 0"
				>Delete Kernel</Button
			>
			<Button
				style="flex-grow: 0.2; height: 30px; margin: 0px 0px 10px 10px"
				@click="confirmDeleteAll"
				:disabled="runningSessions.length <= 0"
				>Delete ALL</Button
			>
			<Button
				style="flex-grow: 0.2; height: 30px; margin: 0px 0px 10px 10px"
				@click="confirmReconnect"
				:disabled="runningSessions.length <= 0"
				>Reconnect</Button
			>
		</div>

		<!-- Jupyter Response and Input -->
		<div ref="messageContainer">
			<tera-jupyter-response
				v-for="(msg, index) in nestedMessages"
				:key="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="msg"
				:has-been-drawn="renderedMessages.has(msg.query_id)"
				:is-executing-code="isExecutingCode"
				:show-chat-thought="props.showChatThought"
				@has-been-drawn="hasBeenDrawn(msg.query_id)"
			/>

			<!-- Chatty Input -->
			<tera-chatty-input
				class="tera-chatty-input"
				:kernel-is-busy="props.kernelStatus !== KernelState.idle"
				context="dataset"
				@submitQuery="submitQuery"
			/>
		</div>
	</div>
</template>
<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { ref, watch, onUnmounted, computed } from 'vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { JupyterMessage, sessionManager, serverSettings, KernelState } from '@/services/jupyter';
import { CsvAsset } from '@/types/Types';
import { useConfirm } from 'primevue/useconfirm';
import TeraChattyInput from '@/components/llm/tera-chatty-input.vue';
import TeraJupyterResponse from '@/components/llm/tera-jupyter-response.vue';
import { IModel } from '@jupyterlab/services/lib/session/session';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { shutdownKernel } from '@jupyterlab/services/lib/kernel/restapi';
import ConfirmDialog from 'primevue/confirmdialog';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';

const messagesHistory = ref<JupyterMessage[]>([]);
const isExecutingCode = ref(false);
const renderedMessages = ref(new Set<any>());
const messageContainer = ref(<HTMLElement | null>null);
const confirm = useConfirm();
const selectedKernel = ref();
const activeSessions = ref(sessionManager.running());
const runningSessions = ref();

const emit = defineEmits([
	'new-message',
	'update-table-preview',
	'update-kernel-status',
	'new-dataset-saved'
]);

const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
	showHistory?: { value: boolean; default: false };
	showJupyterSettings?: boolean;
	showChatThought?: boolean;
	jupyterSession: SessionContext;
	kernelStatus: String;
}>();

const isQuery = (message) => message.header.msg_type === 'llm_request';
const hasBeenDrawn = (message_id: string) => {
	renderedMessages.value.add(message_id);
	messageContainer.value?.scrollIntoView({ behavior: 'smooth' });
};

const queryString = ref('');

const iopubMessageHandler = (_session, message) => {
	if (message.msg_type === 'status') {
		const newState: KernelState = KernelState[KernelState[message.content.execution_state]];
		updateKernelStatus(KernelState[newState]);
		return;
	}
	newJupyterResponse(message);
};

props.jupyterSession.iopubMessage.connect(iopubMessageHandler);

const submitQuery = (inputStr: string | undefined) => {
	console.log(inputStr);
	if (inputStr !== undefined) {
		const kernel = props.jupyterSession.session?.kernel as IKernelConnection;
		if (kernel === undefined || kernel === null) {
			return;
		}
		updateKernelStatus(KernelState.busy);
		const message: JupyterMessage = createMessage({
			session: props.jupyterSession.session?.name || '',
			channel: 'shell',
			content: { request: inputStr },
			msgType: 'llm_request',
			msgId: `${kernel.id}-setcontext`
		});
		kernel?.sendJupyterMessage(message);
		newJupyterResponse({ ...message, msg_type: 'llm_request' });
		queryString.value = '';
	}
};

// let jupyterCsv: CsvAsset | null = null;

// function cloneCsvAsset(asset: CsvAsset): CsvAsset {
// 	return {
// 		csv: [...asset.csv.map((row) => [...row])], // create a copy of each sub-array
// 		stats: asset.stats ? [...asset.stats] : undefined, // copy stats array if it exists
// 		headers: [...asset.headers] // copy headers array
// 	};
// }

const nestedMessages = computed(() => {
	// This computed property groups Jupyter messages into queries
	// and stores resulting csv after each query.
	const result: {
		query_id: string;
		query: string;
		timestamp: string;
		messages: JupyterMessage[];
		resultingCsv: CsvAsset | null;
	}[] = [];
	let currentQuery = '';
	let currentQueryId = '';
	let currentTimestamp = '';
	let currentMessages: JupyterMessage[] = [];

	if (messagesHistory.value.length) {
		messagesHistory.value.forEach((message) => {
			if (isQuery(message)) {
				if (currentMessages.length > 0) {
					// removes the empty query
					if (result.length > 0 && result[result.length - 1].query === currentQuery) {
						result.pop();
					}
					// When a new query is encountered, push the previous query and its messages into result
					result.push({
						query_id: currentQueryId,
						query: currentQuery,
						timestamp: currentTimestamp,
						messages: currentMessages,
						resultingCsv: null
						// resultingCsv: jupyterCsv ? cloneCsvAsset(jupyterCsv) : null
					});
					// Clear the currentMessages for new query
					currentMessages = [];
				}
				result.push({
					query_id: message.header.msg_id,
					// eslint-disable-next-line @typescript-eslint/dot-notation
					query: message.content['request'],
					timestamp: message.header.date,
					messages: [],
					resultingCsv: null
					// resultingCsv: jupyterCsv ? cloneCsvAsset(jupyterCsv) : null
				});
				// Update the currentQuery, currentQueryId and currentTimestamp with new values
				// eslint-disable-next-line @typescript-eslint/dot-notation
				currentQuery = message.content['request'];
				currentQueryId = message.header.msg_id;
				currentTimestamp = message.header.date;
			} else {
				// If the message is not a query, add it to currentMessages
				currentMessages.push(message);
			}
		});
		// Add the last group of messages to the result
		if (currentMessages.length > 0) {
			if (result.length > 0 && result[result.length - 1].query === currentQuery) {
				result[result.length - 1].messages = currentMessages;
			} else {
				result.push({
					query_id: currentQueryId,
					query: currentQuery,
					timestamp: currentTimestamp,
					messages: currentMessages,
					resultingCsv: null
					// resultingCsv: jupyterCsv ? cloneCsvAsset(jupyterCsv) : null
				});
			}
		}
	}
	return result;
});

// eslint-disable-next-line vue/return-in-computed-property
const updateKernelList = () => {
	props.jupyterSession.ready.then(() => {
		if (props.jupyterSession.session) {
			const sessions = sessionManager.running();
			const results: IModel[] = [];
			let result = sessions.next();
			while (result) {
				result = sessions.next();
			}
			runningSessions.value = undefined;
			runningSessions.value = results
				.reverse()
				.map((r) => ({ kernelId: r.kernel?.id, value: r.id }));
			selectedKernel.value = {
				kernelId: props.jupyterSession.session?.kernel?.id,
				value: props.jupyterSession.session?.id
			};
		}
	});
};

const updateKernelStatus = (kernelStatus) => {
	emit('update-kernel-status', kernelStatus);
};

const newJupyterResponse = (jupyterResponse) => {
	if (
		['stream', 'code_cell', 'llm_request', 'chatty_response'].indexOf(
			jupyterResponse.header.msg_type
		) > -1
	) {
		messagesHistory.value.push(jupyterResponse);
		isExecutingCode.value = false;
		emit('new-message', messagesHistory.value);
	} else if (jupyterResponse.header.msg_type === 'dataset') {
		emit('update-table-preview', jupyterResponse.content);
		// jupyterCsv = jupyterResponse.content;
		isExecutingCode.value = false;
	} else if (jupyterResponse.header.msg_type === 'save_dataset_response') {
		emit('new-dataset-saved', jupyterResponse.content);
		isExecutingCode.value = false;
	} else if (jupyterResponse.header.msg_type === 'download_response') {
		emit('download-response', jupyterResponse.content);
		isExecutingCode.value = false;
	} else if (jupyterResponse.header.msg_type === 'execute_input') {
		isExecutingCode.value = true;
	} else {
		console.log('Unknown Jupyter event', jupyterResponse);
	}
};

const killKernel = () => {
	shutdownKernel(selectedKernel.value.kernelId, serverSettings);
	updateKernelList();
};

const deleteAllKernels = () => {
	runningSessions.value.forEach((k) => {
		shutdownKernel(k.kernelId, serverSettings);
	});
	updateKernelList();
};

// Kernel Confirmation dialogs
const confirmReconnect = () => {
	confirm.require({
		message: `Are you sure you want to proceed to terminate ${runningSessions.value.length} ?`,
		header: 'Confirmation',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			deleteAllKernels();
		}
	});
};

const confirmDeleteAll = () => {
	confirm.require({
		message: `Are you sure you want to proceed to terminate ${runningSessions.value.length} sessions?`,
		header: 'Terminate All Kernels',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			deleteAllKernels();
		}
	});
};

const confirmDelete = () => {
	confirm.require({
		message: `Are you sure you want to terminate session ${runningSessions.value.length}?`,
		header: 'Terminate Kernel',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			killKernel();
		}
	});
};

onUnmounted(() => {
	messagesHistory.value = [];
});

watch(
	() => [activeSessions.value],
	() => {
		if (props.jupyterSession.session) {
			const sessions = sessionManager.running();
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
