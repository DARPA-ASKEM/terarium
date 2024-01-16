<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
			</template>
			<table v-if="states.length > 0" class="datatable" style="--columns: 5">
				<tr>
					<th>Symbol</th>
					<th>Name</th>
					<th>Unit</th>
					<th>Concept</th>
					<th>Extractions</th>
				</tr>
				<tr
					v-for="(state, i) in states"
					:key="state.id"
					:class="[
						{ active: isRowEditable === `${VariableTypes.STATE}-${state.id}` },
						`${VariableTypes.STATE}-${state.id}`
					]"
				>
					<template v-if="isRowEditable === `${VariableTypes.STATE}-${state.id}`">
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
								<Tag
									class="clickable-tag"
									:value="extractions?.[state?.id].length"
									@click="openExtractions(VariableTypes.STATE, state?.id)"
								/>
							</template>
							<template v-else>--</template>
						</td>
					</template>
					<td
						v-if="shouldShowExtractions(VariableTypes.STATE, state.id)"
						style="grid-column: 1 / span 4"
					>
						<tera-model-extraction :extractions="extractions[state.id]" />
					</td>
				</tr>
			</table>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
			</template>
			<table
				v-if="parameters.length > 0"
				class="datatable"
				:style="{ '--columns': readonly ? 5 : 6 }"
			>
				<tr>
					<th>Symbol</th>
					<th>Name</th>
					<th>Value</th>
					<th>Distribution</th>
					<th>Extractions</th>
				</tr>
				<tr
					v-for="(parameter, i) in parameters"
					:key="parameter.id"
					:class="[
						{ active: isRowEditable === `${VariableTypes.PARAMETER}-${parameter.id}` },
						`${VariableTypes.PARAMETER}-${parameter.id}`
					]"
				>
					<template v-if="isRowEditable === `${VariableTypes.PARAMETER}-${parameter.id}`">
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
								:value="parameter?.name ?? '--'"
								@input="updateTable('parameters', i, 'name', $event.target?.['value'])"
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
						<td>{{ parameter?.name }}</td>
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
								<Tag
									class="clickable-tag"
									:value="extractions?.[parameter?.id].length"
									@click="openExtractions(VariableTypes.PARAMETER, parameter?.id)"
								/>
							</template>
							<template v-else>--</template>
						</td>
					</template>
					<td v-if="!readonly && !isRowEditable">
						<Button icon="pi pi-file-edit" text @click="editRow" />
					</td>
					<td v-else-if="isRowEditable === `${VariableTypes.PARAMETER}-${parameter.id}`">
						<Button icon="pi pi-check" text rounded aria-label="Save" @click="confirmEdit" />
						<Button icon="pi pi-times" text rounded aria-label="Discard" @click="cancelEdit" />
					</td>
					<td
						v-if="shouldShowExtractions(VariableTypes.PARAMETER, parameter.id)"
						style="grid-column: 1 / span 4"
					>
						<tera-model-extraction :extractions="extractions[parameter.id]" />
					</td>
				</tr>
			</table>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<table v-if="!isEmpty(observables)" class="datatable" style="--columns: 4">
				<tr>
					<th>Symbol</th>
					<th>Name</th>
					<th>Expression</th>
					<th>Extractions</th>
				</tr>
				<tr
					v-for="observable in observables"
					:key="observable.id"
					:class="[
						{ active: isSectionEditable === `${VariableTypes.OBSERVABLE}-${observable.id}` },
						`observable-${observable.id}`
					]"
				>
					<template v-if="isSectionEditable === `${VariableTypes.OBSERVABLE}-${observable.id}`">
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
								<Tag
									class="clickable-tag"
									:value="extractions?.[observable?.id].length"
									@click="openExtractions(VariableTypes.OBSERVABLE, observable?.id)"
								/>
							</template>
							<template v-else>--</template>
						</td>
						<td
							v-if="shouldShowExtractions(VariableTypes.OBSERVABLE, observable.id)"
							style="grid-column: 1 / span 4"
						>
							<tera-model-extraction :extractions="extractions[observable.id]" />
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
					<th>Symbol</th>
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
						{ active: isSectionEditable === `${VariableTypes.TRANSITION}-${index}` },
						`transition-${index}`
					]"
				>
					<template v-if="isSectionEditable === `${VariableTypes.TRANSITION}-${index}`">
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
							<template v-if="transition?.extractions">
								<Tag
									class="clickable-tag"
									:value="extractions?.[transition?.id].length"
									@click="openExtractions(VariableTypes.TRANSITION, transition?.id)"
								/>
							</template>
							<template v-else>--</template>
						</td>
						<td
							v-if="shouldShowExtractions(VariableTypes.TRANSITION, transition.id)"
							style="grid-column: 1 / span 4"
						>
							<tera-model-extraction :extractions="extractions[transition.id]" />
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
			<table v-if="otherConcepts.length > 0" class="datatable" style="--columns: 5">
				<tr>
					<th>Payload id</th>
					<th>Names</th>
					<th>Values</th>
					<th>Descriptions</th>
					<th>Concept</th>
				</tr>
				<tr v-for="item in otherConcepts" :key="item.payload?.id?.id">
					<td>{{ item.payload?.id?.id }}</td>
					<td>
						{{
							item.payload?.names?.map((n) => n?.name).join(', ') ||
							item.payload?.mentions?.map((m) => m?.name).join(', ')
						}}
					</td>
					<td>
						{{
							item.payload?.values?.map((n) => n?.value?.amount).join(', ') ||
							item.payload?.value_descriptions?.map((m) => m?.value?.amount).join(', ')
						}}
					</td>
					<td>
						{{
							item.payload?.descriptions?.map((d) => d?.source).join(', ') ||
							item.payload?.text_descriptions?.map((d) => d?.description).join(', ')
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
			</table>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Time
				<span class="artifact-amount">({{ time.length }})</span>
			</template>
			<table v-if="!isEmpty(time)" class="datatable" style="--columns: 3">
				<tr>
					<th>Symbol</th>
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
</template>

<script setup lang="ts">
import type { Model, ModelConfiguration } from '@/types/Types';
import { cloneDeep, groupBy, isEmpty, round } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import { Dictionary } from 'vue-gtag';
import {
	updateConfigFields,
	updateParameterId
} from '@/model-representation/petrinet/petrinet-service';
import { logger } from '@/utils/logger';
import { getCuriesEntities } from '@/services/concept';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import TeraModelExtraction from './tera-model-extraction.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model']);

enum VariableTypes {
	STATE = 'state',
	OBSERVABLE = 'observable',
	PARAMETER = 'parameter',
	TRANSITION = 'transition'
}

// Used to keep track of the values of the current row being edited
interface ModelTableTypes {
	tableType: string;
	idx: number;
	updateProperty: { [key: string]: string };
}

const isSectionEditable = ref<string | null>();
const isRowEditable = ref<string | null>();
const showExtractions = ref<string | null>();
const transientTableValue = ref<ModelTableTypes | null>(null);
const nameOfCurieCache = ref(new Map<string, string>());

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
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
				input: !isEmpty(t.input) ? t.input.join(', ') : '--',
				output: !isEmpty(t.output) ? t.output.join(', ') : '--',
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

	// find keys that are not aligned
	const unalignedKeys = Object.keys(extractions.value).filter((k) => !ids.includes(k));

	let unalignedExtractions: Dictionary<any>[] = [];
	unalignedKeys.forEach((key) => {
		unalignedExtractions = unalignedExtractions.concat(
			extractions.value[key.toString()].filter((e) =>
				['anchored_extraction', 'anchored_entity'].includes(e.type)
			)
		);
	});

	return unalignedExtractions ?? [];
});

function openExtractions(variableType: VariableTypes, id: string) {
	if (showExtractions.value === `${variableType}-${id}`) {
		showExtractions.value = null;
		return;
	}

	showExtractions.value = `${variableType}-${id}`;
}

function shouldShowExtractions(variableType: VariableTypes, id: string) {
	return (
		showExtractions.value === `${variableType}-${id}` &&
		extractions.value[id] &&
		isRowEditable.value !== `${variableType}-${id}` &&
		isSectionEditable.value !== `${variableType}-${id}`
	);
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
									props.modelConfigurations!,
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
</script>

<style scoped>
section {
	margin-left: 1rem;
}

table th {
	text-align: left;
}

table tr > td:empty:before {
	content: '--';
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

.clickable-tag:hover {
	cursor: pointer;
}
</style>
