<template>
	<main>
		<header>Profile</header>
		<section>
			<Button @click="logout"
				>Logout
				<IconLogout16 />
			</Button>
		</section>
	</main>
</template>

<script setup lang="ts">
/**
 * Profile Sidebar Panel
 * Display option specific to the user.
 */

import Button from '@/components/Button.vue';
import IconLogout16 from '@carbon/icons-vue/es/logout/16';
import useAuthStore from '@/stores/auth';

const auth = useAuthStore();

const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};
</script>

<style scoped>
main {
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
}

header {
	color: var(--un-color-body-text-secondary);
	font: var(--un-font-h5);
}
</style>
