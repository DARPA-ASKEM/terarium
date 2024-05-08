<template>
	<QuillEditor theme="snow" v-model:content="text" contentType="text" />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const props = defineProps<{
	initialText: string;
}>();

const text = ref(props.initialText);

watch(
	() => props.initialText,
	() => {
		text.value = props.initialText;
	},
	{ immediate: true }
);
</script>

<!-- scoped is omitted because :deep modifier was not working -->
<style>
.ql-snow strong {
	font-weight: 600;
}

.ql-snow em {
	font-style: italic;
}
</style>
