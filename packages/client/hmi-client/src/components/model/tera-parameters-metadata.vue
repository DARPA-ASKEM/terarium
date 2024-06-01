<template>
	<ul v-if="isStratified">
		<li
			v-for="([baseParameterId, values], index) in collapsedParameters.entries()"
			:key="baseParameterId"
		>
			<tera-parameter-metadata-entry
				:parameter="{ id: baseParameterId }"
				is-toggleable
				:show-stratified-variables="toggleStates[index]"
				@toggle-stratified-variables="toggleStates[index] = !toggleStates[index]"
				@update-parameter-metadata="updateBaseParameter(baseParameterId, $event)"
			/>
			<ul v-if="toggleStates[index]" class="stratified">
				<li v-for="id in values" :key="id">
					<tera-parameter-metadata-entry
						:parameter="parameters.find((p) => p.id === id) ?? parameters[0]"
						is-stratified
						@update-parameter-metadata="$emit('update-parameter', { parameterId: id, ...$event })"
					/>
				</li>
			</ul>
		</li>
	</ul>
	<ul v-else>
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
import { ref } from 'vue';
import { Model } from '@/types/Types';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters, isStratifiedModel } from '@/model-representation/mira/mira';
import TeraParameterMetadataEntry from '@/components/model/tera-parameter-metadata-entry.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = getParameters(props.model);
const isStratified = isStratifiedModel(props.mmt);
const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);

const toggleStates = ref(Array.from({ length: collapsedParameters.size }, () => false));

function updateBaseParameter(baseParameter: string, event: any) {
	emit('update-parameter', { target: baseParameter, ...event }); // Write metadata func for storing base parameter stuff
	// Update stratified parameters if the event is a unit change
	const ids = collapsedParameters.get(baseParameter);
	if (ids && event.key === 'units') {
		ids.forEach((id) => {
			emit('update-parameter', { parameterId: id, ...event });
		});
	}
}
</script>

<style scoped>
li {
	padding: var(--gap-xsmall) 0;
	border-bottom: 1px solid var(--surface-border);
}

.stratified {
	margin-left: var(--gap-medium);

	& > li {
		border-left: 2px solid var(--primary-color-dark);
		padding-left: var(--gap);
		border-bottom: none;
	}
}
</style>
