<template>
	<section ref="operatorMenu" :style="style">
		{{ console.log(style) }}
		<Button
			v-if="node"
			ref="openMenuButton"
			class="button"
			type="button"
			size="small"
			@click="showMenu"
			label="+"
			:style="{
				top: `${currentPortPosition.y - 18}px`,
				left: `${currentPortPosition.x + 50}px`
			}"
			aria-haspopup="true"
			aria-controls="overlay_menu"
			severity="secondary"
		/>
		<Menu ref="menu" id="overlay_menu" :model="menuItems" :popup="true" />
	</section>
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
		items: [{ label: 'Configure Model' }, { label: 'Stratify Model' }, { label: 'Edit Model' }]
	}
]);

function showMenu(event) {
	console.log('click', event);
	// contextMenu.value.show(event);
	menu.value.toggle(event);
	isMenuShowing.value = true;
}
</script>
<style scoped>
.button {
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
