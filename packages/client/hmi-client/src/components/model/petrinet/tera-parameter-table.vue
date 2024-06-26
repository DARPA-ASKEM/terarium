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
				>
					<template #value>
						{{ DistributionTypeLabel[uncertaintyType].toLowerCase() }}
					</template>
					<template #option="{ option }">
						{{ option.name.toLowerCase() }}
					</template>
				</Dropdown>
				uncertainty with ±
				<InputNumber
					class="uncertainty-percentage"
					v-model="uncertaintyPercentage"
					suffix="%"
					:min="0"
					:max="100"
				/>
				bounds on the value of the selected constant parameters.
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
							<li v-for="parameterId in values" :key="parameterId">
								<div class="flex gap-4">
									<Checkbox
										v-if="
											isAddingUncertainty &&
											getParameterDistribution(modelConfiguration, parameterId).type ===
												DistributionType.Constant
										"
										binary
										:model-value="selectedParameters.includes(parameterId)"
										@change="onSelect(parameterId)"
									/>
									<tera-parameter-entry
										:model="model"
										:model-configuration="props.modelConfiguration"
										:parameter-id="parameterId"
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
				<li v-for="{ referenceId } in getParameters(modelConfiguration)" :key="referenceId">
					<div class="flex gap-4">
						<Checkbox
							v-if="
								isAddingUncertainty &&
								getParameterDistribution(modelConfiguration, referenceId).type ===
									DistributionType.Constant
							"
							binary
							:model-value="selectedParameters.includes(referenceId)"
							@change="onSelect(referenceId)"
						/>
						<tera-parameter-entry
							:model="model"
							:model-configuration="modelConfiguration"
							:parameter-id="referenceId"
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
import { Model, ModelConfiguration, ModelDistribution } from '@/types/Types';
import { getParameterDistribution, getParameters } from '@/services/model-configurations';
import { StratifiedMatrix } from '@/types/Model';
import { computed, ref } from 'vue';
import { collapseParameters, isStratifiedModel } from '@/model-representation/mira/mira';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import {
	DistributionType,
	DistributionTypeLabel,
	distributionTypeOptions
} from '@/services/distribution';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Checkbox from 'primevue/checkbox';
import TeraParameterEntry from './tera-parameter-entry.vue';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';

const props = defineProps<{
	model: Model;
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
	const selected = Object.keys(props.mmt.parameters).filter(
		(paramId) =>
			getParameterDistribution(props.modelConfiguration, paramId).type === DistributionType.Constant
	);
	selectedParameters.value = selected;
	isAddingUncertainty.value = true;
};

const onSelect = (paramId: string) => {
	if (selectedParameters.value.includes(paramId)) {
		selectedParameters.value.splice(selectedParameters.value.indexOf(paramId), 1);
	} else {
		selectedParameters.value.push(paramId);
	}
};

const onUpdateDistributions = () => {
	const distributionParameterMappings: { id: string; distribution: ModelDistribution }[] = [];
	if (uncertaintyType.value === DistributionType.Uniform) {
		selectedParameters.value.forEach((paramId) => {
			const distribution = getParameterDistribution(props.modelConfiguration, paramId);
			if (distribution.type !== DistributionType.Constant) return;

			const v = distribution.parameters.value;
			const delta = (distribution.parameters.value * uncertaintyPercentage.value) / 100;

			const distributionParameterMapping = {
				id: paramId,
				distribution: {
					type: uncertaintyType.value,
					parameters: {
						minimum: v - delta,
						maximum: v + delta
					}
				}
			};
			distributionParameterMappings.push(distributionParameterMapping);
		});
	}

	emit('update-parameters', distributionParameterMappings);
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
:deep(.uncertainty-percentage) > input {
	width: 4rem;
}
</style>
