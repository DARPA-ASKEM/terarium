<template>
	<ul>
		<li
			v-for="({ baseParameter, childParameters, isVirtual }, index) in parameterList"
			:key="index"
		>
			<template v-if="isVirtual && !isEmpty(baseOptions)">
				<section class="base">
					<span>
						<Button
							:icon="baseOptions[index].showChildren ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
							text
							rounded
							size="small"
							@click="baseOptions[index].showChildren = !baseOptions[index].showChildren"
						/>
						<h6>{{ baseParameter.id }}</h6>
					</span>
					<Button
						v-if="!baseOptions[index].isEditingChildrenUnits"
						@click="baseOptions[index].isEditingChildrenUnits = true"
						label="Add unit to all children"
						text
						size="small"
					/>
					<span v-else>
						<tera-input
							label="Unit"
							placeholder="Add a unit"
							v-model="baseOptions[index].childrenUnits"
						/>
						<Button
							icon="pi pi-check"
							text
							rounded
							size="small"
							@click="
								() => {
									updateAllChildren(baseParameter.id, 'units', baseOptions[index].childrenUnits);
									baseOptions[index].isEditingChildrenUnits = false;
								}
							"
						/>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							@click="baseOptions[index].isEditingChildrenUnits = false"
						/>
					</span>
					<Button label="Open matrix" text size="small" @click="matrixModalId = baseParameter.id" />
					<Button
						v-if="!baseOptions[index].isEditingChildrenConcepts"
						@click="baseOptions[index].isEditingChildrenConcepts = true"
						label="Add concept to all children"
						text
						size="small"
					/>
					<span v-else>
						<tera-input
							label="Concept"
							placeholder="Select a concept"
							icon="pi pi-search"
							disabled
							v-model="baseOptions[index].childrenConcepts"
						/>
						<Button
							icon="pi pi-check"
							text
							rounded
							size="small"
							@click="
								() => {
									updateAllChildren(
										baseParameter.id,
										'concept',
										baseOptions[index].childrenConcepts
									);
									baseOptions[index].isEditingChildrenConcepts = false;
								}
							"
						/>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							@click="baseOptions[index].isEditingChildrenConcepts = false"
						/>
					</span>
				</section>
				<ul v-if="baseOptions[index].showChildren" class="stratified">
					<li v-for="childParameter in childParameters" :key="childParameter.id">
						<tera-parameter-metadata-entry
							:parameter="childParameter"
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
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			is-read-only
			@close-modal="matrixModalId = ''"
		/>
	</teleport>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, computed, onMounted } from 'vue';
import { Model, ModelParameter } from '@/types/Types';
import { StratifiedMatrix } from '@/types/Model';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { getParameters } from '@/model-representation/service';
import { collapseParameters } from '@/model-representation/mira/mira';
import TeraParameterMetadataEntry from '@/components/model/tera-parameter-metadata-entry.vue';
import Button from 'primevue/button';
import TeraStratifiedMatrixModal from './petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import TeraInput from '../widgets/tera-input.vue';

const props = defineProps<{
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-parameter']);

const parameters = computed(() => getParameters(props.model));
const collapsedParameters = computed(() => collapseParameters(props.mmt, props.mmtParams));
const parameterList = computed<
	{ baseParameter: ModelParameter; childParameters: ModelParameter[]; isVirtual: boolean }[]
>(() =>
	Array.from(collapsedParameters.value.keys())
		.flat()
		.map((id) => {
			const childIds = collapsedParameters.value.get(id) ?? [];
			const childParameters = childIds
				.map((childId) => parameters.value.find((p) => p.id === childId))
				.filter(Boolean) as ModelParameter[];
			const isVirtual = childIds.length > 1;

			// If the parameter is virtual, we need to get the parameter data from model.metadata
			const baseParameter = isVirtual
				? props.model.metadata?.parameters?.[id] ?? { id } // If we haven't saved it in the metadata yet, create it
				: parameters.value.find((p) => p.id === id);

			return { baseParameter, childParameters, isVirtual };
		})
);

const matrixModalId = ref('');

const baseOptions = ref<
	{
		showChildren: boolean;
		isEditingChildrenUnits: boolean;
		isEditingChildrenConcepts: boolean;
		childrenUnits: string;
		childrenConcepts: string;
	}[]
>([]);
onMounted(() => {
	baseOptions.value = Array.from({ length: collapsedParameters.value.size }, () => ({
		showChildren: false,
		isEditingChildrenUnits: false,
		isEditingChildrenConcepts: false,
		childrenUnits: '',
		childrenConcepts: ''
	}));
});

function updateAllChildren(baseParameter: string, key: string, value: string) {
	const ids = collapsedParameters.value.get(baseParameter);
	ids?.forEach((id) => emit('update-parameter', { parameterId: id, key, value }));
}
</script>

<style scoped>
li {
	padding-bottom: var(--gap-small);
	border-bottom: 1px solid var(--surface-border);
}

.base,
.base > span {
	display: flex;
	justify-content: space-between;
	align-items: center;
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
