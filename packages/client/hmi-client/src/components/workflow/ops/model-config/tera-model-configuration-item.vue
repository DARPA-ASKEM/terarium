<template>
	<main :class="{ selected: selected }" @click="emit('use')">
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
	</main>

	<tera-modal v-if="viewLatexTable" class="w-8" @modal-mask-clicked="viewLatexTable = false">
		<template #header>
			<div class="flex align-items-center">
				<h4>LaTeX</h4>
				<Button class="p-button-sm ml-auto" severity="secondary" @click="setCopyClipboard(latexTable)">
					{{ btnCopyLabel }}
				</Button>
			</div>
		</template>
		<textarea v-model="latexTable" readonly width="100%" :rows="20" />
	</tera-modal>
	<tera-modal v-if="viewCSVTable" class="w-8" @modal-mask-clicked="viewCSVTable = false">
		<template #header>
			<div class="flex align-items-center">
				<h4>CSV</h4>
				<Button class="p-button-sm ml-auto" severity="secondary" @click="setCopyClipboard(csvTable)">
					{{ btnCopyLabel }}
				</Button>
			</div>
		</template>
		<textarea v-model="csvTable" readonly width="100%" :rows="20" />
	</tera-modal>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { formatTimestamp } from '@/utils/date';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import { computed, ref, watch } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import {
	deleteModelConfiguration,
	getModelConfigurationAsCsvTable,
	getModelConfigurationAsLatexTable
} from '@/services/model-configurations';
import { createCopyTextToClipboard } from '@/utils/clipboard';
import TeraModal from '@/components/widgets/tera-modal.vue';

const emit = defineEmits(['delete', 'use', 'downloadArchive', 'downloadModel']);
const props = defineProps<{
	configuration: ModelConfiguration;
	selected?: boolean;
	emptyInputCount?: string;
}>();

const isCalibrated = computed(() => props.configuration.simulationId !== null);

const viewLatexTable = ref(false);
const viewCSVTable = ref(false);
const { btnCopyLabel, setCopyClipboard } = createCopyTextToClipboard();
const latexTable = ref('');
const csvTable = ref('');
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
		label: 'Download',
		icon: 'pi pi-download',
		command() {
			emit('downloadArchive');
		}
	},
	{
		label: 'Download as configured model',
		icon: 'pi pi-download',
		command() {
			emit('downloadModel');
		}
	},
	{
		label: 'LaTeX table',
		icon: 'pi pi-table',
		disabled: isCalibrated,
		command() {
			showLatexTable();
		}
	},
	{
		label: 'CSV table',
		icon: 'pi pi-table',
		disabled: isCalibrated,
		command() {
			showCSVTable();
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

const showLatexTable = () => {
	viewLatexTable.value = true;
};
const showCSVTable = () => {
	viewCSVTable.value = true;
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

// Watch for viewLatexTable changes and fetch the latexTable value
watch(viewLatexTable, async (value) => {
	if (value) {
		latexTable.value = await getModelConfigurationAsLatexTable(props.configuration.id);
	}
});

watch(viewCSVTable, async (value) => {
	if (value) {
		csvTable.value = await getModelConfigurationAsCsvTable(props.configuration.id);
	}
});
</script>

<style scoped>
main {
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
