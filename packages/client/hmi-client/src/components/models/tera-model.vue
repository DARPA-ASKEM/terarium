<template>
	<tera-asset
		:name="name"
		:overline="model?.framework"
		:is-editable="isEditable"
		:is-creating-asset="assetId === ''"
		@close-preview="emit('close-preview')"
	>
		<template #nav>
			<tera-asset-nav
				:asset-content="modelContent"
				:show-header-links="modelView === ModelView.DESCRIPTION"
			>
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Description"
							icon="pi pi-list"
							@click="modelView = ModelView.DESCRIPTION"
							:active="modelView === ModelView.DESCRIPTION"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="Model"
							icon="pi pi-file"
							@click="modelView = ModelView.MODEL"
							:active="modelView === ModelView.MODEL"
						/>
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<template #name-input>
			<InputText v-model="newModelName" placeholder="Title of new model" />
		</template>
		<template #edit-buttons>
			<span class="p-input-icon-left">
				<i class="pi pi-search" />
				<InputText
					v-model="globalFilter['global'].value"
					class="p-inputtext-sm"
					placeholder="Search keyword"
				/>
			</span>
			<Button
				v-if="assetId === ''"
				@click="createNewModel"
				label="Create new model"
				class="p-button-sm"
			/>
			<Button
				v-else
				@click="launchForecast"
				label="Open simulation space"
				:disabled="isEditing"
				class="p-button-sm"
			/>
		</template>
		<template v-if="modelView === ModelView.DESCRIPTION">
			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4]">
				<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<p v-if="assetId !== ''" v-html="description" />
					<template v-else>
						<label for="placeholder" /><Textarea
							v-model="newDescription"
							rows="5"
							placeholder="Description of new model"
						/>
					</template>
				</AccordionTab>
				<AccordionTab>
					<template #header>
						<header id="Parameters">Parameters</header>
					</template>
					<DataTable class="p-datatable-sm" :value="amr?.model.parameters">
						<Column field="id" header="ID"></Column>
						<Column field="value" header="Value"></Column>
					</DataTable>
				</AccordionTab>
				<AccordionTab>
					<template #header>
						<header id="State variables">State variables</header>
					</template>
					<DataTable class="p-datatable-sm" :value="amr?.model.states">
						<Column field="id" header="ID"></Column>
						<Column field="name" header="Name"></Column>
						<Column field="grounding.context" header="Context"></Column>
						<Column field="grounding.identifiers" header="Identifiers"></Column>
					</DataTable>
				</AccordionTab>
				<AccordionTab>
					<template #header>
						<header id="Transitions">Transitions</header>
					</template>
					<DataTable class="p-datatable-sm" :value="amr?.model.transitions">
						<Column field="id" header="ID"></Column>
						<Column field="properties.name" header="Name"></Column>
						<Column field="input" header="Input"></Column>
						<Column field="output" header="Output"></Column>
						<!-- <Column field="properties.rate.expression" header="Expression"></Column> -->
						<Column field="properties.rate.expression_mathml" header="Equation">
							<template #body="slotProps">
								<katex-element :expression="slotProps.data.properties.rate?.expression" />
							</template>
						</Column>
					</DataTable>
				</AccordionTab>
				<AccordionTab>
					<template #header>
						<header id="Variable Statements">Variable Statements</header>
					</template>
					<DataTable paginator :rows="25" class="p-datatable-sm" :value="metaData">
						<Column field="id" header="ID"></Column>
						<Column field="variable.name" header="Variable"></Column>
						<Column field="value.value" header="Value"></Column>
						<Column header="Extraction Type">
							<template #body="slotProps">
								<Tag :value="getExtractionType(slotProps)" />
							</template>
						</Column>
						<Column header="Source">
							<template #body="slotProps">
								<div>{{ getSource(slotProps) }}</div>
							</template>
						</Column>
						<Column field="variable.equations ?? ''" header="Equations"></Column>
					</DataTable>
				</AccordionTab>
			</Accordion>
		</template>
		<template v-if="modelView === ModelView.MODEL">
			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4]">
				<AccordionTab header="Model diagram">
					<tera-model-diagram :asset-id="props.assetId" :is-editable="props.isEditable" />
				</AccordionTab>
				<AccordionTab v-if="model">
					<template #header> Model configurations </template>
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
								<Column header="" />
								<Column header="" />
								<Column header="Initial conditions" :colspan="model.content.S.length" />
								<Column header="Parameters" :colspan="paramLength" />
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
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
					<DataTable :value="relatedTerariumModels">
						<Column field="name" header="Models"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDatasets">
						<Column field="name" header="Datasets"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDocuments">
						<Column field="name" header="Documents"></Column>
					</DataTable>
				</AccordionTab>
			</Accordion>
		</template>
		<Teleport to="body">
			<ForecastLauncher
				v-if="showForecastLauncher && model"
				:model="model"
				@close="showForecastLauncher = false"
				@launch-forecast="goToSimulationRunPage"
			/>
		</Teleport>
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
	</tera-asset>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { watch, ref, computed, onUpdated, PropType, ComputedRef } from 'vue';
import { PetriNet } from '@/petrinet/petrinet-service';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import { createModel, addModelToProject, getModel } from '@/services/model';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Accordion from 'primevue/accordion';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import * as textUtil from '@/utils/text';
import ForecastLauncher from '@/components/models/tera-forecast-launcher.vue';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { ITypedModel, Model } from '@/types/Model';
import { AskemModelRepresentationType } from '@/types/AskemModelRepresentation';
import { ResultType } from '@/types/common';
import { Document, Dataset, ProvenanceType } from '@/types/Types';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { FilterMatchMode } from 'primevue/api';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import { getRelatedArtifacts } from '@/services/provenance';
import TeraModelDiagram from './tera-model-diagram.vue';

interface StringValueMap {
	[key: string]: string;
}

enum ModelView {
	DESCRIPTION = 'description',
	MODEL = 'model'
}

const modelContent = computed(() => [
	{ key: 'Description', value: description },
	{ key: 'Intended Use', value: null },
	{ key: 'Training Data', value: null },
	{ key: 'Evaluation Data', value: null },
	{ key: 'Metrics', value: null },
	{ key: 'Training', value: null },
	{ key: 'Model Output', value: null },
	{ key: 'Ethical Considerations', value: null },
	{ key: 'Authors and Contributors', value: null },
	{ key: 'License', value: null },
	{ key: 'Parameters', value: amr.value?.model.parameters },
	{ key: 'State variables', value: amr.value?.model.states },
	{ key: 'Transitions', value: amr.value?.model.transitions },
	{ key: 'Variable Statements', value: amr.value?.metadata?.variable_statements }
]);

const modelView = ref(ModelView.DESCRIPTION);

// Get rid of these emits
const emit = defineEmits(['update-tab-name', 'close-preview', 'asset-loaded', 'close-current-tab']);

const props = defineProps({
	project: {
		type: Object as PropType<IProject> | null,
		default: null,
		required: false
	},
	assetId: {
		type: String,
		required: true
	},
	isEditable: {
		type: Boolean,
		required: true
	},
	highlight: {
		type: String,
		default: '',
		required: false
	}
});

const resources = useResourcesStore();
const router = useRouter();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

const model = ref<ITypedModel<PetriNet> | null>(null);
const amr = ref<AskemModelRepresentationType | null>(null);

const isEditing = ref<boolean>(false);
const isEditingEQ = ref<boolean>(false);

const newModelName = ref('New Model');
const newDescription = ref<string | undefined>('');
const newPetri = ref();

const isMathMLValid = ref<boolean>(true);

const showForecastLauncher = ref(false);

const modelConfigNames = ref([{ name: 'Config 1' }]);
const fakeExtractions = ref(['Resource 1', 'Resource 2', 'Resource 3']);

const initialValues = ref<StringValueMap[]>([{}]);
const parameterValues = ref<StringValueMap[]>([{}]);
const openValueConfig = ref(false);
const cellValueToEdit = ref({ data: {}, field: '', index: 0 });

const selectedModelConfig = ref();
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

const paramLength = computed(() => {
	if (amr.value) {
		return amr.value.model.parameters.length;
	}
	return model.value?.content.T.length;
});

const modelStates: ComputedRef<Array<{ name: string }> | undefined> = computed(() => {
	if (amr.value) {
		return amr.value.model.states.map((state) => ({ name: state.id }));
	}
	return model.value?.content.S.map((state) => ({ name: state.sname }));
});

const modelTransitions: ComputedRef<Array<{ name: string }> | undefined> = computed(() => {
	if (amr.value) {
		return amr.value.model.transitions.map((transitions) => ({ name: transitions.id }));
	}
	return model.value?.content.T.map((state) => ({ name: state.tname }));
});

const metaData = computed(() => {
	if (amr.value) {
		// const extractions = amr.value.metadata.variable_statements.map((staments) => {
		// 	return staments.id;
		// });
		return amr.value.metadata.variable_statements;
	}
	return null;
});

// const modelParameters = computed(() =>{
// 	if (amr.value){
// 		return amr.value.model.parameters;
// 	}
// 	return model.value?.content.parameters;
// })

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
		model.value &&
		openedWorkflowNodeStore.assetId === model.value.id.toString() &&
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
	else if (model.value) {
		model.value?.content.S.forEach((s) => {
			initialValues.value[0][s.sname] = `${1}`;
		});

		model.value?.content.T.forEach((s) => {
			parameterValues.value[0][s.tname] = `${0.0005}`;
		});
	}
}

watch(
	() => [openedWorkflowNodeStore.initialValues, openedWorkflowNodeStore.parameterValues],
	() => {
		generateModelConfigValues();
	},
	{ deep: true }
);

watch(
	() => model.value,
	async () => {
		// Reset model config on model change
		modelConfigNames.value = [{ name: 'Config 1' }];
		fakeExtractions.value = ['Resource 1', 'Resource 2', 'Resource 3'];
		initialValues.value = [{}];
		parameterValues.value = [{}];
		openValueConfig.value = false;
		cellValueToEdit.value = { data: {}, field: '', index: 0 };
		generateModelConfigValues();
	}
);

const globalFilter = ref({
	// @ts-ignore
	// eslint-disable-line
	global: { value: '', matchMode: FilterMatchMode.CONTAINS }
});

// States/transitions aren't selected like this anymore - maybe somehow later?
// const onStateVariableClick = () => {
// 	if (selectedRow.value) {
// 		equationLatex.value = equationLatexOriginal.value.replaceAll(
// 			selectedRow.value.sname,
// 			String.raw`{\color{red}${selectedRow.value.sname}}`
// 		);
// 	} else {@vnode-mounted
// 		equationLatex.value = equationLatexOriginal.value;
// 	}
// };

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}
const fetchRelatedTerariumArtifacts = async () => {
	if (model.value) {
		const results = await getRelatedArtifacts(props.assetId, ProvenanceType.ModelRevision);
		relatedTerariumArtifacts.value = results;
	} else {
		relatedTerariumArtifacts.value = [];
	}
};

watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			const result = await getModel(props.assetId);
			model.value = result;
			fetchRelatedTerariumArtifacts();
		} else {
			model.value = null;
		}
	},
	{ immediate: true }
);

watch(
	() => newModelName.value,
	(newValue, oldValue) => {
		if (newValue !== oldValue) {
			emit('update-tab-name', newValue);
		}
	}
);

onUpdated(() => {
	if (model.value) {
		emit('asset-loaded');
	}
});

const launchForecast = () => {
	showForecastLauncher.value = true;
};

const createNewModel = async () => {
	if (props.project) {
		const newModel = {
			name: newModelName.value,
			framework: 'Petri Net',
			description: newDescription.value,
			content: JSON.stringify(newPetri.value ?? { S: [], T: [], I: [], O: [] })
		};
		const newModelResp = await createModel(newModel);
		if (newModelResp) {
			const modelId = newModelResp.id.toString();
			emit('close-current-tab');
			await addModelToProject(props.project.id, modelId, resources);

			// Go to the model you just created
			router.push({
				name: RouteName.ProjectRoute,
				params: {
					assetName: newModelName.value,
					assetId: modelId,
					pageType: ProjectAssetTypes.MODELS
				}
			});
		}
		isEditingEQ.value = false;
		isMathMLValid.value = true;
	}
};

const goToSimulationRunPage = () => {
	showForecastLauncher.value = false;
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetId: model.value?.id ?? 0 + 1000,
			assetName: highlightSearchTerms(model.value?.name ?? ''),
			pageType: 'simulation_runs'
		}
	});
};

const name = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => {
	if (amr.value) {
		return highlightSearchTerms(amr.value?.description);
	}
	return highlightSearchTerms(model.value?.description);
});

function getExtractionType(sp) {
	if (sp.data.variable.column.length > 0) {
		return 'DataSet';
	}
	return 'Document';
}

function getSource(sp) {
	if (sp.data.variable.column.length > 0) {
		return sp.data.variable.column[0].dataset.name;
	}
	return sp.data.variable.paper.name;
}
</script>

<style scoped>
.p-toolbar {
	position: absolute;
	width: 100%;
	z-index: 1;
	isolation: isolate;
	background: transparent;
	padding: 0.5rem;
}

.p-button.p-component.p-button-sm.p-button-outlined.toolbar-button {
	background-color: var(--surface-0);
	margin: 0.25rem;
}

.toolbar-button-saveModel {
	margin: 0.25rem;
}

.toolbar-subgroup {
	display: flex;
}

section math-editor {
	justify-content: center;
}

.floating-edit-button {
	background-color: var(--surface-0);
	margin-top: 10px;
	position: absolute;
	right: 10px;
	z-index: 10;
}

.splitter-container {
	height: 100%;
}

.graph-element {
	background-color: var(--surface-secondary);
	height: 100%;
	max-height: 100%;
	flex-grow: 1;
	overflow: hidden;
	border: none;
	position: relative;
}

.math-editor-container {
	display: flex;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: 0px var(--border-radius) var(--border-radius) 0px;
	overflow: auto;
}

.math-editor-selected {
	border: 4px solid var(--primary-color);
}

.math-editor-error {
	border: 4px solid var(--surface-border-warning);
	transition: outline 0.3s ease-in-out, color 0.3s ease-in-out, opacity 0.3s ease-in-out;
}

.model_diagram {
	display: flex;
	height: 100%;
}

.p-splitter {
	height: 100%;
}

.p-datatable:deep(td:hover) {
	background-color: var(--surface-secondary);
	cursor: pointer;
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

.tera-split-panel {
	position: relative;
	height: 100%;
	display: flex;
	align-items: center;
	width: 100%;
}

.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

.model-configuration {
	margin-bottom: 1rem;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}
</style>
