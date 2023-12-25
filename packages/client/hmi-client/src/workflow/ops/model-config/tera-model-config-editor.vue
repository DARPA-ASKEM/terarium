<template>
	<h4>Parameters</h4>
	<Accordion v-if="stratifiedModelType && !_.isEmpty(parameters)" :multiple="true">
		<AccordionTab v-for="[param, vals] in parameters" :key="param">
			<template #header>
				{{ param }}
			</template>
			<div v-for="val in vals" :key="val">
				<Button
					:label="val"
					class="asset-button"
					plain
					text
					size="small"
					@click="getClickedParam(val)"
				/>
			</div>
		</AccordionTab>
	</Accordion>
	<div
		v-else-if="!stratifiedModelType && !_.isEmpty(parameters)"
		v-for="[param, vals] in parameters"
		:key="param"
	>
		<Button
			:label="param"
			class="asset-button"
			plain
			text
			size="large"
			@click="getClickedParam(vals[0])"
		/>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { Model } from '@/types/Types';
import { getCatlabAMRPresentationData } from '@/model-representation/petrinet/catlab-petri';
import {
	getMiraAMRPresentationData,
	getUnstratifiedParameters2
	// extractNestedStratas
} from '@/model-representation/petrinet/mira-petri';
import {
	getStratificationType,
	StratifiedModel
} from '@/model-representation/petrinet/petrinet-service';

const props = defineProps<{
	model: Model;
}>();

const configModel = ref<Model>(props.model);
const stratifiedModelType = computed(() => getStratificationType(props.model));
const baseModel = computed<any>(() => {
	if (stratifiedModelType.value === StratifiedModel.Catlab) {
		return getCatlabAMRPresentationData(props.model).compactModel;
	}
	if (stratifiedModelType.value === StratifiedModel.Mira) {
		return getMiraAMRPresentationData(props.model).compactModel.model;
	}
	return props.model.model;
});

const parameters = computed<Map<string, string[]>>(() => getUnstratifiedParameters2(props.model));

const getClickedParam = (param) => {
	const selectedParam = props.model.semantics?.ode.parameters?.find((p) => p.id === param);
	console.log(selectedParam);
};

watch(
	() => props.model,
	() => {
		console.log(configModel.value);
		console.log(stratifiedModelType.value);
		console.log(baseModel.value);
		console.log(parameters.value);
	},
	{ immediate: true }
);
</script>
