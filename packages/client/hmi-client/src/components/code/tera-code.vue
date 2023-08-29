<template>
	<header>
		<section>
			<h4>{{ name }}</h4>
			<section>
				<Button label="Extract model" />
			</section>
		</section>
	</header>
	<v-ace-editor
		v-model:value="code"
		@init="initialize"
		lang="python"
		theme="chrome"
		style="height: 100%; width: 100%"
		class="ace-editor"
	/>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import Button from 'primevue/button';

const name = ref('New code');
const code = ref('# Paste some code here');
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
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

<style scoped>
header {
	padding: 0.5rem 1rem;
	background-color: var(--surface-section);
}

header > section {
	display: flex;
	justify-content: space-between;
	flex-direction: row;
}

h4 {
	margin-top: 0.25rem;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
}
</style>
