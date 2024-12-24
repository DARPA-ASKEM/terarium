<template>
	<!-- Output selector and Save as buttons artificially injected into drilldown header -->
	<div class="data-transform-container h-full overflow-hidden">
		<!-- Toolbar -->
		<div class="toolbar flex">
			<!-- Kernel Status -->
			<div class="kernel-status-container">
				<span><i class="pi pi-circle-fill kernel-status" :style="statusStyle" /></span>
				<header id="GPT">
					{{ kernelStatus === 'idle' ? 'Ready' : kernelStatus === 'busy' ? 'Busy' : 'Offline' }}
				</header>
			</div>
			<!-- Beaker Input -->
			<tera-beaker-input
				class="tera-beaker-input"
				:kernel-is-busy="kernelStatus !== KernelState.idle"
				:default-options="sampleAgentQuestions"
				context="dataset"
				@submitQuery="onSubmitQuery"
				@add-code-cell="onAddCodeCell"
				@run-all-cells="onRunAllCells"
				@keydown.stop
			/>
			<span class="flex-auto"></span>

			<!-- Jupyter Kernel Settings -->
			<div class="settings-title" v-if="showKernels">Kernel Settings</div>
			<div class="jupyter-settings" v-if="showKernels">
				<!-- Kernel Dropdown Selector -->
				<Dropdown
					v-model="selectedKernel"
					:options="runningSessions"
					filter
					optionLabel="kernelId"
					optionsValue="value"
					:disabled="runningSessions.length === 0"
				/>
				<!-- Kernel Control Buttons -->
				<Button
					severity="secondary"
					outlined
					class="p-button p-button-sm"
					@click="confirmDelete"
					:disabled="runningSessions.length <= 0"
					label="Delete kernel"
				/>
				<Button
					severity="secondary"
					outlined
					class="p-button p-button-sm"
					@click="confirmDeleteAll"
					:disabled="runningSessions.length <= 0"
					label="Delete all"
				/>
				<Button
					severity="secondary"
					outlined
					class="p-button p-button-sm"
					@click="confirmReconnect"
					:disabled="runningSessions.length <= 0"
					label="Reconnect"
				/>
			</div>

			<!-- Reset kernel -->
			<div class="toolbar-right">
				<Dropdown
					:model-value="selectedLanguage"
					placeholder="Select a language"
					:options="languages"
					option-disabled="disabled"
					option-label="name"
					option-value="value"
					class="language-dropdown"
					:disabled="kernelStatus !== 'idle'"
					@change="onChangeLanguage"
				/>
				<Button
					label="Reset kernel"
					severity="secondary"
					outlined
					icon="pi pi-replay"
					class="p-button p-button-sm pr-4"
					@click="confirmReset"
				/>
			</div>
		</div>
		<div class="notebook-container">
			<!-- Re-run message banner -->
			<div v-if="showRerunMessage" class="rerun-message">
				Re-run all the cells to restore the context if you need to make any changes or use them downstream.
				<Button icon="pi pi-times" text rounded aria-label="Close" @click="showRerunMessage = false" />
			</div>

			<!-- Data context description -->
			<div class="context-description">
				<ul>
					<li v-for="(data, idx) in dataContextDescription" :key="idx">{{ data }}</li>
				</ul>
			</div>

			<!-- Jupyter Chat -->
			<tera-jupyter-chat
				ref="chat"
				:show-jupyter-settings="true"
				:show-chat-thoughts="props.showChatThoughts"
				:jupyter-session="jupyterSession"
				:kernel-status="kernelStatus"
				:language="selectedLanguage"
				:default-preview="defaultPreview"
				@update-kernel-state="(e) => emit('update-kernel-state', e)"
				@update-kernel-status="updateKernelStatus"
				@new-dataset-saved="onNewDatasetSaved"
				@download-response="onDownloadResponse"
				@update-language="(lang) => emit('update-language', lang)"
				@update-selected-outputs="(outputs) => emit('update-selected-outputs', outputs)"
				:notebook-session="props.notebookSession"
			/>
		</div>

		<!-- Save as dialog -->
		<tera-modal v-if="showSaveInput" class="w-4">
			<template #header>
				<h4>Save as</h4>
			</template>
			<tera-input-text
				id="name"
				v-model="saveAsName"
				placeholder="What do you want to call it?"
				auto-focus
				@keyup.enter="saveAsNewDataset()"
			/>
			<template #footer>
				<Button label="Cancel" @click="showSaveInput = false" severity="secondary" outlined />
				<Button label="Save" :disabled="!hasValidDatasetName" @click="saveAsNewDataset()" />
			</template>
		</tera-modal>
	</div>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, Ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { useToastService } from '@/services/toast';
import { IModel } from '@jupyterlab/services/lib/session/session';
import type { CsvAsset, NotebookSession } from '@/types/Types';
import { AssetType } from '@/types/Types';
import TeraJupyterChat from '@/components/llm/tera-jupyter-chat.vue';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import {
	createMessageId,
	getServerSettings,
	getSessionManager,
	JupyterMessage,
	KernelState,
	newSession
} from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import Dropdown from 'primevue/dropdown';
import { shutdownKernel } from '@jupyterlab/services/lib/kernel/restapi';
import { useConfirm } from 'primevue/useconfirm';
import { useProjects } from '@/composables/project';
import { programmingLanguageOptions } from '@/types/common';
import TeraBeakerInput from '@/components/llm/tera-beaker-input.vue';
import { isEmpty } from 'lodash';

const openDialog = () => {
	showSaveInput.value = true;
};

defineExpose({
	openDialog
});
const jupyterSession: SessionContext = await newSession('beaker_kernel', 'Beaker Kernel');
const selectedKernel = ref();
const runningSessions = ref<any[]>([]);

// This is used to inform the user what is in the context.
const dataContextDescription = computed(() => props.assets.map((asset, index) => `#d${index + 1} = ${asset.name}\n`));

const defaultPreview = ref<string>('d1');
const confirm = useConfirm();

const props = defineProps<{
	assets: { id: string; type: string; name: string }[];
	showKernels: boolean;
	showChatThoughts: boolean;
	notebookSession?: NotebookSession;
	programmingLanguage?: string;
	kernelState: any;
	selectedDataset: string | null;
	sampleAgentQuestions: string[];
}>();

const showRerunMessage = ref(false);
const languages = programmingLanguageOptions();
const selectedLanguage = computed(() => props.programmingLanguage || languages[0].value);

const emit = defineEmits(['new-dataset-saved', 'update-language', 'update-kernel-state', 'update-selected-outputs']);

const chat = ref();
const kernelStatus = ref(<string>'');

const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);
const showSaveInput = ref(<boolean>false);
const saveAsName = ref('');
const toast = useToastService();

const updateKernelStatus = (statusString: string) => {
	kernelStatus.value = statusString;
};

const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const statusStyle = computed(() => {
	if (kernelStatus.value === 'idle') {
		return 'color: green';
	}

	if (kernelStatus.value === 'busy') {
		return 'color: orange';
	}

	return 'color: red';
});

const setKernelContext = (kernel: IKernelConnection, context_info) => {
	const messageBody = {
		session: jupyterSession.session?.name || '',
		channel: 'shell',
		content: context_info,
		msgType: 'context_setup_request',
		msgId: 'tgpt-context_setup_request'
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);
};

// FIXME: this is a bit fragile, the output is meant to match the terms used in askem-beaker
// and not necessarily asset type enums
const toAssetType = (t: string) => {
	if (t.endsWith('Id')) {
		return t.substring(0, t.length - 2);
	}
	throw new Error(`Cannot convert type ${t}`);
};

jupyterSession.kernelChanged.connect((_context, kernelInfo) => {
	const kernel = kernelInfo.newValue;

	const contextInfo: any = {};
	props.assets.forEach((asset, i) => {
		const key = `d${i + 1}`;
		contextInfo[key] = {
			id: asset.id,
			asset_type: toAssetType(asset.type)
		};
	});
	if (kernel?.name === 'beaker_kernel') {
		setKernelContext(kernel as IKernelConnection, {
			context: 'dataset',
			language: selectedLanguage.value,
			context_info: contextInfo
		});
	}
});

const onChangeLanguage = (val) => {
	confirm.require({
		message:
			'Are you sure you want to change the language? Changing the language will reset the kernel to its starting state.',
		header: 'Change language',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			const session = jupyterSession.session;
			const kernel = session?.kernel as IKernelConnection;

			const contextInfo: any = {};
			props.assets.forEach((asset, i) => {
				const key = `d${i + 1}`;
				contextInfo[key] = {
					id: asset.id,
					asset_type: toAssetType(asset.type)
				};
			});
			const messageBody = {
				session: session?.name || '',
				channel: 'shell',
				content: {
					context: 'dataset',
					language: val.value,
					context_info: contextInfo
				},
				msgType: 'context_setup_request',
				msgId: createMessageId('context_setup_request')
			};
			const message: JupyterMessage = createMessage(messageBody);
			kernel.sendJupyterMessage(message);
		}
	});
};

watch(
	() => [jupyterCsv.value?.csv],
	() => {
		if (jupyterCsv.value?.csv) {
			oldCsvHeaders.value = newCsvHeader.value;
			newCsvContent.value = jupyterCsv.value.csv.slice(1, jupyterCsv.value.csv.length);
			newCsvHeader.value = jupyterCsv.value.headers;
		}
	}
);

onMounted(() => {
	// for admin panel

	jupyterSession.ready.then(() => {
		if (jupyterSession.session) {
			const sessions = getSessionManager().running();
			const results: IModel[] = [];
			let result = sessions.next();
			while (result) {
				results.push(result);
				result = sessions.next();
			}
			runningSessions.value = results.reverse().map((r) => ({ kernelId: r.kernel?.id, value: r.id }));
			selectedKernel.value = {
				kernelId: jupyterSession.session?.kernel?.id,
				value: jupyterSession.session?.id
			};
			// Show rerun message if there are any cells that have been executed
			if (props.notebookSession?.data.history?.some((historyItem) => !isEmpty(historyItem.executions))) {
				showRerunMessage.value = true;
			}
		}
	});
});

onUnmounted(() => {
	jupyterSession.shutdown();
});

// TODO: Integrate tera-save-asset-modal.vue instead of doing this
// There is no clear way to save a csv dataset unless we do it using jupyter like we have now, once we figure out how to save using createDataset we should move this
// Save file function
const saveAsNewDataset = async () => {
	if (!hasValidDatasetName.value || saveAsName.value === null) {
		return;
	}
	const datasetName = saveAsName.value;
	// TODO: Fix this so that the dataset is created through hmi-server. Currently this is breaking because hmi-server is converting the
	// data_type property on the columns to upper case.

	// Code for creating the dataset via the hmi-service
	// const filename = 'dataset.csv'
	// let newDataset = cloneDeep(props.dataset);
	// delete newDataset['id'];
	// newDataset.name = datasetName;
	// newDataset.description = (newDataset.description ? newDataset.description : "") +
	// 		`\nTransformed from dataset '${props.dataset.name}' (${props.dataset.id}) at ${new Date().toUTCString()}`;
	// newDataset.fileNames = [filename];
	// const result = await createNewDataset(newDataset);

	const session = jupyterSession.session;
	const kernel = session?.kernel as IKernelConnection;
	const messageBody = {
		session: session?.name || '',
		channel: 'shell',
		content: {
			parent_dataset_id: String(props.assets[0].id),
			name: datasetName,
			var_name: props.selectedDataset
		},
		msgType: 'save_dataset_request',
		msgId: createMessageId('save_dataset_request')
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);

	// Close the modal when the dataset is saved
	showSaveInput.value = false;
};

const resetKernel = async () => {
	const session = jupyterSession.session;
	const kernel = session?.kernel as IKernelConnection;

	chat.value.clearOutputs();
	await session?.changeKernel({ name: kernel.name });
};

const killKernel = () => {
	shutdownKernel(selectedKernel.value.kernelId, getServerSettings());
	updateKernelList();
};

const deleteAllKernels = () => {
	runningSessions.value.forEach((k) => {
		shutdownKernel(k.kernelId, getServerSettings());
	});
	updateKernelList();
};

const confirmReset = () => {
	confirm.require({
		message: `Are you sure you want to reset the kernel?

This will reset the kernel back to its starting state, but keep all of your prompts and code cells.
The code cells will need to be rerun.`,
		header: 'Reset kernel',
		accept: () => {
			resetKernel();
		}
	});
};

// Kernel Confirmation dialogs
const confirmReconnect = () => {
	confirm.require({
		message: `Are you sure you want to terminate ${runningSessions.value.length} ?`,
		header: 'Confirmation',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			deleteAllKernels();
		}
	});
};

const confirmDeleteAll = () => {
	confirm.require({
		message: `Are you sure you want to terminate ${runningSessions.value.length} sessions?`,
		header: 'Terminate all kernels',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			deleteAllKernels();
		}
	});
};

const confirmDelete = () => {
	confirm.require({
		message: `Are you sure you want to terminate session ${runningSessions.value.length}?`,
		header: 'Terminate kernel',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			killKernel();
		}
	});
};

// eslint-disable-next-line vue/return-in-computed-property
const updateKernelList = () => {
	jupyterSession.ready.then(() => {
		if (jupyterSession.session) {
			const sessions = getSessionManager().running();
			const results: IModel[] = [];
			let result = sessions.next();
			while (result) {
				result = sessions.next();
			}
			runningSessions.value = results.reverse().map((r) => ({ kernelId: r.kernel?.id, value: r.id }));
			selectedKernel.value = {
				kernelId: jupyterSession.session?.kernel?.id,
				value: jupyterSession.session?.id
			};
		}
	});
};

const onNewDatasetSaved = async (payload) => {
	if (!useProjects().activeProject.value) {
		toast.error('Unable to save dataset', "Can't find active an project");
		return;
	}
	const datasetId = payload.dataset_id;
	await useProjects().addAsset(AssetType.Dataset, datasetId);
	emit('new-dataset-saved', { id: datasetId, name: saveAsName.value });
	toast.success('Dataset saved successfully', 'Refresh to see the dataset in the resource explorer');
};

const onSubmitQuery = (question: string) => {
	if (chat.value) {
		chat.value.submitQuery(question);
	}
};

const onAddCodeCell = () => {
	if (chat.value) {
		chat.value.addCodeCell();
	}
};

const onRunAllCells = () => {
	confirm.require({
		message: 'Are you sure you want to rerun all cells?',
		header: 'Rerun all cells',
		accept: () => {
			const event = new Event('run-all-cells');
			window.dispatchEvent(event);
			showRerunMessage.value = false;
		}
	});
};

/* Download dataset feature has been removed, but keeping this code here in case it returns */
// const downloadDataset = () => {
// 	const session = jupyterSession.session;
// 	const kernel = session?.kernel as IKernelConnection;
// 	const messageBody = {
// 		session: session?.name || '',
// 		channel: 'shell',
// 		content: {
// 			var_name: actionTarget.value
// 		},
// 		msgType: 'download_dataset_request',
// 		msgId: createMessageId('download_dataset_request')
// 	};
// 	const message: JupyterMessage = createMessage(messageBody);
// 	kernel?.sendJupyterMessage(message);
// };

const onDownloadResponse = (payload) => {
	const data = payload.data;
	const fileContents = data.map(atob).join('');

	// Hackish way to download the file contents from within the HMI
	const el = document.createElement('a');
	el.setAttribute('href', `data:text/plain;charset=utf8,${encodeURIComponent(fileContents)}`);
	el.setAttribute('download', 'dataset.csv');
	document.body.appendChild(el);
	el.click();
	document.body.removeChild(el);
};
</script>

<style scoped>
.context-description {
	background-color: var(--surface-100);
	width: 100%;
	width: fill-available;
	font-family: var(--font-family);
	font-feature-settings: 'tnum';
	border: none;
	margin: 0;
	padding: var(--gap-4) var(--gap-12) 0;
}

.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}
.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
	border-radius: var(--border-radius);
	border: 4px solid var(--primary-color);
	border-width: 0px 0px 0px 6px;
}
.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
}
.metadata {
	margin: 1rem;
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
}

.metadata > section {
	flex: 1;
}

.kernel-status-container {
	display: flex;
	flex-direction: row;
	align-items: top;
	flex-wrap: nowrap;
	white-space: nowrap;
	min-width: 5.25rem;
	padding-top: var(--gap-2-5);
}

.kernel-status {
	margin-right: 10px;
}

.save-button {
	margin-left: 10px;
}

.toolbar {
	display: flex;
	flex-direction: row;
	align-items: top;
	background-color: var(--surface-100);
	border-bottom: 1px solid var(--surface-border);
	position: sticky;
	top: 0;
	left: 0;
	z-index: 100;
	width: 100%;
	padding: var(--gap-2) var(--gap-4) var(--gap-2) 1.5rem;
	gap: var(--gap-2);
	box-shadow: 0 4px 6px -5px rgba(0, 0, 0, 0.2);
}
.toolbar:deep(.p-button .p-button-label) {
	font-weight: 500;
}
.toolbar-right {
	display: flex;
	flex-direction: row;
	align-items: top;
	gap: var(--gap-2);
	height: 32px; /* aligns with the height of the input buttons */
}

:deep(.language-dropdown span) {
	font-size: 12px;
	margin-left: var(--gap-1-5);
}
.tera-beaker-input {
	padding-top: 0;
}

.variables-table {
	display: grid;
	grid-template-columns: 1fr;
}

.variables-table div {
	padding: var(--gap-1);
}

.variables-header {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.variables-row {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	grid-template-rows: 1fr 1fr;
	border-top: 1px solid var(--surface-border);
}

.variables-row:hover {
	background-color: var(--surface-highlight);
}

.variables-description {
	grid-row: 2;
	grid-column: 1 / span 6;
	color: var(--text-color-subdued);
}

.rerun-message {
	position: sticky;
	top: 0;
	z-index: 1;
	display: flex;
	background-color: var(--surface-warning);
	justify-content: space-between;
	align-items: center;
	padding: var(--gap-2);
}

.notebook-container {
	height: calc(100% - 3.5rem);
	overflow-y: auto;
	background-color: var(--surface-100);
}
</style>
