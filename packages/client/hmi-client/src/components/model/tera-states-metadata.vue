<template>
	<ul>
		<li v-for="({ baseState, childStates, isVirtual }, index) in stateList" :key="index">
			<template v-if="isVirtual">
				<tera-state-metadata-entry
					:state="baseState"
					is-base
					:show-stratified-variables="toggleStates[index]"
					@toggle-stratified-variables="toggleStates[index] = !toggleStates[index]"
					@update-state="updateBaseState(baseState.id, $event)"
				/>
				<ul v-if="toggleStates[index]" class="stratified">
					<li v-for="childState in childStates" :key="childState.id">
						<tera-state-metadata-entry
							:state="childState"
							is-stratified
							@update-state="$emit('update-state', { id: childState.id, ...$event })"
						/>
					</li>
				</ul>
			</template>
			<tera-state-metadata-entry
				:state="baseState"
				@update-state="$emit('update-state', { id: baseState.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Model, PetriNetState, RegNetVertex } from '@/types/Types';
import { getStates } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraStateMetadataEntry from '@/components/model/tera-state-metadata-entry.vue';

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

function updateBaseState(baseId: string, event: any) {
	// In order to modify the base we need to do it within the model's metadata since it doesn't actually exist in the model
	emit('update-state', { id: baseId, isMetadata: true, ...event });
	// Cascade the change to all children
	const targets = collapsedInitials.value.get(baseId);
	targets?.forEach((target) => emit('update-state', { id: target, ...event }));
}

const toggleStates = ref<boolean[]>([]);
watch(
	() => collapsedInitials.value.size,
	() => {
		toggleStates.value = Array.from({ length: collapsedInitials.value.size }, () => false);
	}
);
</script>

<style scoped>
li {
	padding-bottom: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
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
