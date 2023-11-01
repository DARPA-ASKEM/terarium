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
					<Panel toggleable>
						<template #header>
							<section>
								<h5>{{ name }}</h5>
								<Button icon="pi pi-pencil" text rounded />
							</section>
						</template>
						<template #icons>
							<label>Include in process</label>
							<InputSwitch v-model="codeBlocks[i].includeInProcess" />
							<Button icon="pi pi-trash" text rounded @click="removeCodeBlock(i)" />
						</template>
						<template #toggler-icon>
							<Button icon="pi pi-chevon-down" text rounded />
						</template>
						<v-ace-editor
							v-model:value="codeBlocks[i].code"
							@init="initialize"
							:lang="programmingLanguage"
							theme="chrome"
							style="height: 10rem; width: 100%"
							class="ace-editor"
						/>
					</Panel>
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
import Panel from 'primevue/panel';
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

function removeCodeBlock(index: number) {
	codeBlocks.value.splice(index, 1);
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
	display: flex;
	flex-direction: column;
	gap: 1rem;
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
}

header > * {
	max-height: 40px;
}

ul {
	list-style: none;
	display: flex;
	overflow: auto;
	flex-direction: column;
	gap: 0.5rem;
	/* flex: 1; */
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
