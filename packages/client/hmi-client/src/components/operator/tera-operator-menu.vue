<template>
	<nav>
		<Button
			type="button"
			size="small"
			@click="showMenu"
			icon="pi pi-plus"
			aria-haspopup="true"
			aria-controls="overlay_menu"
			severity="secondary"
			:style="style"
		/>
		<Menu ref="menu" id="overlay_menu" :model="menuItems" :popup="true" />
	</nav>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Menu from 'primevue/menu';
import Button from 'primevue/button';

defineProps<{
	style: { top: string; left: string };
}>();
const isMenuShowing = ref<boolean>(false);

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
nav {
	position: relative;
}

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
