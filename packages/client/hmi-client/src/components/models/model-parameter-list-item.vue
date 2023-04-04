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
							@click.stop
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
							@click.stop
						/>
						<span v-else>{{ parameterRow.name }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="editedParameterRow.units"
							type="text"
							class="p-inputtext-sm"
							@click.stop
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
				</tr>
			</table>
			<aside>
				<template v-if="!isEditing">
					<Chip label="extractions" @click.stop="showExtractions = !showExtractions" />
					<!-- <Button :icon="showRange ? 'pi pi-eye' : 'pi pi-eye-slash'"
						class="p-button-icon-only p-button-text p-button-rounded" @click="showRange = !showRange" /> -->
					<Button
						icon="pi pi-ellipsis-v"
						class="p-button-icon-only p-button-text p-button-rounded"
						@click.stop="toggleContextMenu"
					/>
					<Menu ref="contextMenu" :model="parameterMenuItems" :popup="true" />
				</template>
				<template v-else>
					<span class="apply-edits">
						<Button
							icon="pi pi-times"
							class="p-button-icon-only p-button-text p-button-rounded"
							@click.stop="isEditing = false"
						/>
						<span class="divider">|</span>
						<Button
							icon="pi pi-check"
							class="p-button-icon-only p-button-text p-button-rounded"
							@click.stop="applyParameterEdits"
						/>
					</span>
				</template>
			</aside>
		</section>
		<section v-if="showExtractions">
			<ul class="extractions">
				<template v-for="(value, key) in example[exampleIndex]">
					<!--temporary lazy filtering-->
					<template
						v-if="
							key.toString() !== 'id' &&
							key.toString() !== 'name' &&
							key.toString() !== 'type' &&
							key.toString() !== 'text_annotations'
						"
					>
						<li :key="key">
							<span class="extraction-type">
								{{ key.toString().split('_')[0] }}
							</span>
							<ul class="extraction-values">
								<li v-for="(ex, index) in [value].flat()" :key="index">
									<a v-if="key.toString() === 'doi'" :href="ex">{{ ex }}</a>
									<a
										v-else-if="key.toString() === 'dkg_annotations'"
										:href="`http://34.230.33.149:8772/${ex[0]}`"
										>{{ ex[1] }}</a
									>
									<template v-else-if="key.toString() === 'data_annotations'">
										{{ ex[0] }}: {{ ex[1] }}
									</template>
									<vue-mathjax
										v-else-if="key.toString() === 'equation_annotations'"
										:formula="String.raw`$$${Object.keys(ex)[0]}$$`"
									/>
									<template v-else>
										{{ ex }}
									</template>
								</li>
							</ul>
						</li>
					</template>
				</template>
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
const showExtractions = ref(false);
const contextMenu = ref();
const editedParameterRow = ref({ ...props.parameterRow });

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
	overflow: hidden;
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
	min-width: 13rem;
}

table span {
	display: block;
	/* color: var(--primary-color); */
}

table td span:empty:before,
table td ul:empty:before {
	content: '--';
}

table ul {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

table a {
	color: var(--text-color-primary);
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
