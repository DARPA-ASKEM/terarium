<template>
	<div class="data-transform-container">
		<ConfirmDialog></ConfirmDialog>
		<!-- Jupyter Kernel Settings -->
		<div class="settings-title" v-if="showKernels">Kernel Settings</div>
		<div class="jupyter-settings" v-if="showKernels">
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
		<Accordion :multiple="true" :activeIndex="getActiveIndex">
			<AccordionTab>
				<template #header>
					Data Preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
				</template>
				<tera-dataset-datatable
					class="tera-dataset-datatable"
					v-if="jupyterCsv"
					:rows="10"
					:raw-content="jupyterCsv"
					:previous-headers="oldCsvHeaders"
				/>
				<div>
					<Button
						class="save-button p-button p-button-secondary p-button-sm"
						title="Saves the current version of df as a new Terarium asset"
						@click="showSaveInput = !showSaveInput"
					>
						<span class="pi pi-save p-button-icon p-button-icon-left"></span>
						<span class="p-button-text">Save as</span>
					</Button>
					<span v-if="showSaveInput" style="padding-left: 1em; padding-right: 2em">
						<InputText v-model="saveAsName" class="post-fix" placeholder="New dataset name" />
						<i
							class="pi pi-times i"
							:class="{ clear: hasValidDatasetName }"
							@click="saveAsName = ''"
						></i>
						<i
							class="pi pi-check i"
							:class="{ save: hasValidDatasetName }"
							@click="
								saveAsNewDataset();
								showSaveInput = false;
							"
						></i>
					</span>
					<Button
						class="save-button p-button p-button-secondary p-button-sm"
						title="Download the current version of df as a CSV file"
						@click="downloadDataset"
					>
						<span class="pi pi-download p-button-icon p-button-icon-left"></span>
						<span class="p-button-text">Download</span>
					</Button>
				</div>
			</AccordionTab>
		</Accordion>

		<div class="gpt-header">
			<span><i class="pi pi-circle-fill kernel-status" :style="statusStyle" /></span>
			<span><header id="GPT">TGPT</header></span>
		</div>
		<tera-jupyter-chat
			:project="props.project"
			:asset-id="props.assetId"
			:show-jupyter-settings="true"
			:show-chat-thoughts="props.showChatThoughts"
			:jupyter-session="jupyterSession"
			:kernel-status="kernelStatus"
			@update-table-preview="updateJupyterTable"
			@update-kernel-status="updateKernelStatus"
			@new-dataset-saved="onNewDatasetSaved"
			@download-response="onDownloadResponse"
			@is-typing="emit('is-typing')"
		/>
	</div>
</template>
<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted, Ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
// import { cloneDeep } from 'lodash';
import { useToastService } from '@/services/toast';
import { addAsset } from '@/services/project';
import { ProjectAssetTypes, IProject } from '@/types/Project';
import { IModel } from '@jupyterlab/services/lib/session/session';
import { CsvAsset, Dataset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraJupyterChat from '@/components/llm/tera-jupyter-chat.vue';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import {
	newSession,
	JupyterMessage,
	getServerSettings,
	getSessionManager
} from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import Dropdown from 'primevue/dropdown';
import { shutdownKernel } from '@jupyterlab/services/lib/kernel/restapi';
import ConfirmDialog from 'primevue/confirmdialog';
import { useConfirm } from 'primevue/useconfirm';

// import { createNewDataset } from '@/services/dataset';

// const jupyterSession = ref(<SessionContext>newSession('llmkernel', 'ChattyNode'));
const jupyterSession: SessionContext = await newSession('llmkernel', 'ChattyNode');
const selectedKernel = ref();
const runningSessions = ref<any[]>([]);

const confirm = useConfirm();

const props = defineProps<{
	assetId: string;
	project: IProject;
	dataset: Dataset;
	showKernels: boolean;
	showChatThoughts: boolean;
}>();

const kernelStatus = ref(<string>'');
const showKernels = ref(<boolean>false);
const emit = defineEmits(['is-typing']);

const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);
const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');
const toast = useToastService();

const updateKernelStatus = (statusString: string) => {
	kernelStatus.value = statusString;
};

const csvContent = computed(() => jupyterCsv.value?.csv);

const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const getActiveIndex = computed(() => {
	if (!jupyterCsv.value) {
		return [1];
	}
	return [0, 1];
});

const statusStyle = computed(() => {
	if (kernelStatus.value === 'idle') {
		return 'color: green';
	}

	if (kernelStatus.value === 'busy') {
		return 'color: orange';
	}

	return 'color: red';
});

const updateJupyterTable = (newJupyterCsv: CsvAsset) => {
	jupyterCsv.value = newJupyterCsv;
};

const setKernelContext = (kernel: IKernelConnection, context_info) => {
	const messageBody = {
		session: jupyterSession.session?.name || '',
		channel: 'shell',
		content: context_info,
		msgType: 'context_setup_request',
		msgId: `${kernel.id}-setcontext`
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);
};

jupyterSession.kernelChanged.connect((_context, kernelInfo) => {
	const kernel = kernelInfo.newValue;
	if (kernel?.name === 'llmkernel') {
		setKernelContext(kernel as IKernelConnection, {
			context: 'dataset',
			context_info: { id: props.assetId }
		});
	}
});

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

	// import { KernelConnection as JupyterKernelConnection } from '@/services/jupyter';
	const session = jupyterSession.session;
	const kernel = session?.kernel as IKernelConnection;
	const messageBody = {
		session: session?.name || '',
		channel: 'shell',
		content: {
			parent_dataset_id: String(props.assetId),
			name: datasetName
		},
		msgType: 'save_dataset_request',
		msgId: `${kernel?.id}-save_dataset_request`
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);
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

const onNewDatasetSaved = async (payload) => {
	const datasetId = payload.dataset_id;
	await addAsset(props.project.id, ProjectAssetTypes.DATASETS, datasetId);
	toast.success(
		'Dataset saved successfully',
		'Refresh to see the dataset in the resource explorer'
	);
};

const downloadDataset = () => {
	const session = jupyterSession.session;
	const kernel = session?.kernel as IKernelConnection;
	const messageBody = {
		session: session?.name || '',
		channel: 'shell',
		content: {},
		msgType: 'download_dataset_request',
		msgId: `${kernel?.id}-download_dataset_request`
	};
	const message: JupyterMessage = createMessage(messageBody);
	kernel?.sendJupyterMessage(message);
};

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

/* Datatable  */
.data-row > section > header {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.data-row > section > section:last-child {
	font-size: var(--font-body-small);
}

.feature-type {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

main .annotation-group {
	padding: 0.25rem;
	border: solid 1px var(--surface-border-light);
	background-color: var(--gray-50);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-bottom: 1rem;
	max-width: var(--constrain-width);
}

.annotation-subheader {
	font-weight: var(--font-weight-semibold);
}

.annotation-row {
	display: flex;
	flex-direction: row;
	gap: 3rem;
}

.tera-dataset-datatable {
	width: 100%;
}

.data-transform-container {
	display: flex;
	flex-direction: column;
	padding: 0.5rem;
	margin: 0.5rem;
	max-height: 90%;
}

.kernel-status {
	margin-right: 10px;
}

.save-button {
	margin-left: 10px;
}

.gpt-header {
	display: flex;
	width: 90%;
}

.variables-table {
	display: grid;
	grid-template-columns: 1fr;
}

.variables-table div {
	padding: 0.25rem;
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
</style>
