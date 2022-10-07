<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconLogin16 from '@carbon/icons-vue/es/login/16';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const login = () => window.location.assign('http://localhost:8078');
</script>

<template>
	<nav>
		<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<Button v-if="!auth.isAuthenticated" @click="login"> Login <IconLogin16 /> </Button>
	</nav>
</template>

<style scoped>
nav {
	align-items: center;
	border-bottom: 1px solid lightgrey;
	display: flex;
	justify-content: space-between;
	padding: 0.5rem 1rem;
}

header {
	font-size: var(--un-font-xlarge);
}
</style>
