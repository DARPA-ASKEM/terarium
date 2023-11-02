<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === FunmanTabs.wizard"
			@click="activeTab = FunmanTabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === FunmanTabs.notebook"
			@click="activeTab = FunmanTabs.notebook"
		/>
		<Button class="p-button-sm run-button" label="Run" icon="pi pi-play" @click="runMakeQuery" />
	</div>
	<div v-if="activeTab === FunmanTabs.wizard" class="container">
		<div class="left-side">
			<h4 class="primary-text">Set validation parameters <i class="pi pi-info-circle" /></h4>
			<p class="secondary-text">
				The validator will use these parameters to execute the sanity checks.
			</p>
			<div class="first-row">
				<label>Tolerance</label>
				<InputNumber
					mode="decimal"
					:min-fraction-digits="0"
					:max-fraction-digits="7"
					v-model="tolerance"
				/>
			</div>
			<div class="second-row">
				<label>Start time</label>
				<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="startTime" />
				<label>End time</label>
				<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="endTime" />
				<label>Number of steps</label>
				<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="numberOfSteps" />
				<InputText
					:disabled="true"
					class="p-inputtext-sm"
					inputId="integeronly"
					v-model="calculatedStepListString"
				/>
			</div>

			<h4>Add sanity checks</h4>
			<p>Model configurations will be tested against these constraints</p>
			<tera-constraint-group-form
				v-for="(cfg, index) in node.state.constraintGroups"
				:key="index"
				:config="cfg"
				:index="index"
				:model-node-options="modelNodeOptions"
				@delete-self="deleteConstraintGroupForm"
				@update-self="updateConstraintGroupForm"
			/>

			<Button label="Add another constraint" size="small" @click="addConstraintForm" />
		</div>
		<div class="right-side">
			<div v-if="false">
				<h4>Output graph goes here</h4>
			</div>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Output</h4>
			</div>
		</div>
	</div>
	<div v-else class="container">
		<div class="left-side">
			<!-- TODO: Are we demoing notebook? -->
			<!-- <InputText v-model="sampleRequest"/> -->
		</div>
		<div class="right-side">
			<div v-if="false">
				<h4>Output graph goes here</h4>
			</div>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Output</h4>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import { FunmanPostQueriesRequest, Model, ModelConfiguration } from '@/types/Types';
import { makeQueries } from '@/services/models/funman-service';
// import { request } from '@/temp/funmanRequest';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import teraConstraintGroupForm from '@/components/funman/tera-constraint-group-form.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel } from '@/services/model';
import { FunmanOperationState, ConstraintGroup } from './funman-operation';

// TODO List:
// 2) computedParameters
// 4) fix constraintGroups typing -> mostly just adding weights, and if 1 variable then change it to linear
// 5) fix css for overlow

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

enum FunmanTabs {
	wizard,
	notebook
}
const activeTab = ref(FunmanTabs.wizard);
const tolerance = ref(props.node.state.tolerance);
const startTime = ref(props.node.state.currentTimespan.start);
const endTime = ref(props.node.state.currentTimespan.end);
const numberOfSteps = ref(props.node.state.numSteps);
const calculatedStepList = computed(() => getStepList());
const calculatedStepListString = computed(() => calculatedStepList.value.join()); // Just used to display. dont like this but need to be quick
// TOM TODO
const computedParameters = computed(() => [
	{
		name: 'beta',
		interval: {
			lb: 1e-8,
			ub: 0.01
		},
		label: 'all'
	},
	{
		name: 'gamma',
		interval: {
			lb: 0.1,
			ub: 0.18
		},
		label: 'all'
	},
	{
		name: 'S0',
		interval: {
			lb: 999,
			ub: 999
		},
		label: 'any'
	},
	{
		name: 'I0',
		interval: {
			lb: 1,
			ub: 1
		},
		label: 'any'
	},
	{
		name: 'R0',
		interval: {
			lb: 0,
			ub: 0
		},
		label: 'any'
	}
]);
const response = ref();
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const modelNodeOptions = ref<string[]>([]);

// const sampleRequest: FunmanPostQueriesRequest = request;

const runMakeQuery = async () => {
	if (!model.value) {
		console.log('No Model provided');
		return;
	}

	const request: FunmanPostQueriesRequest = {
		model: model.value,
		request: {
			constraints: props.node.state.constraintGroups,
			parameters: computedParameters.value,
			structure_parameters: {
				name: 'schedules',
				schedules: [
					{
						timepoints: calculatedStepList.value
					}
				]
			}
		}
	};

	response.value = await makeQueries(request);
	console.log(response.value);
};

const addConstraintForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: ConstraintGroup = {
		borderColour: '#00c387',
		name: '',
		timepoints: { lb: 0, ub: 100 },
		variables: []
	};
	state.constraintGroups.push(newGroup);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const deleteConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups.splice(data.index, 1);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const updateConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups[data.index] = data.updatedConfig;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// Used to set calculatedStepList.
// Grab startTime, endTime, numberOfSteps and create list.
function getStepList() {
	const aList = [startTime.value];
	const stepSize = (endTime.value - startTime.value) / numberOfSteps.value;
	for (let i = 1; i < numberOfSteps.value - 1; i++) {
		aList[i] = i * stepSize;
	}
	aList.push(endTime.value);
	return aList;
}

// Set model, modelConfiguration, modelNodeOptions
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
				modelNodeOptions.value = modelColumnNameOptions;
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

.primary-text {
	color: var(--Text-Primary, #020203);
	/* Body Medium/Semibold */
	font-family: Inter;
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem; /* 150% */
	letter-spacing: 0.03125rem;
}

.secondary-text {
	color: var(--Text-Secondary, #667085);
	/* Body Small/Regular */
	font-family: Figtree;
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}

.first-row {
	display: flex;
	padding: 1rem 0rem 0.5rem 0rem;
	align-items: flex-start;
	gap: 2.5rem;
	align-self: stretch;
}

.second-row {
	display: flex;
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
}
</style>
