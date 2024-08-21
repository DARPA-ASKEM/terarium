<template>
	<div v-if="messageType === 'stream'">{{ jupyterMessage.content.text }}</div>
	<div v-else-if="['display_data', 'execute_result'].includes(messageType)">test</div>
</template>

<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { computed } from 'vue';

const props = defineProps<{
	jupyterMessage: JupyterMessage;
}>();

const messageType = computed(() => props.jupyterMessage.header.msg_type);
</script>
