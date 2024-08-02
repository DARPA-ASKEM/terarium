<template>
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
		@mouseenter="emit('menu-mouseenter')"
		@mouseleave="emit('menu-mouseleave')"
		@focus="() => {}"
		@blur="() => {}"
	/>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Menu from 'primevue/menu';
import Button from 'primevue/button';
import { OperatorMenuItem } from '@/services/workflow';

const props = defineProps<{
	nodeMenu: OperatorMenuItem[];
}>();

const emit = defineEmits(['menu-mouseenter', 'menu-mouseleave', 'menu-selection']);

const isMenuShowing = ref<boolean>(false);
const menu = ref();
const menuItems = ref();

onMounted(() => {
	const options: Array<{}> = [];
	props.nodeMenu.forEach((node) =>
		options.push({
			label: node.displayName,
			command() {
				emit('menu-selection', node.type);
			}
		})
	);
	menuItems.value = [{ items: options }];
});

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
