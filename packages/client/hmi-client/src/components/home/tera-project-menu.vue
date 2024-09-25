<template>
	<span v-if="isCopying">Copying...</span>
	<Button v-else icon="pi pi-ellipsis-v" rounded text @click.stop="toggle" :disabled="isEmpty(projectMenuItems)" />
	<Menu ref="menu" :model="projectMenuItems" :popup="true" @focus="menuProject = project" />
</template>

<script setup lang="ts">
import { useProjects } from '@/composables/project';
import { useProjectMenu } from '@/composables/project-menu';
import { Project } from '@/types/Types';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { computed, ref } from 'vue';
import { exportProjectAsFile } from '@/services/project';
import { AcceptedExtensions } from '@/types/common';

const props = defineProps<{ project: Project | null }>();

const emit = defineEmits(['copied-project']);

// Triggers modals from tera-common-modal-dialogs.vue to open
const { isShareDialogVisible, isRemoveDialogVisible, isProjectConfigDialogVisible, menuProject } = useProjectMenu();

const isCopying = ref(false);

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
const copyMenuItem = {
	label: 'Copy this project',
	icon: 'pi pi-clone',
	command: async () => {
		if (props.project) {
			isCopying.value = true;
			const copiedProject = await useProjects().clone(props.project.id);
			isCopying.value = false;
			if (!copiedProject) return;
			emit('copied-project', copiedProject);
		}
	}
};

const downloadMenuItem = {
	label: 'Download this project',
	icon: 'pi pi-download',
	command: async () => {
		if (props.project) {
			isCopying.value = true;
			const blob = await exportProjectAsFile(props.project.id);
			if (blob) {
				const a = document.createElement('a');
				a.href = URL.createObjectURL(blob);
				a.download = `${props.project.name}.${AcceptedExtensions.PROJECTCONFIG}`;
				a.click();
				a.remove();
			}
			isCopying.value = false;
		}
	}
};

const separatorMenuItem = { separator: true };
const projectMenuItems = computed(() => {
	const items = [] as any[];
	if (props.project?.publicProject || props.project?.userPermission === 'creator') {
		items.push(copyMenuItem);
		items.push(downloadMenuItem);
	}
	if (props.project?.userPermission === 'creator') {
		items.push(renameMenuItem, shareMenuItem, separatorMenuItem, removeMenuItem);
	}
	if (props.project?.userPermission === 'writer') {
		items.push(renameMenuItem);
	}
	return items;
});

function toggle(event) {
	menu.value.toggle(event);
}
</script>

<style scoped>
span {
	color: var(--primary-color);
}
</style>
