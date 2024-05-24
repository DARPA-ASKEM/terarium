<template>
	<header>
		<strong>{{ parameterId }}</strong>
		<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
		<span v-if="unit" class="ml-2">({{ unit }})</span>
		<span v-if="description" class="ml-4">{{ description }}</span>
	</header>
	<main>
		<span class="flex gap-2">
			<Dropdown
				:model-value="getParameterDistributionType(modelConfiguration, parameterId)"
				@change="emit('select-distribution', { id: parameterId, value: $event.value })"
				option-label="name"
				option-value="value"
				:options="distributionTypeOptions()"
			/>

			<!-- Constant -->
			<tera-input
				v-if="
					getParameterDistributionType(modelConfiguration, parameterId) ===
					DistributionType.Constant
				"
				label="Constant"
				type="nist"
				:model-value="getParameterConstant(modelConfiguration, parameterId)"
				@update:model-value="emit('update-constant', { id: parameterId, value: $event })"
			/>
			<!-- Uniform Distribution -->
			<template
				v-if="
					getParameterDistributionType(modelConfiguration, parameterId) === DistributionType.Uniform
				"
			>
				<tera-input
					label="Min"
					type="nist"
					:model-value="
						getParameterDistribution(modelConfiguration, parameterId)?.parameters.minimum
					"
					@update:model-value="
						emit('update-distribution', { id: parameterId, parameters: { minimum: $event } })
					"
				/>
				<tera-input
					label="Max"
					type="nist"
					:model-value="
						getParameterDistribution(modelConfiguration, parameterId)?.parameters.maximum
					"
					@update:model-value="
						emit('update-distribution', { id: parameterId, parameters: { maximum: $event } })
					"
				/>
			</template>
		</span>
		<Button
			:label="getSourceLabel(parameterId)"
			text
			size="small"
			@click="sourceOpen = !sourceOpen"
		/>
	</main>
	<footer v-if="sourceOpen">
		<tera-input
			label="Source / notes / etc"
			:model-value="getParameterSource(modelConfiguration, parameterId)"
			@update:model-value="emit('update-source', { id: parameterId, value: $event })"
		/>
	</footer>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import {
	getParameterName,
	getParameterDescription,
	getParameterUnit,
	getParameterSource,
	getParameterConstant,
	getParameterDistributionType,
	getParameterDistribution
} from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DistributionType, distributionTypeOptions } from '@/types/common';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	parameterId: string;
}>();

const emit = defineEmits([
	'update-constant',
	'update-distribution',
	'update-source',
	'select-distribution'
]);

const name = getParameterName(props.modelConfiguration, props.parameterId);
const unit = getParameterUnit(props.modelConfiguration, props.parameterId);
const description = getParameterDescription(props.modelConfiguration, props.parameterId);

const sourceOpen = ref(false);

function getSourceLabel(initialId) {
	if (sourceOpen.value) return 'Hide source';
	if (!getParameterSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}
</script>

<style scoped>
header {
	display: flex;
	padding-bottom: var(--gap-small);
}
.expression {
	flex-grow: 1;
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
