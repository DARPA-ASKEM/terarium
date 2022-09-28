<script setup lang="ts">
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};
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

a {
	color: white;
	text-decoration: none;
}
</style>
