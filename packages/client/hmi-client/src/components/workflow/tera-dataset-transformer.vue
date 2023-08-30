<template>
	<div class="background">
		<Suspense>
			<tera-dataset-jupyter-panel
				asset-id="e3aa40fd-9eb5-4d11-8932-65d478b2bd1f"
				:asset-ids="assetIds"
				:project="props.project"
				:show-kernels="showKernels"
				:show-chat-thoughts="showChatThoughts"
			/>
		</Suspense>
	</div>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { IProject } from '@/types/Project';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraDatasetJupyterPanel from '@/components/dataset/tera-dataset-jupyter-panel.vue';
import { computed, onMounted, ref, watch } from 'vue';

const props = defineProps<{
	node: WorkflowNode;
	project: IProject;
}>();
const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const assetIds = computed(() =>
	props.node?.inputs
		.filter((inputNode) => inputNode.status === WorkflowPortStatus.CONNECTED && inputNode.value)
		.map((inputNode) => inputNode.value![0])
);

onMounted(() => {});
watch(
	() => props.node.inputs,
	async () => console.log('change input'),
	{ deep: true }
);
</script>

<style>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}
</style>
