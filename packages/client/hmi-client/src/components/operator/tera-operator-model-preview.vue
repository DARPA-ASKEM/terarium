<template>
	<SelectButton class="p-button-xsm" :model-value="view" :options="viewOptions" @change="handleChange" />
	<div class="container">
		<tera-model-diagram v-if="view === View.Diagram" :model="model" :feature-config="{ isPreview: true }" />
		<tera-model-equation v-else-if="view === View.Equation" :model="model" />
	</div>
</template>
<script setup lang="ts">
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';
import SelectButton from 'primevue/selectbutton';
import { ref } from 'vue';
import { Model } from '@/types/Types';

defineProps<{
	model: Model;
}>();

enum View {
	Diagram = 'Diagram',
	Equation = 'Equation'
}
const view = ref(View.Diagram);
const viewOptions = ref([View.Diagram, View.Equation]);
const handleChange = (event) => {
	if (event.value) {
		view.value = event.value;
	}
};
</script>

<style scoped>
.container {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: hidden;
}

.p-selectbutton {
	width: 100%;
}

.p-selectbutton:deep(.p-button) {
	flex-grow: 1;
}
</style>
