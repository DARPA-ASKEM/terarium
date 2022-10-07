<script setup lang="ts">
import { computed } from 'vue';
import Login from '@carbon/icons-vue/es/login/16';
import Logout from '@carbon/icons-vue/es/logout/16';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const isAuthenticated = computed(() => auth.isAuthenticated);

const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};

const login = () => window.location.assign('http://localhost:8078');
</script>

<template>
	<nav>
		<!-- Use relative path instead of alias for now as Component Tests seem to have an issue -->
		<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		{{ auth.name }}
		<Button v-if="isAuthenticated" @click="logout">Logout <Logout /></Button>
		<Button v-else @click="login">Login <Login /></Button>
	</nav>
</template>

<style scoped>
nav {
	align-items: center;
	border-bottom: 1px solid lightgrey;
	display: flex;
	justify-content: space-between;
	padding: 0.5rem 1rem;
	height: 3.5rem;
}

header {
	font-size: var(--un-font-xlarge);
}
</style>
