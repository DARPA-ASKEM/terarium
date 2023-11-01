<template>
	<main>
		<section>
			<header>
				<h5>Code</h5>
				<Dropdown
					class="w-full md:w-14rem"
					v-model="selectedProgrammingLanguage"
					:options="programmingLanguages"
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
						<template #toggler-icon>
							<Button icon="pi pi-chevon-down" text rounded />
						</template>
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
				/></span>
				<Button label="Run" icon="pi pi-play" severity="secondary" outlined size="large" />
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
			</section>
			<footer>
				<Button disabled label="Save as new model" severity="secondary" outlined size="large" />
				<span class="btn-group">
					<Button label="Cancel" severity="secondary" outlined size="large" />
					<Button disabled label="Apply changes and close" size="large" />
				</span>
			</footer>
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
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

const codeBlock = {
	...props.node.state,
	name: 'Code block 1',
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
