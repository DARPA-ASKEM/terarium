<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Navbar from '@/components/Navbar.vue';
import Sidebar from '@/components/Sidebar.vue';
import { Project } from '@/types/Project';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import API from '@/api/api';
import { RoutePath, useCurrentRoute } from './router/index';

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

const searchBarText = ref('');
const relatedSearchTerms = ref<string[]>([]);
const resources = useResourcesStore();
/**
 * Project
 *
 * As we use only one Project per application instance.
 * It is loaded at the root and passed to all views as prop.
 */
const project = ref<Project | null>(null);

function updateSearchBar(newQuery) {
	searchBarText.value = newQuery;
}

function updateRelatedSearchTerms(newTerms) {
	relatedSearchTerms.value = newTerms.slice(0, 5);
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
		}
	},
	{ immediate: true }
);
</script>

<template>
	<Navbar
		v-if="!isErrorState"
		class="header"
		:project="project"
		:searchBarText="searchBarText"
		:relatedSearchTerms="relatedSearchTerms"
	/>
	<main>
		<Sidebar
			v-if="isSidebarVisible && !isErrorState"
			class="sidebar"
			data-test-id="sidebar"
			:project="project"
		/>
		<router-view
			class="page"
			:project="project"
			@search-query-changed="updateSearchBar"
			@related-search-terms-updated="updateRelatedSearchTerms"
		/>
	</main>
	<footer>
		<img src="@assets/images/Uncharted-logo.png" alt="logo" class="uncharted-logo" />
	</footer>
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

footer {
	width: 100%;
	height: 48px;
	background-color: var(--gray-800);
	flex: none;
	display: flex;
	align-items: center;
}

.uncharted-logo {
	padding-left: 1rem;
}
</style>
