<template>
	<div class="code-cell" @keyup.ctrl.enter.prevent="run" @keyup.shift.enter.prevent="run">
		<template ref="codeCell" />
		<div class="controls">
			<i class="pi pi-play play" v-tooltip="`Run again`" @click="run"></i>
			<div class="run">Run again</div>
			<i class="pi pi-check-circle check"></i>
			<div class="run">Success</div>
		</div>
		<div class="save-file-container">
			<div class="save-as">Save As:</div>
			<div class="saved-name">{{ props.savedName }}</div>
			<InputText v-model="savedFileName" class="post-fix" :style="`padding:3px;`" />
			<i class="pi pi-times i" :class="{ clear: hasValidFileName }" @click="savedFileName = ''"></i>
			<i class="pi pi-check i" :class="{ save: hasValidFileName }" @click="saveFile"></i>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
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
	savedName: {
		type: String,
		default: 'Dataset Name_'
	},
	postFix: {
		type: String,
		default: 'new'
	}
});

const savedFileName = ref<string>('');
const hasValidFileName = computed<boolean>(() => savedFileName.value !== '');
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
	execute: () => CodeCell.execute(cellWidget, props.jupyterSession)
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
const saveFile = () => {
	console.log('Saving File...');
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
	CodeCell.execute(cellWidget, props.jupyterSession);
};

cellWidget.activate();
</script>

<style lang="scss" global>
@import '@jupyterlab/application/style/index.css';
@import '@jupyterlab/cells/style/index.css';
@import '@jupyterlab/theme-light-extension/style/theme.css';
@import '@jupyterlab/completer/style/index.css';

// (the rest of your CSS goes here)
</style>
