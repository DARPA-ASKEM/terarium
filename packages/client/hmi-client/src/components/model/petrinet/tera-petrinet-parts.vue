<template>
	<Accordion multiple :active-index="currentActiveIndexes">
		<AccordionTab>
			<template #header>
				State variables<span class="artifact-amount">({{ states.length }})</span>
				<tera-model-part-filter
					v-if="!isEmpty(mmt.initials)"
					class="ml-auto"
					:model-errors="getModelErrors(ModelErrorType.STATE)"
					v-model:filter="statesFilter"
					v-model:filter-severity="stateFilterSeverity"
				/>
			</template>
			<tera-model-error-message :modelErrors="getModelErrors(ModelErrorType.STATE)" />
			<tera-model-part
				v-if="!isEmpty(mmt.initials)"
				:part-type="PartType.STATE"
				:items="stateList"
				:model-errors="getModelErrors(ModelErrorType.STATE)"
				:feature-config="featureConfig"
				:filter="statesFilter"
				:filter-severity="stateFilterSeverity"
				@update-item="emit('update-state', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ parameters.length }})</span>
				<tera-model-part-filter
					v-if="!isEmpty(mmt.parameters)"
					class="ml-auto"
					:model-errors="getModelErrors(ModelErrorType.PARAMETER)"
					v-model:filter="parametersFilter"
					v-model:filter-severity="parametersFilterSeverity"
				/>
			</template>
			<tera-model-error-message :modelErrors="getModelErrors(ModelErrorType.PARAMETER)" />
			<tera-model-part
				v-if="!isEmpty(mmt.parameters)"
				:part-type="PartType.PARAMETER"
				:items="parameterList"
				:model-errors="getModelErrors(ModelErrorType.PARAMETER)"
				:feature-config="featureConfig"
				show-matrix
				:filter="parametersFilter"
				:filter-severity="parametersFilterSeverity"
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
				<tera-model-part-filter
					v-if="!isEmpty(observables)"
					class="ml-auto"
					:model-errors="getModelErrors(ModelErrorType.OBSERVABLE)"
					v-model:filter="observablesFilter"
					v-model:filter-severity="observablesFilterSeverity"
				/>
			</template>
			<tera-model-error-message :modelErrors="getModelErrors(ModelErrorType.OBSERVABLE)" />
			<tera-model-part
				:part-type="PartType.OBSERVABLE"
				v-if="!isEmpty(observables)"
				:items="observablesList"
				:model-errors="getModelErrors(ModelErrorType.OBSERVABLE)"
				:feature-config="featureConfig"
				:filter="observablesFilter"
				:filter-severity="observablesFilterSeverity"
				@update-item="emit('update-observable', $event)"
			/>
		</AccordionTab>
		<AccordionTab>
			<template #header>
				Transitions<span class="artifact-amount">({{ transitions.length }})</span>
				<tera-model-part-filter
					class="ml-auto"
					:model-errors="getModelErrors(ModelErrorType.TRANSITION)"
					v-model:filter="transitionsFilter"
					v-model:filter-severity="transitionsFilterSeverity"
				/>
			</template>
			<tera-model-error-message :modelErrors="getModelErrors(ModelErrorType.TRANSITION)" />
			<tera-model-part
				:part-type="PartType.TRANSITION"
				v-if="!isEmpty(transitions) && !isEmpty(mmt.templates)"
				:items="transitionsList"
				:model-errors="getModelErrors(ModelErrorType.TRANSITION)"
				:feature-config="featureConfig"
				:filter="transitionsFilter"
				:filter-severity="transitionsFilterSeverity"
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
			<tera-model-error-message :modelErrors="getModelErrors(ModelErrorType.TIME)" />
			<tera-model-part
				v-if="time"
				:part-type="PartType.TIME"
				:items="timeList"
				:model-errors="getModelErrors(ModelErrorType.TIME)"
				:feature-config="featureConfig"
				:filter-severity="null"
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
import { computed, ref } from 'vue';
import type { MiraModel, MiraTemplate, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { collapseInitials, collapseParameters, collapseTemplates } from '@/model-representation/mira/mira';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import type { FeatureConfig } from '@/types/common';
import {
	createPartsList,
	createObservablesList,
	createTimeList,
	PartType,
	ModelError,
	ModelErrorSeverity,
	ModelErrorType
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
const stateFilterSeverity = ref<ModelErrorSeverity | null>(null);
const parametersFilter = ref('');
const parametersFilterSeverity = ref<ModelErrorSeverity | null>(null);
const observablesFilter = ref('');
const observablesFilterSeverity = ref<ModelErrorSeverity | null>(null);
const transitionsFilter = ref('');
const transitionsFilterSeverity = ref<ModelErrorSeverity | null>(null);

const parameters = computed(() => props.model?.semantics?.ode.parameters ?? []);
const observables = computed(() => props.model?.semantics?.ode?.observables ?? []);
const time = computed(() => (props.model?.semantics?.ode?.time ? [props.model?.semantics.ode.time] : []));

const collapsedInitials = collapseInitials(props.mmt);
const states = computed<State[]>(() => props.model?.model?.states ?? []);

const stateList = computed(() => createPartsList(collapsedInitials, props.model, PartType.STATE));

const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);
const parameterList = computed(() => createPartsList(collapsedParameters, props.model, PartType.PARAMETER));

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
		expression: t.rate_law,
		description: t.subject.description ? t.subject.description : ''
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

const transitionsList = createTransitionParts();

const parameterMatrixModalId = ref('');
const transitionMatrixModalId = ref('');
const observablesList = computed(() => createObservablesList(observables.value));
const timeList = computed<ModelPartItemTree[]>(() => createTimeList(time.value));

// Model Errors are filtered by type
function getModelErrors(entryType: ModelErrorType) {
	return props.modelErrors.filter(({ type }) => type === entryType);
}
</script>

<style scoped>
:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: var(--gap-1);
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
