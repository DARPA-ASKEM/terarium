<template>
	<div class="code-cell" @keyup.ctrl.enter.prevent="run" @keyup.shift.enter.prevent="run">
		<template ref="codeCell" />
		<div class="controls">
			<i class="pi pi-play x" @click="run"></i>
			<div class="run">Run again</div>
			<i class="pi pi-check-circle check"></i>
			<div class="run">Success</div>
		</div>
		<div class="save-file-container">
			<div class="save-as">Save As:</div>
			<div class="saved-name">{{ props.savedName }}</div>
			<InputText class="post-fix" :style="`padding:3px;`" />
			<i class="pi pi-times x"></i>
			<i class="pi pi-check check" @click="saveFile"></i>
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
import InputText from 'primevue/inputtext';

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
	},
	savedName: {
		type: String,
		default: 'Dataset Name_'
	},
	postFix: {
		type: String,
		default: 'new'
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

const saveFile = () => {
	console.log('save file');
};

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
	background-color: var(--gray-50);
	min-width: 300px;
}

.controls {
	display: flex;
	align-items: center;
	padding-top: 15px;
	padding-bottom: 10px;
	flex-direction: row;
	font-size: 0.8rem;
}

.run {
	color: var(--gray-500);
	font-family: var(--font-family);
	padding-left: 5px;
	padding-right: 10px;
}
.save-file-container {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding-bottom: 5px;
	padding-top: 5px;
	font-size: 1rem;
}

.saved-name {
	display: flex;
	flex-direction: row;
	padding-left: 5px;
	padding-right: 5px;
}
.post-fix {
	padding: 0px;
	height: 20px;
}

.x {
	color: var(--primary-color);
	padding-left: 5px;
	padding-right: 5px;
}

.check {
	color: var(--primary-color);
	padding-left: 5px;
	padding-right: 5px;
}
</style>
