<template>
	<header>
		<strong>{{ parameterId }}</strong>
		<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
		<span v-if="unit" class="ml-2">({{ unit }})</span>
		<span v-if="description" class="ml-4">{{ description }}</span>
	</header>
	<main>
		<span class="flex gap-2">
			<tera-tooltip position="top" :has-arrow="false">
				<Dropdown
					:model-value="parameterDistribution.type"
					@change="
						emit('update-parameter', {
							id: parameterId,
							distribution: formatPayloadFromTypeChange($event.value)
						})
					"
					filter
					option-label="name"
					option-value="value"
					:options="distributionTypeOptions()"
				>
					<template #option="{ option }">
						<span class="flex flex-1" v-tooltip="getTooltipContent(option.value)">{{
							option.name
						}}</span>
					</template>
				</Dropdown>
				<template #tooltip-content>
					<h1 class="pb-1">{{ DistributionTypeLabel[parameterDistribution.type] }}</h1>
					<p class="distribution-description">
						{{ DistributionTypeDescription[parameterDistribution.type] }}
					</p>
				</template>
			</tera-tooltip>

			<!-- Constant -->
			<tera-input
				v-if="parameterDistribution.type === DistributionType.Constant"
				label="value"
				type="nist"
				:model-value="parameterDistribution?.parameters.value"
				@update:model-value="
					emit('update-parameter', {
						id: parameterId,
						distribution: formatPayloadFromParameterChange({ value: $event })
					})
				"
			/>
			<!-- Uniform Distribution -->
			<template v-if="parameterDistribution.type === DistributionType.Uniform">
				<tera-input
					label="low"
					type="nist"
					:model-value="parameterDistribution?.parameters.minimum"
					@update:model-value="
						emit('update-parameter', {
							id: parameterId,
							distribution: formatPayloadFromParameterChange({ minimum: $event })
						})
					"
				/>
				<tera-input
					label="high"
					type="nist"
					:model-value="parameterDistribution.parameters.maximum"
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
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import {
	getParameterName,
	getParameterDescription,
	getParameterUnit,
	getParameterSource,
	getParameterDistribution
} from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import {
	DistributionType,
	DistributionTypeDescription,
	DistributionTypeLabel,
	distributionTypeOptions
} from '@/services/distribution';
import TeraTooltip from '@/components/widgets/tera-tooltip.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	parameterId: string;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const name = getParameterName(props.modelConfiguration, props.parameterId);
const unit = getParameterUnit(props.modelConfiguration, props.parameterId);
const description = getParameterDescription(props.modelConfiguration, props.parameterId);

const isSourceOpen = ref(false);

const parameterDistribution = computed(() =>
	getParameterDistribution(props.modelConfiguration, props.parameterId)
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

function getTooltipContent(distributionType: DistributionType) {
	return {
		value: `${DistributionTypeLabel[distributionType]}: ${DistributionTypeDescription[distributionType]}`,
		pt: {
			text: {
				style: {
					backgroundColor: 'var(--surface-overlay)',
					color: 'var(--text-primary)',
					border: '1px solid var(--surface-border-alt)',
					borderRadius: 'var(--border-radius-small)'
				}
			}
		},
		showDelay: 300,
		hideDelay: 300
	};
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

.distribution-description {
	width: 12rem;
}
</style>
