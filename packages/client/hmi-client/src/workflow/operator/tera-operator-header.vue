<template>
	<nav :class="status">
		<h5>{{ name }}</h5>
		<Button
			icon="pi pi-ellipsis-v"
			class="p-button-icon-only p-button-text p-button-rounded"
			@click="toggleMenu"
		/>
		<Menu ref="menu" :model="options" :popup="true" />
	</nav>
</template>

<script setup lang="ts">
import { ref, PropType } from 'vue';
import { OperatorStatusType } from '@/types/operator';
import Button from 'primevue/button';
import Menu from 'primevue/menu';

const emit = defineEmits(['remove-node', 'open-in-new-window']);

defineProps({
	name: {
		type: String,
		default: ''
	},
	status: {
		type: Object as PropType<OperatorStatusType>,
		default: OperatorStatusType.DEFAULT
	}
});

const menu = ref();
const toggleMenu = (event) => {
	menu.value.toggle(event);
};

function bringToFront() {
	// TODO: bring to front
	// maybe there can be a z-index variable in the parent component
	// and we can just increment it here, and add a z-index style to the node
	// console.log('bring to front');
}

const options = ref([
	{ icon: 'pi pi-clone', label: 'Duplicate', command: () => bringToFront },
	{
		icon: 'pi pi-external-link',
		label: 'Open in new window',
		command: () => emit('open-in-new-window')
	},
	{ icon: 'pi pi-arrow-up', label: 'Bring to front', command: () => bringToFront },
	{ icon: 'pi pi-arrow-down', label: 'Bring to back', command: () => bringToFront },
	{ icon: 'pi pi-trash', label: 'Remove', command: () => emit('remove-node') }
]);
</script>

<style scoped>
nav {
	display: flex;
	padding: 0.5rem;
	height: 32px;
	justify-content: space-between;
	align-items: center;
	background-color: var(--surface-highlight);
	white-space: nowrap;
	border-top-right-radius: var(--border-radius-medium);
	border-top-left-radius: var(--border-radius-medium);
}

.warning {
	background-color: var(--surface-warning);
}

.error,
.failed {
	background-color: var(--surface-error);
}

.focus {
	background-color: var(--primary-color);
	color: var(--gray-0);
}

.disabled {
	background-color: var(--surface-disabled);
	color: var(--text-color-disabled);
}

nav.active-node {
	background-color: var(--primary-color);
}
nav .p-button.p-button-text:enabled:hover {
	color: var(--surface-highlight);
}

h5 {
	overflow: hidden;
	text-overflow: ellipsis;
	font-weight: normal;
}
</style>
