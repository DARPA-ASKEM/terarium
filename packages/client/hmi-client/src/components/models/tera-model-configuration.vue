<template>
	<DataTable
		class="model-configuration"
		v-model:selection="selectedModelConfig"
		:value="modelConfiguration"
		editMode="cell"
		showGridlines
		@cell-edit-init="onCellEditStart"
		@cell-edit-complete="onCellEditComplete"
	>
		<ColumnGroup type="header">
			<!--Style top rows-->
			<Row>
				<Column header="" style="border: none" />
				<Column header="" style="border: none" />
				<Column header="Initial conditions" :colspan="modelStates.length" />
				<Column header="Parameters" :colspan="modelTransitions.length" />
				<!-- <Column header="Observables" /> -->
			</Row>
			<Row>
				<Column selection-mode="multiple" headerStyle="width: 3rem" />
				<Column header="Select all" />
				<Column v-for="(s, i) of modelStates" :key="i" :header="s.name" />
				<Column v-for="(t, i) of modelTransitions" :key="i" :header="t.name" />
			</Row>
			<!-- <Row> Add show in workflow later
							<Column header="Show in workflow" />
							<Column v-for="(s, i) of model.content.S" :key="i">
								<template #header>
									<Checkbox :binary="true" />
								</template>
							</Column>
							<Column v-for="(t, i) of model.content.T" :key="i">
								<template #header>
									<Checkbox :binary="true" />
								</template>
							</Column>
						</Row> -->
		</ColumnGroup>
		<Column selection-mode="multiple" headerStyle="width: 3rem" />
		<Column field="name">
			<template #body="{ data, field }">
				{{ data[field] }}
			</template>
			<template #editor="{ data, field }">
				<InputText v-model="data[field]" autofocus />
			</template>
		</Column>
		<Column
			v-for="(value, i) of [...model.content.S, ...model.content.T]"
			:key="i"
			:field="value['sname'] ?? value['tname']"
		>
			<template #body="{ data, field }">
				{{ data[field] }}
			</template>
			<template #editor="{ data, field }">
				{{ data[field] }}
			</template>
		</Column>
	</DataTable>
	<Button
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
				<Button label="OK" @click="updateModelConfigValue" />
				<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { watch, ref, computed, onMounted, ComputedRef } from 'vue';
import { cloneDeep } from 'lodash';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import InputText from 'primevue/inputtext';
import { Model } from '@/types/Model';
import { AskemModelRepresentationType } from '@/types/AskemModelRepresentation';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';

interface StringValueMap {
	[key: string]: string;
}

const props = defineProps<{
	model: Model;
	amr?: AskemModelRepresentationType | null;
}>();

const selectedModelConfig = ref();
const modelConfigNames = ref([{ name: 'Config 1' }]);
const fakeExtractions = ref(['Resource 1', 'Resource 2', 'Resource 3']);

const initialValues = ref<StringValueMap[]>([{}]);
const parameterValues = ref<StringValueMap[]>([{}]);
const openValueConfig = ref(false);
const cellValueToEdit = ref({ data: {}, field: '', index: 0 });

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const modelConfiguration = computed(() => {
	const newModelConfiguration: any[] = [];

	for (let i = 0; i < modelConfigNames.value.length; i++) {
		newModelConfiguration.push({
			name: modelConfigNames.value[i].name,
			...initialValues.value[i],
			...parameterValues.value[i]
		});
	}

	return newModelConfiguration;
});

// TODO: The modelStates/Transitions don't update properly when switching from an amr (Bucky) to a normal model that is already opened
const modelStates: ComputedRef<Array<{ name: string }>> = computed(() => {
	if (props.amr) {
		return props.amr.model.states.map((s) => ({ name: s.id }));
	}
	return props.model.content.S.map((s) => ({ name: s.sname }));
});

const modelTransitions: ComputedRef<Array<{ name: string }>> = computed(() => {
	if (props.amr) {
		return props.amr.model.transitions.map((t) => ({ name: t.id }));
	}
	return props.model.content.T.map((t) => ({ name: t.tname }));
});

function addModelConfiguration() {
	modelConfigNames.value.push({ name: `Config ${modelConfigNames.value.length + 1}` });
	initialValues.value.push(cloneDeep(initialValues.value[initialValues.value.length - 1]));
	parameterValues.value.push(cloneDeep(parameterValues.value[parameterValues.value.length - 1]));
}

function addConfigValue() {
	fakeExtractions.value.push(`Resource ${fakeExtractions.value.length + 1}`);
}

const onCellEditComplete = (event) => {
	const { data, newValue, field } = event;

	switch (field) {
		case 'name':
			data[field] = newValue;
			break;
		default:
			break;
	}
};

const onCellEditStart = (event) => {
	const { data, field, index } = event;

	if (field !== 'name') {
		openValueConfig.value = true;
		cellValueToEdit.value = { data, field, index };
	}
};

function updateModelConfigValue() {
	const { data, field, index } = cellValueToEdit.value;

	if (initialValues.value[index][field]) {
		initialValues.value[index][field] = data[field];
	} else if (parameterValues.value[index][field]) {
		parameterValues.value[index][field] = data[field];
	}

	openValueConfig.value = false;
}

function generateModelConfigValues() {
	// Sync with workflow
	if (
		props.model &&
		openedWorkflowNodeStore.assetId === props.model.id.toString() &&
		openedWorkflowNodeStore.initialValues !== null &&
		openedWorkflowNodeStore.parameterValues !== null
	) {
		// Shallow copy
		initialValues.value = openedWorkflowNodeStore.initialValues as any; // Not sure why the typing doesn't match
		parameterValues.value = openedWorkflowNodeStore.parameterValues as any;

		if (modelConfigNames.value.length < initialValues.value.length - 1) {
			modelConfigNames.value.push({ name: `Config ${modelConfigNames.value.length + 1}` });
		}
	}
	// Default values
	else if (props.model) {
		props.model?.content.S.forEach((s) => {
			initialValues.value[0][s.sname] = `${1}`;
		});

		props.model?.content.T.forEach((s) => {
			parameterValues.value[0][s.tname] = `${0.0005}`;
		});
	}
}

function resetModelConfiguration() {
	modelConfigNames.value = [{ name: 'Config 1' }];
	fakeExtractions.value = ['Resource 1', 'Resource 2', 'Resource 3'];
	initialValues.value = [{}];
	parameterValues.value = [{}];
	openValueConfig.value = false;
	cellValueToEdit.value = { data: {}, field: '', index: 0 };
	generateModelConfigValues();
}

watch(
	() => [openedWorkflowNodeStore.initialValues, openedWorkflowNodeStore.parameterValues],
	() => {
		generateModelConfigValues();
	},
	{ deep: true }
);

watch(
	() => props.model,
	() => resetModelConfiguration(),
	{ deep: true }
);

onMounted(() => resetModelConfiguration());
</script>

<style scoped>
.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

.model-configuration {
	margin-bottom: 1rem;
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
