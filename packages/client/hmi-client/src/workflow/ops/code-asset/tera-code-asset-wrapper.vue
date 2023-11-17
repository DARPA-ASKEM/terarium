<template>
	<tera-code :asset-id="props.node?.state?.codeAssetId ?? ''" is-preview />
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraCode from '@/components/code/tera-code.vue';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();
</script>

<style scoped></style>
