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
					:model-value="distribution.type"
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
					v-if="distribution.type === DistributionType.Constant"
					label="Constant"
					type="nist"
					:model-value="distribution?.parameters.value"
					@update:model-value="
						emit('update-parameter', {
							id: parameterId,
							distribution: formatPayloadFromParameterChange({ value: $event })
						})
					"
				/>
				<!-- Uniform Distribution -->
				<template v-if="distribution.type === DistributionType.Uniform">
					<tera-input
						label="Min"
						type="nist"
						:model-value="distribution?.parameters.minimum"
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
						:model-value="distribution?.parameters.maximum"
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
import { Model, ModelConfiguration } from '@/types/Types';
import { getParameterSource, getParameterDistribution } from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DistributionType, distributionTypeOptions } from '@/services/distribution';
import { getParameter } from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	parameterId: string;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const name = getParameter(props.model, props.parameterId)?.name;
const units = getParameter(props.model, props.parameterId)?.units?.expression;
const description = getParameter(props.model, props.parameterId)?.description;

const distribution = computed(() =>
	getParameterDistribution(props.modelConfiguration, props.parameterId)
);

const isSourceOpen = ref(false);

function getSourceLabel(initialId) {
	if (isSourceOpen.value) return 'Hide source';
	if (!getParameterSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}

function formatPayloadFromParameterChange(parameters) {
	const parameterDistribution = getParameterDistribution(
		props.modelConfiguration,
		props.parameterId
	);
	Object.keys(parameters).forEach((key) => {
		if (!parameterDistribution) return;
		if (key in parameterDistribution.parameters) {
			parameterDistribution.parameters[key] = parameters[key];
		}
	});

	return distribution;
}

function formatPayloadFromTypeChange(type: DistributionType) {
	const parameterDistribution = getParameterDistribution(
		props.modelConfiguration,
		props.parameterId
	);

	parameterDistribution.type = type;
	parameterDistribution.parameters = {};
	return parameterDistribution;
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
