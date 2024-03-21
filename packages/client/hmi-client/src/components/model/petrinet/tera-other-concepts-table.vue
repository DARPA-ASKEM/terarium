<template>
	<DataTable v-if="!isEmpty(otherConcepts)" data-key="id" :value="otherConcepts">
		<Column header="Assign to">
			<template #body="{ data }">
				<Button text icon="pi pi-plus" @click.stop="openAlignmentAssignmentDialog(data)" />
			</template>
		</Column>
		<Column field="payload.id.id" header="Payload id" />
		<Column header="Names">
			<template #body="{ data }">
				{{ getName(data) }}
			</template>
		</Column>
		<Column header="Descriptions">
			<template #body="{ data }">
				{{ getDescription(data) }}
			</template>
		</Column>
		<Column field="payload.groundings" header="Concept">
			<template #body="{ data }">
				<template v-if="!getConcept(data)">--</template>
				<template v-else>
					{{ getConcept(data).grounding_text }}
					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="getCurieUrl(getConcept(data).grounding_id)"
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
			</template>
		</Column>
		<Column header="Values">
			<template #body="{ data }">
				{{ getValue(data) }}
			</template>
		</Column>
	</DataTable>

	<Teleport to="body">
		<tera-modal v-if="isAssignmentModalVisible" @modal-mask-clicked="onCloseAssignemntModal">
			<template #header>
				<h3>Assign metadata</h3>
			</template>
			<DataTable
				v-model:selection="selectedAssignmentRows"
				data-key="name"
				:value="assignmentTableData"
				tableStyle="min-width: 50rem"
			>
				<Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
				<Column field="name" />
				<Column field="suggestedValue" header="Suggested new values">
					<template #body="{ data }">
						<span v-if="data.key === ComparisonKey.GROUNDING">
							{{ data.suggestedValue?.grounding_text }}
							<a
								v-if="data.suggestedValue"
								target="_blank"
								rel="noopener noreferrer"
								:href="getCurieUrl(data.suggestedValue?.grounding_id)"
								aria-label="Open Concept"
							>
								<i class="pi pi-external-link" />
							</a>
						</span>
						<span v-else>
							{{ data.suggestedValue }}
						</span>
					</template>
				</Column>
				<Column field="currentValue">
					<template #header>
						Current values for:
						<AutoComplete
							v-model="semanticSearchTerm"
							placeholder="Select a variable or parameter"
							:suggestions="filteredSemantics"
							@complete="onSearch"
							@item-select="onItemSelect"
							option-label="name"
						/>
					</template>
					<template #body="{ data }">
						<span v-if="data.key === ComparisonKey.GROUNDING && data.currentValue?.identifiers">
							{{
								getNameOfCurieCached(
									nameOfCurieCache,
									getCurieFromGroudingIdentifier(data.currentValue.identifiers)
								)
							}}
							<a
								v-if="data.currentValue"
								target="_blank"
								rel="noopener noreferrer"
								:href="getCurieUrl(getCurieFromGroudingIdentifier(data.currentValue.identifiers))"
								aria-label="Open Concept"
							>
								<i class="pi pi-external-link" />
							</a>
						</span>
						<span v-else>
							{{ data.currentValue }}
						</span>
					</template>
				</Column>
			</DataTable>
			<template #footer>
				<Button
					:disabled="isEmpty(selectedAssignmentRows) || isEmpty(selectedCurrentSemantic)"
					label="Replace current with selected"
					@click="updateModel"
				/>
				<Button outlined label="Cancel" @click="onCloseAssignemntModal" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { Initial, Model, ModelParameter } from '@/types/Types';
import { computed, ref } from 'vue';
import { cloneDeep, groupBy, isEmpty } from 'lodash';
import { Dictionary } from 'vue-gtag';
import {
	getCurieUrl,
	parseCurie,
	getCurieFromGroudingIdentifier,
	getNameOfCurieCached
} from '@/services/concept';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import AutoComplete, {
	AutoCompleteCompleteEvent,
	AutoCompleteItemSelectEvent
} from 'primevue/autocomplete';
import { isInitial, isModelParameter } from '@/utils/data-util';

const props = defineProps<{
	model: Model;
}>();

enum ComparisonKey {
	NAME = 'name',
	DESCRIPTION = 'description',
	GROUNDING = 'grounding',
	VALUE = 'value'
}
interface ComparisonData {
	key: ComparisonKey;
	name: string;
	suggestedValue: any;
	currentValue: any;
}
const emit = defineEmits(['update-model']);

const isAssignmentModalVisible = ref(false);

const nameOfCurieCache = ref(new Map<string, string>());
const selectedConcept = ref(null);
const semanticSearchTerm = ref('');
const selectedCurrentSemantic = ref<ModelParameter | Initial | null>(null);

const selectedAssignmentRows = ref<ComparisonData[]>([]);
const assignmentTableData = computed<ComparisonData[]>(() => {
	const concept = selectedConcept.value;

	let currentName;
	let currentDescription;
	let currentConcept;
	let currentValue;

	if (isInitial(selectedCurrentSemantic.value)) {
		const metadata = props.model.metadata?.initials?.[selectedCurrentSemantic.value.target];
		currentName = metadata?.name;
		currentDescription = metadata?.description;
		currentConcept = metadata?.concept?.grounding;
		currentValue = selectedCurrentSemantic.value.expression;
	}

	if (isModelParameter(selectedCurrentSemantic.value)) {
		currentName = selectedCurrentSemantic.value.name;
		currentDescription = selectedCurrentSemantic.value.description;
		currentConcept = selectedCurrentSemantic.value.grounding;
		currentValue = selectedCurrentSemantic.value.value;
	}

	return [
		{
			key: ComparisonKey.NAME,
			name: 'Name',
			suggestedValue: getName(concept),
			currentValue: currentName
		},
		{
			key: ComparisonKey.DESCRIPTION,
			name: 'Description',
			suggestedValue: getDescription(concept),
			currentValue: currentDescription
		},
		{
			key: ComparisonKey.GROUNDING,
			name: 'Concept',
			suggestedValue: getConcept(concept),
			currentValue: currentConcept
		},
		{
			key: ComparisonKey.VALUE,
			name: 'Value',
			suggestedValue: getValue(concept),
			currentValue
		}
	];
});
const semantics = computed(() => {
	const semanticsArray: any[] = [];
	props.model.semantics?.ode.initials?.forEach((initial) => {
		semanticsArray.push({
			id: initial.target,
			name: initial.target,
			value: initial
		});
	});

	props.model.semantics?.ode.parameters?.forEach((parameter) => {
		semanticsArray.push({
			id: parameter.id,
			name: parameter.name,
			value: parameter
		});
	});

	return semanticsArray;
});

const filteredSemantics = ref<any[]>([]);

const extractions = computed(() => {
	const attributes = props.model?.metadata?.attributes ?? [];
	return groupBy(attributes, 'amr_element_id');
});
const states = computed(() => props.model?.model?.states ?? []);
const transitions = computed(() => {
	const results: any[] = [];
	if (props.model?.model?.transitions) {
		props.model.model.transitions.forEach((t) => {
			results.push({
				id: t.id,
				name: t?.properties?.name ?? '--',
				input: !isEmpty(t.input) ? t.input.join(', ') : '--',
				output: !isEmpty(t.output) ? t.output.join(', ') : '--',
				expression:
					props.model?.semantics?.ode?.rates?.find((rate) => rate.target === t.id)?.expression ??
					null,
				extractions: extractions?.[t.id] ?? null
			});
		});
	}
	return results;
});
const otherConcepts = computed(() => {
	const ids = [
		...(states.value?.map((s) => s.id) ?? []),
		...(transitions.value?.map((t) => t.id) ?? [])
	];

	// find keys that are not aligned
	const unalignedKeys = Object.keys(extractions.value).filter((k) => !ids.includes(k));

	let unalignedExtractions: Dictionary<any>[] = [];
	unalignedKeys.forEach((key) => {
		unalignedExtractions = unalignedExtractions.concat(
			extractions.value[key.toString()].filter((e) =>
				['anchored_extraction', 'anchored_entity'].includes(e.type)
			)
		);
	});

	return unalignedExtractions ?? [];
});

const openAlignmentAssignmentDialog = (data: any) => {
	selectedConcept.value = data;
	isAssignmentModalVisible.value = true;
};

const onCloseAssignemntModal = () => {
	isAssignmentModalVisible.value = false;
	semanticSearchTerm.value = '';
	selectedCurrentSemantic.value = null;
	selectedAssignmentRows.value = [];
};

const onSearch = (event: AutoCompleteCompleteEvent) => {
	const query = event.query.toLowerCase();
	filteredSemantics.value = semantics.value.filter((s) => s.name.toLowerCase().includes(query));
};

const onItemSelect = (event: AutoCompleteItemSelectEvent) => {
	selectedCurrentSemantic.value = event.value?.value;
};

const getDescription = (data) =>
	data.payload?.descriptions?.map((d) => d?.source).join(', ') ||
	data.payload?.text_descriptions?.map((d) => d?.description).join(', ');

const getName = (data) =>
	data.payload?.names?.map((n) => n?.name).join(', ') ||
	data.payload?.mentions?.map((m) => m?.name).join(', ');

const getConcept = (data) => data.payload?.groundings?.[0];

const getValue = (data) =>
	data.payload?.values?.map((n) => n?.value?.amount).join(', ') ||
	data.payload?.value_descriptions?.map((m) => m?.value?.amount).join(', ');

const updateModel = () => {
	const nameRow = selectedAssignmentRows.value.find((row) => row.key === ComparisonKey.NAME);
	const descriptionRow = selectedAssignmentRows.value.find(
		(row) => row.key === ComparisonKey.DESCRIPTION
	);
	const groundingRow = selectedAssignmentRows.value.find(
		(row) => row.key === ComparisonKey.GROUNDING
	);
	const valueRow = selectedAssignmentRows.value.find((row) => row.key === ComparisonKey.VALUE);

	const clonedModel = cloneDeep(props.model);
	if (isInitial(selectedCurrentSemantic.value)) {
		const initial = clonedModel.semantics?.ode.initials?.find(
			(i) => i.target === (selectedCurrentSemantic.value as Initial).target
		);
		if (!initial) return;
		const metadata = clonedModel.metadata?.initials?.[selectedCurrentSemantic.value.target];

		if (nameRow) {
			metadata.name = nameRow.suggestedValue;
		}

		if (descriptionRow) {
			metadata.description = descriptionRow.suggestedValue;
		}

		if (groundingRow) {
			metadata.concept ??= {};
			metadata.concept.grounding = {
				identifiers: parseCurie(groundingRow.suggestedValue?.grounding_id)
			};
		}

		if (valueRow) {
			initial.expression = valueRow.suggestedValue;
		}
	}

	if (isModelParameter(selectedCurrentSemantic.value)) {
		const parameter = clonedModel.semantics?.ode.parameters?.find(
			(p) => p.id === (selectedCurrentSemantic.value as ModelParameter).id
		);
		if (!parameter) return;

		if (nameRow) {
			parameter.name = nameRow.suggestedValue;
		}

		if (descriptionRow) {
			parameter.description = descriptionRow.suggestedValue;
		}

		if (groundingRow) {
			parameter.grounding = {
				identifiers: parseCurie(groundingRow.suggestedValue?.grounding_id)
			};
		}

		if (valueRow && !Number.isNaN(valueRow.suggestedValue)) {
			parameter.value = valueRow.suggestedValue;
		}
	}

	emit('update-model', clonedModel);
	onCloseAssignemntModal();
};
</script>
