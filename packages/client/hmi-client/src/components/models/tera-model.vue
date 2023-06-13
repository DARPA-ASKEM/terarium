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
		<table class="model-biblio">
			<tr>
				<th class="model-biblio-header">Framework</th>
				<th class="model-biblio-header">Model Version</th>
				<th class="model-biblio-header">Date Created</th>
				<th class="model-biblio-header">Created By</th>
				<th class="model-biblio-header">Source</th>
			</tr>
			<tr>
				<td class="model-biblio-column">{{ model?.framework }}</td>
				<td class="model-biblio-column">{{ model?.model_version }}</td>
				<td class="model-biblio-column">{{ model?.metadata.processed_at }}</td>
				<td class="model-biblio-column">{{ model?.description }}</td>
				<td class="model-biblio-column">{{ model?.metadata.processed_by }}</td>
			</tr>
		</table>
		<Accordion
			v-if="modelView === ModelView.DESCRIPTION"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
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
				<DataTable
					class="p-datatable-sm"
					:value="modelParameters"
					rowGroupMode="subheader"
					groupRowsBy="description"
				>
					<Column class="borderless-row" field="description" header="description"></Column>
					<Column class="borderless-row" field="id" header="ID"></Column>
					<Column class="borderless-row" field="value" header="Value"></Column>
					<template #groupfooter="modelParameters">
						<div>
							<span class="parameter-description">{{ modelParameters.data.description }}</span>
						</div>
					</template>
				</DataTable>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="State variables">State variables</header>
				</template>
				<DataTable class="p-datatable-sm" :value="model?.model.states">
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
				<DataTable class="p-datatable-sm" :value="model?.model.transitions">
					<Column field="id" header="ID"></Column>
					<Column field="properties.name" header="Name"></Column>
					<Column field="input" header="Input"></Column>
					<Column field="output" header="Output"></Column>
					<!-- <Column field="properties.rate.expression" header="Expression"></Column> -->
					<Column field="properties.rate.expression_mathml" header="Expression">
						<template #body="slotProps">
							<katex-element
								:expression="
									model?.semantics?.ode.rates.find((rate) => rate.target === slotProps.data.id)
										?.expression
								"
							/>
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
		<Accordion
			v-else-if="modelView === ModelView.MODEL"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
			<AccordionTab header="Model diagram">
				<tera-model-diagram
					:model="model"
					:is-editable="props.isEditable"
					@update-model-content="updateModelContent"
				/>
			</AccordionTab>
			<AccordionTab v-if="model" header="Model configurations">
				<tera-model-configuration :model="model" :is-editable="props.isEditable" />
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
	</tera-asset>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { watch, ref, computed, onUpdated, PropType } from 'vue';
import { parseIGraph2PetriNet } from '@/petrinet/petrinet-service';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import { createModel, addModelToProject, getModel } from '@/services/model';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as textUtil from '@/utils/text';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { Model, Document, Dataset, ProvenanceType } from '@/types/Types';
import { ResultType } from '@/types/common';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { getRelatedArtifacts } from '@/services/provenance';
import TeraModelDiagram from './tera-model-diagram.vue';
import TeraModelConfiguration from './tera-model-configuration.vue';

enum ModelView {
	DESCRIPTION = 'description',
	MODEL = 'model'
}
/*
// This is the model content that is displayed in the scroll-to-section featuer
// That feature was removed, but way may want to bring it back.
// I suggest we keep this unil we decide to remove it for good.
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
	{ key: 'Parameters', value: model.value?.semantics?.ode?.parameters },
	{ key: 'State variables', value: model.value?.model.states },
	{ key: 'Transitions', value: model.value?.model.transitions },
	{ key: 'Variable Statements', value: model.value?.metadata?.variable_statements }
]);
*/

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

const modelView = ref(ModelView.DESCRIPTION);
const resources = useResourcesStore();
const router = useRouter();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

const model = ref<Model | null>(null);

// apparently this is never used?
// const isEditing = ref<boolean>(false);
const isEditingEQ = ref<boolean>(false);

const newModelName = ref('New Model');
const newDescription = ref<string | undefined>('');
const newPetri = ref();

const isMathMLValid = ref<boolean>(true);

const metaData = computed(() => model.value?.metadata?.variable_statements);

const name = computed(() => highlightSearchTerms(model.value?.name ?? ''));
const description = computed(() => highlightSearchTerms(model.value?.description));
const modelParameters = computed(() => model.value?.semantics?.ode.parameters);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

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

watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			model.value = await getModel(props.assetId);
			fetchRelatedTerariumArtifacts();
			console.log(model.value);
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
		isEditingEQ.value = false;
		isMathMLValid.value = true;
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
/* :deep(.p-datatable .p-datatable-tbody > .p-rowgroup-footer > td){
	border: none;
} */

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
.model-biblio-header {
	padding-right: 2rem;
	font-family: var(--font-family);
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	text-align: left;
}
.model-biblio-column {
	padding-right: 50px;
	font-family: var(--font-family);
	font-weight: 400;
	font-size: var(--font-body-small);
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
</style>
