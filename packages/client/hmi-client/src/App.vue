<template>
	<!-- Sets the Toast notification groups and their respective levels-->
	<Toast position="top-right" group="error" />
	<Toast position="top-right" group="warn" />
	<Toast position="bottom-right" group="info" />
	<Toast position="bottom-right" group="success" />
	<header>
		<tera-navbar :active="displayNavBar" :show-suggestions="showSuggestions" />
	</header>
	<main>
		<div id="step1" />
		<router-view v-slot="{ Component }">
			<component class="page" ref="pageRef" :is="Component" />
		</router-view>
	</main>
	<footer>
		<tera-footer />
	</footer>
	<tera-common-modal-dialogs />
</template>

<script setup lang="ts">
import Toast from 'primevue/toast';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useShepherd } from 'vue-shepherd';

import API from '@/api/api';
import TeraCommonModalDialogs from '@/components/widgets/tera-common-modal-dialogs.vue';
import TeraFooter from '@/components/navbar/tera-footer.vue';
import TeraNavbar from '@/components/navbar/tera-navbar.vue';
import { IProject } from '@/types/Project';
import { ResourceType } from '@/types/common';
import { ToastSeverity, ToastSummaries, useToastService } from '@/services/toast';
import { useCurrentRoute } from '@/router/index';
import { useProjects } from '@/composables/project';

const toast = useToastService();

/**
 * Router
 */
const route = useRoute();
const router = useRouter();
const currentRoute = useCurrentRoute();

const displayNavBar = computed(() => currentRoute.value.name !== 'unauthorized');

// This pageRef is used to grab the assetType being searched for in data-explorer.vue, it is accessed using defineExpose
const pageRef = ref();
// For navbar.vue -> search-bar.vue
// Later the asset type searched for in the data explorer should be in the route,
// so we won't have to pass this from here
const showSuggestions = computed(() => {
	const assetType = pageRef.value?.resourceType ?? ResourceType.XDD;
	return assetType === ResourceType.XDD;
});

const tour = useShepherd({
	useModalOverlay: true
});

/**
 * Project
 */
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

// Update the project when the projectId changes
watch(
	() => route.params.projectId,
	(projectId) => {
		useProjects().get(projectId as IProject['id']);
	},
	{ immediate: true }
);

onMounted(async () => {
	tour.addStep({
		attachTo: { element: '#step1', on: 'top' },
		text: 'Test'
	});
	tour.start();

	await useProjects().getAll();
});
</script>

<style scoped>
main {
	grid-area: main;
	display: flex;
	flex-grow: 1;
	isolation: isolate;
	z-index: 1;
	overflow: hidden;
	position: relative;
}

header {
	grid-area: header;
}

footer {
	grid-area: footer;
}

.page {
	z-index: 1;
	flex: 1;
	min-width: 0;
	display: flex;
	flex-grow: 1;
}
</style>
