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
	<Sidebar v-if="isAuthenticated" />
	<router-view v-if="isAuthenticated"></router-view>
</template>
