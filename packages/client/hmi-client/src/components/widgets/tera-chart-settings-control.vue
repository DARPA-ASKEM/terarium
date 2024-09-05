<template>
	<aside>
		<MultiSelect
			v-model="selectedVariable"
			:options="options"
			:selection-limit="!multiSelect ? 1 : undefined"
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
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import MultiSelect from 'primevue/multiselect';
import { ChartSetting, ChartSettingType } from '@/types/workflow';

const props = defineProps<{
	settings: ChartSetting[];
	selectedSettingsIds: string[];
	type: ChartSettingType;
	multiSelect: boolean;
}>();

const emit = defineEmits(['configuration-change']);
const options = computed(() =>
	props.settings.filter((setting) => setting.type === props.type).map((setting) => setting.name)
);

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
