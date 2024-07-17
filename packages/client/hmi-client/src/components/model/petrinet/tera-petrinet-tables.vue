<template>
	<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
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
				Parameters<span class="artifact-amount">({{ parameters.length }})</span>
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
				@update-transition="emit('update-transition', $event)"
			/>
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
			<tera-time v-if="time" :time="time" @update-time="emit('update-time', $event)" />
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model, Transition, State } from '@/types/Types';
import { groupBy, isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed } from 'vue';
import { Dictionary } from 'vue-gtag';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import TeraStates from '@/components/model/model-parts/tera-states.vue';
import TeraParameters from '@/components/model/model-parts/tera-parameters.vue';
import TeraObservables from '@/components/model/model-parts/tera-observables.vue';
import TeraTransitions from '@/components/model/model-parts/tera-transitions.vue';
import TeraTime from '@/components/model/model-parts/tera-time.vue';
import TeraOtherConceptsTable from './tera-other-concepts-table.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	readonly?: boolean;
}>();

const emit = defineEmits([
	'update-model',
	'update-state',
	'update-parameter',
	'update-observable',
	'update-transition',
	'update-time'
]);

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() =>
	props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []
);
const extractions = computed(() => {
	const attributes = props.model?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const states = computed<State[]>(() => props.model?.model?.states ?? []);
const transitions = computed<Transition[]>(() =>
	props.model.model.transitions?.map((transition: Transition) => ({
		...transition,
		name: transition.name ?? transition?.properties?.name ?? transition.id,
		expression: props.model?.semantics?.ode?.rates?.find((rate) => rate.target === transition.id)
			?.expression
	}))
);
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
</script>
