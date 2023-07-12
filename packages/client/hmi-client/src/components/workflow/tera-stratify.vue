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
				@click="runStratify"
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
							@click="strataModel = null"
						/>
						<Button
							class="p-button-sm"
							label="Continue to step 2: Assign types"
							icon="pi pi-arrow-right"
							@click="stratifyStep = 2"
						/>
					</div>
					<span v-else>Define the groups you want to stratify your model with.</span>
				</div>
				<Accordion :active-index="0">
					<AccordionTab header="Model">
						<div class="step-1-inner">
							<tera-strata-model-diagram
								v-if="model"
								:model="model"
								:show-typing-toolbar="stratifyStep === 2"
								:type-system="strataModelTypeSystem"
							/>
							<div class="input">
								<label for="strata-type">Select a strata type</label>
								<Dropdown
									id="strata-type"
									v-model="strataType"
									:options="['Age groups', 'Location-travel']"
								/>
							</div>
							<section v-if="!strataModel">
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
							<section v-else>
								<tera-strata-model-diagram :model="strataModel" :show-typing-toolbar="false" />
							</section>
						</div>
					</AccordionTab>
				</Accordion>
			</section>
		</section>
		<section class="step-1" v-else-if="stratifyView === StratifyView.Output">
			<div>If this is not what you expected, go back to the input page to make changes.</div>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab header="Stratified model">
					<div class="step-1-inner">
						<tera-strata-model-diagram
							:model="stratify_output"
							:show-typing-toolbar="stratifyStep === 2"
							:type-system="strataModelTypeSystem"
						/>
					</div>
				</AccordionTab>
				<AccordionTab header="Strata model">
					<div class="step-1-inner">
						<tera-strata-model-diagram
							:model="stratify_output"
							:show-typing-toolbar="stratifyStep === 2"
							:type-system="strataModelTypeSystem"
						/>
					</div>
				</AccordionTab>
			</Accordion>
			<div>Saved as: {{ stratify_output.name }}</div>
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
import { sir_typed_aug } from '@/temp/models/sir_typed_aug';
import { flux_typed_aug } from '@/temp/models/flux_typed_aug';
import { stratify_output } from '@/temp/models/stratify_output';
import { stratify } from '@/model-representation/petrinet/petrinet-service';
import TeraStrataModelDiagram from '../models/tera-strata-model-diagram.vue';

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
	() => strataModel.value?.semantics?.typing?.type_system
);

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

async function runStratify() {
	const stratifiedModel = await stratify(sir_typed_aug, flux_typed_aug);
	console.log(stratifiedModel);
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

.stratify-button {
	margin-left: auto;
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
</style>
