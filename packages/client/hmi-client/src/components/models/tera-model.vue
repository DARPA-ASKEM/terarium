<template>
	<tera-asset
		:name="name"
		:is-editable="isEditable"
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
			<Button
				v-if="isEditable"
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleOptionsMenu"
			/>
			<Menu v-if="isEditable" ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			<Button
				v-if="assetId === ''"
				@click="createNewModel"
				label="Create new model"
				class="p-button-sm"
			/>
		</template>
		<!-- For testing dummy data -->
		<!-- <tera-stratified-model-configuration :model="stratify_output" :is-editable="props.isEditable" /> -->
		<template v-if="modelView === ModelView.DESCRIPTION">
			<div class="container">
				<Message class="inline-message" icon="none">
					This page describes the model. Use the content switcher above to see the diagram and
					manage configurations.
				</Message>
			</div>
			<table class="model-biblio">
				<tr>
					<th>Framework</th>
					<th>Model version</th>
					<th>Date created</th>
					<th>Created by</th>
					<th>Source</th>
				</tr>
				<tr>
					<td>{{ capitalize(model?.schema_name ?? '--') }}</td>
					<td>{{ model?.model_version ?? '--' }}</td>
					<td>{{ model?.metadata?.processed_at ?? '--' }}</td>
					<td>
						{{
							model?.metadata?.annotations?.authors &&
							model?.metadata?.annotations?.authors?.length > 0
								? model?.metadata?.annotations?.authors?.join(', ')
								: '--'
						}}
					</td>
					<td>{{ model?.metadata?.processed_by ?? '--' }}</td>
				</tr>
			</table>
			<tera-related-publications
				:publications="publications"
				:project="project"
				:dialog-flavour="'model'"
			/>
			<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6]">
				<!-- Description -->
				<AccordionTab>
					<template #header>Description</template>
					<p v-if="assetId !== ''" v-html="description" />
					<template v-else>
						<label for="placeholder" />
						<Textarea v-model="newDescription" rows="5" placeholder="Description of new model" />
					</template>
				</AccordionTab>

				<!-- Parameters -->
				<AccordionTab>
					<template #header>
						Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
					</template>
					<main v-if="parameters.length > 0" class="datatable" style="--columns: 5">
						<header>
							<div>ID</div>
							<div>Value</div>
							<div>Distribution</div>
							<div>Extractions</div>
						</header>
						<section
							v-for="(parameter, i) in parameters"
							:key="parameter.id"
							:class="[
								{ active: isSectionEditable === `parameter-${parameter.id}` },
								`parameter-${parameter.id}`
							]"
						>
							<template v-if="isRowEditable === `parameter-${parameter.id}`">
								<div>
									<input
										type="text"
										:value="parameter?.id ?? '--'"
										@input="updateTable('parameters', i, 'id', $event.target?.value)"
									/>
								</div>
								<div>
									<input
										type="text"
										:value="parameter?.value ?? '--'"
										@input="updateTable('parameters', i, 'id', $event.target?.value)"
									/>
								</div>
								<div>--</div>
								<div>
									<template v-if="parameter?.distribution?.parameters">
										[{{ round(parameter?.distribution?.parameters.minimum, 4) }},
										{{ round(parameter?.distribution?.parameters.maximum, 4) }}]
									</template>
									<template v-else>--</template>
								</div>
								<div v-if="extractions?.[parameter?.id]" style="grid-column: 1 / span 4">
									<tera-model-extraction :extractions="extractions[parameter.id]" />
								</div>
							</template>
							<template v-else>
								<div>{{ parameter?.id ?? '--' }}</div>
								<div>{{ parameter?.value ?? '--' }}</div>
								<div>
									<template v-if="parameter?.distribution?.parameters">
										[{{ round(parameter?.distribution?.parameters.minimum, 4) }},
										{{ round(parameter?.distribution?.parameters.maximum, 4) }}]
									</template>
									<template v-else>--</template>
								</div>
								<div>
									<template v-if="extractions?.[parameter.id]">
										<Tag :value="extractions?.[parameter.id].length" />
									</template>
									<template v-else>--</template>
								</div>
							</template>
							<div v-if="!isRowEditable">
								<Button icon="pi pi-file-edit" text @click="editRow" />
							</div>
							<div v-else-if="isRowEditable === `parameter-${parameter.id}`">
								<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
							</div>
						</section>
					</main>
				</AccordionTab>

				<!-- State variables -->
				<AccordionTab>
					<template #header>
						State variables<span class="artifact-amount">({{ states.length }})</span>
					</template>
					<main v-if="states.length > 0" class="datatable" style="--columns: 6">
						<header>
							<div>Id</div>
							<div>Name</div>
							<div>Unit</div>
							<div>Concept</div>
							<div>Extractions</div>
						</header>
						<section
							v-for="(state, i) in states"
							:key="state.id"
							:class="[{ active: isRowEditable === `state-${state.id}` }, `state-${state.id}`]"
						>
							<template v-if="isRowEditable === `state-${state.id}`">
								<div>
									<input
										type="text"
										:value="state?.id ?? '--'"
										@input="updateTable('states', i, 'id', $event.target?.value)"
									/>
								</div>
								<div>
									<input
										type="text"
										:value="state?.name ?? '--'"
										@input="updateTable('states', i, 'name', $event.target?.value)"
									/>
								</div>
								<div><input type="text" :value="state?.units?.expression ?? '--'" /></div>
								<div>Identifiers</div>
								<div>
									<template v-if="extractions?.[state?.id]">
										<Tag :value="extractions?.[state?.id].length" />
									</template>
									<template v-else>--</template>
								</div>
								<div v-if="extractions?.[state?.id]" style="grid-column: 1 / span 4">
									<tera-model-extraction :extractions="extractions[state.id]" />
								</div>
							</template>
							<template v-else>
								<div>{{ state.id ?? '--' }}</div>
								<div>{{ state?.name ?? '--' }}</div>
								<div>{{ state?.units?.expression ?? '--' }}</div>
								<div>
									<template
										v-if="state?.grounding?.identifiers && !isEmpty(state.grounding.identifiers)"
									>
										<a
											target="_blank"
											rel="noopener noreferrer"
											:href="`http://34.230.33.149:8772/${getCurieFromGroudingIdentifier(
												state.grounding.identifiers
											)}`"
										>
											{{
												getNameOfCurieCached(
													getCurieFromGroudingIdentifier(state.grounding.identifiers)
												)
											}}
										</a>
									</template>
									<template v-else>--</template>
								</div>
								<div>
									<template v-if="extractions?.[state?.id]">
										<Tag :value="extractions?.[state?.id].length" />
									</template>
									<template v-else>--</template>
								</div>
							</template>
							<div v-if="!isRowEditable">
								<Button icon="pi pi-file-edit" text @click="editRow" />
							</div>
							<div v-else-if="isRowEditable === `state-${state.id}`">
								<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
							</div>
						</section>
					</main>
				</AccordionTab>

				<!-- Observables -->
				<AccordionTab>
					<template #header>
						Observables <span class="artifact-amount">({{ observables.length }})</span>
					</template>
					<main v-if="observables.length > 0" class="datatable" style="--columns: 4">
						<header>
							<div>ID</div>
							<div>Name</div>
							<div>Expression</div>
							<div>Extractions</div>
						</header>
						<section
							v-for="observable in observables"
							:key="observable.id"
							:class="[
								{ active: isSectionEditable === `observable-${observable.id}` },
								`observable-${observable.id}`
							]"
						>
							<template v-if="isSectionEditable === `observable-${observable.id}`">
								<div>{{ observable.id ?? '--' }}</div>
								<div>{{ observable.name ?? '--' }}</div>
								<div>
									<katex-element
										v-if="observable.expression"
										:expression="observable.expression"
										:throw-on-error="false"
									/>
									<template v-else>--</template>
								</div>
								<div>
									<!-- TODO: needs to make those button active -->
									<Button icon="pi pi-check" text rounded aria-label="Save" />
									<Button icon="pi pi-times" text rounded aria-label="Discard" />
								</div>
								<div v-if="extractions?.[observable?.id]" style="grid-column: 1 / span 4">
									<tera-model-extraction :extractions="extractions[observable.id]" />
								</div>
							</template>
							<template v-else>
								<div>{{ observable.id ?? '--' }}</div>
								<div>{{ observable.name ?? '--' }}</div>
								<div>
									<katex-element
										v-if="observable.expression"
										:expression="observable.expression"
										:throw-on-error="false"
									/>
									<template v-else>--</template>
								</div>
								<div>
									<template v-if="extractions?.[observable.id]">
										<Tag :value="extractions?.[observable.id].length" />
									</template>
									<template v-else>--</template>
								</div>
							</template>
						</section>
					</main>
				</AccordionTab>

				<!-- Transitions -->
				<AccordionTab>
					<template #header>
						Transitions<span class="artifact-amount">({{ transitions.length }})</span>
					</template>
					<main v-if="transitions.length > 0" class="datatable" style="--columns: 6">
						<header>
							<div>Id</div>
							<div>Name</div>
							<div>Input</div>
							<div>Output</div>
							<div>Expression</div>
							<div>Extractions</div>
						</header>
						<section
							v-for="(transition, index) in transitions"
							:key="index"
							:class="[
								{ active: isSectionEditable === `transition-${index}` },
								`transition-${index}`
							]"
						>
							<template v-if="isSectionEditable === `transition-${index}`">
								<div>{{ transition.id }}</div>
								<div>{{ transition.name }}</div>
								<div>{{ transition.input }}</div>
								<div>{{ transition.output }}</div>
								<div>
									<katex-element
										v-if="transition.expression"
										:expression="transition.expression"
										:throw-on-error="false"
									/>
									<template v-else>--</template>
								</div>
								<div>
									<!-- TODO: needs to make those button active -->
									<Button icon="pi pi-check" text rounded aria-label="Save" />
									<Button icon="pi pi-times" text rounded aria-label="Discard" />
								</div>
								<div v-if="transition?.extractions" style="grid-column: 1 / span 5">
									<tera-model-extraction :extractions="transition?.extractions" />
								</div>
							</template>
							<template v-else>
								<div>{{ transition.id }}</div>
								<div>{{ transition.name }}</div>
								<div>{{ transition.input }}</div>
								<div>{{ transition.output }}</div>
								<div>
									<katex-element
										v-if="transition.expression"
										:expression="transition.expression"
										:throw-on-error="false"
									/>
									<template v-else>--</template>
								</div>
								<div>
									<Tag
										v-if="transition?.extractions"
										:value="extractions?.[transition.id].length"
									/>
									<template v-else>--</template>
								</div>
							</template>
						</section>
					</main>
				</AccordionTab>

				<!-- Other concepts -->
				<AccordionTab>
					<template #header>
						Other concepts
						<span class="artifact-amount">({{ otherConcepts.length }})</span>
					</template>
					<main v-if="otherConcepts.length > 0" class="datatable" style="--columns: 4">
						<header>
							<div>Payload id</div>
							<div>Names</div>
							<div>Descriptions</div>
							<div>Concept</div>
						</header>
						<section v-for="item in otherConcepts" :key="item.payload?.id?.id">
							<div>{{ item.payload?.id?.id ?? '--' }}</div>
							<div>
								{{
									item.payload?.names?.length > 0
										? item.payload?.names?.map((n) => n?.name).join(', ')
										: '--'
								}}
							</div>
							<div>
								{{
									item.payload?.descriptions?.length > 0
										? item.payload?.descriptions?.map((d) => d?.source).join(', ')
										: '--'
								}}
							</div>
							<div>
								<template v-if="!item.payload.groundings || item.payload.groundings.length < 1"
									>--</template
								>
								<template
									v-else
									v-for="grounding in item.payload.groundings"
									:key="grounding.grounding_id"
								>
									<a
										target="_blank"
										rel="noopener noreferrer"
										:href="`http://34.230.33.149:8772/${grounding.grounding_id}`"
									>
										{{ grounding.grounding_text }}
									</a>
								</template>
							</div>
						</section>
					</main>
				</AccordionTab>

				<!-- Time -->
				<AccordionTab>
					<template #header>
						Time
						<span class="artifact-amount">({{ time.length }})</span>
					</template>
					<main v-if="time.length > 0" class="datatable" style="--columns: 3">
						<header>
							<div>ID</div>
							<div>Units</div>
							<div>Extractions</div>
						</header>
						<section v-for="(item, index) in time" :key="index">
							<div>{{ item?.id ?? '--' }}</div>
							<div>{{ item?.units?.expression ?? '--' }}</div>
						</section>
					</main>
				</AccordionTab>
			</Accordion>
		</template>
		<template v-if="modelView === ModelView.MODEL">
			<tera-model-diagram
				:model="model"
				:is-editable="props.isEditable"
				@update-model-content="updateModelContent"
				@update-model-observables="updateModelObservables"
			/>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab v-if="model" header="Model configurations">
					<tera-stratified-model-configuration
						v-if="model.semantics?.span"
						:model="model"
						:is-editable="props.isEditable"
					/>
					<tera-model-configuration v-else :model="model" :is-editable="props.isEditable" />
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
				<!-- @is-typing="updateScroll" -->
			</Suspense>
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

		<!-- Copy model modal -->
		<Teleport to="body">
			<tera-modal
				v-if="isCopyModelModalVisible"
				class="modal"
				@modal-mask-clicked="isCopyModelModalVisible = false"
			>
				<template #header>
					<h4>Make a copy</h4>
				</template>
				<template #default>
					<form>
						<label for="copy-model">{{ copyModelNameInputPrompt }}</label>
						<InputText
							v-bind:class="invalidInputStyle"
							id="copy-model"
							type="text"
							v-model="copyModelName"
							placeholder="Model name"
						/>
					</form>
				</template>
				<template #footer>
					<Button @click="duplicateModel">Copy model</Button>
					<Button class="p-button-secondary" @click="isCopyModelModalVisible = false">
						Cancel
					</Button>
				</template>
			</tera-modal>
		</Teleport>
	</tera-asset>
</template>

<script setup lang="ts">
import { capitalize, groupBy, isEmpty, round, cloneDeep } from 'lodash';
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
import TeraRelatedPublications from '@/components/widgets/tera-related-publications.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import {
	convertToAMRModel,
	replaceValuesInMathML
} from '@/model-representation/petrinet/petrinet-service';
import { RouteName } from '@/router/routes';
import { getCuriesEntities } from '@/services/concept';
import { createModel, addModelToProject, getModel, updateModel } from '@/services/model';
import * as ProjectService from '@/services/project';
import { getRelatedArtifacts } from '@/services/provenance';
import { ResultType } from '@/types/common';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { Model, Document, Dataset, ProvenanceType } from '@/types/Types';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import * as textUtil from '@/utils/text';
import Menu from 'primevue/menu';
import TeraModelExtraction from '@/components/models/tera-model-extraction.vue';
import { logger } from '@/utils/logger';
import TeraStratifiedModelConfiguration from '@/components/models/tera-stratified-model-configuration.vue';
// import { stratify_output } from '@/temp/models/stratify_output';
import TeraModelDiagram from './tera-model-diagram.vue';
import TeraModelConfiguration from './tera-model-configuration.vue';
import TeraModelJupyterPanel from './tera-model-jupyter-panel.vue';

enum ModelView {
	DESCRIPTION,
	MODEL,
	NOTEBOOK
}

// TODO - Get rid of these emits
const emit = defineEmits(['update-tab-name', 'close-preview', 'asset-loaded', 'close-current-tab']);

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
	isEditable: {
		type: Boolean,
		default: false,
		required: false
	},
	highlight: {
		type: String,
		default: '',
		required: false
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

const isCopyModelModalVisible = ref<boolean>(false);
const copyModelNameInputPrompt = ref<string>('');
const copyModelName = ref<string>('');

const isValidName = ref<boolean>(true);
const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));

const existingModelNames = computed(() => {
	const modelNames: string[] = [];
	props.project.assets?.models.forEach((item) => {
		modelNames.push(item.name);
	});
	return modelNames;
});

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

/*
 * User Menu
 */
const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingModel.value = true;
			newModelName.value = model.value?.name ?? '';
		}
	},
	{ icon: 'pi pi-clone', label: 'Make a copy', command: initiateModelDuplication }
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteModel }
]);

function getJustModelName(modelName: string): string {
	let potentialNum: string = '';
	let completeParen: boolean = false;
	let idx = modelName.length;
	if (modelName.charAt(modelName.length - 1) === ')') {
		for (let i = modelName.length - 2; i >= 0; i--) {
			if (modelName.charAt(i) === '(') {
				completeParen = true;
				idx = i;
				break;
			}
			potentialNum = modelName.charAt(i) + potentialNum;
		}
	}

	if (completeParen && !Number.isNaN(potentialNum as any)) {
		return modelName.substring(0, idx).trim();
	}
	return modelName.trim();
}

function getSuggestedModelName(currModelName: string, counter: number): string {
	const suggestedName = `${currModelName} (${counter})`;

	if (!existingModelNames.value.includes(suggestedName)) {
		return suggestedName;
	}
	return getSuggestedModelName(currModelName, counter + 1);
}

function initiateModelDuplication() {
	if (!model.value) {
		logger.info('Failed to duplicate model.');
		return;
	}
	copyModelNameInputPrompt.value = 'What do you want to name it?';
	const modelName = getJustModelName(model.value.name.trim());
	copyModelName.value = getSuggestedModelName(modelName, 1);
	isCopyModelModalVisible.value = true;
}

async function duplicateModel() {
	if (existingModelNames.value.includes(copyModelName.value.trim())) {
		copyModelNameInputPrompt.value = 'Duplicate model name - please enter a different name:';
		isValidName.value = false;
		logger.info('Duplicate model name - please enter a different name');
		return;
	}
	copyModelNameInputPrompt.value = 'Creating a copy...';
	isValidName.value = true;
	const duplicateModelResponse = await createModel({
		...model.value,
		name: copyModelName.value.trim()
	});
	if (!duplicateModelResponse) {
		logger.info('Failed to duplicate model.');
		isCopyModelModalVisible.value = false;
		return;
	}
	await ProjectService.addAsset(
		props.project.id,
		ProjectAssetTypes.MODELS,
		duplicateModelResponse.id
	);
	isCopyModelModalVisible.value = false;
}

/* Model */
const name = computed(() => highlightSearchTerms(model.value?.name));
const description = computed(() => highlightSearchTerms(model.value?.description));
const parameters = computed(() => model.value?.semantics?.ode.parameters ?? []);
const time = computed(() =>
	model.value?.semantics?.ode?.time ? [model.value?.semantics.ode.time] : []
);
const states = computed(() => model.value?.model?.states ?? []);
const transientTableValue = ref();

// Model Transitions
const transitions = computed(() => {
	const results: any[] = [];
	if (model.value?.model?.transitions) {
		model.value.model.transitions.forEach((t) => {
			results.push({
				id: t.id,
				name: t?.properties?.name ?? '--',
				input: !isEmpty(t.input) ? t.input.sort().join(', ') : '--',
				output: !isEmpty(t.output) ? t.output.sort().join(', ') : '--',
				expression:
					model?.value?.semantics?.ode.rates.find((rate) => rate.target === t.id)?.expression ??
					null,
				extractions: extractions?.[t.id] ?? null
			});
		});
	}
	return results;
});

const observables = computed(() => model.value?.semantics?.ode?.observables ?? []);

const publications = computed(() => []);

const extractions = computed(() => {
	const attributes = model.value?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const otherConcepts = computed(() => {
	const ids = [
		...(states.value?.map((s) => s.id) ?? []),
		...(transitions.value?.map((t) => t.id) ?? [])
	];
	const key = Object.keys(extractions.value).find((k) => !ids.includes(k));
	if (key) {
		return extractions.value[key.toString()].filter((e) => e.type === 'anchored_extraction');
	}
	return [];
});
const isSectionEditable = ref<string | null>();
const isRowEditable = ref<string | null>();

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

/**
 * Concepts
 */
const nameOfCurieCache = ref(new Map<string, string>());
const getNameOfCurieCached = (curie: string): string => {
	if (!nameOfCurieCache.value.has(curie)) {
		getCuriesEntities([curie]).then((response) =>
			nameOfCurieCache.value.set(curie, response?.[0].name ?? '')
		);
	}
	return nameOfCurieCache.value.get(curie) ?? '';
};

function getCurieFromGroudingIdentifier(identifier: Object | undefined): string {
	if (!!identifier && !isEmpty(identifier)) {
		const [key, value] = Object.entries(identifier)[0];
		return `${key}:${value}`;
	}
	return '';
}

function updateModelContent(rendererGraph) {
	if (model.value) model.value = convertToAMRModel(rendererGraph);
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
			description: newDescription.value,
			content: JSON.stringify(newPetri.value ?? { S: [], T: [], I: [], O: [] })
		};
		const newModelResp = await createModel(newModel);
		if (newModelResp) {
			const modelId = newModelResp.id.toString();
			emit('close-current-tab');
			await addModelToProject(props.project.id, modelId);

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

async function updateModelName() {
	if (model.value) {
		const modelClone = cloneDeep(model.value);
		modelClone.name = newModelName.value;
		await updateModel(modelClone);
		model.value = await getModel(props.assetId);
		// FIXME: Names aren't updated in sidebar
	}
	isRenamingModel.value = false;
}

// Toggle rows to become editable
function editRow(event: Event) {
	if (!event?.target) return;
	const row = (event.target as HTMLElement).closest('.datatable section');
	if (!row) return;
	isRowEditable.value = isRowEditable.value === row.className ? null : row.className;
}

async function confirmEdit() {
	if (model.value && transientTableValue.value) {
		const { tableType, idx, updateProperty } = transientTableValue.value;
		const modelClone = cloneDeep(model.value);

		switch (tableType) {
			case 'parameters':
				if (modelClone.semantics?.ode.parameters) {
					Object.entries(updateProperty).forEach(([key, value]) => {
						modelClone.semantics!.ode.parameters![idx][key] = value;

						if (key === 'id') {
							// if we update parameter id we also need to update rate expression and expression_mathml
							const updatedRates = model.value!.semantics!.ode.rates.map((rate) => ({
								...rate,
								expression: rate.expression.replaceAll(
									model.value!.semantics!.ode.parameters![idx][key],
									value as string
								),
								expression_mathml: rate.expression_mathml
									? replaceValuesInMathML(
											rate.expression_mathml,
											model.value!.semantics!.ode.parameters![idx][key],
											value as string
									  )
									: undefined
							}));
							modelClone.semantics!.ode.rates = updatedRates;
						}
					});
				}
				break;
			case 'states':
				Object.entries(updateProperty).forEach(([key, value]) => {
					if (key !== 'unit') {
						// TODO: remove this condition when we proper editing of unit
						modelClone.model.states[idx][key] = value;
					}
					// TODO: update all of the properties affected by state id
				});
				break;
			default:
				logger.info(`${tableType} not recognized`);
		}
		await updateModel(modelClone);
		model.value = await getModel(props.assetId);
	}
	isRowEditable.value = null;
	transientTableValue.value = {};
}

function cancelEdit() {
	isRowEditable.value = null;
	transientTableValue.value = {};
}

function updateTable(tableType, idx, key, value) {
	transientTableValue.value = {
		...transientTableValue.value,
		tableType,
		idx,
		updateProperty: {
			...transientTableValue.value?.updateProperty,
			[key]: value
		}
	};
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

.floating-edit-button {
	background-color: var(--surface-0);
	margin-top: 10px;
	position: absolute;
	right: 10px;
	z-index: 10;
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
