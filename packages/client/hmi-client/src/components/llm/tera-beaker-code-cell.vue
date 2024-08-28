<template>
	<div class="flex relative" @keyup.ctrl.enter.prevent="runCell">
		<pre>In  [{{ jupyterMessage.content?.execution_count ?? '*' }}]</pre>
		<v-ace-editor
			:value="code"
			@init="initialize"
			theme="chrome"
			class="ace-editor ml-5 w-full"
			:options="{
				showPrintMargin: false,
				maxLines: 1000,
				enableBasicAutocompletion: true,
				enableLiveAutocompletion: true
			}"
			:lang="language"
		/>
		<div class="flex">
			<Button text rounded icon="pi pi-trash" class="danger-hover pt-0" @click="onDelete" />
			<Button text rounded icon="pi pi-play" class="pt-0" @click="runCell" />
		</div>
	</div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { SessionContext } from '@jupyterlab/apputils';
import { INotebookItem, JupyterMessage, renderMime } from '@/services/jupyter';
import Button from 'primevue/button';
import { CodeCell, CodeCellModel } from '@jupyterlab/cells';
import { useConfirm } from 'primevue/useconfirm';

const props = defineProps<{
	jupyterMessage: JupyterMessage;
	jupyterSession: SessionContext;
	notebookItem: INotebookItem;
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

	CodeCell.execute(cellWidget, props.jupyterSession, { notebook_item: props.notebookItem.query_id });
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

onMounted(() => {
	if (props.notebookItem.autoRun) {
		runCell();
	}
});

defineExpose({
	enter,
	runCell
});
</script>
