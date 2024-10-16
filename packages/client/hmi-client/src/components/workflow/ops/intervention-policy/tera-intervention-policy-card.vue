<template>
	<div class="policy-card" :class="{ 'card-selected': selected }">
		<div>
			<span class="flex align-items-center">
				<h6>{{ interventionPolicy.name }}</h6>
				<Button class="ml-auto" text icon="pi pi-ellipsis-v" @click.stop="toggleContextMenu" />
			</span>
			<ContextMenu ref="contextMenu" :model="contextMenuItems"></ContextMenu>
			<p>{{ interventionPolicy.description }}</p>
			<p>{{ formatTimestamp(interventionPolicy.createdOn) }}</p>
		</div>
		<Divider />
	</div>
</template>

<script setup lang="ts">
import { formatTimestamp } from '@/utils/date';
import ContextMenu from 'primevue/contextmenu';
import { ref } from 'vue';
import Divider from 'primevue/divider';
import Button from 'primevue/button';
import { InterventionPolicy } from '@/types/Types';

defineProps<{
	interventionPolicy: InterventionPolicy;
	selected?: boolean;
}>();

const emit = defineEmits(['use-intervention', 'delete-intervention-policy']);

const contextMenu = ref();
const contextMenuItems = [
	{
		label: 'Use',
		icon: 'pi pi-arrow-right',
		command() {
			emit('use-intervention');
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-trash',
		command() {
			emit('delete-intervention-policy');
		}
	}
];

const toggleContextMenu = (event) => {
	contextMenu.value.toggle(event);
};
</script>

<style scoped>
.policy-card {
	background-color: var(--gray-0);
	border-left: 4px solid var(--surface-300);

	&.card-selected {
		border-left: 4px solid var(--primary-color);
	}

	&:not(.card-selected):hover {
		cursor: pointer;
		background-color: var(--gray-50);
	}

	& > div {
		padding-left: var(--gap-small);
	}
}

p {
	color: var(--text-color-subdued);
}
:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: var(--gap);
		margin-bottom: 0;
		color: var(--gray-300);
	}
}
</style>
