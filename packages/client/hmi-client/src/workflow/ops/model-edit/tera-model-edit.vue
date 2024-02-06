<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="ModelEditTabs.Wizard">
			<tera-drilldown-section>
				<template #footer>
					<Button style="margin-right: auto" label="Reset" @click="resetModel" />
				</template>
			</tera-drilldown-section>
		</div>
		<div :tabName="ModelEditTabs.Notebook">
			<tera-drilldown-section>
				<h4>Code Editor - Python</h4>
				<Suspense>
					<tera-notebook-jupyter-input
						context="mira_model_edit"
						:contextInfo="contextInfo"
						@llm-output="getOutputFromLLM"
					/>
				</Suspense>
				<v-ace-editor
					v-model:value="codeText"
					@init="initialize"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>
				<template #footer>
					<Button style="margin-right: auto" label="Run" @click="runFromCodeWrapper" />
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview
				title="Model Preview"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				:options="outputs"
				is-selectable
			>
				<div>
					<tera-model-diagram
						v-if="amr"
						ref="teraModelDiagramRef"
						:model="amr"
						:is-editable="false"
					/>
					<div v-else>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
						<h4>No Model Provided</h4>
					</div>
				</div>
				<template #footer>
					<InputText
						v-model="newModelName"
						placeholder="model name"
						type="text"
						class="input-small"
					/>
					<Button
						:disabled="!amr"
						outlined
						style="margin-right: auto"
						label="Save as new Model"
						@click="
							() => saveNewModel(newModelName, { addToProject: true, appendOutputPort: true })
						"
					/>
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { onUnmounted, ref, watch, computed } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import type { Model } from '@/types/Types';
import { AssetType } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { createModel, getModel } from '@/services/model';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { KernelSessionManager } from '@/services/jupyter';
import teraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { ModelEditOperationState } from './model-edit-operation';

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();
const emit = defineEmits([
	'append-output-port',
	'update-state',
	'close',
	'select-output',
	'update-output-port'
]);

enum ModelEditTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface SaveOptions {
	addToProject?: boolean;
	appendOutputPort?: boolean;
}

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
const selectedOutputId = ref<string>();
const activeOutput = ref<WorkflowOutput<ModelEditOperationState> | null>(null);

const kernelManager = new KernelSessionManager();
const amr = ref<Model | null>(null);
const modelId = props.node.inputs[0].value?.[0];
const contextInfo = { id: modelId }; // context for jupyter-input
const teraModelDiagramRef = ref();
const newModelName = ref('');
let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref(
	'# This environment contains the variable "model" \n# which is displayed on the right'
);

const getOutputFromLLM = (data) => {
	codeText.value = codeText.value.concat(' \n', data.value.content.code as string);
};

// Reset model, then execute the code
const runFromCodeWrapper = () => {
	const code = editor?.getValue();
	if (!code) return;

	// Reset model
	kernelManager.sendMessage('reset_request', {}).register('reset_response', () => {
		runFromCode();
	});
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

const resetModel = () => {
	if (!amr.value) return;

	kernelManager
		.sendMessage('reset_request', {})
		.register('reset_response', handleResetResponse)
		.register('model_preview', handleModelPreview);
};

const handleResetResponse = (data: any) => {
	if (data.content.success) {
		// updateStratifyGroupForm(blankStratifyGroup);

		codeText.value = '';
		saveCodeToState('', false);

		logger.info('Model reset');
	} else {
		logger.error('Error resetting model');
	}
};

const handleModelPreview = (data: any) => {
	amr.value = data.content['application/json'];
};

const buildJupyterContext = () => {
	if (!amr.value) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}

	return {
		context: 'mira_model',
		language: 'python3',
		context_info: {
			id: amr.value.id
		}
	};
};

const inputChangeHandler = async () => {
	if (!modelId) return;

	amr.value = await getModel(modelId);
	if (!amr.value) return;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const saveNewModel = async (modelName: string, options: SaveOptions) => {
	if (!amr.value || !modelName) return;
	amr.value.header.name = modelName;

	const projectResource = useProjects();
	const modelData = await createModel(amr.value);
	const projectId = projectResource.activeProject.value?.id;

	if (!modelData) return;

	if (options.addToProject) {
		await projectResource.addAsset(AssetType.Model, modelData.id, projectId);
	}

	if (options.appendOutputPort) {
		emit('append-output-port', {
			id: uuidv4(),
			label: modelName,
			type: 'modelId',
			value: [modelData.id]
		});
		emit('close');
	}
};

const initialize = (editorInstance: any) => {
	editor = editorInstance;
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

const onUpdateOutput = (id: string) => {
	emit('select-output', id);
};

const onUpdateSelection = (id) => {
	const outputPort = _.cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
};

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			activeOutput.value = props.node.outputs.find((d) => d.id === props.node.active) as any;
			selectedOutputId.value = props.node.active;
			inputChangeHandler();
		}
	},
	{ immediate: true }
);

// Set model, modelConfiguration, modelNodeOptions
watch(
	() => props.node.inputs[0],
	async () => {
		await inputChangeHandler();
	},
	{ immediate: true }
);

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
.input {
	width: 95%;
}
.code-container {
	display: flex;
	flex-direction: column;
}

.input-small {
	padding: 0.5rem;
}

.code-executed-warning {
	background-color: #ffe6e6;
	color: #cc0000;
	padding: 10px;
	border-radius: 4px;
}
</style>
