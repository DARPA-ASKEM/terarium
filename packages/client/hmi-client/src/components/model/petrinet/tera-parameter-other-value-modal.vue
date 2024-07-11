<template>
	<tera-modal>
		<section>
			<DataTable
				class="value-border"
				:value="otherValueList"
				@update:selection="onCustomSelectionChange"
				dataKey="id"
				:rowsPerPageOptions="[10, 20, 50]"
				tableStyle="min-width: 65rem"
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
						<template v-if="col.field === 'target'">
							{{ data.referenceId }}
						</template>
						<template v-if="col.field === 'value'">
							<section class="inline-flex gap-1">
								<template v-if="data.distribution.type === DistributionType.Constant">
									<span>Constants</span>
									<span class="pl-1">{{
										displayNumber(data.distribution.parameters.value.toString())
									}}</span>
								</template>
							</section>
						</template>
						<template v-if="col.field === 'minimum'">
							<template v-if="data.distribution.type === DistributionType.Uniform">
								<div>
									<span>Min</span>
									<span class="pl-1 pr-1">{{
										displayNumber(data.distribution.parameters.minimum.toString())
									}}</span>
								</div>
							</template>
						</template>
						<template v-if="col.field === 'maximum'">
							<template v-if="data.distribution.type === DistributionType.Uniform">
								<div>
									<span>Max</span>
									<span class="pl-1 pr-1">{{
										displayNumber(data.distribution.parameters.maximum.toString())
									}}</span>
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
						<Column>
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
								<Dropdown class="input-field" v-model="numberType" :options="numberOptions" />
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[0]">
								<tera-input
									type="nist"
									placeholder="Constant"
									v-model="customConstant"
									@update:modelValue="onCustomSelectionChange"
								/>
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[1]">
								<tera-input
									type="nist"
									placeholder="Min"
									v-model="customMin"
									@update:modelValue="onCustomSelectionChange"
								/>
							</template>
						</Column>
						<Column>
							<template #footer v-if="numberType === numberOptions[1]">
								<tera-input
									type="nist"
									placeholder="Max"
									v-model="customMax"
									@update:modelValue="onCustomSelectionChange"
								/>
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
import { displayNumber } from '@/utils/number';
import { DistributionType } from '@/services/distribution';
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
	otherValueList: any;
}>();

const otherValueList = ref(props.otherValueList);

const columns = ref([
	{ field: 'name', header: 'Configuration name' },
	{ field: 'target', header: 'Source' },
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
			val.type === DistributionType.Constant
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

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'name':
			return 40;
		case 'target':
			return 5;
		case 'value':
			return 25;
		case 'minimum':
			return 15;
		case 'maximum':
			return 15;
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
.input-field {
	height: 40px;
}
</style>
