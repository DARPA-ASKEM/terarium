<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<section :tabName="SimulateTabs.Wizard" class="ml-4 mr-2 pt-3">
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
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="SimulateTabs.Notebook" class="ml-4 mr-2 pt-3">
			<p>Under construction. Use the wizard for now.</p>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
				class="mr-4 ml-2 mt-3 mb-3"
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
					<div v-if="view === OutputView.Charts" ref="outputPanel">
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.chartConfigs"
							:key="idx"
							:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
							:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
							@configuration-change="chartProxy.configurationChange(idx, $event)"
							@remove="chartProxy.removeChart(idx)"
							show-remove-button
							:size="chartSize"
							color-by-run
						/>
						<Button
							class="p-button-sm p-button-text"
							@click="chartProxy.addChart"
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
import InputNumber from 'primevue/inputnumber';
import type { CsvAsset, SimulationRequest, TimeSpan } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
import type { WorkflowNode } from '@/types/workflow';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getRunResult, makeForecastJob } from '@/services/models/simulation-service';
import { createCsvAssetFromRunResults } from '@/services/dataset';
import { csvParse } from 'd3';
import { chartActionsProxy, drilldownChartSize } from '@/components/workflow/util';
import { useProjects } from '@/composables/project';

import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';

import { SimulateJuliaOperationState } from './simulate-julia-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'select-output', 'close']);

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);

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

const runResults = ref<RunResults>({});
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

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const chartProxy = chartActionsProxy(props.node, (state: SimulateJuliaOperationState) => {
	emit('update-state', state);
});

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.currentTimespan = timespan.value;
	emit('update-state', state);
};

// Main entry point
const run = async () => {
	const simulationId = await makeForecastRequest();

	const state = _.cloneDeep(props.node.state);
	state.inProgressSimulationId = simulationId;
	emit('update-state', state);
};

const makeForecastRequest = async (): Promise<string> => {
	const configId = props.node.inputs[0].value?.[0];
	if (!configId) throw new Error('No model configuration found for simulate');

	const state = props.node.state;
	const payload: SimulationRequest = {
		projectId: useProjects().activeProject.value?.id as string,
		modelConfigId: configId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {},
		engine: 'sciml'
	};
	const response = await makeForecastJob(payload);
	return response.id;
};

const lazyLoadSimulationData = async (runId: string) => {
	if (runResults.value[runId] && rawContent.value[runId]) return;

	// there's only a single input config
	const modelConfigId = props.node.inputs[0].value?.[0];
	const modelConfiguration = await getModelConfigurationById(modelConfigId);

	const resultCsv = await getRunResult(runId, 'result.csv');
	const csvData = csvParse(resultCsv);

	if (modelConfiguration) {
		const parameters = modelConfiguration.configuration?.semantics?.ode?.parameters;
		if (parameters) {
			csvData.forEach((row) =>
				parameters.forEach((parameter: any) => {
					row[parameter.id] = parameter.value;
				})
			);
		}
	}
	runResults.value[runId] = csvData as any;
	rawContent.value[runId] = createCsvAssetFromRunResults(runResults.value, runId);
};

const onSelection = (id: string) => {
	emit('select-output', id);
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

		// Update Wizard form fields with current selected output state timespan
		timespan.value = props.node.state.currentTimespan;

		// Resume or fetch result
		const simulationId = selectedRunId.value;
		await lazyLoadSimulationData(simulationId);
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
