<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="StratifyTabs.Wizard">
			<tera-drilldown-section>
				<h4>Stratify Model <i class="pi pi-info-circle" /></h4>
				<p>The model will be stratified with the following settings.</p>
				<p v-if="node.state.hasCodeBeenRun" class="code-executed-warning">
					Note: Code has been executed which may not be reflected here.
				</p>
				<tera-stratification-group-form
					:modelNodeOptions="modelNodeOptions"
					:config="node.state.strataGroup"
					@update-self="updateStratifyGroupForm"
				/>
				<template #footer>
					<Button label="Stratify" @click="stratifyModel" />
					<Button style="margin-right: auto" label="Reset" @click="resetModel" />
				</template>
			</tera-drilldown-section>
		</div>
		<div :tabName="StratifyTabs.Notebook">
			<tera-drilldown-section>
				<h4>Code Editor - Python</h4>
				<v-ace-editor
					v-model:value="codeText"
					@init="initialize"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>

				<template #footer>
					<Button style="margin-right: auto" label="Run" @click="runCodeStratify" />
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview>
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
import { watch, ref, onUnmounted, onMounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import teraStratificationGroupForm from '@/components/stratification/tera-stratification-group-form.vue';
import { Model, AssetType } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { getModel, createModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

/* Jupyter imports */
import { KernelSessionManager } from '@/services/jupyter';
import {
	StratifyOperationStateMira,
	StratifyGroup,
	blankStratifyGroup
} from './stratify-mira-operation';

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum StratifyTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface SaveOptions {
	addToProject?: boolean;
	appendOutputPort?: boolean;
}

const kernelManager = new KernelSessionManager();

const amr = ref<Model | null>(null);
const modelNodeOptions = ref<string[]>([]);
const teraModelDiagramRef = ref();
const newModelName = ref('');

let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

const updateStratifyGroupForm = (config: StratifyGroup) => {
	const state = _.cloneDeep(props.node.state);
	state.strataGroup = config;
	emit('update-state', state);
};

const stratifyModel = () => {
	stratifyRequest();
};

const resetModel = () => {
	if (!amr.value) return;

	kernelManager
		.sendMessage('reset_request', {})
		?.register('reset_response', handleResetResponse)
		?.register('model_preview', handleModelPreview);
};

const handleResetResponse = (data: any) => {
	if (data.content.success) {
		updateStratifyGroupForm(blankStratifyGroup);

		codeText.value = '';
		saveCodeToState('', false);

		logger.info('Model reset');
	} else {
		logger.error('Error resetting model');
	}
};

const stratifyRequest = () => {
	if (!amr.value) return;

	const strataOption = props.node.state.strataGroup;
	const messageContent = {
		stratify_args: {
			key: strataOption.name,
			strata: strataOption.groupLabels.split(',').map((d) => d.trim()),
			concepts_to_stratify: strataOption.selectedVariables,
			cartesian_control: strataOption.cartesianProduct
		}
	};

	kernelManager.sendMessage('reset_request', {})?.register('reset_response', () => {
		kernelManager
			.sendMessage('stratify_request', messageContent)
			?.register('stratify_response', handleStratifyResponse)
			?.register('model_preview', handleModelPreview);
	});
};

const handleStratifyResponse = (data: any) => {
	const executedCode = data.content.executed_code;
	if (executedCode) {
		codeText.value = executedCode;

		// If stratify is run from the wizard, save the code but set `hasCodeBeenRun` to false
		saveCodeToState(executedCode, false);
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
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

	amr.value = await getModel(modelId);
	if (!amr.value) return;

	const modelColumnNameOptions: string[] = amr.value.model.states.map((state: any) => state.id);
	// add observables
	if (amr.value.model.semantics?.ode?.observables) {
		amr.value.model.semantics.ode.observables.forEach((o) => {
			modelColumnNameOptions.push(o.id);
		});
	}
	modelNodeOptions.value = modelColumnNameOptions;

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
		await projectResource.addAsset(AssetType.Models, modelData.id, projectId);
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

const runCodeStratify = () => {
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

	kernelManager.sendMessage('reset_request', {})?.register('reset_response', () => {
		kernelManager
			.sendMessage('execute_request', messageContent)
			?.register('execute_input', (data) => {
				executedCode = data.content.code;
			})
			?.register('stream', (data) => {
				console.log('stream', data);
			})
			?.register('error', (data) => {
				logger.error(`${data.content.ename}: ${data.content.evalue}`);
			})
			?.register('model_preview', (data) => {
				// TODO: https://github.com/DARPA-ASKEM/terarium/issues/2305
				// currently no matter what kind of code is run we always get a `model_preview` response.
				// We may want to compare the response model with the existing model to see if the response model
				// has been stratified - if not then don't save the model or the code.
				handleModelPreview(data);

				if (executedCode) {
					saveCodeToState(executedCode, true);
				}
			});
	});
};

const saveCodeToState = (code: string, hasCodeBeenRun: boolean) => {
	const state = _.cloneDeep(props.node.state);
	state.hasCodeBeenRun = hasCodeBeenRun;

	// for now only save the last code executed, may want to save all code executed in the future
	const codeHistoryLength = props.node.state.strataCodeHistory.length;
	const timestamp = Date.now();
	if (codeHistoryLength > 0) {
		state.strataCodeHistory[0] = { code, timestamp };
	} else {
		state.strataCodeHistory.push({ code, timestamp });
	}

	emit('update-state', state);
};

// Set model, modelConfiguration, modelNodeOptions
watch(
	() => props.node.inputs[0],
	async () => {
		await inputChangeHandler();
	},
	{ immediate: true }
);

onMounted(() => {
	const codeHistoryLength = props.node.state.strataCodeHistory.length;
	if (codeHistoryLength > 0) {
		codeText.value = props.node.state.strataCodeHistory[codeHistoryLength - 1].code;
	}
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
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
