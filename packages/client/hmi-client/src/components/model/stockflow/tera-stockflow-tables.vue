<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<AccordionTab>
			<template #header>
				Initial variables<span class="artifact-amount">({{ initialsLength }})</span>
			</template>
			<tera-states
				v-if="!isEmpty(mmt.initials)"
				:model="model"
				:mmt="mmt"
				@update-state="emit('update-state', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parametersLength }})</span>
			</template>
			<tera-parameters
				v-if="!isEmpty(mmt.parameters)"
				:model="model"
				:mmt="mmt"
				:mmt-params="mmtParams"
				@update-parameter="emit('update-parameter', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<tera-observables
				v-if="!isEmpty(observables)"
				:model="model"
				:mmt="mmt"
				:observables="observables"
				@update-variable="emit('update-observable', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Flows<span class="artifact-amount">({{ flows.length }})</span>
			</template>
			<DataTable v-if="!isEmpty(flows)" data-key="id" :value="flows">
				<Column field="id" header="ID" />
				<Column field="name" header="Name" />
				<Column field="upstream_stock" header="Upstream stock" />
				<Column field="downstream_stock" header="Downstream stock" />
				<Column field="rate_expression" header="Rate expression">
					<template #body="{ data }">
						<katex-element
							v-if="data.rate_expression"
							:expression="data.rate_expression"
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
			<tera-time v-if="time" :time="time" @update-time="emit('update-time', $event)" />
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model } from '@/types/Types';
import { groupBy, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed } from 'vue';
import { Dictionary } from 'vue-gtag';
import { getCurieUrl } from '@/services/concept';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type {
	MiraModel,
	MiraTemplateParams,
	ObservableSummary
} from '@/model-representation/mira/mira-common';
import TeraStates from '@/components/model/model-parts/tera-states.vue';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraTime from '@/components/model/model-parts/tera-time.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	observableSummary: ObservableSummary;
	readonly?: boolean;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-time']);

const initialsLength = computed(() => props.model?.semantics?.ode?.initials?.length ?? 0);
const parametersLength = computed(
	() =>
		(props.model?.semantics?.ode.parameters?.length ?? 0) +
		(props.model?.model?.auxiliaries?.length ?? 0)
);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() =>
	props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []
);
const extractions = computed(() => {
	const attributes = props.model?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const stocks = computed(() => props.model?.model?.stocks ?? []);
const flows = computed(() => props.model?.model?.flows ?? []);
const otherConcepts = computed(() => {
	const ids = [...(stocks.value?.map((s) => s.id) ?? []), ...(flows.value?.map((f) => f.id) ?? [])];

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
</script>
