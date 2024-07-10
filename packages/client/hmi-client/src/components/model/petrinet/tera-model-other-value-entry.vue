<template>
	<tera-modal>
		<section style="width: 800px">
			<DataTable
				class="value-border"
				:value="tableData"
				@update:selection="onSelectionChange"
				dataKey="id"
				:rowsPerPageOptions="[10, 20, 50]"
				scrollable
				scrollHeight="45rem"
			>
				<template #header> </template>
				<Column selectionMode="single" headerStyle="width: 2rem"></Column>
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
							<section class="inline-flex gap-1">
								<div v-if="isConstant()">
									<span>Constants</span>
									<span>{{ data.expression }}</span>
								</div>
							</section>
						</template>
					</template>
				</Column>
				<ColumnGroup type="footer">
					<Row>
						<Column>
							<template #footer>
								<RadioButton
									v-model="customSelection"
									value="true"
									variant="filled"
									@change="onCustomSelectionChange"
								/>
							</template>
						</Column>
						<Column :colspan="2">
							<template #footer>
								<tera-input placeholder="Add a source" :model-value="customSource" />
							</template>
						</Column>
						<Column>
							<template #footer>
								<section v-if="isConstant()" class="inline-flex gap-1">
									<span class="pl-1 pr-1">Constant</span>
									<tera-input
										class=""
										placeholder="Constant"
										v-model="customConstant"
										@update:modelValue="onCustomSelectionChange"
									/>
								</section>
								<section v-else class="inline-flex gap-1">
									<Dropdown v-model="numberType" :options="numberOptions"></Dropdown>
									<template v-if="numberType === numberOptions[0]">
										<tera-input
											class=""
											placeholder="Constant"
											v-model="customConstant"
											@update:modelValue="onCustomSelectionChange"
										/>
									</template>
									<template v-if="numberType === numberOptions[1]">
										<tera-input
											class=""
											placeholder="Min"
											v-model="customMin"
											@update:modelValue="onCustomSelectionChange"
										/>
										<tera-input
											class=""
											placeholder="Max"
											v-model="customMax"
											@update:modelValue="onCustomSelectionChange"
										/>
									</template>
								</section>
							</template>
						</Column>
					</Row>
				</ColumnGroup>
			</DataTable>
		</section>
		<template #footer>
			<Button label="Apply selected value" @click="applySelectedValue" :disabled="!selection" />
			<Button label="Cancel" severity="secondary" raised @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { OtherValuesInputTypes, OtherValuesInput } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import TeraInput from '@/components/widgets/tera-input.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import RadioButton from 'primevue/radiobutton';

const props = defineProps<{
	id: string;
	tableData: any;
	otherValuesInputTypes: OtherValuesInputTypes;
}>();

const tableData = ref(props.tableData);

const columns = ref([
	{ field: 'name', header: 'Configuration name' },
	{ field: 'target', header: 'Source' },
	{ field: 'expression', header: 'Value' },
	{ field: 'max', header: '' },
	{ field: 'min', header: '' }
]);

const emit = defineEmits(['update-expression', 'update-source', 'close-modal']);

const isConstant = () => props.otherValuesInputTypes === OtherValuesInputTypes.constant;

const customSource = ref('default');
const numberType = ref('constant');
const customConstant = ref(0);
const customMin = ref(0);
const customMax = ref(1);

const numberOptions = ['constant', 'uniform'];

const selectedColumns = ref(columns.value);
const customSelection = ref(false);
const selection = ref<null | OtherValuesInput>(null);

const onSelectionChange = (val) => {
	console.log(val);
	console.log(val.expression);
	selection.value = { constant: val.expression, source: customSource.value };
	customSelection.value = false;
	console.log(selection.value);
};

const onCustomSelectionChange = () => {
	if (customSelection.value) {
		selection.value =
			numberType.value === numberOptions[0]
				? { constant: customConstant.value, source: customSource.value }
				: { constant: 0, source: '' };
	}
};

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

function applySelectedValue() {
	emit('update-expression', { id: props.id, value: selection.value?.constant });
	emit('update-source', { id: props.id, value: selection.value?.source });
	emit('close-modal');
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

.value-border > p-datatable-wrapper {
	border: 1px solid #000;
	border-radius: 8px;
}
</style>
