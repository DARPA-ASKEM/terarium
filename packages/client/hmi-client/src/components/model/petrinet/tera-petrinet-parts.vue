<template>
	<Accordion multiple :active-index="currentActiveIndexes">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
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
				Parameters<span class="artifact-amount">({{ parameters.length }})</span>
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
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
			</template>
			<tera-transitions
				v-if="!isEmpty(transitions) && !isEmpty(mmt.templates)"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:transitions="transitions"
				:feature-config="featureConfig"
				@update-transition="emit('update-transition', $event)"
			/>
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
import type { Model, Transition, State } from '@/types/Types';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import TeraStates from '@/components/model/model-parts/tera-states.vue';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraTransitions from '@/components/model/model-parts/tera-transitions.vue';
import TeraTime from '@/components/model/model-parts/tera-time.vue';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-transition', 'update-time']);

// Keep track of active indexes using ref
const currentActiveIndexes = ref([0, 1, 2, 3, 4]);

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));

const states = computed<State[]>(() => props.model?.model?.states ?? []);
const transitions = computed<Transition[]>(() =>
	props.model.model.transitions?.map((transition: Transition) => ({
		...transition,
		name: transition.name ?? transition.properties?.name ?? transition.id,
		expression: props.model?.semantics?.ode?.rates?.find((rate) => rate.target === transition.id)?.expression
	}))
);
</script>
