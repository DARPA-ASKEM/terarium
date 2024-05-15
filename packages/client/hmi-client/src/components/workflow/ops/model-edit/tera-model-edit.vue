<template>
	<tera-drilldown
		:node="node"
		:menu-items="menuItems"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div :tabName="ModelEditTabs.Wizard">
			<tera-model-template-editor
				v-if="amr && isKernelReady"
				:model="amr"
				:kernel-manager="kernelManager"
				@output-code="(data: any) => appendCode(data, 'executed_code')"
				@sync-with-mira-model="syncWithMiraModel"
			/>
		</div>
		<div :tabName="ModelEditTabs.Notebook">
			<tera-drilldown-section class="notebook-section">
				<div class="toolbar">
					<Suspense>
						<tera-notebook-jupyter-input
							:kernel-manager="kernelManager"
							:default-options="sampleAgentQuestions"
							:context-language="contextLanguage"
							@llm-output="(data: any) => appendCode(data, 'code')"
							@llm-thought-output="(data: any) => llmThoughts.push(data)"
							@question-asked="llmThoughts = []"
						>
							<template #toolbar-right-side>
								<Button
									label="Reset"
									outlined
									severity="secondary"
									size="small"
									@click="resetModel"
								/>
								<Button icon="pi pi-play" label="Run" size="small" @click="runFromCodeWrapper" />
							</template>
						</tera-notebook-jupyter-input>
					</Suspense>
					<tera-notebook-jupyter-thought-output :llm-thoughts="llmThoughts" />
				</div>
				<v-ace-editor
					v-model:value="codeText"
					@init="initializeAceEditor"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>
			</tera-drilldown-section>
			<div class="preview-container">
				<tera-drilldown-preview
					title="Preview"
					v-model:output="selectedOutputId"
					@update:selection="onSelection"
					:options="outputs"
					is-selectable
					class="h-full"
				>
					<tera-notebook-error
						v-if="executeResponse.status === OperatorStatus.ERROR"
						:name="executeResponse.name"
						:value="executeResponse.value"
						:traceback="executeResponse.traceback"
					/>
					<tera-model-diagram v-else-if="amr" :model="amr" :is-editable="true" />
					<div v-else>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
					</div>
				</tera-drilldown-preview>
			</div>
		</div>
	</tera-drilldown>
	<tera-save-asset-modal
		v-if="amr"
		:model="amr"
		:is-visible="showSaveModelModal"
		@close-modal="showSaveModelModal = false"
	/>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import '@/ace-config';
import { v4 as uuidv4 } from 'uuid';
import type { Model } from '@/types/Types';
import { getModel, createModel, updateModel } from '@/services/model';
import { WorkflowNode, WorkflowOutput, OperatorStatus } from '@/types/workflow';
import { logger } from '@/utils/logger';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraModelTemplateEditor from '@/components/model-template/tera-model-template-editor.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';

import { KernelSessionManager } from '@/services/jupyter';
import { getModelIdFromModelConfigurationId } from '@/services/model-configurations';
import TeraSaveAssetModal from '@/page/project/components/tera-save-asset-modal.vue';
import { saveCodeToState } from '@/services/notebook';
import { ModelEditOperationState } from './model-edit-operation';

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close', 'select-output']);

enum ModelEditTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

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
const selectedOutputId = ref<string>('');
const activeOutput = ref<WorkflowOutput<ModelEditOperationState> | null>(null);

const kernelManager = new KernelSessionManager();
const isKernelReady = ref(false);
const amr = ref<Model | null>(null);
let activeModelId: string | null = null;
const readyToSaveOutputModel = ref(false);
const showSaveModelModal = ref(false);

let editor: VAceEditorInstance['_editor'] | null;
const sampleAgentQuestions = [
	'Add a new transition from S to R with the name vaccine with the rate of v and unit Days.',
	'Add a new transition from I to D. Name the transition death that has a dependency on R. The rate is I*R*u with unit Days',
	'Add a new transition (from nowhere) to S with a rate constant of f with unit Days.',
	'Add a new transition (from nowhere) to S with a rate constant of f with unit Days. The rate depends on R.',
	'Add a new transition from S (to nowhere) with a rate constant of v with unit Days',
	'Add a new transition from S (to nowhere) with a rate constant of v with unit Days. The Rate depends on R',
	'Add an observable titled sample with the expression A * B  * p.',
	'Rename the state S to Susceptible in the infection transition.',
	'Rename the transition infection to inf.',
	'Change rate law of inf to S * I * z.'
];

const contextLanguage = ref<string>('python3');

const defaultCodeText =
	'# This environment contains the variable "model" \n# which is displayed on the right';
const codeText = ref(defaultCodeText);
const llmThoughts = ref<any[]>([]);

const executeResponse = ref({
	status: OperatorStatus.DEFAULT,
	name: '',
	value: '',
	traceback: ''
});

const menuItems = computed(() => [
	{
		label: 'Save as new model',
		icon: 'pi pi-pencil',
		command: () => {
			showSaveModelModal.value = true;
		}
	}
]);

const appendCode = (data: any, property: string) => {
	const newCode = data.content[property] as string;
	if (newCode) {
		codeText.value = (codeText.value ?? defaultCodeText).concat(' \n', newCode);

		if (property === 'executed_code') {
			updateCodeState();
		}
	} else {
		logger.error('No code to append');
	}
};

const syncWithMiraModel = (data: any) => {
	const updatedModel = data.content?.['application/json'];
	if (!updatedModel || !activeModelId) {
		logger.error('Error getting updated model from beaker');
		return;
	}
	updatedModel.id = activeModelId;
	const firstOutputId = outputs.value?.[0].items[0].id;
	// If the first output is edited, create a new output
	if (firstOutputId === selectedOutputId.value) {
		createOutput(updatedModel);
	} else {
		amr.value = updatedModel;
		readyToSaveOutputModel.value = true;
	}
};

// Reset model, then execute the code
const runFromCodeWrapper = () => {
	// Reset model
	kernelManager.sendMessage('reset_request', {}).register('reset_response', () => {
		runFromCode(editor?.getValue() as string);
	});
};

const runFromCode = (code: string) => {
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
		.register('model_preview', (data) => {
			if (!data.content) return;
			syncWithMiraModel(data);

			if (executedCode) {
				updateCodeState(executedCode);
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

const resetModel = () => {
	if (!amr.value) return;

	kernelManager
		.sendMessage('reset_request', {})
		.register('reset_response', handleResetResponse)
		.register('model_preview', syncWithMiraModel);
};

const handleResetResponse = (data: any) => {
	if (data.content.success) {
		// updateStratifyGroupForm(blankStratifyGroup);

		codeText.value = defaultCodeText;
		updateCodeState('', false);

		logger.info('Model reset');
	} else {
		logger.error('Error resetting model');
	}
};

const initializeAceEditor = (editorInstance: any) => {
	editor = editorInstance;
};

function updateCodeState(code: string = codeText.value, hasCodeRun: boolean = true) {
	const state = saveCodeToState(props.node, code, hasCodeRun);
	emit('update-state', state);
}

// Saves the output model in the backend
// Not called after every little model edit to avoid too many requests
// Called when the selected output is changed, component unmounts or before the window is closed
function updateOutputModel() {
	if (readyToSaveOutputModel.value && amr.value) {
		updateModel(amr.value);
		readyToSaveOutputModel.value = false;
	}
}

const createOutput = async (modelToSave: Model) => {
	// If it's the original model, use that otherwise create a new one
	const modelData = isEmpty(outputs.value) ? modelToSave : await createModel(modelToSave);
	if (!modelData) return;

	emit('append-output', {
		id: uuidv4(),
		label: isEmpty(outputs.value) ? modelData.name : `Output ${Date.now()}`, // Just label the original model with its name
		type: 'modelId',
		state: cloneDeep(props.node.state),
		value: [modelData.id]
	});
};

const buildJupyterContext = () => {
	if (!amr.value) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}
	return {
		context: 'mira_model_edit',
		language: 'python3',
		context_info: {
			id: amr.value.id
		}
	};
};

const handleOutputChange = async () => {
	// Switch to model from output
	activeModelId = activeOutput.value?.value?.[0];
	if (!activeModelId) return;
	amr.value = await getModel(activeModelId);
	if (!amr.value) return;

	codeText.value = props.node.state.notebookHistory[0]?.code ?? defaultCodeText;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
			isKernelReady.value = true;
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const onSelection = (id: string) => {
	updateOutputModel(); // Save the model before switching to the new one
	emit('select-output', id);
};

// Updates output selection
watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			activeOutput.value = props.node.outputs.find((d) => d.id === selectedOutputId.value) ?? null;
			await handleOutputChange();
		}
	},
	{ immediate: true }
);

onMounted(async () => {
	// By default the first output option is the original model
	if (isEmpty(outputs.value)) {
		const input = props.node.inputs[0];
		if (!input) return;

		// Get input model id
		let modelId: string | null = null;
		if (input.type === 'modelId') {
			modelId = input.value?.[0];
		} else if (input.type === 'modelConfigId') {
			modelId = await getModelIdFromModelConfigurationId(input.value?.[0]);
		}
		if (!modelId) return;

		// Get model
		const originalModel = await getModel(modelId);
		if (!originalModel) return;

		// Set default output which is the input (original model)
		createOutput(originalModel);
	}
	window.addEventListener('beforeunload', updateOutputModel);
});

onUnmounted(() => {
	updateOutputModel();
	kernelManager.shutdown();
});
</script>

<style scoped>
/* The wizard of this operator is atypical and needs the outside margins to be removed
	TODO: This case should be handled in the tera-drilldown component or something as it messes with the padding in the notebook tab */
.overlay-container:deep(main) {
	padding: 0 0 0 0;
}

.input {
	width: 95%;
}

.code-container {
	display: flex;
	flex-direction: column;
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
}

.notebook-section:deep(main .toolbar) {
	padding-left: var(--gap-medium);
}

.preview-container {
	display: flex;
	flex-direction: column;
	padding: 1rem;
}

:deep(.diagram-container) {
	height: calc(100vh - 270px) !important;
}
:deep(.resize-handle) {
	display: none;
}
.input-small {
	padding: 0.5rem;
	width: 100%;
}

.code-executed-warning {
	background-color: #ffe6e6;
	color: #cc0000;
	padding: 10px;
	border-radius: 4px;
}
</style>
