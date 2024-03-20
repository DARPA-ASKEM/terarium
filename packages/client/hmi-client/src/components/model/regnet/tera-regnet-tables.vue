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
			<template #header>
				<Button v-if="!readonly" @click.stop="emit('update-model', transientModel)" class="ml-auto"
					>Save Changes</Button
				>
			</template>
			<tera-parameter-table
				:model="transientModel"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-value="updateParam"
				@update-model="(updatedModel: Model) => (transientModel = updatedModel)"
				:readonly="readonly"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { DKG, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import { cloneDeep, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import {
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	parseCurie
} from '@/services/concept';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import { getMMT } from '@/services/model';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import Button from 'primevue/button';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const transientModel = ref(cloneDeep(props.model));
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const vertices = computed(() => props.model?.model?.vertices ?? []);
const edges = computed(() => props.model.model?.edges ?? []);
const nameOfCurieCache = ref(new Map<string, string>());
const curies = ref<DKG[]>([]);
const conceptSearchTerm = ref('');

const emit = defineEmits(['update-model']);

const updateParam = (params: ModelParameter[]) => {
	const modelParameters = transientModel.value.model?.parameters ?? [];
	for (let i = 0; i < modelParameters.length; i++) {
		const foundParam = params.find((p) => p.id === modelParameters![i].id);
		if (foundParam) {
			modelParameters[i] = foundParam;
		}
	}
};

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

watch(
	() => props.model,
	async (model) => {
		transientModel.value = cloneDeep(model);
		const response: any = await getMMT(model);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	margin-left: 1rem;
}
</style>
