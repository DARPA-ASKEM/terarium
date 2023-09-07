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
				<!-- <section v-if="!cellEditStates[i].initials[j]" class="editable-cell"> -->
				<section :class="!cellEditStates[i].initials[j] ? 'editable-cell' : 'editable-cell-hidden'">
					<span>{{ initial.expression }}</span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="emit('open-value-modal', 'initials', 'expression', i, j)"
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
						if (!configuration?.metadata?.timeseries?.[parameter.id]) {
							emit('enter-value-cell', 'parameters', 'value', i, j);
						}
					}
				"
				@keyup.enter="
					() => {
						if (!configuration?.metadata?.timeseries?.[parameter.id]) {
							emit('enter-value-cell', 'parameters', 'value', i, j);
						}
					}
				"
			>
				<section
					:class="!cellEditStates[i].parameters[j] ? 'editable-cell' : 'editable-cell-hidden'"
				>
					<div class="distribution-cell">
						<!-- To represent a time series variable -->
						<span v-if="configuration?.metadata?.timeseries?.[parameter.id]">TS</span>
						<span v-else>{{ parameter.value }}</span>
						<span class="distribution-range" v-if="parameter.distribution"
							>Min: {{ parameter.distribution.parameters.minimum }} Max:
							{{ parameter.distribution.parameters.maximum }}</span
						>
					</div>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
						@click.stop="emit('open-value-modal', 'parameters', 'value', i, j)"
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
import { ModelConfiguration } from '@/types/Types';

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
	'open-value-modal',
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

.cell-menu {
	visibility: hidden;
}

.cell-input {
	height: 4rem;
	width: 100%;
	padding-left: 12px;
}

td:has(.cell-input) {
	padding: 0px !important;
}

.p-datatable:deep(td) {
	cursor: pointer;
}

.p-datatable:deep(td:focus) {
	background-color: var(--primary-color-lighter);
}

.p-frozen-column {
	left: 0px;
}

.second-frozen {
	left: 48px;
}

.p-datatable .p-datatable-tbody > tr > td {
	padding-right: 0.5rem;
	white-space: nowrap;
}

th:hover .cell-menu,
td:hover .cell-menu {
	visibility: visible;
}

.editable-cell-hidden .cell-menu {
	visibility: hidden !important;
}

.p-tabview {
	display: flex;
	gap: 1rem;
	margin-bottom: 1rem;
}

.p-tabview:deep(> *) {
	width: 50vw;
	overflow: auto;
}

.p-tabview:deep(.p-tabview-nav) {
	flex-direction: column;
}

.p-tabview:deep(label) {
	display: block;
	font-size: var(--font-caption);
	margin-bottom: 0.25rem;
	width: 20%;
}

.p-tabview:deep(.p-tabview-nav-container, .p-tabview-nav-content) {
	width: 20%;
}

.p-tabview:deep(.p-tabview-panels) {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	background-color: var(--surface-ground);
	width: 100%;
	height: 100%;
}

.p-tabview:deep(.p-tabview-panel) {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.p-tabview:deep(.p-tabview-nav li) {
	border-left: 3px solid transparent;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-header:nth-last-child(n + 3)) {
	border-bottom: 1px solid var(--surface-border-light);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight) {
	border-left: 3px solid var(--primary-color);
	background: var(--surface-highlight);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight .p-tabview-nav-link) {
	background: none;
}

.p-tabview:deep(.p-inputtext) {
	width: 100%;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-ink-bar) {
	display: none;
}

.distribution-cell {
	display: flex;
	flex-direction: column;
}

.distribution-range {
	white-space: nowrap;
	color: var(--text-color-subdued);
}

.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}

.capitalize {
	text-transform: capitalize !important;
	font-size: var(--font-body-medium) !important;
}

.modal-input-container {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
}

.modal-input {
	height: 25px;
	padding-left: 5px;
	margin: 5px;
	align-items: baseline;
}

.modal-input-label {
	margin-left: 5px;
	padding-top: 5px;
	padding-bottom: 5px;
	align-items: baseline;
}
</style>
