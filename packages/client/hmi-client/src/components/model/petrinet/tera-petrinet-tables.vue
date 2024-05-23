<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<AccordionTab>
			<template #header>
				Initial variables<span class="artifact-amount">({{ initialsLength }})</span>
				<Button v-if="!readonly" @click.stop="cleanAndEmit" class="ml-auto">Save Changes</Button>
			</template>
			<tera-initial-table
				:model="transientModel"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-value="updateInitial"
				@update-model="(updateModel: Model) => (transientModel = updateModel)"
				:readonly="readonly"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
				<Button v-if="!readonly" @click.stop="cleanAndEmit" class="ml-auto">Save Changes</Button>
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
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(observables)" edit-mode="cell" data-key="id" :value="observables">
				<Column field="id" header="Symbol">
					<template #body="slotProps">
						<span class="latex-font">
							{{ slotProps.data.id }}
						</span>
					</template>
				</Column>
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
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(transitions)" data-key="id" :value="transitions">
				<Column field="id" header="Symbol">
					<template #body="slotProps">
						<span class="latex-font">
							{{ slotProps.data.id }}
						</span>
					</template>
				</Column>
				<Column field="name" header="Name" />
				<Column field="input" header="Input">
					<template #body="slotProps">
						<span class="latex-font">
							{{ slotProps.data.id }}
						</span>
					</template>
				</Column>
				<Column field="output" header="Output">
					<template #body="slotProps">
						<span class="latex-font">
							{{ slotProps.data.id }}
						</span>
					</template>
				</Column>
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
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Other concepts
				<span class="artifact-amount">({{ otherConcepts.length }})</span>
			</template>
			<tera-other-concepts-table
				:model="model"
				@update-model="(updatedModel) => emit('update-model', updatedModel)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Time
				<span class="artifact-amount">({{ time.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(time)" data-key="id" :value="time">
				<Column field="id" header="Symbol">
					<template #body="slotProps">
						<span class="latex-font">
							{{ slotProps.data.id }}
						</span>
					</template>
				</Column>
				<Column field="units.expression" header="Unit" />
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Initial, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import { cloneDeep, groupBy, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref, watch, onMounted } from 'vue';
import { Dictionary } from 'vue-gtag';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import { getMMT } from '@/services/model';
import Button from 'primevue/button';
import { cleanModel } from '@/model-representation/service';
import TeraOtherConceptsTable from './tera-other-concepts-table.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const emit = defineEmits(['update-model']);

const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

const transientModel = ref(cloneDeep(props.model));
const initialsLength = computed(() => props.model?.semantics?.ode?.initials?.length ?? 0);
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

	const gollmExtractions = props.model?.metadata?.gollmExtractions ?? [];
	unalignedExtractions.push(...gollmExtractions);

	return unalignedExtractions ?? [];
});

const updateInitial = (inits: Initial[]) => {
	const modelInitials = transientModel.value.semantics?.ode.initials ?? [];
	for (let i = 0; i < modelInitials.length; i++) {
		const foundInitial = inits.find((init) => init.target === modelInitials![i].target);
		if (foundInitial) {
			modelInitials[i] = foundInitial;
		}
	}
};

const updateParam = (params: ModelParameter[]) => {
	const modelParameters = transientModel.value.semantics?.ode.parameters ?? [];
	for (let i = 0; i < modelParameters.length; i++) {
		const foundParam = params.find((p) => p.id === modelParameters![i].id);
		if (foundParam) {
			modelParameters[i] = foundParam;
		}
	}
};

function updateMMT() {
	getMMT(props.model).then((response) => {
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	});
}

function cleanAndEmit() {
	const modelToClean = cloneDeep(transientModel.value);
	cleanModel(modelToClean);
	emit('update-model', modelToClean);
}

watch(
	() => props.model,
	(model) => {
		transientModel.value = cloneDeep(model);
		updateMMT();
	}
);

onMounted(() => updateMMT());
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.clickable-tag:hover {
	cursor: pointer;
}

:deep(.p-accordion-content:empty::before) {
	content: 'None';
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	margin-left: 1rem;
}
</style>
