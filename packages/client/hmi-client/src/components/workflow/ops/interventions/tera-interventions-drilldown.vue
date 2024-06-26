<template>
	<tera-drilldown :node="node" @on-close-clicked="emit('close')">
		<template #sidebar>
			<tera-slider-panel
				v-model:is-open="isSidebarOpen"
				content-width="360px"
				header="Intervention policies"
			>
				<template #content>
					<div class="m-3">
						<div class="flex flex-column gap-1">
							<tera-input v-model="filterInterventionsText" placeholder="Filter" />
						</div>
						<ul>
							<li v-for="policy in interventionPolicies" :key="policy.id">
								<tera-interventions-policy-card :interventionPolicy="policy" :selected="true" />
							</li>
						</ul>
					</div>
				</template>
			</tera-slider-panel>
		</template>
		<tera-drilldown-section>
			<tera-columnar-panel>
				<div>test1</div>
				<div>test2</div>
			</tera-columnar-panel>
		</tera-drilldown-section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import { ref } from 'vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import { uniqueId } from 'lodash';
import { DummyIntervention, InterventionsState } from './tera-interventions-operation';
import TeraInterventionsPolicyCard from './tera-interventions-policy-card.vue';

const interventionPolicies: DummyIntervention[] = [
	{
		id: uniqueId(),
		createdOn: new Date(),
		name: 'Intervention 1',
		description: 'Description of intervention 1',
		modelId: 'model1',
		parameterId: 'parameter1',
		staticValues: [
			{
				threshold: 0.5,
				timestep: 1
			}
		],
		dynamicValues: [
			{
				parameterId: 'parameter2',
				threshold: 0.5,
				timestep: 1
			}
		]
	},
	{
		id: uniqueId(),
		createdOn: new Date(),
		name: 'Intervention 2',
		description: 'Description of intervention 2',
		modelId: 'model2',
		parameterId: 'parameter2',
		staticValues: [
			{
				threshold: 0.5,
				timestep: 1
			}
		],
		dynamicValues: [
			{
				parameterId: 'parameter1',
				threshold: 0.5,
				timestep: 1
			}
		]
	}
];

defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();
const emit = defineEmits(['close']);

const isSidebarOpen = ref(true);
const filterInterventionsText = ref('');
</script>
