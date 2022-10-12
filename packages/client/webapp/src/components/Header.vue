<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconLogin16 from '@carbon/icons-vue/es/login/16';
import IconSearchLocate16 from '@carbon/icons-vue/es/search--locate/16';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

const login = () => window.location.assign('http://localhost:8078');
const homepage = () => router.push('/');

const projectName = 'Name of the project that can be long for clarity and precision';
</script>

<template>
	<header>
		<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<p>
			<a @click="homepage">Projects</a>
			<span>{{ projectName }}</span>
		</p>

		<aside>
			<Button class="data-explorer" @click="router.push('/explorer')"
				><IconSearchLocate16
			/></Button>
			<Button v-if="!auth.isAuthenticated" @click="login">Login <IconLogin16 /></Button>
		</aside>
	</header>
</template>

<style scoped>
header {
	align-items: center;
	background-color: var(--un-color-body-surface-primary);
	box-shadow: var(--un-box-shadow-small);
	display: flex;
	gap: 2em;
	padding: 0.5em 1em;
}

p {
	align-items: center;
	display: flex;
	font-size: var(--un-font-xlarge);
}

p > * + *::before {
	content: '>';
	margin: 0 1em;
}

p a {
	text-decoration: underline;
}

p a:hover,
p a:focus {
	color: var(--un-color-accent-dark);
}

aside {
	display: flex;
	margin-left: auto; /* Push it to the far side */
	gap: 1em;
}

button.data-explorer {
	background-color: var(--un-color-accent);
}
</style>
