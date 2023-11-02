<template>
	<main>
		<section>
			<header>
				<h5>Code</h5>
				<Dropdown
					class="w-full md:w-14rem"
					v-model="selectedProgrammingLanguage"
					:options="programmingLanguages"
					@change="setKernelContext"
				/>
				<Button label="Add code block" icon="pi pi-plus" text @click="addCodeBlock" />
			</header>
			<ul>
				<li v-for="({ name, codeLanguage }, i) in codeBlocks" :key="i">
					<Panel toggleable>
						<template #header>
							<section>
								<h5>{{ name }}</h5>
								<Button icon="pi pi-pencil" text rounded />
							</section>
						</template>
						<template #icons>
							<span>
								<label>Include in process</label>
								<InputSwitch v-model="codeBlocks[i].includeInProcess" />
							</span>
							<Button icon="pi pi-trash" text rounded @click="removeCodeBlock(i)" />
						</template>
						<!--FIXME: togglericon slot isn't recognized for some reason maybe update prime vue?
						<template #togglericon="{ collapsed }">
							<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
						</template> -->
						<v-ace-editor
							v-model:value="codeBlocks[i].codeContent"
							@init="initialize"
							:lang="codeLanguage"
							theme="chrome"
							style="height: 10rem; width: 100%"
							class="ace-editor"
						/>
					</Panel>
				</li>
			</ul>
			<footer>
				<span
					><label>Model framework:</label
					><Dropdown
						class="w-full md:w-14rem"
						v-model="selectedModelFramework"
						:options="modelFrameworks"
						@change="setKernelContext"
				/></span>
				<Button
					label="Run"
					icon="pi pi-play"
					severity="secondary"
					outlined
					size="large"
					@click="handleCode"
				/>
			</footer>
		</section>
		<section>
			<section class="preview">
				<header>
					<h5>Preview</h5>
					<Dropdown
						class="w-full md:w-14rem"
						v-model="selectedModelFramework"
						:options="modelFrameworks"
					/>
				</header>
				<div class="flex flex-column gap-2">
					<label>Model name</label>
					<InputText v-model="modelName" />
				</div>
				<template v-if="selectedModelFramework === ModelFramework.Petrinet">
					<!--Potentially put tera-model-diagram here just for petrinets-->
					<!--Potentially breakdown tera-model-descriptions state and parameter tables and put them here-->
				</template>
				<template v-if="selectedModelFramework === ModelFramework.Decapodes">
					<div :innerHTML="previewHTML" />
				</template>
			</section>
			<footer>
				<Button
					:disabled="!modelValid || modelName === ''"
					label="Save as new model"
					severity="secondary"
					outlined
					size="large"
					@click="saveAsNewModel"
				/>
				<span class="btn-group">
					<Button label="Cancel" severity="secondary" outlined size="large" />
					<Button
						:disabled="!modelValid || modelName === ''"
						label="Apply changes and close"
						size="large"
						@click="getModel"
					/>
				</span>
			</footer>
		</section>
	</main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import Panel from 'primevue/panel';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputSwitch from 'primevue/inputswitch';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import { ProgrammingLanguage } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { cloneDeep, isEmpty } from 'lodash';
import { newSession, JupyterMessage, createMessageId } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { ModelFromCodeState } from './model-from-code-operation';

const props = defineProps<{
	node: WorkflowNode<ModelFromCodeState>;
}>();

enum ModelFramework {
	Petrinet = 'Petrinet',
	Decapodes = 'Decapodes'
}

const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const modelName = ref('');
const programmingLanguages = Object.values(ProgrammingLanguage);
const selectedProgrammingLanguage = ref(ProgrammingLanguage.Python);
const modelFrameworks = Object.values(ModelFramework);
const selectedModelFramework = ref(
	isEmpty(props.node.state.modelFramework)
		? ModelFramework.Decapodes
		: props.node.state.modelFramework
);
const modelValid = ref(false);
const jupyterSession = ref<SessionContext | null>(null);
const previewHTML = ref('');

const codeBlock = {
	...props.node.state,
	name: 'Code block 1',
	includeInProcess: true
};

const codeBlocks = ref([codeBlock]);

onMounted(async () => {
	const session = await newSession('beaker', 'Beaker');
	jupyterSession.value = session;

	session.kernelChanged.connect((_context, kernelInfo) => {
		const kernel = kernelInfo.newValue;
		if (kernel?.name === 'beaker') {
			session.iopubMessage.connect(iopubMessageHandler);
			setKernelContext();
		}
	});
});

onUnmounted(async () => {
	// Get rid of session/kernels on unmount, otherwise we end upwith lots of kernels hanging
	// around using up memory.
	jupyterSession.value?.shutdown();
});

function setKernelContext() {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) {
		return;
	}
	const contextName =
		selectedModelFramework.value === ModelFramework.Decapodes ? 'decapodes_creation' : null;
	const languageName =
		selectedProgrammingLanguage.value === ProgrammingLanguage.Julia ? 'julia-1.9' : null;
	if (contextName === null || languageName === null) {
		console.log("Can't work with current language. Do nothing.");
		return;
	}
	const contextMessage: JupyterMessage = createMessage({
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			context: contextName,
			language: languageName,
			context_info: {}
		},
		msgType: 'context_setup_request',
		msgId: createMessageId('context_setup')
	});

	kernel.sendJupyterMessage(contextMessage);
}

function handleCode() {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) {
		return;
	}
	const code = editor.value?.getValue();
	const compileExprMessage: JupyterMessage = createMessage({
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			declaration: code
		},
		msgType: 'compile_expr_request',
		msgId: createMessageId('context_setup')
	});

	modelValid.value = false;
	kernel.sendJupyterMessage(compileExprMessage);
}

function getModel() {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) {
		return;
	}
	const constructModelMessage: JupyterMessage = createMessage({
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			name: modelName.value
		},
		msgType: 'construct_amr_request',
		msgId: createMessageId('context_setup')
	});

	modelValid.value = false;
	kernel.sendJupyterMessage(constructModelMessage);
}

function saveAsNewModel() {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) {
		return;
	}
	const saveAsNewMessage: JupyterMessage = createMessage({
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			header: {
				description: modelName.value,
				name: modelName.value,
				_type: 'Header',
				model_version: 'v1.0',
				schema: 'modelreps.io/DecaExpr',
				schema_name: 'DecaExpr'
			}
		},
		msgType: 'save_amr_request',
		msgId: createMessageId('context_setup')
	});

	modelValid.value = false;
	kernel.sendJupyterMessage(saveAsNewMessage);
}

const iopubMessageHandler = (_session, message) => {
	if (message.header.msg_type === 'status') {
		return;
	}
	if (message.header.msg_type === 'compile_expr_response') {
		modelValid.value = true;
	} else if (message.header.msg_type === 'decapodes_preview') {
		console.log('Decapode preview', message.content);
		previewHTML.value = message.content['image/svg'];
	} else if (message.header.msg_type === 'construct_amr_response') {
		// console.log("Decapode preview", message.content);
		// previewHTML.value = message.content["image/svg"];
		alert(JSON.stringify(message.content, null, 2));
	}
};

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
}

function addCodeBlock() {
	codeBlocks.value.push(cloneDeep(codeBlock));
}

function removeCodeBlock(index: number) {
	codeBlocks.value.splice(index, 1);
}
</script>

<style scoped>
main {
	padding: 16px;
	display: flex;
	gap: 2rem;
	flex: 1;
	height: 100%;
}

main > section {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 1rem;
	overflow-y: auto;
	height: 100%;
}

section > header,
footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

footer {
	margin-top: auto;
}

.preview {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	background-color: var(--surface-ground);
	padding: 1rem;
	flex: 1;
	flex-direction: column;
	display: flex;
	gap: 0.5rem;
}

.preview > section {
	display: flex;
	justify-content: space-between;
}

span {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.btn-group {
	gap: 1rem;
}

.p-dropdown {
	max-height: 40px;
}

ul {
	list-style: none;
	display: flex;
	overflow: auto;
	flex-direction: column;
	gap: 0.5rem;
	flex: 1;
}
.p-panel {
	border: 1px solid var(--surface-border-light);
}

.p-panel:deep(section) {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.p-panel:deep(.p-panel-icons) {
	display: flex;
	gap: 1rem;
	align-items: center;
}

li > header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

li > header > section {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.ace-editor {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
}
</style>
