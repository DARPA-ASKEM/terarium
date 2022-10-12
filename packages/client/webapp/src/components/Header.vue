<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconLogin16 from '@carbon/icons-vue/es/login/16';
import IconSearchLocate16 from '@carbon/icons-vue/es/search--locate/16';
import { useRouter } from 'vue-router';

const router = useRouter();

const goToHomepage = () => router.push('/');
const goToDataExplorer = () => router.push('/explorer');

const projectName = 'Name of the project that can be long for clarity and precision';
</script>

<template>
	<header>
		<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<p>
			<a @click="goToHomepage">Projects</a>
			<span>{{ projectName }}</span>
		</p>

		<aside>
			<Button class="data-explorer" @click="goToDataExplorer"><IconSearchLocate16 /></Button>
			<Button v-if="false" @click="login">Login <IconLogin16 /></Button>
		</aside>
	</header>
</template>

<style scoped>
header {
	align-items: center;
	background-color: var(--un-color-body-surface-primary);
	box-shadow: var(--un-box-shadow-small);
	display: flex;
	gap: 2rem;
	padding: 0.5rem 1rem;
}

p {
	align-items: center;
	display: flex;
	font-size: var(--un-font-xlarge);
}

p > * + *::before {
	content: '>';
	margin: 0 1rem;
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
	gap: 1rem;
}

button.data-explorer {
	background-color: var(--un-color-accent);
}
</style>
