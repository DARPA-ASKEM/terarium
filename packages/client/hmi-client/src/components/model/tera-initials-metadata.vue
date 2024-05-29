<template>
	<ul>
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
import { getInitials } from '@/model-representation/service';
import TeraInitialMetadataEntry from '@/components/model/tera-initial-metadata-entry.vue';

const props = defineProps<{
	model: Model;
}>();

defineEmits(['update-initial-metadata']);

const initials = getInitials(props.model);
</script>

<style scoped>
li {
	padding-top: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
}
</style>
