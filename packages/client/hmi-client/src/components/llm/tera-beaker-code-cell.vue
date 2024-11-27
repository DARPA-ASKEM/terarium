<template>
	<div class="code-cell-row" @keyup.ctrl.enter.prevent="runCell">
		<pre class="m-0 pt-2">In  [{{ jupyterMessage.content?.execution_count ?? '*' }}]</pre>
		<v-ace-editor
			:value="code"
			@init="initialize"
			theme="chrome"
			class="ace-editor w-full p-0"
			:options="{
				showPrintMargin: false,
				maxLines: 1000,
				enableBasicAutocompletion: true,
				enableLiveAutocompletion: true
			}"
			:lang="language"
		/>
		<div class="flex pt-1">
			<Button text rounded icon="pi pi-trash" class="danger-hover" @click="onDelete" />
			<Button text rounded icon="pi pi-play" class="" @click="runCell" />
		</div>
	</div>
</template>
<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref } from 'vue';
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
	index: Number;
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
		header: 'Delete cell',
		message: 'Are you sure you want to delete this cell?',
		accept: () => {
			emit('deleteRequested');
		}
	});
};

onMounted(() => {
	if (props.notebookItem.autoRun) {
		runCell();
	}
	window.addEventListener('run-all-cells', () => {
		setTimeout(() => {
			runCell();
		}, 1000 * props.index.valueOf()); // ensures the cells are run in the correct order
	});
});

onBeforeUnmount(() => {
	window.removeEventListener('run-all-cells', runCell); // Clean up listener
});

defineExpose({
	enter,
	runCell
});
</script>
<style scoped>
.code-cell-row {
	display: flex;
	position: relative;
	align-items: start;
	gap: var(--gap-5);
	padding-bottom: var(--gap-3);
}

.ace_editor {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-2);
	margin-left: 1px;
	align-self: center;
}

.danger-hover:hover {
	color: var(--red-700);
}
/* Ace editor style overrides */
:deep(.ace_active-line) {
	background: transparent !important;
}
:deep(.ace_gutter-active-line) {
	background: transparent !important;
}
:deep(.ace_gutter) {
	height: 100%;
}
</style>
