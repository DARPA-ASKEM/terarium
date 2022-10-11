<script setup lang="ts">
import { useRouter } from 'vue-router';
import Button from '@/components/Button.vue';
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
	<section>
		<p>Test API calls/Home placeholder</p>
		<div>
			<Button>New Project</Button>
			<Button action>Update Project</Button>
			<Button success>Get Projects</Button>
			<Button info>Delete Project</Button>
		</div>
		<div>
			<Button @click="openDataExplorer">Data Explorer</Button>
			<Button warning>New Model</Button>
			<Button danger>Update Model</Button>
			<Button>Get Model</Button>
			<Button>Delete Model</Button>
		</div>
		<div>
			<Button @click="apiCall('user')">User Call</Button>
			<Button @click="apiCall('admin')">Admin Call</Button>
		</div>
	</section>
</template>

<style scoped>
section {
	margin: 0 auto;
	max-width: 1280px;
	padding: 2rem;
	text-align: center;
}

div {
	display: flex;
	gap: 1em;
	margin: 2em;
	justify-content: center;
}
</style>
