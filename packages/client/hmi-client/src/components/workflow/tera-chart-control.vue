<template>
	<aside class="multiselect-container">
		<MultiSelect
			v-model="selectedVariable"
			:options="variables"
			placeholder="Select variables to display"
			@update:model-value="updateSelectedVariable"
			filter
		>
			<template v-slot:value>
				<template v-for="(variable, index) in selectedVariable" :key="index">
					<template v-if="index > 0">,&nbsp;</template>
					<span> {{ variable }} </span>
				</template>
			</template>
		</MultiSelect>
		<Button v-if="showRemoveButton" title="Remove chart" icon="pi pi-trash" @click="$emit('remove')" rounded text />
	</aside>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import Button from 'primevue/button';
import MultiSelect from 'primevue/multiselect';
import { ChartConfig } from '@/types/SimulateConfig';

const props = defineProps<{
	variables: string[];
	chartConfig: ChartConfig;
	showRemoveButton: boolean;
}>();

const emit = defineEmits(['configuration-change', 'remove']);

const selectedVariable = ref<string[]>(props.chartConfig.selectedVariable);

const updateSelectedVariable = () => {
	emit('configuration-change', {
		selectedVariable: selectedVariable.value,
		selectedRun: props.chartConfig.selectedRun
	});
};
</script>

<style scoped>
.multiselect-container {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: var(--gap-2);
	justify-content: space-between;
	position: relative;
	margin: 0 var(--gap-2);
}
</style>
