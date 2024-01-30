<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="SubsetDataTabs.Wizard">
			<tera-drilldown-section>
				<InputText placeholder="What do you want to do?" />
				<h3>Select geo-boundaries</h3>
				<SelectButton :model-value="selectedBoundaryType" :options="boundaryOptions"></SelectButton>
			</tera-drilldown-section>
		</div>
		<div :tabName="SubsetDataTabs.Notebook"></div>
		<template #preview>
			<tera-drilldown-preview>sdsd</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import InputText from 'primevue/inputtext';
import SelectButton from 'primevue/selectbutton';
import { SubsetDataOperationState } from './subset-data-operation';

// const props =
defineProps<{
	node: WorkflowNode<SubsetDataOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum SubsetDataTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum Boundaries {
	Area = 'Area',
	SinglePoint = 'Single point'
}

const selectedBoundaryType = ref(Boundaries.Area);
const boundaryOptions = ref([Boundaries.Area, Boundaries.SinglePoint]);
</script>
