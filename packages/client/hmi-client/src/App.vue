<script setup lang="ts">
import { ref } from 'vue';
import Header from '@/components/Header.vue';
import Sidebar from '@/components/Sidebar.vue';
import Overlay from '@/components/Overlay.vue';
import DataExplorer from '@/views/DataExplorer.vue';

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
		<Sidebar class="sidebar" />
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
