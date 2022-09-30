<script setup lang="ts">
import { useRouter } from 'vue-router';
import { uncloak } from '../utils/uncloak';

const router = useRouter();

async function apiCall(type = '') {
	if (type === 'user') {
		const data = await uncloak('/api/user/me');
		if (data === null) {
			console.log('Unauthorized Access');
		}
		console.log(`Response: ${JSON.stringify(data)}`);
	}
	if (type === 'admin') {
		const data = await uncloak('/api/admin');
		if (data === null) {
			console.log('Unauthorized Access');
		}
		console.log(`Response: ${JSON.stringify(data)}`);
	}
}

function openDataExplorer() {
	router.push('/explorer');
}
</script>

<template>
	<main>
		<p>Test API calls/Home placeholder</p>
		<div class="test-api-calls">
			<button type="button" @click="apiCall">New Project</button>
			<button type="button" @click="apiCall">Update Project</button>
			<button type="button" @click="apiCall">Get Projects</button>
			<button type="button" @click="apiCall">Delete Project</button>
		</div>
		<div class="test-api-calls">
			<button type="button" @click="openDataExplorer">Data Explorer</button>
			<button type="button" @click="apiCall">New Model</button>
			<button type="button" @click="apiCall">Update Model</button>
			<button type="button" @click="apiCall">Get Model</button>
			<button type="button" @click="apiCall">Delete Model</button>
		</div>
		<div class="test-api-calls">
			<button type="button" @click="apiCall('user')">User Call</button>
			<button type="button" @click="apiCall('admin')">Admin Call</button>
		</div>
	</main>
</template>

<style scoped>
main {
	place-items: center;
	margin: 0 auto;
	max-width: 1280px;
	padding: 2rem;
	text-align: center;
}

.test-api-calls {
	display: flex;
	margin: 2rem;
	justify-content: center;
}
button[type='button'] {
	margin: 0.5rem 1rem;
	min-width: 10rem;
}
</style>
