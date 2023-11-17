<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === StratifyTabs.wizard"
			@click="activeTab = StratifyTabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === StratifyTabs.notebook"
			@click="activeTab = StratifyTabs.notebook"
		/>
	</div>
	<div class="container">
		<div class="left-side" v-if="activeTab === StratifyTabs.wizard">
			<h4>Stratify Model <i class="pi pi-info-circle" /></h4>
			<p>The model will be stratified with the following settings.</p>
			<tera-stratification-group-form
				v-for="(cfg, index) in node.state.strataGroups"
				:key="index"
				:modelNodeOptions="modelNodeOptions"
				:config="cfg"
				:index="index"
				@delete-self="deleteStratifyGroupForm"
				@update-self="updateStratifyGroupForm"
			/>
			<!--
			<Button label="Add another strata group" size="small" @click="addGroupForm" />
			-->
			<Button label="Stratify" size="small" @click="stratifyModel" />
			<Button label="Reset" size="small" @click="resetModel" />
		</div>
		<div class="left-side" v-if="activeTab === StratifyTabs.notebook">
			<Suspense>
				<tera-mira-notebook />
			</Suspense>
		</div>

		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Model Provided</h4>
			</div>
			<div v-if="model">
				<InputText
					v-model="newModelName"
					placeholder="model name"
					type="text"
					class="input-small"
				/>
				<Button label="Save as new Model" size="small" @click="saveNewModel" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { watch, ref, onUnmounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import teraStratificationGroupForm from '@/components/stratification/tera-stratification-group-form.vue';
import teraMiraNotebook from '@/components/stratification/tera-mira-notebook.vue';
import { Model, ModelConfiguration, AssetType } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel, createModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';

/* Jupyter imports */
import { KernelSessionManager } from '@/services/jupyter';
// import { StratifyOperationStateMira, StratifyGroup, StratifyMiraOperation } from './stratify-mira-operation';
import { StratifyOperationStateMira } from './stratify-mira-operation';

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();

enum StratifyTabs {
	wizard,
	notebook
}

const kernelManager = new KernelSessionManager();

const activeTab = ref(StratifyTabs.wizard);
const modelConfiguration = ref<ModelConfiguration>();
const model = ref<Model | null>(null);
const modelNodeOptions = ref<string[]>([]);
const teraModelDiagramRef = ref();
const newModelName = ref('');

// TODO: Limit to single strata for now - DC, Nov 2023
// const addGroupForm = () => {
// 	const state = _.cloneDeep(props.node.state);
// 	const newGroup: StratifyGroup = {
// 		borderColour: '#00c387',
// 		name: '',
// 		selectedVariables: [],
// 		groupLabels: '',
// 		cartesianProduct: true,
// 		isPending: true
// 	};
// 	state.strataGroups.push(newGroup);
//
// 	workflowEventBus.emitNodeStateChange({
// 		workflowId: props.node.workflowId,
// 		nodeId: props.node.id,
// 		state
// 	});
// };

const deleteStratifyGroupForm = (data: any) => {
	const state = _.cloneDeep(props.node.state);
	state.strataGroups.splice(data.index, 1);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const updateStratifyGroupForm = (data: any) => {
	const state = _.cloneDeep(props.node.state);
	state.strataGroups[data.index] = data.updatedConfig;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const stratifyModel = () => {
	stratifyRequest();
};

const resetModel = () => {
	if (!model.value) return;

	kernelManager.sendMessage('reset_request', {})?.register('reset_response', handleResetResponse);
};

const handleResetResponse = (data: any) => {
	console.log(data.content);
};

const stratifyRequest = () => {
	if (!model.value) return;

	const strataOption = props.node.state.strataGroups[0];
	const messageContent = {
		stratify_args: {
			key: strataOption.name,
			strata: strataOption.groupLabels.split(',').map((d) => d.trim()),
			concepts_to_stratify: strataOption.selectedVariables,
			cartesian_control: strataOption.cartesianProduct
		}
	};

	kernelManager
		.sendMessage('stratify_request', messageContent)
		?.register('stratify_response', handleStratifyResponse)
		?.register('model_preview', handleModelPreview);
};

const handleStratifyResponse = (data: any) => {
	const codes = data.content.executed_code.split('\n');
	codes.forEach((c: any) => {
		console.log(c);
	});
};

const handleModelPreview = (data: any) => {
	console.log('model preview', data);
	model.value = data.content['application/json'];
};

const buildJupyterContext = () => {
	if (!model.value) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}

	return {
		context: 'mira_model',
		language: 'python3',
		context_info: {
			id: model.value.id
		}
	};
};

const inputChangeHandler = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;

	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);

	if (!modelConfiguration.value) return;
	model.value = await getModel(modelConfiguration.value.modelId);

	const modelColumnNameOptions: string[] = modelConfiguration.value.configuration.model.states.map(
		(state: any) => state.id
	);
	// add observables
	if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
		modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
			modelColumnNameOptions.push(o.id);
		});
	}
	modelNodeOptions.value = modelColumnNameOptions;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			await kernelManager.init('beaker', 'Beaker', buildJupyterContext());
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const saveNewModel = async () => {
	if (!model.value || !newModelName.value) return;
	model.value.header.name = newModelName.value;

	const projectResource = useProjects();
	const modelData = await createModel(model.value);
	const projectId = projectResource.activeProject.value?.id;

	if (!modelData) return;
	await projectResource.addAsset(AssetType.Models, modelData.id, projectId);
};

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
.container {
	display: flex;
	flex-direction: row;
	margin-top: 1rem;
}

.left-side {
	width: 45%;
	padding-right: 2.5%;
}
.left-side h1 {
	color: var(--text-color-primary);
	font-family: Inter;
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem; /* 150% */
	letter-spacing: 0.03125rem;
}
.left-side p {
	color: var(--Text-Secondary);
	/* Body Small/Regular */
	font-family: Figtree;
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}

.right-side {
	width: 45%;
	padding-left: 2.5%;
}

.input-small {
	padding: 0.5rem;
}
</style>
