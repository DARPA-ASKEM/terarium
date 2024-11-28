<template>
	<span class="flex gap-2">
		<section>
			<Dropdown
				:id="parameterId"
				ref="dropdownRef"
				:model-value="getParameterDistribution(modelConfiguration, parameterId).type"
				@change="
					emit('update-parameter', {
						id: parameterId,
						distribution: formatPayloadFromTypeChange($event.value)
					})
				"
				@show="onDropdownShow()"
				@hide="hidePopup()"
				option-label="name"
				option-value="value"
				:options="distributionOptions"
				class="mr-3 parameter-input"
			>
				<template #option="slotProps">
					<div class="flex align-items-center" v-tooltip="tool">
						<div>{{ slotProps.option.name }}</div>
					</div>
				</template>
			</Dropdown>
			<div
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
								<p>
									{{ distributionParamDescription[DistributionInputOptions[hoveredOption.value][index]] }}
								</p>
							</li>
						</ul>
					</template>
				</Card>
			</div>
		</section>
		<template v-for="(option, index) in options" :key="index">
			<tera-input-number
				:label="DistributionInputLabels[parameter.type][index]"
				:model-value="getParameterDistribution(modelConfiguration, parameterId, true)?.parameters[option]"
				error-empty
				@update:model-value="onParameterChange($event, DistributionInputOptions[parameter.type][index])"
				class="parameter-input"
			/>
		</template>
	</span>
</template>
<script setup lang="ts">
import { computed, ref, nextTick, onMounted, onUnmounted } from 'vue';
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

const tool = 'Distribution: \n\n this is the current option \n this is the current option';
const dropdownRef = ref();
const isParameterEmpty = ref(false);
const distributionOptions = ref(distributionTypeOptions());
const hoveredOption = ref();
const popupPosition = ref({ top: 0, left: 0 });

let dropdownPanel;

async function onDropdownShow() {
	// Wait for the dropdown panel to be rendered and attach hover events
	await nextTick();
	dropdownPanel = document.querySelector('.p-dropdown-panel');
	const test = dropdownRef.value.$el;
	if (dropdownPanel) {
		const items = dropdownPanel.querySelectorAll('.p-dropdown-item');
		items.forEach((item: HTMLElement, index) => {
			item.addEventListener('mouseenter', () => showPopup(test, index));
			item.addEventListener('mouseleave', hidePopup);
		});
	}
}

function showPopup(item: HTMLElement, index) {
	hoveredOption.value = distributionOptions.value[index];
	const rect = item.getBoundingClientRect();
	popupPosition.value = {
		top: rect.y / rect.top + window.scrollY,
		left: rect.x / rect.left + rect.width
	};
}

function hidePopup() {
	hoveredOption.value = null;
}

onUnmounted(() => {
	if (dropdownPanel) {
		const items = dropdownPanel.querySelectorAll('.p-dropdown-item');
		items.forEach((item) => {
			item.removeEventListener('mouseenter', showPopup);
			item.removeEventListener('mouseleave', hidePopup);
		});
	}
});

function isParameterInputEmpty(parameter) {
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
	/* background-color: red; */
	/* border: 1px solid #ccc; */
	/* padding: 10px; */
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
	z-index: 1000;
}
.inside {
	position: absolute;
	/* background-color: red; */
	min-height: 100px;
	min-width: 300px;
}
</style>
