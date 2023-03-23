<template>
	<main>
		<!-- <table>
			<tr class="parameters_header">
				<th>Label</th>
				<th>Name</th>
				<th>Units</th>
				<th>Concept</th>
				<th>Definition</th>
			</tr>
		</table> -->
		<ul>
			<li v-for="(parameterRow, index) in parameters" :key="parameterRow.id">
				<model-parameter-list-item
					class="model-parameter-list-item"
					:parameter-row="parameterRow"
					:example-index="(index + 1).toString()"
					@update-parameter-row="updateParamaterRow"
					@click="variableClick($event, parameterRow.name)"
				/>
			</li>
			<footer>
				<Button label="Add parameter" icon="pi pi-plus" />
				<Button label="Extract from a dataset" icon="pi pi-file-export" />
			</footer>
		</ul>
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import ModelParameterListItem from '@/components/models/model-parameter-list-item.vue';

const props = defineProps<{
	parameters: any; // Temporary - this is also any in ITypeModel
	attribute: string;
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

table {
	margin: 0.5rem 1.5rem;
	text-transform: uppercase;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

table tr {
	display: flex;
	gap: 10rem;
}

table tr th {
	font-weight: normal;
	width: 20%;
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
