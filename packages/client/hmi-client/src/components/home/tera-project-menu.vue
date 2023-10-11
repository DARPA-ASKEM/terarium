<template>
	<Button
		icon="pi pi-ellipsis-v"
		class="project-options p-button-icon-only p-button-text p-button-rounded"
		@click.stop="toggle"
		:disabled="projectMenuItems.length === 0"
	/>
	<Menu ref="menu" :model="projectMenuItems" :popup="true" @focus="setProject">
		<template #item="{ item }">
			<div>
				<span :class="item.icon"></span><span>{{ item.label }}</span>
			</div>
		</template>
	</Menu>
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useProjectMenu } from '@/composables/project-menu';
import { useProjects } from '@/composables/project';
import useAuthStore from '@/stores/auth';

const props = defineProps<{ project: IProject }>();
const emit = defineEmits(['forked-project']);

const menu = ref();
const renameMenuItem = {
	label: 'Rename',
	icon: 'pi pi-pencil',
	command: () => {}
};
const shareMenuItem = {
	label: 'Share',
	icon: 'pi pi-user-plus',
	command: () => {
		useProjectMenu().isShareDialogVisible.value = true;
	}
};
const removeMenuItem = {
	label: 'Remove',
	icon: 'pi pi-trash',
	command: () => {
		useProjectMenu().isRemoveDialogVisible.value = true;
	}
};
const forkMenuItem = {
	label: 'Fork this project',
	icon: 'pi pi-clone',
	command: async () => {
		const cloned = await useProjects().clone(props.project.id, useAuthStore().user?.name);
		emit('forked-project', cloned);
	}
};
const separatorMenuItem = { separator: true };
const projectMenuItems = computed(() => {
	if (props.project.publicProject) {
		return [forkMenuItem];
	}
	if (props.project.userPermission === 'creator') {
		return [renameMenuItem, separatorMenuItem, shareMenuItem, separatorMenuItem, removeMenuItem];
	}
	if (props.project.userPermission === 'writer') {
		return [renameMenuItem, separatorMenuItem, shareMenuItem];
	}
	if (props.project.userPermission === 'reader') {
		return [];
	}
	return [];
});

function setProject() {
	useProjectMenu().selectedMenuProject.value = props.project;
}

function toggle(event) {
	menu.value.toggle(event);
}
</script>

<style scoped>
div > span {
	margin-right: 0.5rem;
}

div {
	padding: 0.5rem 1rem;
}

div:hover {
	background: rgba(0, 0, 0, 0.04);
}
</style>
