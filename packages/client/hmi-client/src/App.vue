<template>
	<!-- Sets the Toast notification groups and their respective levels-->
	<Toast position="top-right" group="error" />
	<Toast position="top-right" group="warn" />
	<Toast position="bottom-right" group="info" />
	<Toast position="bottom-right" group="success" />
	<tera-navbar class="header" :active="displayNavBar" :show-suggestions="showSuggestions" />
	<main>
		<router-view v-slot="{ Component }">
			<component class="page" ref="pageRef" :is="Component" :key="route.path" />
		</router-view>
	</main>
	<tera-footer class="footer" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import Toast from 'primevue/toast';

import { ToastSummaries, ToastSeverity, useToastService } from '@/services/toast';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import TeraNavbar from '@/components/navbar/tera-navbar.vue';
import TeraFooter from '@/components/navbar/tera-footer.vue';
import { IProject } from '@/types/Project';
import { ResourceType } from '@/types/common';
import { useProjects } from '@/composables/project';
import { useCurrentRoute } from './router/index';

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

.header {
	grid-area: header;
}

.footer {
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
