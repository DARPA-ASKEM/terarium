<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="SubsetDataTabs.Wizard">
			<tera-drilldown-section>
				<!-- <InputText placeholder="What do you want to do?" /> -->
				<h3>Select geo-boundaries</h3>
				<img :src="preview" alt="Preview" />
				<span>
					<label>Latitude</label>
					<InputNumber v-model="latitudeStart" placeholder="Start" />
					<InputNumber v-model="latitudeEnd" placeholder="End" />
				</span>
				<span>
					<label>Longitude</label>
					<InputNumber v-model="longitudeStart" placeholder="Start" />
					<InputNumber v-model="longitudeEnd" placeholder="End" />
				</span>
				<code>
					selectSpatialDomain(['{{ latitudeStart }}', '{{ latitudeEnd }}', '{{ longitudeStart }}',
					'{{ longitudeEnd }}'])
				</code>
				<span>
					<Checkbox v-model="isSpatialSkipping" binary />
					<label>Spatial skipping</label>
				</span>
				<div class="number-entry">
					<label class="float-label">Keep every nth datapoint</label>
					<InputNumber v-model="spatialSkipping" :disabled="!isSpatialSkipping" />
				</div>
				<h3>Select temporal slice</h3>
				<span>
					<div>
						<label class="float-label">From</label>
						<Calender v-model="fromDate" showIcon />
					</div>
					<div>
						<label class="float-label">To</label>
						<Calender v-model="toDate" showIcon />
					</div>
				</span>
				<span>
					<Checkbox v-model="isTimeSkipping" binary />
					<label>Time skipping</label>
				</span>
				<div class="number-entry">
					<label class="float-label">Keep every nth time slice</label>
					<InputNumber v-model="timeSkipping" :disabled="!isTimeSkipping" />
				</div>
				<template #footer>
					<!--FIXME: A lot of these drilldowns have this margin auto for the left footer's button.
						We should make it so the left footer is aligned left and the right
						footer is aligned right-->
					<Button
						:style="{ marginRight: 'auto' }"
						@click="run"
						label="Run"
						icon="pi pi-play"
						outlined
						severity="secondary"
					/>
				</template>
			</tera-drilldown-section>
		</div>
		<div :tabName="SubsetDataTabs.Notebook"></div>
		<template #preview>
			<tera-drilldown-preview
				title="Output"
				:options="outputs"
				@update:selection="onSelection"
				v-model:output="selectedOutputId"
				is-selectable
			>
				<!--FIXME: Should universally define css rules for tabs so that the styles used in the drilldowns are sharable here-->
				<TabView>
					<TabPanel header="Map view">
						<tera-progress-spinner v-if="isSubsetLoading" />
						<img :src="subsetPreview" alt="Subset preview" />
					</TabPanel>
					<TabPanel header="Description"> </TabPanel>
					<TabPanel header="Data"> </TabPanel>
				</TabView>
				<template #footer>
					<Button label="Save as new dataset" outlined severity="secondary" />
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { ref, computed, onMounted } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { WorkflowNode } from '@/types/workflow';
import { getDataset, getClimateDatasetPreview, getClimateSubset } from '@/services/dataset';
import type { Dataset } from '@/types/Types';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
// import InputText from 'primevue/InputText';
import InputNumber from 'primevue/inputnumber';
import Button from 'primevue/button';
import Calender from 'primevue/calendar';
import Checkbox from 'primevue/checkbox';
import { logger } from '@/utils/logger';
import { SubsetDataOperationState } from './subset-data-operation';

const props = defineProps<{
	node: WorkflowNode<SubsetDataOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'select-output', 'close']);

enum SubsetDataTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const dataset = ref<Dataset | null>(null);
const preview = ref<string | undefined>(undefined);

const subset = ref<Dataset | null>(null);
const subsetPreview = ref<string | undefined>(undefined);
const isSubsetLoading = ref(false);

const latitudeStart = ref(props.node.state.latitudeStart);
const latitudeEnd = ref(props.node.state.latitudeEnd);
const longitudeStart = ref(props.node.state.longitudeStart);
const longitudeEnd = ref(props.node.state.longitudeEnd);

const isSpatialSkipping = ref(props.node.state.isSpatialSkipping);
const spatialSkipping = ref(props.node.state.spatialSkipping);

const isTimeSkipping = ref(props.node.state.isTimeSkipping);
const timeSkipping = ref(props.node.state.timeSkipping);

const fromDate = ref(props.node.state.fromDate);
const toDate = ref(props.node.state.toDate);

const selectedOutputId = ref(props.node.active ?? '');

const outputs = computed(() => {
	if (isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const onSelection = (id: string) => {
	emit('select-output', id);
};

function updateState() {
	const state = cloneDeep(props.node.state);
	state.datasetId = dataset.value?.id ?? null;
	state.latitudeStart = latitudeStart.value;
	state.latitudeEnd = latitudeEnd.value;
	state.longitudeStart = longitudeStart.value;
	state.longitudeEnd = longitudeEnd.value;
	state.isSpatialSkipping = isSpatialSkipping.value;
	state.spatialSkipping = spatialSkipping.value;
	state.isTimeSkipping = isTimeSkipping.value;
	state.timeSkipping = timeSkipping.value;
	state.fromDate = fromDate.value;
	state.toDate = toDate.value;
	emit('update-state', state);
}

async function run() {
	if (dataset.value?.esgfId && dataset.value?.id) {
		updateState();
		isSubsetLoading.value = true;

		subset.value = await getClimateSubset(
			dataset.value.esgfId,
			dataset.value.id,
			`${longitudeStart.value},${longitudeEnd.value},${latitudeStart.value},${latitudeEnd.value}`
			// `${fromDate.value.toISOString()},${toDate.value.toISOString()}`,
			// isSpatialSkipping.value ? spatialSkipping.value ?? undefined : undefined // Not sure if its this or timeSkipping
		);
		isSubsetLoading.value = false;
		console.log('subset', subset.value);
		if (subset.value) {
			console.log('dataset', dataset.value);
			console.log('subset', subset.value);
		}
	}
}

async function loadDataset(id: string) {
	dataset.value = await getDataset(id);
	if (!dataset.value?.esgfId) {
		logger.error('No esgfId found');
		return;
	}

	preview.value = await getClimateDatasetPreview(dataset.value.esgfId);

	if (!props.node.state.datasetId) {
		fromDate.value = new Date(dataset.value.metadata.datetime_start);
		toDate.value = new Date(dataset.value.metadata.datetime_end);
	}
}

onMounted(() => {
	if (!props.node.state.datasetId && !props.node.inputs?.[0]?.value?.[0]) {
		logger.error('No dataset id found');
		return;
	}
	loadDataset(props.node.state.datasetId ?? props.node.inputs?.[0]?.value?.[0]);
});
</script>

<style scoped>
span {
	display: flex;
	align-items: center;
	gap: var(--gap);

	& > label {
		width: 8rem;
	}
}

:deep(main) {
	gap: var(--gap);
}

code {
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	padding: var(--gap-small);
}

.float-label {
	font-size: var(--font-caption);
}

.number-entry {
	width: 14rem;
}

.p-tabview {
	margin-top: var(--gap-small);
	display: flex;
	flex-direction: column;
	flex: 1;

	&:deep(.p-tabview-panels) {
		flex: 1;
		border: 1px solid var(--surface-border);
		border-radius: var(--border-radius);
		background-color: var(--surface-ground);
	}
}
</style>
