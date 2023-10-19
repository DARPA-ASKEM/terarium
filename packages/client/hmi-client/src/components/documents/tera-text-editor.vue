<template>
	<v-ace-editor
		v-model:value="text"
		@init="initialize"
		theme="chrome"
		style="height: 100%; width: 100%"
		class="ace-editor"
	/>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';

const props = defineProps<{
	initialText: string;
}>();

const text = ref(props.initialText);
const selectedText = ref('');
const editor = ref<VAceEditorInstance['_editor'] | null>(null);

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}
</script>

<styled scoped></styled>
