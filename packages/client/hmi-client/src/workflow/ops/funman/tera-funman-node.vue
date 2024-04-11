<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<Button @click="emit('open-drilldown')" label="Review checks" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { watch, onUnmounted } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { FunmanOperationState, FunmanOperation } from '@/workflow/ops/funman/funman-operation';
import Button from 'primevue/button';
import { Poller, PollerState } from '@/api/api';
import { getQueries } from '@/services/models/funman-service';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

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
		.setPollAction(async () => {
			const response = await getQueries(runId);
			if (response.done && response.done === true) {
				return { data: response } as any;
			}
			return { data: null } as any;
		});
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		console.error(`Funman: ${runId} has failed`);
		throw Error('Failed Funman validation');
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
			const state = _.cloneDeep(props.node.state);
			state.inProgressId = '';
			emit('update-state', state);
		}
	},
	{ immediate: true }
);
</script>

<style scoped></style>
