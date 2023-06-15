<template>
	<div>
		<div>
			<tera-chatty-input
				:llm-context="jupyterSession"
				@update-kernel-status="updateKernelStatus"
				@new-message="newJupyterResponse"
				context="dataset"
				:context_info="{
					id: props.assetId
				}"
			/>
			<tera-jupyter-response-window
				v-for="(message, index) in messagesHistory"
				:key="index"
				:jupyter-session="jupyterSession"
				:asset-id="props.assetId"
				:msg="message"
			/>
		</div>
		<!-- <div>{{test}}</div> -->
	</div>
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { ref, watch, onUnmounted } from 'vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import TeraChattyInput from '@/components/llm/tera-chatty-input.vue';
import { newSession, JupyterMessage } from '@/services/jupyter';
import TeraJupyterResponseWindow from '@/components/llm/tera-jupyter-response-window.vue';

const jupyterSession = newSession('llmkernel', 'ChattyNode');
console.log(jupyterSession);
const messagesHistory = ref<JupyterMessage[]>([]);

const emit = defineEmits(['update-table-preview', 'jupyter-event', 'update-kernel-status']);

const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
	showHistory?: { value: boolean; default: false };
}>();

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);

const updateKernelStatus = (kernelStatus) => {
	emit('update-kernel-status', kernelStatus);
};

const newJupyterResponse = (jupyterResponse) => {
	if (
		['stream', 'code_cell', 'llm_request', 'chatty_response'].indexOf(
			jupyterResponse.header.msg_type
		) > -1
	) {
		messagesHistory.value.push(jupyterResponse);
		emit('jupyter-event', messagesHistory.value);
	} else if (jupyterResponse.header.msg_type === 'dataset') {
		emit('update-table-preview', jupyterResponse.content);
	} else {
		console.log('Unknown Jupyter event', jupyterResponse);
	}
};
// 2ef46c20-2568-4565-b46c-719863bd6fd2
onUnmounted(() => {
	console.log('jupyter chat unmounted');
	jupyterSession.shutdown();
	// jupyterSession.changeKernel()
});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}
</style>
