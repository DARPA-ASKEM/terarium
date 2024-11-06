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
									<!-- Index matching listModelLabels and ensembleConfigs-->
									<tr v-for="(id, i) in listModelLabels" :key="i">
										<td>
											{{ id }}
										</td>
										<td>
											<tera-input-number v-model="ensembleConfigs[i].weight" />
										</td>
									</tr>
								</tbody>
							</table>
						</div>
						<Button
							label="Set weights to be equal"
							class="p-button-sm p-button-outlined mt-2"
							outlined
							severity="secondary"
							@click="calculateEvenWeights()"
						/>
					</AccordionTab>

					<!-- Mapping -->
					<AccordionTab header="Mapping">
						<p class="subheader">Map the variables from the models to the ensemble variables.</p>
						<template v-if="ensembleConfigs.length > 0">
							<table class="w-full mb-2">
								<tbody>
									<tr>
										<th>Ensemble variables</th>
										<th v-for="(element, i) in listModelLabels" :key="i">
											{{ element }}
										</th>
									</tr>

									<tr v-for="key in Object.keys(ensembleConfigs[0].solutionMappings)" :key="key">
										<td>{{ key }}</td>
										<td v-for="config in ensembleConfigs" :key="config.id">
											<Dropdown
												class="w-full"
												:options="allModelOptions[config.id]"
												v-model="config.solutionMappings[key]"
												placeholder="Select a variable"
											/>
										</td>
										<td>
											<Button class="p-button-sm" icon="pi pi-times" rounded text @click="deleteMapping(key)" />
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
									@keypress.enter="
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
										<tera-input-number class="w-full" v-model="timeSpan.start" />
									</td>
									<td>
										<tera-input-number class="w-full" v-model="timeSpan.end" />
									</td>
									<td>
										<tera-input-number class="w-full" v-model="numSamples" />
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
import type { TimeSpan, EnsembleModelConfigs, EnsembleSimulationCiemssRequest } from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';

import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import { SimulateEnsembleCiemssOperationState } from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['select-output', 'update-state', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const showSpinner = ref(false);

const showAddMappingInput = ref(false);

const listModelLabels = ref<string[]>([]);

// List of each observible + state for each model.
const allModelOptions = ref<{ [key: string]: string[] }>({});
const ensembleConfigs = ref<EnsembleModelConfigs[]>(props.node.state.mapping);

const timeSpan = ref<TimeSpan>(props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);

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

const calculateEvenWeights = () => {
	if (!ensembleConfigs.value) return;
	const percent = 1 / ensembleConfigs.value.length;
	for (let i = 0; i < ensembleConfigs.value.length; i++) {
		ensembleConfigs.value[i].weight = percent;
	}
};

const addMapping = () => {
	for (let i = 0; i < ensembleConfigs.value.length; i++) {
		ensembleConfigs.value[i].solutionMappings[newSolutionMappingKey.value] = '';
	}

	const state = _.cloneDeep(props.node.state);
	state.mapping = ensembleConfigs.value;
	emit('update-state', state);
};

function deleteMapping(key) {
	for (let i = 0; i < ensembleConfigs.value.length; i++) {
		delete ensembleConfigs.value[i].solutionMappings[key];
	}

	const state = _.cloneDeep(props.node.state);
	state.mapping = ensembleConfigs.value;
	emit('update-state', state);
}

const runEnsemble = async () => {
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
		engine: 'ciemss',
		extra: { num_samples: numSamples.value }
	};
	const response = await makeEnsembleCiemssSimulation(params, nodeMetadata(props.node));

	const state = _.cloneDeep(props.node.state);
	state.inProgressForecastId = response.simulationId;
	emit('update-state', state);
};

onMounted(async () => {
	const modelConfigurationIds: string[] = [];
	props.node.inputs.forEach((ele) => {
		if (ele.value) modelConfigurationIds.push(ele.value[0]);
	});
	if (!modelConfigurationIds) return;

	const allModelConfigurations = await Promise.all(modelConfigurationIds.map((id) => getModelConfigurationById(id)));

	allModelOptions.value = {};
	for (let i = 0; i < allModelConfigurations.length; i++) {
		const tempList: string[] = [];
		getInitials(allModelConfigurations[i]).forEach((element) => tempList.push(element.target));
		getObservables(allModelConfigurations[i]).forEach((element) => tempList.push(element.referenceId));
		allModelOptions.value[allModelConfigurations[i].id as string] = tempList;
	}
	listModelLabels.value = allModelConfigurations.map((ele) => ele.name ?? '');

	const state = _.cloneDeep(props.node.state);

	if (state.mapping && state.mapping.length === 0) {
		for (let i = 0; i < allModelConfigurations.length; i++) {
			ensembleConfigs.value.push({
				id: allModelConfigurations[i].id as string,
				solutionMappings: {},
				weight: 0
			});
		}
	}

	if (ensembleConfigs.value.some((ele) => ele.weight === 0)) {
		calculateEvenWeights();
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
	() => [timeSpan.value, numSamples.value],
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.timeSpan = timeSpan.value;
		state.numSamples = numSamples.value;
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
