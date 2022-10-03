<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};

const authenticatedLabel = computed(() => (auth.isAuthenticated ? 'Logout' : 'Login'));
</script>

<template>
	<nav>
		<header>TERArium</header>
		<span>{{ auth.name }}</span>
		<button type="button" @click="logout">{{ authenticatedLabel }}</button>
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
