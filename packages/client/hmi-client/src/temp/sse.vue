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
import { EventSourcePolyfill } from 'event-source-polyfill';
import useAuthStore from '../stores/auth';

function listen() {
	const auth = useAuthStore();
	const events = new EventSourcePolyfill('/api/user/server-sent-events', {
		headers: {
			Authorization: `Bearer ${auth.token}`
		}
	});
	//const events = new EventSource("/api/server-sent-events", { withCredentials: true });
	events.onmessage = (event) => {
		console.log(event);
		const json = JSON.parse(event.data);
		console.log(json);
	};
}

async function createEmptyModel() {
	const model = await ModelService.create();
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
