<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
		<div class="background">
			<Suspense>
				<tera-dataset-jupyter-regridding-panel
					:assets="assets"
					:show-kernels="showKernels"
					:show-chat-thoughts="showChatThoughts"
					@new-dataset-saved="addOutputPort"
					:notebook-session="notebookSession"
				/>
			</Suspense>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraDatasetJupyterRegriddingPanel from '@/components/dataset/tera-dataset-jupyter-regridding-panel.vue';
import { computed, onMounted, ref } from 'vue';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import type { NotebookSession } from '@/types/Types';
import { cloneDeep } from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import { RegriddingState } from './regridding-operation';

const props = defineProps<{
	node: WorkflowNode<RegriddingState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);

const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const assets = computed(() =>
	props.node.inputs
		.filter((inputNode) => inputNode.status === WorkflowPortStatus.CONNECTED && inputNode.value)
		.map((inputNode) => ({
			type: inputNode.type,
			id: inputNode.value![0]
		}))
);

const notebookSession = ref(<NotebookSession | undefined>undefined);

onMounted(async () => {
	const state = cloneDeep(props.node.state);
	let notebookSessionId = props.node.state?.notebookSessionId;
	if (!notebookSessionId) {
		// create a new notebook session log if it does not exist
		const response = await createNotebookSession({
			id: uuidv4(),
			name: props.node.id,
			description: '',
			data: { history: [] }
		});
		notebookSessionId = response?.id;

		if (notebookSessionId) {
			// update the node state with the notebook session id
			state.notebookSessionId = notebookSessionId;
			emit('update-state', state);
		}
	}

	notebookSession.value = await getNotebookSessionById(notebookSessionId!);
});

const addOutputPort = (data: any) => {
	emit('append-output', {
		id: data.id,
		label: data.name,
		type: 'datasetId',
		value: data.id
	});
};
</script>

<style scoped>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}
</style>
