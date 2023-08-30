<template>
	<main>
		<header>
			<section>
				<h4>{{ name }}</h4>
				<section class="buttons">
					<Button label="Save code" @click="saveCode" />
					<Button label="Extract model" @click="extractModel" :disabled="!codeAsset" />
				</section>
			</section>
		</header>
		<v-ace-editor
			v-model:value="code"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
			class="ace-editor"
		/>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import Button from 'primevue/button';
import { uploadCodeToProject } from '@/services/code';
import { useToastService } from '@/services/toast';
import { codeToAMR } from '@/services/models/extractions';
import { Code } from '@/types/Types';

const props = defineProps<{
	projectId: string;
	assetId: string;
}>();

const toast = useToastService();

const name = ref('newcode.py');
const code = ref('# Paste some code here');
const codeAsset = ref<Code>();
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const progress = ref(0);
/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

async function saveCode() {
	const file = new File([code.value], name.value);
	const newCodeAsset = await uploadCodeToProject(props.projectId, file, progress);
	if (!newCodeAsset) {
		toast.error('', 'Unable to save code');
	} else {
		toast.success('', `File saved as ${name.value}`);
		codeAsset.value = newCodeAsset;
	}
	return newCodeAsset;
}

async function extractModel() {
	if (codeAsset.value?.id) {
		const model = await codeToAMR(codeAsset.value.id);
		console.log(model);
	}
}
</script>

<style scoped>
header {
	padding: 0.5rem 1rem;
	background-color: var(--surface-section);
}

header > section {
	display: flex;
	justify-content: space-between;
	flex-direction: row;
}

main {
	height: 100%;
}

h4 {
	margin-top: 0.25rem;
}

.buttons {
	display: flex;
	gap: 0.5rem;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
}
</style>
