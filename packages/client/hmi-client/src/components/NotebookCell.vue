<script setup lang="ts">
import { ref } from "vue";
import Panel from 'primevue/panel';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';

const display = ref(true);
const notebookRef = ref(null);
const code = ref("");
const result = ref("");

const syncJupyter = () => {
	const iframe = notebookRef.value;
	const notebookDoc = iframe?.contentDocument;
	const cellContents = notebookDoc?.querySelector("#code-cell .cm-content")?.innerText;
	const results = notebookDoc?.querySelector("#code-output .jp-OutputArea-output")?.innerHTML;
	code.value = cellContents;
	result.value = results;
}

const run = () => {
	const iframe = notebookRef.value;
	const notebookWindow = iframe?.contentWindow;
	const event = new Event("run:cell");
	notebookWindow.dispatchEvent(event);
}

</script>


<template>
	<Panel class="container-panel">
		<iframe id="terarium-code-cell" ref="notebookRef" src="/notebook/notebook" @cell:ran="syncJupyter"></iframe>
		<div>
			<button @click="run">Run</button>
		</div>
		<Panel header="Results">
			<Panel id="notebook-code" header="Code">
				{{ code }}
			</Panel>
			<Panel class="output" header="Output"><div v-html="result"/></Panel>
		</Panel>
	</Panel>
</template>

<style>
	.p-panel-content {
		display: flex;
		flex-direction: column;
	}

	#terarium-code-cell {
		min-height: 300px;
		min-width: 300px;
	}

	#notebook-code {
		white-space: pre-line;
	}
</style>
