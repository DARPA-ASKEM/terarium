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
				:model-errors="modelErrors.filter((d) => d.type === 'state')"
				:feature-config="featureConfig"
				:filter="statesFilter"
				:filter-type="stateFilterType"
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
				:model-errors="[]"
				:feature-config="featureConfig"
				show-matrix
				:filter="parametersFilter"
				:filter-type="parametersFilterType"
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
				:model-errors="[]"
				:feature-config="featureConfig"
				:filter="observablesFilter"
				:filter-type="observablesFilterType"
				@update-item="emit('update-observable', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
				<tera-model-part-filter
					class="ml-auto"
					:model-errors="getModelErrors('transition')"
					v-model:filter="transitionsFilter"
					v-model:filter-type="transitionsFilterType"
				/>
			</template>
			<tera-model-error-message :items="getModelErrors('transition')" @filter-item="transitionsFilter = $event" />
			<tera-model-part
				:part-type="PartType.TRANSITION"
				v-if="!isEmpty(transitions) && !isEmpty(mmt.templates)"
				:items="transitionsList"
				:model-errors="getModelErrors('transition')"
				:feature-config="featureConfig"
				:filter="transitionsFilter"
				:filter-type="transitionsFilterType"
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
				:model-errors="[]"
				:feature-config="featureConfig"
				:filter-type="null"
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
import type { MiraModel, MiraTemplate, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseInitials, collapseParameters, collapseTemplates } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import type { FeatureConfig } from '@/types/common';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import {
	createPartsList,
	createObservablesList,
	createTimeList,
	PartType,
	ModelError
} from '@/model-representation/service';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { ModelPartItem, ModelPartItemTree, StratifiedMatrix } from '@/types/Model';
import { getControllerNames } from '@/model-representation/mira/mira-util';
import TeraModelErrorMessage from '@/components/model/model-parts/tera-model-error-message.vue';
import TeraModelPartFilter from '@/components/model/model-parts/tera-model-part-filter.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	modelErrors: ModelError[];
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-state', 'update-parameter', 'update-observable', 'update-transition', 'update-time']);

// Keep track of active indexes using ref
const currentActiveIndexes = ref([0, 1, 2, 3, 4]);

const statesFilter = ref('');
const stateFilterType = ref<'warn' | 'error' | null>(null);
const parametersFilter = ref('');
const parametersFilterType = ref<'warn' | 'error' | null>(null);
const observablesFilter = ref('');
const observablesFilterType = ref<'warn' | 'error' | null>(null);
const transitionsFilter = ref('');
const transitionsFilterType = ref<'warn' | 'error' | null>(null);

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));

const collapsedInitials = collapseInitials(props.mmt);
const states = computed<State[]>(() => props.model?.model?.states ?? []);
let stateList: ModelPartItemTree[] = createPartsList(collapsedInitials, props.model, PartType.STATE);

watch(
	() => props.model?.model?.states,
	() => {
		stateList = createPartsList(collapsedInitials, props.model, PartType.STATE);
	}
);

const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);
let parameterList: ModelPartItemTree[] = createPartsList(collapsedParameters, props.model, PartType.PARAMETER);

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

const createTransitionParts = () => {
	const extract = (t: MiraTemplate): ModelPartItem => ({
		id: t.name,
		name: t.name,
		subject: t.subject ? t.subject.name : '',
		outcome: t.outcome ? t.outcome.name : '',
		controllers: getControllerNames(t).join(', '),
		expression: t.rate_law
	});

	const templatesMap = collapseTemplates(props.mmt).matrixMap;
	return Array.from(templatesMap.keys()).map((templateId) => {
		const children = templatesMap.get(templateId) as MiraTemplate[];
		if (children.length === 1) {
			const item = children[0];
			return {
				base: extract(item),
				isParent: false,
				children: []
			};
		}
		return {
			base: { id: templateId, name: templateId },
			isParent: true,
			children: children.map(extract)
		};
	});
};

const transitionsList: ModelPartItemTree[] = createTransitionParts();

const parameterMatrixModalId = ref('');
const transitionMatrixModalId = ref('');
const observablesList = computed(() => createObservablesList(observables.value));
const timeList = computed<ModelPartItemTree[]>(() => createTimeList(time.value));

// Model Errors are filtered by type and severity
function getModelErrors(type: string) {
	return props.modelErrors.filter(({ type: errorType }) => errorType === type);
}
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
