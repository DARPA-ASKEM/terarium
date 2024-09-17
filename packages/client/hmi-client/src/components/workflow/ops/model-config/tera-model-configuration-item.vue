<template>
	<div class="config-card" :class="{ 'card-selected': selected }">
		<div>
			<span class="flex align-items-center"
				><h6>{{ configuration.name }}</h6>
				<Button class="ml-auto" text icon="pi pi-ellipsis-v" @click.stop="toggleContextMenu"></Button
			></span>
			<ContextMenu ref="contextMenu" :model="contextMenuItems"></ContextMenu>
			<p>{{ configuration.description }}</p>
			<p>{{ formatTimestamp(configuration.createdOn) }}</p>
		</div>
		<Divider />
	</div>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { formatTimestamp } from '@/utils/date';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import ContextMenu from 'primevue/contextmenu';
import { ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { deleteModelConfiguration } from '@/services/model-configurations';

const emit = defineEmits(['delete', 'use', 'download']);
const props = defineProps<{
	configuration: ModelConfiguration;
	selected?: boolean;
}>();

const confirm = useConfirm();

const contextMenu = ref();
const contextMenuItems = ref([
	{
		label: 'Use',
		icon: 'pi pi-arrow-right',
		command() {
			emit('use');
		}
	},
	{
		label: 'Download',
		icon: 'pi pi-download',
		command() {
			emit('download');
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-trash',
		disabled: true,
		command() {
			onDeleteConfiguration();
		}
	}
]);

const toggleContextMenu = (event) => {
	contextMenu.value.toggle(event);
};

const onDeleteConfiguration = () => {
	confirm.require({
		message: `Are you sure you want to delete the configuration ${props.configuration.name}?`,
		header: 'Delete Confirmation',
		icon: 'pi pi-exclamation-triangle',
		acceptLabel: 'Confirm',
		rejectLabel: 'Cancel',
		accept: async () => {
			if (props.configuration.id) await deleteModelConfiguration(props.configuration.id);
			emit('delete');
		}
	});
};
</script>

<style scoped>
.config-card {
	background: var(--surface-0);
	border-left: 4px solid var(--surface-300);
	&:hover {
		cursor: pointer;
		background-color: var(--gray-50);
	}
	&.card-selected {
		border-left: 4px solid var(--primary-color);
	}

	> div {
		padding-left: var(--gap);
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
