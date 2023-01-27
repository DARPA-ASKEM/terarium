<template>
	<div class="code">
		<div class="controls">
			<Button
				label="Extract Model"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="uploadSelected"
			></Button>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="myUploader"
				mode="basic"
				auto
				chooseLabel="Load File"
			/>
		</div>
		<v-ace-editor
			v-model:value="content"
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
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import API from '@/api/api';

const DEFAULT_TEXT = '# Paste some python code here or import from the controls above';
const content = ref(DEFAULT_TEXT);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');

async function myUploader(event) {
	const reader = new FileReader();
	reader.readAsText(event.files[0], 'UTF-8');
	reader.onload = (evt) => {
		content.value = evt?.target?.result?.toString() ?? DEFAULT_TEXT;
	};
}

async function uploadSelected() {
	const payload = {
		files: ['test'],
		blobs: [selectedText.value],
		system_name: '',
		root_name: ''
	};
	console.log(`Transforming: ${selectedText.value}`);
	const response = await API.post('/code', payload);
	// eslint-disable-next-line
	alert(JSON.stringify(response.data));
}

function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
}
</script>
<style>
.code {
	padding: 20px 0;
}
.controls {
	margin-left: 40px;
	margin-bottom: 10px;
	display: flex;
	gap: 10px;
}
</style>
