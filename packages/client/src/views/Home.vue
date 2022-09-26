<script setup lang="ts">
function apiCall() {
	alert('api call');
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
			<button type="button" @click="apiCall">New Model</button>
			<button type="button" @click="apiCall">Update Model</button>
			<button type="button" @click="apiCall">Get Model</button>
			<button type="button" @click="apiCall">Delete Model</button>
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
