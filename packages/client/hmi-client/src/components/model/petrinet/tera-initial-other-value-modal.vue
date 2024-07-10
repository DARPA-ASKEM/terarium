<template>
	<tera-modal>
		<section style="width: 800px">
			<DataTable
				:value="otherValueList"
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
								<span>Constants</span>
								<span class="pl-1">{{ data.expression }}</span>
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
								<tera-input
									placeholder="Add a source"
									v-model="customSource"
									@update:modelValue="onCustomSelectionChange"
								/>
							</template>
						</Column>
						<Column>
							<template #footer>
								<section class="inline-flex gap-1">
									<span class="pl-1 pr-1">Constant</span>
									<tera-input
										placeholder="Constant"
										v-model="customConstant"
										@update:modelValue="onCustomSelectionChange"
									/>
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
import TeraInput from '@/components/widgets/tera-input.vue';
import { DistributionType } from '@/services/distribution';
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
	otherValueList: any[];
	otherValuesInputTypes: DistributionType;
}>();

const otherValueList = ref(props.otherValueList);
const columns = ref([
	{ field: 'name', header: 'Configuration name' },
	{ field: 'target', header: 'Source' },
	{ field: 'expression', header: 'Value' }
]);

const emit = defineEmits(['update-expression', 'update-source', 'close-modal']);

const customSource = ref('default');
const numberType = ref(DistributionType.Constant);
const customConstant = ref(0);

const numberOptions = [DistributionType.Constant, DistributionType.Uniform];

const selectedColumns = ref(columns.value);
const customSelection = ref(false);
const selection = ref<null | { id?: string; source?: string; constant?: number }>(null);

const onSelectionChange = (val) => {
	selection.value = { constant: val.expression, source: customSource.value };
	customSelection.value = false;
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
</style>
