<template>
	<Accordion multiple :active-index="[0, 1, 2, 3]">
		<AccordionTab header="Model diagram">
			<tera-model-diagram
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="!featureConfig.isPreview"
				@update-model="updateModelContent"
			/>
		</AccordionTab>
		<AccordionTab header="Model equations">
			<tera-model-equation
				:model="model"
				:is-editable="!featureConfig.isPreview"
				@update-diagram="updateDiagramFromEquation"
			/>
		</AccordionTab>
		<AccordionTab header="Model observables">
			<tera-model-observable
				:model="model"
				:is-editable="!featureConfig.isPreview"
				@update-model="updateModelContent"
			/>
		</AccordionTab>
		<AccordionTab header="Model configurations"></AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelObservable from '@/components/model/petrinet/tera-model-observable.vue';
import { FeatureConfig } from '@/types/common';
import { Model } from '@/types/Types';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';

defineProps<{
	model: Model;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-model']);

const teraModelDiagramRef = ref();
function updateDiagramFromEquation(updatedModel: Model) {
	teraModelDiagramRef.value.rendererGraph(updatedModel);
}

function updateModelContent(updatedModel: Model) {
	emit('update-model', updatedModel);
}
</script>
