<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
			</template>

			<DataTable
				v-if="!isEmpty(states)"
				:edit-mode="readonly ? undefined : 'cell'"
				data-key="id"
				:value="states"
				@cell-edit-complete="onCellEditComplete"
				v-model:expanded-rows="expandedRows[VariableTypes.STATE]"
			>
				<Column field="id" header="Symbol" />
				<Column field="name" header="Name" />
				<Column field="units.expression" header="Unit" />
				<Column field="grounding.identifiers" header="Concept">
					<template #body="{ data }">
						<template v-if="data?.grounding?.identifiers && !isEmpty(data.grounding.identifiers)">
							{{
								getNameOfCurieCached(
									nameOfCurieCache,
									getCurieFromGroudingIdentifier(data.grounding.identifiers)
								)
							}}

							<a
								target="_blank"
								rel="noopener noreferrer"
								:href="getCurieUrl(getCurieFromGroudingIdentifier(data.grounding.identifiers))"
								@click.stop
								aria-label="Open Concept"
							>
								<i class="pi pi-external-link" />
							</a>
						</template>
						<template v-else>--</template>
					</template>
					<template #editor="{ index }">
						<AutoComplete
							v-model="conceptSearchTerm.name"
							:suggestions="curies"
							@complete="onSearch"
							@item-select="
								updateTable('states', index, 'grounding', {
									identifiers: parseCurie($event.value?.curie)
								})
							"
							optionLabel="name"
							:forceSelection="true"
							:inputStyle="{ width: '100%' }"
						/>
					</template>
				</Column>
				<Column header="Extractions">
					<template #body="{ data }">
						<template v-if="extractions?.[data?.id]">
							<Tag
								class="clickable-tag"
								:value="extractions?.[data?.id].length"
								@click="openExtractions(VariableTypes.STATE, data)"
							/>
						</template>
						<template v-else>--</template>
					</template>
				</Column>
				<template #expansion="{ data }">
					<tera-model-extraction :extractions="extractions[data.id]" />
				</template>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
			</template>
			<DataTable
				v-if="!isEmpty(parameters)"
				:edit-mode="readonly ? undefined : 'cell'"
				data-key="id"
				:value="parameters"
				:expanded-rows="expandedRows[VariableTypes.PARAMETER]"
			>
				<Column field="id" header="Symbol">
					<template #editor="{ data, index }">
						<InputText
							:value="data?.id ?? '--'"
							@input="updateTable('parameters', index, 'id', $event.target?.['value'])"
						/>
					</template>
				</Column>
				<Column field="name" header="Name">
					<template #editor="{ data, index }">
						<InputText
							:value="data?.name ?? '--'"
							@input="updateTable('parameters', index, 'name', $event.target?.['value'])"
						/>
					</template>
				</Column>
				<Column field="value" header="Value">
					<template #editor="{ data, index }">
						<InputText
							:value="data?.value ?? '--'"
							@input="updateTable('parameters', index, 'value', $event.target?.['value'])"
						/>
					</template>
				</Column>
				<Column field="distribution.parameters" header="Distribution">
					<template #body="{ data }">
						<template v-if="data?.distribution?.parameters">
							[{{ round(data?.distribution?.parameters.minimum, 4) }},
							{{ round(data?.distribution?.parameters.maximum, 4) }}]
						</template>
						<template v-else>--</template>
					</template>
				</Column>
				<Column header="Extractions">
					<template #body="{ data }">
						<template v-if="extractions?.[data?.id]">
							<Tag
								class="clickable-tag"
								:value="extractions?.[data?.id].length"
								@click="openExtractions(VariableTypes.PARAMETER, data)"
							/>
						</template>
						<template v-else>--</template>
					</template>
				</Column>
				<template #expansion="{ data }">
					<tera-model-extraction :extractions="extractions[data.id]" />
				</template>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<DataTable
				v-if="!isEmpty(observables)"
				edit-mode="cell"
				data-key="id"
				:value="observables"
				:expanded-rows="expandedRows[VariableTypes.OBSERVABLE]"
			>
				<Column field="id" header="Symbol" />
				<Column field="name" header="Name" />
				<Column field="expression" header="Expression">
					<template #body="{ data }">
						<katex-element
							v-if="data.expression"
							:expression="data.expression"
							:throw-on-error="false"
						/>
						<template v-else>--</template>
					</template>
				</Column>
				<Column header="Extractions">
					<template #body="{ data }">
						<template v-if="extractions?.[data?.id]">
							<Tag
								class="clickable-tag"
								:value="extractions?.[data?.id].length"
								@click="openExtractions(VariableTypes.OBSERVABLE, data)"
							/>
						</template>
						<template v-else>--</template>
					</template>
				</Column>
				<template #expansion="{ data }">
					<tera-model-extraction :extractions="extractions[data.id]" />
				</template>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
			</template>
			<DataTable
				v-if="!isEmpty(transitions)"
				data-key="id"
				:value="transitions"
				:expanded-rows="expandedRows[VariableTypes.TRANSITION]"
			>
				<Column field="id" header="Symbol" />
				<Column field="name" header="Name" />
				<Column field="input" header="Input" />
				<Column field="output" header="Output" />
				<Column field="expression" header="Expression">
					<template #body="{ data }">
						<katex-element
							v-if="data.expression"
							:expression="data.expression"
							:throw-on-error="false"
						/>
						<template v-else>--</template>
					</template>
				</Column>
				<Column header="Extractions">
					<template #body="{ data }">
						<template v-if="extractions?.[data?.id]">
							<Tag
								class="clickable-tag"
								:value="extractions?.[data?.id].length"
								@click="openExtractions(VariableTypes.TRANSITION, data)"
							/>
						</template>
						<template v-else>--</template>
					</template>
				</Column>
				<template #expansion="{ data }">
					<tera-model-extraction :extractions="extractions[data.id]" />
				</template>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Other concepts
				<span class="artifact-amount">({{ otherConcepts.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(otherConcepts)" data-key="id" :value="otherConcepts">
				<Column field="payload.id.id" header="Payload id" />
				<Column header="Names">
					<template #body="{ data }">
						{{
							data.payload?.names?.map((n) => n?.name).join(', ') ||
							data.payload?.mentions?.map((m) => m?.name).join(', ') ||
							'--'
						}}
					</template>
				</Column>
				<Column header="Values">
					<template #body="{ data }">
						{{
							data.payload?.values?.map((n) => n?.value?.amount).join(', ') ||
							data.payload?.value_descriptions?.map((m) => m?.value?.amount).join(', ') ||
							'--'
						}}
					</template>
				</Column>
				<Column header="Descriptions">
					<template #body="{ data }">
						{{
							data.payload?.descriptions?.map((d) => d?.source).join(', ') ||
							data.payload?.text_descriptions?.map((d) => d?.description).join(', ') ||
							'--'
						}}
					</template>
				</Column>
				<Column field="payload.groundings" header="Concept">
					<template #body="{ data }">
						<template v-if="!data?.payload?.groundings || data?.payload?.groundings.length < 1"
							>--</template
						>
						<template
							v-else
							v-for="grounding in data?.payload?.groundings"
							:key="grounding.grounding_id"
						>
							{{ grounding.grounding_text }}
							<a
								target="_blank"
								rel="noopener noreferrer"
								:href="getCurieUrl(grounding.grounding_id)"
								aria-label="Open Concept"
							>
								<i class="pi pi-external-link" />
							</a>
						</template>
					</template>
				</Column>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Time
				<span class="artifact-amount">({{ time.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(time)" data-key="id" :value="time">
				<Column field="id" header="Symbol" />
				<Column field="units.expression" header="Unit" />
				<Column header="Extractions" />
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { DKG, Model, ModelConfiguration } from '@/types/Types';
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
import {
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl
} from '@/services/concept';
import Tag from 'primevue/tag';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import InputText from 'primevue/inputtext';
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

const isRowEditable = ref<string | null>();
const transientTableValue = ref<ModelTableTypes | null>(null);
const nameOfCurieCache = ref(new Map<string, string>());

const curies = ref<DKG[]>([]);
const conceptSearchTerm = ref({
	curie: '',
	name: ''
});
const expandedRows = ref<{
	[VariableTypes.STATE]: any[];
	[VariableTypes.PARAMETER]: any[];
	[VariableTypes.OBSERVABLE]: any[];
	[VariableTypes.TRANSITION]: any[];
}>({
	[VariableTypes.STATE]: [],
	[VariableTypes.PARAMETER]: [],
	[VariableTypes.OBSERVABLE]: [],
	[VariableTypes.TRANSITION]: []
});
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
					props.model?.semantics?.ode?.rates?.find((rate) => rate.target === t.id)?.expression ??
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

function openExtractions(variableType: VariableTypes, data: any) {
	const clonedExpandedRows = cloneDeep(expandedRows.value);

	if (clonedExpandedRows[variableType].find((row: any) => row.id === data.id)) {
		clonedExpandedRows[variableType] = clonedExpandedRows[variableType].filter(
			(row: any) => row.id !== data.id
		);
	} else {
		clonedExpandedRows[variableType].push(data);
	}

	expandedRows.value = clonedExpandedRows;
}

function updateTable(tableType: string, idx: number, key: string, value: any) {
	transientTableValue.value = {
		...transientTableValue.value,
		tableType,
		idx,
		updateProperty: {
			...transientTableValue.value?.updateProperty,
			[key]: value
		}
	};
	confirmEdit();
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

async function onSearch(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) {
		const response = await searchCuriesEntities(query);
		curies.value = response;
	}
}

function parseCurie(curie: string) {
	const key = curie.split(':')[0];
	const value = curie.split(':')[1];
	return { [key]: value };
}

function onCellEditComplete() {
	conceptSearchTerm.value = { curie: '', name: '' };
}
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}
</style>
