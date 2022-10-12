<script setup lang="ts">
import { onBeforeMount, computed } from 'vue';
import Header from '@/components/Header.vue';
import Sidebar from '@/components/Sidebar.vue';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
/**
 * Before mounting go fetch the SSO
 * to make sure user has credentials to view app
 */
onBeforeMount(() => {
	auth.fetchSSO();
});

const isAuthenticated = computed(() => auth.isAuthenticated);
// watch(isAuthenticated, (currentValue, oldValue) => {
// 	if (!currentValue && currentValue !== oldValue) {
// 		window.location.assign('/logout');
// 	}
// });
</script>

<template>
	<Header />
	<main v-if="isAuthenticated">
		<Sidebar />
		<router-view />
	</main>
</template>

<style scoped>
main {
	display: flex;
	flex-grow: 1;
	isolation: isolate;
}

/* Sidebar */
main > nav {
	z-index: 2;
}

/* Pages */
main > section {
	z-index: 1;
}
</style>
<style>
body {
	isolation: isolate;
}

header {
	z-index: 2;
}

main {
	z-index: 1;
}
</style>
