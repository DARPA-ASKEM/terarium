<template>
	<ul>
		<li
			v-for="({ baseParameter, childParameters, isVirtual }, index) in parameterList"
			:key="baseParameter.id"
		>
			<template v-if="isVirtual">
				<tera-parameter-metadata-entry
					:parameter="baseParameter"
					is-base
					:show-stratified-variables="toggleStates[index]"
					@toggle-stratified-variables="toggleStates[index] = !toggleStates[index]"
					@update-parameter="updateBaseParameter(baseParameter.id, $event)"
					@open-matrix="matrixModalId = baseParameter.id"
				/>
				<ul v-if="toggleStates[index]" class="stratified">
					<li v-for="childParameter in childParameters" :key="childParameter.id">
						<tera-parameter-metadata-entry
							:parameter="childParameter"
							is-stratified
							@update-parameter="
								$emit('update-parameter', { parameterId: childParameter.id, ...$event })
							"
						/>
					</li>
				</ul>
			</template>
			<tera-parameter-metadata-entry
				v-else
				:parameter="baseParameter"
				@update-parameter="$emit('update-parameter', { parameterId: baseParameter.id, ...$event })"
			/>
		</li>
	</ul>
	<teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalId"
			:id="matrixModalId"
			:mmt="props.mmt"
			:mmt-params="props.mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			is-read-only
			@close-modal="matrixModalId = ''"
		/>
	</teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Model, ModelParameter } from '@/types/Types';
import { StratifiedMatrix } from '@/types/Model';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters } from '@/model-representation/mira/mira';
import TeraParameterMetadataEntry from '@/components/model/tera-parameter-metadata-entry.vue';
import TeraStratifiedMatrixModal from './petrinet/model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = getParameters(props.model);
const collapsedParameters = collapseParameters(props.mmt, props.mmtParams);
const parameterList = Array.from(collapsedParameters.keys())
	.flat()
	.map((id) => {
		const childIds = collapsedParameters.get(id) ?? [];
		const childParameters = childIds
			.map((childId) => parameters.find((p) => p.id === childId))
			.filter(Boolean) as ModelParameter[];
		const isVirtual = childIds.length > 1;

		// If the parameter is virtual, we need to get the parameter data from model.metadata
		const baseParameter = isVirtual
			? props.model.metadata?.parameters?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
			: parameters.find((p) => p.id === id);

		return { baseParameter, childParameters, isVirtual };
	});

const toggleStates = ref(Array.from({ length: collapsedParameters.size }, () => false));
const matrixModalId = ref('');

function updateBaseParameter(baseParameter: string, event: any) {
	// In order to modify the base parameter we need to do it within the model's metadata since it doesn't actually exist in the model
	emit('update-parameter', { parameterId: baseParameter, isMetadata: true, ...event });
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
	padding-bottom: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
}

.stratified {
	gap: var(--gap-xsmall);
	margin: var(--gap-small) 0 0 var(--gap-medium);

	& > li {
		border-left: 2px solid var(--primary-color-dark);
		padding-left: var(--gap);
		padding-bottom: 0;
		border-bottom: none;
	}
}
</style>
