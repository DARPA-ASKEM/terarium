<template>
	<main>
		<section>
			<header>
				<h5>Code</h5>
				<Dropdown v-model="programmingLanguages" />
				<Button label="Add code block" icon="pi pi-plus" @click="addCodeBlock" />
			</header>
			<ul>
				<li v-for="({ name, code, programmingLanguage }, i) in codeBlocks" :key="i">
					<Card>
						<header>
							<h5>{{ name }}</h5>
						</header>
						<v-ace-editor
							:value="code"
							@init="initialize"
							:lang="programmingLanguage"
							theme="chrome"
							style="height: 100%; width: 100%"
							class="ace-editor"
						/>
					</Card>
				</li>
			</ul>
			<footer>
				<span><label>Model framework:</label><Dropdown /></span>
				<Button label="Run" icon="pi pi-play" severity="secondary" outlined size="large" />
			</footer>
		</section>
		<section class="preview">
			<h5>Preview</h5>
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Card from 'primevue/card';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import { ProgrammingLanguage } from '@/types/Types';

const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const programmingLanguages = [
	ProgrammingLanguage.Julia,
	ProgrammingLanguage.Python,
	ProgrammingLanguage.R
];

const codeBlock = {
	name: 'Code block 1',
	code: '',
	programmingLanguage: ProgrammingLanguage.Python
};

const codeBlocks = ref([codeBlock]);

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

function addCodeBlock() {
	codeBlocks.value.push(codeBlock);
}
</script>

<style scoped>
main {
	margin: 1rem;
	display: flex;
	gap: 2rem;
}

main > section {
	flex: 1;
}

header,
footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

footer {
	margin-top: auto;
}

.preview {
}

header > * {
	max-height: 40px;
}

ul {
	list-style: none;
}
</style>
