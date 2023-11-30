<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<div>
				<div class="header p-buttonset">
					<Button
						label="Wizard"
						severity="secondary"
						icon="pi pi-sign-in"
						size="small"
						:active="activeTab === OptimizeTabs.wizard"
						@click="activeTab = OptimizeTabs.wizard"
					/>
					<Button
						label="Notebook"
						severity="secondary"
						icon="pi pi-sign-out"
						size="small"
						:active="activeTab === OptimizeTabs.notebook"
						@click="activeTab = OptimizeTabs.notebook"
					/>
				</div>
				<div class="container">
					<div class="left-side" v-if="activeTab === OptimizeTabs.wizard">
						<tera-intervention-policy-group-form
							v-for="(cfg, idx) in props.node.state.interventionPolicyGroups"
							:key="idx"
							:config="cfg"
							:index="idx"
							:model-node-options="modelNodeOptions"
							@update-self="updateInterventionPolicyGroupForm"
							@delete-self="() => deleteInterverntionPolicyGroupForm(idx)"
						/>
					</div>
					<div class="left-side" v-else-if="activeTab === OptimizeTabs.notebook"></div>

					<div class="right-side"></div>
				</div>
			</div>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref } from 'vue';
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import { ModelOptimizeOperationState, InterventionPolicyGroup } from './model-optimize-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum OptimizeTabs {
	wizard = 'wizard',
	notebook = 'notebook'
}

const activeTab = ref<OptimizeTabs>(OptimizeTabs.wizard);

interface PolicyDropdowns {
	parameters: string[];
	goals: string[];
	costBenefitFns: string[];
}

const modelNodeOptions = ref<PolicyDropdowns>({
	parameters: ['beta', 'gamma'],
	goals: ['Minimize', 'Maximize'],
	costBenefitFns: ['L1 Norm', 'L2 Norm']
});

const updateInterventionPolicyGroupForm = (data: InterventionPolicyGroup) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups[data.index] = data.updatedConfig;
	emit('update-state', state);
};

const deleteInterverntionPolicyGroupForm = (index: number) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups.splice(index, 1);
	emit('update-state', state);
};
</script>

<style scoped>
.left-side {
	width: 45%;
	padding-right: 2.5%;
}

.right-side {
	width: 45%;
	padding-left: 2.5%;
}
</style>
