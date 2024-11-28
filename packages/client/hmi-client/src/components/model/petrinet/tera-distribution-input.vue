<template>
	<span class="flex gap-2">
		<section>
			<Dropdown
				:id="parameterId"
				ref="dropdownRef"
				:model-value="getParameterDistribution(modelConfiguration, parameterId).type"
				@change="onChange"
				option-label="name"
				option-value="value"
				:options="distributionOptions"
				class="mr-3 parameter-input"
			>
				<template #option="slotProps">
					<section
						class="flex align-items-center w-full"
						@mouseover="showPopup(dropdownRef.$el, slotProps.index)"
						@mouseleave="hidePopup()"
						@focusin="() => showPopup(dropdownRef.$el, slotProps.index)"
						@blur="() => hidePopup()"
					>
						<div>{{ slotProps.option.name }}</div>
					</section>
				</template>
			</Dropdown>
			<section
				v-if="hoveredOption"
				class="popup"
				:style="{ top: `${popupPosition.top}px`, left: `${popupPosition.left}px` }"
			>
				<Card class="inside p-2">
					<template #title>{{ hoveredOption.name }}</template>
					<template #content>
						{{ distributionDescription[hoveredOption.value] }}
						<h5 class="pt-3 pb-2">Required parameters:</h5>
						<ul class="ml-5">
							<li class="pb-2" v-for="(param, index) in DistributionInputLabels[hoveredOption.value]" :key="param">
								<h6>{{ param }}</h6>
								<p class="parameter-description">
									{{ distributionParamDescription[DistributionInputOptions[hoveredOption.value][index]] }}
								</p>
							</li>
						</ul>
					</template>
				</Card>
			</section>
		</section>
		<template v-for="(option, index) in options" :key="index">
			<tera-input-number
				class="parameter-input"
				error-empty
				:label="DistributionInputLabels[parameter.type][index]"
				:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters[option]"
				@update:model-value="onParameterChange($event, DistributionInputOptions[parameter.type][index])"
			/>
		</template>
	</span>
</template>
<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { getParameterDistribution, isNumberInputEmpty } from '@/services/model-configurations';
import { Model, ModelConfiguration, ModelDistribution } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import {
	DistributionType,
	distributionTypeOptions,
	DistributionInputOptions,
	DistributionInputLabels,
	distributionDescription,
	distributionParamDescription
} from '@/services/distribution';
import Card from 'primevue/card';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	parameterId: string;
	parameter: ModelDistribution;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const options = computed(() => DistributionInputOptions[props.parameter.type]);

const dropdownRef = ref();
const isParameterEmpty = ref<boolean>(false);
const distributionOptions = ref<{ name: string; value: string }[]>(distributionTypeOptions());
const hoveredOption = ref<{ name: string; value: string } | null>();
const popupPosition = ref({ top: 0, left: 0 });

function showPopup(item: HTMLElement | null, index: number) {
	if (!item) return;

	hoveredOption.value = distributionOptions.value[index];
	const rect = item.getBoundingClientRect();
	popupPosition.value = {
		top: rect.y / rect.top + window.scrollY - 35,
		left: rect.x / rect.left + rect.width
	};
}

function hidePopup() {
	hoveredOption.value = null;
}

function onChange(event) {
	emit('update-parameter', {
		id: props.parameterId,
		distribution: formatPayloadFromTypeChange(event.value)
	});
	hidePopup();
}

function isParameterInputEmpty(parameter) {
	if (!DistributionInputOptions?.[parameter.type]?.length) {
		return true;
	}

	if (DistributionInputOptions[parameter.type].length === 1) {
		return isNumberInputEmpty(DistributionInputOptions[parameter.type][0]);
	}
	return (
		isNumberInputEmpty(DistributionInputOptions[parameter.type][0]) ||
		isNumberInputEmpty(DistributionInputOptions[parameter.type][1])
	);
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

function formatPayloadFromParameterChange(parameters) {
	const distribution = getParameterDistribution(props.modelConfiguration, props.parameterId, true);
	Object.keys(parameters).forEach((key) => {
		if (!distribution) return;
		distribution.parameters[key] = parameters[key];
	});
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
<style scoped>
.popup {
	position: relative;
	box-shadow: 0 var(--gap-0-5) var(--gap-1) rgba(0, 0, 0, 0.2);
	padding: var(--gap-0-5);
	z-index: 3;
}
.inside {
	position: absolute;
	border: 1px solid #ccc;
	min-height: 100px;
	min-width: 300px;
}

.parameter-description {
	color: var(--text-color-subdued);
	font-style: italic;
}
</style>
