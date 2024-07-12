<template>
	<ul>
		<li v-for="({ baseState, childStates, isVirtual }, index) in stateList" :key="index">
			<template v-if="isVirtual && !isEmpty(baseOptions)">
				<section class="base">
					<span>
						<Button
							:icon="baseOptions[index].showChildren ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="baseOptions[index].showChildren = !baseOptions[index].showChildren"
						/>
						<h6>{{ baseState.id }}</h6>
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
									updateAllChildren(baseState.id, 'units', baseOptions[index].childrenUnits);
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
									updateAllChildren(baseState.id, 'concept', baseOptions[index].childrenConcepts);
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
					<li v-for="({ id, name, grounding, initial }, index) in childStates" :key="index">
						<tera-variable-metadata-entry
							:id="id"
							:name="name"
							:grounding="grounding"
							:unit="initial?.expression"
							@update-variable="$emit('update-state', { id, ...$event })"
						/>
					</li>
				</ul>
			</template>
			<tera-variable-metadata-entry
				v-else
				:id="baseState.id"
				:name="baseState.name"
				:grounding="baseState.grounding"
				:unit="baseState.initial?.expression"
				@update-variable="$emit('update-state', { id: baseState.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, computed, onMounted } from 'vue';
import Button from 'primevue/button';
import { Model, PetriNetState, RegNetVertex } from '@/types/Types';
import { getStates } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraVariableMetadataEntry from '@/components/model/tera-variable-metadata-entry.vue';
import TeraInput from '../widgets/tera-input.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

const emit = defineEmits(['update-state']);

const states = computed(() => getStates(props.model)); // could be states, vertices, and stocks type
const collapsedInitials = computed(() => collapseInitials(props.mmt));
const stateList = computed<
	{
		baseState: PetriNetState | RegNetVertex;
		childStates: (PetriNetState | RegNetVertex)[];
		isVirtual: boolean;
	}[]
>(() =>
	Array.from(collapsedInitials.value.keys())
		.flat()
		.map((id) => {
			const childTargets = collapsedInitials.value.get(id) ?? [];
			const childStates = childTargets
				.map((childTarget) => states.value.find((i: any) => i.id === childTarget))
				.filter(Boolean);
			const isVirtual = childTargets.length > 1;

			// If the initial is virtual, we need to get it from model.metadata
			const baseState = isVirtual
				? props.model.metadata?.initials?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
				: states.value.find((i: any) => i.id === id);

			return { baseState, childStates, isVirtual };
		})
);

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
	baseOptions.value = Array.from({ length: collapsedInitials.value.size }, () => ({
		showChildren: false,
		isEditingChildrenUnits: false,
		isEditingChildrenConcepts: false,
		childrenUnits: '',
		childrenConcepts: ''
	}));
});

function updateAllChildren(baseId: string, key: string, value: string) {
	if (isEmpty(value)) return;
	const ids = collapsedInitials.value.get(baseId);
	ids?.forEach((id) => emit('update-state', { id, key, value }));
}
</script>

<style scoped>
li {
	padding-bottom: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
}

.base,
.base > span {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.stratified {
	gap: var(--gap-small);
	margin: var(--gap-small) 0 0 var(--gap-medium);

	& > li {
		border-left: 2px solid var(--primary-color-dark);
		padding-left: var(--gap);
		padding-bottom: 0;
		border-bottom: none;
	}
}
</style>
