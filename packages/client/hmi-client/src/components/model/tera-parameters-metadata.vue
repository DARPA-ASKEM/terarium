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
			<Divider type="solid" />
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import { getParameters } from '@/model-representation/service';
import TeraParameterMetadataEntry from '@/components/model/tera-parameter-metadata-entry.vue';
import Divider from 'primevue/divider';

const props = defineProps<{
	model: Model;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = getParameters(props.model);
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
