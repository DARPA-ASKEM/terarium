<template>
	<div
		v-if="isConfigurationVisible"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header model-configuration"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead class="p-datatable-thead">
					<tr v-if="isEditable">
						<th class="p-frozen-column"></th>
						<th class="p-frozen-column second-frozen"></th>
						<th v-for="({ name, colspan }, i) in tableHeaders" :colspan="colspan" :key="i">
							{{ name }}
						</th>
					</tr>
					<tr>
						<th class="p-frozen-column" />
						<th class="p-frozen-column second-frozen">Select all</th>
						<th v-for="({ id }, i) in configuration.states" :header="id" :key="i">
							{{ id }}
						</th>
						<th v-for="({ id }, i) in configuration.transitions" :header="id" :key="i">
							{{ id }}
						</th>
						<!--TODO: Insert new th loops for time and observables here-->
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr>
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
						<td class="p-frozen-column second-frozen" tabindex="0">Default name</td>
						<td
							v-for="({ id }, i) in [...configuration.states, ...configuration.transitions]"
							:key="i"
						>
							<section class="editable-cell">
								<span>{{ id }}<i class="pi pi-table"></i></span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click.stop="openValueModal"
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
	></SplitButton>
	<Teleport to="body">
		<tera-modal v-if="openValueConfig" @modal-mask-clicked="openValueConfig = false">
			<template #header>
				<h4>
					Matrix
					<!-- {{
                        modelConfigurations[modalVal.configIndex].configuration.semantics.ode[modalVal.odeType][
                        modalVal.odeObjIndex
                        ]['id'] ??
                        modelConfigurations[modalVal.configIndex].configuration.semantics.ode[modalVal.odeType][
                            modalVal.odeObjIndex
                        ]['target']
                    }} -->
				</h4>
				<span>Configure the matrix values</span>
			</template>
			<template #default>
				<tera-stratify-value-matrix />
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
							<label for="name">Value</label>
							<InputText class="p-inputtext-sm" :key="'value' + i" v-model="extraction.value" />
						</div>
						<div v-if="modalVal.odeType === 'parameters'">
							<label for="name">Distribution</label>
							<Checkbox v-model="extraction.isDistribution" :binary="true"></Checkbox>
							<div v-if="extraction.isDistribution">
								<label for="name">Min</label>
								<InputText
									class="p-inputtext-sm"
									:key="'min' + i"
									v-model="extraction.distribution.parameters.minimum"
								/>
								<label for="name">Max</label>
								<InputText
									class="p-inputtext-sm"
									:key="'max' + i"
									v-model="extraction.distribution.parameters.maximum"
								/>
							</div>
						</div>
					</TabPanel>
				</TabView>
			</template>
			<template #footer>
				<Button
					label="OK"
					@click="
						() => {
							setModelParameters();
							updateModelConfigValue();
						}
					"
				/>
				<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { watch, ref, computed, onMounted } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
import Button from 'primevue/button';
import SplitButton from 'primevue/splitbutton';
import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import { ModelConfiguration, Model } from '@/types/Types';
import {
	createModelConfiguration,
	updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import { getModelConfigurations } from '@/services/model';
import TeraStratifyValueMatrix from '@/components/models/tera-stratify-value-matrix.vue';

const props = defineProps<{
	isEditable: boolean;
	model: Model;
	calibrationConfig?: boolean;
}>();

const modelConfigurations = ref<ModelConfiguration[]>([]);
const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const modalVal = ref({ odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 });

const activeIndex = ref(0);
const configItems = ref<any[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigurations.value?.map((m) => m.configuration) ?? []
);

const configuration = computed<any>(() => props.model?.semantics?.span?.[0].system.model);

// Decide if we should display the whole configuration table
const isConfigurationVisible = computed(
	() => !isEmpty(configurations) && !isEmpty(tableHeaders) && !isEmpty(cellEditStates)
);

// Makes cell inputs focus once they appear
// const vFocus = {
//     mounted: (el) => el.focus()
// };

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => [
	{ name: 'initials', colspan: configuration.value.states.length },
	{ name: 'parameters', colspan: configuration.value.transitions.length }
]);

async function addModelConfiguration(config: ModelConfiguration) {
	await createModelConfiguration(
		props.model.id,
		`Copy of ${config.name}`,
		config.description as string,
		config.configuration
	);
	setTimeout(() => {
		initializeConfigSpace();
	}, 800);
}

function openValueModal() {
	if (props.isEditable) {
		activeIndex.value = 0;
		openValueConfig.value = true;
		// modalVal.value = { odeType, valueName, configIndex, odeObjIndex };
		// const modelParameter = cloneDeep(
		//     modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex]
		// );
		// extractions.value[0].value = modelParameter[valueName];
		// extractions.value[0].name = modelParameter.name ?? 'Default';
		// extractions.value[0].isDistribution = !!modelParameter.distribution;
		// // we are only adding the ability to add one type of distribution for now...
		// extractions.value[0].distribution = modelParameter.distribution ?? {
		//     type: 'Uniform1',
		//     parameters: { minimum: null, maximum: null }
		// };
	}
}

// function to set the provided values from the modal
function setModelParameters() {
	const { odeType, valueName, configIndex, odeObjIndex } = modalVal.value;
	const modelParameter =
		modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex];
	modelParameter[valueName] = extractions.value[activeIndex.value].value;
	modelParameter.name = extractions.value[activeIndex.value].name;

	// delete the distribution if the checkbox isn't selected
	if (extractions.value[activeIndex.value].isDistribution) {
		modelParameter.distribution = extractions.value[activeIndex.value].distribution;
	} else {
		delete modelParameter.distribution;
	}
}

function updateModelConfigValue(configIndex: number = modalVal.value.configIndex) {
	const configToUpdate = modelConfigurations.value[configIndex];
	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
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

	// Refresh the datastore with whatever we currently have
	const defaultConfig = modelConfigurations.value.find(
		(d) => d.name === 'Default config'
	) as ModelConfiguration;
	if (defaultConfig) {
		defaultConfig.configuration = cloneDeep(props.model);
		updateModelConfiguration(defaultConfig);
	}

	openValueConfig.value = false;
	modalVal.value = { odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 };
	extractions.value = [{ name: '', value: '' }];
}

function resetCellEditing() {
	const row = { name: false };

	for (let i = 0; i < tableHeaders.value.length; i++) {
		const { name, colspan } = tableHeaders.value[i];
		row[name] = Array(colspan).fill(false);
	}

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigurations.value.length);
	for (let i = 0; i < modelConfigurations.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

watch(
	() => tableHeaders.value,
	() => {
		resetCellEditing();
	}
);

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

.p-datatable-thead th {
	text-transform: capitalize;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.cell-menu {
	visibility: hidden;
}

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
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

.second-frozen {
	left: 48px;
}

th:hover .cell-menu,
td:hover .cell-menu {
	visibility: visible;
}

.distribution-cell {
	display: flex;
	flex-direction: column;
}

.distribution-range {
	white-space: nowrap;
}
</style>
