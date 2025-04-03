<template>
	<Panel class="asset-panel" :toggleable="isToggleable" :class="{ selected: isSelected }">
		<template #header>
			<slot name="header" />
		</template>
		<template #icons>
			<Button v-if="isDeletable" icon="pi pi-trash" text rounded @click="emit('delete')" />
		</template>
		<template #togglericon="{ collapsed }">
			<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
		</template>
		<main class="panel-content">
			<slot />
		</main>

		<template v-if="isSelected" #footer>
			<footer class="flex">
				<Button label="Close" outlined severity="secondary" @click.stop="emit('close')" />
				<Button class="ml-auto" icon="pi pi-arrow-up" label="Previous" @click.stop="emit('previous')" />
				<Button class="ml-2" icon="pi pi-arrow-down" icon-pos="right" label="Next" @click.stop="emit('next')" />
			</footer>
		</template>
	</Panel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel';
import Button from 'primevue/button';

const emit = defineEmits(['delete', 'edit', 'update:is-included', 'next', 'previous', 'close']);

defineProps({
	isDeletable: {
		type: Boolean
	},
	isIncluded: {
		type: Boolean
	},
	isToggleable: {
		type: Boolean,
		default: true
	},
	isSelected: {
		type: Boolean,
		default: false
	}
});
</script>

<style scoped>
.asset-panel {
	border: 1px solid var(--surface-border-light);
	border-left: 4px solid var(--surface-400);
	border-radius: var(--border-radius);
	overflow: auto;
	background: var(--surface-0);
	cursor: pointer;
	&.selected {
		border-left: 4px solid var(--primary-color);
	}
}
.asset-panel:deep(.p-panel-header) {
	padding-bottom: var(--gap-1);
	background: transparent;
}
.asset-panel:deep(.p-panel-content) {
	background: transparent;
}
.asset-panel:deep(.p-panel-footer) {
	background: transparent;
}
.asset-panel:hover {
	background: var(--surface-highlight);
}

.p-panel:deep(section) {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.p-panel:deep(.p-panel-icons) {
	display: flex;
	gap: 1rem;
	align-items: center;
}

header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

header > section {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.panel-content {
	display: flex;
	flex-direction: column;
}
</style>
