<template>
	<DataTable
		v-if="configurations?.[0]?.initials && configurations?.[0]?.parameters"
		class="model-configuration"
		v-model:selection="selectedModelConfig"
		:value="modelConfigurationTable"
		editMode="cell"
		showGridlines
		scrollable
		@cell-edit-complete="onCellEditComplete"
	>
		<ColumnGroup type="header">
			<Row>
				<Column v-if="isEditable" header="" style="border: none" frozen />
				<Column header="" style="border: none" frozen />
				<Column
					v-for="(header, i) in Object.keys(configurations[0])"
					:header="capitalize(header)"
					:colspan="
						header === 'observables'
							? configurations[0][header].length + 1
							: configurations[0][header].length
					"
					:key="i"
				/>
				<Column
					v-if="!Object.keys(configurations[0]).includes('observables')"
					header="Observables"
					:colspan="observables.length + 1"
				/>
			</Row>
			<Row>
				<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" frozen />
				<Column :header="isEditable ? 'Select all' : ''" frozen />
				<Column
					v-for="(variableName, i) in configurations[0].rates"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configurations[0].initials"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configurations[0].parameters"
					:header="variableName.id"
					:key="i"
				/>
				<Column v-for="(variableName, i) in configurations[0].observables" :key="i">
					<template #header>
						<section class="editable-cell">
							<span>{{ variableName.id }}</span>
							<Button
								class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
								icon="pi pi-ellipsis-v"
								@click="($event) => showObservableHeaderMenu($event, i)"
							/>
							<Menu ref="observableHeaderMenu" :model="observableHeaderMenuItems" :popup="true" />
						</section>
					</template>
					<template #editor="{ data, field }">
						<InputText v-model="data[field]" autofocus />
					</template>
				</Column>
				<Column>
					<template #header>
						<Button
							class="p-button-sm p-button-outlined"
							label="Add"
							icon="pi pi-plus"
							@click="addObservable"
						/>
					</template>
				</Column>
			</Row>
			<!--  Add show in workflow later (very similar to "Select variables and parameters to calibrate") -->
		</ColumnGroup>
		<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" frozen />
		<Column field="name" frozen>
			<template #body="{ data, field }">
				{{ data[field] }}
			</template>
			<template #editor="{ data, field }">
				<InputText v-model="data[field]" autofocus />
			</template>
		</Column>
		<!--TODO: The slice here skips the name attribute, see about skipping it and rates in a clearer way-->
		<Column
			v-for="(value, i) of Object.keys(modelConfigurationTable[0]).slice(
				1,
				Object.keys(modelConfigurationTable[0]).length
			)"
			:key="i"
			:field="value"
		>
			<template #body="{ data, field }">
				<section class="editable-cell">
					<span>{{ data[field]?.value }}</span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="openValueModal(data, field)"
					/>
				</section>
			</template>
			<template #editor="{ data, field }">
				<InputText v-if="data[field].value" v-model="data[field].value" autofocus />
			</template>
		</Column>
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
	<!-- FIXME: what is this???
	<Button @click="createConfig" label="Create" />
	-->
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
								v-model="cellValueToEdit.data[cellValueToEdit.field].value"
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
import { cloneDeep, capitalize, isArray, isEmpty } from 'lodash';
import DataTable from 'primevue/datatable';
// import Checkbox from 'primevue/checkbox'; // Add back in later
import Menu from 'primevue/menu';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import InputText from 'primevue/inputtext';
import { ModelConfiguration } from '@/types/Types';
import { AnyValueMap } from '@/types/common';
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

// Selected columns
const selectedInitials = ref<string[]>([]);
const selectedParameters = ref<string[]>([]);

let chosenObservableIndex = 0;

const configurations = computed<any[]>(
	() => editableModelConfigs.value?.map((m) => m.configuration.semantics.ode) ?? []
);

const observables = ref<any[]>([]);

function addObservable() {
	for (let i = 0; i < editableModelConfigs.value.length; i++) {
		if (!editableModelConfigs.value[i].configuration.semantics.ode.observables) {
			editableModelConfigs.value[i].configuration.semantics.ode.observables = [];
		}

		editableModelConfigs.value[i].configuration.semantics.ode.observables.push({
			id: `obs${editableModelConfigs.value[0].configuration.semantics.ode.observables.length + 1}`,
			expression: 'expr'
		});

		updateModelConfiguration(editableModelConfigs.value[i]);
	}
}

const observableHeaderMenu = ref();
const showObservableHeaderMenu = (event, i) => {
	chosenObservableIndex = i;
	observableHeaderMenu.value[i].toggle(event);
};

const observableHeaderMenuItems = ref([
	{
		label: 'Edit name',
		command: () => {}
	},
	{
		label: 'Remove',
		command: () => {
			for (let i = 0; i < editableModelConfigs.value.length; i++) {
				editableModelConfigs.value[i].configuration.semantics.ode.observables.splice(
					chosenObservableIndex,
					1
				);
				updateModelConfiguration(editableModelConfigs.value[i]);
			}
		}
	}
]);

// TODO: Clean this up and use appropriate loops
const modelConfigurationTable = computed(() => {
	if (editableModelConfigs.value && !isEmpty(configurations.value)) {
		console.log('Configuration', configurations.value);

		const odes: object[] = [];

		for (let i = 0; i < configurations.value.length; i++) {
			if (configurations.value[i]) {
				odes.push({});
				// eslint-disable-next-line
				for (const key of Object.keys(configurations.value[i])) {
					odes[i][key] = [];

					configurations.value[i][key].forEach((value) => {
						const newPair = {};
						newPair[value.target ?? value.id] = value.expression ?? value.value;
						odes[i][key].push(newPair);
					});
				}
			}
		}
		// console.log(odes);

		const variables: AnyValueMap[] = [];
		// eslint-disable-next-line
		for (let i = 0; i < odes.length; i++) {
			variables[i] = {};
			// eslint-disable-next-line
			for (const [key, values] of Object.entries(odes[i])) {
				const flattenedObj = {};

				if (isArray(values)) {
					// @ts-ignore
					// eslint-disable-next-line
					for (let j = 0; j < values.length; j++) {
						const newKey: string = Object.keys(values[j])[0];
						const newVal = {};
						newVal[newKey] = {
							value: Object.values(values[j])[0],
							type: key,
							name: newKey,
							typeIndex: j,
							configIndex: i
						};
						Object.assign(flattenedObj, newVal);
					}
					// console.log(flattenedObj, values, key);
				}
				variables[i] = { ...variables[i], ...flattenedObj };
			}
		}

		// console.log(variables);

		return editableModelConfigs.value.map((modelConfig, i) => ({
			name: modelConfig.name,
			...variables[i]
		}));
	}
	return [];
});

// TODO: Reimplement this for calibration
const selectedModelVariables = computed(() => [
	...selectedInitials.value,
	...selectedParameters.value
]);
defineExpose({ selectedModelVariables });

async function addModelConfiguration() {
	const response = await createModelConfiguration(
		props.modelConfigurations[0].configuration.id, // model id
		`Config ${props.modelConfigurations.length + 1}`,
		'shawntest',
		editableModelConfigs.value[editableModelConfigs.value.length - 1].configuration
	);

	// FIXME: Not a good idea to update reactive variables through global storage
	openedWorkflowNodeStore.appendOutputPort({
		type: ModelOperation.outputs[0].type,
		label: `Config ${props.modelConfigurations.length + 1}`,
		value: response.id
	});
}

function addConfigValue() {
	extractions.value.push(`Resource ${extractions.value.length + 1} `);
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

	if (field === 'name' && newValue) {
		editableModelConfigs.value[configIndex].name = newValue;
	} else if (
		editableModelConfigs.value[configIndex].configuration.semantics.ode[type][typeIndex].value
	) {
		editableModelConfigs.value[configIndex].configuration.semantics.ode[type][typeIndex].value =
			value;
	} else if (
		editableModelConfigs.value[configIndex].configuration.semantics.ode[type][typeIndex].expression
	) {
		editableModelConfigs.value[configIndex].configuration.semantics.ode[type][
			typeIndex
		].expression = value;
	}

	updateModelConfiguration(editableModelConfigs.value[configIndex]);
	openValueConfig.value = false;
}

function initializeConfigSpace() {
	// console.log(props.modelConfigurations);
	editableModelConfigs.value = [];
	editableModelConfigs.value = cloneDeep(props.modelConfigurations);
	extractions.value = ['Resource 1'];
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
