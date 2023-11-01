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
					v-model="calculatedStepList"
				/>
			</div>

			<h4>Add sanity checks</h4>
			<p>Model configurations will be tested against these constraints</p>
			<tera-constraint-group-form
				v-for="(cfg, index) in node.state.constraintGroups"
				:key="index"
				:config="cfg"
				:index="index"
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
			<!-- <InputText type="text" v-model="sampleRequest"/> -->
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
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
// import { FunmanPostQueriesRequest } from '@/types/Types';
// import { makeQueries } from '@/services/models/funman-service';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import teraConstraintGroupForm from '@/components/funman/tera-constraint-group-form.vue';
import { FunmanOperationState, ConstraintGroup } from './funman-operation';

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
const calculatedStepList = computed(() => '[1,2,3]'); // TOM TODO

// const sampleRequest: FunmanPostQueriesRequest = {

// }

const runMakeQuery = async () => {
	// const response = await makeQueries(sampleRequest);
	console.log('TODO');
};

const addConstraintForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: ConstraintGroup = {
		borderColour: '#00c387',
		name: '',
		currentTimespan: { start: 0, end: 100 },
		target: 'All variables',
		lowerBound: 0
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
