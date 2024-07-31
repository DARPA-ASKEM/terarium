<template>
	<nav ref="operatorMenu" :style="style">
		<Button
			ref="openMenuButton"
			type="button"
			size="small"
			@click="showMenu"
			icon="pi pi-plus"
			:style="{
				top: `${currentPortPosition.y - 18}px`,
				left: `${currentPortPosition.x + 40}px`
			}"
			aria-haspopup="true"
			aria-controls="overlay_menu"
			severity="secondary"
		/>
		<Menu ref="menu" id="overlay_menu" :model="menuItems" :popup="true" />
	</nav>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import type { WorkflowNode } from '@/types/workflow';
import Menu from 'primevue/menu';
import Button from 'primevue/button';

defineProps<{
	node: WorkflowNode<any> | null;
	style: { top: string; left: string };
	currentPortPosition: any;
}>();

const openMenuButton = ref<HTMLElement>();

const isMenuShowing = ref<boolean>(false);
const operatorMenu = ref<HTMLElement>();

const menu = ref();
const menuItems = ref([
	{
		// label: "Options",
		items: [
			{
				label: 'Configure Model',
				command() {}
			},
			{
				label: 'Stratify Model',
				command() {}
			},
			{
				label: 'Edit Model',
				command() {}
			}
		]
	}
]);

function showMenu(event) {
	menu.value.show(event);
	isMenuShowing.value = true;
}
</script>
<style scoped>
button {
	outline: 1px solid var(--surface-border);
	border-radius: var(--border-radius-medium);
	box-shadow: var(--overlay-menu-shadow);
	transition: box-shadow 80ms ease;

	&:hover {
		box-shadow: var(--overlay-menu-shadow-hover);
		z-index: 2;
	}
}
</style>
