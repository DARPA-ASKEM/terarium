<script setup lang="ts">
function logout() {
	console.log('logout');
}
</script>

<template>
	<nav id="navbar">
		<div class="navbar-left">
			<h2>TERArium</h2>
		</div>
		<div class="navbar-right">
			<button type="button" @click="logout">Logout</button>
		</div>
	</nav>
</template>

<style>
#navbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1px solid lightgrey;
}

.navbar-left,
.navbar-right {
	margin: 0.5rem 1rem;
}

h2 {
	margin: 0;
}
</style>
