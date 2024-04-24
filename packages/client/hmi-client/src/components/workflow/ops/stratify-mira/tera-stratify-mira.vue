<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div :tabName="StratifyTabs.Wizard">
			<tera-drilldown-section class="pl-4 pt-3">
				<div class="form-section">
					<h5>Stratify model</h5>
					<p>The model will be stratified with the following settings.</p>
					<p v-if="node.state.hasCodeBeenRun" class="code-executed-warning">
						Note: Code has been executed which may not be reflected here.
					</p>
					<tera-stratification-group-form
						class="mt-2"
						:model-node-options="modelNodeOptions"
						:config="node.state.strataGroup"
						@update-self="updateStratifyGroupForm"
					/>
				</div>
				<template #footer>
					<div class="flex flex-row gap-2 w-full mb-2">
						<Button
							outlined
							label="Stratify"
							size="large"
							icon="pi pi-play"
							@click="stratifyModel"
						/>
						<Button
							style="margin-right: auto"
							size="large"
							severity="secondary"
							outlined
							label="Reset"
							@click="resetModel"
						/>
					</div>
				</template>
			</tera-drilldown-section>
		</div>
		<div :tabName="StratifyTabs.Notebook">
			<tera-drilldown-section>
				<p class="mt-3 ml-4">Code Editor - Python</p>
				<v-ace-editor
					v-model:value="codeText"
					@init="initialize"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>

				<template #footer>
					<Button
						outlined
						style="margin-right: auto"
						label="Run"
						size="large"
						icon="pi pi-play"
						@click="runCodeStratify"
						class="ml-4 mb-2"
					/>
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview
				title="Preview"
				:options="outputs"
				@update:selection="onSelection"
				v-model:output="selectedOutputId"
				is-selectable
				class="mr-4 mt-3 mb-2"
			>
				<div class="h-full">
					<tera-notebook-error
						v-if="executeResponse.status === OperatorStatus.ERROR"
						:name="executeResponse.name"
						:value="executeResponse.value"
						:traceback="executeResponse.traceback"
					/>
					<template v-else-if="stratifiedAmr">
						<tera-model-diagram :model="stratifiedAmr" :is-editable="false" />
						<TeraModelSemanticTables :model="stratifiedAmr" :is-editable="false" />
					</template>
					<div v-else class="flex flex-column h-full justify-content-center">
						<tera-operator-placeholder :operation-type="node.operationType" />
					</div>
				</div>
				<template #footer>
					<Button
						:disabled="!amr"
						outlined
						severity="secondary"
						size="large"
						label="Save as new model"
						@click="isNewModelModalVisible = true"
					/>
					<Button label="Close" size="large" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-modal v-if="isNewModelModalVisible" class="save-as-dialog">
		<template #header>
			<h4>Save as a new model</h4>
		</template>
		<form @submit.prevent class="mt-3">
			<label for="new-model">What would you like to call it?</label>
			<InputText
				id="new-model"
				type="text"
				v-model="newModelName"
				placeholder="Enter a unique name for your model"
			/>
		</form>
		<template #footer>
			<Button label="Save" size="large" @click="() => saveNewModel(newModelName)" />
			<Button
				class="p-button-secondary"
				size="large"
				label="Cancel"
				@click="isNewModelModalVisible = false"
			/>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import _ from 'lodash';
import { AssetType } from '@/types/Types';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraModelSemanticTables from '@/components/model/tera-model-semantic-tables.vue';

import TeraStratificationGroupForm from '@/components/workflow/ops/stratify-mira/tera-stratification-group-form.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useProjects } from '@/composables/project';
import { createModel, getModel } from '@/services/model';
import { WorkflowNode, OperatorStatus } from '@/types/workflow';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { v4 as uuidv4 } from 'uuid';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { useToastService } from '@/services/toast';
import '@/ace-config';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import type { Model } from '@/types/Types';
import { AMRSchemaNames } from '@/types/common';
import { getModelIdFromModelConfigurationId } from '@/services/model-configurations';

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
const emit = defineEmits([
	'append-output',
	'update-state',
	'close',
	'update-output-port',
	'select-output'
]);

enum StratifyTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const amr = ref<Model | null>(null);
const stratifiedAmr = ref<Model | null>(null);
const executeResponse = ref({
	status: OperatorStatus.DEFAULT,
	name: '',
	value: '',
	traceback: ''
});
const modelNodeOptions = ref<string[]>([]);

const isNewModelModalVisible = ref(false);
const newModelName = ref('');

const selectedOutputId = ref<string>();
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

const kernelManager = new KernelSessionManager();

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

	// Sanity check states vs parameters
	const conceptsToStratify: string[] = [];
	const parametersToStratify: string[] = [];
	const strataOption = props.node.state.strataGroup;
	const { modelStates, modelParameters } = getStatesAndParameters(amr.value);

	strataOption.selectedVariables.forEach((v) => {
		if (modelStates.includes(v)) conceptsToStratify.push(v);
		if (modelParameters.includes(v)) parametersToStratify.push(v);
	});

	const messageContent = {
		stratify_args: {
			key: strataOption.name,
			strata: strataOption.groupLabels.split(',').map((d) => d.trim()),
			concepts_to_stratify: conceptsToStratify,
			params_to_stratify: parametersToStratify,
			cartesian_control: strataOption.cartesianProduct,
			structure: strataOption.useStructure === true ? null : []
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

const handleModelPreview = async (data: any) => {
	stratifiedAmr.value = data.content['application/json'];

	// Create output
	const modelData = await createModel(stratifiedAmr.value);
	if (!modelData) return;

	emit('append-output', {
		id: uuidv4(),
		label: `Output ${Date.now()}`,
		type: 'modelId',
		state: {
			strataGroup: _.cloneDeep(props.node.state.strataGroup),
			strataCodeHistory: _.cloneDeep(props.node.state.strataCodeHistory)
		},
		value: [modelData.id]
	});
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

const getStatesAndParameters = (amrModel: Model) => {
	const modelFramework = amrModel.header.schema_name;
	const modelStates: string[] = [];
	const modelParameters: string[] = [];

	const model = amrModel.model;
	const semantics = amrModel.semantics;

	if (
		(modelFramework === AMRSchemaNames.PETRINET || modelFramework === AMRSchemaNames.STOCKFLOW) &&
		semantics?.ode
	) {
		const { initials, parameters, observables } = semantics.ode;

		initials?.forEach((i) => {
			modelStates.push(i.target);
		});
		parameters?.forEach((p) => {
			modelParameters.push(p.id);
		});
		observables?.forEach((o) => {
			modelStates.push(o.id);
		});
	} else if (modelFramework === AMRSchemaNames.REGNET) {
		model.vertices.forEach((v) => {
			modelStates.push(v.id);
		});
		model.parameters.forEach((p) => {
			modelParameters.push(p.id);
		});
	} else {
		console.error(`Unknown framework ${modelFramework}`);
		throw new Error(`Unknown framework ${modelFramework}`);
	}
	return { modelStates, modelParameters };
};

const inputChangeHandler = async () => {
	const input = props.node.inputs[0];
	if (!input) return;

	let modelId: string | null = null;
	if (input.type === 'modelId') {
		modelId = input.value?.[0];
	} else if (input.type === 'modelConfigId') {
		modelId = await getModelIdFromModelConfigurationId(input.value?.[0]);
	}
	if (!modelId) return;

	amr.value = await getModel(modelId);
	if (!amr.value) return;

	const { modelStates, modelParameters } = getStatesAndParameters(amr.value);
	modelNodeOptions.value = [...modelStates, ...modelParameters];

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

const saveNewModel = async (modelName: string) => {
	if (!stratifiedAmr.value || !modelName) return;
	stratifiedAmr.value.header.name = modelName;

	const projectResource = useProjects();
	const modelData = await createModel(stratifiedAmr.value);
	const projectId = projectResource.activeProject.value?.id;

	if (!modelData) return;
	await projectResource.addAsset(AssetType.Model, modelData.id, projectId);
	useToastService().success('', `Saved to project ${projectResource.activeProject.value?.name}`);

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
			.register('model_preview', (data) => {
				// TODO: https://github.com/DARPA-ASKEM/terarium/issues/2305
				// currently no matter what kind of code is run we always get a `model_preview` response.
				// We may want to compare the response model with the existing model to see if the response model
				// has been stratified - if not then don't save the model or the code.
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

const onSelection = (id: string) => {
	emit('select-output', id);
};

watch(
	() => props.node.active,
	async () => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;

			const output = props.node.outputs.find((d) => d.id === selectedOutputId.value);
			if (!output) {
				console.error(`cannot find active output ${selectedOutputId.value}`);
				return;
			}
			const modelIdToLoad = output.value?.[0];
			stratifiedAmr.value = await getModel(modelIdToLoad);
			codeText.value = _.last(props.node.state.strataCodeHistory)?.code ?? '';
		}
	},
	{ immediate: true }
);

// Set model, modelNodeOptions
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

.save-as-dialog:deep(section) {
	width: 40rem;
}
</style>
