<template>
	<section class="tera-simulate">
		<div class="simulate-header p-buttonset">
			<Button
				label="Input"
				severity="secondary"
				icon="pi pi-sign-in"
				size="small"
				:active="activeTab === SimulateTabs.input"
				@click="activeTab = SimulateTabs.input"
			/>
			<Button
				label="Ouput"
				severity="secondary"
				icon="pi pi-sign-out"
				size="small"
				:active="activeTab === SimulateTabs.output"
				@click="activeTab = SimulateTabs.output"
			/>
			<span class="simulate-header-label">Simulate</span>
		</div>
		<div v-if="activeTab === SimulateTabs.output" class="simulate-container">
			<simulate-chart
				v-for="index in openedWorkflowNodeStore.numCharts"
				:key="index"
				:run-results="props.node.outputs[0].value?.[0].runResults"
				:run-id-list="props.node.outputs[0].value?.[0].runIdList"
				:chart-idx="index"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="openedWorkflowNodeStore.appendChart"
				label="Add Chart"
				icon="pi pi-plus"
			></Button>
		</div>
		<div v-else-if="activeTab === SimulateTabs.input" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> {{ props.node.outputs[0].value?.[0].model.name }} </template>
						<model-diagram :model="props.node.outputs[0].value?.[0].model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Model configurations ({{ simConfigs.length }}) </template>
						<DataTable
							v-if="props.node.outputs[0].value?.length"
							class="model-configuration"
							showGridlines
							:value="simConfigs"
						>
							<ColumnGroup type="header">
								<Row>
									<Column selection-mode="multiple" headerStyle="width: 3rem" />
									<Column v-for="(v, i) of simVars" :key="i" :header="v" />
								</Row>
							</ColumnGroup>
							<Column selection-mode="multiple" headerStyle="width: 3rem" />
							<Column v-for="(value, i) of simVars" :key="i" :field="value"> </Column>
						</DataTable>
					</AccordionTab>
					<AccordionTab>
						<template #header> Simulation Time Range </template>
						<div class="sim-tspan-container">
							<div class="sim-tspan-group">
								<label for="1">Units</label>
								<Dropdown
									id="1"
									class="p-inputtext-sm"
									v-model="openedWorkflowNodeStore.tspanUnit"
									:options="TspanUnitList"
								/>
							</div>
							<div class="sim-tspan-group">
								<label for="2">Start date</label>
								<InputNumber
									id="2"
									class="p-inputtext-sm"
									v-model="openedWorkflowNodeStore.tspan[0]"
									inputId="integeronly"
								/>
							</div>
							<div class="sim-tspan-group">
								<label for="3">End date</label>
								<InputNumber
									id="3"
									class="p-inputtext-sm"
									v-model="openedWorkflowNodeStore.tspan[1]"
									inputId="integeronly"
								/>
							</div>
						</div>
					</AccordionTab>
				</Accordion>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { WorkflowNode } from '@/types/workflow';

import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { TspanUnits } from '@/types/SimulateConfig';

import SimulateChart from './tera-simulate-chart.vue';
import ModelDiagram from '../models/tera-model-diagram.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

enum SimulateTabs {
	input,
	output
}

const activeTab = ref(SimulateTabs.input);

const TspanUnitList = computed(() =>
	Object.values(TspanUnits).filter((v) => Number.isNaN(Number(v)))
);

const simVars = computed(() => [
	'Configuration Name',
	...(props.node.outputs[0].value as any[])[0].model.content.S.map((state) => state.sname),
	...(props.node.outputs[0].value as any[])[0].model.content.T.map((state) => state.tname)
]);

const simConfigs = computed(() =>
	props.node.outputs[0].value?.[0].runConfigs.map(
		(runConfig: typeof openedWorkflowNodeStore, i: number) => ({
			'Configuration Name': `Config ${i + 1}`,
			...runConfig.initialValues,
			...runConfig.parameterValues
		})
	)
);
</script>

<style scoped>
.add-chart {
	width: 9em;
	margin: 0em 1em;
	margin-bottom: 1em;
}

.tera-simulate {
	background: white;
	z-index: 1;
}

.simulate-header {
	display: flex;
	margin: 1em;
}
.simulate-header-label {
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
