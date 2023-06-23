<template>
	<tera-asset
		:name="name"
		:is-editable="isEditable"
		:is-creating-asset="assetId === ''"
		:stretch-content="modelView === ModelView.MODEL"
		@close-preview="emit('close-preview')"
	>
		<template #name-input>
			<InputText v-model="newModelName" placeholder="Title of new model" />
		</template>
		<template #edit-buttons>
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
			<Button
				v-if="assetId === ''"
				@click="createNewModel"
				label="Create new model"
				class="p-button-sm"
			/>
		</template>
		<template v-if="modelView === ModelView.DESCRIPTION">
			<div class="container">
				<Message class="inline-message" icon="none"
					>This page describes the model. Use the content switcher above to see the diagram and
					manage configurations.</Message
				>
			</div>
			<table class="model-biblio">
				<tr>
					<th>Framework</th>
					<th>Model Version</th>
					<th>Date Created</th>
					<th>Created By</th>
					<th>Source</th>
				</tr>
				<tr>
					<td>{{ model?.framework ?? 'PetriNet' }}</td>
					<td>{{ model?.model_version }}</td>
					<td>{{ model?.metadata.processed_at }}</td>
					<td>{{ model?.description }}</td>
					<td>{{ model?.metadata.processed_by }}</td>
				</tr>
			</table>
			<RelatedPublications />
			<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6]">
				<!-- Description -->
				<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<p v-if="assetId !== ''" v-html="description" />
					<template v-else>
						<label for="placeholder" />
						<Textarea v-model="newDescription" rows="5" placeholder="Description of new model" />
					</template>
				</AccordionTab>

				<!-- Parameters -->
				<AccordionTab>
					<template #header>
						<header id="Parameters">
							Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
						</header>
					</template>
					<DataTable
						class="p-datatable-sm"
						:value="parameters"
						rowGroupMode="subheader"
						groupRowsBy="description"
					>
						<Column field="id" header="ID" />
						<Column field="name" header="Name" />
						<Column field="units" header="Units" />
						<Column field="concept" header="Concept" />
						<Column field="extractions" header="Extractions" />
						<template #groupfooter="parameters">
							<div>
								<span class="parameter-description">{{ parameters.data.description }}</span>
							</div>
						</template>
					</DataTable>
				</AccordionTab>

				<!-- State variables -->
				<AccordionTab>
					<template #header>
						<header id="State variables">
							State variables<span class="artifact-amount">({{ states.length }})</span>
						</header>
					</template>
					<DataTable
						class="p-datatable-sm"
						:value="states"
						rowGroupMode="subheader"
						groupRowsBy="description"
					>
						<Column field="id" header="ID" />
						<Column field="name" header="Name" />
						<Column field="grounding.context" header="Concept"></Column>
						<Column field="grounding.identifiers" header="Identifiers"></Column>
						<template #groupfooter="states">
							<div>
								<span class="parameter-description">{{ states.data.description }}</span>
							</div>
						</template>
					</DataTable>
				</AccordionTab>

				<!-- Observables -->
				<AccordionTab>
					<template #header>
						<header>Observables</header>
					</template>
				</AccordionTab>

				<!-- Transitions -->
				<AccordionTab>
					<template #header>
						<header id="Transitions">
							Transitions<span class="artifact-amount"
								>({{ model?.model.transitions.length }})</span
							>
						</header>
					</template>
					<DataTable class="p-datatable-sm" :value="model?.model.transitions">
						<Column field="properties.name" header="Label" />
						<Column field="input" header="Input"
							><template #body="slotProps">{{
								slotProps.data.input.sort().join(', ')
							}}</template></Column
						>
						<Column field="output" header="Output"
							><template #body="slotProps">{{
								slotProps.data.output.sort().join(', ')
							}}</template></Column
						>
						<Column field="properties.rate.expression_mathml" header="Expression">
							<template #body="slotProps">
								<katex-element :expression="getTransitionExpression(slotProps.data.id)" />
							</template>
						</Column>
					</DataTable>
				</AccordionTab>

				<!-- Other extractions -->
				<AccordionTab>
					<template #header>
						<header id="Other extractions">
							Other extractions<span class="artifact-amount">({{ 'unknown' }})</span>
						</header>
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
			<Accordion multiple :active-index="[0, 1, 2, 3, 4]">
				<AccordionTab header="Model diagram">
					<tera-model-diagram
						:model="model"
						:is-editable="props.isEditable"
						@update-model-content="updateModelContent"
					/>
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
								<Column header="" style="border: none" />
								<Column header="" style="border: none" />
								<Column header="Initial conditions" :colspan="model.model.states.length" />
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
							v-for="(value, i) of [...model.model.states, ...model.model.transitions]"
							:key="i"
							:field="value['name'] ?? value['name']"
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
			<tera-modal v-if="openValueConfig" @modal-mask-clicked="openValueConfig = false">
				<template #header>
					<header>Related publications</header>
				</template>
				<p>
					Terarium can extract information from papers and other resources to describe this model.
				</p>
				<Button label="Add resources to describe this model" link icon="pi pi-plus" />
			</tera-modal>
		</Teleport>
	</tera-asset>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { watch, ref, computed, onUpdated, PropType } from 'vue';
import { useRouter } from 'vue-router';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import Tag from 'primevue/tag';
import Textarea from 'primevue/textarea';
import TeraAsset from '@/components/asset/tera-asset.vue';
import RelatedPublications from '@/components/widgets/tera-related-publications.vue';
import { parseIGraph2PetriNet } from '@/petrinet/petrinet-service';
import { RouteName } from '@/router/routes';
import { createModel, addModelToProject, getModel } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getRelatedArtifacts } from '@/services/provenance';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import useResourcesStore from '@/stores/resources';
import { ResultType } from '@/types/common';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { Model, Document, Dataset, ProvenanceType, ModelConfiguration } from '@/types/Types';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import * as textUtil from '@/utils/text';
import TeraModelDiagram from './tera-model-diagram.vue';

enum ModelView {
	DESCRIPTION = 'description',
	MODEL = 'model'
}

// TODO - Get rid of these emits
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

const openValueConfig = ref(false);
const modelView = ref(ModelView.DESCRIPTION);
const resources = useResourcesStore();
const router = useRouter();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

const model = ref<Model | null>(null);

const modelConfigurations = ref<ModelConfiguration[]>([]);

const newModelName = ref('New Model');
const newDescription = ref<string | undefined>('');
const newPetri = ref();

const metaData = computed(() => model.value?.metadata?.variable_statements);

const name = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => highlightSearchTerms(model.value?.description));
const parameters = computed(() => model.value?.semantics?.ode.parameters ?? []);
const states = computed(() => model.value?.model?.states ?? []);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

// Get the mathematical expression of a transition
function getTransitionExpression(id): string {
	return model?.value?.semantics?.ode.rates.find((rate) => rate.target === id)?.expression ?? '';
}

// States/transitions aren't selected like this anymore - maybe somehow later?
// const onStateVariableClick = () => {
// 	if (selectedRow.value) {
// 		equationLatex.value = equationLatexOriginal.value.replaceAll(
// 			selectedRow.value.sname,
// 			String.raw`{\color{red}${selectedRow.value.sname}}`
// 		);
// 	} else {
// 		equationLatex.value = equationLatexOriginal.value;
// 	}
// };

function updateModelContent(rendererGraph) {
	if (model.value) model.value.model = parseIGraph2PetriNet(rendererGraph);
}

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

async function getModelConfigurations() {
	if (openedWorkflowNodeStore.node) {
		const modelConfigIds = openedWorkflowNodeStore.node.outputs;
		modelConfigurations.value = [];

		// FIXME: If you keep the drilldown open while switching from one model node to the next you'll see a duplicate of the previous row
		// It's a duplicate of a config that belongs to that node as they both have the same config id
		// Also this function seems to run twice and a bunch of petrinet service errors show up (when you switch nodes and drilldown is open)
		// console.log(openedWorkflowNodeStore.node.outputs)

		if (modelConfigIds) {
			for (let i = 0; i < modelConfigIds.length; i++) {
				const modelConfigId = modelConfigIds[i].value?.[0];
				// Don't need to eslint-disable no await in for loop once we are able to pass in a list of ids
				// eslint-disable-next-line
				const response = await getModelConfigurationById(modelConfigId);
				modelConfigurations.value.push(response);
			}
			if (modelConfigurations.value) {
				model.value = await getModel(modelConfigurations.value[0].configuration.id);
				fetchRelatedTerariumArtifacts();
			}
		}
	}
}

watch(
	() => [openedWorkflowNodeStore.node?.outputs],
	() => {
		getModelConfigurations();
	},
	{ deep: true }
);

watch(
	() => [props.assetId],
	async () => {
		if (openedWorkflowNodeStore.node?.operationType === 'ModelOperation') {
			getModelConfigurations();
		} else if (props.assetId !== '') {
			model.value = await getModel(props.assetId);
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
	}
};

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
:deep(.p-datatable .p-datatable-tbody > tr > .borderless-row) {
	border-bottom: none;
}

.parameter-description {
	font-weight: 500;
	font-size: var(--font-body-small);
	color: var(--text-color-secondary);
}

.model-biblio {
	padding: 1rem;
}

:deep(.p-accordion .p-accordion-header .p-accordion-header-link) {
	font-size: var(--font-body-medium);
	font-weight: 600;
	color: var(--text-color-primary);
}
.model-biblio th {
	padding-right: 2rem;
	font-family: var(--font-family);
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	text-align: left;
}
.model-biblio td {
	padding-right: 50px;
	font-family: var(--font-family);
	font-weight: 400;
	font-size: var(--font-body-small);
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}
.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
	border-radius: var(--border-radius);
	border: 4px solid var(--primary-color);
	border-width: 0px 0px 0px 6px;
}

.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
}

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
	background-color: var(--surface-0);
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
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: auto;
}

.p-splitter {
	height: 100%;
}

.p-datatable:deep(td:hover) {
	background-color: var(--surface-secondary);
	cursor: pointer;
}

.tera-split-panel {
	position: relative;
	height: 100%;
	display: flex;
	align-items: center;
	width: 100%;
}

/* Let svg dynamically resize when the sidebar opens/closes or page resizes */
:deep(.graph-element svg) {
	width: 100%;
	height: 100%;
}

:deep(.p-datatable .p-datatable-thead > tr > th) {
	color: var(--text-color-light);
	font-size: var(--font-caption);
	text-transform: uppercase;
}
</style>
