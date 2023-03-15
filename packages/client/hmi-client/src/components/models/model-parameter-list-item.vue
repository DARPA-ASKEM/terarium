<template>
	<section>
		<i class="pi pi-sort-alt" />
		<table>
			<tr>
				<td>
					<InputText
						v-if="isEditing"
						v-model="paramRowRef.label"
						type="text"
						class="p-inputtext-sm"
					/>
					<span v-else>{{ paramRow.label ?? '--' }}</span>
				</td>
				<td>{{ paramRow.name ?? '--' }}</td>
				<td>{{ paramRow.units ?? '--' }}</td>
				<td>{{ paramRow.concept ?? '--' }}</td>
				<td>{{ paramRow.definition ?? '--' }}</td>
			</tr>
		</table>
		<Chip label="extractions" />
		<Button icon="pi pi-eye" class="p-button-icon-only p-button-text p-button-rounded" />
		<Button
			icon="pi pi-ellipsis-v"
			class="p-button-icon-only p-button-text p-button-rounded"
			@click="toggleEditMode"
		/>
	</section>
	<section class="tile-container"></section>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Chip from 'primevue/chip';

const props = defineProps<{
	paramRow: { label: string; name: string; units: string; concept: string; definition: string };
}>();

const isEditing = ref(false);
const paramRowRef = ref(props.paramRow);

// function onRowEditSave() {
//     console.log(0);
// }

function toggleEditMode() {
	isEditing.value = !isEditing.value;
}
</script>
<style scoped>
table {
	width: 100%;
}

table th {
	text-align: left;
}

table:has(.parameters_header) {
	margin-left: 3rem;
	width: 80%;
}

section {
	display: flex;
	gap: 0.5rem;
	margin: 0 0 0.5rem 0;
}

.tile-container {
	height: 6rem;
	background-color: var(--surface-ground);
	border: 1px solid var(--surface-border);
	border-radius: 0.75rem;
	margin: 0 0.5rem;
}
</style>
