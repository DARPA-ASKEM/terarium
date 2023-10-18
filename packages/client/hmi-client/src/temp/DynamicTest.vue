<template>
	<main style="display: flex; flex-direction: column">
		<div>Testing dynamic components</div>
		<br />
		<br />

		<div v-for="(key, idx) of workflowNodes" :key="idx">
			<component :is="registry[key]" :text="veryLongText" :lines="4" @blah="doStuff" />
		</div>
	</main>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue';

const workflowNodes = ['xyz', 'xyz', 'def'];

const doStuff = () => {
	console.log('done');
};

const veryLongText = `
async function loadDynamicModules(name: string, path: string) {
	console.log('hihi loading:', path);
	const d = await import(path);
	console.log('!!!!!!!!!!!!!1');
	console.log(path, d);
	registry.value.push(d);
}
`;

const registry = ref<{ [key: string]: any }>({});
async function registerDynamicModules(name: string, path: string) {
	const d = await import(path);
	registry.value[name] = markRaw(d.default);
}
registerDynamicModules('xyz', '../components/widgets/tera-show-more-text.vue');
registerDynamicModules('def', '../temp/EvaluationScenarios.vue');
</script>
