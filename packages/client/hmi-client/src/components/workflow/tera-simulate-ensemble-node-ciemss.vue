<template>
	<section v-if="!showSpinner" class="result-container">
		<div class="invalid-block" v-if="props.node.statusCode === WorkflowStatus.INVALID">
			<img class="image" src="@assets/svg/plants.svg" alt="" />
			<p>Configure in side panel</p>
		</div>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode, WorkflowStatus } from '@/types/workflow';
// import { RunResults } from '@/types/SimulateConfig';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import { EnsembleModelConfigs } from '@/types/Types';
import { EnsembleCiemssOperationState } from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();
// const emit = defineEmits(['append-output-port']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const mapping = ref<EnsembleModelConfigs[]>([]);

watch(
	() => modelConfigIds.value,
	async () => {
		console.log('Updated node model config ids');
		if (modelConfigIds.value) {
			console.log(modelConfigIds.value);

			// Init ensemble Configs:
			for (let i = 0; i < modelConfigIds.value.length; i++) {
				mapping.value[i] = {
					id: modelConfigIds.value[i],
					observables: [],
					weight: 0
				};
			}
			console.log(mapping.value);
			const state: EnsembleCiemssOperationState = _.cloneDeep(props.node.state);
			state.modelConfigIds = modelConfigIds.value;
			state.mapping = mapping.value;
			workflowEventBus.emitNodeStateChange({
				workflowId: props.node.workflowId,
				nodeId: props.node.id,
				state
			});
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.result-container {
	align-items: center;
}
.image {
	height: 8.75rem;
	margin-bottom: 0.5rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	background-color: rgb(0, 0, 0, 0);
}
.invalid-block {
	display: contents;
}
.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
