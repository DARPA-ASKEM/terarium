<template>
	<tera-modal>
		<template #header>
			<h4>Other configuration values for '{{ otherValueList[0].target }}'</h4>
		</template>
		<DataTable
			dataKey="id"
			tableStyle="min-width: 55rem"
			:rowsPerPageOptions="[10, 20, 50]"
			:value="otherValueList"
			@update:selection="onCustomSelectionChange"
		>
			<template #header> </template>
			<Column>
				<template #body="{ data }">
					<RadioButton
						v-model="customSelection"
						variant="filled"
						:inputId="data.id"
						:value="data"
						@change="onCustomSelectionChange(data)"
					/>
				</template>
			</Column>
			<Column sortable>
				<template #header>Configuration name</template>
				<template #body="{ data }">{{ data.name }}</template>
			</Column>
			<Column>
				<template #header>Description</template>
				<template #body="{ data }">{{ data.description }}</template>
			</Column>
			<Column sortable>
				<template #header>Source</template>
				<template #body="{ data }">{{ data.source }}</template>
			</Column>
			<Column sortable>
				<template #header>Value</template>
				<template #body="{ data }">
					<span class="value-label">Constants</span>
					{{ numberToNist(data.expression) || data.expression }}
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
			<Button label="Apply selected value" :disabled="!selection" @click="applySelectedValue" />
			<Button label="Cancel" severity="secondary" outlined @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import ColumnGroup from 'primevue/columngroup';
import RadioButton from 'primevue/radiobutton';
import Row from 'primevue/row';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { numberToNist } from '@/utils/number';
import { DistributionType } from '@/services/distribution';

const props = defineProps<{
	id: string;
	otherValueList: any[];
	otherValuesInputTypes: DistributionType;
}>();

const otherValueList = ref(props.otherValueList);

const emit = defineEmits(['update-expression', 'update-source', 'close-modal']);

const customSource = ref('default');
const numberType = ref(DistributionType.Constant);
const customConstant = ref(0);

const numberOptions = [DistributionType.Constant, DistributionType.Uniform];

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

function applySelectedValue() {
	emit('update-expression', { id: props.id, value: selection.value?.constant });
	emit('update-source', { id: props.id, value: selection.value?.source });
	emit('close-modal');
}
</script>

<style scoped>
/* Override PrimeVue styles */
:deep(.p-datatable-table) th {
	padding: var(--gap-4);
	width: 100%;
}
:deep(.p-datatable-table) th:nth-of-type(1) {
	width: 2rem;
}
:deep(.p-datatable-table) th:nth-of-type(2) {
	width: 40%;
}
:deep(.p-datatable-table) th:nth-of-type(3) {
	width: 20%;
}

.value-label {
	color: var(--text-color-subdued);
	padding-right: var(--gap-4);
}

.custom-input-label {
	align-items: center;
	display: flex;
	justify-content: left;
	padding-right: var(--gap-4);
}

.custom-input {
	height: 100%;
}
</style>
