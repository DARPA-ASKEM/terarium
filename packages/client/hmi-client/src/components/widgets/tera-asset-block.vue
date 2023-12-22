<template>
	<Panel toggleable>
		<template #header>
			<section>
				<slot name="header" />
				<Button v-if="isEditable" icon="pi pi-pencil" text rounded />
			</section>
		</template>
		<template #icons>
			<label>Include in process</label>
			<InputSwitch
				:model-value="isIncluded"
				@update:model-value="emit('update:is-included')"
			/>

			<Button v-if="isDeletable" icon="pi pi-trash" text rounded @click="emit('delete')" />
		</template>
		<template #togglericon="{ collapsed }">
			<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
		</template>
		<slot />
	</Panel>
</template>

<script setup lang="ts">
import Panel from 'primevue/panel';
import Button from 'primevue/button';
import InputSwitch from 'primevue/inputswitch';

const emit = defineEmits(['delete', 'edit', 'update:is-included']);

defineProps<{
	isDeletable?: boolean;
	isIncluded?: boolean;
	isEditable?: boolean;
}>();
</script>

<style scoped>
.p-panel {
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
</style>
