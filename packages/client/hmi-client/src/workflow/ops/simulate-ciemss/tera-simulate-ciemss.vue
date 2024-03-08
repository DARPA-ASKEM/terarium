<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
		<section :tabName="SimulateTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Set simulation parameters</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="2">Start time</label>
							<InputNumber
								id="2"
								class="p-inputtext-sm"
								v-model="timespan.start"
								inputId="integeronly"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="3">End time</label>
							<InputNumber
								id="3"
								class="p-inputtext-sm"
								v-model="timespan.end"
								inputId="integeronly"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="4">Number of samples</label>
							<InputNumber
								id="4"
								class="p-inputtext-sm"
								v-model="numSamples"
								inputId="integeronly"
								:min="1"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="5">Method</label>
							<Dropdown
								id="5"
								class="p-inputtext-sm"
								v-model="method"
								:options="ciemssMethodOptions"
								@update:model-value="updateState"
							/>
						</div>
					</div>
					<div v-if="inferredParameters">
						Using inferred parameters from calibration: {{ inferredParameters[0] }}
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="SimulateTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
			>
				<SelectButton
					:model-value="view"
					@change="if ($event.value) view = $event.value;"
					:options="viewOptions"
					option-value="value"
				>
					<template #option="{ option }">
						<i :class="`${option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ option.value }}</span>
					</template>
				</SelectButton>
				<template v-if="runResults[selectedRunId]">
					<div v-if="view === OutputView.Charts">
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.chartConfigs"
							:key="idx"
							:run-results="runResults[selectedRunId]"
							:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
							has-mean-line
							@configuration-change="configurationChange(idx, $event)"
						/>
						<Button
							class="p-button-sm p-button-text"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						/>
					</div>
					<div v-else-if="view === OutputView.Data">
						<tera-dataset-datatable
							v-if="rawContent[selectedRunId]"
							:rows="10"
							:raw-content="rawContent[selectedRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="run"
				:disabled="showSpinner"
			/>
			<tera-save-dataset-from-simulation :simulation-run-id="selectedRunId" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import type { CsvAsset, SimulationRequest, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import {
	getRunResultCiemss,
	makeForecastJobCiemss as makeForecastJob
} from '@/services/models/simulation-service';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import { createCsvAssetFromRunResults } from '@/services/dataset';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'select-output', 'close']);

const inferredParameters = computed(() => props.node.inputs[1].value);

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
// extras
const numSamples = ref<number>(props.node.state.numSamples);
const method = ref(props.node.state.method);
const ciemssMethodOptions = ref(['dopri5', 'euler']);

enum SimulateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

const view = ref(OutputView.Charts);
const viewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);

const showSpinner = ref(false);
const runResults = ref<{ [runId: string]: RunResults }>({});

const rawContent = ref<{ [runId: string]: CsvAsset | null }>({});

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
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.currentTimespan = timespan.value;
	state.numSamples = numSamples.value;
	state.method = method.value;
	emit('update-state', state);
};

const run = async () => {
	const simulationId = await makeForecastRequest();

	const state = _.cloneDeep(props.node.state);
	state.inProgressSimulationId = simulationId;
	emit('update-state', state);
};

const makeForecastRequest = async () => {
	const modelConfigId = props.node.inputs[0].value?.[0];
	const state = props.node.state;

	const payload: SimulationRequest = {
		projectId: '',
		modelConfigId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {
			num_samples: state.numSamples,
			method: state.method
		},
		engine: 'ciemss'
	};

	if (inferredParameters.value) {
		payload.extra.inferred_parameters = inferredParameters.value[0];
	}

	const response = await makeForecastJob(payload);
	return response.id;
};

const lazyLoadSimulationData = async (runId: string) => {
	if (runResults.value[runId] && rawContent.value[runId]) return;

	const output = await getRunResultCiemss(runId);
	runResults.value[runId] = output.runResults;
	rawContent.value[runId] = createCsvAssetFromRunResults(runResults.value[runId]);
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);

	emit('update-state', state);
};

watch(
	() => props.node.state.inProgressSimulationId,
	(id) => {
		if (id === '') showSpinner.value = false;
		else showSpinner.value = true;
	}
);

watch(
	() => props.node.active,
	async (newValue, oldValue) => {
		if (!props.node.active || newValue === oldValue) return;
		selectedOutputId.value = props.node.active;

		// Update Wizard form fields with current selected output state
		timespan.value = props.node.state.currentTimespan;
		numSamples.value = props.node.state.numSamples;
		method.value = props.node.state.method;

		lazyLoadSimulationData(selectedRunId.value);
	},
	{ immediate: true }
);
</script>

<style scoped>
.simulate-chart {
	margin: 2em 1.5em;
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;

	& > * {
		flex: 1;
	}
}
</style>
