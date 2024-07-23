<template>
	<aside>
		<MultiSelect
			v-if="multiSelect"
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
		<Dropdown
			v-else
			placeholder="Select variable to display"
			v-model="selectedVariable"
			:options="variables"
			@change="updateSelectedVariable"
		/>
		<Button v-if="showRemoveButton" title="Remove chart" icon="pi pi-trash" @click="$emit('remove')" rounded text />
	</aside>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import { ChartConfig } from '@/types/SimulateConfig';

const props = defineProps<{
	variables: string[];
	chartConfig: ChartConfig;
	showRemoveButton: boolean;
	multiSelect: boolean;
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
aside {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: var(--gap-2);
	justify-content: space-between;
	position: relative;
	margin: var(--gap-2) 0;
}
</style>
