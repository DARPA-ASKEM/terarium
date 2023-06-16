<template>
	<DataTable
		v-if="configuration.semantics?.ode && configuration.semantics?.ode"
		class="model-configuration"
		v-model:selection="selectedModelConfig"
		:value="modelConfigurationTable"
		editMode="cell"
		showGridlines
		@cell-edit-init="onCellEditStart"
		@cell-edit-complete="onCellEditComplete"
	>
		<ColumnGroup type="header">
			<Row>
				<Column v-if="isEditable" header="" style="border: none" />
				<Column header="" style="border: none" />
				<Column
					v-for="(header, i) in Object.keys(configuration.semantics.ode)"
					:header="capitalize(header)"
					:colspan="configuration.semantics.ode[header].length"
					:key="i"
				/>
			</Row>
			<Row>
				<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" />
				<Column :header="isEditable ? 'Select all' : ''" />
				<!-- Can't do a loop inside a loop within the datatable 
				<template v-for="header in Object.keys(model.semantics.ode)">
					<Column v-if="model.semantics?.ode" v-for="(variableName, i) in model.semantics.ode[header]"
						:header="variableName.target" :key="i" />
				</template> -->
				<Column
					v-for="(variableName, i) in configuration.semantics.ode.rates"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configuration.semantics.ode.initials"
					:header="variableName.target"
					:key="i"
				/>
				<Column
					v-for="(variableName, i) in configuration.semantics.ode.parameters"
					:header="variableName.id"
					:key="i"
				/>
			</Row>
			<!--  Add show in workflow later (very similar to "Select variables and parameters to calibrate") -->
		</ColumnGroup>
		<Column v-if="isEditable" selection-mode="multiple" headerStyle="width: 3rem" />
		<Column field="name">
			<template #body="{ data, field }">
				{{ data[field] }}
			</template>
			<template #editor="{ data, field }">
				<InputText v-model="data[field]" autofocus />
			</template>
		</Column>
		<Column
			v-for="(value, i) of configuration.semantics.ode.rates"
			:key="i"
			:field="value['target']"
		>
			<template #body="{ data, field }">
				{{ data[field]?.value }}
			</template>
			<template #editor="{ data, field }">
				{{ data[field]?.value }}
			</template>
		</Column>
		<Column
			v-for="(value, i) of configuration.semantics.ode.initials"
			:key="i"
			:field="value['target']"
		>
			<template #body="{ data, field }">
				{{ data[field]?.value }}
			</template>
			<template #editor="{ data, field }">
				{{ data[field]?.value }}
			</template>
		</Column>
		<Column
			v-for="(value, i) of configuration.semantics.ode.parameters"
			:key="i"
			:field="value['id']"
		>
			<template #body="{ data, field }">
				{{ data[field]?.value }}
			</template>
			<template #editor="{ data, field }">
				{{ data[field]?.value }}
			</template>
		</Column>
		<!-- <ColumnGroup v-if="calibrationConfig" type="footer">
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
	<Button @click="createConfig" label="Create" />
	<Teleport to="body">
		<tera-modal v-if="openValueConfig" @modal-mask-clicked="openValueConfig = false">
			<template #header>
				<h4>{{ cellValueToEdit.field }}</h4>
				<span>Select a value for this configuration</span>
			</template>
			<template #default>
				<TabView>
					<TabPanel v-for="(tab, i) in fakeExtractions" :key="tab" :header="tab">
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" v-model="fakeExtractions[i]" />
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
				<Button label="OK" @click="updateModelConfigValue" />
				<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { watch, ref, computed, onMounted } from 'vue';
import { cloneDeep, capitalize, isArray } from 'lodash';
import DataTable from 'primevue/datatable';
// import Checkbox from 'primevue/checkbox';
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

const props = defineProps<{
	isEditable: boolean;
	modelConfiguration: ModelConfiguration;
	calibrationConfig?: boolean;
}>();

const modelConfigNames = ref(['Config 1']);
const modelConfiguration = ref(cloneDeep(props.modelConfiguration));

const selectedModelConfig = ref();
const fakeExtractions = ref(['Resource 1', 'Resource 2', 'Resource 3']);

const openValueConfig = ref(false);
const cellValueToEdit = ref({ data: {}, field: '', index: 0 });

// const

// Selected columns
const selectedInitials = ref<string[]>([]);
const selectedParameters = ref<string[]>([]);

// const variableTypes = computed(() => props.model.semantics ? Object.keys(props.model.semantics.ode) : []);

const configuration = computed(() => modelConfiguration.value.configuration);

// TODO: Clean this up and use appropriate loops
const modelConfigurationTable = computed(() => {
	const odes = [{}];

	if (configuration.value.semantics?.ode) {
		// eslint-disable-next-line
		for (const key of Object.keys(configuration.value.semantics.ode)) {
			odes[0][key] = [];

			configuration.value.semantics?.ode[key].forEach((value) => {
				const newPair = {};
				newPair[value.target ?? value.id] = value.expression ?? value.value;
				odes[0][key].push(newPair);
			});
		}
	}

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
						index: j
					};
					Object.assign(flattenedObj, newVal);
				}
				console.log(flattenedObj, values, key);
			}
			variables[i] = { ...variables[i], ...flattenedObj };
		}
	}

	return modelConfigNames.value.map((name, i) => ({
		name,
		...variables[i]
	}));
});

const selectedModelVariables = computed(() => [
	...selectedInitials.value,
	...selectedParameters.value
]);
defineExpose({ selectedModelVariables });

// TODO: Make multiple configs work
function addModelConfiguration() {
	// modelConfigNames.value.push(`Config ${modelConfigNames.value.length + 1} `);
}

function addConfigValue() {
	fakeExtractions.value.push(`Resource ${fakeExtractions.value.length + 1} `);
}

const onCellEditComplete = (event) => {
	if (props.isEditable) {
		const { data, newValue, field } = event;

		switch (field) {
			case 'name':
				data[field] = newValue;
				break;
			default:
				break;
		}
	}
};

const onCellEditStart = (event) => {
	console.log(event);
	if (props.isEditable) {
		const { data, field, index } = event;

		if (field !== 'name') {
			openValueConfig.value = true;
			cellValueToEdit.value = { data, field, index };
		}
	}
};

function updateModelConfigValue() {
	const { data, field } = cellValueToEdit.value;
	const { type, value, index } = data[field];

	if (modelConfiguration.value.configuration.semantics.ode[type][index].value) {
		modelConfiguration.value.configuration.semantics.ode[type][index].value = value;
	}
	// Not sure if we want to edit these
	else if (modelConfiguration.value.configuration.semantics.ode[type][index].expression) {
		modelConfiguration.value.configuration.semantics.ode[type][index].expression = value;
	}

	// Casing fix needs work
	updateModelConfiguration(modelConfiguration.value);

	openValueConfig.value = false;
}

function resetDummyValues() {
	console.log(props.modelConfiguration);
	modelConfigNames.value = [props.modelConfiguration.name ?? 'Config 1'];
	fakeExtractions.value = ['Resource 1', 'Resource 2', 'Resource 3'];
	openValueConfig.value = false;
	cellValueToEdit.value = { data: {}, field: '', index: 0 };
}

watch(
	() => props.modelConfiguration,
	() => resetDummyValues(),
	{ deep: true }
);

// There should be a service function that grabs all the configs made for this model within this workflow???
// function getModelConfigurations() {

// }

async function createConfig() {
	const response = await createModelConfiguration(
		configuration.value.id,
		'Configg',
		'shawntest',
		configuration.value
	);

	console.log(response);
}

onMounted(() => {
	resetDummyValues();

	// createConfig();
	// getModelConfiguration();
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
