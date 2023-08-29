<template>
	<div
		v-if="isConfigurationVisible"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header model-configuration"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead class="p-datatable-thead">
					<tr v-if="!featureConfig.isPreview">
						<th class="p-frozen-column"></th>
						<th class="p-frozen-column second-frozen"></th>
						<th v-for="({ name, colspan }, i) in tableHeaders" :colspan="colspan" :key="i">
							{{ name }}
						</th>
					</tr>
					<tr>
						<th class="p-frozen-column" />
						<th class="p-frozen-column second-frozen">Select all</th>
						<th v-for="(id, i) in baseModelStates" :header="id" :key="i">
							{{ id }}
						</th>
						<th v-for="(id, i) in baseModelTransitions" :header="id" :key="i">
							{{ id }}
						</th>
						<!--TODO: Insert new th loops for time and observables here-->
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="({ name }, i) in modelConfigurations" :key="i">
						<!--TODO: This td is a placeholder, row selection doesn't work-->
						<td class="p-selection-column p-frozen-column">
							<div class="p-checkbox p-component">
								<div class="p-hidden-accessible">
									<input type="checkbox" tabindex="0" aria-label="Row Unselected" />
								</div>
								<div class="p-checkbox-box p-component">
									<span class="p-checkbox-icon"></span>
								</div>
							</div>
						</td>
						<td
							class="p-frozen-column second-frozen"
							tabindex="0"
							@keyup.enter="
								modelConfigInputValue = cloneDeep(modelConfigurations[i].name);
								cellEditStates[i].name = true;
							"
							@click="
								modelConfigInputValue = cloneDeep(modelConfigurations[i].name);
								cellEditStates[i].name = true;
							"
						>
							<InputText
								v-if="cellEditStates[i]?.name"
								v-model.lazy="modelConfigInputValue"
								v-focus
								@focusout="updateModelConfigName(i)"
								@keyup.stop.enter="updateModelConfigName(i)"
								class="cell-input"
							/>
							<span v-else class="editable-cell">
								{{ name }}
							</span>
						</td>
						<td v-for="(id, j) in [...baseModelStates, ...baseModelTransitions]" :key="j">
							<section class="editable-cell" @click="openValueModal(id, i)">
								<span>{{ id }}<i class="pi pi-table" /></span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
								/>
							</section>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	<SplitButton
		outlined
		label="Add configuration"
		size="small"
		icon="pi pi-plus"
		:model="configItems"
	/>
	<Teleport to="body">
		<tera-modal
			v-if="openValueConfig"
			@modal-mask-clicked="
				openValueConfig = false;
				emit('sync-configs');
			"
			@model-submit="
				openValueConfig = false;
				emit('sync-configs');
			"
		>
			<template #header>
				<h4>{{ modalVal.id }}</h4>
				<span>Configure the matrix values</span>
			</template>
			<template #default>
				<!-- TODO: Implement value tabs for the modal once we are ready
					<TabView v-model:activeIndex="activeIndex">
					<TabPanel v-for="(extraction, i) in extractions" :key="i">
						<template #header>
							<span>{{ extraction.name }}</span>
						</template>
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" :key="'name' + i" v-model="extraction.name" />
						</div>
						<div>
							<label for="name">Matrix</label>
							<tera-stratified-value-matrix
								:model-configuration="modelConfigurations[modalVal.configIndex]"
								:id="modalVal.id"
								:stratified-model-type="stratifiedModelType"
								:node-type="modalVal.nodeType"
							/>
						</div>
					</TabPanel>
				</TabView> -->
				<tera-stratified-value-matrix
					:model-configuration="modelConfigurations[modalVal.configIndex]"
					:id="modalVal.id"
					:stratified-model-type="stratifiedModelType"
					:node-type="modalVal.nodeType"
				/>
			</template>
			<template #footer>
				<Button
					label="OK"
					@click="
						openValueConfig = false;
						emit('sync-configs');
					"
				/>
				<Button
					class="p-button-outlined"
					label="Cancel"
					@click="
						openValueConfig = false;
						emit('sync-configs');
					"
				/>
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { watch, ref, computed, onMounted } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
import Button from 'primevue/button';
import SplitButton from 'primevue/splitbutton';
// import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
// import TabPanel from 'primevue/tabpanel'; // TODO: Implement value tabs for the modal once we are ready
import InputText from 'primevue/inputtext';
// import Checkbox from 'primevue/checkbox';
import { ModelConfiguration, Model } from '@/types/Types';
import {
	createModelConfiguration,
	updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import { getModelConfigurations } from '@/services/model';
import TeraStratifiedValueMatrix from '@/components/models/tera-stratified-value-matrix.vue';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
import { getCatlabAMRPresentationData } from '@/model-representation/petrinet/catlab-petri';
import { getMiraAMRPresentationData } from '@/model-representation/petrinet/mira-petri';
import { FeatureConfig } from '@/types/common';

const props = defineProps<{
	featureConfig: FeatureConfig;
	model: Model;
	stratifiedModelType: StratifiedModelType;
	calibrationConfig?: boolean;
}>();

const emit = defineEmits(['new-model-configuration', 'update-model-configuration', 'sync-configs']);

const modelConfigInputValue = ref<string>('');
const modelConfigurations = ref<ModelConfiguration[]>([]);
const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });

const activeIndex = ref(0);
const configItems = ref<any[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigurations.value?.map((m) => m.configuration) ?? []
);

const baseModel = computed<any>(() => {
	if (props.stratifiedModelType === StratifiedModelType.Catlab) {
		return getCatlabAMRPresentationData(props.model).compactModel;
	}
	if (props.stratifiedModelType === StratifiedModelType.Mira) {
		return getMiraAMRPresentationData(props.model).compactModel.model;
	}
	return props.model.model;
});
const baseModelStates = computed<any>(() => baseModel.value.states.map(({ id }) => id));
const baseModelTransitions = computed<any>(() => baseModel.value.transitions.map(({ id }) => id));

// Decide if we should display the whole configuration table
const isConfigurationVisible = computed(
	() => !isEmpty(configurations) && !isEmpty(tableHeaders) && !isEmpty(cellEditStates)
);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => [
	{ name: 'Initials', colspan: baseModelStates.value.length },
	{ name: 'Parameters', colspan: baseModelTransitions.value.length }
]);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

async function updateModelConfigName(configIndex: number) {
	cellEditStates.value[configIndex].name = false;
	modelConfigurations.value[configIndex].name = modelConfigInputValue.value;
	await updateModelConfiguration(modelConfigurations.value[configIndex]);
	setTimeout(() => {
		emit('update-model-configuration');
		emit('sync-configs');
	}, 800);
}

async function addModelConfiguration(config: ModelConfiguration) {
	await createModelConfiguration(
		props.model.id,
		`Copy of ${config.name}`,
		config.description as string,
		config.configuration
	);
	setTimeout(() => {
		emit('new-model-configuration');
		initializeConfigSpace();
		emit('sync-configs');
	}, 800);
}

function openValueModal(id: string, configIndex: number) {
	if (!props.featureConfig.isPreview) {
		const nodeType = baseModelStates.value.includes(id) ? NodeType.State : NodeType.Transition;

		activeIndex.value = 0;
		openValueConfig.value = true;
		modalVal.value = { id, configIndex, nodeType };
	}
}

function resetCellEditing() {
	const row = { name: false };

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigurations.value.length);
	for (let i = 0; i < modelConfigurations.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

async function initializeConfigSpace() {
	let tempConfigurations = await getModelConfigurations(props.model.id);

	configItems.value = tempConfigurations.map((config) => ({
		label: config.name,
		command: () => {
			addModelConfiguration(config);
		}
	}));

	// Ensure that we always have a "default config" model configuration
	if (isEmpty(tempConfigurations) || !tempConfigurations.find((d) => d.name === 'Default config')) {
		await addDefaultConfiguration(props.model);
		tempConfigurations = await getModelConfigurations(props.model.id);
	}

	modelConfigurations.value = tempConfigurations;

	resetCellEditing();

	openValueConfig.value = false;
	modalVal.value = { id: '', configIndex: 0, nodeType: NodeType.State };
	extractions.value = [{ name: 'Default', value: '' }];
}
defineExpose({ initializeConfigSpace });

watch(
	() => props.model.id,
	() => initializeConfigSpace()
);

onMounted(() => {
	initializeConfigSpace();
});
</script>

<style scoped>
.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

.model-configuration {
	margin-bottom: 1rem;
}

/** TODO: Apply to all tables in theme or create second table rules?  */
.p-datatable-thead th {
	text-transform: none !important;
	color: var(--text-color-primary) !important;
	font-size: var(--font-size-small) !important;
	padding-left: 1rem !important;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.cell-menu {
	visibility: hidden;
}

.cell-input {
	width: calc(100%);
	height: 4rem;
}

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
}

td:has(.cell-input) {
	padding: 2px !important;
	max-width: 4rem;
}

.p-datatable:deep(td) {
	cursor: pointer;
}

.p-datatable:deep(td:focus) {
	background-color: var(--primary-color-lighter);
}

.p-frozen-column {
	left: 0px;
	white-space: nowrap;
}

.p-frozen-column,
th {
	background: transparent;
}

.second-frozen {
	left: 48px;
}

th:hover .cell-menu,
td:hover .cell-menu {
	visibility: visible;
}

.p-tabview {
	display: flex;
	gap: 1rem;
	margin-bottom: 1rem;
	justify-content: space-between;
}

.p-tabview:deep(> *) {
	width: 50vw;
	height: 65vh;
	overflow: auto;
}

.p-tabview:deep(.p-tabview-nav) {
	flex-direction: column;
}

.p-tabview:deep(label) {
	display: block;
	font-size: var(--font-caption);
	margin-bottom: 0.25rem;
}

.p-tabview:deep(.p-tabview-nav-container, .p-tabview-nav-content) {
	width: 30%;
}

.p-tabview:deep(.p-tabview-panels) {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	background-color: var(--surface-ground);
}

.p-tabview:deep(.p-tabview-panel) {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.p-tabview:deep(.p-tabview-nav li) {
	border-left: 3px solid transparent;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-header:nth-last-child(n + 3)) {
	border-bottom: 1px solid var(--surface-border-light);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight) {
	border-left: 3px solid var(--primary-color);
	background: var(--surface-highlight);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight .p-tabview-nav-link) {
	background: none;
}

.p-tabview:deep(.p-inputtext) {
	width: 100%;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-ink-bar) {
	display: none;
}

.distribution-cell {
	display: flex;
	flex-direction: column;
}

.distribution-range {
	white-space: nowrap;
}

.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}

.capitalize {
	text-transform: capitalize !important;
	font-size: var(--font-body-medium) !important;
}
</style>
