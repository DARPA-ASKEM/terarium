<template>
	<tbody class="p-datatable-tbody">
		<tr v-for="({ configuration, name }, i) in modelConfigurations" :key="i">
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
			<td
				v-for="(initial, j) of configuration?.semantics?.ode.initials"
				:key="j"
				tabindex="0"
				@click="emit('enter-value-cell', 'initials', 'expression', i, j)"
				@keyup.enter="emit('enter-value-cell', 'initials', 'expression', i, j)"
			>
				<section :class="!cellEditStates[i].initials[j] ? 'editable-cell' : 'editable-cell-hidden'">
					<span>{{ initial.expression }}</span>
					<Button
						class="cell-modal-button p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						icon="pi pi-ellipsis-v"
						@click.stop="emit('open-modal', 'initials', 'expression', i, j)"
					/>
				</section>
				<InputText
					v-if="cellEditStates[i].initials[j]"
					:value="editValue"
					@input="emit('update:editValue', ($event.target as HTMLInputElement).value)"
					v-focus
					@focusout="emit('update-value', 'initials', 'expression', i, j)"
					@keyup.stop.enter="emit('update-value', 'initials', 'expression', i, j)"
					class="cell-input"
				/>
			</td>
			<td
				v-for="(parameter, j) of configuration?.semantics?.ode.parameters"
				:key="j"
				tabindex="0"
				@click="
					() => {
						emit('enter-value-cell', 'parameters', 'value', i, j);
					}
				"
				@keyup.enter="
					() => {
						emit('enter-value-cell', 'parameters', 'value', i, j);
					}
				"
			>
				<section
					:class="!cellEditStates[i].parameters[j] ? 'editable-cell' : 'editable-cell-hidden'"
				>
					<div class="distribution-cell">
						<span>{{ parameter.value }}</span>
						<span class="distribution-range" v-if="parameter.distribution"
							>Min: {{ parameter.distribution.parameters.minimum }} Max:
							{{ parameter.distribution.parameters.maximum }}</span
						>
					</div>
					<Button
						class="cell-modal-button p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						icon="pi pi-ellipsis-v"
						@click.stop="emit('open-modal', 'parameters', 'value', i, j)"
					/>
				</section>
				<InputText
					v-if="cellEditStates[i].parameters[j]"
					:value="editValue"
					@input="emit('update:editValue', ($event.target as HTMLInputElement).value)"
					v-focus
					@focusout="emit('update-value', 'parameters', 'value', i, j)"
					@keyup.stop.enter="emit('update-value', 'parameters', 'value', i, j)"
					class="cell-input"
				/>
			</td>
		</tr>
	</tbody>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import type { ModelConfiguration } from '@/types/Types';

defineProps<{
	editValue: string;
	modelConfigurations: ModelConfiguration[];
	calibrationConfig?: boolean;
	cellEditStates: any[];
}>();

const emit = defineEmits([
	'new-model-configuration',
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
	visibility: visible;
	width: 100%;
}

.editable-cell-hidden {
	display: flex;
	justify-content: space-between;
	align-items: center;
	visibility: hidden;
	width: 100%;
	height: 0px;
	border-left: 12px solid transparent;
	border-right: 11px solid transparent;
}

.p-datatable .p-datatable-tbody > tr > td {
	padding-right: 0.5rem;
	white-space: nowrap;
}

.editable-cell-hidden {
	visibility: hidden !important;
}

.distribution-cell {
	display: flex;
	flex-direction: column;
}

.distribution-range {
	white-space: nowrap;
	color: var(--text-color-subdued);
}
</style>
