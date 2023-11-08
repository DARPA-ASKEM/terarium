<template>
	<Button
		icon="pi pi-ellipsis-v"
		rounded
		text
		@click.stop="toggle"
		:disabled="isEmpty(projectMenuItems)"
	/>
	<Menu ref="menu" :model="projectMenuItems" :popup="true" />
	<Teleport to="body">
		<tera-project-configuration-modal
			v-if="isProjectConfigModalVisible && project"
			confirm-text="Update"
			modal-title="Edit project"
			:project="project"
			@close-modal="isProjectConfigModalVisible = false"
		/>
	</Teleport>
	<Dialog :header="`Remove ${project?.name}`" v-model:visible="isRemoveDialogVisible">
		<p>
			You are about to remove project <em>{{ project?.name }}</em>.
		</p>
		<p>Are you sure?</p>
		<template #footer>
			<Button label="Cancel" class="p-button-secondary" @click="isRemoveDialogVisible = false" />
			<Button label="Remove project" @click="removeProject" />
		</template>
	</Dialog>
	<tera-share-project v-if="project" v-model="isShareDialogVisible" :project="project" />
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useProjects } from '@/composables/project';
import { isEmpty } from 'lodash';
import Dialog from 'primevue/dialog';
import TeraShareProject from '@/components/widgets/share-project/tera-share-project.vue';
import TeraProjectConfigurationModal from '@/page/project/components/tera-project-configuration-modal.vue';
import { logger } from '@/utils/logger';
import { useRouter } from 'vue-router';
import { RoutePath, useCurrentRoute } from '@/router/index';
import { RouteName } from '@/router/routes';

const router = useRouter();
const currentRoute = useCurrentRoute();

const props = defineProps<{ project: IProject | null }>();

const emit = defineEmits(['forked-project']);

// Not using project-menu.ts
const isProjectConfigModalVisible = ref(false);
const isShareDialogVisible = ref(false);
const isRemoveDialogVisible = ref(false);

const menu = ref();
const renameMenuItem = {
	label: 'Edit project details',
	icon: 'pi pi-pencil',
	command: () => {
		isProjectConfigModalVisible.value = true;
	}
};
const shareMenuItem = {
	label: 'Share',
	icon: 'pi pi-user-plus',
	command: () => {
		isShareDialogVisible.value = true;
	}
};
const removeMenuItem = {
	label: 'Remove',
	icon: 'pi pi-trash',
	command: () => {
		isRemoveDialogVisible.value = true;
	}
};
const forkMenuItem = {
	label: 'Fork this project',
	icon: 'pi pi-clone',
	command: async () => {
		if (props.project) {
			const cloned = await useProjects().clone(props.project.id);
			emit('forked-project', cloned);
		}
	}
};
const separatorMenuItem = { separator: true };
const projectMenuItems = computed(() => {
	if (props.project?.publicProject) {
		return [forkMenuItem];
	}
	if (props.project?.userPermission === 'creator') {
		return [renameMenuItem, separatorMenuItem, shareMenuItem, separatorMenuItem, removeMenuItem];
	}
	if (props.project?.userPermission === 'writer') {
		return [renameMenuItem, separatorMenuItem, shareMenuItem];
	}
	if (props.project?.userPermission === 'reader') {
		return [];
	}
	return [];
});

const removeProject = async () => {
	if (!props.project) return;
	const { name, id } = props.project;
	const isDeleted = await useProjects().remove(id);
	isRemoveDialogVisible.value = false;
	if (isDeleted) {
		useProjects().getAll();
		logger.info(`The project ${name} was removed`, { showToast: true });
		if (currentRoute.value.name !== RouteName.Home) router.push(RoutePath.Home);
	} else {
		logger.error(`Unable to delete the project ${name}`, { showToast: true });
	}
};

function toggle(event) {
	menu.value.toggle(event);
}
</script>
