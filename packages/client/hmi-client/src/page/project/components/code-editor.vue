<template>
	<div class="code">
		<div class="controls">
			<Button
				label="Extract Model"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractModel"
			/>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load File"
			/>
		</div>
		<v-ace-editor
			v-model:value="code"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
		/>
	</div>
</template>
<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { ref } from 'vue';
import { logger } from '@/utils/logger';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import API from '@/api/api';

const props = defineProps({
	initialCode: {
		type: String,
		default: '# Paste some python code here or import from the controls above'
	}
});

const code = ref(props.initialCode);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');

/**
 * File open/add event handler.  Immediately render the contents of the file to the editor
 * content
 * @param event	the input event when a file is added
 */
async function onFileOpen(event) {
	const reader = new FileReader();
	reader.readAsText(event.files[0], 'UTF-8');
	reader.onload = (evt) => {
		code.value = evt?.target?.result?.toString() ?? props.initialCode;
	};
}

/**
 * Send the selected contents of the editor to the backend for persistence and model extraction
 * via TA1
 */
async function onExtractModel() {
	logger.info(`Transforming: ${selectedText.value}`);
	const response = await API.post('/code', selectedText.value);
	// eslint-disable-next-line
	alert(JSON.stringify(response.data));
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}
</script>

<style>
.code {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding-top: 1rem;
}

.controls {
	margin-left: 40px;
	margin-bottom: 10px;
	display: flex;
	gap: 10px;
}
</style>
