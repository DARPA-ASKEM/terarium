<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="Tabs.Wizard" class="ml-3 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-right>
					<Button label="Run" icon="pi pi-play" @click="runEnsemble" :disabled="false" />
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
				</template>
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<!-- Model weights -->
					<AccordionTab header="Model weights">
						<p class="subheader">
							How do you want to distribute weights of the attached models? You can distribute them equally or set
							custom weights using the input boxes.
						</p>
						<div class="model-weights">
							<table class="p-datatable-table">
								<tbody class="p-datatable-tbody">
									<!-- Index matching listModelLabels and mapping-->
									<tr v-for="(ele, indx) in knobs.weights" :key="indx">
										<td>
											{{ modelConfigIdToNameMap[ele.modelConfigurationId] }}
										</td>
										<td>
											<tera-input-number v-model="ele.value" />
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</AccordionTab>

					<!-- Mapping -->
					<AccordionTab header="Mapping">
						<p class="subheader">Map the variables from the models to the ensemble variables.</p>
						<template v-if="knobs.mapping.length > 0">
							<table class="w-full mb-2">
								<tbody>
									<tr>
										<th>Ensemble variables</th>
										<th v-for="(element, i) in listModelLabels" :key="i">
											{{ element }}
										</th>
									</tr>
									<tr v-for="(ele, indx) in knobs.mapping" :key="indx">
										<td>{{ ele.newName }}</td>
										<td v-for="(row, indx) in ele.modelConfigurationMappings" :key="indx">
											<Dropdown
												class="w-full"
												:options="allModelOptions[row.modelConfigId]"
												v-model="row.compartmentName"
												placeholder="Select a variable"
											/>
										</td>
										<td>
											<Button class="p-button-sm" icon="pi pi-times" rounded text @click="deleteMappingRow(ele.id)" />
										</td>
									</tr>
								</tbody>
							</table>
						</template>
						<section class="add-mapping">
							<Button
								v-if="!showAddMappingInput"
								outlined
								:style="{ marginRight: 'auto' }"
								label="Add mapping"
								size="small"
								severity="secondary"
								icon="pi pi-plus"
								@click="
									newSolutionMappingKey = '';
									showAddMappingInput = true;
								"
							/>
							<div v-if="showAddMappingInput" class="flex items-center">
								<tera-input-text
									v-model="newSolutionMappingKey"
									auto-focus
									class="w-full"
									placeholder="Add a name"
									@keydown.enter="
										addMapping();
										showAddMappingInput = false;
									"
								/>
								<Button
									class="p-button-sm p-button-outlined w-2 ml-2"
									severity="secondary"
									icon="pi pi-times"
									label="Cancel"
									@click="
										newSolutionMappingKey = '';
										showAddMappingInput = false;
									"
								/>
								<Button
									:disabled="!newSolutionMappingKey"
									class="p-button-sm p-button-outlined w-2 ml-2"
									icon="pi pi-check"
									label="Add"
									@click="
										addMapping();
										showAddMappingInput = false;
									"
								/>
							</div>
						</section>
					</AccordionTab>

					<!-- Time span -->
					<AccordionTab header="Time span">
						<p class="subheader">Set the time span and number of samples for the ensemble simulation.</p>
						<table class="w-full">
							<thead class="p-datatable-thead">
								<tr>
									<th>Units</th>
									<th>Start Step</th>
									<th>End Step</th>
									<th>Number of Samples</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr>
									<td class="w-2">Steps</td>
									<td>
										<tera-input-number class="w-full" v-model="knobs.timeSpan.start" />
									</td>
									<td>
										<tera-input-number class="w-full" v-model="knobs.timeSpan.end" />
									</td>
									<td>
										<tera-input-number class="w-full" v-model="knobs.numSamples" />
									</td>
								</tr>
							</tbody>
						</table>
					</AccordionTab>
				</Accordion>
			</tera-drilldown-section>
		</section>
		<section :tabName="Tabs.Notebook">
			<div class="mt-3 ml-4 mr-2">Under construction. Use the wizard for now.</div>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				is-selectable
				:is-loading="showSpinner"
				@update:selection="onSelection"
			>
				<tera-notebook-error v-if="!_.isEmpty(node.state?.errorMessage?.traceback)" v-bind="node.state.errorMessage" />
				<section ref="outputPanel">
					<tera-simulate-chart
						v-for="(cfg, index) of node.state.chartConfigs"
						:key="index"
						:run-results="runResults"
						:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
						has-mean-line
						:size="chartSize"
						@configuration-change="chartProxy.configurationChange(index, $event)"
						@remove="chartProxy.removeChart(index)"
						show-remove-button
					/>
					<Button
						class="add-chart"
						text
						:outlined="true"
						@click="chartProxy.addChart()"
						label="Add chart"
						icon="pi pi-plus"
					/>
				</section>
			</tera-drilldown-preview>
		</template>
		<template #footer> </template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Dropdown from 'primevue/dropdown';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import { getRunResultCiemss, makeEnsembleCiemssSimulation } from '@/services/models/simulation-service';
import { getModelConfigurationById, getObservables, getInitials } from '@/services/model-configurations';
import { chartActionsProxy, drilldownChartSize, nodeMetadata } from '@/components/workflow/util';
import type { WorkflowNode } from '@/types/workflow';
import type { TimeSpan, EnsembleSimulationCiemssRequest } from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import { v4 as uuidv4 } from 'uuid';
import {
	SimulateEnsembleCiemssOperationState,
	SimulateEnsembleMappingRow,
	SimulateEnsembleWeight
} from './simulate-ensemble-ciemss-operation';
import { formatSimulateModelConfigurations } from './simulate-ensemble-util';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['select-output', 'update-state', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface BasicKnobs {
	mapping: SimulateEnsembleMappingRow[];
	weights: SimulateEnsembleWeight[];
	numSamples: number;
	timeSpan: TimeSpan;
}

const knobs = ref<BasicKnobs>({
	mapping: props.node.state.mapping,
	weights: props.node.state.weights,
	numSamples: props.node.state.numSamples,
	timeSpan: props.node.state.timeSpan
});

const showSpinner = ref(false);
const showAddMappingInput = ref(false);
const listModelLabels = ref<string[]>([]);

// List of each observible + state for each model.
const allModelOptions = ref<{ [key: string]: string[] }>({});
const modelConfigurationIds = ref<string[]>(
	props.node.inputs.filter((ele) => ele.value?.[0]).map((ele) => ele.value?.[0])
);
const modelConfigIdToNameMap = ref();

const newSolutionMappingKey = ref<string>('');
const runResults = ref<RunResults>({});
const cancelRunId = computed(() => props.node.state.inProgressForecastId);
// Preview selection
const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();
const selectedRunId = ref<string>('');

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const chartProxy = chartActionsProxy(props.node, (state: SimulateEnsembleCiemssOperationState) => {
	emit('update-state', state);
});

const onSelection = (id: string) => {
	emit('select-output', id);
};

const addMapping = () => {
	knobs.value.mapping.push({
		id: uuidv4(),
		newName: newSolutionMappingKey.value,
		modelConfigurationMappings: modelConfigurationIds.value.map((id) => ({
			modelConfigId: id as string,
			compartmentName: ''
		}))
	});
	const state = _.cloneDeep(props.node.state);
	state.mapping = knobs.value.mapping;
	emit('update-state', state);
};

function deleteMappingRow(id: string) {
	knobs.value.mapping = knobs.value.mapping.filter((ele) => ele.id !== id);
	const state = _.cloneDeep(props.node.state);
	state.mapping = knobs.value.mapping;
	emit('update-state', state);
}

const runEnsemble = async () => {
	const modelConfigs = formatSimulateModelConfigurations(knobs.value.mapping, knobs.value.weights);
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs,
		timespan: knobs.value.timeSpan,
		engine: 'ciemss',
		extra: { num_samples: knobs.value.numSamples }
	};
	const response = await makeEnsembleCiemssSimulation(params, nodeMetadata(props.node));

	const state = _.cloneDeep(props.node.state);
	state.inProgressForecastId = response.simulationId;
	emit('update-state', state);
};

onMounted(async () => {
	if (!modelConfigurationIds.value) return;
	const allModelConfigurations = await Promise.all(
		modelConfigurationIds.value.map((id) => getModelConfigurationById(id))
	);

	modelConfigIdToNameMap.value = {};
	allModelConfigurations.forEach((config) => {
		modelConfigIdToNameMap.value[config.id as string] = config.name;
	});

	allModelOptions.value = {};
	for (let i = 0; i < allModelConfigurations.length; i++) {
		const tempList: string[] = [];
		getInitials(allModelConfigurations[i]).forEach((element) => tempList.push(element.target));
		getObservables(allModelConfigurations[i]).forEach((element) => tempList.push(element.referenceId));
		allModelOptions.value[allModelConfigurations[i].id as string] = tempList;
	}
	listModelLabels.value = allModelConfigurations.map((ele) => ele.name ?? '');

	const state = _.cloneDeep(props.node.state);

	// Initalize weights:
	if (
		!knobs.value.weights ||
		knobs.value.weights.length === 0 ||
		knobs.value.weights.length !== modelConfigurationIds.value.length
	) {
		knobs.value.weights = [];
		modelConfigurationIds.value.forEach((id) => {
			knobs.value.weights.push({
				modelConfigurationId: id,
				value: 5
			});
		});
	}

	emit('update-state', state);
});

watch(
	() => props.node.state.inProgressForecastId,
	(id) => {
		if (id === '') showSpinner.value = false;
		else showSpinner.value = true;
	}
);

watch(
	() => props.node.active,
	async () => {
		const output = props.node.outputs.find((d) => d.id === props.node.active);
		if (!output || !output.value) return;

		selectedOutputId.value = output.id;
		selectedRunId.value = output.value[0];
		const forecastId = props.node.state.forecastId;
		if (!forecastId) return;

		const response = await getRunResultCiemss(forecastId, 'result.csv');
		runResults.value = response.runResults;
	},
	{ immediate: true }
);

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.mapping = knobs.value.mapping;
		state.weights = knobs.value.weights;
		state.timeSpan = knobs.value.timeSpan;
		state.numSamples = knobs.value.numSamples;
		emit('update-state', state);
	},
	{ immediate: true }
);
</script>

<style scoped>
.subheader {
	color: var(--text-color-subdued);
	margin-bottom: var(--gap-4);
}

.ensemble-calibration-graph {
	height: 100px;
}

.model-weights {
	display: flex;
	align-items: start;
}

.ensemble-header {
	display: flex;
	margin: 1em;
}

th {
	text-align: left;
}
</style>
