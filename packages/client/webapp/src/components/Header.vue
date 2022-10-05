<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '../stores/auth';
import Button from '@/components/Button.vue';

const auth = useAuthStore();

const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};

const authenticatedLabel = computed(() => (auth.isAuthenticated ? 'Logout' : 'Login'));
</script>

<template>
	<nav>
		<img src="/assets/images/logo.png" height="32" width="128" alt="TERArium logo" />
		<span>{{ auth.name }}</span>
		<Button @click="logout">{{ authenticatedLabel }}</Button>
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
	font-size: var(--un-font-xlarge);
}
</style>
