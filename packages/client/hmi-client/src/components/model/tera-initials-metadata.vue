<template>
	<ul>
		<li
			v-for="({ baseInitial, childInitials, isVirtual }, index) in initialList"
			:key="baseInitial.id"
		>
			<template v-if="isVirtual">
				<tera-initial-metadata-entry
					:initial="baseInitial"
					is-base
					:show-stratified-variables="toggleStates[index]"
					@toggle-stratified-variables="toggleStates[index] = !toggleStates[index]"
					@update-initial-metadata="updateBaseInitial(baseInitial.target, $event)"
				/>
				<ul v-if="toggleStates[index]" class="stratified">
					<li v-for="childInitial in childInitials" :key="childInitial.target">
						<tera-initial-metadata-entry
							:initial="childInitial"
							is-stratified
							@update-initial-metadata="
								$emit('update-initial-metadata', { id: childInitial.target, ...$event })
							"
						/>
					</li>
				</ul>
			</template>
			<tera-initial-metadata-entry
				v-else
				:initial="baseInitial"
				@update-initial-metadata="
					$emit('update-initial-metadata', { id: baseInitial.target, ...$event })
				"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Model } from '@/types/Types';
import { getInitialsAlt } from '@/model-representation/service';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { collapseInitials } from '@/model-representation/mira/mira';
import TeraInitialMetadataEntry from '@/components/model/tera-initial-metadata-entry.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

const emit = defineEmits(['update-initial-metadata']);

const initials = getInitialsAlt(props.model); // could be states, vertices, and stocks type

const collapsedInitials = collapseInitials(props.mmt);
const initialList = Array.from(collapsedInitials.keys())
	.flat()
	.map((id) => {
		const childTargets = collapsedInitials.get(id) ?? [];
		const childInitials = childTargets
			.map((childTarget) => initials.find((i: any) => i.id === childTarget))
			.filter(Boolean);
		const isVirtual = childTargets.length > 1;

		// If the initial is virtual, we need to get it from model.metadata
		const baseInitial = isVirtual
			? props.model.metadata?.initials?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
			: initials.find((i: any) => i.id === id);

		console.log('baseInitial', baseInitial);

		return { baseInitial, childInitials, isVirtual };
	});

const toggleStates = ref(Array.from({ length: collapsedInitials.size }, () => false));

function updateBaseInitial(baseTarget: string, event: any) {
	// In order to modify the base we need to do it within the model's metadata since it doesn't actually exist in the model
	emit('update-initial-metadata', { id: baseTarget, isMetadata: true, ...event });
	// Cascade the change to all children
	const targets = collapsedInitials.get(baseTarget);
	targets?.forEach((target) => emit('update-initial-metadata', { id: target, ...event }));
}
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
