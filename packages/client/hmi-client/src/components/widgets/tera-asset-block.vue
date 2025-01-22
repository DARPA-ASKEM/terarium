<template>
	<Panel :toggleable="isToggleable" :class="{ 'asset-panel': useDefaultStyle }">
		<template #header>
			<section>
				<slot name="header" />
				<Button v-if="isEditable" icon="pi pi-pencil" text rounded @click="emit('edit')" />
			</section>
		</template>
		<template #icons>
			<template v-if="isPermitted">
				<label>Include in process</label>
				<InputSwitch :model-value="isIncluded" @update:model-value="emit('update:is-included')" />
			</template>

			<Button v-if="isDeletable" icon="pi pi-trash" text rounded @click="emit('delete')" />
		</template>
		<template #togglericon="{ collapsed }">
			<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
		</template>
		<main class="panel-content">
			<slot />
		</main>

		<template #footer>
			<slot name="footer" />
		</template>
	</Panel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel';
import Button from 'primevue/button';
import InputSwitch from 'primevue/inputswitch';

const emit = defineEmits(['delete', 'edit', 'update:is-included']);

defineProps({
	isDeletable: {
		type: Boolean
	},
	isPermitted: {
		type: Boolean,
		default: true
	},
	isIncluded: {
		type: Boolean
	},
	isEditable: {
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
	border-radius: var(--border-radius);
	border-left: 4px solid var(--surface-400);
	background: var(--surface-0);
	cursor: pointer;
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
:deep(.p-panel-header) {
	background: transparent;
}
:deep(.p-panel-content) {
	background: transparent;
}
</style>
