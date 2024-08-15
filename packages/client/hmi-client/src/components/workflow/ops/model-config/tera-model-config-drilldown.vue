<template>
	<tera-drilldown
		v-bind="$attrs"
		:node="node"
		:menu-items="menuItems"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<template #sidebar>
			<tera-slider-panel v-model:is-open="isSidebarOpen" header="Configurations" content-width="360px">
				<template #content>
					<div class="m-3">
						<div class="flex flex-column gap-1">
							<Button
								outlined
								icon="pi pi-plus"
								label="Extract from inputs"
								@click="extractConfigurationsFromInputs"
								severity="secondary"
								:loading="isLoading"
							/>

							<span class="flex gap-1">
								<Dropdown
									class="flex-1"
									v-model="selectedSortOption"
									:options="sortByOptions"
									option-label="label"
									option-value="value"
								>
									<template #value="{ value }">
										<label class="sort-by-label">Sort by</label
										>{{ sortByOptions.find((option) => option.value === value)?.label }}
									</template>
								</Dropdown>
								<Dropdown
									class="flex-1"
									v-model="selectedShownOption"
									:options="shownOptionsList"
									option-label="label"
									option-value="value"
								></Dropdown>
							</span>

							<tera-input-text v-model="filterModelConfigurationsText" placeholder="Filter" />
						</div>
						<ul v-if="!isLoading && model?.id">
							<li v-for="configuration in filteredModelConfigurations" :key="configuration.id">
								<tera-model-configuration-item
									:configuration="configuration"
									@click="onSelectConfiguration(configuration)"
									:selected="selectedConfigId === configuration.id"
									@use="onSelectConfiguration(configuration)"
									@delete="fetchConfigurations(model.id)"
									@download="downloadConfiguredModel(configuration)"
								/>
							</li>
						</ul>
						<tera-progress-spinner is-centered :font-size="2" v-if="isLoading" />
					</div>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="ConfigTabs.Wizard" class="pl-3">
			<template #header-controls-left>
				<tera-toggleable-input
					v-if="knobs.transientModelConfig.name"
					:model-value="knobs.transientModelConfig.name"
					tag="h4"
				/>
			</template>
			<template #header-controls-right>
				<Button label="Reset" @click="resetConfiguration" outlined severity="secondary" />
				<Button class="mr-3" :disabled="isSaveDisabled" label="Save" @click="() => createConfiguration()" />
			</template>

			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab>
					<template #header>
						Description
						<Button v-if="!isEditingDescription" icon="pi pi-pencil" text @click.stop="onEditDescription" />
						<template v-else>
							<Button icon="pi pi-times" text @click.stop="isEditingDescription = false" />
							<Button icon="pi pi-check" text @click.stop="onConfirmEditDescription" />
						</template>
					</template>
					<p class="description text" v-if="!isEditingDescription">
						{{ knobs.transientModelConfig.description }}
					</p>
					<Textarea v-else class="context-item" placeholder="Enter a description" v-model="newDescription" />
				</AccordionTab>
				<AccordionTab header="Diagram">
					<tera-model-diagram v-if="model" :model="model" />
				</AccordionTab>
			</Accordion>
			<template v-if="model">
				<Message v-if="isModelMissingMetadata(model)" class="m-2">
					Some metadata is missing from these values. This information can be added manually to the attached model.
				</Message>
				<template v-if="!isEmpty(knobs.transientModelConfig)">
					<tera-initial-table
						v-if="!isEmpty(mmt.initials)"
						:model="model"
						:model-configuration="knobs.transientModelConfig"
						:modelConfigurations="filteredModelConfigurations"
						:mmt="mmt"
						:mmt-params="mmtParams"
						@update-expression="setInitialExpression(knobs.transientModelConfig, $event.id, $event.value)"
						@update-source="setInitialSource(knobs.transientModelConfig, $event.id, $event.value)"
					/>
					<tera-parameter-table
						v-if="!isEmpty(mmt.parameters)"
						:model="model"
						:model-configuration="knobs.transientModelConfig"
						:modelConfigurations="filteredModelConfigurations"
						:mmt="mmt"
						:mmt-params="mmtParams"
						@update-parameters="setParameterDistributions(knobs.transientModelConfig, $event)"
						@update-source="setParameterSource(knobs.transientModelConfig, $event.id, $event.value)"
					/>
					<Accordion :active-index="0" v-if="!isEmpty(calibratedConfigObservables)">
						<AccordionTab header="Observables">
							<tera-observables
								class="pl-4"
								:model="model"
								:mmt="mmt"
								:observables="calibratedConfigObservables"
								:feature-config="{ isPreview: true }"
							/>
						</AccordionTab>
					</Accordion>
				</template>
			</template>
		</tera-drilldown-section>
		<tera-columnar-panel :tabName="ConfigTabs.Notebook">
			<tera-drilldown-section id="notebook-section">
				<div class="toolbar-right-side"></div>
				<div class="toolbar">
					<Suspense>
						<tera-notebook-jupyter-input
							:kernel-manager="kernelManager"
							:defaultOptions="sampleAgentQuestions"
							:context-language="contextLanguage"
							@llm-output="(data: any) => appendCode(data, 'code')"
							@llm-thought-output="(data: any) => llmThoughts.push(data)"
							@question-asked="llmThoughts = []"
						>
							<template #toolbar-right-side>
								<tera-input-text v-model="knobs.transientModelConfig.name" placeholder="Configuration Name" />
								<Button icon="pi pi-play" label="Run" @click="runFromCode" />
							</template>
						</tera-notebook-jupyter-input>
					</Suspense>
					<tera-notebook-jupyter-thought-output :llm-thoughts="llmThoughts" />
				</div>
				<v-ace-editor
					v-model:value="codeText"
					@init="initializeEditor"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>
			</tera-drilldown-section>
			<tera-drilldown-preview title="Output Preview">
				<tera-notebook-error
					v-if="executeResponse.status === OperatorStatus.ERROR"
					:name="executeResponse.name"
					:value="executeResponse.value"
					:traceback="executeResponse.traceback"
				/>
				<div v-if="executeResponse.status !== OperatorStatus.ERROR">{{ notebookResponse }}</div>
			</tera-drilldown-preview>
		</tera-columnar-panel>
	</tera-drilldown>
	<tera-modal v-if="sanityCheckErrors.length > 0">
		<template #header>
			<h4>Warning, these settings may cause errors</h4>
		</template>
		<template #default>
			<section style="max-height: 22rem; overflow-y: scroll">
				<div v-for="(errString, idx) of sanityCheckErrors" :key="idx">
					{{ errString }}
				</div>
			</section>
		</template>
		<template #footer>
			<Button label="Ok" class="p-button-primary" @click="sanityCheckErrors = []" />
			<Button
				label="Ignore warnings and use configuration"
				class="p-button-secondary"
				@click="() => createConfiguration()"
			/>
		</template>
	</tera-modal>

	<!-- Matrix effect easter egg  -->
	<canvas id="matrix-canvas" />
</template>

<script setup lang="ts">
import '@/ace-config';
import { cloneDeep, isEmpty, orderBy } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Textarea from 'primevue/textarea';
import { computed, onUnmounted, ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { useClientEvent } from '@/composables/useClientEvent';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import { emptyMiraModel, generateModelDatasetConfigurationContext } from '@/model-representation/mira/mira';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { configureModelFromDatasets, configureModelFromDocument } from '@/services/goLLM';
import { KernelSessionManager } from '@/services/jupyter';
import { getMMT, getModel, getModelConfigurationsForModel } from '@/services/model';
import {
	createModelConfiguration,
	setInitialSource,
	setInitialExpression,
	setParameterSource,
	setParameterDistributions,
	getAsConfiguredModel
} from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import type { Model, ModelConfiguration, TaskResponse, ClientEvent } from '@/types/Types';
import { ClientEventType, Observable, TaskStatus } from '@/types/Types';
import type { WorkflowNode } from '@/types/workflow';
import { OperatorStatus } from '@/types/workflow';
import { logger } from '@/utils/logger';
import { isModelMissingMetadata } from '@/model-representation/service';
import Message from 'primevue/message';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import { useConfirm } from 'primevue/useconfirm';
import Dropdown from 'primevue/dropdown';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import TeraModelConfigurationItem from './tera-model-configuration-item.vue';
import { ModelConfigOperation, ModelConfigOperationState, blankModelConfig } from './model-config-operation';

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();

const isSidebarOpen = ref(true);
const isEditingDescription = ref(false);
const newDescription = ref('');

const menuItems = computed(() => [
	{
		label: 'Download',
		icon: 'pi pi-download',
		disabled: isSaveDisabled.value,
		command: () => {
			downloadConfiguredModel();
		}
	}
]);

const emit = defineEmits(['append-output', 'update-state', 'select-output', 'close']);

interface BasicKnobs {
	transientModelConfig: ModelConfiguration;
}

const knobs = ref<BasicKnobs>({
	transientModelConfig: blankModelConfig
});

const calibratedConfigObservables = computed<Observable[]>(() =>
	knobs.value.transientModelConfig.observableSemanticList.map(({ referenceId, states, expression }) => ({
		id: referenceId,
		name: referenceId,
		states,
		expression
	}))
);

const sanityCheckErrors = ref<string[]>([]);
const isSaveDisabled = computed(() => knobs.value.transientModelConfig.name === '');

const kernelManager = new KernelSessionManager();
let editor: VAceEditorInstance['_editor'] | null;
const buildJupyterContext = () => {
	const contextId = selectedConfigId.value;
	if (!model.value) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}
	return {
		context: 'model_configuration',
		language: 'python3',
		context_info: {
			id: contextId
		}
	};
};
const codeText = ref('# This environment contains the variable "model_config" to be read and updated');
const llmThoughts = ref<any[]>([]);
const notebookResponse = ref();
const executeResponse = ref({
	status: OperatorStatus.DEFAULT,
	name: '',
	value: '',
	traceback: ''
});
const sampleAgentQuestions = [
	'What are the current parameters values?',
	'Update parameter gamma to constant 0.13',
	'Update parameter beta to a uniform distribution with max 0.5 and min 0.2'
];
const contextLanguage = ref<string>('python3');

const appendCode = (data: any, property: string, runUpdatedCode = false) => {
	codeText.value = codeText.value.concat(' \n', data.content[property] as string);
	if (runUpdatedCode) runFromCode();
};

const confirm = useConfirm();
const filterModelConfigurationsText = ref('');
const filteredModelConfigurations = computed(() => {
	const searchTerm = filterModelConfigurationsText.value.toLowerCase();
	const filteredConfigurations = suggestedConfigurationContext.value.tableData.filter(
		(config) =>
			config.name?.toLowerCase().includes(searchTerm) || config.description?.toLowerCase().includes(searchTerm)
	);

	return orderBy(filteredConfigurations, [selectedSortOption.value], ['desc']);
});

const selectedSortOption = ref('createdOn');
const sortByOptions = [{ label: 'Created On', value: 'createdOn' }];

const selectedShownOption = ref('all');
const shownOptionsList = [{ label: 'Show all', value: 'all' }];

const runFromCode = () => {
	const code = editor?.getValue();
	if (!code) return;

	const messageContent = {
		silent: false,
		store_history: false,
		code
	};

	let executedCode = '';

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('execute_input', (data) => {
			executedCode = data.content.code;
		})
		.register('stream', (data) => {
			notebookResponse.value = data.content.text;
		})
		.register('model_configuration_preview', (data) => {
			if (!data.content) return;
			knobs.value.transientModelConfig = data.content;

			if (executedCode) {
				saveCodeToState(executedCode, true);
			}
		})
		.register('any_execute_reply', (data) => {
			let status = OperatorStatus.DEFAULT;
			if (data.msg.content.status === 'ok') status = OperatorStatus.SUCCESS;
			if (data.msg.content.status === 'error') status = OperatorStatus.ERROR;
			executeResponse.value = {
				status,
				name: data.msg.content.ename ? data.msg.content.ename : '',
				value: data.msg.content.evalue ? data.msg.content.evalue : '',
				traceback: data.msg.content.traceback ? data.msg.content.traceback : ''
			};
		});
};

// FIXME: Copy pasted in 3 locations, could be written cleaner and in a service
const saveCodeToState = (code: string, hasCodeBeenRun: boolean) => {
	const state = cloneDeep(props.node.state);
	state.hasCodeBeenRun = hasCodeBeenRun;

	// for now only save the last code executed, may want to save all code executed in the future
	const codeHistoryLength = props.node.state.modelEditCodeHistory.length;
	const timestamp = Date.now();
	if (codeHistoryLength > 0) {
		state.modelEditCodeHistory[0] = { code, timestamp };
	} else {
		state.modelEditCodeHistory.push({ code, timestamp });
	}
	emit('update-state', state);
};

const initializeEditor = (editorInstance: any) => {
	editor = editorInstance;
};

const extractConfigurationsFromInputs = async () => {
	if (!model.value?.id) {
		return;
	}
	if (documentId.value) {
		const resp = await configureModelFromDocument(
			documentId.value,
			model.value.id,
			props.node.workflowId,
			props.node.id
		);
		documentModelConfigTaskId.value = resp.id;
	}
	if (datasetIds.value) {
		const matrixStr = generateModelDatasetConfigurationContext(mmt.value, mmtParams.value);
		const resp = await configureModelFromDatasets(
			model.value.id,
			datasetIds.value,
			matrixStr,
			props.node.workflowId,
			props.node.id
		);
		datasetModelConfigTaskId.value = resp.id;
	}
};

const configModelEventHandler = async (event: ClientEvent<TaskResponse>) => {
	const taskIdRefs = {
		[ClientEventType.TaskGollmConfigureModel]: documentModelConfigTaskId,
		[ClientEventType.TaskGollmConfigureFromDataset]: datasetModelConfigTaskId
	};
	if (event.data?.id !== taskIdRefs[event.type].value) return;
	if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
		taskIdRefs[event.type].value = '';
	}
	if (event.data.status === TaskStatus.Success && model.value?.id) await fetchConfigurations(model.value.id);
};

useClientEvent(ClientEventType.TaskGollmConfigureModel, configModelEventHandler);
useClientEvent(ClientEventType.TaskGollmConfigureFromDataset, configModelEventHandler);

const selectedOutputId = ref<string>('');
const selectedConfigId = computed(() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]);

const documentId = computed(() => props.node.inputs[1]?.value?.[0]?.documentId);
const datasetIds = computed(() => props.node.inputs[2]?.value);

const suggestedConfigurationContext = ref<{
	isOpen: boolean;
	tableData: ModelConfiguration[];
	modelConfiguration: ModelConfiguration | null;
}>({
	isOpen: false,
	tableData: [],
	modelConfiguration: null
});
const isFetching = ref(false);
const documentModelConfigTaskId = ref('');
const datasetModelConfigTaskId = ref('');
const isLoading = computed(
	() => documentModelConfigTaskId.value !== '' || datasetModelConfigTaskId.value !== '' || isFetching.value
);

const model = ref<Model | null>(null);
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

const downloadConfiguredModel = async (configuration: ModelConfiguration = knobs.value.transientModelConfig) => {
	const rawModel = await getAsConfiguredModel(configuration);
	if (rawModel) {
		const data = `text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(rawModel, null, 2))}`;
		const a = document.createElement('a');
		a.href = `data:${data}`;
		a.download = `${configuration.name ?? 'configured_model'}.json`;
		a.innerHTML = 'download JSON';
		a.click();
		a.remove();
	}
};

const createConfiguration = async () => {
	if (!model.value || isSaveDisabled.value) return;

	const state = cloneDeep(props.node.state);

	const modelConfig = cloneDeep(knobs.value.transientModelConfig);

	const data = await createModelConfiguration(modelConfig);

	if (!data) {
		logger.error('Failed to create model configuration');
		return;
	}

	knobs.value.transientModelConfig = cloneDeep(data);
	state.transientModelConfig = knobs.value.transientModelConfig;
	useToastService().success('', 'Created model configuration');
	emit('append-output', {
		type: ModelConfigOperation.outputs[0].type,
		label: state.transientModelConfig.name,
		value: data.id,
		isSelected: false,
		state
	});
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

const fetchConfigurations = async (modelId: string) => {
	isFetching.value = true;
	suggestedConfigurationContext.value.tableData = await getModelConfigurationsForModel(modelId);
	isFetching.value = false;
};

// Fill the form with the config data
const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;
	await fetchConfigurations(modelId);

	model.value = await getModel(modelId);

	if (!state.transientModelConfig.id) {
		// Apply a configuration if one hasn't been applied yet
		applyConfigValues(suggestedConfigurationContext.value.tableData[0]);
	} else {
		knobs.value.transientModelConfig = cloneDeep(state.transientModelConfig);
	}

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shut down first
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const onSelectConfiguration = (config: ModelConfiguration) => {
	confirm.require({
		header: 'Are you sure you want to select this configuration?',
		message: `This will apply the configuration "${config.name}" to the model.  All current values will be replaced.`,
		accept: () => {
			applyConfigValues(config);
		},
		acceptLabel: 'Confirm',
		rejectLabel: 'Cancel'
	});
};

const applyConfigValues = (config: ModelConfiguration) => {
	const state = cloneDeep(props.node.state);
	knobs.value.transientModelConfig = cloneDeep(config);

	// Update output port:
	if (!config.id) {
		logger.error('Model configuration not found');
		return;
	}
	const listOfConfigIds: string[] = props.node.outputs.map((output) => output.value?.[0]);
	// Check if this output already exists
	if (listOfConfigIds.includes(config.id)) {
		// Select the existing output
		const output = props.node.outputs.find((ele) => ele.value?.[0] === config.id);
		emit('select-output', output?.id);
	}
	// If the output does not already exist
	else {
		// Append this config to the output.
		state.transientModelConfig = knobs.value.transientModelConfig;
		emit('append-output', {
			type: ModelConfigOperation.outputs[0].type,
			label: config.name,
			value: config.id,
			isSelected: false,
			state
		});
	}
	logger.success(`Configuration applied ${config.name}`);
};

const onEditDescription = () => {
	isEditingDescription.value = true;
	newDescription.value = knobs.value.transientModelConfig.description ?? '';
};

const onConfirmEditDescription = () => {
	knobs.value.transientModelConfig.description = newDescription.value;
	isEditingDescription.value = false;
};

const resetConfiguration = () => {
	confirm.require({
		header: 'Are you sure you want to reset the configuration?',
		message: 'This will reset all values original values of the configuration.',
		accept: () => {
			const originalConfig = suggestedConfigurationContext.value.tableData.find((c) => c.id === selectedConfigId.value);
			if (originalConfig) {
				applyConfigValues(originalConfig);
			}
		},
		acceptLabel: 'Confirm',
		rejectLabel: 'Cancel'
	});
};

watch(
	() => model.value,
	async () => {
		if (!model.value) return;
		const response: any = await getMMT(model.value);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	},
	{ immediate: true, deep: true }
);

watch(
	() => knobs.value,
	async () => {
		const state = cloneDeep(props.node.state);
		state.transientModelConfig = knobs.value.transientModelConfig;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.active,
	() => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			initialize();
		}
	},
	{ immediate: true }
);

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
:deep(.p-datatable-loading-overlay.p-component-overlay) {
	background-color: var(--surface-section);
}

.p-datatable.p-datatable-sm :deep(.p-datatable-tbody > tr > td) {
	padding: 0;
}
.context-item {
	width: 100%;
}

:deep(.p-button:disabled.p-button-outlined) {
	background-color: var(--surface-0) !important;
}

:deep(.p-accordion-content) {
	padding-bottom: var(--gap-2);
}

#notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
}

.toolbar-right-side {
	position: absolute;
	top: var(--gap);
	right: 0;
	gap: var(--gap-small);
	display: flex;
	align-items: center;
}

.toolbar {
	padding-left: var(--gap-medium);
}

#matrix-canvas {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1000;
	pointer-events: none;
	mix-blend-mode: darken;
	opacity: 1;
	transition: opacity 1s;
}

.sort-by-label {
	color: var(--text-color-subdued);
	padding-right: var(--gap-small);
}

ul {
	list-style: none;
	padding-top: var(--gap-small);
}
</style>
