<template>
	<main>
		<table>
			<tr>
				<th>Label</th>
				<th>Name</th>
				<th>Units</th>
				<th>Concepts/Definition</th>
			</tr>
		</table>
		<ul>
			<li v-for="(parameterRow, index) in parameters" :key="parameterRow.id">
				<model-parameter-list-item
					class="model-parameter"
					:active="parameterRow.label === selectedVariable"
					:parameter-row="parameterRow"
					:example-index="(index + 1).toString()"
					@update-parameter-row="updateParamaterRow"
					@click="variableClick($event, parameterRow.label)"
				/>
			</li>
			<footer>
				<Button label="Add variable" icon="pi pi-plus" />
				<Button label="Extract from a dataset" icon="pi pi-file-export" />
			</footer>
		</ul>
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import ModelParameterListItem from '@/components/models/tera-model-parameter-list-item.vue';

const props = defineProps<{
	parameters: any; // Temporary - this is also any in ITypeModel
	attribute: string;
	selectedVariable: string;
}>();

const emit = defineEmits(['update-parameter-row', 'parameter-click']);

const variableClick = (_event: Event, variable: any) => {
	emit('parameter-click', variable);
};

function updateParamaterRow(newParameterRow: any) {
	emit('update-parameter-row', props.attribute, newParameterRow);
}
</script>

<style scoped>
ul {
	background-color: var(--surface-ground);
	border-radius: 0.75rem;
	padding: 0.75rem;
}

.model-parameter[active='true'] {
	background-color: var(--surface-highlight);
}

table {
	width: 80%;
	margin: 0.5rem 1.5rem;
	text-transform: uppercase;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

table tr {
	display: flex;
}

table tr th {
	min-width: 13rem;
	text-align: left;
	font-weight: normal;
}

.over {
	border: 1px dotted #666;
}

footer {
	display: flex;
	gap: 1rem;
	margin-left: 1rem;
}

footer .p-button {
	color: var(--primary-color);
	background-color: transparent;
}

footer .p-button:hover,
footer .p-button:focus {
	color: var(--primary-color);
	background-color: var(--surface-highlight);
}
</style>
