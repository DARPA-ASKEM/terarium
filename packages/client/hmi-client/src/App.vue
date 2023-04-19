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
		:show-suggestions="showSuggestions"
	/>
	<main>
		<router-view v-slot="{ Component }">
			<component
				class="page"
				ref="pageRef"
				:is="Component"
				:project="project"
				@update-project="fetchProject"
			/>
		</router-view>
	</main>
	<footer class="footer">
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
		<div class="footer-button-group">
			<Button label="about" class="footer-button" text @click="isAboutModalVisible = true" />
			<a href="https://terarium.canny.io/report-an-issue" class="no-text-decoration">
				<Button label="report an issue" class="footer-button" text />{{ null }}
			</a>
			<a href="https://terarium.canny.io/request-a-feature" class="no-text-decoration">
				<Button label="request a feature" class="footer-button" text />{{ null }}
			</a>
		</div>
	</footer>

	<Modal v-if="isAboutModalVisible" class="modal" @modal-mask-clicked="isAboutModalVisible = false">
		<template #header>
			<h4>About Terarium</h4>
		</template>
		<template #default>
			<div class="about-modal-content">
				<div class="about-section">
					<img
						src="@/assets/svg/terarium-logo.svg"
						alt="Terarium logo"
						class="about-terarium-logo"
					/>
				</div>
				<div class="about-section">
					<p class="constrain-width">
						Terarium is a comprehensive modeling and simulation platform designed to help
						researchers and analysts find models in academic literature, parameterize and calibrate
						them, run simulations to test a variety of scenarios, and analyze the results.
					</p>
				</div>
				<div class="about-section">
					<img
						src="@/assets/svg/uncharted-logo-official.svg"
						alt="Uncharted Software logo"
						class="about-uncharted-logo"
					/>
					<p class="constrain-width">
						Uncharted Software provides design, development and consulting services related to data
						visualization and analysis software.
					</p>
				</div>
			</div>
		</template>
		<template #footer>
			<div class="modal-footer">
				<p>&copy; Copyright Uncharted Software {{ new Date().getFullYear() }}</p>
				<Button class="p-button" @click="isAboutModalVisible = false">Close</Button>
			</div>
		</template>
	</Modal>
</template>

<script setup lang="ts">
import { computed, shallowRef, ref, watch } from 'vue';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ToastSummaries, ToastSeverity, useToastService } from '@/services/toast';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import Navbar from '@/components/Navbar.vue';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { IProject } from '@/types/Project';
import { ResourceType } from '@/types/common';
import { useCurrentRoute } from './router/index';
import Modal from './components/widgets/Modal.vue';

const toast = useToastService();
/**
 * Router
 */
const route = useRoute();
const router = useRouter();
const currentRoute = useCurrentRoute();

const isErrorState = computed(() => currentRoute.value.name === 'unauthorized');

// This pageRef is used to grab the assetType being searched for in data-explorer.vue, it is accessed using defineExpose
const pageRef = ref();
// For navbar.vue -> search-bar.vue
// Later the asset type searched for in the data explorer should be in the route so we won't have to pass this from here
const showSuggestions = computed(() => {
	const assetType = pageRef.value?.resourceType ?? ResourceType.XDD;
	return assetType === ResourceType.XDD;
});

/**
 * Project
 *
 * As we use only one Project per application instance.
 * It is loaded at the root and passed to all views as prop.
 */
const resourcesStore = useResourcesStore();
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

async function fetchProject(id: IProject['id']) {
	// fetch project metadata
	project.value = await ProjectService.get(id, true);

	// fetch basic metadata about project assets and save them into a global store/cache
	resourcesStore.activeProjectAssets = project.value?.assets ?? null;
	resourcesStore.setActiveProject(project.value);
}

watch(
	() => route.params.projectId,
	async (projectId) => {
		// If the projectId or the Project are null, set the Project to null.
		if (projectId && !!projectId) {
			fetchProject(projectId as IProject['id']);
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
resourcesStore.$subscribe((mutation, state) => {
	project.value = state.activeProject;
});

const isAboutModalVisible = ref(false);
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
	border-top: 1px solid var(--surface-border-light);
	display: flex;
	grid-area: footer;
	height: 3rem;
	justify-content: space-between;
}

.footer-button-group {
	padding-right: 3rem;
}

.no-text-decoration {
	text-decoration: none;
}

.p-button.p-component.p-button-text.footer-button {
	color: var(--text-color-secondary);
}
.p-button.p-component.p-button-text.footer-button:hover {
	color: var(--text-color-primary);
}

.about-modal-content {
	display: flex;
	flex-direction: column;
	gap: 2rem;
}

.about-terarium-logo {
	margin-top: 1rem;
	width: 20rem;
}
.about-uncharted-logo {
	width: 10rem;
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.about-section {
	display: flex;
	flex-direction: column;
	flex-wrap: wrap;
	justify-content: space-between;
}
.modal-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}
.constrain-width {
	max-width: 40rem;
}

.subdued {
	color: var(--text-color-secondary);
}
</style>
