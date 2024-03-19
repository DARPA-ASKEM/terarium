<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<!-- Initial variables -->
		<AccordionTab>
			<template #header>
				Initial variables<span class="artifact-amount">({{ states.length }})</span>
				<Button
					v-if="!readonly && editModeVariables"
					@click.stop="
						emit('update-model', transientModel);
						editModeVariables = false;
					"
					class="ml-auto"
					>Save changes</Button
				>
				<Button
					v-if="!readonly && !editModeVariables"
					@click.stop="editModeVariables = true"
					outlined
					severity="secondary"
					class="ml-auto"
					>Edit</Button
				>
			</template>
			<tera-initial-table
				:model="transientModel"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-value="updateInitial"
				@update-model="(updateModel: Model) => (transientModel = updateModel)"
				:readonly="readonly"
				:editMode="editModeVariables"
			/>
		</AccordionTab>

		<!-- Parameters -->
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters?.length }})</span>
				<Button
					v-if="!readonly && editModeParameters"
					@click.stop="
						emit('update-model', transientModel);
						editModeParameters = false;
					"
					class="ml-auto"
					>Save changes</Button
				>
				<Button
					v-if="!readonly && !editModeParameters"
					@click.stop="editModeParameters = true"
					severity="secondary"
					outlined
					class="ml-auto"
					>Edit</Button
				>
			</template>
			<tera-parameter-table
				:model="transientModel"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-value="updateParam"
				@update-model="(updatedModel: Model) => (transientModel = updatedModel)"
				:readonly="readonly"
				:editMode="editModeParameters"
			/>
		</AccordionTab>

		<!-- Observables -->
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(observables)" edit-mode="cell" data-key="id" :value="observables">
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
			</DataTable>
		</AccordionTab>

		<!-- Transitions -->
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(transitions)" data-key="id" :value="transitions">
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
			</DataTable>
		</AccordionTab>

		<!-- Other concepts -->
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
			</DataTable>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Initial, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import { cloneDeep, groupBy, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref, watch } from 'vue';
import { Dictionary } from 'vue-gtag';
import { getCurieUrl } from '@/services/concept';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraParameterTable from '@/components/model/petrinet/tera-parameter-table.vue';
import TeraInitialTable from '@/components/model/petrinet/tera-initial-table.vue';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import { getMMT } from '@/services/model';
import Button from 'primevue/button';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	readonly?: boolean;
}>();

const editModeVariables = ref(false);
const editModeParameters = ref(false);

const emit = defineEmits(['update-model']);

const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});

const transientModel = ref(cloneDeep(props.model));
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

watch(
	() => props.model,
	async (model) => {
		if (!model) return;
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
