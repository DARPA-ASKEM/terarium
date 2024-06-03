<template>
	<ul>
		<li v-for="parameter in parameters" :key="parameter.id">
			<tera-parameter-metadata-entry
				:parameter="parameter"
				@update-parameter="
					emit('update-parameter', {
						parameterId: parameter.id,
						...$event
					})
				"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import { getParameters } from '@/model-representation/service';
import TeraParameterMetadataEntry from '@/components/model/tera-parameter-metadata-entry.vue';

const props = defineProps<{
	model: Model;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = getParameters(props.model);
</script>

<style scoped>
li {
	padding: var(--gap-small) 0;
	border-bottom: 1px solid var(--surface-border);
}
</style>
