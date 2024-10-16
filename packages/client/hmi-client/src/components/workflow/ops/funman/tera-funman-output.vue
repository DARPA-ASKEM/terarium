<template>
	<header class="flex align-items-start">
		<div>
			<h4>{{ validatedModelConfiguration?.name }}</h4>
			<span class="secondary-text">Output generated date</span>
		</div>
		<div class="btn-group">
			<Button label="Add to report" outlined severity="secondary" disabled />
			<Button label="Save for reuse" outlined severity="secondary" disabled />
		</div>
	</header>

	<Accordion multiple :active-index="[0, 1, 2, 3]">
		<AccordionTab header="Summary"> Summary text </AccordionTab>
		<AccordionTab>
			<template #header> State variables<i class="pi pi-info-circle" /> </template>
			<!--TODO: Will put these checkbox options in output settings later-->
			<div class="flex align-items-center gap-2 ml-4 mb-3">
				<Checkbox v-model="onlyShowLatestResults" binary @change="renderCharts" />
				<label>Only show furthest results</label>
			</div>
			<div class="flex align-items-center gap-2 ml-4 mb-4">
				<Checkbox v-model="focusOnModelChecks" binary @change="updateStateChart" /> <label>Focus on model checks</label>
			</div>
			<template v-if="stateChart">
				<Dropdown class="ml-4" v-model="selectedState" :options="stateOptions" @update:model-value="updateStateChart" />
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
import Checkbox from 'primevue/checkbox';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import {
	type ProcessedFunmanResult,
	type FunmanConstraintsResponse,
	processFunman
} from '@/services/models/funman-service';
import { createFunmanStateChart, createFunmanParameterCharts } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { getRunResult } from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue'; // TODO: Once we save the output model properly in the backend we can use this.
import { logger } from '@/utils/logger';
import type { Model, ModelConfiguration, Observable } from '@/types/Types';
import { getModelByModelConfigurationId, getMMT } from '@/services/model';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel, makeConfiguredMMT } from '@/model-representation/mira/mira';
import { View, parse } from 'vega';

const props = defineProps<{
	runId: string;
	trajectoryState?: string;
}>();

const emit = defineEmits(['update:trajectoryState']);

let processedFunmanResult: ProcessedFunmanResult | null = null;
let constraintsResponse: FunmanConstraintsResponse[] = [];
let mmt: MiraModel = emptyMiraModel();
let funmanResult: any = {};

// Model configuration stuff
const model = ref<Model | null>(null);
const validatedModelConfiguration = ref<ModelConfiguration | null>(null);
const configuredMmt = ref<MiraModel | null>(null);
const mmtParams = ref<MiraTemplateParams>({});
const calibratedConfigObservables = ref<Observable[]>([]);

const stateOptions = ref<string[]>([]);
const selectedState = ref<string>('');
const onlyShowLatestResults = ref(false);
const focusOnModelChecks = ref(false);

const stateChart = ref<any>({});
const parameterCharts = ref<any>({});

let selectedBoxId: number = -1;
let parameterChartView: View | null = null;
let parameterChartListener: ((name: string, value: any) => void) | null = null;

function updateStateChart() {
	if (!processedFunmanResult) return;
	emit('update:trajectoryState', selectedState.value);
	stateChart.value = createFunmanStateChart(
		processedFunmanResult.trajectories,
		constraintsResponse,
		selectedState.value,
		focusOnModelChecks.value,
		selectedBoxId
	);
}

async function renderCharts() {
	processedFunmanResult = processFunman(funmanResult, onlyShowLatestResults.value);

	// State chart
	selectedState.value = props.trajectoryState ?? stateOptions.value[0];
	updateStateChart();

	// Parameter charts
	const distributionParameters = funmanResult.request.parameters.filter((d: any) => d.interval.lb !== d.interval.ub); // TODO: This conditional may change as funman will return constants soon
	if (processedFunmanResult.boxes) {
		parameterCharts.value = createFunmanParameterCharts(distributionParameters, processedFunmanResult.boxes);
	}

	// Remove existing event listener if it exists
	if (parameterChartView && parameterChartListener) {
		parameterChartView.removeSignalListener('selectedBoxId', parameterChartListener);
	}
	// FIXME: Try to grab the selected box id from the parameter chart view
	parameterChartView = new View(parse(parameterCharts.value)).renderer('canvas').initialize('#view').run();
	parameterChartListener = (name, value) => {
		console.log('Selected Box ID:', name, value);
		selectedBoxId = value;
		updateStateChart();
	};
	parameterChartView.addSignalListener('selectedBoxId', parameterChartListener);

	// For displaying model/model configuration
	// Model will be the same on runId change, no need to fetch it again
	if (!model.value) {
		model.value = await getModelByModelConfigurationId(funmanResult.modelConfiguration.id);
		if (!model.value) {
			logger.error('Failed to fetch model');
			return;
		}
		const response = await getMMT(model.value);
		if (response) {
			mmt = response.mmt;
			mmtParams.value = response.template_params;
		}
	}

	configuredMmt.value = makeConfiguredMMT(mmt, funmanResult.modelConfiguration);
	calibratedConfigObservables.value = funmanResult.modelConfiguration.observableSemanticList.map(
		({ referenceId, states, expression }) => ({
			id: referenceId,
			name: referenceId,
			states,
			expression
		})
	);
}

const initalize = async () => {
	const rawFunmanResult = await getRunResult(props.runId, 'validation.json');
	if (!rawFunmanResult) {
		logger.error('Failed to fetch funman result');
		return;
	}
	funmanResult = JSON.parse(rawFunmanResult);
	constraintsResponse = funmanResult.request.constraints;
	stateOptions.value = funmanResult.model.petrinet.model.states.map(({ id }) => id);
	validatedModelConfiguration.value = funmanResult.modelConfiguration;

	renderCharts();
};

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
