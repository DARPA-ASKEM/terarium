<template>
	<main>
		<header>
			<h2>Test SSE</h2>
			<Button label="Start listening" @click="listen" />
			<Button label="Create empty models" @click="createEmptyModel" />
		</header>
		<ul>
			<li v-for="modelId in models" :key="modelId">{{ modelId }}</li>
		</ul>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import API from '@/api/api';
import { EventSourcePolyfill } from 'event-source-polyfill';

const models = ref<Array<string>>([]);

function listen() {
	const events = new EventSourcePolyfill('/api/user/server-sent-events');
	//const events = new EventSource("/api/server-sent-events", { withCredentials: true });
	events.onmessage = (event) => {
		const id: string = JSON.parse(event.data)?.id;
		models.value.push(id);
		console.log(id);
	};
}

async function createEmptyModel() {
	await API.put('/dev-tests/user-event');
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
