<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import Header from '@/components/Header.vue';
import Overlay from '@/components/Overlay.vue';
import DataExplorer from '@/views/DataExplorer.vue';
import Sidebar from '@/components/Sidebar.vue';
import { Project } from '@/types/Project';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { useCurrentRouter } from './router/index';

/**
 * Router
 */
const route = useRoute();
const { isCurrentRouteHome } = useCurrentRouter();
const isSidebarVisible = computed(() => !isCurrentRouteHome.value);

const resources = useResourcesStore();

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
		// If the projectId or the Project are null, set the Project to null.
		if (projectId && !!projectId) {
			const id = projectId as string;
			// fetch project metadata
			project.value = await ProjectService.get(id);
			// fetch basic metadata about project assets and save them into a global store/cache
			resources.activeProjectAssets = await ProjectService.getAssets(id);
			resources.setActiveProject(project.value);
		}
	},
	{ immediate: true }
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
		<Sidebar v-if="isSidebarVisible" class="sidebar" data-test-id="sidebar" :project="project" />
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
	overflow: hidden;
}

.sidebar {
	z-index: 2;
}

.page {
	z-index: 1;
	flex: 1;
	min-width: 0;
}

.data-explorer {
	z-index: 3;
}
</style>
