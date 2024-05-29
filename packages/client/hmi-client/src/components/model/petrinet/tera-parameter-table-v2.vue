<template>
	<Accordion multiple :active-index="[0]">
		<AccordionTab>
			<template #header>
				Parameters<span class="artifact-amount">({{ numParameters }})</span>
				<Button
					v-if="!isAddingUncertainty"
					label="Add uncertainty"
					text
					size="small"
					@click.stop="onAddUncertainty"
				/>
			</template>

			<!-- Adding uncertainty header -->
			<span v-if="isAddingUncertainty">
				<Button size="small" text label="Unselect all" @click="selectedParameters = []" />
				Add
				<Dropdown
					v-model="uncertaintyType"
					option-label="name"
					option-value="value"
					:options="
						distributionTypeOptions().filter((type) => type.value !== DistributionType.Constant)
					"
				/>
				uncertainty +/-
				<InputNumber v-model="uncertaintyPercentage" suffix="%" :min="0" :max="100" />
				to the selected constant values
				<Button text small icon="pi pi-check" @click="onUpdateDistributions" />
				<Button text small icon="pi pi-times" @click="isAddingUncertainty = false" />
			</span>

			<!-- Stratified -->
			<Accordion v-if="isStratified" multiple>
				<AccordionTab
					v-for="[key, values] in collapseParameters(props.mmt, props.mmtParams).entries()"
					:key="key"
				>
					<template #header>
						<span>{{ key }}</span>
						<Button label="Open Matrix" text size="small" @click.stop="matrixModalId = key" />
					</template>
					<div class="flex">
						<Divider layout="vertical" type="solid" />
						<ul>
							<li v-for="parameter in values" :key="parameter">
								<div class="flex gap-4">
									<Checkbox
										v-if="isAddingUncertainty"
										binary
										:model-value="selectedParameters.includes(parameter)"
										@change="onSelect(parameter)"
									/>
									<tera-parameter-entry
										:model-configuration="props.modelConfiguration"
										:parameter-id="parameter"
										@update-parameter="emit('update-parameters', [$event])"
										@update-source="emit('update-source', $event)"
									/>
								</div>
								<Divider type="solid" />
							</li>
						</ul>
					</div>
				</AccordionTab>
			</Accordion>

			<!-- Unstratified -->
			<ul v-else class="flex-grow">
				<li v-for="{ id } in getParameters(modelConfiguration)" :key="id">
					<div class="flex gap-4">
						<Checkbox
							v-if="isAddingUncertainty"
							binary
							:model-value="selectedParameters.includes(id)"
							@change="onSelect(id)"
						/>
						<tera-parameter-entry
							:model-configuration="modelConfiguration"
							:parameter-id="id"
							@update-parameter="emit('update-parameters', [$event])"
							@update-source="emit('update-source', $event)"
						/>
					</div>
					<Divider type="solid" />
				</li>
			</ul>
		</AccordionTab>
	</Accordion>

	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalId && isStratified"
			:id="matrixModalId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			:open-value-config="!!matrixModalId"
			@close-modal="matrixModalId = ''"
			@update-cell-value="
				emit('update-parameters', [
					{
						id: $event.variableName,
						distribution: {
							type: DistributionType.Constant,
							parameters: { value: $event.newValue }
						}
					}
				])
			"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { ModelConfiguration, ModelDistribution } from '@/types/Types';
import { getParameterDistribution, getParameters } from '@/services/model-configurations';
import { StratifiedMatrix } from '@/types/Model';
import { computed, ref } from 'vue';
import { collapseParameters, isStratifiedModel } from '@/model-representation/mira/mira';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import { DistributionType, distributionTypeOptions } from '@/services/distribution';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Checkbox from 'primevue/checkbox';
import TeraParameterEntry from './tera-parameter-entry.vue';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-parameters', 'update-source']);

const isStratified = isStratifiedModel(props.mmt);

const isAddingUncertainty = ref(false);
const uncertaintyType = ref(DistributionType.Uniform);
const uncertaintyPercentage = ref(10);
const selectedParameters = ref<string[]>([]);

const numParameters = computed(() => Object.keys(props.mmt.parameters).length);

const matrixModalId = ref('');

const onAddUncertainty = () => {
	const selected = Object.keys(props.mmt.parameters);
	selectedParameters.value = selected;
	isAddingUncertainty.value = true;
};

const onSelect = (paramId: string) => {
	console.log(paramId);
	if (selectedParameters.value.includes(paramId)) {
		selectedParameters.value.splice(selectedParameters.value.indexOf(paramId), 1);
	} else {
		selectedParameters.value.push(paramId);
	}
};

const onUpdateDistributions = () => {
	const distributions: { id: string; distribution: ModelDistribution }[] = [];
	if (uncertaintyType.value === DistributionType.Uniform) {
		selectedParameters.value.forEach((paramId) => {
			const parameter = getParameterDistribution(props.modelConfiguration, paramId);
			if (parameter.type !== DistributionType.Constant) return;
			const distribution = {
				id: paramId,
				distribution: {
					type: uncertaintyType.value,
					parameters: {
						minimum:
							parameter.parameters.value -
							(parameter.parameters.value * uncertaintyPercentage.value) / 100,
						maximum:
							parameter.parameters.value +
							(parameter.parameters.value * uncertaintyPercentage.value) / 100
					}
				}
			};
			distributions.push(distribution);
		});
	}

	emit('update-parameters', distributions);
	isAddingUncertainty.value = false;
};
</script>

<style scoped>
ul {
	flex-grow: 1;
	li {
		list-style: none;
	}
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

.artifact-amount {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}
</style>
