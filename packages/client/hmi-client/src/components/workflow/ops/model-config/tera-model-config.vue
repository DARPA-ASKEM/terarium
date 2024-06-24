<template>
	<tera-drilldown
		:node="node"
		:menu-items="menuItems"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section class="pl-3 pr-3">
				<template #header-controls>
					<Button
						size="small"
						:disabled="isSaveDisabled"
						label="Run"
						icon="pi pi-play"
						@click="createConfiguration()"
					/>
				</template>
				<template #header-controls-right>
					<Button
						class="mr-3"
						:disabled="isSaveDisabled"
						label="Save"
						@click="() => createConfiguration()"
					/>
				</template>
				<!-- Suggested configurations -->
				<div class="box-container mr-2" v-if="model">
					<Accordion multiple :active-index="[0]">
						<AccordionTab>
							<template #header>
								Suggested configurations
								<span v-if="suggestedConfigurationContext.tableData" class="artifact-amount">
									({{ suggestedConfigurationContext.tableData.length }})
								</span>
								<Button
									class="ml-auto"
									icon="pi pi-sign-out"
									label="Extract configurations from inputs"
									outlined
									severity="secondary"
									:loading="isLoading"
									@click.stop="extractConfigurationsFromInputs"
								/>
							</template>
							<DataTable
								v-if="suggestedConfigurationContext.tableData"
								:value="suggestedConfigurationContext.tableData"
								size="small"
								data-key="id"
								:paginator="suggestedConfigurationContext.tableData.length > 5"
								:rows="5"
								sort-field="createdOn"
								:sort-order="-1"
								:loading="isLoading"
							>
								<Column field="name" header="Name" style="width: 15%">
									<template #body="{ data }">
										<Button :label="data.name" text @click="onOpenSuggestedConfiguration(data)" />
									</template>
								</Column>
								<Column field="description" header="Description" style="width: 30%" />
								<Column field="createdOn" header="Created On" :sortable="true" style="width: 25%">
									<template #body="{ data }">
										{{ formatTimestamp(data?.createdOn) }}
									</template>
								</Column>
								<Column header="Source" style="width: 30%">
									<template #body="{ data }">
										{{ data?.configuration?.metadata?.source?.join(',') || '--' }}
									</template>
								</Column>
								<Column style="width: 7rem">
									<template #body="{ data }">
										<Button
											class="use-button"
											label="Apply configuration values"
											@click="applyConfigValues(data)"
											text
										/>
									</template>
								</Column>
								<template #loading>
									<tera-progress-spinner :font-size="2" is-centered
										>Fetching suggested configurations...</tera-progress-spinner
									>
								</template>
								<template #empty>
									<p class="empty-section m-3">No configurations found.</p>
								</template>
							</DataTable>
						</AccordionTab>
					</Accordion>
				</div>
				<Accordion multiple :active-index="[0, 1, 2, 3]">
					<AccordionTab header="Context">
						<p class="text-sm mb-1">Name</p>
						<InputText
							class="context-item"
							placeholder="Enter a name for this configuration"
							v-model="knobs.transientModelConfig.name"
						/>
						<p class="text-sm mb-1 mt-3">Description</p>
						<Textarea
							class="context-item"
							placeholder="Enter a description"
							v-model="knobs.transientModelConfig.description"
						/>
					</AccordionTab>
					<AccordionTab header="Diagram">
						<tera-model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
				</Accordion>
				<Message v-if="model && isModelMissingMetadata(model)" class="m-2"
					>Some metadata is missing from these values. This information can be added manually to the
					attached model.</Message
				>
				<Accordion multiple :active-index="[0]">
					<AccordionTab>
						<template #header>
							Initial variable values<span class="artifact-amount">({{ numInitials }})</span>
						</template>
						<tera-initial-table
							v-if="!isEmpty(knobs.transientModelConfig) && !isEmpty(mmt.initials) && model"
							:model="model"
							:model-configuration="knobs.transientModelConfig"
							:mmt="mmt"
							:mmt-params="mmtParams"
							@update-expression="
								setInitialExpression(knobs.transientModelConfig, $event.id, $event.value)
							"
							@update-source="setInitialSource(knobs.transientModelConfig, $event.id, $event.value)"
						/>
					</AccordionTab>
				</Accordion>
				<tera-parameter-table
					v-if="!isEmpty(knobs.transientModelConfig) && !isEmpty(mmt.parameters) && model"
					:model="model"
					:model-configuration="knobs.transientModelConfig"
					:mmt="mmt"
					:mmt-params="mmtParams"
					@update-parameters="setParameterDistributions(knobs.transientModelConfig, $event)"
					@update-source="setParameterSource(knobs.transientModelConfig, $event.id, $event.value)"
				/>
				<Accordion multiple :active-index="[0]" class="pb-6">
					<AccordionTab>
						<template #header> Interventions </template>
						<Button outlined size="small" label="Add Intervention" @click="addIntervention" />
						<tera-model-intervention
							v-for="(intervention, idx) of interventions"
							:key="intervention.name + intervention.timestep + intervention.value"
							:intervention="intervention"
							:parameter-options="Object.keys(mmt.parameters)"
							@update-value="
								(data: Intervention) => {
									interventions[idx] = data;
								}
							"
							@delete="interventions.splice(idx, 1)"
						/>
					</AccordionTab>
				</Accordion>

				<!-- TODO - For Nelson eval debug, remove in April 2024 -->
				<div style="padding-left: 1rem; font-size: 90%; color: #555555">
					<div>Model config id: {{ selectedConfigId }}</div>
					<div>Model id: {{ props.node.inputs[0].value?.[0] }}</div>
				</div>
			</tera-drilldown-section>
		</section>
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
								<InputText
									v-model="knobs.transientModelConfig.name"
									placeholder="Configuration Name"
									type="text"
									class="input-small"
								/>
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
	<tera-drilldown
		v-if="suggestedConfigurationContext.isOpen"
		:title="suggestedConfigurationContext.modelConfiguration?.name ?? 'Model Configuration'"
		:node="node"
		@on-close-clicked="suggestedConfigurationContext.isOpen = false"
		popover
	>
		<tera-drilldown-section class="p-2">
			<!-- Redo this to show model configs-->
			<tera-model-semantic-tables v-if="model" readonly :model="model" />
		</tera-drilldown-section>
	</tera-drilldown>

	<Teleport to="body">
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
	</Teleport>

	<!-- Matrix effect easter egg  -->
	<canvas id="matrix-canvas" />
</template>

<script setup lang="ts">
import '@/ace-config';
import { cloneDeep, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelSemanticTables from '@/components/model/tera-model-semantic-tables.vue';
// import teraModelIntervention from '@/components/model/petrinet/tera-model-intervention.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';

import { FatalError } from '@/api/api';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import {
	emptyMiraModel,
	generateModelDatasetConfigurationContext
} from '@/model-representation/mira/mira';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { configureModelFromDatasets, configureModelFromDocument } from '@/services/goLLM';
import { KernelSessionManager } from '@/services/jupyter';
import { getMMT, getModel, getModelConfigurations } from '@/services/model';
import {
	createModelConfiguration,
	setInitialSource,
	setInitialExpression,
	setParameterSource,
	setParameterDistributions,
	getAsConfiguredModel,
	getInterventions,
	setInterventions,
	amrToModelConfiguration
} from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import type { Intervention, Model, ModelConfiguration } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import type { WorkflowNode } from '@/types/workflow';
import { OperatorStatus } from '@/types/workflow';
import { formatTimestamp } from '@/utils/date';
import { logger } from '@/utils/logger';
import { isModelMissingMetadata } from '@/model-representation/service';
import { b64DecodeUnicode } from '@/utils/binary';
import Message from 'primevue/message';
import TeraModelIntervention from '@/components/model/petrinet/tera-model-intervention.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();

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
	transientModelConfig: {
		name: '',
		description: '',
		modelId: '',
		calibrationRunId: '',
		observableSemanticList: [],
		parameterSemanticList: [],
		initialSemanticList: []
	}
});

const interventions = ref<Intervention[]>([]);
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
		context: 'mira_config_edit',
		language: 'python3',
		context_info: {
			id: contextId
		}
	};
};
const codeText = ref(
	'# This environment contains the variable "model_config" to be read and updated'
);
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
	'update the parameters {gamma: 0.13}'
];
const contextLanguage = ref<string>('python3');

const appendCode = (data: any, property: string, runUpdatedCode = false) => {
	codeText.value = codeText.value.concat(' \n', data.content[property] as string);
	if (runUpdatedCode) runFromCode();
};

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
		.register('model_preview', (data) => {
			if (!data.content) return;
			handleModelPreview(data);

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
	console.group('Extracting configurations from inputs');
	if (!model.value?.id) {
		console.debug('Model not loaded yet, try later.');
		return;
	}
	if (documentId.value) {
		console.debug('Configuring model from document', documentId.value);
		modelFromDocumentHandler.value = await configureModelFromDocument(
			documentId.value,
			model.value.id,
			{
				ondata(data, closeConnection) {
					if (data?.status === TaskStatus.Failed) {
						closeConnection();
						console.debug('Task failed');
						throw new FatalError('Configs from document(s) - Task failed');
					}
					if (data?.status === TaskStatus.Success) {
						logger.success('Model configured from document(s)');
						const outputJSON = JSON.parse(b64DecodeUnicode(data.output));
						console.debug('Task success', outputJSON);
						closeConnection();
					}
					if (![TaskStatus.Failed, TaskStatus.Success].includes(data?.status)) {
						console.debug('Task running');
						closeConnection();
					}
				},
				onclose() {
					if (model.value?.id) {
						fetchConfigurations(model.value.id);
					}
				}
			},
			props.node.workflowId,
			props.node.id
		);
	}
	if (datasetIds.value) {
		console.debug('Configuring model from dataset(s)', datasetIds.value?.toString());

		const matrixStr = generateModelDatasetConfigurationContext(mmt.value, mmtParams.value);

		modelFromDatasetHandler.value = await configureModelFromDatasets(
			model.value.id,
			datasetIds.value,
			matrixStr,
			{
				ondata(data, closeConnection) {
					if (data?.status === TaskStatus.Failed) {
						closeConnection();
						console.debug('Task failed');
						throw new FatalError('Configs from dataset(s) - Task failed');
					}
					if (data.status === TaskStatus.Success) {
						logger.success('Model configured from dataset(s)');
						const outputJSON = JSON.parse(new TextDecoder().decode(data.output));
						console.debug('Task success', outputJSON);
						closeConnection();
					}
					if (![TaskStatus.Failed, TaskStatus.Success].includes(data?.status)) {
						console.debug('Task running');
						closeConnection();
					}
				},
				onclose() {
					if (model.value?.id) {
						fetchConfigurations(model.value.id);
					}
				}
			},
			props.node.workflowId,
			props.node.id
		);
	}
	console.groupEnd();
};

const handleModelPreview = async (data: any) => {
	if (!model.value) return;
	// Only update the keys provided in the model preview (not ID, temporary ect)
	Object.assign(model.value, cloneDeep(data.content['application/json']));
	const modelConfig = await amrToModelConfiguration(model.value);
	setInterventions(modelConfig, interventions.value);
	knobs.value.transientModelConfig = modelConfig;
};

const selectedOutputId = ref<string>('');
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const documentId = computed(() => props.node.inputs?.[1]?.value?.[0]?.documentId);
const datasetIds = computed(() => props.node.inputs?.[2]?.value);

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
const modelFromDocumentHandler = ref();
const modelFromDatasetHandler = ref();
const isLoading = computed(
	() =>
		modelFromDocumentHandler.value?.isRunning ||
		modelFromDatasetHandler.value?.isRunning ||
		isFetching.value
);

const model = ref<Model | null>(null);
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

const numInitials = computed(() => {
	if (!mmt.value) return 0;
	return Object.keys(mmt.value.initials).length;
});

const addIntervention = () => {
	interventions.value.push({
		name: '',
		timestep: 1,
		value: 1
	});
};

const downloadConfiguredModel = async () => {
	const rawModel = await getAsConfiguredModel(knobs.value?.transientModelConfig);
	if (rawModel) {
		const data = `text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(rawModel, null, 2))}`;
		const a = document.createElement('a');
		a.href = `data:${data}`;
		a.download = `${knobs.value?.transientModelConfig.name ?? 'configured_model'}.json`;
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
	if (modelId) {
		isFetching.value = true;
		suggestedConfigurationContext.value.tableData = await getModelConfigurations(modelId);
		isFetching.value = false;
	}
};

// Fill the form with the config data
const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;
	await fetchConfigurations(modelId);

	model.value = await getModel(modelId);

	if (!state.transientModelConfig.id) {
		// apply a configuration if one hasnt been applied yet
		applyConfigValues(suggestedConfigurationContext.value.tableData[0]);
	} else {
		knobs.value.transientModelConfig = cloneDeep(state.transientModelConfig);
		interventions.value = getInterventions(knobs.value.transientModelConfig);
	}

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				await kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const applyConfigValues = (config: ModelConfiguration) => {
	const state = cloneDeep(props.node.state);
	knobs.value.transientModelConfig = cloneDeep(config);
	interventions.value = getInterventions(config);

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

const onOpenSuggestedConfiguration = (config: ModelConfiguration) => {
	suggestedConfigurationContext.value.modelConfiguration = config;
	suggestedConfigurationContext.value.isOpen = true;
};

onMounted(async () => {
	await initialize();
});

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

// A very temporary way of doing interventions until we do a redesign
watch(
	() => interventions.value,
	() => {
		if (!isEmpty(interventions.value))
			setInterventions(knobs.value.transientModelConfig, interventions.value);
	},
	{ deep: true }
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
	async () => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			await initialize();
		}
	},
	{ immediate: true }
);

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
/* This is for the box around the suggested configurations section */
.box-container {
	border: solid 1px var(--surface-border);
	border-radius: var(--border-radius);
	background-color: var(--surface-50);
}

.box-container:deep(.p-accordion .p-accordion-content) {
	padding: 0;
	background-color: transparent;
}

.box-container:deep(.p-datatable .p-datatable-tbody > tr) {
	background-color: transparent;
}

.box-container:deep(.p-paginator) {
	background-color: transparent;
}

.box-container:deep(.p-accordion .p-accordion-header .p-accordion-header-link) {
	background-color: transparent;
}

.box-container:deep(
		.p-accordion .p-accordion-header:not(.p-disabled).p-highlight .p-accordion-header-link
	) {
	background-color: transparent;
}
.box-container:deep(.p-datatable .p-sortable-column.p-highlight) {
	background-color: transparent;
}
.box-container:deep(table > thead > tr > th:nth-child(1)) {
	padding-left: var(--gap);
}
.box-container:deep(.p-button .p-button-label) {
	text-align: left;
}

:deep(.p-datatable-loading-overlay.p-component-overlay) {
	background-color: var(--surface-section);
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}

.artifact-amount {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}
.empty-section {
	color: var(--text-color-subdued);
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

.use-button {
	white-space: nowrap;
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

.footer {
	display: flex;
	justify-content: space-between;
	width: 100%;
	padding-top: var(--gap-small);
	padding-bottom: var(--gap-small);
	border-top: 1px solid var(--surface-border-light);
}
</style>
