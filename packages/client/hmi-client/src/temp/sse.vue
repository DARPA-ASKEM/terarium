<template>
	<main>
		<header>
			<h2>Test SSE</h2>
			<Button label="Start listening" @click="listen" />
			<Button label="Create empty models" @click="createEmptyModel" />
		</header>
		<ul></ul>
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import * as ModelService from '@/services/models/model-service';

function listen() {}

function createEmptyModel() {
	const model = ModelService.create();
	console.log('Model created', model);
}
</script>

<style scoped>
main {
	flex-direction: column;
	gap: 1rem;
	padding: 4rem;
}

header {
	display: flex;
	gap: 2rem;
}

.p-button {
	display: inline-block;
}
</style>
