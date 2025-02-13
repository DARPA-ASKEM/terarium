<template>
	<span class="flex gap-2">
		<section>
			<Dropdown
				:id="parameterId"
				ref="dropdownRef"
				:model-value="getParameterDistribution(modelConfiguration, parameterId).type"
				@change="onChange"
				@hide="hidePopup"
				option-label="name"
				option-value="value"
				:options="distributionOptions"
				class="mr-3 parameter-input"
			>
				<template #option="slotProps">
					<!-- Disabling popup until the not disappearing issue is fixed -->
					<!-- <section
						class="flex align-items-center w-full"
						@mouseover="showPopup(dropdownRef.$el, slotProps.index)"
						@focusin="() => showPopup(dropdownRef.$el, slotProps.index)"
					> -->
					<section class="flex align-items-center w-full">
						<div>{{ slotProps.option.name }}</div>
					</section>
				</template>
			</Dropdown>
			<section class="popup" :style="{ top: `${popupPosition.top}px`, left: `${popupPosition.left}px` }">
				<TeraDistributionInput ref="cardRef" class="p-2" :distribution-type="hoveredOption?.value ?? null" />
			</section>
		</section>
		<template v-for="option in options" :key="option.name">
			<tera-input-number
				class="parameter-input"
				error-empty
				:label="option.label"
				:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters[option.name]"
				@update:model-value="onParameterChange($event, option.name)"
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
import { DistributionType, distributionTypeOptions, distributions } from '@/services/distribution';
import TeraDistributionInput from './tera-distribution-tooltips.vue';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	parameterId: string;
	parameter: ModelDistribution;
}>();

const emit = defineEmits(['update-parameter', 'update-source']);

const options = computed(() => distributions[props.parameter.type].parameters);

const dropdownRef = ref();
const cardRef = ref();
const isParameterEmpty = ref<boolean>(false);
const distributionOptions = ref<{ name: string; value: string }[]>(distributionTypeOptions());
const hoveredOption = ref<{ name: string; value: string } | null>();
const popupPosition = ref({ top: 0, left: 0 });

function hidePopup() {
	hoveredOption.value = null;
}

// Disabling popup until the not disappearing issue is fixed

// async function showPopup(item: HTMLElement | null, index: number) {
// 	hidePopup();
// 	if (!item) return;

// 	hoveredOption.value = distributionOptions.value[index];
// 	// nextTick() waiting for dropdown to render before getting position and height
// 	await nextTick();
// 	const rect = item.getBoundingClientRect();
// 	const viewport = window.innerHeight;

// 	const element = cardRef.value.tooltipRef.getBoundingClientRect();
// 	const cardBottom = element.height + rect.bottom;

// 	let top = rect.y / rect.top + window.scrollY - 35;
// 	if (cardBottom > viewport) {
// 		top -= Math.abs(cardBottom - viewport) + 35;
// 	}

// 	popupPosition.value = {
// 		top,
// 		left: rect.x / rect.left + rect.width
// 	};
// }

function onChange(event: { value: DistributionType }) {
	const distribution = getParameterDistribution(props.modelConfiguration, props.parameterId, true);
	distribution.type = event.value;
	distribution.parameters = {};
	emit('update-parameter', {
		id: props.parameterId,
		distribution
	});
	hidePopup();
}

function isParameterInputEmpty(parameter: ModelDistribution) {
	if (distributions[parameter.type].parameters.length === 1) {
		return isNumberInputEmpty(distributions[parameter.type].parameters[0]);
	}
	return (
		isNumberInputEmpty(distributions[parameter.type].parameters[0]) ||
		isNumberInputEmpty(distributions[parameter.type].parameters[1])
	);
}

function onParameterChange(value: string, parameter: string) {
	isParameterEmpty.value = isNumberInputEmpty(value);
	const distribution = formatPayloadFromParameterChange({ [parameter]: value });
	if (!isParameterEmpty.value && distribution) {
		emit('update-parameter', {
			id: props.parameterId,
			distribution
		});
	}
}

function formatPayloadFromParameterChange(parameters: { [key: string]: string }) {
	const distribution = getParameterDistribution(props.modelConfiguration, props.parameterId, true);
	Object.keys(parameters).forEach((key) => {
		if (!distribution) return;
		distribution.parameters[key] = parameters[key];
	});
	return distribution;
}

onMounted(async () => {
	isParameterEmpty.value = isParameterInputEmpty(props.parameter);
});
</script>
<style scoped>
.popup {
	position: relative;
	border: 0;
	z-index: 3;
}
</style>
