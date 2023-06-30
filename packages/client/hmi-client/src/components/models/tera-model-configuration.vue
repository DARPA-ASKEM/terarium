<template>
	<DataTable
		v-if="!isEmpty(configurations) && !isEmpty(tableHeaders)"
		class="model-configuration"
		v-model:selection="selectedModelConfig"
		:value="modelConfigs"
		editMode="cell"
		showGridlines
		scrollable
		@cell-edit-complete="onCellEditComplete"
	>
		<ColumnGroup type="header">
			<Row>
				<Column v-if="isEditable" header="" frozen />
				<Column header="" frozen />
				<Column
					v-for="({ name, colspan }, i) in tableHeaders"
					:header="capitalize(name)"
					:colspan="colspan"
					:key="i"
				/>
			</Row>
			<Row>
				<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" frozen />
				<Column :header="isEditable ? 'Select all' : ''" frozen />
				<Column
					v-for="(variableName, i) in configurations[0]?.semantics?.ode.rates"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configurations[0]?.semantics?.ode.initials"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configurations[0]?.semantics?.ode.parameters"
					:header="variableName.id"
					:key="i"
				/>
			</Row>
			<!--  Add show in workflow later (very similar to "Select variables and parameters to calibrate") -->
		</ColumnGroup>
		<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" frozen />
		<!--Need to get from editableConfigs-->
		<Column field="name" frozen>
			<template #body="{ data, field }">
				{{ data[field] }}
			</template>
			<template #editor="{ data, field }">
				<InputText v-model="data[field]" autofocus />
			</template>
		</Column>
		<template v-for="({ configuration, id }, i) in [modelConfigs[0]]" :key="i">
			<Column v-for="(rate, j) of configuration?.semantics?.ode.rates" :key="i + j">
				<template #body>
					<section class="editable-cell">
						<span>{{ rate.expression }}</span
						>{{ id }}
						<Button
							class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
							icon="pi pi-ellipsis-v"
							@click.stop="openValueModal('rates', 'expression', i, j)"
						/>
					</section>
				</template>
				<template #editor>
					<InputText
						v-model="modelConfigs[i].configuration.semantics.ode.rates[j].expression"
						autofocus
					/>
				</template>
			</Column>
			<Column v-for="(initial, j) of configuration?.semantics?.ode.initials" :key="i + j + 100">
				<template #body>
					<section class="editable-cell">
						<span>{{ initial.expression }}</span
						>{{ i + j }}
						<Button
							class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
							icon="pi pi-ellipsis-v"
							@click.stop="openValueModal('initials', 'expression', i, j)"
						/>
					</section>
				</template>
				<template #editor>
					<InputText
						v-model="modelConfigs[i].configuration.semantics.ode.initials[j].expression"
						autofocus
					/>
				</template>
			</Column>
			<Column v-for="(parameter, j) of configuration?.semantics?.ode.parameters" :key="i + j + 200">
				<template #body>
					<section class="editable-cell">
						<span>{{ parameter.value }}</span
						>{{ i + j }}
						<Button
							class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
							icon="pi pi-ellipsis-v"
							@click.stop="openValueModal('parameters', 'value', i, j)"
						/>
					</section>
				</template>
				<template #editor>
					<InputText
						v-model="modelConfigs[i].configuration.semantics.ode.parameters[j].value"
						autofocus
					/>
				</template>
			</Column>
		</template>
		<!-- FIXME: Add checkboxes for calibrate in a seperate PR
			<ColumnGroup v-if="calibrationConfig" type="footer">
			<Row>
				<Column footer="Select variables and parameters to calibrate" />
				<Column v-for="(name, i) of Object.keys(initialValues[0])" :key="i">
					<template #footer>
						<Checkbox v-model="selectedInitials" :inputId="i.toString()" :value="name" />
					</template>
				</Column>
				<Column v-for="(name, i) of Object.keys(parameterValues[0])" :key="i">
					<template #footer>
						<Checkbox v-model="selectedParameters" :inputId="i.toString()" :value="name" />
					</template>
				</Column>
			</Row>
		</ColumnGroup> -->
	</DataTable>
	<Button
		v-if="isEditable"
		class="p-button-sm p-button-outlined"
		icon="pi pi-plus"
		label="Add configuration"
		@click="addModelConfiguration"
	/>
	<Teleport to="body">
		<tera-modal v-if="openValueConfig" @modal-mask-clicked="openValueConfig = false">
			<template #header>
				<h4>
					{{
						modelConfigs[modalVal.configIndex].configuration.semantics.ode[modalVal.odeType][
							modalVal.odeObjIndex
						]['id'] ??
						modelConfigs[modalVal.configIndex].configuration.semantics.ode[modalVal.odeType][
							modalVal.odeObjIndex
						]['target']
					}}
				</h4>
				<span>Select a value for this configuration</span>
			</template>
			<template #default>
				<TabView>
					<TabPanel v-for="(tab, i) in extractions" :key="tab" :header="tab">
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" v-model="extractions[i]" />
						</div>
						<div>
							<label for="name">Source</label>
							<InputText class="p-inputtext-sm" />
						</div>
						<div>
							<label for="name">Value</label>
							<InputText
								class="p-inputtext-sm"
								v-model="
									modelConfigs[modalVal.configIndex].configuration.semantics.ode[modalVal.odeType][
										modalVal.odeObjIndex
									][modalVal.valueName]
								"
							/>
						</div>
					</TabPanel>
				</TabView>
				<Button
					class="p-button-sm p-button-outlined"
					icon="pi pi-plus"
					label="Add value"
					@click="addConfigValue"
				/>
			</template>
			<template #footer>
				<Button label="OK" @click="updateModelConfigValue()" />
				<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>
<script setup lang="ts">
import { watch, ref, computed, onMounted } from 'vue';
import { capitalize, isEmpty } from 'lodash';
import DataTable from 'primevue/datatable';
// import Checkbox from 'primevue/checkbox'; // Add back in later
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import InputText from 'primevue/inputtext';
import { ModelConfiguration, Model } from '@/types/Types';
import {
	createModelConfiguration,
	updateModelConfiguration
} from '@/services/model-configurations';
import { getModelConfigurations } from '@/services/model';

const props = defineProps<{
	isEditable: boolean;
	model: Model;
	calibrationConfig?: boolean;
}>();

const modelConfigs = ref<ModelConfiguration[]>([]);

const selectedModelConfig = ref();
const extractions = ref<any[]>([]);

const openValueConfig = ref(false);
const modalVal = ref({ odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 });

// Selected columns - TODO: add in for filtering calibration dropdowns
const selectedInitials = ref<string[]>([]);
const selectedParameters = ref<string[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigs.value?.map((m) => m.configuration) ?? []
);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => {
	if (configurations.value?.[0].semantics) {
		const headerNames = Object.keys(configurations.value[0]?.semantics.ode) ?? [];
		const result: { name: string; colspan: number }[] = [];

		for (let i = 0; i < headerNames.length; i++) {
			if (configurations.value?.[0]?.semantics?.ode[headerNames[i]]) {
				result.push({
					name: headerNames[i],
					colspan: configurations.value?.[0]?.semantics?.ode[headerNames[i]].length
				});
			}
		}

		return result;
	}
	return [];
});

const selectedModelVariables = computed(() => [
	...selectedInitials.value,
	...selectedParameters.value
]);
defineExpose({ selectedModelVariables });

async function addModelConfiguration() {
	const response = await createModelConfiguration(
		props.model.id,
		`Config ${modelConfigs.value.length + 1}`,
		'shawntest',
		modelConfigs.value[modelConfigs.value.length - 1].configuration
	);
	console.log(response);

	// TODO: notify change
}

function addConfigValue() {
	extractions.value.push(`Untitled`);
}

const onCellEditComplete = ({ newData, index }) => {
	if (props.isEditable) {
		modelConfigs.value[index].configuration = newData;
		updateModelConfiguration(modelConfigs.value[index]);
	}
};

function openValueModal(
	odeType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	if (props.isEditable) {
		openValueConfig.value = true;
		modalVal.value = { odeType, valueName, configIndex, odeObjIndex };
	}
}

function updateModelConfigValue() {
	const { configIndex } = modalVal.value;
	const configToUpdate = modelConfigs.value[configIndex];
	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
}

async function initializeConfigSpace() {
	modelConfigs.value = [];
	modelConfigs.value = (await getModelConfigurations(props.model.id)) as ModelConfiguration[];

	console.log('number of configs', modelConfigs.value);

	extractions.value = ['Default'];
	openValueConfig.value = false;
	modalVal.value = { odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 };
}

watch(
	() => props.model,
	() => initializeConfigSpace(),
	{ deep: true }
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

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.cell-menu {
	visibility: hidden;
}

.p-datatable:deep(.editable-cell) {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
}

.p-datatable:deep(td) {
	cursor: pointer;
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
	height: 50vh;
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
	width: 100%;
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
</style>
