<template>
	<div
		class="code-cell"
		@mouseover="mouseOverParent = true"
		@mouseleave="mouseOverParent = false"
		@focusin="mouseOverParent = true"
		@focusout="mouseOverParent = false"
		@keyup.ctrl.enter.prevent="run"
		@keyup.shift.enter.prevent="run"
	>
		<template ref="codeCell" />
		<div class="controls">
			<Button
				label="Delete"
				outlined
				size="small"
				severity="secondary"
				icon="pi pi-trash"
				disabled
			/>
			<Button
				label="Run"
				outlined
				size="small"
				severity="secondary"
				icon="pi pi-play"
				@click="run"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { CodeCell, CodeCellModel } from '@jupyterlab/cells';
import { SessionContext } from '@jupyterlab/apputils';
import {
	Completer,
	CompleterModel,
	CompletionHandler,
	CompletionConnector
} from '@jupyterlab/completer';
import { CommandRegistry } from '@lumino/commands';
import { mimeService, renderMime } from '@/services/jupyter';
import Button from 'primevue/button';

const mouseOverParent = ref(false);

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
	execute: () =>
		CodeCell.execute(cellWidget, props.jupyterSession, { notebook_item: props.notebookItemId })
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

// Save file function
// const saveAsNewDataset = () => {
// 	emit('save-as-new-dataset', { datasetName: savedFileName.value });
// 	console.log('save-as-new-dataset emitted');
// };

const clear = () => {
	cellWidget.model.clearExecution();
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
	clear
});
</script>

<style lang="scss" global>
@import '@jupyterlab/application/style/index.css';
@import '@jupyterlab/cells/style/index.css';
@import '@jupyterlab/theme-light-extension/style/theme.css';
@import '@jupyterlab/completer/style/index.css';

.jp-CodeCell {
	min-width: 300px;
}

.controls {
	padding-top: var(--gap-small);
	display: flex;
	align-items: end;
	flex-direction: row;
	justify-content: end;
	gap: var(--gap-small);
}

.visible {
	display: block;
}

.hidden {
	display: none;
}
</style>
