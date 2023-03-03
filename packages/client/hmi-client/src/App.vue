<template>
	<!-- Sets the Toast notification groups and their respective levels-->
	<Toast position="top-right" group="error" />
	<Toast position="top-right" group="warn" />
	<Toast position="bottom-right" group="info" />
	<Toast position="bottom-right" group="success" />
	<Navbar
		class="header"
		:active="!isErrorState"
		:current-project-id="project?.id ?? null"
		:projects="projects"
		:resourceType="pageRef?.resourceType ?? ResourceType.XDD"
	/>
	<main>
		<router-view v-slot="{ Component }">
			<component class="page" ref="pageRef" :is="Component" :project="project" />
		</router-view>
	</main>
	<footer>
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
	</footer>
</template>

<script setup lang="ts">
import { computed, shallowRef, ref, watch } from 'vue';
import Toast from 'primevue/toast';
import { ToastSummaries, ToastSeverity, useToastService } from '@/services/toast';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import Navbar from '@/components/Navbar.vue';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { IProject } from '@/types/Project';
import { ResourceType } from '@/types/common';
import { useCurrentRoute } from './router/index';

const toast = useToastService();
/**
 * Router
 */
const route = useRoute();
const router = useRouter();
const currentRoute = useCurrentRoute();
const isErrorState = computed(() => currentRoute.value.name === 'unauthorized');

const resources = useResourcesStore();
const pageRef = ref();

/**
 * Project
 *
 * As we use only one Project per application instance.
 * It is loaded at the root and passed to all views as prop.
 */
const project = shallowRef<IProject | null>(null);
const projects = shallowRef<IProject[] | null>(null);

API.interceptors.response.use(
	(response) => response,
	(error) => {
		const status = error.response.status;
		toast.showToast(
			ToastSeverity.error,
			`${ToastSummaries.NETWORK_ERROR} (${status})`,
			'Unauthorized',
			5000
		);
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
	position: relative;
}

.page {
	z-index: 1;
	flex: 1;
	min-width: 0;
	display: flex;
	flex-grow: 1;
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
