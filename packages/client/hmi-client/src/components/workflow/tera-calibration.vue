<template>
	<!--Probably rename tera-asset to something even more abstract-->
	<tera-asset :name="'Calibrate'" is-editable stretch-content>
		<template #nav>
			<tera-asset-nav :show-header-links="false">
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Input"
							icon="pi pi-sign-in"
							@click="calibrationView = CalibrationView.INPUT"
							:active="calibrationView === CalibrationView.INPUT"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="Output"
							icon="pi pi-sign-out"
							@click="calibrationView = CalibrationView.OUTPUT"
							:active="calibrationView === CalibrationView.OUTPUT"
						/>
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<template #edit-buttons>
			<Button
				icon="pi pi-play"
				label="Run"
				class="p-button-sm"
				:disabled="disableRunButton"
				@click="calibrate"
			/>
		</template>
		<Accordion
			v-if="calibrationView === CalibrationView.INPUT && modelConfiguration"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
			<AccordionTab :header="modelConfiguration.configuration.model.name">
				<tera-model-diagram :model="modelConfiguration.configuration.model" :is-editable="false" />
			</AccordionTab>
			<AccordionTab header="Model configuation">
				<!-- <tera-model-configuration
					ref="modelConfigurationRef"
					:model-configuration="modelConfiguration"
					calibration-config
				/> -->
			</AccordionTab>
			<AccordionTab v-if="datasetId" :header="datasetName">
				<tera-dataset-datatable preview-mode :raw-content="csvAsset ?? null" />
			</AccordionTab>
			<AccordionTab header="Train / Test ratio">
				<section class="train-test-ratio">
					<InputNumber v-model="trainTestValue" />
					<Slider v-model="trainTestValue" />
				</section>
			</AccordionTab>
			<AccordionTab header="Mapping">
				<section class="mapping">
					<div>
						Select target variables from the model and the corresponding data column you want to
						match them to.
					</div>
					<DataTable :value="mapping" v-model:expandedRows="expandedRows">
						<ColumnGroup type="header">
							<Row>
								<Column />
								<Column header="Model variable" />
								<Column header="Dataset variable" />
							</Row>
							<Row>
								<Column />
								<Column header="Timestep" style="font-weight: normal" />
								<Column>
									<template #header>
										<Dropdown
											class="w-full"
											placeholder="Timestep column"
											v-model="timestepColumn"
											:options="datasetVariables"
										/>
									</template>
								</Column>
							</Row>
						</ColumnGroup>
						<Column expander style="width: 5rem" />
						<Column field="modelVariable">
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field].label"
									:options="modelVariables"
								/>
							</template>
						</Column>
						<Column field="datasetVariable">
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field].label"
									:options="datasetVariables"
								/>
							</template>
						</Column>
						<template #expansion="slotProps">
							<DataTable
								class="p-datatable-sm"
								:value="[slotProps.data.modelVariable, slotProps.data.datasetVariable]"
							>
								<Column field="label" header="Label" />
								<Column field="name" header="Name" />
								<Column field="units" header="Units" />
								<Column field="concept" header="Concept" />
								<Column field="definition" header="Definition" />
							</DataTable>
						</template>
					</DataTable>
					<div>
						<Button
							class="p-button-sm p-button-outlined"
							icon="pi pi-plus"
							label="Add mapping"
							@click="addMapping"
						/>
					</div>
				</section>
			</AccordionTab>
		</Accordion>
		<Accordion
			v-if="calibrationView === CalibrationView.OUTPUT && modelConfiguration"
			:multiple="true"
			:active-index="[0, 1]"
		>
			<AccordionTab header="Variables">
				<tera-simulate-chart
					v-for="index in openedWorkflowNodeStore.calibrateNumCharts"
					:key="index"
					:run-results="openedWorkflowNodeStore.calibrateRunResults"
					:run-id-list="openedWorkflowNodeStore.calibrateRunIdList"
					:chart-idx="index"
				/>
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="openedWorkflowNodeStore.calibrateNumCharts++"
					label="Add Chart"
					icon="pi pi-plus"
				></Button>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<!-- <tera-model-configuration :model="modelConfig.model" :is-editable="false"
					:model-config-node-input="calibratedModelConfig" /> -->
			</AccordionTab>
		</Accordion>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import { cloneDeep } from 'lodash';
import { csvParse } from 'd3';
import Button from 'primevue/button';
import { Poller } from '@/api/api';
import {
	makeCalibrateJob,
	makeForecast,
	getRunStatus,
	getRunResult
} from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import DataTable from 'primevue/datatable';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { WorkflowNode } from '@/types/workflow';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
// import TeraModelConfiguration from '@/components/models/tera-model-configuration.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { logger } from '@/utils/logger';
import { CalibrationParams, CsvAsset, Dataset, ModelConfiguration } from '@/types/Types';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { shimPetriModel } from '@/services/models/petri-shim';
import { AMRToPetri } from '@/model-representation/petrinet/petrinet-service';
import Slider from 'primevue/slider';
import InputNumber from 'primevue/inputnumber';
import { getModelConfigurationById } from '@/services/model-configurations';
import TeraSimulateChart from './tera-simulate-chart.vue';

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

enum CalibrationView {
	INPUT = 'input',
	OUTPUT = 'output'
}

const props = defineProps<{
	node: WorkflowNode;
}>();

const modelConfigurationRef = ref();
const calibrationView = ref(CalibrationView.INPUT);
const expandedRows = ref([]);

const runId = ref<number>(0);
const trainTestValue = ref(80);

const timestepColumn = ref('');
const mapping = ref<any[]>([
	{
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	}
]);
const datasetVariables = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const modelConfiguration = ref<ModelConfiguration | null>(null);
const calibratedModelConfig = ref<ModelConfiguration | undefined>(undefined);

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1].value?.[0]);
const datasetName = computed(() => props.node.inputs[1].label?.[0]);

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelVariables = computed(() => {
	if (modelConfigurationRef.value) {
		return modelConfiguration.value?.configuration.model.model.states
			.filter((state) => modelConfigurationRef.value.selectedModelVariables.includes(state.sname))
			.map((state) => state.sname);
	}
	return modelConfiguration.value?.configuration.model.model.states.map((state) => state.sname);
});

const disableRunButton = computed(
	() => !mapping.value?.[0].modelVariable.label || !mapping.value?.[0].datasetVariable.label
);

const mappingSimplified = computed(() =>
	mapping.value.map((m) => ({
		modelVariable: m.modelVariable.label,
		datasetVariable: m.datasetVariable.label
	}))
);

const calibrate = async () => {
	// Make calibration job.
	if (modelConfiguration.value && csvAsset.value) {
		const featureMappings: { [index: string]: string } = {};
		// Go from 2D array to a index: value like they want
		// was just easier to work with 2D array for user input
		for (let i = 0; i < mapping.value.length; i++) {
			if (mapping.value[i].modelVariable.label && mapping.value[i].datasetVariable.label) {
				featureMappings[mapping.value[i].datasetVariable.label] =
					mapping.value[i].modelVariable.label;
			}
		}

		const calibrationParam: CalibrationParams = {
			model: shimPetriModel(AMRToPetri(modelConfiguration.value.configuration.model)), // Take out all the extra content in model
			initials: {}, // reconstruct from modelConfiguration.value.configuration
			params: {},
			timesteps_column: timestepColumn.value,
			feature_mappings: featureMappings,
			dataset: csvAsset.value.csv.map((row) => row.join(',')).join('\n')
		};
		const results = await makeCalibrateJob(calibrationParam);
		runId.value = results.id;

		const calibratePoller = new Poller<object>()
			.setInterval(2000)
			.setThreshold(90)
			.setPollAction(async () => {
				const statusResponse = await getRunStatus(results.id);

				if (statusResponse.status === 'done') {
					return {
						data: statusResponse,
						progress: null,
						error: null
					};
				}
				if (statusResponse.status !== 'running') {
					throw Error('failed calibrate');
				}

				return {
					data: null,
					progress: null,
					error: null
				};
			});
		await calibratePoller.start();

		const calibratedParams = await getRunResult(results.id);

		const resultsHaveNull =
			calibratedParams === null || Object.values(calibratedParams).some((v) => v === null);

		const { csv, headers } = csvAsset.value;
		const indexOfTimestep = headers.indexOf(timestepColumn.value);
		const payload = {
			model: calibrationParam.model,
			initials: {}, // reconstruct from modelConfig.value.configuration
			params: calibratedParams,
			tspan: [Number(csv[1][indexOfTimestep]), Number(csv[csv.length - 1][indexOfTimestep])]
		};

		calibratedModelConfig.value = cloneDeep(modelConfiguration.value);
		if (calibratedParams) {
			// calibratedModelConfig.value.parameterValues = calibratedParams;
		}

		if (resultsHaveNull) {
			logger.error("The resulting parameters include null value(s) which can't be simulated.");
		} else {
			// do polling and retrieve calibration result
			const forecastResponse = await makeForecast(payload);
			const forecastPoller = new Poller<object>()
				.setInterval(2000)
				.setThreshold(90)
				.setPollAction(async () => {
					const statusResponse = await getRunStatus(forecastResponse.id);

					if (statusResponse.status === 'done') {
						return {
							data: statusResponse,
							progress: null,
							error: null
						};
					}
					if (statusResponse.status !== 'running') {
						throw Error('failed forecast');
					}

					return {
						data: null,
						progress: null,
						error: null
					};
				});
			await forecastPoller.start();

			const resultCsv = await getRunResult(forecastResponse.id);
			const result = csvParse(resultCsv);

			openedWorkflowNodeStore.setCalibrateResults(
				csvAsset.value,
				result as any,
				indexOfTimestep,
				featureMappings,
				mappingSimplified.value
			);
		}
	}
};

function addMapping() {
	mapping.value.push({
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	});
}

watch(
	() => modelConfigId.value,
	async () => {
		if (modelConfigId.value) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigId.value);
		}
	}
);

watch(
	() => datasetId.value,
	async () => {
		// Trouble getting these as computed values
		if (datasetId.value) {
			// Get dataset:
			const dataset: Dataset | null = await getDataset(datasetId.value.toString());
			// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
			csvAsset.value = (await downloadRawFile(
				datasetId.value.toString(),
				dataset?.fileNames?.[0] ?? ''
			)) as CsvAsset;
			datasetVariables.value = csvAsset.value?.headers;
		}
		// Reset mapping on update for now
		mapping.value = [
			{
				modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
				datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
			}
		];
	},
	{
		immediate: true
	}
);
</script>

<style scoped>
.p-accordion {
	padding-top: 1rem;
}

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

.train-test-ratio {
	display: flex;
	gap: 1rem;
	margin: 0.5rem 0;
}

.train-test-ratio > .p-slider {
	margin-top: 1rem;
	width: 100%;
}

.mapping {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
