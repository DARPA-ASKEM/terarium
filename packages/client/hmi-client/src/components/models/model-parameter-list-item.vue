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
						<span
							:class="editedParameterRow.state_variable ? 'label state' : 'label transition'"
							v-else
							>{{ parameterRow.label }}</span
						>
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
						<ul>
							<li v-for="(ontology, index) in editedParameterRow.concepts" :key="index">
								<!-- <InputText v-if="isEditing" v-model="editedParameterRow.concepts" type="text"
									class="p-inputtext-sm" /> -->
								<a :href="ontology?.link">{{ ontology?.title }}</a>
								<br />{{ ontology?.description }}
							</li>
						</ul>
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
			<aside>
				<template v-if="!isEditing">
					<Chip
						ref="extractionChip"
						v-tooltip="{
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
					<span class="apply-edits">
						<Button
							icon="pi pi-times"
							class="p-button-icon-only p-button-text p-button-rounded"
							@click="isEditing = false"
						/>
						<span class="divider">|</span>
						<Button
							icon="pi pi-check"
							class="p-button-icon-only p-button-text p-button-rounded"
							@click="applyParameterEdits"
						/>
					</span>
				</template>
				<Button
					v-if="!isEditing"
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleContextMenu"
				/>
			</aside>
		</section>
		<!-- <section> Move this to its own component
			<section v-if="showRange" class="range"></section>
			<ul>
				<li v-for="(value, key) in parameterRow" :key="key">
					<span class="extraction-type">
						{{ startCase(key.toString()) }}
					</span>
					<ul class="extraction-values">
						<li>
							{{ value.toString() }}
						</li>
					</ul>
				</li>
			</ul>
			<section class="tile-container">
			</section>
		</section>-->
		<section style="display: none">
			<ul class="extractions" ref="tooltipContent">
				<li v-for="(value, key) in example[exampleIndex]" :key="key">
					<span class="extraction-type">
						{{ key.toString().split('_')[0] }}
					</span>
					<ul class="extraction-values">
						<li v-for="(ex, index) in [value].flat()" :key="index">
							<template v-if="key.toString() === 'doi'">
								<a :href="ex">{{ ex }}</a>
							</template>
							<template v-else-if="key.toString() === 'dkg_annotations'">
								<a :href="`http://34.230.33.149:8772/${ex[0]}`">{{ ex[1] }}</a>
							</template>
							<template v-else-if="key.toString() === 'data_annotations'">
								{{ ex[0] }}: {{ ex[1] }}
							</template>
							<template v-else-if="key.toString() === 'equation_annotations'">
								<!--
									It seems this refuses to format within the tooltip.
									In fact once I go to the edit mode and then back it its formatted correctly...maybe I need to wait for something
								-->
								<vue-mathjax :formula="String.raw`$$${Object.keys(ex)[0]}$$`" />
							</template>
							<template v-else>
								{{ ex }}
							</template>
						</li>
					</ul>
				</li>
			</ul>
		</section>
	</main>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import InputText from 'primevue/inputtext';
import Chip from 'primevue/chip';
import { example } from './example-model-extraction';

const props = defineProps<{
	parameterRow: {
		id: number | string;
		state_variable: boolean;
		label: string;
		name: string;
		units: string;
		concepts: any;
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
		icon: 'pi pi-fw pi-pencil',
		command: () => {
			editedParameterRow.value = { ...props.parameterRow };
			isEditing.value = true;
		}
	},
	{
		label: 'Duplicate',
		icon: 'pi pi-fw pi-copy'
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash'
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

.label {
	border: 1px solid var(--surface-border);
	height: 2rem;
	width: 2rem;
	display: flex;
	justify-content: center;
	align-items: center;
	/* overflow: hidden; */
}

.p-button:deep(.pi-check) {
	color: var(--primary-color);
}

.state {
	border-radius: 3rem;
}

.transition {
	border-radius: var(--border-radius);
}

section {
	display: flex;
	min-height: 2.25rem;
	gap: 0.25rem;
}

.grab {
	cursor: grab;
}

.grab:active {
	cursor: grabbing;
}

table {
	width: 100%;
}

table tr {
	display: flex;
}

table tr td {
	min-width: 10%;
}

table span {
	display: block;
	color: var(--primary-color);
}

table td span:empty:before,
table td ul:empty:before {
	content: '--';
}

.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
}

.range {
	width: 6rem;
}

aside {
	display: flex;
	justify-content: center;
}

.apply-edits {
	display: flex;
	height: 2rem;
	align-items: center;
}

.apply-edits .divider {
	color: var(--text-color-light);
}

.apply-edits .p-button {
	margin: 0 0.5rem;
}

.extractions {
	color: var(--text-color-primary);
	white-space: nowrap;
	font-size: var(--font-caption);
	padding: 0 0.5rem;
}

.extraction-type {
	color: var(--text-color-subdued);
	text-transform: uppercase;
	min-width: 4rem;
	text-align: right;
}

.extractions li {
	display: flex;
	gap: 1rem;
	margin-bottom: 0.25rem;
}

.p-chip {
	cursor: pointer;
	height: fit-content;
	margin-top: 0.2rem;
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
