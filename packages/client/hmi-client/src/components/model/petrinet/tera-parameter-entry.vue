<template>
	<div class="flex flex-column flex-1">
		<header>
			<strong>{{ parameterId }}</strong>
			<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
			<span v-if="units" class="ml-2">({{ units }})</span>
			<span v-if="description" class="ml-4">{{ description }}</span>
		</header>
		<main>
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
				/>

				<!-- Constant -->
				<tera-input
					v-if="
						getParameterDistribution(modelConfiguration, parameterId).type ===
						DistributionType.Constant
					"
					label="Constant"
					type="nist"
					:model-value="getParameterDistribution(modelConfiguration, parameterId)?.parameters.value"
					@update:model-value="
						emit('update-parameter', {
							id: parameterId,
							distribution: formatPayloadFromParameterChange({ value: $event })
						})
					"
				/>
				<!-- Uniform Distribution -->
				<template
					v-if="
						getParameterDistribution(modelConfiguration, parameterId).type ===
						DistributionType.Uniform
					"
				>
					<tera-input
						label="Min"
						type="nist"
						:model-value="
							getParameterDistribution(modelConfiguration, parameterId)?.parameters.minimum
						"
						@update:model-value="
							emit('update-parameter', {
								id: parameterId,
								distribution: formatPayloadFromParameterChange({ minimum: $event })
							})
						"
					/>
					<tera-input
						label="Max"
						type="nist"
						:model-value="
							getParameterDistribution(modelConfiguration, parameterId)?.parameters.maximum
						"
						@update:model-value="
							emit('update-parameter', {
								id: parameterId,
								distribution: formatPayloadFromParameterChange({ maximum: $event })
							})
						"
					/>
				</template>
			</span>
			<Button
				:label="getSourceLabel(parameterId)"
				text
				size="small"
				@click="isSourceOpen = !isSourceOpen"
			/>
		</main>
		<footer v-if="isSourceOpen">
			<tera-input
				label="Source / notes / etc"
				:model-value="getParameterSource(modelConfiguration, parameterId)"
				@update:model-value="emit('update-source', { id: parameterId, value: $event })"
			/>
		</footer>
	</div>
</template>

<script setup lang="ts">
import { ModelConfigurationLegacy, Model } from '@/types/Types';
import {
	getParameterSource,
	getParameterDistribution
} from '@/services/model-configurations-legacy';
import TeraInput from '@/components/widgets/tera-input.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DistributionType, distributionTypeOptions } from '@/services/distribution';
import { getParameter } from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfigurationLegacy;
	parameterId: string;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const name = getParameter(props.model, props.parameterId)?.name;
const units = getParameter(props.model, props.parameterId)?.units?.expression;
const description = getParameter(props.model, props.parameterId)?.description;

const isSourceOpen = ref(false);

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
</script>

<style scoped>
header {
	display: flex;
	padding-bottom: var(--gap-small);
}
main {
	display: flex;
	justify-content: space-between;
	padding-bottom: var(--gap-small);
}

:deep(.p-dropdown-label) {
	min-width: 10rem;
}
</style>
