<template>
	<DataTable
		v-if="!isEmpty(amrConfigurations) && !isEmpty(tableHeaders)"
		class="model-configuration"
		v-model:selection="selectedModelConfig"
		:value="amrConfigurations"
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
					v-for="(variableName, i) in amrConfigurations[0]?.semantics?.ode.rates"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in amrConfigurations[0]?.semantics?.ode.initials"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in amrConfigurations[0]?.semantics?.ode.parameters"
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
		<Column v-for="(rate, i) of amrConfigurations[0]?.semantics?.ode.rates" :key="i">
			<template #body>
				<section class="editable-cell">
					<span>{{ rate.expression }}</span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="openValueModal(rate, 'expression')"
					/>
				</section>
			</template>
			<template #editor>
				<InputText v-model="rate.expression" autofocus />
			</template>
		</Column>
		<Column v-for="(initial, i) of amrConfigurations[0]?.semantics?.ode.initials" :key="i">
			<template #body>
				<section class="editable-cell">
					<span>{{ initial.expression }}</span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="openValueModal(initial, 'expression')"
					/>
				</section>
			</template>
			<template #editor>
				<InputText v-model="initial.expression" autofocus />
			</template>
		</Column>
		<Column v-for="(parameter, i) of amrConfigurations[0]?.semantics?.ode.parameters" :key="i">
			<template #body>
				<section class="editable-cell">
					<span>{{ parameter.value }}</span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="openValueModal(parameter, 'value')"
					/>
				</section>
			</template>
			<template #editor>
				<InputText v-model="parameter.value" autofocus />
			</template>
		</Column>
		<!-- <Column v-for="(value, i) of Object.keys(modelConfigurationTable[0]).slice(
			1,
			Object.keys(modelConfigurationTable[0]).length
		)" :key="i" :field="value">
			<template #body="{ data, field }">
				<section class="editable-cell">
					<span>{{ data[field]?.value }}</span>
					<Button class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v" @click.stop="openValueModal(data, field)" />
				</section>
			</template>
			<template #editor="{ data, field }">
				<InputText v-if="data[field].value" v-model="data[field].value" autofocus />
			</template>
		</Column> -->
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
				<h4>{{ cellValueToEdit.field }}</h4>
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
								v-model="cellValueToEdit.data[cellValueToEdit.field]"
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
import { cloneDeep, capitalize, isEmpty } from 'lodash';
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
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { ModelOperation } from '@/components/workflow/model-operation';

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const props = defineProps<{
	isEditable: boolean;
	modelConfigurations: ModelConfiguration[];
	calibrationConfig?: boolean;
}>();

const editableModelConfigs = ref<ModelConfiguration[]>([]);

const selectedModelConfig = ref();
const extractions = ref<any[]>([]);

const openValueConfig = ref(false);
const cellValueToEdit = ref({ data: {}, field: '' });

// Selected columns - TODO: add in for filtering calibration dropdowns
const selectedInitials = ref<string[]>([]);
const selectedParameters = ref<string[]>([]);

const amrConfigurations = computed<Model[]>(
	() => editableModelConfigs.value?.map((m) => m.amrConfiguration) ?? []
);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => {
	const headerNames =
		Object.keys(props.modelConfigurations[0]?.amrConfiguration.semantics.ode) ?? [];
	const result: { name: string; colspan: number }[] = [];

	for (let i = 0; i < headerNames.length; i++) {
		if (amrConfigurations.value?.[0]?.semantics?.ode[headerNames[i]]) {
			result.push({
				name: headerNames[i],
				colspan: amrConfigurations.value?.[0]?.semantics?.ode[headerNames[i]].length
			});
		}
	}
	return result;
});

// TODO: Reimplement this for calibration
const selectedModelVariables = computed(() => [
	...selectedInitials.value,
	...selectedParameters.value
]);
defineExpose({ selectedModelVariables });

async function addModelConfiguration() {
	const response = await createModelConfiguration(
		props.modelConfigurations[0].modelId,
		`Config ${props.modelConfigurations.length + 1}`,
		'shawntest',
		editableModelConfigs.value[editableModelConfigs.value.length - 1].amrConfiguration
	);

	// FIXME: Not a good idea to update reactive variables through global storage
	openedWorkflowNodeStore.appendOutputPort({
		type: ModelOperation.outputs[0].type,
		label: `Config ${props.modelConfigurations.length + 1}`,
		value: response.id
	});
}

function addConfigValue() {
	extractions.value.push(`Untitled`);
}

const onCellEditComplete = (event) => {
	if (props.isEditable) {
		const { data, field } = event;
		cellValueToEdit.value = { data, field };
		updateModelConfigValue(event.newValue, event.index);
	}
};

function openValueModal(data: any, field: string) {
	if (props.isEditable) {
		openValueConfig.value = true;
		cellValueToEdit.value = { data, field };
	}
}

function updateModelConfigValue(
	newValue?: string,
	configIndex: number = cellValueToEdit.value.data[cellValueToEdit.value.field].configIndex
) {
	const { data, field } = cellValueToEdit.value;
	const { type, value, typeIndex } = data[field];

	const configToUpdate = editableModelConfigs.value[configIndex];

	if (field === 'name' && newValue) {
		configToUpdate.name = newValue;
	} else if (configToUpdate.amrConfiguration.semantics.ode[type][typeIndex].value) {
		configToUpdate.amrConfiguration.semantics.ode[type][typeIndex].value = value;
	} else if (configToUpdate.amrConfiguration.semantics.ode[type][typeIndex].expression) {
		configToUpdate.amrConfiguration.semantics.ode[type][typeIndex].expression = value;
	}

	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
}

function initializeConfigSpace() {
	const amr: Model = props.modelConfigurations[0].amrConfiguration;
	console.log(amr);

	editableModelConfigs.value = [];
	editableModelConfigs.value = cloneDeep(props.modelConfigurations);
	extractions.value = ['Default'];
	openValueConfig.value = false;
	cellValueToEdit.value = { data: {}, field: '' };
}

watch(
	() => props.modelConfigurations,
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
