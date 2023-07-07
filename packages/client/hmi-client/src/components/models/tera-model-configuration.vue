<template>
	<!--TODO: Once we implement the unique border design remove the p-datatable-gridlines class and make a custom series of classes to support the borders we want-->
	<div
		v-if="!isEmpty(configurations) && !isEmpty(tableHeaders) && !isEmpty(cellEditStates)"
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
						<th
							v-for="({ target }, i) in configurations[0]?.semantics?.ode.rates"
							:header="target"
							:key="i"
						>
							{{ target }}
						</th>
						<th
							v-for="({ target }, i) in configurations[0]?.semantics?.ode.initials"
							:header="target"
							:key="i"
						>
							{{ target }}
						</th>
						<th
							v-for="({ id }, i) in configurations[0]?.semantics?.ode.parameters"
							:header="id"
							:key="i"
						>
							{{ id }}
						</th>
						<th
							v-for="({ id }, i) in configurations[0]?.semantics?.ode.observables"
							:header="id"
							:key="i"
						>
							<section class="editable-cell">
								<span>{{ id }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click="($event) => showObservableHeaderMenu($event, i)"
								/>
								<Menu ref="observableHeaderMenu" :model="observableHeaderMenuItems" :popup="true" />
								<!-- Changes observable name for all configs?? -->
								<!-- <InputText v-model.lazy="modelConfigs[i].configuration.semantics.ode.observables[j].id" v-focus /> -->
							</section>
						</th>
						<th>
							<Button
								class="p-button-sm p-button-outlined"
								label="Add"
								icon="pi pi-plus"
								@click="addObservable"
							/>
						</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="({ configuration, name }, i) in modelConfigs" :key="i">
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
						<td class="p-frozen-column second-frozen">
							<span v-if="!cellEditStates[i].name" @click="cellEditStates[i].name = true">
								{{ name }}
							</span>
							<InputText
								v-else
								v-model.lazy="modelConfigs[i].name"
								v-focus
								@focusout="cellEditStates[i].name = false"
								@keyup.enter="
									cellEditStates[i].name = false;
									updateModelConfigValue(i);
								"
							/>
						</td>
						<td
							v-for="(rate, j) of configuration?.semantics?.ode.rates"
							class="p-editable-column"
							:key="j"
							@click="cellEditStates[i].rates[j] = true"
						>
							<section v-if="!cellEditStates[i].rates[j]" class="editable-cell">
								<span>{{ rate.expression }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click.stop="openValueModal('rates', 'expression', i, j)"
								/>
							</section>
							<InputText
								v-else
								v-model.lazy="modelConfigs[i].configuration.semantics.ode.rates[j].expression"
								v-focus
								@focusout="cellEditStates[i].rates[j] = false"
								@keyup.enter="
									cellEditStates[i].rates[j] = false;
									updateModelConfigValue(i);
								"
							/>
						</td>
						<td
							v-for="(initial, j) of configuration?.semantics?.ode.initials"
							:key="j"
							@click="cellEditStates[i].initials[j] = true"
						>
							<section v-if="!cellEditStates[i].initials[j]" class="editable-cell">
								<span>{{ initial.expression }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click.stop="openValueModal('initials', 'expression', i, j)"
								/>
							</section>
							<InputText
								v-else
								v-model.lazy="modelConfigs[i].configuration.semantics.ode.initials[j].expression"
								v-focus
								@focusout="cellEditStates[i].initials[j] = false"
								@keyup.enter="
									cellEditStates[i].initials[j] = false;
									updateModelConfigValue(i);
								"
							/>
						</td>
						<td
							v-for="(parameter, j) of configuration?.semantics?.ode.parameters"
							:key="j"
							@click="cellEditStates[i].parameters[j] = true"
						>
							<section v-if="!cellEditStates[i].parameters[j]" class="editable-cell">
								<span>{{ parameter.value }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click.stop="openValueModal('parameters', 'value', i, j)"
								/>
							</section>
							<InputText
								v-else
								v-model.lazy="modelConfigs[i].configuration.semantics.ode.parameters[j].value"
								v-focus
								@focusout="cellEditStates[i].parameters[j] = false"
								@keyup.enter="
									cellEditStates[i].parameters[j] = false;
									updateModelConfigValue(i);
								"
							/>
						</td>
						<td
							v-for="(observable, j) of configuration?.semantics?.ode.observables"
							:key="j"
							@click="cellEditStates[i].observables[j] = true"
						>
							<section v-if="!cellEditStates[i].observables[j]" class="editable-cell">
								<span>{{ observable.expression }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
									@click.stop="openValueModal('observables', 'expression', i, j)"
								/>
							</section>
							<InputText
								v-else
								v-model.lazy="modelConfigs[i].configuration.semantics.ode.observables[j].expression"
								v-focus
								@focusout="cellEditStates[i].observables[j] = false"
								@keyup.enter="
									cellEditStates[i].observables[j] = false;
									updateModelConfigValue(i);
								"
							/>
						</td>
						<!--TODO: Insert new td loops for time and observables here-->
					</tr>
				</tbody>
			</table>
		</div>
	</div>
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
import { isEmpty, cloneDeep } from 'lodash';
// import Checkbox from 'primevue/checkbox'; // Add back in later
// import Menu from 'primevue/menu';
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
const cellEditStates = ref<any[]>([]);

// const selectedModelConfig = ref();
const extractions = ref<any[]>([]);

const openValueConfig = ref(false);
const modalVal = ref({ odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 });

// Selected columns - TODO: add in for filtering calibration dropdowns
const selectedInitials = ref<string[]>([]);
const selectedParameters = ref<string[]>([]);

let chosenObservableIndex = 0;

const configurations = computed<Model[]>(
	() => modelConfigs.value?.map((m) => m.configuration) ?? []
);

// Now it adds the observable to all the configurations...
function addObservable() {
	for (let i = 0; i < modelConfigs.value.length; i++) {
		if (!modelConfigs.value[i].configuration.semantics.ode.observables) {
			modelConfigs.value[i].configuration.semantics.ode.observables = [];
		}

		modelConfigs.value[i].configuration.semantics.ode.observables.push({
			id: `noninf`,
			name: `Non-infectious`,
			states: ['S', 'R'],
			expression: 'S+R',
			expression_mathml: '<apply><plus/><ci>S</ci><ci>R</ci></apply>'
		});

		updateModelConfiguration(modelConfigs.value[i]);
	}
}

const observableHeaderMenu = ref();
const showObservableHeaderMenu = (event, i) => {
	chosenObservableIndex = i;
	console.log(observableHeaderMenu.value[i]);
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
			for (let i = 0; i < modelConfigs.value.length; i++) {
				modelConfigs.value[i].configuration.semantics.ode.observables.splice(
					chosenObservableIndex,
					1
				);
				updateModelConfiguration(modelConfigs.value[i]);
			}
		}
	}
]);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => {
	if (configurations.value?.[0]?.semantics) {
		const headerNames = Object.keys(configurations.value[0]?.semantics.ode) ?? [];
		const result: { name: string; colspan: number }[] = [];

		for (let i = 0; i < headerNames.length; i++) {
			if (configurations.value?.[0]?.semantics?.ode[headerNames[i]]) {
				const colspan = configurations.value?.[0]?.semantics?.ode[headerNames[i]].length;

				result.push({
					name: headerNames[i],
					colspan: headerNames[i] === 'observables' ? colspan + 1 : colspan // + 1 to fit Add button
				});
			}
		}
		// Just so user can add an observable when there are none by default
		if (!headerNames.includes('observables')) result.push({ name: 'observables', colspan: 1 });

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
	const configurationToAdd = modelConfigs.value.length
		? modelConfigs.value[0].configuration
		: props.model;

	const response = await createModelConfiguration(
		props.model.id,
		`Config ${modelConfigs.value.length + 1}`,
		'Test',
		configurationToAdd
	);
	console.log(response);

	// TODO: notify change
}

function addConfigValue() {
	extractions.value.push(`Untitled`);
}

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

function updateModelConfigValue(configIndex: number = modalVal.value.configIndex) {
	const configToUpdate = modelConfigs.value[configIndex];
	console.log(configToUpdate);
	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
}

async function initializeConfigSpace() {
	modelConfigs.value = [];
	modelConfigs.value = (await getModelConfigurations(props.model.id)) as ModelConfiguration[];

	console.log('Configs', modelConfigs.value);

	extractions.value = ['Default'];
	openValueConfig.value = false;
	modalVal.value = { odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 };
}

function resetCellEditing() {
	const row = { name: false };

	for (let i = 0; i < tableHeaders.value.length; i++) {
		const { name, colspan } = tableHeaders.value[i];
		row[name] = Array(colspan).fill(false);
	}

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigs.value.length);
	for (let i = 0; i < modelConfigs.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

watch(
	() => tableHeaders.value,
	() => {
		resetCellEditing();
	}
);

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

.p-frozen-column {
	left: 0px;
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
