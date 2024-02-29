<template>
	<Accordion multiple :active-index="[0, 1, 2]">
		<AccordionTab header="Vertices">
			<DataTable
				v-if="!isEmpty(vertices)"
				data-key="id"
				:value="vertices"
				v-on:cell-edit-complete="onCellEditComplete"
				edit-mode="cell"
			>
				<Column field="id" header="Symbol" />
				<Column field="name" header="Name" />
				<Column field="rate_constant" header="Rate Constant" />
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
					<template #editor="{ data }">
						<AutoComplete
							v-if="!readonly"
							v-model="conceptSearchTerm"
							:suggestions="curies"
							@complete="onSearch"
							@item-select="
								updateVertex(data.id, 'grounding', {
									...data.grounding,
									identifiers: parseCurie($event.value?.curie)
								})
							"
							optionLabel="name"
							:forceSelection="true"
							:inputStyle="{ width: '100%' }"
						/>
					</template>
				</Column>
			</DataTable>
		</AccordionTab>
		<AccordionTab header="Edges">
			<DataTable v-if="!isEmpty(edges)" data-key="id" :value="edges">
				<Column field="id" header="Symbol" />
				<Column field="source" header="Source" />
				<Column field="target" header="Target" />
				<Column field="properties.rate_constant" header="Rate Constant" />
			</DataTable>
		</AccordionTab>
		<AccordionTab header="Parameters">
			<DataTable v-if="!isEmpty(parameters)" data-key="id" :value="parameters" edit-mode="cell">
				<Column field="id" header="Symbol" />
				<Column field="value" header="Value">
					<template v-if="!readonly" #editor="{ data }">
						<InputText
							:value="data?.value"
							@input="updateParameter(data?.id, 'value', $event.target?.['value'])"
						/>
					</template>
				</Column>
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { DKG, Model, ModelConfiguration } from '@/types/Types';
import { cloneDeep, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import {
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	parseCurie
} from '@/services/concept';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const vertices = computed(() => props.model?.model?.vertices ?? []);
const edges = computed(() => props.model.model?.edges ?? []);
const parameters = computed(() => props.model?.model?.parameters ?? []);
const nameOfCurieCache = ref(new Map<string, string>());
const curies = ref<DKG[]>([]);
const conceptSearchTerm = ref('');

const emit = defineEmits(['update-model']);

function updateParameter(id: string, key: string, value: string) {
	const model = cloneDeep(props.model);
	model.model.parameters.find((p) => p.id === id)[key] = value;
	emit('update-model', model);
}

function updateVertex(id: string, key: string, value: any) {
	const model = cloneDeep(props.model);
	model.model.vertices.find((v) => v.id === id)[key] = value;
	emit('update-model', model);
}

async function onSearch(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) {
		const response = await searchCuriesEntities(query);
		curies.value = response;
	}
}

function onCellEditComplete() {
	conceptSearchTerm.value = '';
}
</script>

<style scoped>
section {
	margin-left: 1rem;
}
</style>
