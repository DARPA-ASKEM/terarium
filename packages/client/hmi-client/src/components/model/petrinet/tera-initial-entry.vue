<template>
	<div>
		<header>
			<div>
				<strong>{{ initialId }}</strong>
				<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
				<template v-if="unit">
					<label class="ml-2">Unit</label>
					<span class="ml-1">{{ unit }}</span>
				</template>
			</div>
			<span v-if="description" class="ml-4">{{ description }}</span>
		</header>
		<main>
			<span class="expression">
				<tera-input
					label="Expression"
					:model-value="getInitialExpression(modelConfiguration, initialId)"
					@update:model-value="emit('update-expression', { id: initialId, value: $event })"
				/>
			</span>
			<Button
				:label="getSourceLabel(initialId)"
				text
				size="small"
				@click="sourceOpen = !sourceOpen"
			/>
			<Button :label="getOtherValuesLabel()" text size="small" @click="openModal(initialId)" />
		</main>
		<footer v-if="sourceOpen">
			<tera-input
				placeholder="Add a source"
				:model-value="getInitialSource(modelConfiguration, initialId)"
				@update:model-value="emit('update-source', { id: initialId, value: $event })"
			/>
		</footer>
	</div>
	<Teleport to="body">
		<tera-modal
			v-if="showOtherConfigValueModal"
			@modal-mask-clicked="showOtherConfigValueModal = false"
			class="about-modal"
		>
			<section style="width: 800px">
				<DataTable
					:value="tableData"
					@update:selection="onSelectionChange"
					dataKey="id"
					:rowsPerPageOptions="[10, 20, 50]"
					scrollable
					scrollHeight="45rem"
				>
					<template #header> </template>
					<Column selectionMode="single" headerStyle="width: 3rem"></Column>
					<Column
						v-for="(col, index) in selectedColumns"
						:field="col.field"
						:header="col.header"
						:sortable="col.field !== 'stats'"
						:key="index"
						:style="`width: ${getColumnWidth(col.field)}%`"
					>
						<template #body="{ data }">
							<template v-if="col.field === 'name'">
								{{ data.name }}
							</template>
							<template v-if="col.field === 'target'">
								{{ data.target }}
							</template>
							<template v-if="col.field === 'expression'">
								<span>Constants {{ data.expression }}</span>
							</template>
						</template>
					</Column>
					<ColumnGroup type="footer">
						<Row>
							<Column>
								<template #footer>
									<input
										type="checkbox"
										:checked="customSelection"
										:onchange="onCustomSelectionChange"
									/>
								</template>
							</Column>
							<Column :colspan="2">
								<template #footer>
									<tera-input placeholder="Add a source" :model-value="customSourceName" />
								</template>
							</Column>
							<Column>
								<template #footer>
									<p>
										<tera-input placeholder="Constant" :model-value="customConstant" />
										<tera-input placeholder="Min" :model-value="customMin" />
										<tera-input placeholder="Max" :model-value="customMax" />
									</p>
								</template>
							</Column>
						</Row>
					</ColumnGroup>
				</DataTable>
			</section>
			<template #footer> </template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { Model, ModelConfiguration } from '@/types/Types';
import { getInitialExpression, getInitialSource } from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import {
	getInitialDescription,
	getInitialName,
	getInitialUnits
} from '@/model-representation/service';
import TeraModal from '@/components/widgets/tera-modal.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
	initialId: string;
}>();

const tableData = ref();

const columns = ref([
	{ field: 'name', header: 'Configuration name' },
	{ field: 'target', header: 'Source' },
	{ field: 'expression', header: 'Value' }
]);

const emit = defineEmits(['update-expression', 'update-source']);

const name = getInitialName(props.model, props.initialId);
const unit = getInitialUnits(props.model, props.initialId);
const description = getInitialDescription(props.model, props.initialId);

const sourceOpen = ref(false);
const showOtherConfigValueModal = ref(false);
const customSourceName = ref();
const customConstant = ref();
const customMin = ref();
const customMax = ref();

const selectedColumns = ref(columns.value);
const customSelection = ref(false);
const selection = ref<null | string | object>(null);

const onSelectionChange = (val) => {
	selection.value = val;
	customSelection.value = false;
	console.log('selection', selection.value);
};

const onCustomSelectionChange = () => {
	customSelection.value = !customSelection.value;
	if (customSelection.value) {
		selection.value = 'custom';
	}
	console.log('selection', selection.value);
};

function getSourceLabel(initialId) {
	if (sourceOpen.value) return 'Hide source';
	if (!getInitialSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}

function getOtherValuesLabel() {
	const amountOfValues = '#';
	return `Other Values(${amountOfValues})`;
}

function openModal(initialId: string) {
	tableData.value = [];
	const modelConfigTableData = props.modelConfigurations.map((modelConfig) => ({
		name: modelConfig.name,
		initialSemanticList: modelConfig.initialSemanticList
	}));
	modelConfigTableData.forEach((modelConfig) => {
		const config = modelConfig.initialSemanticList.filter((item) => item.target === initialId)[0];
		if (config && modelConfig.name) {
			const data = { name: modelConfig.name, ...config };
			tableData.value.push(data);
		}
	});
	showOtherConfigValueModal.value = true;
}

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'name':
			return 40;
		case 'target':
			return 20;
		case 'expression':
			return 100;
		default:
			return 100;
	}
}
</script>

<style scoped>
header {
	display: flex;
	flex-direction: column;
	padding-bottom: var(--gap-small);
}
.expression {
	flex-grow: 1;
}
main {
	display: flex;
	justify-content: space-between;
	padding-bottom: var(--gap-small);
}

label {
	color: var(--text-color-subdued);
}
</style>
