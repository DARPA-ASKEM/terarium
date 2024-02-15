<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<Accordion multiple :active-index="[0, 1, 2, 3]">
					<AccordionTab header="Context">
						<template #header>
							<tera-output-dropdown
								@click.stop
								style="margin-left: auto"
								:output="selectedOutputId"
								is-selectable
								:options="outputs"
								@update:output="onUpdateOutput"
								@update:selection="onUpdateSelection"
							/>
						</template>
						<h3>Name</h3>
						<InputText
							class="context-item"
							placeholder="Enter a name for this configuration"
							v-model="knobs.name"
						/>
						<h3>Description</h3>
						<Textarea
							class="context-item"
							placeholder="Enter a description"
							v-model="configDescription"
							@update:model-value="() => debouncedUpdateState({ description: configDescription })"
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
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<tera-drilldown-section>
				<h4>Code Editor - Python</h4>
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
					<Button style="margin-right: auto" label="Run" @click="runFromCode" />
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
			<tera-drilldown-preview
				title="Output Preview"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				:options="outputs"
				is-selectable
			>
				<div>OUTPUT HERE</div>
			</tera-drilldown-preview>
		</section>
		<template #footer>
			<Button
				outlined
				:disabled="isSaveDisabled"
				label="Run"
				icon="pi pi-play"
				@click="createConfiguration"
			/>
			<Button style="margin-left: auto" label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _, { cloneDeep, isEmpty } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { getModel } from '@/services/model';
import {
	createModelConfiguration,
	getModelConfigurationById
} from '@/services/model-configurations';
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
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { VAceEditor } from 'vue3-ace-editor';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { KernelSessionManager } from '@/services/jupyter';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import TeraModelConfigTable from './tera-model-config-table.vue';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';

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

const emit = defineEmits([
	'append-output-port',
	'update-state',
	'select-output',
	'update-output-port',
	'close'
]);

interface BasicKnobs {
	name: string;
	description: string;
	initials: Initial[];
	parameters: ModelParameter[];
}

const knobs = ref<BasicKnobs>({
	name: '',
	description: '',
	initials: [],
	parameters: []
});

const isSaveDisabled = computed(() => {
	if (knobs.value.name === '') return true;
	return false;
});

const kernelManager = new KernelSessionManager();
// const isKernelReady = ref(false);
let editor: VAceEditorInstance['_editor'] | null;
const buildJupyterContext = () => {
	console.log('buildJupyterContext');
	const contextId = selectedConfigId.value ?? props.node.state.tempConfigId;
	console.log(contextId);
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
	'# This environment contains the variable "model" \n# which is displayed on the right'
);
const sampleAgentQuestions = ['What are the current parameters values?'];

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
		user_expressions: {},
		allow_stdin: true,
		stop_on_error: false,
		code
	};

	let executedCode = '';

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('execute_input', (data) => {
			executedCode = data.content.code;
		})
		.register('stream', (data) => {
			console.log('stream', data);
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
	console.log('Handle model preview:');
	console.log(data);
	model.value = data.content['application/json'];
};

const selectedOutputId = ref<string>('');
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const configCache = ref<Record<string, ModelConfiguration>>({});
const configDescription = ref<string>(props.node.state.description);
const model = ref<Model>();

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
		cloneModel.semantics.ode.initials = configInitials.value;
		cloneModel.semantics.ode.parameters = configParams.value;
		cloneModel.metadata.timeseries = configTimeSeries.value;
	}
	const modelConfig: ModelConfiguration = {
		id: '',
		name: '',
		model_id: cloneModel.id ?? '',
		configuration: cloneModel
	};
	return modelConfig;
});

const configInitials = ref<Initial[]>();
const configParams = ref<ModelParameter[]>();
const configTimeSeries = ref<{ [index: string]: any }>();

const stratifiedModelType = computed(() => {
	if (!model.value) return null;
	return getStratificationType(model.value);
});

const parameters = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	return getUnstratifiedParameters(model.value);
});

const initials = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	return getUnstratifiedInitials(model.value);
});

const tableFormattedInitials = computed<ModelConfigTableData[]>(() => {
	const formattedInitials: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const initial = configInitials.value?.find((i) => i.target === v);
				return {
					id: v,
					name: v,
					type: ParamType.EXPRESSION,
					value: initial,
					source: '',
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
			formattedInitials.push({
				id: init,
				name: init,
				type: ParamType.EXPRESSION,
				value: configInitials.value?.find((i) => i.target === vals[0]),
				source: '',
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
				const param = configParams.value?.find((i) => i.id === v);
				const paramType = getParamType(param);
				const timeseriesValue = configTimeSeries.value?.[param!.id];
				return {
					id: v,
					name: v,
					type: paramType,
					value: param,
					source: '',
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
			const param = configParams.value?.find((i) => i.id === vals[0]);
			const paramType = getParamType(param);

			const timeseriesValue = configTimeSeries.value?.[param!.id];
			formattedParams.push({
				id: init,
				name: init,
				type: paramType,
				value: param,
				source: '',
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

const updateState = (updatedField) => {
	let state = _.cloneDeep(props.node.state);
	state = { ...state, ...updatedField };
	emit('update-state', state);
};

const updateConfigParam = (params: ModelParameter[]) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.parameters) return;

	for (let i = 0; i < state.parameters.length; i++) {
		const foundParam = params.find((p) => p.id === state.parameters![i].id);
		if (foundParam) {
			state.parameters[i] = foundParam;
		}
	}
};

const updateConfigInitial = (inits: Initial[]) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.initials) return;

	for (let i = 0; i < state.initials.length; i++) {
		const foundInitial = inits.find((init) => init.target === state.initials![i].target);
		if (foundInitial) {
			state.initials[i] = foundInitial;
		}
	}
	emit('update-state', state);
};

const updateFromConfig = (config: ModelConfiguration) => {
	const state = cloneDeep(props.node.state);
	state.initials = config.configuration.semantics?.ode.initials ?? [];
	state.parameters = config.configuration.semantics?.ode.parameters ?? [];
	state.timeseries = config.configuration?.metadata?.timeseries ?? {};
	emit('update-state', state);
};

const createConfiguration = async () => {
	console.log('Creating config');
	if (!model.value) return;

	const state = _.cloneDeep(props.node.state);

	const data = await createModelConfiguration(
		model.value.id,
		knobs.value.name,
		configDescription.value,
		modelConfiguration.value?.configuration
	);
	console.log('Model config created:');
	console.log(data);

	if (!data) {
		logger.error('Failed to create model configuration');
		return;
	}

	useToastService().success('', 'Created model configuration');
	emit('append-output-port', {
		type: ModelConfigOperation.outputs[0].type,
		label: state.name,
		value: data.id,
		isSelected: false,
		state
	});

	setTimeout(async () => {
		const modelConfig = await getModelConfigurationById(data.id);
		// TODO: do we need to cache here at all?
		// model configs aren't too large so we could potentially just fetch each time
		configCache.value[modelConfig.id ?? ''] = modelConfig;
	}, 1000);
};

const debouncedUpdateState = _.debounce(updateState, 500);

const lazyLoadModelConfig = async (configId: string) => {
	if (!configId || configCache.value[configId]) return;

	const config = await getModelConfigurationById(configId);
	if (config) {
		configCache.value[configId] = config;
	}
};

const onUpdateOutput = (id) => {
	console.log(id);
	emit('select-output', id);
};
const onUpdateSelection = (id) => {
	console.log(id);
	const outputPort = _.cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
};

// Creates a temp config (if doesnt exist in state)
// This is used for beaker context when there are no outputs in the node
const createTempModelConfig = async () => {
	console.log('Create temp model');
	console.log(modelConfiguration.value?.configuration);
	const state = _.cloneDeep(props.node.state);
	if (state.tempConfigId !== '' || !model.value) return;
	const data = await createModelConfiguration(
		model.value.id,
		knobs.value.name,
		configDescription.value,
		modelConfiguration.value?.configuration
	);
	state.tempConfigId = data.id;
	emit('update-state', state);
};

// Fill the form with the config data
const initialize = () => {
	knobs.value.name = props.node.state.name;
	configDescription.value = props.node.state.description;
	configInitials.value = props.node.state.initials;
	configParams.value = props.node.state.parameters;
	configTimeSeries.value = props.node.state.timeseries;
};

watch(
	() => props.node.state,
	() => {
		configInitials.value = props.node.state.initials;
		configParams.value = props.node.state.parameters;
		configTimeSeries.value = props.node.state.timeseries;
	},
	{ immediate: true, deep: true }
);

// TODO:
// watch(
// 	() => props.node.inputs[0],
// 	async () => {
// 		initialize()
// 	},
// 	{ immediate: true }
// )

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
		initialize();
	},
	{ immediate: true, deep: true }
);

watch(
	() => selectedConfigId.value,
	() => {
		lazyLoadModelConfig(selectedConfigId.value);
	},
	{ immediate: true }
);

onMounted(async () => {
	const input = props.node.inputs[0];
	if (input.value) {
		const m = await getModel(input.value[0]);
		if (m) {
			model.value = m;
			if (isEmpty(outputs.value)) {
				const state = _.cloneDeep(props.node.state);
				state.initials = m.semantics?.ode.initials;
				state.parameters = m.semantics?.ode.parameters;
				emit('update-state', state);
			}
			// Create a new session and context based on model
			try {
				await createTempModelConfig();
				const jupyterContext = buildJupyterContext();
				if (jupyterContext) {
					await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
				}
			} catch (error) {
				logger.error(`Error initializing Jupyter session: ${error}`);
			}
		}
	}
});
</script>

<style scoped>
.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}

.context-item {
	width: 100%;
}
</style>
