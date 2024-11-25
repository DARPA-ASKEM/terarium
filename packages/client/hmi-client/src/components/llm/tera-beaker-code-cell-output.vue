<template>
	<div class="output-area" ref="outputAreaRef" />
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
		const imgElement = outputArea.node.querySelector('img');
		if (imgElement) {
			imgElement.draggable = false;
		}
		outputAreaRef.value.replaceChildren(outputArea.node);
	}
};

const formatOutputs = () => {
	const outputs: any[] = [];
	const content = props.jupyterMessage.content;
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

const clear = () => {
	renderOutputs([]);
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

defineExpose({
	clear
});
</script>

<style scoped>
div {
	&.output-area {
		&:deep(.lm-Widget.p-Widget.jp-OutputPrompt.jp-OutputArea-prompt) {
			display: none;
		}
	}

	&:not(.isPreview) {
		&:deep(.lm-Widget.p-Widget.jp-OutputPrompt.jp-OutputArea-prompt) {
			color: var(--text-color);
			margin-right: var(--gap-5);
		}
		&:deep(.lm-Widget.p-Widget.lm-Panel.p-Panel.jp-OutputArea-child) {
			margin-bottom: var(--gap-2);
		}
		&:deep(.lm-Widget.p-Widget.jp-OutputPrompt.jp-OutputArea-prompt):before {
			content: 'Out ';
			font-family: 'Menlo', 'Consolas', 'DejaVu Sans Mono', monospace;
			color: var(--text-color);
		}
	}
}
</style>
