<template>
	<div class="parameter-entry flex flex-column flex-1" :class="{ empty: isParameterEmpty }">
		<header class="gap-1 pt-2 pb-2">
			<div class="flex">
				<strong>{{ parameterId }}</strong>
				<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
				<template v-if="units">
					<label class="ml-auto">Unit:</label>
					<span class="ml-1">{{ units }}</span>
				</template>

				<template v-if="concept">
					<label class="ml-6">Concept:</label>
					<span class="ml-1">{{ concept }}</span>
				</template>
			</div>

			<span class="description" v-if="description">{{ description }}</span>
		</header>
		<div v-if="inferredDistribution" class="inferred-parameter">
			<span class="type"><label>Type</label> {{ inferredDistribution.type }}</span>
			<span class="mean">
				<label>Mean</label> {{ displayNumber(inferredDistribution?.parameters?.mean?.toString()) }}
			</span>
			<span class="std">
				<label>STDDEV</label> {{ displayNumber(inferredDistribution?.parameters?.stddev?.toString()) }}
			</span>
		</div>
		<template v-else-if="!featureConfig?.isPreview">
			<main class="flex align-items-center">
				<span class="flex gap-2">
					<Dropdown
						:model-value="getParameterDistribution(modelConfiguration, parameterId).type"
						@change="
							emit('update-parameter', {
								id: parameterId,
								distribution: formatPayloadFromTypeChange($event.value)
							})
						"
						option-label="name"
						option-value="value"
						:options="distributionTypeOptions()"
						class="mr-3 parameter-input"
					/>
					<!-- Constant -->
					<tera-input-number
						v-if="getParameterDistribution(modelConfiguration, parameterId).type === DistributionType.Constant"
						label="Constant"
						:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters.value"
						error-empty
						@update:model-value="onParameterChange($event, Parameter.value)"
						class="parameter-input"
					/>
					<!-- Uniform Distribution -->
					<template v-if="getParameterDistribution(modelConfiguration, parameterId).type === DistributionType.Uniform">
						<tera-input-number
							label="Min"
							:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters.minimum"
							error-empty
							@update:model-value="onParameterChange($event, Parameter.minimum)"
							class="mr-2 parameter-input"
						/>
						<tera-input-number
							label="Max"
							:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters.maximum"
							error-empty
							@update:model-value="onParameterChange($event, Parameter.maximum)"
							class="parameter-input"
						/>
					</template>
				</span>
				<section>
					<Button :label="getSourceLabel(parameterId)" text size="small" @click="isSourceOpen = !isSourceOpen" />
					<Button :label="getOtherValuesLabel" text size="small" @click="showOtherConfigValueModal = true" />
				</section>
			</main>
			<footer v-if="isSourceOpen" class="mb-2">
				<tera-input-text
					placeholder="Add a source"
					:model-value="getParameterSource(modelConfiguration, parameterId)"
					@update:model-value="emit('update-source', { id: parameterId, value: $event })"
				/>
			</footer>
		</template>
		<p class="flex gap-4" v-else>
			<template v-if="getParameterDistribution(modelConfiguration, parameterId).type === DistributionType.Constant">
				<span><label>Type</label> {{ DistributionTypeLabel.Constant }}</span>
				<span>
					<label>Value</label>
					{{ getParameterDistribution(modelConfiguration, parameterId).parameters.value }}
				</span>
			</template>
			<template v-else-if="getParameterDistribution(modelConfiguration, parameterId).type === DistributionType.Uniform">
				<span><label>Type</label> {{ DistributionTypeLabel.StandardUniform1 }}</span>
				<span>
					<label>Min</label>
					{{ getParameterDistribution(modelConfiguration, parameterId).parameters.minimum }}
				</span>
				<span>
					<label>Max</label>
					{{ getParameterDistribution(modelConfiguration, parameterId).parameters.maximum }}
				</span>
			</template>
		</p>
	</div>
	<tera-parameter-other-value-modal
		v-if="showOtherConfigValueModal"
		:id="parameterId"
		:otherValueList="otherValueList"
		@modal-mask-clicked="showOtherConfigValueModal = false"
		@update-parameter="emit('update-parameter', $event)"
		@update-source="emit('update-source', $event)"
		@close-modal="showOtherConfigValueModal = false"
	/>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { Model, ModelConfiguration } from '@/types/Types';
import {
	getParameterSource,
	getParameterDistribution,
	isNumberInputEmpty,
	getOtherValues
} from '@/services/model-configurations';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DistributionType, DistributionTypeLabel, distributionTypeOptions } from '@/services/distribution';
import { getParameter } from '@/model-representation/service';
import TeraParameterOtherValueModal from '@/components/model/petrinet/tera-parameter-other-value-modal.vue';
import { displayNumber } from '@/utils/number';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';
import type { FeatureConfig } from '@/types/common';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
	parameterId: string;
	featureConfig?: FeatureConfig;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const name = getParameter(props.model, props.parameterId)?.name;
const units = getParameter(props.model, props.parameterId)?.units?.expression;
const description = getParameter(props.model, props.parameterId)?.description;
const inferredDistribution = computed(
	() =>
		props.modelConfiguration.inferredParameterList?.find((param) => param.referenceId === props.parameterId)
			?.distribution
);

const concept = ref('');
const isSourceOpen = ref(false);
const showOtherConfigValueModal = ref(false);
const isParameterEmpty = ref(false);

enum Parameter {
	value = 'value',
	maximum = 'maximum',
	minimum = 'minimum'
}

const otherValueList = computed(() =>
	getOtherValues(props.modelConfigurations, props.parameterId, 'referenceId', 'parameterSemanticList')
);

function getSourceLabel(initialId) {
	if (isSourceOpen.value) return 'Hide source';
	if (!getParameterSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}

function formatPayloadFromParameterChange(parameters) {
	const distribution = getParameterDistribution(props.modelConfiguration, props.parameterId);
	Object.keys(parameters).forEach((key) => {
		if (!distribution) return;
		if (key in distribution.parameters) {
			distribution.parameters[key] = parameters[key];
		}
	});

	return distribution;
}

function formatPayloadFromTypeChange(type: DistributionType) {
	const distribution = getParameterDistribution(props.modelConfiguration, props.parameterId);

	distribution.type = type;
	distribution.parameters = {};
	return distribution;
}

const getOtherValuesLabel = computed(() => `Other values (${otherValueList.value?.length})`);

function isParameterInputEmpty(parameter) {
	if (parameter.type === DistributionType.Constant) {
		return isNumberInputEmpty(parameter.parameters.value);
	}
	return isNumberInputEmpty(parameter.parameters.maximum) || isNumberInputEmpty(parameter.parameters.minimum);
}

function onParameterChange(value, parameter) {
	isParameterEmpty.value = isNumberInputEmpty(value);
	if (!isParameterEmpty.value) {
		emit('update-parameter', {
			id: props.parameterId,
			distribution: formatPayloadFromParameterChange({ [parameter]: value })
		});
	}
}

onMounted(async () => {
	const identifiers = getParameter(props.model, props.parameterId)?.grounding?.identifiers;
	if (identifiers) concept.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
	const parameter = getParameterDistribution(props.modelConfiguration, props.parameterId, true);
	isParameterEmpty.value = inferredDistribution.value ? false : isParameterInputEmpty(parameter);
});
</script>

<style scoped>
.parameter-entry {
	border-left: 4px solid var(--surface-300);
	padding-left: var(--gap-4);
}
.empty {
	border-left: 4px solid var(--error-color);
}

header {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	padding-bottom: var(--gap-2);
	padding-bottom: var(--gap-2);
}
main {
	display: flex;
	justify-content: space-between;
	padding-bottom: var(--gap-2);
}

.description {
	color: var(--text-color-subdued);
}

:deep(.p-dropdown-label) {
	min-width: 10rem;
}

label {
	color: var(--text-color-subdued);
}

.inferred-parameter {
	display: flex;
	& > span {
		width: 20%;
	}
}

.parameter-input {
	height: 2rem;
}
</style>
