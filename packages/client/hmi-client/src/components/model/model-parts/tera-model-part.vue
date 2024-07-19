<template>
	<ul>
		<li v-for="({ base, children, isParent }, index) in items" :key="index">
			<template v-if="isParent && !isEmpty(parentEditingState)">
				<section class="parent">
					<span>
						<Button
							:icon="parentEditingState[index].showChildren ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="parentEditingState[index].showChildren = !parentEditingState[index].showChildren"
						/>
						<h6>{{ base.id }}</h6>
					</span>
					<!--N/A if it's a transition-->
					<template v-if="!children[0].input || !children[0].output">
						<Button
							v-if="!parentEditingState[index].isEditingChildrenUnits"
							@click="parentEditingState[index].isEditingChildrenUnits = true"
							label="Add unit to all children"
							text
							size="small"
						/>
						<span v-else>
							<tera-input label="Unit" placeholder="Add a unit" v-model="parentEditingState[index].childrenUnits" />
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
						</span>
					</template>
					<Button v-if="showMatrix" label="Open matrix" text size="small" @click="$emit('open-matrix', base.id)" />
					<Button
						v-if="!parentEditingState[index].isEditingChildrenConcepts"
						@click="parentEditingState[index].isEditingChildrenConcepts = true"
						label="Add concept to all children"
						text
						size="small"
					/>
					<span v-else>
						<tera-input
							label="Concept"
							placeholder="Select a concept"
							icon="pi pi-search"
							disabled
							v-model="parentEditingState[index].childrenConcepts"
						/>
						<Button
							icon="pi pi-check"
							text
							rounded
							size="small"
							@click="
								() => {
									updateAllChildren(base.id, 'concept', parentEditingState[index].childrenConcepts);
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
					</span>
				</section>
				<ul v-show="parentEditingState[index].showChildren" class="stratified">
					<li v-for="(child, index) in children" :key="index">
						<tera-model-part-entry
							:item="child"
							:disabled-inputs="disabledInputs"
							@update-item="$emit('update-item', { id: child.id, ...$event })"
						/>
					</li>
				</ul>
			</template>
			<tera-model-part-entry
				v-else
				:item="base"
				:disabled-inputs="disabledInputs"
				@update-item="$emit('update-item', { id: base.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, onMounted } from 'vue';
import type { ModelPartItem } from '@/types/Model';
import TeraModelPartEntry from '@/components/model/model-parts/tera-model-part-entry.vue';
import Button from 'primevue/button';
import TeraInput from '@/components/widgets/tera-input.vue';

const props = defineProps<{
	items: {
		base: ModelPartItem;
		children: ModelPartItem[];
		isParent: boolean;
	}[];
	collapsedItems?: Map<string, string[]>;
	disabledInputs?: string[];
	showMatrix?: boolean;
}>();

const emit = defineEmits(['update-item', 'open-matrix']);

const parentEditingState = ref<
	{
		showChildren: boolean;
		isEditingChildrenUnits: boolean;
		isEditingChildrenConcepts: boolean;
		childrenUnits: string;
		childrenConcepts: string;
	}[]
>([]);
onMounted(() => {
	parentEditingState.value = Array.from({ length: props.items.length }, () => ({
		showChildren: false,
		isEditingChildrenUnits: false,
		isEditingChildrenConcepts: false,
		childrenUnits: '',
		childrenConcepts: ''
	}));
});

function updateAllChildren(base: string, key: string, value: string) {
	if (isEmpty(value) || !props.collapsedItems) return;
	const ids = props.collapsedItems.get(base);
	ids?.forEach((id) => emit('update-item', { id, key, value }));
}
</script>

<style scoped>
li {
	padding-bottom: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
}

.parent,
.parent > span {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.stratified {
	gap: var(--gap-xsmall);
	margin: var(--gap-small) 0 0 var(--gap-medium);

	& > li {
		border-left: 2px solid var(--primary-color-dark);
		padding-left: var(--gap);
		padding-bottom: 0;
		border-bottom: none;
	}
}
</style>
