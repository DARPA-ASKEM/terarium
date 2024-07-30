<template>
	<Button icon="pi pi-ellipsis-v" rounded text @click.stop="toggle" :disabled="isEmpty(projectMenuItems)" />
	<Menu ref="menu" :model="projectMenuItems" :popup="true" @focus="menuProject = project" />
</template>

<script setup lang="ts">
import { useProjects } from '@/composables/project';
import { useProjectMenu } from '@/composables/project-menu';
import { RouteName } from '@/router/routes';
import { ProjectPages } from '@/types/Project';
import { Project } from '@/types/Types';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const props = defineProps<{ project: Project | null }>();

const emit = defineEmits(['forked-project']);

// Triggers modals from tera-common-modal-dialogs.vue to open
const { isShareDialogVisible, isRemoveDialogVisible, isProjectConfigDialogVisible, menuProject } = useProjectMenu();

const menu = ref();
const renameMenuItem = {
	label: 'Edit project details',
	icon: 'pi pi-pencil',
	command: () => {
		isProjectConfigDialogVisible.value = true;
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
			if (!cloned) {
				return;
			}
			router.push({ name: RouteName.Project, params: { projectId: cloned?.id, pageType: ProjectPages.OVERVIEW } });
			emit('forked-project', cloned);
		}
	}
};

const separatorMenuItem = { separator: true };
const projectMenuItems = computed(() => {
	const items = [] as any[];
	if (props.project?.publicProject) {
		items.push(forkMenuItem);
	}
	if (props.project?.userPermission === 'creator') {
		items.push(renameMenuItem, shareMenuItem, separatorMenuItem, removeMenuItem);
	}
	if (props.project?.userPermission === 'writer') {
		items.push(renameMenuItem, shareMenuItem);
	}
	return items;
});

function toggle(event) {
	menu.value.toggle(event);
}
</script>
