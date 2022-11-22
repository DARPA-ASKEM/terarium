<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import Header from '@/components/Header.vue';
import Overlay from '@/components/Overlay.vue';
import DataExplorer from '@/views/DataExplorer.vue';
import Sidebar from '@/components/Sidebar.vue';
import API from '@/api/api';

import { useCurrentRouter } from './router/index';

const { isCurrentRouteHome } = useCurrentRouter();
const isSidebarVisible = computed(() => !isCurrentRouteHome.value);

const overlayActivated = ref(false);
const overlayMessage = ref('Loading...');

const enableOverlay = (message?: string) => {
	overlayActivated.value = true;
	if (message !== undefined) {
		overlayMessage.value = message;
	}
};

const disableOverlay = () => {
	overlayActivated.value = false;
};

const dataExplorerActivated = ref(false);

onMounted(() => {
	API.get('/projects').then((r) => {
		console.log(r.data);
	});
});
</script>

<template>
	<overlay v-if="overlayActivated" :message="overlayMessage" />
	<data-explorer
		v-if="dataExplorerActivated"
		class="data-explorer"
		@hide="dataExplorerActivated = false"
		@show-overlay="enableOverlay"
		@hide-overlay="disableOverlay"
	/>
	<Header class="header" @show-data-explorer="dataExplorerActivated = true" />
	<main>
		<Sidebar v-if="isSidebarVisible" class="sidebar" data-test-id="sidebar" />
		<router-view class="page" />
	</main>
</template>

<style scoped>
.header {
	z-index: 2;
}

main {
	display: flex;
	flex-grow: 1;
	isolation: isolate;
	z-index: 1;
}

.sidebar {
	z-index: 2;
}

.page {
	z-index: 1;
}

.data-explorer {
	z-index: 3;
}
</style>
