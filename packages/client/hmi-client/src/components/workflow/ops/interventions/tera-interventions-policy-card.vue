<template>
	<div class="policy-card" :class="{ 'card-selected': selected }">
		<div>
			<span class="flex align-items-center"
				><h6>{{ interventionPolicy.name }}</h6>
				<Button
					class="ml-auto"
					text
					icon="pi pi-ellipsis-v"
					@click.stop="toggleContextMenu"
				></Button
			></span>
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

const emit = defineEmits(['use']);

const contextMenu = ref();
const contextMenuItems = [
	{
		label: 'Use',
		icon: 'pi pi-arrow-right',
		command() {
			emit('use');
		}
	}
];

const toggleContextMenu = (event) => {
	contextMenu.value.toggle(event);
};
</script>

<style scoped>
.policy-card {
	&:hover {
		cursor: pointer;
		background-color: var(--gray-100);
	}
	&.card-selected {
		background-color: var(--gray-100);
		border-left: 4px solid var(--primary-color);
	}

	> div {
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
