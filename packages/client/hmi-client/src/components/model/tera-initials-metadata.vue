<template>
	<!-- <ul v-if="isStratified">
		<li v-for="([baseTarget, values], index) in collapsedInitials.entries()" :key="baseTarget">
			<tera-initial-metadata-entry
				:state="states[0]"
				:model="model"
				:target="baseTarget"
				is-base
				:show-stratified-variables="toggleStates[index]"
				@toggle-stratified-variables="toggleStates[index] = !toggleStates[index]"
				@update-initial-metadata="updateBaseInitial(baseTarget, $event)"
			/>
			<ul v-if="toggleStates[index]" class="stratified">
				<li v-for="target in values" :key="target">
					<tera-initial-metadata-entry
						:state="states[0]"
						:model="model"
						:target="target"
						is-stratified
						@update-initial-metadata="$emit('update-initial-metadata', { target, ...$event })"
					/>
				</li>
			</ul>
		</li>
	</ul> -->
	<!-- <ul v-else> -->
	<ul>
		<li v-for="state in states" :key="state.id">
			<tera-initial-metadata-entry
				:state="state"
				:model="model"
				@update-initial-metadata="$emit('update-initial-metadata', { id: state.id, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
// import { ref } from 'vue';
import { Model } from '@/types/Types';
import { MiraModel } from '@/model-representation/mira/mira-common';
// import { getInitials } from '@/model-representation/service';
// import { collapseInitials, isStratifiedModel } from '@/model-representation/mira/mira';
import TeraInitialMetadataEntry from '@/components/model/tera-initial-metadata-entry.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

// const emit = defineEmits(['update-initial-metadata']);

const { states } = props.model.model;
console.log(states);

// const initials = getInitials(props.model);
// const isStratified = isStratifiedModel(props.mmt);
// const collapsedInitials = collapseInitials(props.mmt);

// const toggleStates = ref(Array.from({ length: collapsedInitials.size }, () => false));

// function updateBaseInitial(baseTarget: string, event: any) {
// 	emit('update-initial-metadata', { target: baseTarget, ...event });
// 	// Update stratified initials if the event is a unit change
// 	const targets = collapsedInitials.get(baseTarget);
// 	if (targets && event.key === 'units') {
// 		targets.forEach((target) => emit('update-initial-metadata', { target, ...event }));
// 	}
// }
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
