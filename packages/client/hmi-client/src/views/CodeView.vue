<template>
	<div class="code-view">
		<v-ace-editor v-model:value="content" lang="javascript" theme="chrome" style="height: 300px" />
	</div>
</template>
<script lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { ref } from 'vue';

const content = ref(true);
console.log(content);
export default {
	components: {
		VAceEditor
	}
};
</script>
