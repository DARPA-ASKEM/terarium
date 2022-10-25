<script setup lang="ts">
import Button from '@/components/Button.vue';</script>

<template>
	<section>
		<p>Test API calls/Home placeholder</p>
		<div>
			<Button>Normal</Button>
			<Button action>Action</Button>
			<Button danger>Danger</Button>
			<Button info>Info</Button>
		</div>
		<div>
			<Button @click="apiCall('user')">User Call</Button>
			<Button @click="apiCall('admin')">Admin Call</Button>
		</div>
	</section>
</template>

<script lang="ts">
import {uncloak} from '@/utils/uncloak';

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
</script>

<style scoped>
section {
	margin: 0 auto;
	max-width: 1280px;
	padding: 2rem;
	text-align: center;
}

div {
	display: flex;
	gap: 1rem;
	margin: 2rem;
	justify-content: center;
}
</style>
