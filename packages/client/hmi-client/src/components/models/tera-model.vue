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
				<Message class="inline-message" icon="none">
					This page describes the model. Use the content switcher above to see the diagram and
					manage configurations.
				</Message>
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
			<RelatedPublications :publications="publications" />
			<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6]" @click="editRow">
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
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>ID</th>
									<th>Value</th>
									<th>Distribution</th>
									<th>Extractions</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="parameter in parameters" :key="parameter.id">
									<td>{{ parameter.id ?? '--' }}</td>
									<td>{{ parameter?.value ?? '--' }}</td>
									<td>
										<template v-if="parameter?.distribution?.parameters">
											[{{ round(parameter?.distribution?.parameters.minimum, 4) }},
											{{ round(parameter?.distribution?.parameters.maximum, 4) }}] </template
										><template v-else>--</template>
									</td>
									<td>
										<template v-if="extractions?.[parameter.id]">
											<Tag :value="extractions?.[parameter.id].length" />
										</template>
										<template v-else>--</template>
									</td>
								</tr>
								<!-- <tr class="p-rowgroup-footer">
									<td colspan="5">
										<span class="parameter-description">
											{{ parameterGroup.data[0].description}}
										</span>
									</td>
								</tr> -->
							</tbody>
						</table>
					</div>
				</AccordionTab>

				<!-- State variables -->
				<AccordionTab>
					<template #header>
						State variables<span class="artifact-amount">({{ states.length }})</span>
					</template>
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>ID</th>
									<th>Name</th>
									<th>Unit</th>
									<th>Concept</th>
									<th>Identifiers</th>
									<th>Extractions</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="state in states" :key="state.id" :class="`state-${state.id}`">
									<!-- <template v-if="isRowEditable === `state-${state.id}`">
										<td>
											<input type="text" :value="state?.id ?? '--'" />
										</td>
										<td>
											<input type="text" :value="state?.name ?? '--'" />
										</td>
										<td>
											<input type="text" :value="state?.units?.expression ?? '--'" />
										</td>
										<td>
											<label>Concept</label>
										</td>
										<td>
											<label>Identifiers</label>
										</td>
										<td>
											<button type="submit" label="submit" />	
										</td>
									</template>
									<template v-else> -->
									<td>{{ state?.id ?? '--' }}</td>
									<td>{{ state?.name ?? '--' }}</td>
									<td>{{ state?.units?.expression ?? '--' }}</td>
									<td>
										<template v-if="state?.grounding?.context && !isEmpty(state.grounding.context)">
											<template
												v-for="[key, curie] in Object.entries(state.grounding.context)"
												:key="key"
											>
												<a
													target="_blank"
													rel="noopener noreferrer"
													:href="`http://34.230.33.149:8772/${curie}`"
												>
													{{ key }} ({{ curie }})</a
												><br />
											</template>
										</template>
										<template v-else>--</template>
									</td>
									<td>
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
												{{ getCurieFromGroudingIdentifier(state.grounding.identifiers) }}
											</a>
										</template>
										<template v-else>--</template>
									</td>
									<td>
										<template v-if="extractions?.[state?.id]">
											<Tag :value="extractions?.[state?.id].length" />
										</template>
										<template v-else>--</template>
									</td>
									<!-- </template> -->
								</tr>
							</tbody>
						</table>
					</div>
				</AccordionTab>

				<!-- Observables -->
				<AccordionTab>
					<template #header
						>Observables <span class="artifact-amount">({{ observables.length }})</span></template
					>
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>ID</th>
									<th>Name</th>
									<th>Expression</th>
									<th>Extractions</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="observable in observables" :key="observable.id">
									<td>{{ observable.id ?? '--' }}</td>
									<td>{{ observable.name ?? '--' }}</td>
									<td>
										<katex-element
											v-if="observable.expression"
											:expression="observable.expression"
										/>
										<template v-else>--</template>
									</td>
									<td>
										<template v-if="extractions?.[observable.id]">
											<Tag :value="extractions?.[observable.id].length" />
										</template>
										<template v-else>--</template>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</AccordionTab>

				<!-- Transitions -->
				<AccordionTab>
					<template #header>
						Transitions<span class="artifact-amount">({{ transitions.length }})</span>
					</template>
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>Label</th>
									<th>Input</th>
									<th>Output</th>
									<th>Expression</th>
									<th>Extractions</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="transition in transitions" :key="transition.id">
									<td>{{ transition?.properties?.name ?? '--' }}</td>
									<td>
										{{
											transition?.input && transition.input?.length > 0
												? transition.input.sort().join(', ')
												: '--'
										}}
									</td>
									<td>
										{{
											transition?.output && transition.output?.length > 0
												? transition.output.sort().join(', ')
												: '--'
										}}
									</td>
									<td>
										<katex-element :expression="getTransitionExpression(transition.id)" />
									</td>
									<td>
										<template v-if="extractions?.[transition.id]">
											<Tag :value="extractions?.[transition.id].length" />
										</template>
										<template v-else>--</template>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</AccordionTab>

				<!-- Other extractions -->
				<AccordionTab>
					<template #header>
						Other extractions
						<span class="artifact-amount">({{ otherExtractions.length }})</span>
					</template>
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>payload id</th>
									<th>names</th>
									<th>descriptions</th>
									<th>concept</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="item in otherExtractions" :key="item.payload?.id?.id">
									<td>{{ item.payload?.id?.id ?? '--' }}</td>
									<td>
										{{
											item.payload?.names?.length > 0
												? item.payload?.names?.map((n) => n?.name).join(', ')
												: '--'
										}}
									</td>
									<td>
										{{
											item.payload?.descriptions?.length > 0
												? item.payload?.descriptions?.map((d) => d?.source).join(', ')
												: '--'
										}}
									</td>
									<td>
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
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</AccordionTab>

				<!-- Time -->
				<AccordionTab>
					<template #header>
						Time
						<span class="artifact-amount">({{ time.length }})</span>
					</template>
					<div class="p-datatable p-datatable-sm">
						<table class="p-datatable-table">
							<thead class="p-datatable-thead">
								<tr>
									<th>ID</th>
									<th>Units</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr v-for="(item, index) in time" :key="index">
									<td>{{ item?.id ?? '--' }}</td>
									<td>{{ item?.units?.expression ?? '--' }}</td>
								</tr>
							</tbody>
						</table>
					</div>
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
				<AccordionTab v-if="model" header="Model configurations">
					<tera-model-configuration
						v-if="modelConfigurations"
						:model-configurations="modelConfigurations"
						:is-editable="props.isEditable"
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
import { capitalize, groupBy, isEmpty, round } from 'lodash';
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
import TeraModal from '@/components/widgets/tera-modal.vue';
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
import TeraModelConfiguration from './tera-model-configuration.vue';

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

/* Model */
const name = computed(() => highlightSearchTerms(model.value?.name));
const description = computed(() => highlightSearchTerms(model.value?.description));
const parameters = computed(() => model.value?.semantics?.ode.parameters ?? []);
const time = computed(() => [model.value?.semantics?.ode.time] ?? []);
const states = computed(() => model.value?.model?.states ?? []);
const transitions = computed(() => model.value?.model?.transitions ?? []);
const observables = computed(() => model.value?.semantics?.ode?.observables ?? []);
const publications = computed(() =>
	props.assetId === 'biomd0000000955-model-id' ? ['https://arxiv.org/pdf/2003.09861.pdf'] : []
);
const extractions = computed(() => {
	const attributes = model.value?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const otherExtractions = computed(() => {
	const ids = [
		...(states.value?.map((s) => s.id) ?? []),
		...(transitions.value?.map((t) => t.id) ?? [])
	];
	const key = Object.keys(extractions.value).find((k) => !ids.includes(k));
	if (key) return extractions.value[key.toString()];
	return [];
});
const isRowEditable = ref<string>();

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
	return model?.value?.semantics?.ode.rates.find((rate) => rate.target === id)?.expression ?? '--';
}

function getCurieFromGroudingIdentifier(identifier: Object | undefined): string {
	if (!!identifier && !isEmpty(identifier)) {
		const [key, value] = Object.entries(identifier)[0];
		return `${key}:${value}`;
	}
	return '';
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
			// FIXME: Why is this called when switching from one drilldown panel to a different type (there is already a guard checking for operation type in the watcher that calls this function)
			if (modelConfigurations.value[0].modelId) {
				model.value = await getModel(modelConfigurations.value[0].modelId);
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

			// TODO: Display model config in model page (non-drilldown)
			// When not in drilldown just show defualt config for now???
			// if (model.value) {
			// 	modelConfigurations.value.push({
			// 		id: 'default',
			// 		name: 'Default',
			// 		description: 'Default',
			// 		modelId: model.value.id,
			// 		amrConfiguration: model.value
			//		// missing S, T, I, O configuration
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

// Toggle rows to become editable
function editRow(event: Event) {
	if (!event?.target) return;
	const row = (event.target as HTMLElement).closest('.p-datatable-tbody tr');
	if (!row) return;
	isRowEditable.value = row.className;
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
