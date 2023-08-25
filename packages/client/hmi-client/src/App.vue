<template>
	<!-- Sets the Toast notification groups and their respective levels-->
	<Toast position="top-right" group="error" />
	<Toast position="top-right" group="warn" />
	<Toast position="bottom-right" group="info" />
	<Toast position="bottom-right" group="success" />
	<tera-navbar
		class="header"
		:active="!isErrorState"
		:current-project-id="project?.id ?? null"
		:projects="projects"
		:show-suggestions="showSuggestions"
	/>
	<main>
		<router-view v-slot="{ Component }">
			<component class="page" ref="pageRef" :is="Component" :project="project" />
		</router-view>
	</main>
	<footer class="footer">
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
		<div class="footer-group">
			<a target="_blank" rel="noopener noreferrer" @click="isAboutModalVisible = true">About</a>
			<a target="_blank" rel="noopener noreferrer" :href="documentation">Documentation</a>
			<a target="_blank" rel="noopener noreferrer" href="https://terarium.canny.io/report-an-issue"
				>Report an issue</a
			>
			<a
				target="_blank"
				rel="noopener noreferrer"
				href="https://terarium.canny.io/request-a-feature"
				>Request a feature</a
			>
		</div>
	</footer>
	<tera-modal
		v-if="isAboutModalVisible"
		class="modal"
		@modal-mask-clicked="isAboutModalVisible = false"
		@modal-enter-press="isAboutModalVisible = false"
	>
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
	</tera-modal>
</template>

<script setup lang="ts">
import { computed, shallowRef, ref, watch, provide } from 'vue';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ToastSummaries, ToastSeverity, useToastService } from '@/services/toast';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import TeraNavbar from '@/components/navbar/tera-navbar.vue';
import * as ProjectService from '@/services/project';
import { IProject } from '@/types/Project';
import { ResourceType } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { addAssetKey, deleteAssetKey } from '@/symbols';
import { useCurrentRoute } from './router/index';
import { AssetType } from './types/Types';

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
const project = shallowRef<IProject | null>(null);
const projects = shallowRef<IProject[] | null>(null);

async function addAsset(projectId: string, assetType: string, assetId: string) {
	const newAssetId = await ProjectService.addAsset(projectId, assetType, assetId);
	setTimeout(async () => {
		project.value = await ProjectService.get(projectId as IProject['id'], true);
	}, 1000);
	return newAssetId;
}

async function deleteAsset(projectId: string, assetType: AssetType, assetId: string) {
	return ProjectService.deleteAsset(projectId, assetType, assetId);
}

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
			// fetchProject(projectId as IProject['id']);
			project.value = await ProjectService.get(projectId as IProject['id'], true);
		} else {
			project.value = null;
		}

		// Refetch the list of all projects
		projects.value = (await ProjectService.getAll()) as unknown as IProject[];
	},
	{ immediate: true }
);

// This is crucial - every time the resource store is modified the project prop will be updated to match it
// Once you update assets within a project (add/remove) the project service sets the resource store to what's in the project in the backend
// resourcesStore.$subscribe((_mutation, state) => {
// 	project.value = state.activeProject;
// });

const isAboutModalVisible = ref(false);

const documentation = computed(() => {
	const host = window.location.hostname ?? 'localhost';
	if (host === 'localhost') {
		return '//localhost:8000';
	}
	const url = host.replace(/\bapp\b/g, 'documentation');
	return `https://${url}`;
});

provide('activeProject', project);
provide(addAssetKey, addAsset);
provide(deleteAssetKey, deleteAsset);
provide('projectList', projects);
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
	gap: 2rem;
	grid-area: footer;
	height: 3rem;
	justify-content: space-between;
}

.footer-group {
	font-size: var(--font-caption);
	margin: 0 2rem;
	display: flex;
	align-items: center;
	justify-content: space-around;
	gap: 2rem;
}

.footer-group a {
	text-decoration: none;
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
</style>
