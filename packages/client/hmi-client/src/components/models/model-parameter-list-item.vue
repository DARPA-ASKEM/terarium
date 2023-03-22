<template>
	<main>
		<section>
			<table>
				<tr>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.label"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ parameterRow.label }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.name"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ parameterRow.name }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.units"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ parameterRow.units }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.concept"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ parameterRow.concept }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.definition"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ parameterRow.definition }}</span>
					</td>
				</tr>
			</table>
			<template v-if="!isEditing">
				<Chip
					ref="extractionChip"
					v-tooltip.top="{
						value: tooltipContent?.outerHTML ?? ``,
						escape: true
					}"
					label="extractions"
				/>
				<Button
					:icon="showRange ? 'pi pi-eye' : 'pi pi-eye-slash'"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="showRange = !showRange"
				/>
				<Menu ref="contextMenu" :model="parameterMenuItems" :popup="true" />
			</template>
			<template v-else>
				<Button
					icon="pi pi-times"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="isEditing = false"
				/>
				<Button
					icon="pi pi-check"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="applyParameterEdits"
				/>
			</template>
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleContextMenu"
			/>
		</section>
		<section>
			<section v-if="showRange" class="range"></section>
			<section class="tile-container"></section>
		</section>
		<section style="display: none">
			<ul ref="tooltipContent">
				<li v-for="(value, key) in example[exampleIndex]" :key="key">
					<span class="extraction-type">
						{{ startCase(key.toString()) }}
					</span>
					<ul class="extraction-values">
						<li v-for="(text, index) in [value].flat()" :key="index">
							{{ text }}
						</li>
					</ul>
				</li>
			</ul>
		</section>
	</main>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { startCase } from 'lodash';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import InputText from 'primevue/inputtext';
import Chip from 'primevue/chip';
import { example } from './example-model-extraction';

const props = defineProps<{
	parameterRow: {
		id: number | string;
		label: string;
		name: string;
		units: string;
		concept: string;
		definition: string;
	};
	exampleIndex: string;
}>();

const emit = defineEmits(['update-parameter-row']);

const isEditing = ref(false);
const showRange = ref(false);
const contextMenu = ref();
const editedParameterRow = ref({ ...props.parameterRow });
const tooltipContent = ref();

const parameterMenuItems = [
	{
		label: 'Edit',
		command: () => {
			editedParameterRow.value = { ...props.parameterRow };
			isEditing.value = true;
		}
	}
];

function toggleContextMenu(event) {
	contextMenu.value.toggle(event);
}

function applyParameterEdits() {
	emit('update-parameter-row', editedParameterRow.value);
	isEditing.value = false;
}
</script>
<style scoped>
main {
	background-color: var(--surface-section);
	border-radius: 0.5rem;
	padding: 0.5rem;
}

section {
	display: flex;
	align-items: center;
	min-height: 2.25rem;
	gap: 0.5rem;
	margin: 0 0 0.5rem 0;
}

.grab {
	cursor: grab;
}

.grab:active {
	cursor: grabbing;
}

table {
	width: 100%;
	margin-left: 0.5rem;
}

table tr {
	display: flex;
}

table tr td {
	width: 20%;
}

table td span:empty:before {
	content: '--';
}

.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
}

.range {
	width: 6rem;
}

.extraction-type {
	color: var(--text-color-subdued);
	text-transform: uppercase;
	width: 13rem;
	text-align: right;
}

ul {
	color: var(--text-color-primary);
	white-space: nowrap;
}

li,
.extraction-values {
	display: flex;
	gap: 1rem;
}

.extraction-values li:not(:last-child):after {
	content: '|';
	color: var(--text-color-light);
}

.tile-container {
	width: 100%;
	height: 6rem;
	background-color: var(--surface-ground);
	border: 1px solid var(--surface-border);
	border-radius: 0.75rem;
	margin: 0 0.5rem;
}
</style>
