<template>
	<Navbar
		class="header"
		:active="!isErrorState"
		:current-project-id="project?.id ?? null"
		:projects="projects"
		:resourceType="resourceType"
	/>
	<main>
		<slider-panel
			v-if="isSidebarVisible && !isErrorState"
			content-width="240px"
			header="Resources"
			direction="left"
			class="sidebar"
		>
			<template v-slot:content>
				<tera-resources-sidebar :project="project" />
			</template>
		</slider-panel>
		<Sidebar
			v-if="isSidebarVisible && !isErrorState"
			class="sidebar"
			data-test-id="sidebar"
			:project="project"
		/>
		<router-view class="page" :project="project" @resource-type-changed="updateResourceType" />
	</main>
	<footer>
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
	</footer>
</template>

<script setup lang="ts">
import { computed, shallowRef, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import Sidebar from '@/components/Sidebar.vue';
import Navbar from '@/components/Navbar.vue';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { ProjectType } from '@/types/Project';
import { logBuffer } from '@/utils/logger';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import TeraResourcesSidebar from '@/components/tera-resources-sidebar.vue';
import { RoutePath, useCurrentRoute } from './router/index';
import { ResourceType } from './types/common';

logBuffer.startService();

/**
 * Router
 */
const route = useRoute();
const router = useRouter();
const currentRoute = useCurrentRoute();
const isSidebarVisible = computed(
	() =>
		currentRoute.value.path !== RoutePath.Home && currentRoute.value.path !== RoutePath.DataExplorer
);

const isErrorState = computed(() => currentRoute.value.name === 'unauthorized');

const resources = useResourcesStore();
const resourceType = ref<string>(ResourceType.XDD);

/**
 * Project
 *
 * As we use only one Project per application instance.
 * It is loaded at the root and passed to all views as prop.
 */
const project = shallowRef<ProjectType | null>(null);
const projects = shallowRef<ProjectType[] | null>(null);

function updateResourceType(newResourceType) {
	resourceType.value = newResourceType;
}

API.interceptors.response.use(
	(response) => response,
	(error) => {
		const status = error.response.status;
		console.error(error);
		if (status === 401 || status === 403) {
			router.push({ name: 'unauthorized' });
		}
	}
);

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
		} else {
			project.value = null;
		}

		// Refetch the list of all projects
		projects.value = await ProjectService.getAll();
	},
	{ immediate: true }
);

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
resources.$subscribe((mutation, state) => {
	project.value = state.activeProject;
});
</script>

<style scoped>
.header {
	grid-area: header;
}

main {
	grid-area: main;
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

footer {
	align-items: center;
	background-color: var(--surface-section);
	border-top: 1px solid var(--surface-border);
	display: flex;
	grid-area: footer;
	height: 3rem;
}
</style>
