<template>
	<ul>
		<li v-for="({ base, children, isParent }, index) in variableList" :key="index">
			<template v-if="isParent && !isEmpty(baseOptions)">
				<section class="parent">
					<span>
						<Button
							:icon="baseOptions[index].showChildren ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="baseOptions[index].showChildren = !baseOptions[index].showChildren"
						/>
						<h6>{{ base.id }}</h6>
					</span>
					<Button
						v-if="!baseOptions[index].isEditingChildrenUnits"
						@click="baseOptions[index].isEditingChildrenUnits = true"
						label="Add unit to all children"
						text
						size="small"
					/>
					<span v-else>
						<tera-input
							label="Unit"
							placeholder="Add a unit"
							v-model="baseOptions[index].childrenUnits"
						/>
						<Button
							icon="pi pi-check"
							text
							rounded
							size="small"
							@click="
								() => {
									updateAllChildren(base.id, 'units', baseOptions[index].childrenUnits);
									baseOptions[index].isEditingChildrenUnits = false;
								}
							"
						/>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							@click="baseOptions[index].isEditingChildrenUnits = false"
						/>
					</span>
					<Button label="Open matrix" text size="small" @click="$emit('open-matrix', base.id)" />
					<Button
						v-if="!baseOptions[index].isEditingChildrenConcepts"
						@click="baseOptions[index].isEditingChildrenConcepts = true"
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
							v-model="baseOptions[index].childrenConcepts"
						/>
						<Button
							icon="pi pi-check"
							text
							rounded
							size="small"
							@click="
								() => {
									updateAllChildren(base.id, 'concept', baseOptions[index].childrenConcepts);
									baseOptions[index].isEditingChildrenConcepts = false;
								}
							"
						/>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							@click="baseOptions[index].isEditingChildrenConcepts = false"
						/>
					</span>
				</section>
				<ul v-if="baseOptions[index].showChildren" class="stratified">
					<li v-for="(child, index) in children" :key="index">
						<tera-variable-metadata-entry
							:variable="child"
							@update-variable="$emit('update-variable', { id: child.id, ...$event })"
						/>
					</li>
				</ul>
			</template>
			<tera-variable-metadata-entry
				v-else
				:variable="base"
				@update-variable="$emit('update-variable', { id: base.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, onMounted } from 'vue';
import { ModelVariable } from '@/types/Model';
import { Model } from '@/types/Types';
import TeraVariableMetadataEntry from '@/components/model/tera-variable-metadata-entry.vue';
import Button from 'primevue/button';
import TeraInput from '../widgets/tera-input.vue';

const props = defineProps<{
	model: Model;
	variableList: {
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[];
	collapsedVariables: Map<string, string[]>;
}>();

const emit = defineEmits(['update-variable', 'open-matrix']);

const baseOptions = ref<
	{
		showChildren: boolean;
		isEditingChildrenUnits: boolean;
		isEditingChildrenConcepts: boolean;
		childrenUnits: string;
		childrenConcepts: string;
	}[]
>([]);
onMounted(() => {
	baseOptions.value = Array.from({ length: props.variableList.length }, () => ({
		showChildren: false,
		isEditingChildrenUnits: false,
		isEditingChildrenConcepts: false,
		childrenUnits: '',
		childrenConcepts: ''
	}));
});

function updateAllChildren(base: string, key: string, value: string) {
	if (isEmpty(value)) return;
	const ids = props.collapsedVariables.get(base);
	ids?.forEach((id) => emit('update-variable', { id, key, value }));
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
