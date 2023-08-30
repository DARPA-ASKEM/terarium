<template>
	<main>
		<section>
			<Message icon="none">
				This page describes the model. Use the content switcher above to see the diagram and manage
				configurations.
			</Message>
			<table>
				<tr>
					<th>Framework</th>
					<th>Model version</th>
					<th>Date created</th>
					<th>Created by</th>
					<th>Source</th>
				</tr>
				<tr>
					<td class="framework">{{ model?.schema_name }}</td>
					<td>{{ model?.model_version }}</td>
					<td>{{ model?.metadata?.processed_at }}</td>
					<td>{{ model?.metadata?.annotations?.authors?.join(', ') }}</td>
					<td>{{ model?.metadata?.processed_by }}</td>
					<td>{{ model?.metadata?.license }}</td>
				</tr>
			</table>
		</section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
			<AccordionTab>
				<template #header>Related publications</template>
				<tera-related-publications
					:publications="publications"
					:project="project"
					:asset-type="ResourceType.MODEL"
					:assetId="model.id"
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
							{ active: isRowEditable === `parameter-${parameter.id}` },
							`parameter-${parameter.id}`
						]"
					>
						<template v-if="isRowEditable === `parameter-${parameter.id}`">
							<div>
								<input
									type="text"
									:value="parameter?.id ?? '--'"
									@input="updateTable('parameters', i, 'id', $event.target?.['value'])"
								/>
							</div>
							<div>
								<input
									type="text"
									:value="parameter?.value ?? '--'"
									@input="updateTable('parameters', i, 'value', $event.target?.['value'])"
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
			<AccordionTab>
				<template #header>
					State variables<span class="artifact-amount">({{ states.length }})</span>
				</template>
				<main v-if="states.length > 0" class="datatable" style="--columns: 5">
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
									@input="updateTable('states', i, 'id', $event.target?.['value'])"
								/>
							</div>
							<div>
								<input
									type="text"
									:value="state?.name ?? '--'"
									@input="updateTable('states', i, 'name', $event.target?.['value'])"
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
						<!-- <div v-if="!isRowEditable">
								<Button icon="pi pi-file-edit" text @click="editRow" />
							</div>
							<div v-else-if="isRowEditable === `state-${state.id}`">
								<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
								<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
							</div> -->
					</section>
				</main>
			</AccordionTab>
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
								<Tag v-if="transition?.extractions" :value="extractions?.[transition.id].length" />
								<template v-else>--</template>
							</div>
						</template>
					</section>
				</main>
			</AccordionTab>
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
	</main>
</template>

<script setup lang="ts">
import { round, groupBy, cloneDeep, isEmpty } from 'lodash';
import { ref, computed } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';
import {
	updateConfigFields,
	updateParameterId
} from '@/model-representation/petrinet/petrinet-service';
import { ResourceType } from '@/types/common';
import { getModelConfigurations } from '@/services/model';
import Button from 'primevue/button';
import TeraModelExtraction from '@/components/models/tera-model-extraction.vue';
import * as textUtil from '@/utils/text';
import { getCuriesEntities } from '@/services/concept';
import TeraRelatedPublications from '@/components/widgets/tera-related-publications.vue';
import { IProject } from '@/types/Project';

// Used to keep track of the values of the current row being edited
interface ModelTableTypes {
	tableType: string;
	idx: number;
	updateProperty: { [key: string]: string };
}

const props = defineProps<{
	model: Model;
	highlight: string;
	project: IProject;
}>();

const emit = defineEmits(['update-model']);

const isSectionEditable = ref<string | null>();
const isRowEditable = ref<string | null>();
const transientTableValue = ref<ModelTableTypes | null>(null);
const nameOfCurieCache = ref(new Map<string, string>());

const description = computed(() => highlightSearchTerms(props.model?.description));
const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const publications = computed(() => []);
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
	const row = (event.target as HTMLElement).closest('.datatable section');
	if (!row) return;
	isRowEditable.value = isRowEditable.value === row.className ? null : row.className;
}

async function confirmEdit() {
	if (props.model && transientTableValue.value) {
		const { tableType, idx, updateProperty } = transientTableValue.value;
		const modelClone = cloneDeep(props.model);
		const modelConfigs = await getModelConfigurations(props.model.id);

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
							if (modelConfigs) {
								updateConfigFields(modelConfigs, ode.parameters![idx][key], value as string);
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
	padding-right: 2rem;
	font-family: var(--font-family);
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	text-align: left;
}

table td {
	padding-right: 50px;
	font-family: var(--font-family);
	font-weight: 400;
	font-size: var(--font-body-small);
}

table tr > td:empty:before {
	content: '--';
}

table .framework {
	text-transform: capitalize;
}

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
</style>
