<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #sidebar>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isInputSettingsOpen"
				header="Compare settings"
				content-width="440px"
			>
				<template #content>
					<tera-drilldown-section class="px-2">
						<Button class="ml-auto" size="small" label="Run" @click="onRun" />
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="DrilldownTabs.Wizard"> </tera-drilldown-section>

		<tera-drilldown-section :tabName="DrilldownTabs.Notebook"> </tera-drilldown-section>

		<template #sidebar-right>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isOutputSettingsOpen"
				header="Output settings"
				content-width="360px"
			>
				<template #content>
					<tera-drilldown-section> </tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DrilldownTabs } from '@/types/common';
import { ref } from 'vue';
import Button from 'primevue/button';
import { CompareDatasetsState } from './compare-datasets-operation';
// const props =
defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);

const onRun = () => {
	console.log('run');
};

const emit = defineEmits(['update-state', 'update-status', 'close']);
</script>

<style scoped></style>
