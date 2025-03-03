<template>
	<main>
		<header>
			<h2>Test SSE</h2>
			<Button label="Subscribe" @click="subscribe(ClientEventType.Notification, getMessageHandler)" />
			<Button label="Unsubscribe" @click="unsubscribe(ClientEventType.Notification, getMessageHandler)" />
		</header>
		<template v-for="message in messages" :key="message.id">
			<div>{{ message.id }} : {{ message.data }}</div>
		</template>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import type { ClientEvent } from '@/types/Types';
import { ClientEventType } from '@/types/Types';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
const messages = ref<ClientEvent<any>[]>([]);

function getMessageHandler(event: ClientEvent<any>) {
	messages.value.push(event);
	if (messages.value.length > 10) {
		messages.value.shift();
	}
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
