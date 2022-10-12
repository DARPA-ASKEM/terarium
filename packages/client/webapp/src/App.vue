<script setup lang="ts">
import { onBeforeMount, computed } from 'vue';
import Header from '@/components/Header.vue';
import Sidebar from '@/components/Sidebar.vue';
import Overlay from '@/components/Overlay.vue';

import { useAuthStore } from './stores/auth';
import { useAppStore } from './stores/app';

const auth = useAuthStore();
const appStore = useAppStore();

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

const overlayActivated = computed(() => appStore.overlayActivated);
const overlayMessage = computed(() => appStore.overlayMessage);
const overlayMessageSecondary = computed(() => appStore.overlayMessageSecondary);
const overlayCancelFn = computed(() => appStore.overlayCancelFn);
</script>

<template>
	<overlay
		v-if="overlayActivated"
		:message="overlayMessage"
		:messageSecondary="overlayMessageSecondary"
		:cancel-fn="overlayCancelFn"
	/>
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
}
</style>
