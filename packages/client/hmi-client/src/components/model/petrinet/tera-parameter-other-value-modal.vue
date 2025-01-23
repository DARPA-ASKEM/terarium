<template>
	<tera-modal>
		<template #header>
			<h4>Other configuration values for '{{ otherValueList[0].referenceId }}'</h4>
		</template>
		<section>
			<DataTable
				class="value-border"
				:value="otherValueList"
				@update:selection="onCustomSelectionChange"
				dataKey="id"
				:rowsPerPageOptions="[10, 20, 50]"
				tableStyle="min-width: 90rem"
			>
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
						<template v-if="col.field === 'description'">
							{{ data.description }}
						</template>
						<template v-if="col.field === 'source'">
							{{ data.source }}
						</template>
						<tera-line-graphic
							v-if="col.field === 'range'"
							:max-value="maxValue"
							:min-value="minValue"
							:distribution="data.distribution"
						/>
						<template v-if="col.field === 'value'">
							<template v-if="data.distribution.type === DistributionType.Constant">
								<span class="value-label">Constants</span>
								<span class="value">{{ getValueString(data.distribution.parameters.value?.toString()) }}</span>
							</template>
						</template>
						<template v-if="col.field === 'minimum'">
							<template v-if="data.distribution.type === DistributionType.Uniform">
								<div>
									<span class="value-label">Min</span>
									<span class="value">{{ getValueString(data.distribution.parameters.minimum?.toString()) }}</span>
								</div>
							</template>
						</template>
						<template v-if="col.field === 'maximum'">
							<template v-if="data.distribution.type === DistributionType.Uniform">
								<div>
									<span class="value-label">Max</span>
									<span class="value">{{ getValueString(data.distribution.parameters.maximum?.toString()) }}</span>
								</div>
							</template>
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
								<tera-input-text
									placeholder="Add a source"
									v-model="customSource"
									@update:modelValue="onCustomSelectionChange"
									class="other-value-input"
								/>
							</template>
						</Column>
						<Column>
							<template #footer>
								<Dropdown v-model="numberType" :options="numberOptions" />
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[0]">
								<tera-input-number
									placeholder="Constant"
									v-model="customConstant"
									@update:modelValue="onCustomSelectionChange"
									class="other-value-input"
								/>
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[1]">
								<tera-input-number
									placeholder="Min"
									v-model="customMin"
									@update:modelValue="onCustomSelectionChange"
									class="mb-0"
								/>
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[1]">
								<tera-input-number
									placeholder="Max"
									v-model="customMax"
									@update:modelValue="onCustomSelectionChange"
									class="mb-0"
								/>
							</template>
						</Column>
					</Row>
				</ColumnGroup>
			</DataTable>
		</section>
		<template #footer>
			<Button label="Apply selected value" @click="applySelectedValue" :disabled="!selection" />
			<Button label="Cancel" severity="secondary" outlined @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { displayNumber } from '@/utils/number';
import { extent } from 'd3';
import { DistributionType } from '@/services/distribution';
import { SemanticOtherValues } from '@/services/model-configurations';
import Dropdown from 'primevue/dropdown';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import RadioButton from 'primevue/radiobutton';
import TeraLineGraphic from './tera-line-graphic.vue';

const props = defineProps<{
	id: any;
	otherValueList: SemanticOtherValues[];
}>();

const otherValueList = ref(props.otherValueList);
console.log(otherValueList);

const minValue: number = extent(otherValueList.value, (element) => element?.distribution?.parameters?.minimum)[0];
const maxValue: number = extent(otherValueList.value, (element) => element?.distribution?.parameters?.maximum)[1];

const columns = ref([
	{ field: 'name', header: 'Configuration name' },
	{ field: 'description', header: 'Description' },
	{ field: 'source', header: 'Source' },
	{ field: 'range', header: 'Range' },
	{ field: 'value', header: 'Value' },
	{ field: 'minimum', header: '' },
	{ field: 'maximum', header: '' }
]);

const emit = defineEmits(['update-parameter', 'update-source', 'close-modal']);

const customSource = ref('default');
const numberType = ref(DistributionType.Constant);
const customConstant = ref(0);
const customMin = ref(0);
const customMax = ref(1);

const numberOptions = [DistributionType.Constant, DistributionType.Uniform];

const selectedColumns = ref(columns.value);
const customSelection = ref(false);

interface OtherValueSelection {
	type: DistributionType;
	source?: string;
	constant?: number;
	min?: number;
	max?: number;
}

const selection = ref<null | OtherValueSelection>(null);

const onCustomSelectionChange = (val) => {
	if (customSelection.value && !val?.name) {
		selection.value =
			numberType.value === DistributionType.Constant
				? {
						constant: customConstant.value,
						source: customSource.value,
						type: DistributionType.Constant
					}
				: {
						min: customMin.value,
						max: customMax.value,
						source: customSource.value,
						type: DistributionType.Uniform
					};
	} else {
		selection.value =
			val.distribution.type === DistributionType.Constant
				? {
						constant: val.distribution.parameters.value,
						source: val.source,
						type: val.distribution.type
					}
				: {
						min: val.distribution.parameters.minimum,
						max: val.distribution.parameters.maximum,
						source: val.source,
						type: val.distribution.type
					};
	}
};

function getValueString(value) {
	return value ? displayNumber(value) : '';
}

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'name':
			return 25;
		case 'source':
			return 15;
		case 'range':
			return 20;
		case 'value':
			return 12;
		case 'minimum':
			return 12;
		case 'maximum':
			return 12;
		default:
			return 100;
	}
}

function applySelectedValue() {
	if (selection.value?.type === DistributionType.Constant) {
		emit('update-parameter', {
			id: props.id,
			distribution: { type: selection.value.type }
		});
		emit('update-parameter', {
			id: props.id,
			distribution: {
				parameters: { value: selection.value?.constant },
				type: selection.value.type
			}
		});
		emit('update-source', { id: props.id, value: selection.value?.source });
	} else if (selection.value?.type === DistributionType.Uniform) {
		emit('update-parameter', {
			id: props.id,
			distribution: { type: selection.value.type }
		});
		emit('update-parameter', {
			id: props.id,
			distribution: {
				parameters: { minimum: selection.value?.min, maximum: selection.value?.max },
				type: selection.value.type
			}
		});
		emit('update-source', { id: props.id, value: selection.value?.source });
	}
	emit('close-modal');
}
</script>

<style scoped>
.value-label {
	color: var(--surface-600);
	padding-right: var(--gap-4);
}
.value {
	color: var(--surface-900);
}

/* Change style for Primevue componment */
:deep(td[role='cell'] > div.p-dropdown) {
	height: 50px;
	width: 100%;
}

.other-value-input {
	margin-bottom: 0 !important;
}
</style>
