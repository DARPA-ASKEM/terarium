<template>
	<Button
		icon="pi pi-ellipsis-v"
		rounded
		text
		@click.stop="toggle"
		:disabled="isEmpty(projectMenuItems)"
	/>
	<Menu ref="menu" :model="projectMenuItems" :popup="true" @focus="setProject">
		<template #item="{ item }">
			<div>
				<span :class="item.icon"></span><span>{{ item.label }}</span>
			</div>
		</template>
	</Menu>
	<Teleport to="body">
		<tera-project-configuration-modal
			v-if="isProjectConfigModalVisible"
			confirm-text="Update"
			modal-title="Edit project"
			:project="project"
			@close-modal="isProjectConfigModalVisible = false"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { ref, computed, PropType } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useProjectMenu } from '@/composables/project-menu';
import { useProjects } from '@/composables/project';
import { isEmpty } from 'lodash';
import TeraProjectConfigurationModal from '@/page/project/components/tera-project-configuration-modal.vue';

const props = defineProps({
	project: {
		type: Object as PropType<IProject>,
		default: useProjects().activeProject.value
	}
});

const emit = defineEmits(['forked-project']);

const isProjectConfigModalVisible = ref(false);
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
		const cloned = await useProjects().clone(props.project.id);
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
