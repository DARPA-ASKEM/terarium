<script setup lang="ts">
import { onBeforeMount, ref, computed } from 'vue';
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
	if (localStorage.sidebarPosition === undefined) {
		localStorage.setItem('sidebarPosition', 'left');
	}
});

const isAuthenticated = computed(() => auth.isAuthenticated);
const sidebarPosition = ref(localStorage.sidebarPosition);
// watch(isAuthenticated, (currentValue, oldValue) => {
// 	if (!currentValue && currentValue !== oldValue) {
// 		window.location.assign('/logout');
// 	}
// });
</script>

<template>
	<Header />
	<div :class="sidebarPosition" v-if="isAuthenticated">
		<Sidebar
			:sidebar-position="sidebarPosition"
			@updateSidebarPosition="(newPosition) => (sidebarPosition = newPosition)"
		/>
		<router-view />
	</div>
</template>

<style scoped>
div {
	display: flex;
	height: calc(100vh - 3.5rem);
}

div.right {
	flex-direction: row-reverse;
}
</style>
