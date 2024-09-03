<template>
	<tera-drilldown
		ref="drilldownRef"
		v-bind="$attrs"
		:node="node"
		:menu-items="menuItems"
		:is-draft="isDraft"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update-output-port="(output: any) => emit('update-output-port', output)"
	>
		<tera-drilldown-section :tabName="DrilldownTabs.Notebook" class="notebook-section">
			<div class="toolbar">
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:default-options="sampleAgentQuestions"
						:context-language="contextLanguage"
						@llm-output="(data: any) => appendCode(data, 'code')"
						@llm-thought-output="(data: any) => llmThoughts.push(data)"
						@question-asked="updateLlmQuery"
					>
						<template #toolbar-right-side>
							<Button label="Reset" outlined severity="secondary" size="small" @click="resetModel" />
							<Button
								icon="pi pi-play"
								:label="isUpdatingModel ? 'Loading...' : 'Run'"
								size="small"
								:loading="isUpdatingModel"
								@click="runCode"
							/>
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
		<template #preview v-if="drilldownRef?.selectedTab === DrilldownTabs.Notebook">
			<tera-drilldown-preview
				title="Preview"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:options="outputs"
				is-selectable
			>
				<section class="right-side">
					<Button
						class="mr-3"
						outlined
						severity="secondary"
						:disabled="isSaveDisabled"
						label="Save as reuse"
						@click="showSaveModelModal = true"
					/>
				</section>
				<tera-notebook-error
					v-if="executeResponse.status === OperatorStatus.ERROR"
					:name="executeResponse.name"
					:value="executeResponse.value"
					:traceback="executeResponse.traceback"
				/>
				<template v-else-if="amr">
					<tera-model-diagram :model="amr" />
					<tera-model-parts :model="amr" :feature-config="{ isPreview: true }" />
				</template>
				<tera-progress-spinner v-else-if="isUpdatingModel || !amr" is-centered :font-size="2">
					Loading...
				</tera-progress-spinner>
			</tera-drilldown-preview>
		</template>
		<tera-drilldown-section :tabName="DrilldownTabs.Wizard">
			<!-- <tera-model-template-editor v-if="amr"
					:model="amr"
					:kernel-manager="kernelManager"
					@output-code="(data: any) => appendCode(data, 'executed_code')"
					@sync-with-mira-model="syncWithMiraModel"
					@save-new-model-output="createOutput"
					@reset="resetModel"
				/> -->
			<p class="m-4">Wizard is disabled for now.</p>
		</tera-drilldown-section>
	</tera-drilldown>
	<tera-save-asset-modal
		v-if="amr"
		:asset="amr"
		:initial-name="amr.name"
		:assetType="AssetType.Model"
		:is-overwriting="true"
		:is-visible="showSaveModelModal"
		@close-modal="showSaveModelModal = false"
		@on-save="updateNode"
	/>
</template>

<script setup lang="ts">
import { cloneDeep, isEmpty, isEqual, debounce } from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import '@/ace-config';
import type { Model } from '@/types/Types';
import { AssetType } from '@/types/Types';
import { createModel, getModel } from '@/services/model';
import { OperatorStatus, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { logger } from '@/utils/logger';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelParts from '@/components/model/tera-model-parts.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
// import TeraModelTemplateEditor from '@/components/model-template/tera-model-template-editor.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';

import { KernelSessionManager } from '@/services/jupyter';
import { getModelIdFromModelConfigurationId } from '@/services/model-configurations';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import { saveCodeToState } from '@/services/notebook';
import { DrilldownTabs } from '@/types/common';
import { nodeOutputLabel } from '@/components/workflow/util';
import { useProjects } from '@/composables/project';
import { ModelEditOperationState, ModelEditOperation } from './model-edit-operation';

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close', 'select-output', 'update-output-port']);

const outputs = computed(() => {
	if (!isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select an output',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const isReadyToCreateDefaultOutput = computed(
	() => isEmpty(outputs.value) || (outputs.value.length === 1 && !outputs.value?.[0]?.items[0].value)
);

const drilldownRef = ref();
const selectedOutputId = ref<string>('');
const activeOutput = ref<WorkflowOutput<ModelEditOperationState> | null>(null);
const isUpdatingModel = ref(false);

const kernelManager = new KernelSessionManager();
const amr = ref<Model | null>(null);
let activeModelId: string | null = null;
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
	'Change rate law of inf to S * I * z.',
	'Add a new parameter with id θ and value 0.5.'
];

const contextLanguage = ref<string>('python3');

const defaultCodeText = '# This environment contains the variable "model" \n# which is displayed on the right';
const codeText = ref(defaultCodeText);
const llmQuery = ref('');
const llmThoughts = ref<any[]>([]);

const isDraft = ref(false);

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

const updateLlmQuery = (query: string) => {
	llmThoughts.value = [];
	llmQuery.value = query;
};

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
	amr.value = updatedModel;
	isUpdatingModel.value = false;
	createOutput(amr.value as Model);
};

const runCode = () => {
	isUpdatingModel.value = true;
	amr.value = null;
	kernelManager.sendMessage('reset_request', {}).register('reset_response', () => {
		const messageContent = {
			silent: false,
			store_history: false,
			user_expressions: {},
			allow_stdin: true,
			stop_on_error: false,
			code: editor?.getValue() as string
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
	const state = saveCodeToState(props.node, code, hasCodeRun, llmQuery.value, llmThoughts.value);
	emit('update-state', state);
}

const createOutput = async (modelToSave: Model) => {
	// If it's the original model, use that otherwise create a new one
	const modelData = isReadyToCreateDefaultOutput.value ? modelToSave : await createModel(modelToSave);
	if (!modelData) return;

	const modelLabel = isReadyToCreateDefaultOutput.value
		? (modelData.name as string)
		: nodeOutputLabel(props.node, 'Output'); // Just label the original model with its name

	emit('append-output', {
		label: modelLabel,
		type: ModelEditOperation.outputs[0].type,
		state: cloneDeep(props.node.state),
		value: [modelData.id]
	});

	isDraft.value = false;
};

const buildJupyterContext = () => {
	if (!activeModelId) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}
	return {
		context: 'mira_model_edit',
		language: 'python3',
		context_info: {
			id: activeModelId
		}
	};
};

const handleOutputChange = async () => {
	// Switch to model from output
	activeModelId = activeOutput.value?.value?.[0];
	if (!activeModelId) return;
	codeText.value = props.node.state.notebookHistory?.[0]?.code ?? defaultCodeText;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);

			// Get the model after the kernel is ready so function in model-template-editor can be triggered with an existing kernel
			amr.value = await getModel(activeModelId);
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

// check if user has made changes to the code
const hasCodeChange = () => {
	if (props.node.state.notebookHistory.length) {
		isDraft.value = !isEqual(codeText.value, props.node.state.notebookHistory?.[0]?.code);
	} else {
		isDraft.value = !isEqual(codeText.value, defaultCodeText);
	}
};
const checkForCodeChange = debounce(hasCodeChange, 500);

watch(
	() => codeText.value,
	() => checkForCodeChange()
);

// Updates output selection
watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			activeOutput.value = props.node.outputs.find((d) => d.id === selectedOutputId.value) ?? null;
			handleOutputChange();
		}
	},
	{ immediate: true }
);

const isSaveDisabled = computed(() => {
	const id = amr.value?.id;
	if (!id) return true;
	const outputPort = props.node.outputs?.find((port) => port.value?.[0] === id);
	return useProjects().hasAssetInActiveProject(outputPort?.value?.[0]);
});

function updateNode(model: Model) {
	const id = amr.value?.id;
	if (!id || !model) return;

	amr.value = model;
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.value?.[0] === id));

	if (!outputPort) return;
	outputPort.label = model.header.name;

	emit('update-output-port', outputPort);
}

onMounted(async () => {
	// By default the first output option is the original model
	if (isReadyToCreateDefaultOutput.value) {
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
});

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

.notebook-section:deep(main .toolbar) {
	padding-left: var(--gap-medium);
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
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

.right-side {
	display: flex;
	justify-content: right;
}
</style>
