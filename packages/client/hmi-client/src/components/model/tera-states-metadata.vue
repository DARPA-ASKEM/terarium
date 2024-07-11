<template>
	<ul>
		<li v-for="({ baseState, childStates, isVirtual }, index) in stateList" :key="index">
			<template v-if="isVirtual">
				<section class="base">
					<span>
						<Button
							:icon="toggleStates[index] ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="toggleStates[index] = !toggleStates[index]"
						/>
						<h6>{{ baseState.id }}</h6>
					</span>
					<Button label="Add unit to all children" text size="small" />
					<Button label="Add concept to all children" text size="small" />
				</section>
				<ul v-if="toggleStates[index]" class="stratified">
					<li v-for="childState in childStates" :key="childState.id">
						<tera-state-metadata-entry
							:state="childState"
							@update-state="$emit('update-state', { id: childState.id, ...$event })"
						/>
					</li>
				</ul>
			</template>
			<tera-state-metadata-entry
				v-else
				:state="baseState"
				@update-state="$emit('update-state', { id: baseState.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';
import { Model, PetriNetState, RegNetVertex } from '@/types/Types';
import { getStates } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraStateMetadataEntry from '@/components/model/tera-state-metadata-entry.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

defineEmits(['update-state']);

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
