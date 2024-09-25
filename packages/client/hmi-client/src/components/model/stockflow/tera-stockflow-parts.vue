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
				:feature-config="featureConfig"
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
				:feature-config="featureConfig"
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
			<tera-time v-if="time" :time="time" :feature-config="featureConfig" @update-time="emit('update-time', $event)" />
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model } from '@/types/Types';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed } from 'vue';
import { stringToLatexExpression } from '@/services/model';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { MiraModel, MiraTemplateParams, ObservableSummary } from '@/model-representation/mira/mira-common';
import TeraStates from '@/components/model/model-parts/tera-states.vue';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraTime from '@/components/model/model-parts/tera-time.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	observableSummary: ObservableSummary;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-time']);

const initialsLength = computed(() => props.model?.semantics?.ode?.initials?.length ?? 0);
const parametersLength = computed(
	() => (props.model?.semantics?.ode.parameters?.length ?? 0) + (props.model?.model?.auxiliaries?.length ?? 0)
);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));

const flows = computed(() => props.model?.model?.flows ?? []);
</script>
