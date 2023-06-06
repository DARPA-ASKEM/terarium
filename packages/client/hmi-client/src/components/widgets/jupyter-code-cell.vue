<template>
	<div class="code-cell" @keyup.ctrl.enter.prevent="run" @keyup.shift.enter.prevent="run">
		<div>
			<button type="button" @click="run">Run</button>
		</div>
		<template ref="codeCell" />
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { CodeCell, CodeCellModel } from '@jupyterlab/cells';
import { SessionContext } from '@jupyterlab/apputils';
import { mimeService, renderMime } from '@/utils/jupyter';

import {
	Completer,
	CompleterModel,
	CompletionHandler,
	CompletionConnector
} from '@jupyterlab/completer';

import { CommandRegistry } from '@lumino/commands';

const props = defineProps({
	jupyterSession: {
		type: SessionContext,
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
	}
});
const codeCell = ref<HTMLElement | null>(null);

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

// Set up a completer.
const editor = cellWidget.editor;
const model = new CompleterModel();
const completer = new Completer({ editor, model });
const connector = new CompletionConnector({ editor, session: props.jupyterSession.session });
const handler = new CompletionHandler({ completer, connector });

const commands = new CommandRegistry();

// Add the commands.
commands.addCommand('invoke:completer', {
	execute: () => {
		handler.invoke();
	}
});
commands.addCommand('run:cell', {
	execute: () => CodeCell.execute(cellWidget, props.jupyterSession)
});

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

onMounted(() => {
	props.jupyterSession.ready.then(() => {
		props.jupyterSession.session?.kernel?.info.then((info) => {
			const lang = info.language_info;
			const mimeType = mimeService.getMimeTypeByLanguage(lang);
			cellWidget.model.mimeType = mimeType;
		});
	});
	// Replace templated component with contents of jupyter node.
	codeCell.value?.replaceWith(cellWidget.node);
	// Setup keydown listener to capture events inside code cell.
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

const run = () => {
	CodeCell.execute(cellWidget, props.jupyterSession);
};

cellWidget.activate();
</script>

<style lang="scss" global>
@import '@jupyterlab/application/style/index.css';
@import '@jupyterlab/cells/style/index.css';
@import '@jupyterlab/theme-light-extension/style/theme.css';
@import '@jupyterlab/completer/style/index.css';

.jp-CodeCell {
	min-width: 300px;
}
</style>
