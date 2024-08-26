<template>
	<div ref="outputAreaRef" />
</template>

<script setup lang="ts">
import { JupyterMessage, renderMime } from '@/services/jupyter';
import { computed, onMounted, ref, watch } from 'vue';
import { OutputArea, OutputAreaModel } from '@jupyterlab/outputarea';

const props = defineProps<{
	jupyterMessage: JupyterMessage;
}>();

const outputAreaRef = ref<HTMLDivElement | null>(null);

const renderOutputs = (outputs) => {
	const outputModel = new OutputAreaModel({ trusted: true });
	outputModel.fromJSON(outputs);

	const outputArea = new OutputArea({
		model: outputModel,
		rendermime: renderMime
	});

	if (outputAreaRef.value) {
		outputAreaRef.value.replaceChildren(outputArea.node);
	}
};

const formatOutputs = () => {
	const outputs: any[] = [];
	const content = props.jupyterMessage.content;
	delete content.execution_count;
	outputs.push({
		output_type: messageType.value,
		...content
	});

	return outputs;
};

const updateOutputs = () => {
	const outputs = formatOutputs();
	renderOutputs(outputs);
};

onMounted(() => {
	updateOutputs();
});

watch(
	() => props.jupyterMessage,
	() => {
		updateOutputs();
	}
);

const messageType = computed(() => props.jupyterMessage.header.msg_type);
</script>
