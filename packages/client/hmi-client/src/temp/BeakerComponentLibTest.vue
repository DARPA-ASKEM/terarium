<template>
	<BeakerCodeCell />
</template>

<script setup lang="ts">
import BeakerCodeCell from 'beaker-vue/src/components/cell/BeakerCodeCell.vue';
import { ref } from 'vue';

const previewData = ref<any>();

// const cellComponentMapping = {
//     'code': BeakerCodeCell,
//     'markdown': BeakerMarkdownCell,
//     'query': BeakerLLMQueryCell,
//     'raw': BeakerRawCell,
// }

const sessionId = 'dev_session';

const statusChanged = (newStatus) => {
	connectionStatus.value = newStatus == 'idle' ? 'connected' : newStatus;
};

const connectionStatus = ref('connecting');

const iopubMessage = (msg) => {
	if (msg.header.msg_type === 'preview') {
		previewData.value = msg.content;
	}
};
</script>
