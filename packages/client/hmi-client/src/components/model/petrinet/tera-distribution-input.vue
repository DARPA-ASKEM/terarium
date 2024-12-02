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
					<section
						class="flex align-items-center w-full"
						@mouseover="showPopup(dropdownRef.$el, slotProps.index)"
						@focusin="() => showPopup(dropdownRef.$el, slotProps.index)"
					>
						<div>{{ slotProps.option.name }}</div>
					</section>
				</template>
			</Dropdown>
			<section class="popup" :style="{ top: `${popupPosition.top}px`, left: `${popupPosition.left}px` }">
				<Card v-if="hoveredOption" ref="cardRef" class="inside p-2">
					<template #title>
						{{ hoveredOption?.name }}
						<a
							v-if="externaDoclLinks[hoveredOption.value]"
							v-tooltip="hoveredOption.name + ' pytorch.org docs'"
							:href="externaDoclLinks[hoveredOption.value]"
							:aria-label="hoveredOption.name + ' docs'"
							rel="noopener noreferrer"
							target="_blank"
						>
							<span class="pi pi-external-link"></span>
						</a>
					</template>
					<template #content>
						{{ distributionDescription[hoveredOption?.value] }}
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
import { computed, ref, onMounted, nextTick } from 'vue';
import { getParameterDistribution, isNumberInputEmpty } from '@/services/model-configurations';
import { Model, ModelConfiguration, ModelDistribution } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import {
	DistributionType,
	externaDoclLinks,
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
const cardRef = ref();
const isParameterEmpty = ref<boolean>(false);
const distributionOptions = ref<{ name: string; value: string }[]>(distributionTypeOptions());
const hoveredOption = ref<{ name: string; value: string } | null>();
const popupPosition = ref({ top: 0, left: 0 });

function hidePopup() {
	hoveredOption.value = null;
}

const cardElement = computed(() => cardRef.value.$el.getBoundingClientRect());

async function showPopup(item: HTMLElement | null, index: number) {
	hidePopup();
	if (!item) return;

	hoveredOption.value = distributionOptions.value[index];
	await nextTick();
	const rect = item.getBoundingClientRect();
	const viewport = window.innerHeight;
	const cardBottom = cardElement.value.height + rect.bottom;

	let top = rect.y / rect.top + window.scrollY - 35;
	if (cardBottom > viewport) {
		top -= Math.abs(cardBottom - viewport) + 35;
	}

	popupPosition.value = {
		top,
		left: rect.x / rect.left + rect.width
	};
}

function onChange(event: { value: DistributionType }) {
	emit('update-parameter', {
		id: props.parameterId,
		distribution: formatPayloadFromTypeChange(event.value)
	});
	hidePopup();
}

function isParameterInputEmpty(parameter: ModelDistribution) {
	if (DistributionInputOptions[parameter.type].length === 1) {
		return isNumberInputEmpty(DistributionInputOptions[parameter.type][0]);
	}
	return (
		isNumberInputEmpty(DistributionInputOptions[parameter.type][0]) ||
		isNumberInputEmpty(DistributionInputOptions[parameter.type][1])
	);
}

function onParameterChange(value: string, parameter: string) {
	isParameterEmpty.value = isNumberInputEmpty(value);
	if (!isParameterEmpty.value) {
		emit('update-parameter', {
			id: props.parameterId,
			distribution: formatPayloadFromParameterChange({ [parameter]: value })
		});
	}
}

function formatPayloadFromParameterChange(parameters: { [x: string]: string }) {
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
