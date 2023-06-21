<template>
	<ConfirmDialog></ConfirmDialog>
	<div class="tera-jupyter-chat">
		<div class="settings-title" v-if="props.showJupyterSettings">Kernel Settings</div>
		<div class="jupyter-settings" v-if="props.showJupyterSettings">
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
		<div>
			<tera-jupyter-response
				v-for="(msg, index) in nestedMessages"
				:key="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="msg"
				:has-been-drawn="renderedMessages.has(msg.query_id)"
				@has-been-drawn="hasBeenDrawn(msg.query_id)"
			/>
			<tera-chatty-input
				class="tera-chatty-input"
				:llm-context="jupyterSession"
				@update-kernel-status="updateKernelStatus"
				@new-message="newJupyterResponse"
				context="dataset"
				:context_info="{
					id: props.assetId
				}"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { ref, watch, onUnmounted, onMounted, computed } from 'vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import TeraChattyInput from '@/components/llm/tera-chatty-input.vue';
import { newSession, JupyterMessage, sessionManager, serverSettings } from '@/services/jupyter';
import TeraJupyterResponse from '@/components/llm/tera-jupyter-response.vue';
import { IModel } from '@jupyterlab/services/lib/session/session';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { shutdownKernel } from '@jupyterlab/services/lib/kernel/restapi';
import { useConfirm } from 'primevue/useconfirm';
import ConfirmDialog from 'primevue/confirmdialog';

const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
	showHistory?: { value: boolean; default: false };
	showJupyterSettings?: boolean;
}>();

const isQuery = (message) => message.header.msg_type === 'llm_request';

const renderedMessages = ref(new Set<any>());

const hasBeenDrawn = (message_id) => {
	renderedMessages.value.add(message_id);
};

const nestedMessages = computed(() => {
	const result: {
		query_id: string;
		query: string;
		timestamp: string;
		messages: JupyterMessage[];
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
						messages: currentMessages
					});
					// Clear the currentMessages for new query
					currentMessages = [];
				}
				result.push({
					query_id: message.header.msg_id,
					// eslint-disable-next-line @typescript-eslint/dot-notation
					query: message.content['request'],
					timestamp: message.header.date,
					messages: []
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
					messages: currentMessages
				});
			}
		}
	}
	return result;
});

const confirm = useConfirm();

const jupyterSession = newSession('llmkernel', 'ChattyNode');

const messagesHistory = ref<JupyterMessage[]>([]);
const selectedKernel = ref();
const activeSessions = ref(sessionManager.running());

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

const runningSessions = ref();
const emit = defineEmits(['new-message', 'update-table-preview', 'update-kernel-status']);

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

watch(
	() => [activeSessions.value],
	() => {
		if (jupyterSession.session) {
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

// eslint-disable-next-line vue/return-in-computed-property
const updateKernelList = () => {
	jupyterSession.ready.then(() => {
		if (jupyterSession.session) {
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
				kernelId: jupyterSession.session?.kernel?.id,
				value: jupyterSession.session?.id
			};
		}
	});
};

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);

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
		emit('new-message', messagesHistory.value);
	} else if (jupyterResponse.header.msg_type === 'dataset') {
		emit('update-table-preview', jupyterResponse.content);
	} else {
		console.log('Unknown Jupyter event', jupyterResponse);
	}
};

onMounted(() => {
	// for admin panel
	jupyterSession.ready.then(() => {
		if (jupyterSession.session) {
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
			selectedKernel.value = {
				kernelId: jupyterSession.session?.kernel?.id,
				value: jupyterSession.session?.id
			};
		}
	});
});

onUnmounted(() => {
	jupyterSession.shutdown();
});
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
