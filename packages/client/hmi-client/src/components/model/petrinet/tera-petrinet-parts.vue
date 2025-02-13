<template>
	<Accordion multiple :active-index="currentActiveIndexes">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
				<tera-input-text v-if="!isEmpty(mmt.initials)" class="ml-auto" placeholder="Filter" v-model="statesFilter" />
			</template>
			<tera-model-part
				v-if="!isEmpty(mmt.initials)"
				:part-type="PartType.STATE"
				:items="stateList"
				:collapsed-items="collapsedInitials"
				:feature-config="featureConfig"
				:filter="statesFilter"
				@update-item="emit('update-state', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters.length }})</span>
				<tera-input-text
					v-if="!isEmpty(mmt.parameters)"
					class="ml-auto"
					placeholder="Filter"
					v-model="parametersFilter"
				/>
			</template>
			<tera-model-part
				v-if="!isEmpty(mmt.parameters)"
				:part-type="PartType.PARAMETER"
				:items="parameterList"
				:collapsed-items="collapsedParameters"
				:feature-config="featureConfig"
				show-matrix
				:filter="parametersFilter"
				@open-matrix="(id: string) => (parameterMatrixModalId = id)"
				@update-item="emit('update-parameter', $event)"
			/>
			<tera-stratified-matrix-modal
				v-if="parameterMatrixModalId"
				:id="parameterMatrixModalId"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:stratified-matrix-type="StratifiedMatrix.Parameters"
				is-read-only
				@close-modal="parameterMatrixModalId = ''"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Observables <span class="artifact-amount">({{ observables.length }})</span>
				<tera-input-text
					v-if="!isEmpty(observables)"
					class="ml-auto"
					placeholder="Filter"
					v-model="observablesFilter"
				/>
			</template>
			<tera-model-part
				:part-type="PartType.OBSERVABLE"
				v-if="!isEmpty(observables)"
				:items="observablesList"
				:feature-config="featureConfig"
				:filter="observablesFilter"
				@update-item="emit('update-observable', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
				<tera-input-text class="ml-auto" placeholder="Filter" v-model="transitionsFilter" />
			</template>
			<tera-model-part
				:part-type="PartType.TRANSITION"
				v-if="!isEmpty(transitions) && !isEmpty(mmt.templates)"
				:items="transitionsList"
				:collapsed-items="collapsedTemplates"
				:feature-config="featureConfig"
				:filter="transitionsFilter"
				show-matrix
				@open-matrix="(id: string) => (transitionMatrixModalId = id)"
				@update-item="$emit('update-transition', $event)"
			/>
			<tera-stratified-matrix-modal
				v-if="transitionMatrixModalId"
				:id="transitionMatrixModalId"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:stratified-matrix-type="StratifiedMatrix.Rates"
				is-read-only
				@close-modal="transitionMatrixModalId = ''"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Time <span class="artifact-amount">({{ time.length }})</span>
			</template>
			<tera-model-part
				v-if="time"
				:part-type="PartType.TIME"
				:items="timeList"
				:feature-config="featureConfig"
				@update-item="$emit('update-time', $event)"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import type { Model, Transition, State } from '@/types/Types';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref, watch } from 'vue';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseInitials, collapseParameters, collapseTemplates } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import type { FeatureConfig } from '@/types/common';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { createPartsList, createObservablesList, createTimeList, PartType } from '@/model-representation/service';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { ModelPartItem, StratifiedMatrix } from '@/types/Model';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-transition', 'update-time']);

// Keep track of active indexes using ref
const currentActiveIndexes = ref([0, 1, 2, 3, 4]);
const statesFilter = ref('');
const parametersFilter = ref('');
const observablesFilter = ref('');
const transitionsFilter = ref('');

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));

const collapsedInitials = collapseInitials(props.mmt);
const states = computed<State[]>(() => props.model?.model?.states ?? []);
let stateList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = createPartsList(collapsedInitials, props.model, PartType.STATE);

watch(
	() => props.model?.model?.states,
	() => {
		stateList = createPartsList(collapsedInitials, props.model, PartType.STATE);
	}
);

const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);
let parameterList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = createPartsList(collapsedParameters, props.model, PartType.PARAMETER);

watch(
	() => props.model.semantics?.ode?.parameters,
	() => {
		parameterList = createPartsList(collapsedParameters, props.model, PartType.PARAMETER);
	}
);

const transitions = computed<Transition[]>(() =>
	props.model.model.transitions?.map((transition: Transition) => ({
		...transition,
		name: transition.name ?? transition.properties?.name ?? transition.id,
		expression: props.model?.semantics?.ode?.rates?.find((rate) => rate.target === transition.id)?.expression
	}))
);

const collapsedTemplates = (() => {
	const templateMap = new Map<string, string[]>();
	const collapsedTemplatesMap = collapseTemplates(props.mmt).matrixMap;
	Array.from(collapsedTemplatesMap.keys()).forEach((templateId) => {
		templateMap.set(
			templateId,
			Array.from(collapsedTemplatesMap.get(templateId) ?? []).map(({ name }) => name)
		);
	});
	return templateMap;
})();

const transitionsList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = createPartsList(collapsedTemplates, transitions.value, PartType.TRANSITION);

const parameterMatrixModalId = ref('');
const transitionMatrixModalId = ref('');
const observablesList = computed(() => createObservablesList(observables.value));
const timeList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = createTimeList(time.value);
</script>

<style scoped>
:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}
:deep(.p-accordion-content) {
	margin-bottom: var(--gap-3);
}
:deep(.p-accordion-content:empty::before) {
	content: 'None';
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	margin-left: var(--gap-6);
}
</style>
