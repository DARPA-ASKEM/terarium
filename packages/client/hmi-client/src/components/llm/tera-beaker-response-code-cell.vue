<template>
	<div class="code-cell" @keyup.ctrl.enter.prevent="run">
		<template ref="codeCell" />
		<div class="controls" :class="{ 'controls-with-query': isQuestion }">
			<Button text rounded icon="pi pi-trash" class="danger-hover" @click="showDialog" />
			<Button text rounded icon="pi pi-play" @click="run" />
		</div>
	</div>

	<!-- Are you sure? dialog -->
	<Dialog v-model:visible="showConfirmDialog" modal :closable="false" class="w-3" header="Are you sure?">
		<div class="mt-3">This cannot be undone.</div>
		<template #footer>
			<Button label="No" @click="cancelDelete" severity="secondary" outlined />
			<Button label="Yes" @click="confirmDelete" class="p-button-danger" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { CodeCell, CodeCellModel } from '@jupyterlab/cells';
import { SessionContext } from '@jupyterlab/apputils';
import { Completer, CompleterModel, CompletionHandler, CompletionConnector } from '@jupyterlab/completer';
import { CommandRegistry } from '@lumino/commands';
import { mimeService, renderMime } from '@/services/jupyter';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';

const props = defineProps({
	jupyterSession: {
		type: Object as () => SessionContext,
		required: true,
		default: null
	},
	focusInput: {
		type: Boolean,
		default: false
	},
	language: {
		type: String,
		default: 'python'
	},
	code: {
		type: String,
		default: ''
	},
	autorun: {
		type: Boolean,
		default: false
	},
	context: {
		type: String,
		default: null
	},
	context_info: {
		type: Object,
		default: () => {}
	},
	// savedName: {
	// 	type: String,
	// 	default: 'Dataset Name_'
	// },
	postFix: {
		type: String,
		default: 'new'
	},
	notebookItemId: {
		type: String,
		default: null
	}
});

const isQuestion = computed<boolean>(() => props.context_info?.query !== null);

// const emit = defineEmits(['save-as-new-dataset']);
// const savedFileName = ref<string>('');
// const hasValidFileName = computed<boolean>(() => savedFileName.value !== '');
const codeCell = ref<HTMLElement | null>(null);

// Initialize cellWidget
const cellWidget = new CodeCell({
	rendermime: renderMime,
	editorConfig: {
		lineNumbers: true
	},
	model: new CodeCellModel({
		cell: {
			cell_type: 'code',
			metadata: {},
			source: props.code
		}
	})
}).initializeState();

// Set up a completer
const editor = cellWidget.editor;
const model = new CompleterModel();
const completer = new Completer({ editor, model });
const connector = new CompletionConnector({ editor, session: props.jupyterSession.session });
const handler = new CompletionHandler({ completer, connector });

const commands = new CommandRegistry();

// Add the commands
commands.addCommand('invoke:completer', {
	execute: () => {
		handler.invoke();
	}
});
commands.addCommand('run:cell', {
	execute: () => CodeCell.execute(cellWidget, props.jupyterSession, { notebook_item: props.notebookItemId })
});

commands.addCommand('blur:editor', {
	label: 'Blur Editor',
	execute: () => {
		exit();
	}
});

// Add Key Bindings
commands.addKeyBinding({
	selector: '.jp-InputArea-editor.jp-mod-completer-enabled',
	keys: ['Tab'],
	command: 'invoke:completer'
});
commands.addKeyBinding({
	selector: '.jp-InputArea-editor',
	keys: ['Shift Enter'],
	command: 'run:cell'
});

commands.addKeyBinding({
	selector: '.jp-InputArea-editor',
	keys: ['Escape'],
	command: 'blur:editor'
});

// Save file function
// const saveAsNewDataset = () => {
// 	emit('save-as-new-dataset', { datasetName: savedFileName.value });
// 	console.log('save-as-new-dataset emitted');
// };

const clear = () => {
	cellWidget.model.clearExecution();
};

const enter = () => {
	cellWidget.editor.focus();
};

const exit = () => {
	cellWidget.editor.blur();
};

onMounted(() => {
	props.jupyterSession.ready.then(() => {
		props.jupyterSession.session?.kernel?.info.then((info) => {
			const lang = info.language_info;
			const mimeType = mimeService.getMimeTypeByLanguage(lang);
			cellWidget.model.mimeType = mimeType;
		});
	});

	codeCell.value?.replaceWith(cellWidget.node);

	cellWidget.node.addEventListener(
		'keydown',
		(event) => {
			commands.processKeydownEvent(event);
		},
		true
	);

	if (props.autorun) {
		props.jupyterSession.ready.then(() => {
			run();
		});
	}
});

// Execute cell function
const run = () => {
	CodeCell.execute(cellWidget, props.jupyterSession, { notebook_item: props.notebookItemId });
};

cellWidget.activate();

defineExpose({
	cellWidget,
	clear,
	enter,
	exit
});

// Delete cell function
const emit = defineEmits(['deleteRequested']);

// Are you sure? dialog
const showConfirmDialog = ref(false);

function showDialog() {
	showConfirmDialog.value = true;
}

function confirmDelete() {
	emit('deleteRequested');
	showConfirmDialog.value = false; // Close the dialog
}

function cancelDelete() {
	showConfirmDialog.value = false; // Close the dialog
}
</script>

<style scoped>
.code-cell {
	position: relative;
}
.controls {
	display: flex;
	align-items: end;
	flex-direction: row;
	justify-content: end;
	gap: var(--gap-small);
	position: absolute;
	top: 3px;
	right: 10px;
	z-index: 2;
}
</style>
