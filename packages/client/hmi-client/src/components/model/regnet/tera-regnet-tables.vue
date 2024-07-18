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
				<Column field="initial" header="Initial Value" />
				<Column field="grounding.identifiers" header="Concept">
					<template #body="{ data }">
						<template v-if="data?.grounding?.identifiers && !isEmpty(data.grounding.identifiers)">
							{{
								getNameOfCurieCached(
									nameOfCurieCache,
									getCurieFromGroundingIdentifier(data.grounding.identifiers)
								)
							}}

							<a
								target="_blank"
								rel="noopener noreferrer"
								:href="getCurieUrl(getCurieFromGroundingIdentifier(data.grounding.identifiers))"
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
			<tera-parameters
				v-if="!isEmpty(mmt.parameters)"
				:model="model"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-parameter="emit('update-parameter', $event)"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { DKG, Model } from '@/types/Types';
import { cloneDeep, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import {
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroundingIdentifier,
	getCurieUrl,
	parseCurie
} from '@/services/concept';
import type {
	MiraModel,
	MiraTemplateParams,
	ObservableSummary
} from '@/model-representation/mira/mira-common';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	observableSummary: ObservableSummary;
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model', 'update-parameter']);

const vertices = computed(() => props.model?.model?.vertices ?? []);
const edges = computed(() => props.model.model?.edges ?? []);
const nameOfCurieCache = ref(new Map<string, string>());
const curies = ref<DKG[]>([]);
const conceptSearchTerm = ref('');

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
