<template>
	<Panel :toggleable="isToggleable" :class="{ 'asset-panel': useDefaultStyle }">
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

		<template v-if="$slots.footer" #footer>
			<slot name="footer" />
		</template>
	</Panel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel';
import Button from 'primevue/button';

const emit = defineEmits(['delete', 'edit', 'update:is-included']);

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
	useDefaultStyle: {
		type: Boolean,
		default: true
	}
});
</script>

<style scoped>
.asset-panel {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
	border-left: 0.5rem solid var(--primary-color);
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
