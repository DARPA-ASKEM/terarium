<template>
	<Button
		icon="pi pi-ellipsis-v"
		class="project-options p-button-icon-only p-button-text p-button-rounded"
		@click.stop="toggle"
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
import { ref } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useProjectMenu } from '@/composables/project-menu';

const props = defineProps<{ project: IProject }>();

const menu = ref();
const projectMenuItems = ref([
	{ label: 'Rename', icon: 'pi pi-pencil', command: () => {} },
	{
		label: 'Share',
		icon: 'pi pi-user-plus',
		command: () => {
			useProjectMenu().isShareDialogVisible.value = true;
		}
	},
	{ separator: true },
	{
		label: 'Remove',
		icon: 'pi pi-trash',
		command: () => {
			useProjectMenu().isRemoveDialogVisible.value = true;
		}
	}
]);

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
