<template>
	<template>
		<div class="container">test</div>
	</template>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, watch } from 'vue';
import { Code } from '@/types/Types';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
	droppedCodeAssetId: null | string;
	codeAssets: Code[];
}>();

const emit = defineEmits(['select-code-asset']);

const code = ref<Code | null>(null);

onMounted(async () => {
	if (props.droppedCodeAssetId) {
		code.value = props.codeAssets.find(({ id }) => id === props.droppedCodeAssetId) ?? null;
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
watch(
	() => props.node.state?.codeAssetId,
	async () => {
		console.log('here');
		// await fetchModel();
	}
);
</script>

<style scoped></style>
