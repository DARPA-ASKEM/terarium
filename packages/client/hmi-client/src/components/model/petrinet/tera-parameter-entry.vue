<template>
	<div class="flex flex-column flex-1">
		<header>
			<div>
				<strong>{{ parameterId }}</strong>
				<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
				<template v-if="units">
					<label class="ml-2">Unit</label>
					<span class="ml-1">{{ units }}</span>
				</template>

				<template v-if="concept">
					<label class="ml-auto">Concept</label>
					<span class="ml-1">{{ concept }}</span>
				</template>
			</div>

			<span v-if="description">{{ description }}</span>
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
			<section>
				<Button
					:label="getSourceLabel(parameterId)"
					text
					size="small"
					@click="isSourceOpen = !isSourceOpen"
				/>
				<Button
					:label="getOtherValuesLabel"
					text
					size="small"
					@click="showOtherConfigValueModal = true"
				/>
			</section>
		</main>
		<footer v-if="isSourceOpen">
			<tera-input
				placeholder="Add a source"
				:model-value="getParameterSource(modelConfiguration, parameterId)"
				@update:model-value="emit('update-source', { id: parameterId, value: $event })"
			/>
		</footer>
	</div>
	<Teleport to="body">
		<tera-paramenter-other-value-modal
			v-if="showOtherConfigValueModal"
			:id="parameterId"
			:tableData="tableData"
			@modal-mask-clicked="showOtherConfigValueModal = false"
			@update-parameter="emit('update-parameter', $event)"
			@update-source="emit('update-source', $event)"
			@close-modal="showOtherConfigValueModal = false"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { Model, ModelConfiguration } from '@/types/Types';
import { getParameterSource, getParameterDistribution } from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { computed, onMounted, ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DistributionType, distributionTypeOptions } from '@/services/distribution';
import { getParameter } from '@/model-representation/service';
import TeraParamenterOtherValueModal from '@/components/model/petrinet/tera-parameter-other-value-modal.vue';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
	parameterId: string;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const name = getParameter(props.model, props.parameterId)?.name;
const units = getParameter(props.model, props.parameterId)?.units?.expression;
const description = getParameter(props.model, props.parameterId)?.description;
const concept = getParameter(props.model, props.parameterId)?.grounding?.identifiers[0];

const tableData = ref();

const isSourceOpen = ref(false);
const showOtherConfigValueModal = ref(false);

onMounted(() => setTableData(props.parameterId));

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

const setTableData = (id, key = 'referenceId', otherValueList = 'parameterSemanticList') => {
	tableData.value = [];
	console.log('props.modelConfigurations', props.modelConfigurations);
	const modelConfigTableData = props.modelConfigurations.map((modelConfig) => ({
		name: modelConfig.name,
		list: modelConfig[otherValueList]
	}));

	modelConfigTableData.forEach((modelConfig) => {
		const config = modelConfig.list.filter((item) => item[key] === id)[0];
		if (config && modelConfig.name) {
			const data = {
				name: modelConfig.name,
				...config,
				...config.distribution.parameters,
				type: config.distribution.type
			};
			tableData.value.push(data);
		}
	});
	console.log('tableData.value', tableData.value);
};

const getOtherValuesLabel = computed(() => `Other Values(${tableData.value?.length})`);
</script>

<style scoped>
header {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
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

label {
	color: var(--text-color-subdued);
}
</style>
