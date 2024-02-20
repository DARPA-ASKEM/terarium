<template>
	<h1>Text Block</h1>

	<Editor v-model="textBlockContent" editorStyle="height: 320px" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Editor from 'primevue/editor';

// import { WorkflowNode } from '@/types/workflow';
// import { TextBlockState } from './text-block';

// const props = defineProps<{
//     node: WorkflowNode<TextBlock>;
// }>();

const textBlockContent = ref<string>('hello world');
</script>

<style scoped></style>
