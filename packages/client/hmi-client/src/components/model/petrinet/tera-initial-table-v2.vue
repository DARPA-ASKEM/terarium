<template>
	<tera-initial-entry
		v-for="{ target } in getInitials(modelConfiguration)"
		:model-configuration="modelConfiguration"
		:initial-id="target"
		:key="target"
	/>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { getInitials } from '@/services/model-configurations';
import TeraInitialEntry from './tera-initial-entry.vue';

defineProps<{
	modelConfiguration: ModelConfiguration;
}>();
</script>
