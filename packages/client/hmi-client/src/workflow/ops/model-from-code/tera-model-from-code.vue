<template>
	<main>
		<section>
			<header>
				<h5>Code</h5>
				<Dropdown
					class="w-full md:w-14rem"
					v-model="programmingLanguage"
					:options="programmingLanguages"
				/>
				<Button label="Add code block" icon="pi pi-plus" text @click="addCodeBlock" />
			</header>
			<ul>
				<li v-for="({ name, programmingLanguage }, i) in codeBlocks" :key="i">
					<header>
						<section>
							<h5>{{ name }}</h5>
							<Button icon="pi pi-pencil" text rounded />
						</section>
						<section>
							<label>Include in process</label>
							<InputSwitch v-model="codeBlocks[i].includeInProcess" />
							<Button icon="pi pi-trash" text rounded />
							<Button icon="pi pi-chevron-up" text rounded />
						</section>
					</header>
					<v-ace-editor
						v-model:value="codeBlocks[i].code"
						@init="initialize"
						:lang="programmingLanguage"
						theme="chrome"
						style="height: 10rem; width: 100%"
						class="ace-editor"
					/>
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
import InputSwitch from 'primevue/inputswitch';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import { ProgrammingLanguage } from '@/types/Types';
import { cloneDeep } from 'lodash';

const editor = ref<VAceEditorInstance['_editor'] | null>(null);

const programmingLanguage = ref<ProgrammingLanguage>(ProgrammingLanguage.Python);
const programmingLanguages = [
	ProgrammingLanguage.Julia,
	ProgrammingLanguage.Python,
	ProgrammingLanguage.R
];

const codeBlock = {
	name: 'Code block 1',
	code: '',
	programmingLanguage: ProgrammingLanguage.Python,
	includeInProcess: true
};

const codeBlocks = ref([codeBlock]);

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

section > header,
footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 1rem 0;
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
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

li {
	border: 1px solid var(--surface-border-light);
	border-radius: 4px;
	padding: 0.5rem;
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
</style>
