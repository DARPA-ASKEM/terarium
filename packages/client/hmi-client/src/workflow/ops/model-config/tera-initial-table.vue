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
		<Column header="Symbol">
			<template #body="slotProps">
				<span class="truncate-text">
					{{ slotProps.data.id }}
				</span>
			</template>
		</Column>
		<Column header="Name">
			<template #body="slotProps">
				<span class="truncate-text">
					{{ slotProps.data.name }}
				</span>
			</template>
		</Column>

		<Column header="Description" class="w-2">
			<template #body="slotProps">
				<span v-if="slotProps.data.description" class="truncate-text">
					{{ slotProps.data.description }}</span
				>
				<template v-else>--</template>
			</template>
		</Column>

		<Column header="Concept" class="w-1">
			<template #body="{ data }">
				<template
					v-if="
						data.concept?.grounding?.identifiers && !isEmpty(data.concept.grounding.identifiers)
					"
				>
					{{
						getNameOfCurieCached(
							nameOfCurieCache,
							getCurieFromGroudingIdentifier(data.concept.grounding.identifiers)
						)
					}}

					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="getCurieUrl(getCurieFromGroudingIdentifier(data.concept.grounding.identifiers))"
						@click.stop
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
				<template v-else>--</template>
			</template>
		</Column>

		<Column header="Unit" class="w-1">
			<template #body="slotProps">
				<InputText
					v-if="slotProps.data.type === ParamType.CONSTANT"
					size="small"
					class="w-full"
					v-model.lazy="slotProps.data.unit"
					@update:model-value="(val) => updateMetadata(slotProps.data.value.target, 'unit', val)"
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
					class="cursor-pointer secondary-text"
					>Click to open</span
				>
				<!-- Expression -->
				<span
					v-else-if="
						slotProps.data.type === ParamType.EXPRESSION ||
						slotProps.data.type === ParamType.CONSTANT
					"
				>
					<InputText
						size="small"
						class="tabular-numbers w-full"
						v-model.lazy="slotProps.data.value.expression"
						@update:model-value="updateExpression(slotProps.data.value)"
					/>
				</span>
			</template>
		</Column>

		<!-- Source  -->
		<Column field="source" header="Source" class="w-2">
			<template #body="{ data }">
				<InputText
					v-if="data.type === ParamType.CONSTANT || data.type === ParamType.MATRIX"
					size="small"
					class="w-full"
					v-model.lazy="data.source"
					@update:model-value="(val) => updateMetadata(data.value.target, 'source', val)"
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
				:data="slotProps.data.tableFormattedMatrix"
				@update-value="(val: Initial) => emit('update-value', [val])"
				@update-model="(model: Model) => emit('update-model', model)"
			/>
		</template>
	</Datatable>
	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalContext.isOpen && stratifiedModelType"
			:id="matrixModalContext.matrixId"
			:model="model"
			:stratified-model-type="stratifiedModelType"
			:stratified-matrix-type="StratifiedMatrix.Initials"
			:open-value-config="matrixModalContext.isOpen"
			@close-modal="matrixModalContext.isOpen = false"
			@update-model="(model: Model) => emit('update-model', model)"
		/>
	</Teleport>

	<!-- Matrix effect easter egg  -->
	<canvas id="matrix-canvas"></canvas>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import type { Initial, Model } from '@/types/Types';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import { StratifiedMatrix } from '@/types/Model';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { ModelConfigTableData, ParamType } from '@/types/common';
import { pythonInstance } from '@/python/PyodideController';
import InputText from 'primevue/inputtext';
import { cloneDeep, isEmpty } from 'lodash';
import {
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	getNameOfCurieCached
} from '@/services/concept';
import { getUnstratifiedInitials } from '@/model-representation/petrinet/mira-petri';
import Dropdown from 'primevue/dropdown';

const typeOptions = [
	{ label: 'Constant', value: ParamType.CONSTANT, icon: 'pi pi-hashtag' },
	{ label: 'Expression', value: ParamType.EXPRESSION, icon: 'custom-icon-expression' }
];

const props = defineProps<{
	model: Model;
	data?: ModelConfigTableData[];
	hideHeader?: boolean;
}>();

const emit = defineEmits(['update-value', 'update-model']);

const matrixModalContext = ref({
	isOpen: false,
	matrixId: ''
});

const conceptSearchTerm = ref({
	curie: '',
	name: ''
});
const nameOfCurieCache = ref(new Map<string, string>());

const expandedRows = ref([]);

const initials = computed<Map<string, string[]>>(() => {
	const model = props.model;
	if (stratifiedModelType.value) {
		return getUnstratifiedInitials(model);
	}
	const result = new Map<string, string[]>();
	model.semantics?.ode.initials?.forEach((initial) => {
		result.set(initial.target, [initial.target]);
	});
	return result;
});

const tableFormattedInitials = computed<ModelConfigTableData[]>(() => {
	const model = props.model;
	const formattedInitials: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const initial = model.semantics?.ode.initials?.find((i) => i.target === v);

				const initialsMetadata = model.metadata?.initials?.[initial!.target];
				const sourceValue = initialsMetadata?.source;
				const unitValue = initialsMetadata?.unit;
				const descriptionValue = initialsMetadata?.description;
				const conceptValue = initialsMetadata?.concept;
				const expressionValue = initialsMetadata?.expression;
				return {
					id: v,
					name: v,
					description: descriptionValue,
					concept: conceptValue,
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
			const initial = model.semantics?.ode.initials?.find((i) => i.target === vals[0]);
			const initialsMetadata = model.metadata?.initials?.[initial!.target];
			const sourceValue = initialsMetadata?.source;
			const unitValue = initialsMetadata?.unit;
			const descriptionValue = initialsMetadata?.description;
			const conceptValue = initialsMetadata?.concept;
			const expressionValue = initialsMetadata?.expression;
			formattedInitials.push({
				id: init,
				name: init,
				description: descriptionValue,
				concept: conceptValue,
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
	// Matrix effect easter egg (shows matrix effect 1 in 10 times a person clicks the Matrix button)
	matrixEffect();

	const id = datum.id;
	if (!datum.tableFormattedMatrix) return;
	matrixModalContext.value = {
		isOpen: true,
		matrixId: id
	};
};

const rowClass = (rowData) => (rowData.type === ParamType.MATRIX ? '' : 'no-expander');

const updateMetadata = (id: string, key: string, value: string) => {
	const clonedModel = cloneDeep(props.model);
	if (!clonedModel.metadata?.initials?.[id]) {
		clonedModel.metadata ??= {};
		clonedModel.metadata.initials ??= {};
		clonedModel.metadata.initials[id] ??= {};
	}
	clonedModel.metadata.initials[id][key] = value;
	emit('update-model', clonedModel);
};

const stratifiedModelType = computed(() => getStratificationType(props.model));

const updateExpression = async (value: Initial) => {
	const mathml = (await pythonInstance.parseExpression(value.expression)).mathml;
	value.expression_mathml = mathml;
	emit('update-value', [value]);
};

/* Matrix effect easter egg: This gets triggered 1 in 10 times a person clicks the Matrix button */
const matrixEffect = () => {
	if (Math.random() > 0.1) return;
	const canvas = document.getElementById('matrix-canvas') as HTMLCanvasElement | null;
	if (!canvas) return;
	const ctx = (canvas as HTMLCanvasElement)?.getContext('2d');

	// eslint-disable-next-line no-multi-assign
	const w = (canvas.width = document.body.offsetWidth);
	// eslint-disable-next-line no-multi-assign
	const h = (canvas.height = document.body.offsetHeight);
	const cols = Math.floor(w / 20) + 1;
	const ypos = Array(cols).fill(0);

	if (ctx) {
		ctx.fillStyle = '#FFF';
		ctx.fillRect(0, 0, w, h);
	}

	function matrix() {
		if (ctx) {
			ctx.fillStyle = '#FFF1';
			ctx.fillRect(0, 0, w, h);

			ctx.fillStyle = '#1B8073';
			ctx.font = '15pt monospace';

			ypos.forEach((y, ind) => {
				const text = String.fromCharCode(Math.random() * 128);
				const x = ind * 20;
				ctx.fillText(text, x, y);
				if (y > 100 + Math.random() * 10000) ypos[ind] = 0;
				else ypos[ind] = y + 20;
			});
		}
	}

	const intervalId = setInterval(matrix, 33);

	// after 4 seconds begin the fade out
	setTimeout(() => {
		if (canvas) {
			canvas.style.opacity = '0';
		}
	}, 3000);

	// after 5 seconds clear the canvas, stop the interval, and reset the opacity
	setTimeout(() => {
		clearInterval(intervalId);
		if (ctx) {
			ctx.clearRect(0, 0, w, h);
		}
		if (canvas) {
			canvas.style.opacity = '1';
		}
	}, 4000);
};

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
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.p-datatable.p-datatable-sm :deep(.p-datatable-tbody > tr > td) {
	padding: 0;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td .p-row-toggler) {
	display: none;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander) {
	background: var(--surface-highlight);
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td) {
	padding: 0;
}

.hide-header :deep(.p-datatable-thead) {
	display: none;
}

.distribution-container {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
}

.distribution-item > :deep(input) {
	width: 100%;
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.constant-number > :deep(input) {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.add-plus-minus > :deep(input) {
	width: 3rem;
	margin-left: var(--gap-xsmall);
}

.tabular-numbers {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.min-value {
	position: relative;
}
.min-value::before {
	content: 'Min';
	position: relative;
	top: var(--gap-small);
	left: var(--gap-small);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 0;
}
.max-value::before {
	content: 'Max';
	position: relative;
	top: var(--gap-small);
	left: var(--gap-small);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 0;
}

.custom-icon-distribution {
	background-image: url('@assets/svg/icons/distribution.svg');
	background-size: contain;
	background-repeat: no-repeat;
	display: inline-block;
	width: 1rem;
	height: 1rem;
}
.custom-icon-expression {
	background-image: url('@assets/svg/icons/expression.svg');
	background-size: contain;
	background-repeat: no-repeat;
	display: inline-block;
	width: 1rem;
	height: 1rem;
}
.invalid-message {
	color: var(--text-color-danger);
}

.timeseries-container {
	display: flex;
	flex-direction: column;
}

.secondary-text {
	color: var(--text-color-subdued);
}

.value-type-dropdown {
	min-width: 10rem;
}

#matrix-canvas {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1000;
	pointer-events: none;
	mix-blend-mode: darken;
	opacity: 1;
	transition: opacity 1s;
}
</style>
