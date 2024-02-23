<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="FunmanTabs.Wizard">
			<tera-drilldown-section>
				<main>
					<h4 class="primary-text">
						Set validation parameters
						<i class="pi pi-info-circle" v-tooltip="validateParametersToolTip" />
					</h4>
					<p class="secondary-text">
						The validator will use these parameters to execute the sanity checks.
					</p>
					<div class="section-row timespan">
						<div class="button-column">
							<label>Start time</label>
							<InputNumber v-model="knobs.currentTimespan.start" />
						</div>
						<div class="button-column">
							<label>End time</label>
							<InputNumber v-model="knobs.currentTimespan.end" />
						</div>
						<div class="button-column">
							<label>Number of steps</label>
							<InputNumber v-model="knobs.numberOfSteps" />
						</div>
					</div>
					<InputText
						:disabled="true"
						class="p-inputtext-sm timespan-list"
						v-model="requestStepListString"
					/>
					<h4
						class="primary-text green-text"
						v-if="!showAdditionalOptions"
						@click="toggleAdditonalOptions"
					>
						<i class="pi pi-angle-right" /> Show additional options
					</h4>
					<h4
						class="primary-text green-text"
						v-if="showAdditionalOptions"
						@click="toggleAdditonalOptions"
					>
						<i class="pi pi-angle-down" /> Hide additional options
					</h4>
					<div v-if="showAdditionalOptions">
						<div class="button-column">
							<label>Compartmental constraints</label>
							<InputSwitch v-model="knobs.useCompartmentalConstraint" />
						</div>
						<div class="button-column">
							<label>Tolerance</label>
							<InputNumber
								mode="decimal"
								:min="0"
								:max="1"
								:min-fraction-digits="0"
								:max-fraction-digits="7"
								v-model="knobs.tolerance"
							/>
						</div>
						<Slider v-model="knobs.tolerance" :min="0" :max="1" :step="0.01" />
						<div class="section-row">
							<!-- This will definitely require a proper tool tip. -->
							<label>Select parameters of interest<i class="pi pi-info-circle" /></label>
							<MultiSelect
								ref="columnSelect"
								:modelValue="variablesOfInterest"
								:options="requestParameters.map((d: any) => d.name)"
								:show-toggle-all="false"
								@update:modelValue="onToggleVariableOfInterest"
								:maxSelectedLabels="1"
								placeholder="Select variables"
							/>
						</div>
					</div>
					<div class="spacer">
						<h4>Add sanity checks</h4>
						<p>Model configurations will be tested against these constraints</p>
					</div>
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
				</main>
			</tera-drilldown-section>
		</div>
		<div :tabName="FunmanTabs.Notebook">
			<tera-drilldown-section>
				<main>
					<!-- TODO: notebook functionality -->
					<p>{{ requestConstraints }}</p>
				</main>
			</tera-drilldown-section>
		</div>

		<template #preview>
			<tera-drilldown-preview
				title="Validation results"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:options="outputs"
				is-selectable
			>
				<tera-funman-output v-if="activeOutput" :fun-model-id="activeOutput.value?.[0]" />
				<div v-else>
					<img src="@assets/svg/plants.svg" alt="" draggable="false" />
					<h4>No Output</h4>
				</div>
			</tera-drilldown-preview>
		</template>

		<template #footer>
			<Button
				outlined
				:loading="showSpinner"
				class="run-button"
				label="Run"
				icon="pi pi-play"
				@click="runMakeQuery"
			/>
			<Button outlined label="Save as a new model" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _, { floor } from 'lodash';
import { computed, ref, watch, onUnmounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Slider from 'primevue/slider';
import MultiSelect from 'primevue/multiselect';
import InputSwitch from 'primevue/inputswitch';

import TeraConstraintGroupForm from '@/components/funman/tera-constraint-group-form.vue';
import TeraFunmanOutput from '@/components/funman/tera-funman-output.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

import type {
	FunmanPostQueriesRequest,
	Model,
	ModelConfiguration,
	ModelParameter
} from '@/types/Types';
import { getQueries, makeQueries } from '@/services/models/funman-service';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { getModelConfigurationById } from '@/services/model-configurations';
import { useToastService } from '@/services/toast';
import { Poller, PollerState } from '@/api/api';
import { pythonInstance } from '@/python/PyodideController';
import { FunmanOperationState, ConstraintGroup, FunmanOperation } from './funman-operation';

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

const emit = defineEmits(['append-output', 'select-output', 'update-state', 'close']);

enum FunmanTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}
const toast = useToastService();
const validateParametersToolTip =
	'Validate the configuration of the model using functional model analysis (FUNMAN). \n \n The parameter space regions defined by the model configuration are evaluated to satisfactory or unsatisfactory depending on whether they generate model outputs that are within a given set of time-dependent constraints';

const showSpinner = ref(false);
const showAdditionalOptions = ref(false);

interface BasicKnobs {
	tolerance: number;
	currentTimespan: {
		start: number;
		end: number;
	};
	numberOfSteps: number;
	useCompartmentalConstraint: boolean;
}

const knobs = ref<BasicKnobs>({
	tolerance: 0,
	currentTimespan: { start: 0, end: 0 },
	numberOfSteps: 0,
	useCompartmentalConstraint: false
});

const requestStepList = computed(() => getStepList());
const requestStepListString = computed(() => requestStepList.value.join()); // Just used to display. dont like this but need to be quick

const requestConstraints = computed(
	() =>
		// Same as node state's except typing for state vs linear constraint
		props.node.state.constraintGroups?.map((ele) => {
			if (ele.timepoints) {
				ele.timepoints.closed_upper_bound = true;
			}
			if (ele.variables.length === 1) {
				// State Variable Constraint
				return {
					name: ele.name,
					variable: ele.variables[0],
					interval: ele.interval,
					timepoints: ele.timepoints
				};
			}

			return {
				// Linear Constraint
				name: ele.name,
				variables: ele.variables,
				weights: ele.weights,
				additive_bounds: ele.interval,
				timepoints: ele.timepoints
			};
		})
);

const requestParameters = ref<any[]>([]);
const model = ref<Model | null>();
const modelConfiguration = ref<ModelConfiguration>();
const modelNodeOptions = ref<string[]>([]); // Used for form's multiselect.
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

const activeOutput = ref<WorkflowOutput<FunmanOperationState> | null>(null);

const poller = new Poller();

const toggleAdditonalOptions = () => {
	showAdditionalOptions.value = !showAdditionalOptions.value;
};

const variablesOfInterest = ref<string[]>([]);
const onToggleVariableOfInterest = (vals: string[]) => {
	variablesOfInterest.value = vals;
	requestParameters.value.forEach((d) => {
		if (variablesOfInterest.value.includes(d.name)) {
			d.label = 'all';
		} else {
			d.label = 'any';
		}
	});

	const state = _.cloneDeep(props.node.state);
	state.requestParameters = _.cloneDeep(requestParameters.value);
	emit('update-state', state);
};

const runMakeQuery = async () => {
	if (!model.value) {
		toast.error('', 'No Model provided for request');
		return;
	}

	const request: FunmanPostQueriesRequest = {
		model: model.value,
		request: {
			constraints: requestConstraints.value,
			parameters: requestParameters.value,
			structure_parameters: [
				{
					name: 'schedules',
					schedules: [
						{
							timepoints: requestStepList.value
						}
					]
				}
			],
			config: {
				// use_compartmental_constraints: true,
				use_compartmental_constraints: knobs.value.useCompartmentalConstraint,
				normalization_constant: 1,
				tolerance: knobs.value.tolerance
			}
		}
	};

	// Calculate the normalization mass of the model = Sum(initials)
	const semantics = model.value.semantics;
	if (knobs.value.useCompartmentalConstraint && semantics) {
		const modelInitials = semantics.ode.initials;
		const modelMassExpression = modelInitials?.map((d) => d.expression).join(' + ');

		const parametersMap = {};
		semantics.ode.parameters?.forEach((d) => {
			parametersMap[d.id] = d.value;
		});

		const mass = await pythonInstance.evaluateExpression(
			modelMassExpression as string,
			parametersMap
		);

		if (request.request.config) {
			request.request.config.normalization_constant = parseFloat(mass);
		}
	}
	const response = await makeQueries(request);
	getStatus(response.id);
};

const getStatus = async (runId: string) => {
	showSpinner.value = true;

	poller
		.setInterval(3000)
		.setThreshold(100)
		.setPollAction(async () => {
			const response = await getQueries(runId);
			if (response.done && response.done === true) {
				return { data: response } as any;
			}
			return { data: null } as any;
		});
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		showSpinner.value = false;
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		console.error(`Funman: ${runId} has failed`);
		throw Error('Failed Funman validation');
	}
	showSpinner.value = false;
	addOutputPorts(runId);
};

const addOutputPorts = async (runId: string) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output', {
		label: `${portLabel} Result ${props.node.outputs.length + 1}`,
		type: FunmanOperation.outputs[0].type,
		value: runId,
		state: _.cloneDeep(props.node.state)
	});
	if (props.node.outputs.length === 1) {
		const portId = props.node.outputs[0].id;
		emit('select-output', portId);
	}
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
	emit('update-state', state);
};

const deleteConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups.splice(data.index, 1);
	emit('update-state', state);
};

const updateConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.constraintGroups[data.index] = data.updatedConfig;
	emit('update-state', state);
};

// Used to set requestStepList.
// Grab startTime, endTime, numberOfSteps and create list.
function getStepList() {
	const start = knobs.value.currentTimespan.start;
	const end = knobs.value.currentTimespan.end;
	const steps = knobs.value.numberOfSteps;

	const aList = [start];
	const stepSize = floor((end - start) / steps);
	for (let i = 1; i < steps; i++) {
		aList[i] = i * stepSize;
	}
	aList.push(end);
	return aList;
}

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
	model.value = modelConfiguration.value.configuration as Model;
};

const setModelOptions = async () => {
	if (!model.value) return;

	const initialVars = model.value.semantics?.ode.initials?.map((d) => d.expression);
	const modelColumnNameOptions: string[] = model.value.model.states.map((state: any) => state.id);

	model.value.semantics?.ode.parameters?.forEach((param) => {
		if (initialVars?.includes(param.id)) return;
		modelColumnNameOptions.push(param.id);
	});

	// observables are not currently supported
	// if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
	// 	modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
	// 		modelColumnNameOptions.push(o.id);
	// 	});
	// }
	modelNodeOptions.value = modelColumnNameOptions;

	const state = _.cloneDeep(props.node.state);
	knobs.value.numberOfSteps = state.numSteps;
	knobs.value.currentTimespan = _.cloneDeep(state.currentTimespan);
	knobs.value.tolerance = state.tolerance;
	knobs.value.useCompartmentalConstraint = state.useCompartmentalConstraint;

	if (model.value.semantics?.ode.parameters) {
		setRequestParameters(model.value.semantics?.ode.parameters);

		variablesOfInterest.value = requestParameters.value
			.filter((d: any) => d.label === 'all')
			.map((d: any) => d.name);
	} else {
		toast.error('', 'Provided model has no parameters');
	}

	state.requestParameters = _.cloneDeep(requestParameters.value);
	emit('update-state', state);
};

const setRequestParameters = (modelParameters: ModelParameter[]) => {
	const previous = props.node.state.requestParameters;
	if (previous && previous.length > 0) {
		requestParameters.value = _.cloneDeep(props.node.state.requestParameters);
		return;
	}

	requestParameters.value = modelParameters.map((ele) => {
		let interval = { lb: ele.value, ub: ele.value };
		if (ele.distribution) {
			interval = {
				lb: ele.distribution.parameters.minimum,
				ub: ele.distribution.parameters.maximum
			};
		}
		return {
			name: ele.id,
			interval,
			label: 'any'
		};
	});
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

/* Check for simple parameter changes */
watch(
	() => knobs.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.tolerance = knobs.value.tolerance;
		state.currentTimespan.start = knobs.value.currentTimespan.start;
		state.currentTimespan.end = knobs.value.currentTimespan.end;
		state.numSteps = knobs.value.numberOfSteps;
		state.useCompartmentalConstraint = knobs.value.useCompartmentalConstraint;

		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.inputs[0],
	async () => {
		// Set model, modelConfiguration, modelNodeOptions
		await initialize();
		setModelOptions();
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			activeOutput.value = props.node.outputs.find((d) => d.id === props.node.active) as any;
			selectedOutputId.value = props.node.active;
			setModelOptions();
		}
	},
	{ immediate: true }
);

onUnmounted(() => {
	poller.stop();
});
</script>

<style scoped>
.primary-text {
	color: var(--Text-Primary, #020203);
	/* Body Medium/Semibold */
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem;
	/* 150% */
	letter-spacing: 0.03125rem;
}

.secondary-text {
	color: var(--Text-Secondary, #667085);
	/* Body Small/Regular */
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem;
	/* 150% */
	letter-spacing: 0.01563rem;
}

.button-column {
	display: flex;
	flex-direction: column;
	padding: 1rem 0rem 0.5rem 0rem;
	align-items: flex-start;
	align-self: stretch;
}

.section-row {
	display: flex;
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
}

.timespan > .button-column {
	width: 100%;
}

div.section-row.timespan > div > span {
	width: 100%;
}

.timespan-list {
	width: 100%;
}

.spacer {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.green-text {
	color: var(--Primary, #1b8073);
}

.green-text:hover {
	color: var(--text-color-subdued);
}

.run-button {
	margin-right: auto;
}
</style>
