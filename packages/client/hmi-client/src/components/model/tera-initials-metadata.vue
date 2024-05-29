<template>
	<ul>
		<li v-for="{ target } in initials" :key="target">
			<tera-initial-metadata-entry
				:model="model"
				:target="target"
				@update-initial-metadata="$emit('update-initial-metadata', { target, ...$event })"
			/>
			<Divider type="solid" />
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import { getInitials } from '@/model-representation/service';
import TeraInitialMetadataEntry from '@/components/model/tera-initial-metadata-entry.vue';
import Divider from 'primevue/divider';

const props = defineProps<{
	model: Model;
}>();

defineEmits(['update-initial-metadata']);

const initials = getInitials(props.model);
</script>

<style scoped>
ul > li {
	list-style: none;
}

:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: 0;
		margin-bottom: var(--gap);
		color: var(--gray-300);
	}
	&.p-divider-vertical {
		margin-left: var(--gap-small);
		margin-right: var(--gap);
	}
}
</style>
