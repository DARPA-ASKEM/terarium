<template>
	<div @keyup.ctrl.enter.prevent="runCell">
		<v-ace-editor
			:value="code"
			@init="initialize"
			theme="chrome"
			style="min-height: 30px; width: 100%"
			class="ace-editor"
			:options="{
				showPrintMargin: false,
				maxLines: 1000,
				enableBasicAutocompletion: true,
				enableLiveAutocompletion: true
			}"
			:lang="language"
		/>
		<div class="controls">
			<Button text rounded icon="pi pi-trash" class="danger-hover" @click="onDelete" />
			<Button text rounded icon="pi pi-play" @click="runCell" />
		</div>
	</div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { SessionContext } from '@jupyterlab/apputils';
import { JupyterMessage, renderMime } from '@/services/jupyter';
import Button from 'primevue/button';
import { CodeCell, CodeCellModel } from '@jupyterlab/cells';
import { useConfirm } from 'primevue/useconfirm';

const props = defineProps<{
	jupyterMessage: JupyterMessage;
	jupyterSession: SessionContext;
	notebookItemId: string;
	lang: string;
}>();

const emit = defineEmits(['deleteRequested']);

const confirm = useConfirm();

const language = computed(() => {
	let programmingLanguage = props.lang;
	if (programmingLanguage === 'python3') {
		programmingLanguage = 'python';
	} else if (programmingLanguage === 'ir') {
		programmingLanguage = 'r';
	}
	// editor.value?.session.setMode(`ace/mode/${programmingLanguage}`);
	return programmingLanguage;
});
// const language = ref<string | undefined>('python')

const code = computed(() => props.jupyterMessage.content.code);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const initialize = (editorInstance) => {
	editor.value = editorInstance;
};

const enter = () => {
	editor.value?.focus();
};

defineExpose({
	enter
});

const runCell = async () => {
	if (!editor.value) return;

	const codeContent = editor.value.getValue();
	const cellModel = new CodeCellModel({
		cell: {
			cell_type: 'code',
			metadata: {},
			source: codeContent
		}
	});
	const cellWidget = new CodeCell({
		model: cellModel,
		rendermime: renderMime
	});

	CodeCell.execute(cellWidget, props.jupyterSession, { notebook_item: props.notebookItemId });
};

const onDelete = () => {
	confirm.require({
		message: 'Are you sure you want to delete this cell?',
		icon: 'pi pi-exclamation-triangle',
		accept: () => {
			emit('deleteRequested');
		}
	});
};
</script>
