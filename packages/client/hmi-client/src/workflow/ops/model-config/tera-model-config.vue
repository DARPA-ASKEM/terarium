<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
			<tera-output-dropdown
				@click.stop
				:output="selectedOutputId"
				is-selectable
				:options="outputs"
				@update:selection="onSelection"
			/>
		</template>

		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section class="pl-3 pr-3 gap-0">
				<div class="box-container mt-3" v-if="model">
					<Accordion multiple :active-index="[0]">
						<AccordionTab>
							<template #header>
								Suggested configurations<span class="artifact-amount"
									>({{ suggestedConfirgurationContext.tableData.length }})</span
								>
								<Button
									icon="pi pi-sign-out"
									label="Extract configurations from inputs"
									outlined
									severity="secondary"
									@click.stop="extractConfigurationsFromInputs"
									style="margin-left: auto"
									:loading="isLoading"
								/>
							</template>

							<DataTable
								:value="suggestedConfirgurationContext.tableData"
								size="small"
								data-key="id"
								:paginator="suggestedConfirgurationContext.tableData.length > 5"
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
								<Column field="description" header="Description" style="width: 30%"></Column>
								<Column field="createdOn" header="Created On" :sortable="true" style="width: 25%">
									<template #body="{ data }">
										{{ formatTimestamp(data.createdOn) }}
									</template>
								</Column>
								<Column header="Source" style="width: 30%">
									<template #body="{ data }">
										{{ data.configuration.metadata?.source?.join(',') || '--' }}
									</template>
								</Column>
								<Column style="width: 7rem">
									<template #body="{ data }">
										<Button
											class="use-button"
											label="Apply configuration values"
											@click="useSuggestedConfig(data)"
											text
										/>
									</template>
								</Column>
								<template #loading>
									<div>
										<Vue3Lottie
											:animationData="LoadingWateringCan"
											:height="200"
											:width="200"
										></Vue3Lottie>
										<p>Fetching suggested configurations.</p>
									</div>
								</template>
								<template #empty>
									<p class="empty-section m-3">No configurations found.</p>
								</template>
							</DataTable>
						</AccordionTab>
					</Accordion>
				</div>
				<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]" class="pb-6">
					<AccordionTab header="Context">
						<p class="text-sm mb-1">Name</p>
						<InputText
							class="context-item"
							placeholder="Enter a name for this configuration"
							v-model="knobs.name"
						/>
						<p class="text-sm mb-1 mt-3">Description</p>
						<Textarea
							class="context-item"
							placeholder="Enter a description"
							v-model="knobs.description"
						/>
					</AccordionTab>
					<AccordionTab header="Diagram">
						<tera-model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<template
						v-if="modelType === AMRSchemaNames.PETRINET || modelType === AMRSchemaNames.STOCKFLOW"
					>
						<AccordionTab>
							<template #header>
								Initial variable values<span class="artifact-amount">({{ numInitials }})</span>
							</template>
							<tera-initial-table
								v-if="modelConfiguration"
								:model="modelConfiguration.configuration"
								:mmt="mmt"
								:mmt-params="mmtParams"
								config-view
								@update-value="updateConfigInitial"
								@update-model="
									(modelToUpdate: Model) => {
										updateConfigFromModel(modelToUpdate);
									}
								"
							/>
						</AccordionTab>
					</template>
					<template v-else-if="modelType === AMRSchemaNames.REGNET">
						<AccordionTab header="Vertices">
							<DataTable v-if="!isEmpty(vertices)" data-key="id" :value="vertices">
								<Column field="id" header="Symbol" />
								<Column field="name" header="Name" />
								<Column field="rate_constant" header="Rate Constant" />
								<Column field="initial" header="Initial Value">
									<template #body="{ data, field }">
										<!-- FIXME: temporary hack -->
										<InputText v-model="data[field]" @blur="tempUpdate(data, field)" />
									</template>
								</Column>
							</DataTable>
						</AccordionTab>
						<AccordionTab header="Edges">
							<DataTable v-if="!isEmpty(edges)" data-key="id" :value="edges">
								<Column field="id" header="Symbol" />
								<Column field="source" header="Source" />
								<Column field="target" header="Target" />
								<Column field="properties.rate_constant" header="Rate Constant" />
							</DataTable>
						</AccordionTab>
					</template>
					<AccordionTab>
						<template #header>
							Parameters<span class="artifact-amount">({{ numParameters }})</span>
						</template>
						<tera-parameter-table
							v-if="modelConfiguration"
							:model-configurations="suggestedConfirgurationContext.tableData"
							:model="modelConfiguration.configuration"
							:mmt="mmt"
							:mmt-params="mmtParams"
							config-view
							@update-value="updateConfigParam"
							@update-model="
								(modelToUpdate: Model) => {
									updateConfigFromModel(modelToUpdate);
								}
							"
						/>
						<section v-else>
							<p class="empty-section">No parameters found.</p>
						</section>
					</AccordionTab>
				</Accordion>

				<!-- For Nelson eval debug -->
				<div style="padding-left: 1rem; font-size: 90%; color: #555555">
					<div>Model config id: {{ selectedConfigId }}</div>
					<div>Model id: {{ props.node.inputs[0].value?.[0] }}</div>
				</div>

				<template #footer>
					<div class="footer">
						<Button
							outlined
							size="large"
							:disabled="isSaveDisabled"
							label="Run"
							icon="pi pi-play"
							@click="createConfiguration"
						/>
						<Button style="margin-left: auto" size="large" label="Close" @click="emit('close')" />
					</div>
				</template>
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<tera-drilldown-section id="notebook-section">
				<div class="toolbar-right-side">
					<Button
						icon="pi pi-play"
						label="Run"
						outlined
						severity="secondary"
						@click="runFromCode"
					/>
				</div>
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:defaultOptions="sampleAgentQuestions"
						:context-language="contextLanguage"
						@llm-output="(data: any) => appendCode(data, 'code')"
					/>
				</Suspense>
				<v-ace-editor
					v-model:value="codeText"
					@init="initializeEditor"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>
				<template #footer>
					<InputText
						v-model="knobs.name"
						placeholder="Configuration Name"
						type="text"
						class="input-small"
					/>
					<Button
						:disabled="isSaveDisabled"
						outlined
						style="margin-right: auto"
						label="Save as new configuration"
						@click="createConfiguration"
					/>
				</template>
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
		</section>
	</tera-drilldown>
	<tera-drilldown
		v-if="suggestedConfirgurationContext.isOpen"
		:title="suggestedConfirgurationContext.modelConfiguration?.name ?? 'Model Configuration'"
		@on-close-clicked="suggestedConfirgurationContext.isOpen = false"
		popover
	>
		<tera-drilldown-section>
			<tera-model-semantic-tables
				v-if="suggestedConfirgurationContext.modelConfiguration?.configuration"
				readonly
				:model="suggestedConfirgurationContext.modelConfiguration?.configuration"
			/>
		</tera-drilldown-section>
	</tera-drilldown>
	<!-- Matrix effect easter egg  -->
	<canvas id="matrix-canvas"></canvas>
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
import { Vue3Lottie } from 'vue3-lottie';

import LoadingWateringCan from '@/assets/images/lottie-loading-wateringCan.json';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import teraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';

import { FatalError } from '@/api/api';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { configureModelFromDatasets, configureModelFromDocument } from '@/services/goLLM';
import { KernelSessionManager } from '@/services/jupyter';
import { getMMT, getModel, getModelConfigurations, getModelType } from '@/services/model';
import { createModelConfiguration } from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import type { Initial, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { AMRSchemaNames } from '@/types/common';
import type { WorkflowNode } from '@/types/workflow';
import { OperatorStatus } from '@/types/workflow';
import { formatTimestamp } from '@/utils/date';
import { logger } from '@/utils/logger';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();

const outputs = computed(() => {
	if (!isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const emit = defineEmits(['append-output', 'update-state', 'select-output', 'close']);

interface BasicKnobs {
	name: string;
	description: string;
	initials: Initial[];
	parameters: ModelParameter[];
	timeseries: { [index: string]: any };
	initialsMetadata: { [index: string]: any };
	parametersMetadata: { [index: string]: any };
	tempConfigId: string;
}

const knobs = ref<BasicKnobs>({
	name: '',
	description: '',
	initials: [],
	parameters: [],
	timeseries: {},
	initialsMetadata: {},
	parametersMetadata: {},
	tempConfigId: ''
});

const isSaveDisabled = computed(() => {
	if (knobs.value.name === '') return true;
	return false;
});

const kernelManager = new KernelSessionManager();
let editor: VAceEditorInstance['_editor'] | null;
const buildJupyterContext = () => {
	const contextId = selectedConfigId.value ?? props.node.state.tempConfigId;
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
const edges = computed(() => modelConfiguration?.value?.configuration?.model?.edges ?? []);
const vertices = computed(() => modelConfiguration?.value?.configuration.model?.vertices ?? []);

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
	if (!model.value?.id) return;
	if (documentId.value) {
		modelFromDocumentHandler.value = await configureModelFromDocument(
			documentId.value,
			model.value.id,
			{
				ondata(data, closeConnection) {
					if (data?.status === TaskStatus.Failed) {
						closeConnection();
						throw new FatalError('Configs from document - Task failed');
					}
					if (data.status === TaskStatus.Success) {
						logger.success('Model configured from document');
						closeConnection();
					}
				},
				onclose() {
					if (model.value?.id) {
						fetchConfigurations(model.value.id);
					}
				}
			}
		);
	}
	if (datasetIds.value) {
		modelFromDatasetHandler.value = await configureModelFromDatasets(
			model.value.id,
			datasetIds.value,
			{
				ondata(data, closeConnection) {
					if (data?.status === TaskStatus.Failed) {
						closeConnection();
						throw new FatalError('Configs from datasets - Task failed');
					}
					if (data.status === TaskStatus.Success) {
						logger.success('Model configured from dataset(s)');
						closeConnection();
					}
				},
				onclose() {
					if (model.value?.id) {
						fetchConfigurations(model.value.id);
					}
				}
			}
		);
	}
};

const handleModelPreview = (data: any) => {
	if (!model.value) return;
	// Only update the keys provided in the model preview (not ID, temporary ect)
	Object.assign(model.value, cloneDeep(data.content['application/json']));
	const ode = model.value?.semantics?.ode;
	knobs.value.initials = ode?.initials !== undefined ? ode?.initials : [];
	knobs.value.parameters = ode?.parameters !== undefined ? ode?.parameters : [];
	knobs.value.timeseries =
		model.value?.metadata?.timeseries !== undefined ? model.value?.metadata?.timeseries : {};
	knobs.value.initialsMetadata =
		model.value?.metadata?.initials !== undefined ? model.value?.metadata?.initials : {};
	knobs.value.parametersMetadata =
		model.value?.metadata?.parameters !== undefined ? model.value?.metadata?.parameters : {};
};

const selectedOutputId = ref<string>('');
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const documentId = computed(() => props.node.inputs?.[1]?.value?.[0]?.documentId);
const datasetIds = computed(() => props.node.inputs?.[2]?.value);

const suggestedConfirgurationContext = ref<{
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

const modelConfiguration = computed<ModelConfiguration | null>(() => {
	if (!model.value) return null;

	const cloneModel = cloneDeep(model.value);

	const modelConfig: ModelConfiguration = {
		id: '',
		name: '',
		model_id: cloneModel.id ?? '',
		configuration: cloneModel
	};

	if (!cloneModel.metadata || !cloneModel.metadata.timeseries) {
		cloneModel.metadata = {};
	}

	if (modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		if (cloneModel.semantics) {
			cloneModel.semantics.ode.initials = knobs.value.initials;
			cloneModel.semantics.ode.parameters = knobs.value.parameters;
			cloneModel.metadata.timeseries = knobs.value.timeseries;
			cloneModel.metadata.initials = knobs.value.initialsMetadata;
			cloneModel.metadata.parameters = knobs.value.parametersMetadata;
		}
		modelConfig.configuration = cloneModel;
	} else if (modelType.value === AMRSchemaNames.REGNET) {
		cloneModel.model.parameters = knobs.value.parameters;
		cloneModel.metadata.timeseries = knobs.value.timeseries;
		cloneModel.metadata.initials = knobs.value.initialsMetadata;
		cloneModel.metadata.parameters = knobs.value.parametersMetadata;
		modelConfig.configuration = cloneModel;
	}

	return modelConfig;
});

const numParameters = computed(() => {
	if (!mmt.value) return 0;
	return Object.keys(mmt.value.parameters).length;
});

const numInitials = computed(() => {
	if (!mmt.value) return 0;
	return Object.keys(mmt.value.initials).length;
});

const modelType = computed(() => getModelType(model.value));

const updateConfigParam = (params: ModelParameter[]) => {
	for (let i = 0; i < knobs.value.parameters.length; i++) {
		const foundParam = params.find((p) => p.id === knobs.value.parameters![i].id);
		if (foundParam) {
			knobs.value.parameters[i] = foundParam;
		}
	}
};

const updateConfigInitial = (inits: Initial[]) => {
	for (let i = 0; i < knobs.value.initials.length; i++) {
		const foundInitial = inits.find((init) => init.target === knobs.value.initials![i].target);
		if (foundInitial) {
			knobs.value.initials[i] = foundInitial;
		}
	}
};

const updateConfigFromModel = (inputModel: Model) => {
	if (modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		knobs.value.initials = inputModel.semantics?.ode.initials ?? [];
		knobs.value.parameters = inputModel.semantics?.ode.parameters ?? [];
	} else if (modelType.value === AMRSchemaNames.REGNET) {
		knobs.value.parameters = inputModel.model?.parameters ?? [];
	}
	knobs.value.timeseries = inputModel.metadata?.timeseries ?? {};
	knobs.value.initialsMetadata = inputModel.metadata?.initials ?? {};
	knobs.value.parametersMetadata = inputModel.metadata?.parameters ?? {};
};

const createConfiguration = async () => {
	if (!model.value) return;

	const state = cloneDeep(props.node.state);
	const data = await createModelConfiguration(
		model.value.id,
		knobs.value.name,
		knobs.value.description,
		modelConfiguration.value?.configuration
	);

	if (!data) {
		logger.error('Failed to create model configuration');
		return;
	}

	useToastService().success('', 'Created model configuration');
	emit('append-output', {
		type: ModelConfigOperation.outputs[0].type,
		label: state.name,
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
		suggestedConfirgurationContext.value.tableData = await getModelConfigurations(modelId);
		isFetching.value = false;
	}
};

// Creates a temp config (if doesnt exist in state)
// This is used for beaker context when there are no outputs in the node
const createTempModelConfig = async () => {
	const state = cloneDeep(props.node.state);
	if (state.tempConfigId !== '' || !model.value) return;
	const data = await createModelConfiguration(
		model.value.id,
		'Temp_config_name',
		'Utilized in model config node for beaker purposes',
		model.value,
		true
	);

	knobs.value.tempConfigId = data.id;
};

// Fill the form with the config data
const initialize = async () => {
	const state = props.node.state;
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;
	fetchConfigurations(modelId);
	model.value = await getModel(modelId);

	knobs.value.name = state.name;
	knobs.value.description = state.description;
	knobs.value.tempConfigId = state.tempConfigId;

	// State has never been set up:
	if (knobs.value.tempConfigId === '') {
		// Grab these values from model to inialize them
		const ode = model.value?.semantics?.ode;
		knobs.value.initials = ode?.initials !== undefined ? ode?.initials : [];
		if (
			modelType.value === AMRSchemaNames.PETRINET ||
			modelType.value === AMRSchemaNames.STOCKFLOW
		) {
			knobs.value.parameters = ode?.parameters !== undefined ? ode?.parameters : [];
		} else if (modelType.value === AMRSchemaNames.REGNET) {
			knobs.value.parameters =
				model.value?.model?.parameters !== undefined ? model.value?.model?.parameters : [];
		}

		knobs.value.timeseries =
			model.value?.metadata?.timeseries !== undefined ? model.value?.metadata?.timeseries : {};
		knobs.value.initialsMetadata =
			model.value?.metadata?.initials !== undefined ? model.value?.metadata?.initials : {};
		knobs.value.parametersMetadata =
			model.value?.metadata?.parameters !== undefined ? model.value?.metadata?.parameters : {};
		await createTempModelConfig();
	}
	// State already been set up use it instead:
	else {
		knobs.value.initials = state.initials;
		knobs.value.parameters = state.parameters;
		knobs.value.timeseries = state.timeseries;
		knobs.value.initialsMetadata = state.initialsMetadata;
		knobs.value.parametersMetadata = state.parametersMetadata;
	}

	// Ensure the parameters have constant and distributions for editing in children components
	knobs.value.parameters.forEach((param) => {
		if (!param.distribution) {
			param.distribution = {
				type: 'StandardUniform1',
				parameters: {
					minimum: param.value || 1,
					maximum: param.value || 1
				}
			};
		}
	});

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

const useSuggestedConfig = (config: ModelConfiguration) => {
	const amr = config.configuration;

	knobs.value.name = config.name;
	knobs.value.description = config.description ?? '';
	if (modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		knobs.value.initials = amr.semantics.ode.initials;
		knobs.value.parameters = amr.semantics.ode.parameters;
	} else if (modelType.value === AMRSchemaNames.REGNET) {
		knobs.value.parameters = amr.model.parameters;
	}
	knobs.value.timeseries = amr.metadata?.timeseries ?? {};
	knobs.value.initialsMetadata = amr.metadata?.initials ?? {};
	knobs.value.parametersMetadata = amr.metadata?.parameters ?? {};
	logger.success(`Configuration applied ${config.name}`);
};

const onOpenSuggestedConfiguration = (config: ModelConfiguration) => {
	suggestedConfirgurationContext.value.modelConfiguration = config;
	suggestedConfirgurationContext.value.isOpen = true;
};

// FIXME: temporary hack, need proper config/states to handle all frameworks and fields
const tempUpdate = (data: any, field: any) => {
	data[field] = +data[field];
};

onMounted(async () => {
	await initialize();
});

watch(
	() => modelConfiguration.value,
	async (config) => {
		if (!config) return;
		const response: any = await getMMT(config.configuration);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	},
	{ immediate: true }
);

watch(
	() => knobs.value,
	async () => {
		const state = cloneDeep(props.node.state);
		state.name = knobs.value.name;
		state.description = knobs.value.description;
		state.initials = knobs.value.initials;
		state.parameters = knobs.value.parameters;
		state.timeseries = knobs.value.timeseries;
		state.initialsMetadata = knobs.value.initialsMetadata;
		state.parametersMetadata = knobs.value.parametersMetadata;
		state.tempConfigId = knobs.value.tempConfigId;

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

:deep(.p-datatable-loading-overlay.p-component-overlay) {
	background-color: #fff;
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
