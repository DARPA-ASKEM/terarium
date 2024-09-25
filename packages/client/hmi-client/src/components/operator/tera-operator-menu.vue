<template>
	<aside>
		<Button
			v-bind="$attrs"
			type="button"
			size="small"
			@click="showMenu"
			icon="pi pi-plus"
			aria-haspopup="true"
			aria-controls="overlay_menu"
			severity="secondary"
		/>
		<Menu
			ref="menu"
			id="overlay_menu"
			:model="menuItems"
			:popup="true"
			@focus="emit('menu-focus')"
			@blur="emit('menu-blur')"
			style="width: fit-content"
		/>
	</aside>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue';
import Menu from 'primevue/menu';
import Button from 'primevue/button';
import { OperatorMenuItem } from '@/services/workflow';

const props = defineProps<{
	nodeMenu: OperatorMenuItem[];
}>();

const emit = defineEmits(['menu-focus', 'menu-blur', 'menu-selection']);

const isMenuShowing = ref<boolean>(false);
const menu = ref();
const menuItems = computed(() =>
	props.nodeMenu.map((node) => ({
		label: node.displayName,
		command() {
			emit('menu-selection', node.type);
		}
	}))
);

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
	background: var(--surface-0);
	z-index: 10;

	&:hover {
		box-shadow: var(--overlay-menu-shadow-hover);
	}
}
</style>
