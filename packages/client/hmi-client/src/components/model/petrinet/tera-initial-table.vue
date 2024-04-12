<template>
	<Datatable
		:value="data ?? tableFormattedInitials"
		v-model:expanded-rows="expandedRows"
		dataKey="id"
		:row-class="rowClass"
		size="small"
		:class="{ 'hide-header': hideHeader }"
		edit-mode="cell"
		@cell-edit-complete="conceptSearchTerm = { curie: '', name: '' }"
	>
		<!-- Row expander, ID and Name columns -->
		<Column expander class="w-3rem" />

		<!-- Symbol -->
		<Column header="Symbol" class="w-2">
			<template #body="slotProps">
				<span class="truncate-text">
					{{ slotProps.data.id }}
				</span>
			</template>
		</Column>

		<!-- Name -->
		<Column header="Name">
			<template #body="{ data }">
				<InputText
					v-model.lazy="data.name"
					:disabled="configView || readonly || data.type === ParamType.MATRIX"
					@update:model-value="updateMetadataFromInput(data.value.target, 'name', $event)"
				/>
			</template>
		</Column>

		<Column header="Description" class="w-2">
			<template #body="slotProps">
				<InputText
					v-model.lazy="slotProps.data.description"
					:disabled="configView || readonly || slotProps.data.type === ParamType.MATRIX"
					@update:model-value="
						updateMetadataFromInput(slotProps.data.value.target, 'description', $event)
					"
				/>
			</template>
		</Column>

		<Column header="Concept" class="w-1">
			<template #body="{ data }">
				<template v-if="data.concept?.identifiers && !isEmpty(data.concept.identifiers)">
					{{
						getNameOfCurieCached(
							nameOfCurieCache,
							getCurieFromGroudingIdentifier(data.concept.identifiers)
						)
					}}

					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="getCurieUrl(getCurieFromGroudingIdentifier(data.concept.identifiers))"
						@click.stop
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
				<template v-else>--</template>
			</template>
			<template v-if="!configView && !readonly" #editor="{ data }">
				<AutoComplete
					v-model="conceptSearchTerm.name"
					:suggestions="curies"
					@complete="onSearch"
					@item-select="
						updateMetadataFromInput(data.value.target, 'concept', {
							grounding: { identifiers: parseCurie($event.value.curie) }
						})
					"
					optionLabel="name"
					:forceSelection="true"
					:inputStyle="{ width: '100%' }"
				/>
			</template>
		</Column>

		<Column header="Unit" class="w-1">
			<template #body="slotProps">
				<InputText
					v-if="slotProps.data.type === ParamType.CONSTANT"
					class="w-full"
					:disabled="readonly"
					v-model.lazy="slotProps.data.unit"
					@update:model-value="
						(val) => updateMetadataFromInput(slotProps.data.value.target, 'unit', val)
					"
				/>
				<template v-else>--</template>
			</template>
		</Column>

		<!-- Value type: Matrix or Expression -->
		<Column field="type" header="Value Type" class="w-2">
			<template #body="slotProps">
				<Button
					text
					v-if="slotProps.data.type === ParamType.MATRIX"
					icon="pi pi-table"
					label="Matrix"
					@click="openMatrixModal(slotProps.data)"
					class="p-0"
				/>
				<Dropdown
					v-else
					class="value-type-dropdown w-9"
					:model-value="slotProps.data.type"
					:options="typeOptions"
					optionLabel="label"
					optionValue="value"
					placeholder="Select a parameter type"
					:disabled="readonly"
					@update:model-value="(val) => changeType(slotProps.data.value, val)"
				>
					<template #value="slotProps">
						<span class="flex align-items-center">
							<span
								class="p-dropdown-item-icon mr-2"
								:class="typeOptions.find((op) => slotProps.value === op.value)?.icon"
							></span>
							<span>{{ typeOptions.find((op) => slotProps.value === op.value)?.label }}</span>
						</span>
					</template>
					<template #option="slotProps">
						<span class="flex align-items-center">
							<span class="p-dropdown-item-icon mr-2" :class="slotProps.option.icon"></span>
							<span>{{ slotProps.option.label }}</span>
						</span>
					</template>
				</Dropdown>
			</template>
		</Column>

		<!-- Value: the thing we show depends on the type of number -->
		<Column field="value" header="Value" class="w-2 pr-2">
			<template #body="slotProps">
				<!-- Matrix -->
				<span
					v-if="slotProps.data.type === ParamType.MATRIX"
					@click="openMatrixModal(slotProps.data)"
					class="cursor-pointer secondary-text text-sm"
					>Open matrix</span
				>
				<!-- Expression -->
				<span
					v-else-if="
						slotProps.data.type === ParamType.EXPRESSION ||
						slotProps.data.type === ParamType.CONSTANT
					"
				>
					<InputText
						class="tabular-numbers w-full"
						v-model.lazy="slotProps.data.value.expression"
						:disabled="readonly"
						@update:model-value="updateExpression(slotProps.data.value)"
					/>
				</span>
			</template>
		</Column>

		<!-- Source  -->
		<Column field="source" header="Source" class="w-2">
			<template #body="{ data }">
				<InputText
					v-if="data.type === ParamType.CONSTANT"
					class="w-full"
					v-model.lazy="data.source"
					:disabled="readonly"
					@update:model-value="(val) => updateMetadataFromInput(data.value.target, 'source', val)"
				/>
			</template>
		</Column>
		<!-- Hiding for now until functionality is available
		<Column field="visibility" header="Visibility" style="width: 10%">
			<template #body="slotProps">
				<InputSwitch v-model="slotProps.data.visibility" @click.stop />
			</template>
		</Column> -->
		<template #expansion="slotProps">
			<tera-initial-table
				hide-header
				v-if="slotProps.data.type === ParamType.MATRIX"
				:model="model"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:data="slotProps.data.tableFormattedMatrix"
				:config-view="configView"
				:readonly="readonly"
				@update-value="(val: Initial) => emit('update-value', [val])"
				@update-model="(model: Model) => emit('update-model', model)"
			/>
		</template>
	</Datatable>
	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalContext.isOpen && isStratified"
			:id="matrixModalContext.matrixId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Initials"
			:open-value-config="matrixModalContext.isOpen"
			@close-modal="matrixModalContext.isOpen = false"
			@update-cell-value="(configToUpdate: any) => updateCellValue(configToUpdate)"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import type { Model, Initial, DKG } from '@/types/Types';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';

import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { StratifiedMatrix } from '@/types/Model';
import { ModelConfigTableData, ParamType } from '@/types/common';
import { pythonInstance } from '@/python/PyodideController';
import {
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	getNameOfCurieCached,
	searchCuriesEntities,
	parseCurie
} from '@/services/concept';
import { getUnstratifiedInitials } from '@/model-representation/petrinet/mira-petri';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { isStratifiedModel } from '@/model-representation/mira/mira';
import { matrixEffect } from '@/utils/easter-eggs';
import {
	getMetadata,
	getModelInitials,
	updateVariable,
	updateMetadata
} from '@/model-representation/service';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';

const typeOptions = [
	{ label: 'Constant', value: ParamType.CONSTANT, icon: 'pi pi-hashtag' },
	{ label: 'Expression', value: ParamType.EXPRESSION, icon: 'custom-icon-expression' }
];

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	data?: ModelConfigTableData[];
	hideHeader?: boolean;
	readonly?: boolean;
	configView?: boolean;
}>();

const emit = defineEmits(['update-value', 'update-model']);

const matrixModalContext = ref({
	isOpen: false,
	matrixId: ''
});

const curies = ref<DKG[]>([]);
const conceptSearchTerm = ref({
	curie: '',
	name: ''
});
const nameOfCurieCache = ref(new Map<string, string>());

const expandedRows = ref([]);

const initials = computed<Map<string, string[]>>(() => {
	const result = new Map<string, string[]>();
	if (!props.mmt) return result;

	const model = props.model;
	if (isStratified.value) {
		return getUnstratifiedInitials(model);
	}
	model.semantics?.ode.initials?.forEach((initial) => {
		result.set(initial.target, [initial.target]);
	});
	return result;
});

const tableFormattedInitials = computed<ModelConfigTableData[]>(() => {
	const model = props.model;
	const formattedInitials: ModelConfigTableData[] = [];

	if (isStratified.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const initial = getModelInitials(model).find((i) => i.target === v);
				const initialsMetadata = getMetadata(model, 'initials', initial!.target);
				const sourceValue = initialsMetadata?.source;
				const unitValue = initialsMetadata?.unit;
				const nameValue = initialsMetadata?.name;
				const descriptionValue = initialsMetadata?.description;
				const conceptValue = initialsMetadata?.concept;
				const expressionValue = initialsMetadata?.expression;
				return {
					id: v,
					name: nameValue,
					description: descriptionValue,
					concept: conceptValue?.grounding,
					type: expressionValue ? ParamType.EXPRESSION : ParamType.CONSTANT,
					unit: unitValue,
					value: initial,
					source: sourceValue,
					visibility: false
				};
			});
			formattedInitials.push({
				id: init,
				name: init,
				description: '',
				concept: { identifiers: {} },
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				tableFormattedMatrix
			});
		});
	} else {
		initials.value.forEach((vals, init) => {
			const initial = getModelInitials(model).find((i) => i.target === vals[0]);
			const initialsMetadata = getMetadata(model, 'initials', initial!.target);
			const sourceValue = initialsMetadata?.source;
			const unitValue = initialsMetadata?.unit;
			const nameValue = initialsMetadata?.name;
			const descriptionValue = initialsMetadata?.description;
			const conceptValue = initialsMetadata?.concept;
			const expressionValue = initialsMetadata?.expression;
			formattedInitials.push({
				id: init,
				name: nameValue,
				description: descriptionValue,
				concept: conceptValue?.grounding,
				type: expressionValue ? ParamType.EXPRESSION : ParamType.CONSTANT,
				unit: unitValue,
				value: initial,
				source: sourceValue,
				visibility: false
			});
		});
	}

	return formattedInitials;
});

const openMatrixModal = (datum: ModelConfigTableData) => {
	matrixEffect();

	const id = datum.id;
	if (!datum.tableFormattedMatrix) return;
	matrixModalContext.value = {
		isOpen: true,
		matrixId: id
	};
};

const rowClass = (rowData) => (rowData.type === ParamType.MATRIX ? '' : 'no-expander');

const updateCellValue = (v: any) => {
	const clone = cloneDeep(props.model);
	updateVariable(clone, 'initials', v.variableName, v.newValue, v.mathml);
	emit('update-model', clone);
};

const updateMetadataFromInput = (id: string, key: string, value: any) => {
	const clone = cloneDeep(props.model);
	updateMetadata(clone, id, 'initials', key, value);
	emit('update-model', clone);
};

const isStratified = computed(() => {
	if (!props.mmt) return false;
	return isStratifiedModel(props.mmt);
});

const updateExpression = async (value: Initial) => {
	const mathml = (await pythonInstance.parseExpression(value.expression)).mathml;
	value.expression_mathml = mathml;
	emit('update-value', [value]);
};
async function onSearch(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) {
		const response = await searchCuriesEntities(query);
		curies.value = response;
	}
}

const changeType = (initial: Initial, typeIndex: number) => {
	const clonedModel = cloneDeep(props.model);
	const metadata = clonedModel.metadata?.initials;
	if (!metadata) return;
	if (!metadata[initial.target]) {
		metadata[initial.target] = {};
	}
	switch (typeIndex) {
		case ParamType.EXPRESSION:
			if (metadata) metadata[initial.target].expression = true;
			break;
		case ParamType.CONSTANT:
		default:
			metadata[initial.target].expression = false;
			break;
	}

	emit('update-model', clonedModel);
};
</script>

<style scoped>
.truncate-text {
	display: flex;
	width: 10rem;
}

.p-datatable.p-datatable-sm :deep(.p-datatable-tbody > tr > td) {
	padding: 0;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td .p-row-toggler) {
	visibility: hidden;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander) {
	background: var(--surface-0);
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td) {
	padding: 0;
}

.hide-header :deep(.p-datatable-thead) {
	display: none;
}

.constant-number > :deep(input) {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.tabular-numbers {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.custom-icon-expression {
	background-image: url('@assets/svg/icons/expression.svg');
	background-size: contain;
	background-repeat: no-repeat;
	display: inline-block;
	width: 1rem;
	height: 1rem;
}

.secondary-text {
	color: var(--text-color-subdued);
}

.value-type-dropdown {
	min-width: 10rem;
}
</style>
