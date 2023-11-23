<template>
	<main>code asset node</main>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, watch } from 'vue';
import { Code } from '@/types/Types';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
	codeAssets: Code[];
}>();

const emit = defineEmits(['select-code-asset']);

const code = ref<Code | null>(null);

onMounted(async () => {
	if (props.node.state.codeAssetId) {
		code.value = props.codeAssets.find(({ id }) => id === props.node.state.codeAssetId) ?? null;
	}
});

watch(
	() => code.value,
	() => {
		if (code.value?.id) {
			emit('select-code-asset', { id: code.value.id });
		}
	}
);
</script>

<style scoped></style>
