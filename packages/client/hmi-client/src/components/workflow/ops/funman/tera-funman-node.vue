<template>
	<section>
		<tera-operator-placeholder v-if="!inProgressId" :node="node">
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
import { FunmanOperationState, FunmanOperation } from '@/components/workflow/ops/funman/funman-operation';
import Button from 'primevue/button';
import { Poller, PollerState } from '@/api/api';
import { pollAction, getRunResult } from '@/services/models/simulation-service';
import { nodeOutputLabel } from '@/components/workflow/util';
import { logger } from '@/utils/logger';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();
const inProgressId = computed(() => props.node.state.inProgressId);

const poller = new Poller();

const addOutputPorts = async (runId: string) => {
	// The validated configuration id is set as the output value
	const rawFunmanResult = await getRunResult(runId, 'validation.json');
	if (!rawFunmanResult) {
		logger.error('Failed to fetch funman result');
		return;
	}
	const validatedConfigurationId = JSON.parse(rawFunmanResult).modelConfiguration.id;

	const portLabel = props.node.inputs[0].label;

	const outState = _.cloneDeep(props.node.state);
	outState.inProgressId = '';
	outState.runId = runId;

	emit('append-output', {
		label: nodeOutputLabel(props.node, `${portLabel} Result`),
		type: FunmanOperation.outputs[0].type,
		value: validatedConfigurationId,
		state: outState
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
		console.log(props.node);
		console.log('Polling funman status', id);

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
