<template>
	<header class="flex align-items-start">
		<div>
			<h4>{{ validatedModelConfiguration?.name }}</h4>
			<span class="secondary-text">Output generated date</span>
		</div>
		<div class="btn-group">
			<Button label="Add to report" outlined severity="secondary" disabled />
			<Button label="Save for reuse" outlined severity="secondary" />
		</div>
	</header>
	<Accordion multiple :active-index="[0, 1, 2, 3]">
		<AccordionTab header="Summary"> Summary text </AccordionTab>
		<AccordionTab>
			<template #header> State variables<i class="pi pi-info-circle" /> </template>
			<template v-if="stateChart">
				<Dropdown v-model="selectedState" :options="stateOptions" @update:model-value="updateStateChart" />
				<vega-chart :visualization-spec="stateChart" :are-embed-actions-visible="false" />
			</template>
			<span class="ml-4" v-else> No boxes were generated. </span>
		</AccordionTab>
		<AccordionTab>
			<template #header>Parameters<i class="pi pi-info-circle" /></template>
			<vega-chart :visualization-spec="parameterCharts" :are-embed-actions-visible="false" />
		</AccordionTab>
		<AccordionTab header="Diagram">
			<tera-model-diagram v-if="model" :model="model" />
		</AccordionTab>
	</Accordion>
	<template v-if="model && validatedModelConfiguration && configuredMmt">
		<tera-initial-table
			:model="model"
			:model-configuration="validatedModelConfiguration"
			:model-configurations="[]"
			:mmt="configuredMmt"
			:mmt-params="mmtParams"
			:feature-config="{ isPreview: true }"
		/>
		<tera-parameter-table
			:model="model"
			:model-configuration="validatedModelConfiguration"
			:model-configurations="[]"
			:mmt="configuredMmt"
			:mmt-params="mmtParams"
			:feature-config="{ isPreview: true }"
		/>
		<Accordion :active-index="0" v-if="!isEmpty(calibratedConfigObservables)">
			<AccordionTab v-if="!isEmpty(calibratedConfigObservables)" header="Observables">
				<tera-observables
					class="pl-4"
					:model="model"
					:mmt="configuredMmt"
					:observables="calibratedConfigObservables"
					:feature-config="{ isPreview: true }"
				/>
			</AccordionTab>
		</Accordion>
	</template>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch } from 'vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import {
	type ProcessedFunmanResult,
	type FunmanConstraintsResponse,
	processFunman
} from '@/services/models/funman-service';
import { createFunmanStateChart, createFunmanParameterChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { getRunResult } from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue'; // TODO: Once we save the output model properly in the backend we can use this.
import { logger } from '@/utils/logger';
import type { Model, ModelConfiguration, Observable } from '@/types/Types';
import { getModelByModelConfigurationId, getMMT } from '@/services/model';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { makeConfiguredMMT } from '@/model-representation/mira/mira';

const props = defineProps<{
	runId: string;
	trajectoryState?: string;
}>();

const emit = defineEmits(['update:trajectoryState']);

let processedFunmanResult: ProcessedFunmanResult | null = null;
let constraintsResponse: FunmanConstraintsResponse[] = [];

// Model configuration stuff
const model = ref<Model | null>(null);
const validatedModelConfiguration = ref<ModelConfiguration | null>(null);
const configuredMmt = ref<MiraModel | null>(null);
const mmtParams = ref<MiraTemplateParams>({});
const calibratedConfigObservables = ref<Observable[]>([]);

const stateOptions = ref<string[]>([]);
const selectedState = ref<string>('');

const stateChart = ref();
const parameterCharts = ref();

const initalize = async () => {
	const rawFunmanResult = await getRunResult(props.runId, 'validation.json');
	if (!rawFunmanResult) {
		logger.error('Failed to fetch funman result');
		return;
	}
	const funmanResult = JSON.parse(rawFunmanResult);
	constraintsResponse = funmanResult.request.constraints;
	stateOptions.value = funmanResult.model.petrinet.model.states.map(({ id }) => id);
	validatedModelConfiguration.value = funmanResult.modelConfiguration;

	processedFunmanResult = processFunman(funmanResult);

	// State chart
	selectedState.value = props.trajectoryState ?? stateOptions.value[0];
	updateStateChart();

	console.log(funmanResult); /// //////////////////////////

	// Parameter charts
	const parametersOfInterest = funmanResult.request.parameters.filter((d: any) => d.label === 'all');
	if (processedFunmanResult.boxes) {
		parameterCharts.value = createFunmanParameterChart(parametersOfInterest, processedFunmanResult.boxes);
	}

	// For displaying model/model configuration
	model.value = await getModelByModelConfigurationId(funmanResult.modelConfiguration.id);
	if (!model.value) {
		logger.error('Failed to fetch model');
		return;
	}
	const response = await getMMT(model.value);
	const mmt = response.mmt;
	mmtParams.value = response.template_params;

	configuredMmt.value = makeConfiguredMMT(mmt, funmanResult.modelConfiguration);
	calibratedConfigObservables.value = funmanResult.modelConfiguration.observableSemanticList.map(
		({ referenceId, states, expression }) => ({
			id: referenceId,
			name: referenceId,
			states,
			expression
		})
	);
};

function updateStateChart() {
	if (!processedFunmanResult) return;
	emit('update:trajectoryState', selectedState.value);
	stateChart.value = createFunmanStateChart(processedFunmanResult, constraintsResponse, selectedState.value);
}

watch(
	() => props.runId,
	() => {
		initalize();
	},
	{ immediate: true }
);
</script>

<style scoped>
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
	margin-left: auto;
}

.pi-info-circle {
	margin-left: var(--gap-2);
}

.secondary-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}
</style>
