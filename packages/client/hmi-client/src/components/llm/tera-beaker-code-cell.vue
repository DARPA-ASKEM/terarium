<template>
	<v-ace-editor
		:value="code"
		@init="initialize"
		theme="chrome"
		style="height: 100%; width: 100%"
		class="ace-editor"
		:options="{
			showPrintMargin: false,
			maxLines: 1000,
			enableBasicAutocompletion: true,
			enableLiveAutocompletion: true,
			enableSnippets: true
		}"
		:lang="language"
	/>
</template>
<script setup lang="ts">
import '@/ace-config';
import { computed, ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { SessionContext } from '@jupyterlab/apputils';
import { JupyterMessage } from '@/services/jupyter';

const props = defineProps<{
	jupyterMessage: JupyterMessage;
	jupyterSession: SessionContext;
}>();

const language = computed(() => props.jupyterMessage.content.language);

const code = computed(() => props.jupyterMessage.content.code);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const initialize = (editorInstance) => {
	editor.value = editorInstance;
};
</script>
