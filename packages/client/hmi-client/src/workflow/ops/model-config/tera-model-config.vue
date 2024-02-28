<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-action-row>
			<tera-output-dropdown
				@click.stop
				style="margin-left: auto"
				:output="selectedOutputId"
				is-selectable
				:options="outputs"
				@update:selection="onSelection"
			/>
		</template>
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<Accordion multiple :active-index="[0, 1, 2, 3, 4]" class="pb-6">
					<AccordionTab v-if="model">
						<template #header>
							Suggested configurations<span class="artifact-amount"
								>({{ suggestedConfirgurationContext.tableData.length }})</span
							>
							<Button
								outlined
								label="Extract configurations from a document"
								size="small"
								icon="pi pi-cog"
								@click.stop="extractConfigurations"
								:disabled="loadingConfigs || !documentId || !model.id"
								style="margin-left: auto"
							/>
						</template>

						<DataTable
							v-if="suggestedConfirgurationContext.tableData.length > 0"
							:value="suggestedConfirgurationContext.tableData"
							size="small"
							data-key="id"
							:paginator="suggestedConfirgurationContext.tableData.length > 5"
							:rows="5"
							sort-field="createdOn"
							:sort-order="-1"
							:loading="loadingConfigs"
						>
							<Column field="name" header="Name" style="width: 15%">
								<template #body="{ data }">
									<Button :label="data.name" text @click="onOpenSuggestedConfiguration(data)" />
								</template>
							</Column>
							<Column field="description" header="Description" style="width: 30%"></Column>
							<Column field="createdOn" header="Created On" :sortable="true" style="width: 25%">
								<template #body="{ data }">
									{{ new Date(data.createdOn).toISOString() }}
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
								<Vue3Lottie :animationData="EmptySeed" :height="200" :width="200"></Vue3Lottie>
							</template>
						</DataTable>
						<section v-else>
							<p class="empty-section">No suggested configurations found.</p>
						</section>
					</AccordionTab>
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
					<AccordionTab header="Initials">
						<tera-model-config-table
							v-if="modelConfiguration"
							:model-configuration="modelConfiguration"
							:data="tableFormattedInitials"
							@update-value="updateConfigInitial"
							@update-configuration="
								(configToUpdate: ModelConfiguration) => {
									updateFromConfig(configToUpdate);
								}
							"
						/>
					</AccordionTab>
					<AccordionTab header="Parameters">
						<tera-model-config-table
							v-if="modelConfiguration"
							:model-configuration="modelConfiguration"
							:data="tableFormattedParams"
							@update-value="updateConfigParam"
							@update-configuration="
								(configToUpdate: ModelConfiguration) => {
									updateFromConfig(configToUpdate);
								}
							"
						/>
					</AccordionTab>
				</Accordion>
				<template #footer>
					<Button
						outlined
						size="large"
						:disabled="isSaveDisabled"
						label="Run"
						icon="pi pi-play"
						@click="createConfiguration"
					/>
					<Button style="margin-left: auto" size="large" label="Close" @click="emit('close')" />
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
						size="large"
						@click="runFromCode"
					/>
				</div>
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:defaultOptions="sampleAgentQuestions"
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
				<div>{{ notebookResponse }}</div>
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
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch, onUnmounted, onMounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { getModel, getModelConfigurations } from '@/services/model';
import { createModelConfiguration } from '@/services/model-configurations';
import type { Model, ModelConfiguration, Initial, ModelParameter } from '@/types/Types';
import { ModelConfigTableData, ParamType } from '@/types/common';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import {
	getUnstratifiedInitials,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { useToastService } from '@/services/toast';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import { logger } from '@/utils/logger';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { configureModel } from '@/services/goLLM';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { VAceEditor } from 'vue3-ace-editor';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { KernelSessionManager } from '@/services/jupyter';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import LoadingWateringCan from '@/assets/images/lottie-loading-wateringCan.json';
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import { Vue3Lottie } from 'vue3-lottie';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';
import TeraModelConfigTable from './tera-model-config-table.vue';

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();

const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
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
	sources: { [index: string]: any };
	tempConfigId: string;
}

const knobs = ref<BasicKnobs>({
	name: '',
	description: '',
	initials: [],
	parameters: [],
	timeseries: {},
	sources: {},
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
const sampleAgentQuestions = [
	'What are the current parameters values?',
	'update the parameters {gamma: 0.13}'
];

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
		.register('error', (data) => {
			logger.error(`${data.content.ename}: ${data.content.evalue}`);
			console.log('error', data.content);
		})
		.register('model_preview', (data) => {
			if (!data.content) return;
			handleModelPreview(data);

			if (executedCode) {
				saveCodeToState(executedCode, true);
			}
		});
};

// FIXME: Copy pasted in 3 locations, could be written cleaner and in a service
const saveCodeToState = (code: string, hasCodeBeenRun: boolean) => {
	const state = _.cloneDeep(props.node.state);
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

const handleModelPreview = (data: any) => {
	if (!model.value) return;
	// Only update the keys provided in the model preview (not ID, temporary ect)
	Object.assign(model.value, _.cloneDeep(data.content['application/json']));
	const ode = model.value?.semantics?.ode;
	knobs.value.initials = ode?.initials !== undefined ? ode?.initials : [];
	knobs.value.parameters = ode?.parameters !== undefined ? ode?.parameters : [];
	knobs.value.timeseries =
		model.value?.metadata?.timeseries !== undefined ? model.value?.metadata?.timeseries : {};
	knobs.value.sources =
		model.value?.metadata?.sources !== undefined ? model.value?.metadata?.sources : {};
};

const selectedOutputId = ref<string>(props.node.active ?? '');
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const documentId = computed(() => props.node.inputs?.[1]?.value?.[0]?.documentId);

const suggestedConfirgurationContext = ref<{
	isOpen: boolean;
	tableData: ModelConfiguration[];
	modelConfiguration: ModelConfiguration | null;
}>({
	isOpen: false,
	tableData: [],
	modelConfiguration: null
});

const loadingConfigs = ref(false);
const model = ref<Model | null>();

const modelConfiguration = computed<ModelConfiguration | null>(() => {
	if (!model.value) return null;

	const cloneModel = _.cloneDeep(model.value);
	if (cloneModel.semantics) {
		if (!cloneModel.metadata || !cloneModel.metadata.timeseries) {
			cloneModel.metadata = {
				...cloneModel.metadata,
				timeseries: {}
			};
		}
		cloneModel.semantics.ode.initials = knobs.value.initials;
		cloneModel.semantics.ode.parameters = knobs.value.parameters;
		cloneModel.metadata.timeseries = knobs.value.timeseries;
		cloneModel.metadata.sources = knobs.value.sources;
	}
	const modelConfig: ModelConfiguration = {
		id: '',
		name: '',
		model_id: cloneModel.id ?? '',
		configuration: cloneModel
	};
	return modelConfig;
});

const stratifiedModelType = computed(() => {
	if (!model.value) return null;
	return getStratificationType(model.value);
});

const parameters = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	if (stratifiedModelType.value) {
		return getUnstratifiedParameters(model.value);
	}
	const result = new Map<string, string[]>();
	model.value.semantics?.ode.parameters?.forEach((p) => {
		result.set(p.id, [p.id]);
	});
	return result;
});

const initials = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	if (stratifiedModelType.value) {
		return getUnstratifiedInitials(model.value);
	}
	const result = new Map<string, string[]>();
	model.value.semantics?.ode.initials?.forEach((initial) => {
		result.set(initial.target, [initial.target]);
	});
	return result;
});

const tableFormattedInitials = computed<ModelConfigTableData[]>(() => {
	const formattedInitials: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const initial = knobs.value.initials.find((i) => i.target === v);
				const sourceValue = knobs.value.sources[initial!.target];
				return {
					id: v,
					name: v,
					type: ParamType.EXPRESSION,
					value: initial,
					source: sourceValue,
					visibility: false
				};
			});
			formattedInitials.push({
				id: init,
				name: init,
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				tableFormattedMatrix
			});
		});
	} else {
		initials.value.forEach((vals, init) => {
			const initial = knobs.value.initials.find((i) => i.target === vals[0]);
			const sourceValue = knobs.value.sources[initial!.target];

			formattedInitials.push({
				id: init,
				name: init,
				type: ParamType.EXPRESSION,
				value: initial,
				source: sourceValue,
				visibility: false
			});
		});
	}

	return formattedInitials;
});

const tableFormattedParams = computed<ModelConfigTableData[]>(() => {
	const formattedParams: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		parameters.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const param = knobs.value.parameters.find((i) => i.id === v);
				const paramType = getParamType(param);
				const timeseriesValue = knobs.value.timeseries[param!.id];
				const sourceValue = knobs.value.sources[param!.id];
				return {
					id: v,
					name: v,
					type: paramType,
					value: param,
					source: sourceValue,
					visibility: false,
					timeseries: timeseriesValue
				};
			});
			formattedParams.push({
				id: init,
				name: init,
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				tableFormattedMatrix
			});
		});
	} else {
		parameters.value.forEach((vals, init) => {
			const param = knobs.value.parameters.find((i) => i.id === vals[0]);
			const paramType = getParamType(param);

			const timeseriesValue = knobs.value.timeseries[param!.id];
			const sourceValue = knobs.value.sources[param!.id];
			formattedParams.push({
				id: init,
				name: init,
				type: paramType,
				value: param,
				source: sourceValue,
				visibility: false,
				timeseries: timeseriesValue
			});
		});
	}

	return formattedParams;
});

const getParamType = (param: ModelParameter | undefined) => {
	let type = ParamType.CONSTANT;
	if (!param) return type;
	if (
		modelConfiguration.value?.configuration.metadata?.timeseries?.[param.id] ||
		modelConfiguration.value?.configuration.metadata.timeseries?.[param.id] === ''
	) {
		type = ParamType.TIME_SERIES;
	} else if (param?.distribution) {
		type = ParamType.DISTRIBUTION;
	}
	return type;
};

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

const updateFromConfig = (config: ModelConfiguration) => {
	knobs.value.initials = config.configuration.semantics?.ode.initials ?? [];
	knobs.value.parameters = config.configuration.semantics?.ode.parameters ?? [];
	knobs.value.timeseries = config.configuration?.metadata?.timeseries ?? {};
	knobs.value.sources = config.configuration?.metadata?.sources ?? {};
};

const createConfiguration = async () => {
	if (!model.value) return;

	const state = _.cloneDeep(props.node.state);
	const data = await createModelConfiguration(
		model.value.id,
		knobs.value.name,
		knobs.value.description,
		model.value
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
		loadingConfigs.value = true;
		suggestedConfirgurationContext.value.tableData = await getModelConfigurations(modelId);
		loadingConfigs.value = false;
	}
};

// Creates a temp config (if doesnt exist in state)
// This is used for beaker context when there are no outputs in the node
const createTempModelConfig = async () => {
	const state = _.cloneDeep(props.node.state);
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
		knobs.value.parameters = ode?.parameters !== undefined ? ode?.parameters : [];
		knobs.value.timeseries =
			model.value?.metadata?.timeseries !== undefined ? model.value?.metadata?.timeseries : {};
		knobs.value.sources =
			model.value?.metadata?.sources !== undefined ? model.value?.metadata?.sources : {};
		await createTempModelConfig();
	}
	// State already been set up use it instead:
	else {
		knobs.value.initials = state.initials;
		knobs.value.parameters = state.parameters;
		knobs.value.timeseries = state.timeseries;
		knobs.value.sources = state.sources;
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

const useSuggestedConfig = (config: ModelConfiguration) => {
	knobs.value.name = config.name;
	knobs.value.description = config.description ?? '';
	knobs.value.initials = config.configuration.semantics.ode.initials;
	knobs.value.parameters = config.configuration.semantics.ode.parameters;
	knobs.value.timeseries = config.configuration.metadata?.timeseries ?? {};
	knobs.value.sources = config.configuration.metadata?.sources ?? {};
	logger.success(`Configuration applied ${config.name}`);
};

const extractConfigurations = async () => {
	if (!documentId.value || !model.value?.id) return;
	loadingConfigs.value = true;
	await configureModel(documentId.value, model.value.id);
	loadingConfigs.value = false;
	fetchConfigurations(model.value.id);
};

const onOpenSuggestedConfiguration = (config: ModelConfiguration) => {
	suggestedConfirgurationContext.value.modelConfiguration = config;
	suggestedConfirgurationContext.value.isOpen = true;
};

onMounted(async () => {
	await initialize();
});

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.name = knobs.value.name;
		state.description = knobs.value.description;
		state.initials = knobs.value.initials;
		state.parameters = knobs.value.parameters;
		state.timeseries = knobs.value.timeseries;
		state.sources = knobs.value.sources;
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
	{ deep: true }
);

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
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

.floating-footer {
	display: flex;
	justify-content: flex-end;
	position: fixed;
	padding-top: 0.75rem;
	bottom: 16px;
	left: 3rem;
	width: calc(100% - 6rem);
	background-color: var(--surface-glass);
	backdrop-filter: blur(8px);
	border-top: 1px solid var(--surface-border);
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
</style>
