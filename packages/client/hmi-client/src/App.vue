<template>
	<!-- Sets the Toast notification groups and their respective levels-->
	<Toast position="top-center" group="error" />
	<Toast position="top-center" group="warn" />
	<Toast position="top-center" group="info" />
	<Toast position="top-center" group="success" />
	<header>
		<tera-navbar :active="displayNavBar" />
	</header>
	<main>
		<router-view v-slot="{ Component }">
			<component class="page" :is="Component" />
		</router-view>
		<ConfirmDialog class="w-4" />
	</main>
	<footer>
		<tera-footer />
	</footer>
	<tera-common-modal-dialogs />
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import Toast from 'primevue/toast';

import { ToastSeverity, ToastSummaries, useToastService } from '@/services/toast';
import { useRoute, useRouter } from 'vue-router';
import API from '@/api/api';
import TeraNavbar from '@/components/navbar/tera-navbar.vue';
import TeraFooter from '@/components/navbar/tera-footer.vue';
import { useProjects } from '@/composables/project';
import { Project } from '@/types/Types';
import ConfirmDialog from 'primevue/confirmdialog';
import TeraCommonModalDialogs from './components/widgets/tera-common-modal-dialogs.vue';
import { useCurrentRoute } from './router/index';

const toast = useToastService();

/* Router */
const route = useRoute();
const router = useRouter();
const currentRoute = useCurrentRoute();
const displayNavBar = computed(() => currentRoute.value.name !== 'unauthorized');

/* Project */
API.interceptors.response.use(
	(response) => response,
	(error) => {
		const status = error.response.status;
		toast.showToast(ToastSeverity.error, `${ToastSummaries.NETWORK_ERROR} (${status})`, 'Unauthorized', 5000);
		if (status === 401 || status === 403) {
			router.push({ name: 'unauthorized' });
		}
	}
);

// Update the project when the projectId changes
watch(
	() => route.params.projectId,
	(projectId) => {
		useProjects().get(projectId as Project['id']);
	},
	{ immediate: true }
);
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
