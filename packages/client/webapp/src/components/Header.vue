<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconLogin16 from '@carbon/icons-vue/es/login/16';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

const login = () => window.location.assign('http://localhost:8078');
const homepage = () => router.push('/');

const projectName = 'Name of the project that can be long for clarity and precision';
</script>

<template>
	<nav>
		<header>
			<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
			<p>
				<a @click="homepage">Projects</a>
				<span>{{ projectName }}</span>
			</p>
		</header>

		<Button v-if="!auth.isAuthenticated" @click="login"> Login <IconLogin16 /> </Button>
	</nav>
</template>

<style scoped>
nav {
	align-items: center;
	background-color: var(--un-color-body-surface-primary);
	border-bottom: 1px solid lightgrey;
	box-shadow: var(--un-box-shadow-small);
	display: flex;
	justify-content: space-between;
	padding: 0.5rem 1rem;
	z-index: var(--un-z-index-navbar);
}

header {
	align-items: center;
	display: flex;
	font-size: var(--un-font-xlarge);
	gap: 2em;
	justify-content: space-around;
}

header p > * + *::before {
	content: '>';
	margin: 0 1em;
}

header a {
	text-decoration: underline;
}

header a:hover,
header a:focus {
	color: var(--un-color-accent-dark);
}
</style>
