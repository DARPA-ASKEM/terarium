<template>
	<section>
		<tera-operator-placeholder v-if="!inProgressId" :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>

		<tera-progress-spinner v-if="inProgressId" :font-size="2" is-centered style="height: 100%" />

		<template v-if="node.inputs[0].value">
			<Button @click="emit('open-drilldown')" label="Review checks" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { watch, computed, onUnmounted } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { WorkflowNode } from '@/types/workflow';
import {
	FunmanOperationState,
	FunmanOperation
} from '@/components/workflow/ops/funman/funman-operation';
import Button from 'primevue/button';
import { Poller, PollerState } from '@/api/api';
import { pollAction } from '@/services/models/simulation-service';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();
const inProgressId = computed(() => props.node.state.inProgressId);

const poller = new Poller();

const addOutputPorts = async (runId: string) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output', {
		label: `${portLabel} Result ${props.node.outputs.length + 1}`,
		type: FunmanOperation.outputs[0].type,
		value: runId,
		state: _.cloneDeep(props.node.state)
	});
};

const getStatus = async (runId: string) => {
	poller
		.setInterval(5000)
		.setThreshold(100)
		.setPollAction(async () => pollAction(runId));

	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		console.error(`Funman: ${runId} has failed`, pollerResults);
	}
	return pollerResults;
};

onUnmounted(() => {
	poller.stop();
});

watch(
	() => props.node.state.inProgressId,
	async (id) => {
		if (!id || id === '') return;

		const response = await getStatus(id);
		if (response.state === PollerState.Done) {
			addOutputPorts(id);
		}

		const state = _.cloneDeep(props.node.state);
		state.inProgressId = '';
		emit('update-state', state);
	},
	{ immediate: true }
);
</script>

<style scoped></style>
