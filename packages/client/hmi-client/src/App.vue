<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Header from '@/components/Header.vue';
import Overlay from '@/components/Overlay.vue';
import DataExplorer from '@/views/DataExplorer.vue';
import Sidebar from '@/components/Sidebar.vue';
import { Project } from '@/types/Project';
import * as ProjectService from '@/services/project';
import { useCurrentRouter } from './router/index';

/**
 * Router
 */
const route = useRoute();
const router = useRouter();
const goToHomepage = () => router.push('/');
const { isCurrentRouteHome } = useCurrentRouter();
const isSidebarVisible = computed(() => !isCurrentRouteHome.value);

/**
 * Data Explorer
 */
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

/**
 * Project
 *
 * As we use only one Project per application instance.
 * It is loaded at the root and passed to all views as prop.
 */
const project = ref<Project | null>(null);

watch(
	() => route.params.projectId,
	async (projectId) => {
		if (!projectId) {
			// Set the Project to null,
			// and send the user to the homepage.
			project.value = null;
			goToHomepage();
		} else {
			const id = projectId as string;
			project.value = await ProjectService.get(id);
			if (!project.value) {
				goToHomepage();
			}
		}
	}
);
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
	<Header
		class="header"
		:projectName="project?.name"
		@show-data-explorer="dataExplorerActivated = true"
	/>
	<main>
		<Sidebar v-if="isSidebarVisible" class="sidebar" data-test-id="sidebar" />
		<router-view class="page" :project="project" />
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
