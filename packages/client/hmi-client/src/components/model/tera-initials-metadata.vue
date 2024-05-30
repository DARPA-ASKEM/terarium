<template>
	<ul v-if="isStratified">
		<li v-for="[baseTarget, values] in collapsedInitials.entries()" :key="baseTarget">
			<tera-initial-metadata-entry
				:model="model"
				:target="baseTarget"
				@update-initial-metadata="updateBaseInitial(baseTarget, $event)"
			/>
			<ul>
				<li v-for="target in values" :key="target">
					<tera-initial-metadata-entry
						:model="model"
						:target="target"
						is-stratified
						@update-initial-metadata="$emit('update-initial-metadata', { target, ...$event })"
					/>
				</li>
			</ul>
		</li>
	</ul>
	<ul v-else>
		<li v-for="{ target } in initials" :key="target">
			<tera-initial-metadata-entry
				:model="model"
				:target="target"
				@update-initial-metadata="$emit('update-initial-metadata', { target, ...$event })"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { getInitials } from '@/model-representation/service';
import { collapseInitials, isStratifiedModel } from '@/model-representation/mira/mira';
import TeraInitialMetadataEntry from '@/components/model/tera-initial-metadata-entry.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
}>();

const emit = defineEmits(['update-initial-metadata']);

const initials = getInitials(props.model);
const isStratified = isStratifiedModel(props.mmt);
const collapsedInitials = collapseInitials(props.mmt);

function updateBaseInitial(baseTarget: string, event: any) {
	emit('update-initial-metadata', { target: baseTarget, ...event });
	// Update stratified initials if the event is a unit or concept change
	const targets = collapsedInitials.get(baseTarget);
	if (targets && (event.key === 'units' || event.key === 'concept')) {
		targets.forEach((target) => emit('update-initial-metadata', { target, ...event }));
	}
}
</script>

<style scoped>
li {
	padding: var(--gap-small) 0;
	border-bottom: 1px solid var(--surface-border);
}
</style>
