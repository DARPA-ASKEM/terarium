<template>
	<main>
		<header>
			<h4>Stratify</h4>
			<span class="p-buttonset">
				<Button
					class="p-button-secondary p-button-sm"
					label="Input"
					icon="pi pi-sign-in"
					@click="stratifyView = StratifyView.Input"
					:active="stratifyView === StratifyView.Input"
				/>
				<Button
					class="p-button-secondary p-button-sm"
					label="Output"
					icon="pi pi-sign-out"
					@click="stratifyView = StratifyView.Output"
					:active="stratifyView === StratifyView.Output"
				/>
			</span>
			<Button
				class="stratify-button"
				label="Stratify"
				icon="pi pi-arrow-right"
				@click="doStratify"
				:disabled="stratifyStep !== 3"
			/>
		</header>
		<section v-if="stratifyView === StratifyView.Input">
			<nav>
				<div class="step-header" :active="stratifyStep === 1">
					<h5>Step 1</h5>
					Define strata
				</div>
				<div class="step-header" :active="stratifyStep === 2">
					<h5>Step 2</h5>
					Assign types
				</div>
				<div class="step-header" :active="stratifyStep === 3">
					<h5>Step 3</h5>
					Manage interactions
				</div>
			</nav>
			<section class="step-1">
				<div class="instructions">
					<div class="buttons" v-if="strataModel">
						<Button
							class="p-button-sm p-button-outlined"
							label="Go back"
							icon="pi pi-arrow-left"
							:disabled="stratifyStep === 0"
							@click="goBack"
						/>
						<Button
							v-if="stratifyStep === 1"
							class="p-button-sm"
							label="Continue to step 2: Assign types"
							icon="pi pi-arrow-right"
							@click="stratifyStep = 2"
						/>
						<Button
							v-if="typedBaseModel && stratifyStep === 2"
							class="p-button-sm"
							label="Continue to step 3: Manage interactions"
							icon="pi pi-arrow-right"
							@click="stratifyStep = 3"
						/>
					</div>
					<span v-else>Define the groups you want to stratify your model with.</span>
				</div>
				<div class="step-1-inner">
					<Accordion :active-index="0">
						<AccordionTab header="Model">
							<tera-typed-model-diagram
								v-if="model"
								:model="model"
								:strata-model="strataModel"
								:show-typing-toolbar="stratifyStep === 2"
								:type-system="strataModelTypeSystem"
								@model-updated="(value) => (typedBaseModel = value)"
								:show-reflexives-toolbar="stratifyStep === 3"
							/>
						</AccordionTab>
					</Accordion>
					<section v-if="!strataModel" class="generate-strata-model">
						<div class="input">
							<label for="strata-type">Select a strata type</label>
							<Dropdown
								id="strata-type"
								v-model="strataType"
								:options="['Age groups', 'Location-travel']"
							/>
						</div>
						<section>
							<div class="input">
								<label for="labels"
									>Enter a comma separated list of labels for each group. (Max 100)</label
								>
								<Textarea id="labels" v-model="labels" />
								<span><i class="pi pi-info-circle" />Or drag a CSV file into this box</span>
							</div>
							<div class="buttons">
								<Button
									class="p-button-sm p-button-outlined"
									label="Add another strata group"
									icon="pi pi-plus"
								/>
								<Button
									class="p-button-sm"
									:disabled="!(strataType && labels)"
									label="Generate strata"
									@click="generateStrataModel"
								/>
							</div>
						</section>
					</section>
					<section v-else>
						<Accordion :active-index="0">
							<AccordionTab header="Strata">
								<tera-strata-model-diagram
									:strata-model="strataModel"
									:base-model="typedBaseModel"
									:base-model-type-system="typedBaseModel?.semantics?.typing?.system"
									:show-reflexives-toolbar="stratifyStep === 3"
									@model-updated="(value) => (typedStrataModel = value)"
								/>
							</AccordionTab>
						</Accordion>
					</section>
				</div>
			</section>
		</section>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import {
	generateAgeStrataModel,
	generateLocationStrataModel
} from '@/services/models/stratification-service';
import { Model, ModelConfiguration, TypeSystem } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel } from '@/services/model';
import TeraStrataModelDiagram from '../models/tera-strata-model-diagram.vue';
import TeraTypedModelDiagram from '../models/tera-typed-model-diagram.vue';
// import { stratify } from '@/model-representation/petrinet/petrinet-service';

const props = defineProps<{
	node: WorkflowNode;
}>();

enum StratifyView {
	Input,
	Output
}
const stratifyView = ref(StratifyView.Input);
const stratifyStep = ref(1);
const strataType = ref();
const labels = ref();
const strataModel = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const model = ref<Model | null>(null);
const strataModelTypeSystem = computed<TypeSystem | undefined>(
	() => strataModel.value?.semantics?.typing?.system
);
const typedBaseModel = ref<Model | null>(null);
const typedStrataModel = ref<Model | null>(null);

function generateStrataModel() {
	if (strataType.value && labels.value) {
		const stateNames = labels.value.split(',');
		if (strataType.value === 'Age groups') {
			strataModel.value = generateAgeStrataModel(stateNames);
		} else if (strataType.value === 'Location-travel') {
			strataModel.value = generateLocationStrataModel(stateNames);
		}
	}
}

function doStratify() {
	console.log(typedBaseModel.value);
	console.log(typedStrataModel.value);
	// await stratify(typedBaseModel, )
}

function goBack() {
	if (stratifyStep.value > 0) {
		if (stratifyStep.value === 1) {
			strataModel.value = null;
		}
		stratifyStep.value--;
	}
}

watch(
	() => props.node.inputs[0],
	async () => {
		const modelConfigurationId = props.node.inputs[0].value?.[0];
		if (modelConfigurationId) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
			if (modelConfiguration.value) {
				model.value = await getModel(modelConfiguration.value.modelId);
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	height: 100%;
	width: 100%;
	overflow-y: scroll;
}

header {
	display: flex;
	gap: 1rem;
	padding: 1rem;
	align-items: center;
}

nav {
	padding-left: 1rem;
	display: flex;
}

section {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.step-header {
	display: flex;
	flex-direction: column;
	color: var(--primary-color-dark);
	font-size: var(--font-body-small);
	padding: 1rem;
	border-radius: 8px;
}

.step-header[active='true'] {
	background-color: var(--surface-highlight);
}

.step-header h5 {
	color: var(--text-color-primary);
}

.step-1 {
	padding-left: 0.5rem;
}

.step-1 > div:first-of-type {
	padding-left: 0.5rem;
}

.input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.input i {
	margin-right: 0.5rem;
}

.input span {
	color: var(--text-color-subdued);
	display: flex;
	align-items: center;
}

.step-1-inner {
	display: flex;
	gap: 1rem;
	flex-direction: column;
}

#strata-type {
	width: 50%;
}

.buttons {
	display: flex;
	justify-content: space-between;
}

:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}

.generate-strata-model {
	padding: 0 0.5rem 0 0.5rem;
}
</style>
