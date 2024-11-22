<template>
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

		<!-- <template v-for="(option, index) in options" :key="index">
			<tera-input-number
				:label=DistributionInputLabels[parameter.type][index]
				:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters[option]"
				error-empty
				@update:model-value="onParameterChange($event, DistributionInputOptions[parameter.type][index])"
				class="parameter-input"
			/>
		</template> -->

		<tera-input-number
			v-if="getParameterDistribution(modelConfiguration, parameterId).type === DistributionType.Constant"
			label="Constant"
			:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters.value"
			error-empty
			@update:model-value="onParameterChange($event, Parameter.value)"
			class="parameter-input"
		/>

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
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getParameterDistribution, isNumberInputEmpty } from '@/services/model-configurations';
import { Model, ModelConfiguration, ModelDistribution } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { DistributionType, distributionTypeOptions, DistributionInputOptions } from '@/services/distribution';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	parameterId: string;
	parameter: ModelDistribution;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

// const options = DistributionInputOptions[props.parameter.type];

enum Parameter {
	value = 'value',
	maximum = 'maximum',
	minimum = 'minimum'
}

const isParameterEmpty = ref(false);

function isParameterInputEmpty(parameter) {
	if (parameter.type === DistributionType.Constant) {
		return isNumberInputEmpty(parameter.parameters.value);
	}
	return isNumberInputEmpty(parameter.parameters.maximum) || isNumberInputEmpty(parameter.parameters.minimum);
}

function onParameterChange(value, parameter) {
	console.log('DistributionInputOptions', DistributionInputOptions);
	console.log(value, parameter);
	isParameterEmpty.value = isNumberInputEmpty(value);
	if (!isParameterEmpty.value) {
		emit('update-parameter', {
			id: props.parameterId,
			distribution: formatPayloadFromParameterChange({ [parameter]: value })
		});
	}
}

function formatPayloadFromParameterChange(parameters) {
	console.log('parameters', parameters);
	const distribution = props.parameter;
	Object.keys(parameters).forEach((key) => {
		if (!distribution) return;
		if (key in distribution.parameters) {
			distribution.parameters[key] = parameters[key];
		}
	});
	console.log('distribution', distribution);
	return distribution;
}

function formatPayloadFromTypeChange(type: DistributionType) {
	const distribution = props.parameter;

	distribution.type = type;
	distribution.parameters = {};
	return distribution;
}
onMounted(async () => {
	isParameterEmpty.value = isParameterInputEmpty(props.parameter);
});
</script>
<style scoped></style>
