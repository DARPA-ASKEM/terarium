<template>
	<tera-project-configuration-modal
		id="project-configuration-modal"
		v-if="isProjectConfigDialogVisible"
		:confirm-text="menuProject ? 'Update' : 'Create'"
		:modal-title="menuProject ? 'Edit project' : 'Create project'"
		:project="menuProject"
		@close-modal="isProjectConfigDialogVisible = false"
	/>
	<Dialog
		modal
		style="max-width: 65ch"
		:header="`Delete ${menuProject?.name}?`"
		v-model:visible="isRemoveDialogVisible"
	>
		<p>
			This action is irreversible and will permanently remove
			<span style="font-weight: 600">{{ menuProject?.name }}</span>
			from the system.
		</p>
		<p>Are you sure?</p>
		<template #footer>
			<Button label="Cancel" class="p-button-secondary" @click="isRemoveDialogVisible = false" />
			<Button label="Delete project" severity="danger" @click="removeProject" autofocus />
		</template>
	</Dialog>
	<tera-share-project v-if="menuProject" v-model="isShareDialogVisible" :project="menuProject" />
</template>

<script setup lang="ts">
// To easily display modal dialogs that will commonly appear throughout the app (therefore lives in App.vue)
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import TeraShareProject from '@/components/widgets/share-project/tera-share-project.vue';
import TeraProjectConfigurationModal from '@/components/project/tera-project-configuration-modal.vue';
import { logger } from '@/utils/logger';
import { useRouter } from 'vue-router';
import { RoutePath, useCurrentRoute } from '@/router/index';
import { RouteName } from '@/router/routes';
import { useProjects } from '@/composables/project';
import { useProjectMenu } from '@/composables/project-menu';

const router = useRouter();
const currentRoute = useCurrentRoute();

// For now, we just use project-menu.ts to manage modals related to projects
// For non-project related modals we may want to create new composable or abstract project-menu.ts into a modal manager
const { isShareDialogVisible, isRemoveDialogVisible, isProjectConfigDialogVisible, menuProject } = useProjectMenu();

const removeProject = async () => {
	if (!menuProject.value) return;
	const { name, id } = menuProject.value;
	const isDeleted = await useProjects().remove(id);
	isRemoveDialogVisible.value = false;
	if (isDeleted) {
		useProjects().getAll();
		logger.info(`The project ${name} was removed`, { showToast: true });
		if (currentRoute.value.name !== RouteName.Home) router.push(RoutePath.Home);
	}
};
</script>

<style scoped>
p + p {
	margin-top: var(--gap-2);
}
</style>
