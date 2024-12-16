<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div :tabName="SubsetDataTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-right>
					<Button
						class="mr-auto mb-2"
						@click="run"
						:label="isSubsetLoading ? 'Processing' : 'Run'"
						:icon="isSubsetLoading ? 'pi pi-spinner pi-spin' : 'pi pi-play'"
						:disabled="isSubsetLoading"
					/>
				</template>
				<!-- Geo boundaries -->
				<h5>Select geo-boundaries</h5>
				<p class="subheader">
					Set your desired latitude and longitude to define the spatial boundaries. Apply spatial skipping to retain
					every nth data point for a coarser subset.
				</p>

				<!-- Preview image -->
				<img v-if="preview" :src="preview" alt="Preview" />
				<div v-else class="map-container">
					<div class="geo-box" :style="geoBoxStyle"></div>
					<p>No preview available</p>
				</div>

				<!-- Lat/Long inputs & range sliders -->
				<span>
					<label>Latitude</label>
					<InputNumber v-model="latitudeStart" placeholder="Start" />
					<Slider v-model="latitudeRange" range class="w-full" :min="-90" :max="90" :step="0.001" />
					<InputNumber v-model="latitudeEnd" placeholder="End" />
				</span>
				<span>
					<label>Longitude</label>
					<InputNumber v-model="longitudeStart" placeholder="Start" />
					<Slider v-model="longitudeRange" range class="w-full" :min="-180" :max="180" :step="0.001" />
					<InputNumber v-model="longitudeEnd" placeholder="End" />
				</span>
				<code>
					selectSpatialDomain(['{{ latitudeStart }}', '{{ latitudeEnd }}', '{{ longitudeStart }}', '{{
						longitudeEnd
					}}'])
				</code>
				<div class="flex flex-row align-items-center">
					<span>
						<Checkbox v-model="isSpatialSkipping" binary />
						<label>Spatial skipping</label>
					</span>

					<span class="ml-3">
						<p :class="!isSpatialSkipping ? 'disabled' : ''">Keep every nth datapoint</p>
						<InputNumber v-model="spatialSkipping" :disabled="!isSpatialSkipping" />
					</span>
				</div>

				<!-- Temporal slice -->
				<h5 class="mt-3">Select temporal slice</h5>
				<p class="subheader">
					Set your desired time range to define the temporal boundaries. Apply time skipping to retain every nth time
					slice for a coarser subset.
				</p>
				<div class="flex flex-row">
					<div class="col">
						<label class="float-label">From</label>
						<Calender v-model="fromDate" showIcon showTime hourFormat="24" />
					</div>
					<div class="col">
						<label class="float-label">To</label>
						<Calender v-model="toDate" showIcon showTime hourFormat="24" />
					</div>
				</div>
				<div class="flex flex-row align-items-center">
					<span>
						<Checkbox v-model="isTimeSkipping" binary />
						<label>Time skipping</label>
					</span>
					<span class="ml-3">
						<p :class="!isTimeSkipping ? 'disabled' : ''">Keep every nth time slice</p>
						<InputNumber v-model="timeSkipping" :disabled="!isTimeSkipping" />
					</span>
				</div>
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
				<tera-progress-spinner v-if="isSubsetLoading" is-inline>
					Please wait for the subset to generate. This usually takes a few minutes...
				</tera-progress-spinner>
				<!--FIXME: Should universally define css rules for tabs so that the styles used in the drilldowns are sharable here-->
				<TabView
					><TabPanel header="Description">{{ subset?.description }}</TabPanel>
					<TabPanel header="Map view">
						<tera-carousel v-if="subset?.metadata?.preview" :labels="subset.metadata.preview.map(({ year }) => year)">
							<div v-for="item in subset.metadata.preview" :key="item">
								<img :src="item.image" alt="Preview" />
							</div>
						</tera-carousel>
					</TabPanel>
					<TabPanel header="Data">
						<!--FIXME: Make this look nicer - lazily made with copilot-->
						<table>
							<thead>
								<tr>
									<th>Key</th>
									<th>Attributes</th>
									<th>Indexes</th>
									<th>Coordinates</th>
								</tr>
							</thead>
							<tbody v-if="subset?.metadata?.dataStructure">
								<tr v-for="(value, key) in subset.metadata.dataStructure" :key="key">
									<td>{{ key }}</td>
									<td>
										<ul v-for="(attrValue, attrKey) in value.attrs" :key="attrKey">
											<li>
												<strong>{{ attrKey }}:</strong> {{ attrValue }}
											</li>
										</ul>
									</td>
									<td>
										<ul>
											<li v-for="(index, indexKey) in value.indexes" :key="indexKey">
												{{ index }}
											</li>
										</ul>
									</td>
									<td>
										<ul>
											<li v-for="(coordinate, coordinateKey) in value.coordinates" :key="coordinateKey">
												{{ coordinate }}
											</li>
										</ul>
									</td>
								</tr>
							</tbody>
						</table>
					</TabPanel>
				</TabView>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<!--FIXME: Consider moving this to the modal composable for other dataset drilldowns to use-->
	<!--This modal also causes warnings to popup since the entire component isn't wrapped by something, something to do with emit passing-->
	<!--FIXME: Worry about naming subset later-->
	<!--<tera-modal
			v-if="showSaveDatasetModal"
			@modal-mask-clicked="showSaveDatasetModal = false"
			@modal-enter-press="showSaveDatasetModal = false"
		>
			<template #header>
				<h4>Save dataset as</h4>
			</template>
			<label>Name</label>
			<InputText v-model="newDatasetName" size="large" />
			<template #footer>
				<Button label="Save" @click="addSubsetToProject" :disabled="isEmpty(newDatasetName)" />
				<Button
					label="Cancel"
					severity="secondary"
					outlined
					@click="showSaveDatasetModal = false"
				/>
			</template>
		</tera-modal>-->
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { ref, computed, onMounted, watch } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraCarousel from '@/components/widgets/tera-carousel.vue';
// import TeraModal from '@/components/widgets/tera-modal.vue';
import { WorkflowNode } from '@/types/workflow';
import { getDataset, getClimateDatasetPreview, getClimateSubsetId } from '@/services/dataset';
import type { Dataset } from '@/types/Types';
// import { AssetType } from '@/types/Types';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
// import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Button from 'primevue/button';
import Calender from 'primevue/calendar';
import Checkbox from 'primevue/checkbox';
import Slider from 'primevue/slider';
import { logger } from '@/utils/logger';
// import { useProjects } from '@/composables/project';
import { SubsetDataOperationState } from './subset-data-operation';

const props = defineProps<{
	node: WorkflowNode<SubsetDataOperationState>;
}>();

const emit = defineEmits(['append-output', 'update-state', 'select-output', 'close']);

enum SubsetDataTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const dataset = ref<Dataset | null>(null);
const preview = ref<string | undefined>(undefined);

const subset = ref<Dataset | null>(null);
const isSubsetLoading = ref(props.node.state.isSubsetLoading);

const latitudeStart = ref(props.node.state.latitudeStart);
const latitudeEnd = ref(props.node.state.latitudeEnd);
const latitudeRange = ref([latitudeStart.value, latitudeEnd.value]);
// Watch for changes on latitudeStart and latitudeEnd to update the range
watch(latitudeStart, (newValue) => {
	latitudeRange.value = [newValue, latitudeRange.value[1]];
});

watch(latitudeEnd, (newValue) => {
	latitudeRange.value = [latitudeRange.value[0], newValue];
});

// Watch for changes on latitudeRange to update the start and end values while preventing overlap
watch(latitudeRange, (newRange) => {
	if (newRange[0] <= newRange[1]) {
		// Ensure the start is not greater than the end
		latitudeStart.value = newRange[0];
		latitudeEnd.value = newRange[1];
	} else {
		// Optionally, swap the values or reset to previous values to prevent the overlap
		latitudeRange.value = [latitudeRange.value[0], latitudeRange.value[0]]; // Reset to avoid overlap
	}
});

const longitudeStart = ref(props.node.state.longitudeStart);
const longitudeEnd = ref(props.node.state.longitudeEnd);
const longitudeRange = ref([longitudeStart.value, longitudeEnd.value]);

// Watch for changes on latitudeRange to update the start and end values and allow handles to swap
watch(latitudeRange, (newRange) => {
	const [newStart, newEnd] = newRange;
	if (newStart <= newEnd) {
		latitudeStart.value = newStart;
		latitudeEnd.value = newEnd;
	} else {
		// Swap values when the lower handle is dragged over the upper handle
		latitudeStart.value = newEnd;
		latitudeEnd.value = newStart;
		// Update the range to reflect the swap
		latitudeRange.value = [newEnd, newStart];
	}
});

// Watch for changes on longitudeRange to update the start and end values and allow handles to swap
watch(longitudeRange, (newRange) => {
	const [newStart, newEnd] = newRange;
	if (newStart <= newEnd) {
		longitudeStart.value = newStart;
		longitudeEnd.value = newEnd;
	} else {
		// Swap values when the lower handle is dragged over the upper handle
		longitudeStart.value = newEnd;
		longitudeEnd.value = newStart;
		// Update the range to reflect the swap
		longitudeRange.value = [newEnd, newStart];
	}
});

const geoBoxStyle = computed(() => {
	// Convert latitude and longitude to a percentage of the container
	const latPercentStart = ((latitudeStart.value + 90) / 180) * 100;
	const latPercentEnd = ((latitudeEnd.value + 90) / 180) * 100;
	const longPercentStart = ((longitudeStart.value + 180) / 360) * 100;
	const longPercentEnd = ((longitudeEnd.value + 180) / 360) * 100;

	// Calculate the top, left, width, and height of the box
	const top = `${100 - latPercentEnd}%`;
	const left = `${longPercentStart}%`;
	const width = `${longPercentEnd - longPercentStart}%`;
	const height = `${latPercentEnd - latPercentStart}%`;

	return {
		top,
		left,
		width,
		height
	};
});

const isSpatialSkipping = ref(props.node.state.isSpatialSkipping);
const spatialSkipping = ref(props.node.state.spatialSkipping);

const isTimeSkipping = ref(props.node.state.isTimeSkipping);
const timeSkipping = ref(props.node.state.timeSkipping);

const fromDate = ref(new Date(props.node.state.fromDate));
const toDate = ref(new Date(props.node.state.toDate));

const selectedOutputId = ref(props.node.active ?? '');

// const showSaveDatasetModal = ref(false);
// const newDatasetName = ref('');

const outputs = computed(() => {
	if (!isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select output to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const onSelection = async (id: string) => {
	emit('select-output', id);
};

// FIXME: The loading state is just noticeable in the drilldown - not in the node
function mutateLoadingState(isLoading: boolean) {
	isSubsetLoading.value = isLoading;
	const state = cloneDeep(props.node.state);
	state.isSubsetLoading = isLoading;
	emit('update-state', state);
}

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

// FIXME: removed drilldown hamburger menu
// async function addSubsetToProject() {
// 	const projectId = useProjects().activeProject.value?.id;
// 	if (subset.value?.id && projectId) {
// 		await useProjects().addAsset(AssetType.Dataset, subset.value.id, projectId);
// 		logger.info(`New dataset saved as ${subset.value.name}`);
// 	} else {
// 		logger.error('Subset ID not found.');
// 	}
// }

async function run() {
	console.log('running');
	if (dataset.value?.esgfId && dataset.value?.id) {
		await updateState();
		mutateLoadingState(true);

		const subsetId = await getClimateSubsetId(
			dataset.value.esgfId,
			dataset.value.id,
			`${longitudeStart.value},${longitudeEnd.value},${latitudeStart.value},${latitudeEnd.value}`,
			{
				timestamps: `${fromDate.value.toISOString()},${toDate.value.toISOString()}`
				// spatialSkipping: isSpatialSkipping.value ? spatialSkipping.value ?? undefined : undefined
			}
		);
		mutateLoadingState(false);
		const newSubset = await loadSubset(subsetId);
		if (!newSubset) return;
		emit('append-output', {
			type: 'datasetId',
			label: newSubset.name,
			value: [newSubset.id]
		});
	}
}

async function loadSubset(subsetId?: string | null) {
	if (!subsetId) {
		logger.error('No subset dataset id found');
		return null;
	}
	const newSubset = await getDataset(subsetId);
	if (!newSubset) {
		logger.error('Subset not found');
		return null;
	}
	return newSubset;
}

async function loadDataset(id: string) {
	dataset.value = await getDataset(id);
	if (!dataset.value?.esgfId) {
		logger.error('No esgfId found for dataset');
		return;
	}

	preview.value = await getClimateDatasetPreview(dataset.value.esgfId);

	if (!props.node.state.datasetId) {
		fromDate.value = new Date(dataset.value.metadata.datetime_start);
		toDate.value = new Date(dataset.value.metadata.datetime_end);
	}
}

onMounted(async () => {
	if (!props.node.state.datasetId && !props.node.inputs?.[0]?.value?.[0]) {
		logger.error('No dataset id found');
		return;
	}
	await loadDataset(props.node.state.datasetId ?? props.node.inputs?.[0]?.value?.[0]);
	if (props.node.state.isSubsetLoading) run();
});

watch(
	() => props.node.active,
	async () => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
			const subsetId = props.node.outputs.find((output) => output.id === selectedOutputId.value)?.value?.[0];
			if (!isEmpty(subsetId) && subsetId) {
				subset.value = await loadSubset(subsetId);
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.map-container {
	position: relative;
	display: flex;
	justify-content: center;
	align-items: center;
	height: 20%;
	width: 100%;
	background-color: var(--surface-ground);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	color: var(--text-color-subdued);
}

.geo-box {
	position: absolute;
	background-color: var(--surface-highlight);
	border: 1px dashed var(--primary-color);
	border-radius: 2px;
	display: flex;
	align-items: center;
	justify-content: center;
	opacity: 0.8;
}

span {
	display: flex;
	align-items: center;
	gap: var(--gap-4);

	& > label {
		width: 8rem;
	}
}
.disabled {
	color: var(--text-color-subdued);
}
.subheader {
	color: var(--text-color-subdued);
	margin-bottom: var(--gap-2);
}
:deep(main) {
	gap: var(--gap-4);
}

code {
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	padding: var(--gap-2);
}

.float-label {
	font-size: var(--font-caption);
}

.number-entry {
	width: 14rem;
}

.p-tabview {
	margin-top: var(--gap-2);
	display: flex;
	flex-direction: column;
	flex: 1;

	&:deep(.p-tabview-panels) {
		flex: 1;
		border: 1px solid var(--surface-border);
		border-bottom-left-radius: var(--border-radius);
		border-bottom-right-radius: var(--border-radius);
		background-color: var(--surface-ground);
	}
}
</style>
