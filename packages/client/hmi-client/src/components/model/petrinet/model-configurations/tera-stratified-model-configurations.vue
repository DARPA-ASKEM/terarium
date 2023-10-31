<template>
	<tbody class="p-datatable-tbody">
		<tr v-for="({ name }, i) in modelConfigurations" :key="i">
			<!--TODO: This td is a placeholder, row selection doesn't work-->
			<td class="p-selection-column p-frozen-column">
				<div class="p-checkbox p-component">
					<div class="p-hidden-accessible">
						<input type="checkbox" tabindex="0" aria-label="Row Unselected" />
					</div>
					<div class="p-checkbox-box p-component">
						<span class="p-checkbox-icon"></span>
					</div>
				</div>
			</td>
			<td
				class="p-frozen-column second-frozen"
				tabindex="0"
				@keyup.enter="emit('enter-name-cell', i)"
				@click="emit('enter-name-cell', i)"
			>
				<InputText
					v-if="cellEditStates[i].name"
					:value="editValue"
					@input="emit('update:editValue', ($event.target as HTMLInputElement).value)"
					v-focus
					@focusout="emit('update-name', i)"
					@keyup.stop.enter="emit('update-name', i)"
					class="cell-input"
				/>
				<span v-else class="editable-cell">
					{{ name }}
				</span>
			</td>
			<td v-for="(id, j) in baseInitialsAndParameters" :key="j">
				<section class="editable-cell" @click="emit('open-modal', i, id)">
					<span>{{ id }}<i class="pi pi-table" /></span>
					<Button
						class="cell-modal-button p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						icon="pi pi-ellipsis-v"
					/>
				</section>
			</td>
		</tr>
	</tbody>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { ModelConfiguration } from '@/types/Types';

defineProps<{
	editValue: string;
	modelConfigurations: ModelConfiguration[];
	calibrationConfig?: boolean;
	cellEditStates: any[];
	baseInitialsAndParameters: any;
}>();

const emit = defineEmits([
	'update-name',
	'update-value',
	'enter-name-cell',
	'enter-value-cell',
	'open-modal',
	'update:editValue'
]);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};
</script>

<style scoped>
.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
}

.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}
</style>
