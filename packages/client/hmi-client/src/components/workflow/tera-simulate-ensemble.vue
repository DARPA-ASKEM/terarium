<template>
	<section class="tera-ensemble">
		<div class="ensemble-header p-buttonset">
			<span class="ensemble-header-label">Ensemble</span>
			<Button
				label="Input"
				severity="secondary"
				icon="pi pi-sign-in"
				size="small"
				:active="activeTab === EnsembleTabs.input"
				@click="activeTab = EnsembleTabs.input"
			/>
			<Button
				label="Ouput"
				severity="secondary"
				icon="pi pi-sign-out"
				size="small"
				:active="activeTab === EnsembleTabs.output"
				@click="activeTab = EnsembleTabs.output"
			/>
		</div>
		<div
			v-if="activeTab === EnsembleTabs.output && node?.outputs.length"
			class="simulate-container"
		>
			<p>Ensemble Output here</p>
		</div>

		<div v-else-if="activeTab === EnsembleTabs.input && node" class="simulate-container">
			<Accordion :multiple="true" :active-index="[0]">
				<AccordionTab header="Model Weights">
					<div class="model-weights">
						<section class="ensemble-calibration-mode">
							<label>
								<input
									type="radio"
									v-model="ensembleCalibrationMode"
									:value="EnsembleCalibrationMode.EQUALWEIGHTS"
								/>
								{{ EnsembleCalibrationMode.EQUALWEIGHTS }}
							</label>
							<label>
								<input
									type="radio"
									v-model="ensembleCalibrationMode"
									:value="EnsembleCalibrationMode.CALIBRATIONWEIGHTS"
								/>
								{{ EnsembleCalibrationMode.CALIBRATIONWEIGHTS }}
							</label>
							<label>
								<input
									type="radio"
									v-model="ensembleCalibrationMode"
									:value="EnsembleCalibrationMode.CUSTOM"
								/>
								{{ EnsembleCalibrationMode.CUSTOM }}
							</label>
						</section>
						<!-- Turn this into a horizontal bar chart -->
						<section class="ensemble-calibration-graph">
							<table class="p-datatable-table">
								<thead class="p-datatable-thead">
									<th>Model Config ID</th>
									<th>Weight</th>
								</thead>
								<tbody class="p-datatable-tbody">
									<tr v-for="(id, i) in listModelIds" :key="i">
										<td>
											{{ id }}
										</td>
										<td>
											{{ weights[i] }}
										</td>
									</tr>
								</tbody>
							</table>
						</section>
					</div>
				</AccordionTab>
				<AccordionTab header="Mapping"> </AccordionTab>
			</Accordion>
		</div>
	</section>
</template>

<script setup lang="ts">
// import _ from 'lodash';
import { ref, computed } from 'vue';
// import { Model } from '@/types/Types';
// import { getModelConfigurationById } from '@/services/model-configurations';
// import { getSimulation, getRunResult } from '@/services/models/simulation-service';
// import { getModel } from '@/services/model';
// import { csvParse } from 'd3';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
// import { workflowEventBus } from '@/services/workflow';

const props = defineProps<{
	node: WorkflowNode;
}>();

enum EnsembleTabs {
	input,
	output
}

enum EnsembleCalibrationMode {
	EQUALWEIGHTS = 'equalWeights',
	CALIBRATIONWEIGHTS = 'calibrationWeights',
	CUSTOM = 'custom'
}

const activeTab = ref(EnsembleTabs.input);
const listModelIds = computed<string[]>(() => props.node.state.modelConfigIds);
// const listModels = ref<Model>();
const ensembleCalibrationMode = ref<string>(EnsembleCalibrationMode.EQUALWEIGHTS);

// List of each observible + state for each model.
// const listModelOptions = ref<{}[]>();

function calculateWeights() {
	if (
		ensembleCalibrationMode.value === EnsembleCalibrationMode.EQUALWEIGHTS &&
		listModelIds.value
	) {
		const percent = 1 / listModelIds.value.length;
		const outputList: number[] = [];
		for (let i = 0; i < listModelIds.value.length; i++) {
			outputList.push(percent);
		}
		return outputList;
	}
	return [];
}
const weights = computed(calculateWeights);

// watch(
// 	() => props.node.inputs,
// 	async () => {
// 		console.log("Inputs changed");
// 		if (!props.node.inputs[0].value) return;
// 		listModelIds.value = [];
// 		for (let i = 0; i < props.node.inputs[0].value.length; i++){
// 			listModelIds.value.push(props.node.inputs[0].value[i]);
// 		}
// 		console.log(weights.value);
// 	},
// 	{ immediate: true }
// );
</script>

<style scoped>
.add-chart {
	width: 9em;
	margin: 0em 1em;
	margin-bottom: 1em;
}

.tera-ensemble {
	background: white;
	z-index: 1;
}

.ensemble-calibration-mode {
	display: grid;
	padding-left: 0.5rem;
}

.ensemble-calibration-graph {
	padding-left: 0.5rem;
}
.model-weights {
	display: flex;
}

.ensemble-header {
	display: flex;
	margin: 1em;
}
th {
	text-align: left;
}

th,
td {
	padding-left: 15px;
}

.ensemble-header-label {
	display: flex;
	align-items: center;
	margin: 0 1em;
	font-weight: 700;
	font-size: 1.75em;
}

.simulate-container {
	overflow-y: scroll;
}

.simulate-chart {
	margin: 2em 1em;
}

.sim-tspan-container {
	display: flex;
	gap: 1em;
}

.sim-tspan-group {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	flex-basis: 0;
}

::v-deep .p-inputnumber-input,
.p-inputwrapper {
	width: 100%;
}
</style>
