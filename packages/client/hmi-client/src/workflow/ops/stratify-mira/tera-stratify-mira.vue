<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === SimulateTabs.wizard"
			@click="activeTab = SimulateTabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === SimulateTabs.notebook"
			@click="activeTab = SimulateTabs.notebook"
		/>
	</div>
	<div v-if="activeTab === SimulateTabs.wizard" class="container">
		<div class="left-side">
			<h1>Stratify Model <i class="pi pi-info-circle" /></h1>
			<p>The model will be stratified with the following settings.</p>
			<stratificationGroupForm
				v-for="(cfg, index) in node.state.strataGroups"
				:key="index"
				:modelStates="modelStates"
				:config="cfg"
			/>
			<Button label="Add another strata group" size="small" @click="addGroupForm" />
		</div>
		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h>No Model Provided</h>
			</div>
		</div>
	</div>
	<div v-else class="container">
		<p>TODO</p>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { watch, ref } from 'vue';
import Button from 'primevue/button';
import stratificationGroupForm from '@/components/stratification/stratification-group-form.vue';
import { Model, ModelConfiguration } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import { StratifyOperationStateMira, StratifyGroup } from './stratify-mira-operation';

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();

enum SimulateTabs {
	wizard,
	notebook
}

const activeTab = ref(SimulateTabs.wizard);
const modelConfiguration = ref<ModelConfiguration>();
const model = ref<Model | null>(null);
const modelStates = ref<string[]>([]);
const teraModelDiagramRef = ref();

const addGroupForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: StratifyGroup = {
		borderColour: '#00c387',
		name: '',
		selectedVariables: [],
		groupLabels: ''
	};
	state.strataGroups.push(newGroup);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// Set model, modelConfiguration, modelStates
watch(
	() => props.node.inputs[0],
	async () => {
		const modelConfigurationId = props.node.inputs[0].value?.[0];
		if (modelConfigurationId) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
			if (modelConfiguration.value) {
				model.value = await getModel(modelConfiguration.value.modelId);

				const modelColumnNameOptions: string[] =
					modelConfiguration.value.configuration.model.states.map((state) => state.id);
				// add observables
				if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
					modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
						modelColumnNameOptions.push(o.id);
					});
				}
				modelStates.value = modelColumnNameOptions;
			}
		}
	},
	{ immediate: true }
);
</script>

<style>
.container {
	display: flex;
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
</style>
