<template>
	<Accordion multiple :active-index="currentActiveIndicies">
		<AccordionTab>
			<template #header>
				Initial variables<span class="artifact-amount">({{ initialsLength }})</span>
			</template>
			<tera-model-part
				v-if="!isEmpty(mmt.initials)"
				:model="model"
				:items="stateList"
				:feature-config="featureConfig"
				@update-state="emit('update-state', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parametersLength }})</span>
			</template>
			<tera-model-part
				v-if="!isEmpty(mmt.parameters)"
				:items="parameterList"
				:feature-config="featureConfig"
				@update-parameter="emit('update-parameter', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
			</template>
			<tera-model-part
				v-if="!isEmpty(observables)"
				:items="observablesList"
				:feature-config="featureConfig"
				@update-item="emit('update-observable', $event)"
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
							:expression="stringToLatexExpression(data.rate_expression)"
							:throw-on-error="false"
						/>
						<template v-else>--</template>
					</template>
				</Column>
			</DataTable>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Time
				<span class="artifact-amount">({{ time.length }})</span>
			</template>
			<tera-model-part
				v-if="time"
				is-time-part
				:items="timeList"
				:feature-config="featureConfig"
				@update-time="emit('update-time', $event)"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model } from '@/types/Types';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import { stringToLatexExpression } from '@/services/model';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { MiraModel, MiraTemplateParams, ObservableSummary } from '@/model-representation/mira/mira-common';
import type { FeatureConfig } from '@/types/common';
import { createTimeList, createObservablesList, createPartsList, PART_TYPE } from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	observableSummary: ObservableSummary;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-time']);

const currentActiveIndicies = ref([0, 1, 2, 3, 4, 5]);
const initialsLength = computed(() => props.model?.semantics?.ode?.initials?.length ?? 0);
const stateList = computed(() => createPartsList(props.model?.semantics?.ode?.initials, props.model, PART_TYPE.STATE));
const parametersLength = computed(
	() => (props.model?.semantics?.ode.parameters?.length ?? 0) + (props.model?.model?.auxiliaries?.length ?? 0)
);
const parameterList = computed(() => createPartsList(props.mmtParams, props.model, PART_TYPE.PARAMETER));
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const observablesList = computed(() => createObservablesList(observables));
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));
const timeList = computed(() => createTimeList(time));
const flows = computed(() => props.model?.model?.flows ?? []);
</script>
