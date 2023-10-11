<template>
	<main>
		<section>
			<Message icon="none">
				This page describes the model. Use the content switcher above to see the diagram and manage
				configurations.
			</Message>
			<table class="bibliography">
				<tr>
					<th>Framework</th>
					<th>Model version</th>
					<th>Date created</th>
					<th>Created by</th>
					<th>Source</th>
					<th>Author email</th>
					<th>Institution</th>
					<th>License</th>
					<th>Complexity</th>
				</tr>
				<tr>
					<td class="framework">{{ model?.header?.schema_name }}</td>
					<td>{{ model?.header?.model_version }}</td>
					<td>{{ model?.metadata?.processed_at ?? card?.date }}</td>
					<td>
						{{ card?.authorAuthor }}
						<template v-if="model?.metadata?.annotations?.authors">
							, {{ model.metadata.annotations.authors.join(', ') }}
						</template>
					</td>
					<td>{{ card?.authorEmail }}</td>
					<td>{{ model?.metadata?.processed_by }}</td>
					<td>{{ card?.authorInst }}</td>
					<td>{{ card?.license }}</td>
					<td>{{ card?.complexity }}</td>
				</tr>
			</table>
		</section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]">
			<AccordionTab>
				<template #header>Related publications</template>
				<tera-related-documents
					:documents="documents"
					:related-documents="relatedDocuments"
					:asset-type="ResourceType.MODEL"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</AccordionTab>
			<AccordionTab>
				<template #header>Description</template>
				<p v-html="description" />
				<!--
					For model creation
					<template v-else>
						<label for="placeholder" />
						<Textarea v-model="newDescription" rows="5" placeholder="Description of new model" />
					</template> -->
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(usage)">
				<template #header>Usage</template>
				<p v-html="usage" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(sourceDataset)">
				<template #header>Source dataset</template>
				<p v-html="sourceDataset" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(provenance)">
				<template #header>Provenance</template>
				<p v-html="provenance" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(schema)">
				<template #header>Schema</template>
				<p v-html="schema" />
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
				</template>
				<table v-if="parameters.length > 0" class="datatable" style="--columns: 5">
					<tr>
						<th>ID</th>
						<th>Value</th>
						<th>Distribution</th>
						<th>Extractions</th>
					</tr>
					<tr
						v-for="(parameter, i) in parameters"
						:key="parameter.id"
						:class="[
							{ active: isRowEditable === `parameter-${parameter.id}` },
							`parameter-${parameter.id}`
						]"
					>
						<template v-if="isRowEditable === `parameter-${parameter.id}`">
							<td>
								<input
									type="text"
									:value="parameter?.id ?? '--'"
									@input="updateTable('parameters', i, 'id', $event.target?.['value'])"
								/>
							</td>
							<td>
								<input
									type="text"
									:value="parameter?.value ?? '--'"
									@input="updateTable('parameters', i, 'value', $event.target?.['value'])"
								/>
							</td>
							<td>--</td>
							<td>
								<template v-if="parameter?.distribution?.parameters">
									[{{ round(parameter?.distribution?.parameters.minimum, 4) }},
									{{ round(parameter?.distribution?.parameters.maximum, 4) }}]
								</template>
								<template v-else>--</template>
							</td>
							<td v-if="extractions?.[parameter?.id]" style="grid-column: 1 / span 4">
								<tera-model-extraction :extractions="extractions[parameter.id]" />
							</td>
						</template>
						<template v-else>
							<td>{{ parameter?.id }}</td>
							<td>{{ parameter?.value }}</td>
							<td>
								<template v-if="parameter?.distribution?.parameters">
									[{{ round(parameter?.distribution?.parameters.minimum, 4) }},
									{{ round(parameter?.distribution?.parameters.maximum, 4) }}]
								</template>
								<template v-else>--</template>
							</td>
							<td>
								<template v-if="extractions?.[parameter.id]">
									<Tag :value="extractions?.[parameter.id].length" />
								</template>
								<template v-else>--</template>
							</td>
						</template>
						<td v-if="!isRowEditable">
							<Button icon="pi pi-file-edit" text @click="editRow" />
						</td>
						<td v-else-if="isRowEditable === `parameter-${parameter.id}`">
							<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
							<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
						</td>
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					State variables<span class="artifact-amount">({{ states.length }})</span>
				</template>
				<table v-if="states.length > 0" class="datatable" style="--columns: 5">
					<tr>
						<th>Id</th>
						<th>Name</th>
						<th>Unit</th>
						<th>Concept</th>
						<th>Extractions</th>
					</tr>
					<tr
						v-for="(state, i) in states"
						:key="state.id"
						:class="[{ active: isRowEditable === `state-${state.id}` }, `state-${state.id}`]"
					>
						<template v-if="isRowEditable === `state-${state.id}`">
							<td>
								<input
									type="text"
									:value="state?.id ?? '--'"
									@input="updateTable('states', i, 'id', $event.target?.['value'])"
								/>
							</td>
							<td>
								<input
									type="text"
									:value="state?.name ?? '--'"
									@input="updateTable('states', i, 'name', $event.target?.['value'])"
								/>
							</td>
							<td><input type="text" :value="state?.units?.expression ?? '--'" /></td>
							<td>Identifiers</td>
							<td>
								<template v-if="extractions?.[state?.id]">
									<Tag :value="extractions?.[state?.id].length" />
								</template>
								<template v-else>--</template>
							</td>
							<td v-if="extractions?.[state?.id]" style="grid-column: 1 / span 4">
								<tera-model-extraction :extractions="extractions[state.id]" />
							</td>
						</template>
						<template v-else>
							<td>{{ state.id }}</td>
							<td>{{ state?.name }}</td>
							<td>{{ state?.units?.expression }}</td>
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
										{{
											getNameOfCurieCached(
												getCurieFromGroudingIdentifier(state.grounding.identifiers)
											)
										}}
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
						</template>
						<!-- <div v-if="!isRowEditable">
								<Button icon="pi pi-file-edit" text @click="editRow" />
							</div>
							<div v-else-if="isRowEditable === `state-${state.id}`">
								<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
							</div> -->
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Observables <span class="artifact-amount">({{ observables.length }})</span>
				</template>
				<table v-if="!isEmpty(observables)" class="datatable" style="--columns: 4">
					<tr>
						<th>ID</th>
						<th>Name</th>
						<th>Expression</th>
						<th>Extractions</th>
					</tr>
					<tr
						v-for="observable in observables"
						:key="observable.id"
						:class="[
							{ active: isSectionEditable === `observable-${observable.id}` },
							`observable-${observable.id}`
						]"
					>
						<template v-if="isSectionEditable === `observable-${observable.id}`">
							<td>{{ observable.id }}</td>
							<td>{{ observable.name }}</td>
							<td>
								<katex-element
									v-if="observable.expression"
									:expression="observable.expression"
									:throw-on-error="false"
								/>
								<template v-else>--</template>
							</td>
							<td>
								<!-- TODO: needs to make those button active -->
								<Button icon="pi pi-check" text rounded aria-label="Save" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" />
							</td>
							<td v-if="extractions?.[observable?.id]" style="grid-column: 1 / span 4">
								<tera-model-extraction :extractions="extractions[observable.id]" />
							</td>
						</template>
						<template v-else>
							<td>{{ observable.id }}</td>
							<td>{{ observable.name }}</td>
							<td>
								<katex-element
									v-if="observable.expression"
									:expression="observable.expression"
									:throw-on-error="false"
								/>
								<template v-else>--</template>
							</td>
							<td>
								<template v-if="extractions?.[observable.id]">
									<Tag :value="extractions?.[observable.id].length" />
								</template>
								<template v-else>--</template>
							</td>
						</template>
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Transitions<span class="artifact-amount">({{ transitions.length }})</span>
				</template>
				<table v-if="transitions.length > 0" class="datatable" style="--columns: 6">
					<tr>
						<th>Id</th>
						<th>Name</th>
						<th>Input</th>
						<th>Output</th>
						<th>Expression</th>
						<th>Extractions</th>
					</tr>
					<tr
						v-for="(transition, index) in transitions"
						:key="index"
						:class="[
							{ active: isSectionEditable === `transition-${index}` },
							`transition-${index}`
						]"
					>
						<template v-if="isSectionEditable === `transition-${index}`">
							<td>{{ transition.id }}</td>
							<td>{{ transition.name }}</td>
							<td>{{ transition.input }}</td>
							<td>{{ transition.output }}</td>
							<td>
								<katex-element
									v-if="transition.expression"
									:expression="transition.expression"
									:throw-on-error="false"
								/>
								<template v-else>--</template>
							</td>
							<td>
								<!-- TODO: needs to make those button active -->
								<Button icon="pi pi-check" text rounded aria-label="Save" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" />
							</td>
							<td v-if="transition?.extractions" style="grid-column: 1 / span 5">
								<tera-model-extraction :extractions="transition?.extractions" />
							</td>
						</template>
						<template v-else>
							<td>{{ transition.id }}</td>
							<td>{{ transition.name }}</td>
							<td>{{ transition.input }}</td>
							<td>{{ transition.output }}</td>
							<td>
								<katex-element
									v-if="transition.expression"
									:expression="transition.expression"
									:throw-on-error="false"
								/>
								<template v-else>--</template>
							</td>
							<td>
								<Tag v-if="transition?.extractions" :value="extractions?.[transition.id].length" />
								<template v-else>--</template>
							</td>
						</template>
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Other concepts
					<span class="artifact-amount">({{ otherConcepts.length }})</span>
				</template>
				<table v-if="otherConcepts.length > 0" class="datatable" style="--columns: 4">
					<tr>
						<th>Payload id</th>
						<th>Names</th>
						<th>Descriptions</th>
						<th>Concept</th>
					</tr>
					<tr v-for="item in otherConcepts" :key="item.payload?.id?.id">
						<td>{{ item.payload?.id?.id }}</td>
						<td>{{ item.payload?.names?.map((n) => n?.name).join(', ') }}</td>
						<td>{{ item.payload?.descriptions?.map((d) => d?.source).join(', ') }}</td>
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
				</table>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Time
					<span class="artifact-amount">({{ time.length }})</span>
				</template>
				<table v-if="!isEmpty(time)" class="datatable" style="--columns: 3">
					<tr>
						<th>ID</th>
						<th>Units</th>
						<th>Extractions</th>
					</tr>
					<tr v-for="(item, index) in time" :key="index">
						<td>{{ item?.id }}</td>
						<td>{{ item?.units?.expression }}</td>
					</tr>
				</table>
			</AccordionTab>
		</Accordion>
	</main>
</template>

<script setup lang="ts">
import { round, groupBy, cloneDeep, isEmpty } from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import { DocumentAsset, Model, ModelConfiguration } from '@/types/Types';
import { logger } from '@/utils/logger';
import {
	updateConfigFields,
	updateParameterId
} from '@/model-representation/petrinet/petrinet-service';
import Tag from 'primevue/tag';
import { AcceptedExtensions, ResourceType } from '@/types/common';
import Button from 'primevue/button';
import TeraModelExtraction from '@/components/model/petrinet/tera-model-extraction.vue';
import * as textUtil from '@/utils/text';
import { getCuriesEntities } from '@/services/concept';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import { getManyProvenance } from '@/services/provenance';

// Used to keep track of the values of the current row being edited
interface ModelTableTypes {
	tableType: string;
	idx: number;
	updateProperty: { [key: string]: string };
}

const props = defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	highlight: string;
}>();

const emit = defineEmits(['update-model', 'fetch-model']);

function fetchAsset() {
	emit('fetch-model');
}

const isSectionEditable = ref<string | null>();
const isRowEditable = ref<string | null>();
const transientTableValue = ref<ModelTableTypes | null>(null);
const nameOfCurieCache = ref(new Map<string, string>());

const card = computed(() => {
	if (props.model.metadata?.card) {
		const cardWithUnknowns = props.model.metadata?.card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});
const description = computed(() =>
	highlightSearchTerms(props.model?.header?.description.concat(' ', card.value?.description ?? ''))
);
const usage = computed(() => card.value?.usage ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const documents = computed(
	() =>
		useProjects()
			.activeProject.value?.assets?.documents?.filter((document: DocumentAsset) =>
				[AcceptedExtensions.PDF, AcceptedExtensions.TXT, AcceptedExtensions.MD].some(
					(extension) => {
						if (document.fileNames && !isEmpty(document.fileNames)) {
							return document.fileNames[0]?.endsWith(extension);
						}
						return false;
					}
				)
			)
			.map((document: DocumentAsset) => ({
				name: document.name,
				id: document.id
			})) ?? []
);
const relatedDocuments = ref<{ name: string | undefined; id: string | undefined }[]>([]);
const time = computed(() =>
	props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []
);
const extractions = computed(() => {
	const attributes = props.model?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const states = computed(() => props.model?.model?.states ?? []);
const transitions = computed(() => {
	const results: any[] = [];
	if (props.model?.model?.transitions) {
		props.model.model.transitions.forEach((t) => {
			results.push({
				id: t.id,
				name: t?.properties?.name ?? '--',
				input: !isEmpty(t.input) ? t.input.sort().join(', ') : '--',
				output: !isEmpty(t.output) ? t.output.sort().join(', ') : '--',
				expression:
					props.model?.semantics?.ode.rates.find((rate) => rate.target === t.id)?.expression ??
					null,
				extractions: extractions?.[t.id] ?? null
			});
		});
	}
	return results;
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

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

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

// Toggle rows to become editable
function editRow(event: Event) {
	if (!event?.target) return;
	const row = (event.target as HTMLElement).closest('table.datatable tr');
	if (!row) return;
	isRowEditable.value = isRowEditable.value === row.className ? null : row.className;
}

async function confirmEdit() {
	if (props.model && transientTableValue.value) {
		const { tableType, idx, updateProperty } = transientTableValue.value;
		const modelClone = cloneDeep(props.model);

		switch (tableType) {
			case 'parameters':
				if (props.model.semantics?.ode.parameters) {
					Object.entries(updateProperty).forEach(([key, value]) => {
						modelClone.semantics!.ode.parameters![idx][key] = value;

						if (key === 'id') {
							const ode = props.model!.semantics!.ode;
							// update the parameter id in the model (as well as rate expression and expression_mathml)
							updateParameterId(modelClone, ode.parameters![idx][key], value as string);

							// note that this is making a call to an async function to update the different model configs
							// but we don't need to wait for it to finish because we don't need immediate access to the model configs
							if (!isEmpty(props.modelConfigurations)) {
								updateConfigFields(
									props.modelConfigurations,
									ode.parameters![idx][key],
									value as string
								);
							}
						}
					});
				}
				break;
			case 'states':
				Object.entries(updateProperty).forEach(([key, value]) => {
					if (key !== 'unit') {
						// TODO: remove this condition when we have proper editing of unit
						modelClone.model.states[idx][key] = value;
					}
					// TODO: update all of the properties affected by state id
				});
				break;
			default:
				logger.info(`${tableType} not recognized`);
		}
		emit('update-model', modelClone);
	}

	isRowEditable.value = null;
	transientTableValue.value = null;
}

function cancelEdit() {
	isRowEditable.value = null;
	transientTableValue.value = null;
}

function updateTable(tableType: string, idx: number, key: string, value: string) {
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

async function getRelatedDocuments() {
	const provenanceIds = props.model?.metadata?.provenance ?? [];

	if (isEmpty(provenanceIds)) return;

	// get provenace payload of all provenace ids on the asset
	const response = await getManyProvenance(provenanceIds);

	// get the right link of the provenace (these are all documents asset ids right now)
	const documentIds = response.map((prov) => prov?.right);

	// get all the documents on the current project
	const projectDocuments = useProjects().activeProject.value?.assets?.documents ?? [];

	// map the document ids from the provenace on the asset to the documents ids on the current project
	relatedDocuments.value =
		projectDocuments
			.filter((document: DocumentAsset) => documentIds.includes(document.id))
			.map((document: DocumentAsset) => ({
				name: document.name,
				id: document.id
			})) ?? [];
}

onMounted(() => getRelatedDocuments());

watch(
	() => props.model.metadata?.provenance,
	() => {
		getRelatedDocuments();
	}
);
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.p-message.p-message-info {
	max-width: 70rem;
}

.p-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	border-color: var(--primary-color);
	border-radius: var(--border-radius);
	border-width: 0px 0px 0px 6px;
	color: var(--text-color-primary);
}

table th {
	text-align: left;
}

table tr > td:empty:before {
	content: '--';
}

td.framework {
	text-transform: capitalize;
}

table.bibliography th,
table.bibliography td {
	font-family: var(--font-family);
	max-width: 15rem;
	padding-right: 1rem;
}

table.bibliography th {
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
}

table.bibliography td {
	font-weight: 400;
	font-size: var(--font-body-small);
}

table.datatable {
	display: flex;
	flex-direction: column;
}

table.datatable tr {
	align-items: center;
	border-bottom: 1px solid var(--surface-border-light);
	display: grid;
	grid-template-columns: repeat(var(--columns), calc(100% / var(--columns)));
	grid-auto-flow: row;
	justify-items: start;
}

table.datatable tr > td,
table.datatable tr > td {
	padding: 0.5rem;
}

table.datatable th {
	color: var(--text-color-light);
	font-size: var(--font-caption);
	font-weight: var(--font-weight-semibold);
	text-transform: uppercase;
}

table.datatable tr.active {
	background-color: var(--surface-secondary);
}

table.datatable input {
	width: 100%;
}
</style>
