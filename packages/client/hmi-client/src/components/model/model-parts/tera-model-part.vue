<template>
	<ul>
		<li
			v-for="({ base, children, isParent }, index) in filteredItems.slice(firstRow, firstRow + MAX_NUMBER_OF_ROWS)"
			:key="index"
			class="model-part"
		>
			<template v-if="isParent && !isEmpty(parentEditingState)">
				<section class="parent">
					<span>
						<Button
							:icon="parentEditingState[index].showChildren ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="parentEditingState[index].showChildren = !parentEditingState[index].showChildren"
							class="mr-1"
						/>
						<h6>{{ base.id }}</h6>
					</span>
					<!--N/A if it's a transition-->
					<div class="right-side">
						<template v-if="!featureConfig.isPreview && (!children[0].input || !children[0].output)">
							<Button
								:disabled="parentEditingState[index].isEditingChildrenUnits"
								@click="parentEditingState[index].isEditingChildrenUnits = true"
								label="Add unit to all children"
								text
								size="small"
							/>
						</template>
						<Button v-if="showMatrix" label="Open matrix" text size="small" @click="$emit('open-matrix', base.id)" />
						<template v-if="!featureConfig.isPreview">
							<Button
								:disabled="parentEditingState[index].isEditingChildrenConcepts"
								@click="parentEditingState[index].isEditingChildrenConcepts = true"
								label="Add concept to all children"
								text
								size="small"
							/>
						</template>
					</div>
				</section>

				<!-- Add unit to all children toolbar -->
				<div v-if="parentEditingState[index].isEditingChildrenUnits" class="add-to-all-children-toolbar">
					<tera-input-text label="Unit" placeholder="Add a unit" v-model="parentEditingState[index].childrenUnits" />
					<Button
						icon="pi pi-check"
						text
						rounded
						size="small"
						@click="
							() => {
								updateAllChildren(base.id, 'unitExpression', parentEditingState[index].childrenUnits);
								parentEditingState[index].isEditingChildrenUnits = false;
							}
						"
					/>
					<Button
						icon="pi pi-times"
						text
						rounded
						size="small"
						@click="parentEditingState[index].isEditingChildrenUnits = false"
					/>
				</div>

				<!-- Add concept to all children toolbar -->
				<div v-if="parentEditingState[index].isEditingChildrenConcepts" class="add-to-all-children-toolbar">
					<span class="concept">
						<label>Concept</label>
						<AutoComplete
							label="Concept"
							size="small"
							placeholder="Search concepts"
							v-model="parentEditingState[index].childrenConcepts.name"
							:suggestions="results"
							optionLabel="name"
							@complete="
								async () => (results = await searchCuriesEntities(parentEditingState[index].childrenConcepts.name))
							"
							@item-select="
								($event) => {
									const { name, curie } = $event.value;
									parentEditingState[index].childrenConcepts = { name, curie };
								}
							"
						/>
					</span>
					<Button
						icon="pi pi-check"
						text
						rounded
						size="small"
						@click="
							() => {
								updateAllChildren(base.id, 'concept', parentEditingState[index].childrenConcepts.curie);
								parentEditingState[index].isEditingChildrenConcepts = false;
							}
						"
					/>
					<Button
						icon="pi pi-times"
						text
						rounded
						size="small"
						@click="parentEditingState[index].isEditingChildrenConcepts = false"
					/>
				</div>

				<div class="stratified" v-if="parentEditingState[index].showChildren">
					<ul>
						<li
							v-for="(child, j) in children.slice(
								parentEditingState[index].firstRow,
								parentEditingState[index].firstRow + MAX_NUMBER_OF_ROWS
							)"
							:key="j"
						>
							<tera-model-part-entry
								:item="child"
								:feature-config="featureConfig"
								@update-item="$emit('update-item', { id: child.id, ...$event })"
							/>
						</li>
					</ul>
					<Paginator
						v-if="children.length > MAX_NUMBER_OF_ROWS"
						:rows="MAX_NUMBER_OF_ROWS"
						:first="parentEditingState[index].firstRow"
						:total-records="children.length"
						:template="{
							default: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink JumpToPageDropdown'
						}"
						@page="parentEditingState[index].firstRow = $event.first"
					/>
				</div>
			</template>
			<tera-model-part-entry
				v-else
				:is-time-part="!!isTimePart"
				:item="base"
				:feature-config="featureConfig"
				@update-item="$emit('update-item', { id: base.id, ...$event })"
			/>
		</li>
	</ul>
	<Paginator
		v-if="filteredItems.length > MAX_NUMBER_OF_ROWS"
		:rows="MAX_NUMBER_OF_ROWS"
		:first="firstRow"
		:total-records="filteredItems.length"
		:template="{
			default: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink JumpToPageDropdown'
		}"
		@page="firstRow = $event.first"
	/>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch, computed } from 'vue';
import type { ModelPartItem } from '@/types/Model';
import type { DKG } from '@/types/Types';
import { searchCuriesEntities } from '@/services/concept';
import TeraModelPartEntry from '@/components/model/model-parts/tera-model-part-entry.vue';
import AutoComplete from 'primevue/autocomplete';
import Button from 'primevue/button';
import type { FeatureConfig } from '@/types/common';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Paginator from 'primevue/paginator';

const props = defineProps<{
	items: {
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[];
	featureConfig: FeatureConfig;
	collapsedItems?: Map<string, string[]>;
	showMatrix?: boolean;
	isTimePart?: boolean;
	filter?: string;
}>();

const emit = defineEmits(['update-item', 'open-matrix']);

const MAX_NUMBER_OF_ROWS = 10;

const parentEditingState = ref<
	{
		showChildren: boolean;
		isEditingChildrenUnits: boolean;
		isEditingChildrenConcepts: boolean;
		childrenUnits: string;
		childrenConcepts: {
			name: string;
			curie: string;
		};
		firstRow: number;
	}[]
>([]);
const results = ref<DKG[]>([]);
const firstRow = ref(0);

const filteredItems = computed(() => {
	const filterText = props.filter?.toLowerCase() ?? '';
	if (!filterText) return props.items;
	return props.items
		.map(({ base, children, isParent }) => {
			const filteredChildren = children.filter((child) => child.id.toLowerCase().includes(filterText));
			const baseMatches = base.id.toLowerCase().includes(filterText);
			const childrenMatch = filteredChildren.length > 0;
			if (baseMatches || childrenMatch) {
				return {
					base,
					children: filteredChildren,
					isParent
				};
			}
			return null;
		})
		.filter(Boolean) as { base: ModelPartItem; children: ModelPartItem[]; isParent: boolean }[];
});

function updateAllChildren(base: string, key: string, value: string) {
	if (isEmpty(value) || !props.collapsedItems) return;
	const ids = props.collapsedItems.get(base);
	ids?.forEach((id) => emit('update-item', { id, key, value }));
}

watch(
	() => filteredItems.value,
	() => {
		parentEditingState.value = Array.from({ length: filteredItems.value.length }, () => ({
			showChildren: false,
			isEditingChildrenUnits: false,
			isEditingChildrenConcepts: false,
			childrenUnits: '',
			childrenConcepts: {
				name: '',
				curie: ''
			},
			firstRow: 0
		}));
	},
	{ immediate: true }
);
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
	list-style: none;
	gap: var(--gap-2);
}

.model-part {
	margin-left: var(--gap-1);
	padding: var(--gap-3) 0 var(--gap-3) var(--gap-3);
	border-left: 4px solid var(--surface-border);
	background: var(--surface-0);
	transition: background-color 0.15s;
	&:hover {
		background: var(--surface-50);
	}
	&:has(.parent) {
		padding: var(--gap-2) 0 var(--gap-2) var(--gap-1);
	}
}

li {
	padding-bottom: var(--gap-2);
	border-bottom: 1px solid var(--surface-border-light);
}

.parent,
.parent > span {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.stratified {
	margin: var(--gap-2) 0 0 var(--gap-3);
	& > ul {
		background: var(--surface-0);
		& > li {
			border-left: 4px solid var(--surface-border);
			padding-left: var(--gap-4);
			padding-bottom: var(--gap-2);
			padding-top: var(--gap-2);
			border-bottom: none;
			&:hover {
				background: var(--surface-50);
			}
		}
	}
}

.concept {
	display: flex;
	align-items: center;
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	gap: var(--gap-1);
}

:deep(.p-autocomplete-input) {
	padding: var(--gap-1) var(--gap-2);
}

.add-to-all-children-toolbar {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
	padding: var(--gap-1) var(--gap-9);
	background-color: var(--surface-highlight);
}
</style>
