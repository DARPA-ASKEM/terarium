<template>
	<div :class="{ selected: selected }">
		<header>
			<h6 class="constrain-width">{{ configuration.name }}</h6>
			<Button text icon="pi pi-ellipsis-v" @click.stop="toggleContextMenu" />
		</header>
		<ContextMenu
			ref="contextMenu"
			:model="contextMenuItems"
			@focus="() => {}"
			@blur="hideContextMenu"
			@mouseenter="contextMenuInFocus = true"
			@mouseleave="contextMenuInFocus = false"
		/>
		<p class="constrain-width">{{ configuration.description }}</p>
		<p>{{ formatTimestamp(configuration.createdOn) }}</p>
		<div v-if="emptyInputCount" class="input-count">{{ emptyInputCount }}</div>
	</div>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { formatTimestamp } from '@/utils/date';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import { ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { deleteModelConfiguration } from '@/services/model-configurations';

const emit = defineEmits(['delete', 'use', 'downloadArchive', 'downloadModel']);
const props = defineProps<{
	configuration: ModelConfiguration;
	selected?: boolean;
	emptyInputCount?: string;
}>();

const confirm = useConfirm();
const contextMenuInFocus = ref(false);
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
		label: 'Download model configuration',
		icon: 'pi pi-download',
		command() {
			emit('downloadArchive');
		}
	},
	{
		label: 'Download as Model',
		icon: 'pi pi-download',
		command() {
			emit('downloadModel');
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

const hideContextMenu = () => {
	if (!contextMenuInFocus.value) {
		contextMenu.value.hide();
	}
};

const onDeleteConfiguration = () => {
	confirm.require({
		message: `Are you sure you want to delete the configuration ${props.configuration.name}?`,
		header: 'Delete confirmation',
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
div {
	background: var(--surface-0);
	border-radius: var(--border-radius);
	border-left: 4px solid var(--surface-300);
	padding: var(--gap-1) var(--gap-1) var(--gap-3) var(--gap-3);
	box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
	&:hover {
		background-color: var(--surface-highlight);
		cursor: pointer;
	}

	&.selected {
		border-left-color: var(--primary-color);
	}

	&,
	&.selected {
		transition: border-left-color 15ms;
	}
}

header {
	display: flex;
	align-items: center;

	& > *:last-child {
		margin-left: auto;
	}
}

p,
span {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	padding-right: var(--gap-6);
}
span {
	padding-right: unset;
}

p + p {
	margin-top: var(--gap-2);
}

.constrain-width {
	word-wrap: break-word; /* Legacy property */
	overflow-wrap: break-word; /* Modern property */
	word-break: break-word; /* For compatibility */
	max-width: 100%; /* Ensures the text stays within container */
}

.input-count {
	padding: var(--gap-1) var(--gap-2);
	border-radius: var(--border-radius);
	margin-top: var(--gap-2);
	width: fit-content;
	font-size: var(--font-caption);
	background-color: var(--error-background);
	border: 1px solid var(--error-color);
}
.input-count:hover {
	background-color: var(--error-background);
}
</style>
