<template>
	<Button
		icon="pi pi-ellipsis-v"
		rounded
		text
		@click.stop="toggle"
		:disabled="isEmpty(projectMenuItems)"
	/>
	<Menu ref="menu" :model="projectMenuItems" :popup="true" @focus="menuProject = project" />
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useProjects } from '@/composables/project';
import { isEmpty } from 'lodash';
import { useProjectMenu } from '@/composables/project-menu';

const props = defineProps<{ project: IProject | null }>();

const emit = defineEmits(['forked-project']);

// Triggers modals from tera-common-modal-dialogs.vue to open
const { isShareDialogVisible, isRemoveDialogVisible, isProjectConfigDialogVisible, menuProject } =
	useProjectMenu();

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

function toggle(event) {
	menu.value.toggle(event);
}
</script>
