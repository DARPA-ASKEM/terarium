<template>
	<tera-modal>
		<template #header>
			<h4>Other configuration values for '{{ otherValueList[0].target }}'</h4>
		</template>
		<DataTable
			:value="otherValueList"
			@update:selection="onCustomSelectionChange"
			dataKey="id"
			:rowsPerPageOptions="[10, 20, 50]"
			tableStyle="min-width: 55rem"
		>
			<template #header> </template>
			<Column headerStyle="width: 2rem">
				<template #body="{ data }">
					<RadioButton
						v-model="customSelection"
						:inputId="data.id"
						:value="data"
						variant="filled"
						@change="onCustomSelectionChange(data)"
					/>
				</template>
			</Column>
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
					<template v-if="col.field === 'source'">
						{{ data.source }}
					</template>
					<template v-if="col.field === 'expression'">
						<section class="inline-flex gap-1">
							<span class="value-label">Constants</span>
							<span class="value">{{ numberToNist(data.expression) }}</span>
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
								inputId="custom"
								:value="{}"
								variant="filled"
								@change="onCustomSelectionChange('custom')"
							/>
						</template>
					</Column>
					<Column :colspan="2">
						<template #footer>
							<tera-input-text
								placeholder="Add a source"
								v-model="customSource"
								@update:modelValue="onCustomSelectionChange"
								class="mb-0"
							/>
						</template>
					</Column>
					<Column>
						<template #footer>
							<section class="inline-flex gap-1">
								<span class="custom-input-label">Constant</span>
								<tera-input-number
									class="mb-0"
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
		<template #footer>
			<Button label="Apply selected value" @click="applySelectedValue" :disabled="!selection" />
			<Button label="Cancel" severity="secondary" outlined @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
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
	{ field: 'source', header: 'Source' },
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

const onCustomSelectionChange = (val) => {
	if (customSelection.value && !val?.name) {
		selection.value =
			numberType.value === numberOptions[0]
				? { constant: customConstant.value, source: customSource.value }
				: { constant: 0, source: '' };
	} else {
		selection.value = { constant: val.expression, source: val.source };
	}
};

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'name':
			return 40;
		case 'source':
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
.value-label {
	color: var(--surface-600);
	padding-right: 1rem;
}
.value {
	color: var(--surface-900);
}

.custom-input-label {
	display: flex;
	justify-content: left;
	align-items: center;
	padding-right: 1rem;
}

.custom-input {
	height: 100%;
}

:deep(input) {
	margin-top: 1rem;
}
</style>
