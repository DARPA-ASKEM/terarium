<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="StratifyTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
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
				</div>
				<template #footer>
					<Button outlined label="Stratify" icon="pi pi-play" @click="stratifyModel" />
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
					<Button
						outlined
						style="margin-right: auto"
						label="Run"
						icon="pi pi-play"
						@click="runCodeStratify"
					/>
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview>
				<div>
					<template v-if="amr">
						<tera-model-diagram
							ref="teraModelDiagramRef"
							:model="amr"
							:is-editable="false"
							:model-configuration="modelConfig!"
						/>
						<TeraModelSemanticTables :model="amr" :is-editable="false" />
					</template>
					<div v-else>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
						<h4>No Model Provided</h4>
					</div>
				</div>
				<template #footer>
					<Button
						:disabled="!amr"
						outlined
						label="Save as new Model"
						@click="isNewModelModalVisible = true"
					/>
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-modal v-if="isNewModelModalVisible">
		<template #header>
			<h4>Save as a new model</h4>
		</template>
		<form @submit.prevent>
			<label for="new-model">Model name</label>
			<InputText
				id="new-model"
				type="text"
				v-model="newModelName"
				placeholder="Enter a unique name for your model"
			/>
		</form>
		<template #footer>
			<Button
				label="Save"
				@click="() => saveNewModel(newModelName, { addToProject: true, appendOutputPort: true })"
			/>
			<Button class="p-button-secondary" label="Cancel" @click="isNewModelModalVisible = false" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import type { Model, ModelConfiguration } from '@/types/Types';
import { AssetType } from '@/types/Types';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import teraStratificationGroupForm from '@/components/stratification/tera-stratification-group-form.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useProjects } from '@/composables/project';
import { createModel, getModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { logger } from '@/utils/logger';
import _ from 'lodash';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { v4 as uuidv4 } from 'uuid';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';

/* Jupyter imports */
import { KernelSessionManager } from '@/services/jupyter';
import {
	blankStratifyGroup,
	StratifyGroup,
	StratifyOperationStateMira
} from './stratify-mira-operation';

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);

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

const isNewModelModalVisible = ref(false);
const newModelName = ref('');

const modelConfig = computed<ModelConfiguration | null>(() => {
	if (!amr.value || !amr.value.id) return null;
	return {
		id: 'temporary config',
		name: 'temporary config',
		model_id: amr.value.id,
		configuration: {
			header: _.cloneDeep(amr.value.header),
			model: _.cloneDeep(amr.value.model),
			semantics: _.cloneDeep(amr.value.semantics),
			metadata: _.cloneDeep(amr.value.metadata)
		}
	};
});

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
		.register('reset_response', handleResetResponse)
		.register('model_preview', handleModelPreview);
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

	kernelManager.sendMessage('reset_request', {}).register('reset_response', () => {
		kernelManager
			.sendMessage('stratify_request', messageContent)
			.register('stratify_response', handleStratifyResponse)
			.register('model_preview', handleModelPreview);
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
		await projectResource.addAsset(AssetType.Model, modelData.id, projectId);
	}

	if (options.appendOutputPort) {
		emit('append-output', {
			id: uuidv4(),
			label: modelName,
			type: 'modelId',
			value: [modelData.id]
		});
		emit('close');
	}

	isNewModelModalVisible.value = false;
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

	kernelManager.sendMessage('reset_request', {}).register('reset_response', () => {
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
			})
			.register('model_preview', (data) => {
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

// FIXME: Copy pasted in 3 locations, could be written cleaner and in a service
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
.code-executed-warning {
	background-color: #ffe6e6;
	color: #cc0000;
	padding: 10px;
	border-radius: 4px;
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
}
</style>
