<script setup lang="ts">
function logout() {
	alert('logout');
}
</script>

<template>
	<nav>
		<header>TERArium</header>
		<button type="button" @click="logout">Logout</button>
	</nav>
</template>

<style scoped>
nav {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1px solid lightgrey;
	padding: 0.5rem 1rem;
}

header {
	font-weight: bold;
	font-size: 2rem;
}
</style>
