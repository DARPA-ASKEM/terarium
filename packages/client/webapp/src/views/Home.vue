<script setup lang="ts">
import { uncloak } from '../utils/uncloak';
import Button from '@/components/Button.vue';

function apiCall() {}
</script>

<template>
	<main>
		<p>Test API calls/Home placeholder</p>
		<div>
			<Button @click="apiCall">New Project</Button>
			<Button action @click="apiCall">Update Project</Button>
			<Button success @click="apiCall">Get Projects</Button>
			<Button info @click="apiCall">Delete Project</Button>
		</div>
		<div>
			<Button warning @click="apiCall">New Model</Button>
			<Button danger @click="apiCall">Update Model</Button>
			<Button @click="apiCall">Get Model</Button>
			<Button @click="apiCall">Delete Model</Button>
		</div>
		<div>
			<Button @click="uncloak('/api/user/me')">User Call</Button>
			<Button @click="uncloak('/api/admin')">Admin Call</Button>
		</div>
	</main>
</template>

<style scoped>
main {
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
