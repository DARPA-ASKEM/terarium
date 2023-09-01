<template>
	<tera-asset
		:name="name"
		:feature-config="featureConfig"
		:is-naming-asset="isNamingModel"
		:stretch-content="modelView === ModelView.MODEL"
		@close-preview="emit('close-preview')"
	>
		<template #name-input>
			<InputText
				v-if="isNamingModel"
				v-model.lazy="newModelName"
				placeholder="Title of new model"
				@keyup.enter="updateModelName"
			/>
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
				<Button
					class="p-button-secondary p-button-sm"
					label="Transform"
					icon="pi pi-sync"
					@click="modelView = ModelView.NOTEBOOK"
					:active="modelView === ModelView.NOTEBOOK"
				/>
			</span>
			<template v-if="!featureConfig.isPreview">
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleOptionsMenu"
				/>
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
			<Button
				v-if="assetId === ''"
				@click="createNewModel"
				label="Create new model"
				class="p-button-sm"
			/>
		</template>
		<template v-if="modelView === ModelView.DESCRIPTION">
			<tera-model-description
				v-if="model"
				:model="model"
				:highlight="highlight"
				:project="project"
				@update-model="updateModelContents"
				@fetch-model="fetchModel"
			/>
		</template>
		<template v-if="modelView === ModelView.MODEL">
			<tera-model-diagram
				:model="model"
				:isEditable="!featureConfig.isPreview"
				@update-model-content="updateModelContent"
				@update-model-observables="updateModelObservables"
			/>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab v-if="model" header="Model configurations">
					<div v-if="stratifiedModelType">Stratified configs (WIP)</div>
					<tera-stratified-model-configuration
						ref="stratifiedModelConfigurationRef"
						v-if="stratifiedModelType"
						:stratified-model-type="stratifiedModelType"
						:model="model"
						:feature-config="featureConfig"
						@sync-configs="syncConfigs"
						@new-model-configuration="emit('new-model-configuration')"
					/>
					<div v-if="stratifiedModelType"><br />All values</div>
					<tera-model-configuration
						ref="modelConfigurationRef"
						:model="model"
						:feature-config="featureConfig"
						@sync-configs="syncConfigs"
						@new-model-configuration="emit('new-model-configuration')"
					/>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
					<DataTable :value="relatedTerariumModels">
						<Column field="name" header="Models" />
					</DataTable>
					<DataTable :value="relatedTerariumDatasets">
						<Column field="name" header="Datasets" />
					</DataTable>
					<DataTable :value="relatedTerariumDocuments">
						<Column field="name" header="Documents" />
					</DataTable>
				</AccordionTab>
			</Accordion>
		</template>
		<template v-if="modelView === ModelView.NOTEBOOK">
			<Suspense>
				<tera-model-jupyter-panel
					:asset-id="props.assetId"
					:project="props.project"
					:model="model"
					:show-kernels="false"
					:show-chat-thoughts="false"
				/>
			</Suspense>
		</template>
		<Teleport to="body">
			<tera-modal v-if="openValueConfig" @modal-mask-clicked="openValueConfig = false">
				<template #header>
					<header>Related publications</header>
				</template>
				<p>
					Terarium can extract information from papers and other artifacts to describe this model.
				</p>
				<Button label="Add resources to describe this model" link icon="pi pi-plus" />
			</tera-modal>
		</Teleport>
	</tera-asset>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { watch, ref, computed, onUpdated, PropType } from 'vue';
import { useRouter } from 'vue-router';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import {
	convertToAMRModel,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { RouteName } from '@/router/routes';
import { createModel, getModel, updateModel } from '@/services/model';
import * as ProjectService from '@/services/project';
import { getRelatedArtifacts } from '@/services/provenance';
import { ResultType, FeatureConfig } from '@/types/common';
import { IProject } from '@/types/Project';
import { Model, Document, Dataset, ProvenanceType, AssetType } from '@/types/Types';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import * as textUtil from '@/utils/text';
import Menu from 'primevue/menu';
import TeraStratifiedModelConfiguration from '@/components/models/tera-stratified-model-configuration.vue';
import useResourcesStore from '@/stores/resources';
import TeraModelDiagram from './tera-model-diagram.vue';
import TeraModelConfiguration from './tera-model-configuration.vue';
import TeraModelJupyterPanel from './tera-model-jupyter-panel.vue';

enum ModelView {
	DESCRIPTION,
	MODEL,
	NOTEBOOK
}

// TODO - Get rid of these emits
const emit = defineEmits([
	'close-preview',
	'asset-loaded',
	'close-current-tab',
	'new-model-configuration'
]);

const props = defineProps({
	project: {
		type: Object as PropType<IProject> | null,
		default: null,
		required: true
	},
	assetId: {
		type: String,
		required: true
	},
	highlight: {
		type: String,
		default: '',
		required: false
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const openValueConfig = ref(false);
const modelView = ref(ModelView.DESCRIPTION);
const router = useRouter();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

const model = ref<Model | null>(null);

const newModelName = ref('New Model');
const newDescription = ref<string | undefined>('');
const newPetri = ref();

const isRenamingModel = ref(false);
const isNamingModel = computed(() => props.assetId === '' || isRenamingModel.value);

const stratifiedModelType = computed(() => model.value && getStratificationType(model.value));

/*
 * User Menu
 */
const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingModel.value = true;
			newModelName.value = model.value?.header?.name ?? '';
		}
	}
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteModel }
]);

// These reference the different config components (TEMPORARY)
const stratifiedModelConfigurationRef = ref();
const modelConfigurationRef = ref();

// Sync different configs as we are TEMPORARILY showing both for stratified models
function syncConfigs(updateStratified = false) {
	if (stratifiedModelType.value) {
		if (updateStratified) {
			stratifiedModelConfigurationRef.value?.initializeConfigSpace();
		} else {
			modelConfigurationRef.value?.initializeConfigSpace();
		}
	}
}

/* Model */
const name = computed(() => highlightSearchTerms(model.value?.header?.name));

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

function updateModelContent(rendererGraph) {
	if (model.value) model.value = convertToAMRModel(rendererGraph);
}

// Just for tera-model-description
function updateModelContents(updatedModel: Model) {
	model.value = updatedModel;
	updateModel(updatedModel);
}

function updateModelObservables(observableMathMLList) {
	// assign the new observables
	if (model.value !== null && model.value.semantics?.ode?.observables) {
		model.value.semantics.ode.observables = observableMathMLList;
		updateModel(model.value);
	}
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

// TODO: Get model configurations

watch(
	() => [props.assetId],
	async () => {
		// Reset view of model page
		isRenamingModel.value = false;
		modelView.value = ModelView.DESCRIPTION;
		if (props.assetId !== '') {
			await fetchModel();
			fetchRelatedTerariumArtifacts();
			console.log(model.value);
		} else {
			model.value = null;
		}
	},
	{ immediate: true }
);

async function fetchModel() {
	model.value = await getModel(props.assetId);
}

onUpdated(() => {
	if (model.value) {
		emit('asset-loaded');
	}
});

const createNewModel = async () => {
	if (props.project) {
		const newModel = {
			name: newModelName.value,
			description: newDescription.value,
			content: JSON.stringify(newPetri.value ?? { S: [], T: [], I: [], O: [] })
		};
		const newModelResp = await createModel(newModel);
		if (newModelResp) {
			const modelId = newModelResp.id.toString();
			emit('close-current-tab');
			await ProjectService.addAsset(props.project.id, AssetType.Models, modelId);

			// Go to the model you just created
			router.push({
				name: RouteName.ProjectRoute,
				params: {
					assetName: newModelName.value,
					assetId: modelId,
					pageType: AssetType.Models
				}
			});
		}
	}
};

async function updateModelName() {
	if (model.value && newModelName.value !== '') {
		const modelClone = cloneDeep(model.value);
		modelClone.name = newModelName.value;
		await updateModel(modelClone);
		model.value = await getModel(props.assetId);
		useResourcesStore().setActiveProject(await ProjectService.get(props.project.id, true));
		isRenamingModel.value = false;
	} else if (newModelName.value !== '') {
		isRenamingModel.value = false;
	}
}
</script>

<style scoped>
.datatable header,
.datatable section {
	align-items: center;
	border-bottom: 1px solid var(--surface-border-light);
	display: grid;
	grid-template-columns: repeat(var(--columns), calc(100% / var(--columns)));
	grid-auto-flow: row;
	justify-items: start;
}

.datatable header > div,
.datatable section > div {
	padding: 0.5rem;
}

.datatable header {
	color: var(--text-color-light);
	font-size: var(--font-caption);
	font-weight: var(--font-weight-semibold);
	text-transform: uppercase;
}

.datatable section.active {
	background-color: var(--surface-secondary);
}

.datatable input {
	width: 100%;
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
}

.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}

.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	border-color: var(--primary-color);
	border-radius: var(--border-radius);
	border-width: 0px 0px 0px 6px;
	color: var(--text-color-primary);
}

.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
}

.p-button.p-component.p-button-sm.p-button-outlined.toolbar-button {
	background-color: var(--surface-0);
	margin: 0.25rem;
}

.p-datatable:deep(td:hover) {
	background-color: var(--surface-secondary);
	cursor: pointer;
}

:deep(.p-datatable .p-datatable-thead > tr > th) {
	color: var(--text-color-light);
	font-size: var(--font-caption);
	text-transform: uppercase;
}

.modal label {
	display: block;
	margin-bottom: 0.5em;
}
</style>
