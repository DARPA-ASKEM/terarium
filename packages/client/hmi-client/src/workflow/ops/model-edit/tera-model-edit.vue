<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
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
				<div class="toolbar-right-side">
					<Button label="Reset" outlined severity="secondary" size="small" @click="resetModel" />
					<Button
						icon="pi pi-play"
						label="Run"
						outlined
						severity="secondary"
						size="small"
						@click="runFromCodeWrapper"
					/>
				</div>
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:default-options="sampleAgentQuestions"
						@llm-output="(data: any) => appendCode(data, 'code')"
					/>
				</Suspense>
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
					<tera-model-diagram v-if="amr" :model="amr" :is-editable="true" />
					<div v-else>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
					</div>
					<template #footer>
						<InputText
							v-model="newModelName"
							placeholder="model name"
							type="text"
							class="input-small"
						/>
						<div class="flex gap-2">
							<Button
								:disabled="!amr"
								size="large"
								severity="secondary"
								outlined
								class="white-space-nowrap"
								style="margin-right: auto"
								label="Save as new model"
								@click="
									() => saveNewModel(newModelName, { addToProject: true, appendOutputPort: true })
								"
							/>
							<Button label="Close" size="large" @click="emit('close')" />
						</div>
					</template>
				</tera-drilldown-preview>
			</div>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
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
import '@/ace-config';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { KernelSessionManager } from '@/services/jupyter';
import TeraModelTemplateEditor from '@/components/model-template/tera-model-template-editor.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import { ModelEditOperationState } from './model-edit-operation';

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close', 'select-output']);

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
const isKernelReady = ref(false);
const amr = ref<Model | null>(null);
const modelId = props.node.inputs[0].value?.[0];
const newModelName = ref('');
let editor: VAceEditorInstance['_editor'] | null;
const sampleAgentQuestions = [
	'Add a new transition from S to R with the name vaccine with the rate of v.',
	'Add a new transition from I to D. Name the transition death that has a dependency on R. The rate is I*R*u',
	'Add a new transition (from nowhere) to S with a rate constant of f.',
	'Add a new transition (from nowhere) to S with a rate constant of f. The rate depends on R.',
	'Add a new transition from S (to nowhere) with a rate constant of v',
	'Add a new transition from S (to nowhere) with a rate constant of v. The Rate depends on R',
	'Add an observable titled sample with the expression A * B  * p.',
	'Rename the state S to Susceptible in the infection transition.',
	'Rename the transition infection to inf.'
];

const defaultCodeText =
	'# This environment contains the variable "model" \n# which is displayed on the right';
const codeText = ref(defaultCodeText);

const appendCode = (data: any, property: string) => {
	const code = data.content[property] as string;
	if (code) {
		codeText.value = (codeText.value ?? defaultCodeText).concat(' \n', code);
	} else {
		logger.error('No code to append');
	}
};

const syncWithMiraModel = (data: any) => {
	amr.value = data.content['application/json'];
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
		.register('error', (data) => {
			logger.error(`${data.content.ename}: ${data.content.evalue}`);
			console.log('error', data.content);
		})
		.register('model_preview', (data) => {
			if (!data.content) return;

			syncWithMiraModel(data);

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
		.register('model_preview', syncWithMiraModel);
};

const handleResetResponse = (data: any) => {
	if (data.content.success) {
		// updateStratifyGroupForm(blankStratifyGroup);

		codeText.value = defaultCodeText;
		saveCodeToState('', false);

		logger.info('Model reset');
	} else {
		logger.error('Error resetting model');
	}
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

const inputChangeHandler = async () => {
	if (!modelId) return;

	amr.value = await getModel(modelId);
	if (!amr.value) return;

	codeText.value = props.node.state.modelEditCodeHistory?.[0]?.code ?? defaultCodeText;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
			isKernelReady.value = true;
		}

		if (codeText.value && codeText.value.length > 0) {
			runFromCodeWrapper();
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
		emit('append-output', {
			id: uuidv4(),
			label: modelName,
			type: 'modelId',
			state: _.cloneDeep(props.node.state),
			value: [modelData.id]
		});
		emit('close');
	}
};

const initializeAceEditor = (editorInstance: any) => {
	editor = editorInstance;
};

// FIXME: Copy pasted in 3 locations, could be written cleaner and in a service
const saveCodeToState = (code: string, hasCodeBeenRun: boolean) => {
	const state = _.cloneDeep(props.node.state);
	console.log(state);
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

const onSelection = (id: string) => {
	emit('select-output', id);
};

watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			activeOutput.value = props.node.outputs.find((d) => d.id === props.node.active) as any;
			selectedOutputId.value = props.node.active;

			await inputChangeHandler();
		}
	},
	{ immediate: true }
);

onMounted(async () => {
	await inputChangeHandler();
});

onUnmounted(() => {
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
	/** TODO: Temporary solution, should be using the default overlay-container padding
	 in tera-drilldown...or maybe we should consider the individual drilldowns decide on padding */
	margin-left: 1.5rem;
}

.toolbar-right-side {
	position: absolute;
	top: var(--gap);
	right: 0;
	gap: var(--gap-small);
	display: flex;
	align-items: center;
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
