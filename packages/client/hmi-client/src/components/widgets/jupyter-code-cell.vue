<template>
	<div class="code-cell" @keyup.ctrl.enter.prevent="run" @keyup.shift.enter.prevent="run">
		<div>
			<button type="button" @click="run">Run</button>
		</div>
		<component ref="codeCell" :is="Widget" />
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

import '@jupyterlab/application/style/index.css';
import '@jupyterlab/cells/style/index.css';
import '@jupyterlab/theme-light-extension/style/theme.css';
import '@jupyterlab/completer/style/index.css';
import { Widget } from '@lumino/widgets';

const props = defineProps({
	jupyterContext: {
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
const codeCell = ref(null);

const codeCellContent = ref(props.code);

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
const connector = new CompletionConnector({ editor, session: props.jupyterContext.session });
const handler = new CompletionHandler({ completer, connector });

const commands = new CommandRegistry();

// Add the commands.
commands.addCommand('invoke:completer', {
	execute: () => {
		handler.invoke();
	}
});
commands.addCommand('run:cell', {
	execute: () => CodeCell.execute(cellWidget, props.jupyterContext)
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

// const setKernelContext = async (kernel, context_info) => {
// 	// TODO: Update filename. -- Maybe a global value passed around, since this is used in the chatty kernel
// 	const setupCode = getCodeBlock("python", "read_csv", {filename: `http://data-service:8000/datasets/${context_info.context_info.id}/download/rawfile?wide_format=true`});
// 	const future = kernel?.requestExecute({
//         code: setupCode,
//         silent: false,
//         store_history: true,
//       }, false);
// 	  console.log(future);
//       if (future) {
//         await future.done;
// 		console.log("done");
//         // TODO: Remove spinner after completed
//       }
// }

onMounted(() => {
	props.jupyterContext.ready.then(() => {
		props.jupyterContext.session?.kernel?.info.then((info) => {
			const lang = info.language_info;
			const mimeType = mimeService.getMimeTypeByLanguage(lang);
			cellWidget.model.mimeType = mimeType;
		});
		// setKernelContext(
		// 	props.jupyterContext.session?.kernel,
		// 	{
		// 		context: props.context,
		// 		context_info: props.context_info,
		// 	}
		// );
	});
	// Replace templated component with contents of jupyter node.
	codeCell.value?.replace(cellWidget.node);
	// Setup keydown listener to capture events inside code cell.
	cellWidget.node.addEventListener(
		'keydown',
		(event) => {
			commands.processKeydownEvent(event);
		},
		true
	);
	if (props.autorun) {
		props.jupyterContext.ready.then(() => {
			run();
		});
		// run();
	}
});

const run = () => {
	console.log(codeCellContent.value);
	CodeCell.execute(cellWidget, props.jupyterContext);
};

cellWidget.activate();
</script>

<style lang="scss" global>
.jp-CodeCell {
	min-width: 300px;
}
</style>
