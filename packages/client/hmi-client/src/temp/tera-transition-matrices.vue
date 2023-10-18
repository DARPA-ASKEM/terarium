<template>
	<div v-for="baseTransition in baseTransitions">
		<h4>{{ baseTransition }}</h4>
		<tera-stratified-matrix
			:id="baseTransition"
			:model-configuration="modelConfiguration"
			:stratified-model-type="StratifiedModelType.Mira"
			:ode-type="OdeSemantic.Rates"
			:should-eval="true"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import TeraStratifiedMatrix from '@/components/model/petrinet/model-configurations/tera-stratified-matrix.vue';
import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
import { OdeSemantic } from '@/types/common';
import { ModelConfiguration } from '@/types/Types';
import { getMiraAMRPresentationData } from '@/model-representation/petrinet/mira-petri';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
}>();

const baseTransitions = computed(() => {
	const transitionMatrixData: any[] = getMiraAMRPresentationData(
		props.modelConfiguration.configuration
	).transitionMatrixData;

	const baseTransitionNames: string[] = [];
	for (let i = 0; i < transitionMatrixData.length; i++) {
		baseTransitionNames.push(transitionMatrixData[i].base);
	}
	return [...new Set(baseTransitionNames)].sort();
});
</script>
