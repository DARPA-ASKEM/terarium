<template>
	<main>
		<header>Model Space</header>
		<section>
			<Button action @click="goToTheia">
				<IconScript16 />
				New model from code
			</Button>
		</section>
	</main>
</template>

<script setup lang="ts">
/**
 * Model Sidebar Panel
 * Display a file tree like structure of models available in the current Project.
 */

import { useRouter } from 'vue-router';
import Button from '@/components/Button.vue';
import IconScript16 from '@carbon/icons-vue/es/script/16';

const router = useRouter();
const goToTheia = () => router.push('/theia');
</script>

<style scoped>
main {
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	gap: 1em;
	padding: 1em;
}

header {
	color: var(--un-color-body-text-secondary);
	font: var(--un-font-h6);
}
</style>
