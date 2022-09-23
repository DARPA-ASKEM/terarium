<script setup lang="ts">
function apiCall() {
	console.log('api call');
}
</script>

<template>
	<div id="home">
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
	</div>
</template>

<style>
#home {
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
.test-api-calls button {
	margin: 0 1rem;
	min-width: 10rem;
}
</style>
