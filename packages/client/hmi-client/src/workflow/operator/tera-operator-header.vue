<template>
	<header :class="`${status} ${interactionClasses}`">
		<h5>{{ name }}</h5>
		<Button
			icon="pi pi-ellipsis-v"
			class="p-button-icon-only p-button-text p-button-rounded"
			@click="toggleMenu"
		/>
		<Menu ref="menu" :model="options" :popup="true" />
	</header>
</template>

<script setup lang="ts">
import { ref, PropType, computed } from 'vue';
import { isHover } from '@/services/operator-bitmask';
import { OperatorStatus } from '@/types/workflow';
import Button from 'primevue/button';
import Menu from 'primevue/menu';

const emit = defineEmits(['remove-operator', 'bring-to-front', 'open-in-new-window']);

const props = defineProps({
	name: {
		type: String,
		default: ''
	},
	status: {
		type: String as PropType<OperatorStatus>,
		default: OperatorStatus.DEFAULT
	},
	interactionStatus: {
		type: Number,
		default: 0
	}
});

const interactionClasses = computed(() => {
	const classes: string[] = [];
	// eslint-disable-next-line no-bitwise
	if (isHover(props.interactionStatus)) classes.push('focus');
	return classes.join(' ');
});

const menu = ref();
const toggleMenu = (event) => {
	menu.value.toggle(event);
};

const options = ref([
	{ icon: 'pi pi-clone', label: 'Duplicate', command: () => emit('bring-to-front') },
	{
		icon: 'pi pi-external-link',
		label: 'Open in new window',
		command: () => emit('open-in-new-window')
	},
	{ icon: 'pi pi-arrow-up', label: 'Bring to front', command: () => emit('bring-to-front') },
	{ icon: 'pi pi-arrow-down', label: 'Send to back', command: () => emit('bring-to-front') },
	{ icon: 'pi pi-trash', label: 'Remove', command: () => emit('remove-operator') }
]);
</script>

<style scoped>
header {
	display: flex;
	height: 2rem;
	padding: 0.5rem;
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

.disabled {
	background-color: var(--surface-disabled);
	color: var(--text-color-disabled);
}

.focus {
	background-color: var(--primary-color);
	color: var(--gray-0);
}

.p-button.p-button-icon-only {
	color: var(--text-color-primary);
}

.focus .p-button.p-button-icon-only,
.focus .p-button.p-button-icon-only:hover {
	color: var(--gray-0);
}

h5 {
	overflow: hidden;
	text-overflow: ellipsis;
	font-weight: normal;
}
</style>
