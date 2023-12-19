<template>
	<main>
		<header>
			<SelectButton
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:disabled="!stratifiedModel"
				:options="viewOptions"
				option-value="value"
			>
				<template #option="{ option }">
					<i :class="`${option.icon} p-button-icon-left`" />
					<span class="p-button-label">{{ option.value }}</span>
				</template>
			</SelectButton>
			<div class="buttons" v-if="strataModel && view === StratifyView.Input">
				<Button
					class="p-button-outlined"
					label="Go back"
					icon="pi pi-arrow-left"
					:disabled="stratifyStep === 0"
					@click="goBack"
				/>
				<Button
					v-if="stratifyStep === 1"
					label="Continue to step 2: Assign types"
					icon="pi pi-arrow-right"
					iconPos="right"
					@click="stratifyStep = 2"
				/>
				<Button
					v-if="typedBaseModel && stratifyStep === 2"
					label="Continue to step 3: Manage interactions"
					icon="pi pi-arrow-right"
					iconPos="right"
					@click="stratifyStep = 3"
				/>
				<Button
					v-if="stratifyStep === 3"
					class="stratify-button p-button-sm"
					label="Stratify"
					icon="pi pi-arrow-right"
					iconPos="right"
					@click="doStratify"
				/>
			</div>
		</header>
		<section v-if="view === StratifyView.Input">
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
					<span v-if="stratifyStep === 1"
						>Define the groups you want to stratify your model with.</span
					>
					<span v-if="stratifyStep === 2 && numUntypedNodes > 0"
						>Assign types to each of the nodes in your model.
					</span>
					<span v-if="stratifyStep === 2 && numUntypedNodes > 0" class="nodes-require-types"
						>{{ numUntypedNodes }} nodes require types</span
					>
					<span v-if="stratifyStep === 2 && numUntypedNodes === 0">All nodes have types.</span>
					<span v-if="stratifyStep === 3"
						>Make sure all behaviour in the original model are in the stratified model in order for
						them to be part of the final model.</span
					>
				</div>
				<div class="step-1-inner">
					<Accordion :active-index="0">
						<AccordionTab header="Model">
							<tera-typed-model-diagram
								v-if="model"
								:model="model"
								:strata-model="strataModel"
								:show-typing-toolbar="stratifyStep === 2"
								:type-system="strataModel?.semantics?.typing?.system.model"
								@model-updated="(value) => (typedBaseModel = value)"
								@all-nodes-typed="(value) => onAllNodesTyped(value)"
								@not-all-nodes-typed="
									(value) => {
										typedBaseModel = null;
										numUntypedNodes = value;
									}
								"
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
									class="p-button-outlined"
									label="Add another strata group"
									icon="pi pi-plus"
								/>
								<Button
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
									:base-model-type-system="typedBaseModel?.semantics?.typing?.system.model"
									:show-reflexives-toolbar="stratifyStep === 3"
									@model-updated="(value) => (typedStrataModel = value)"
								/>
							</AccordionTab>
						</Accordion>
					</section>
				</div>
			</section>
		</section>
		<section class="output" v-else-if="view === StratifyView.Output">
			<div>If this is not what you expected, go back to the input page to make changes.</div>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab header="Stratified model">
					<tera-stratify-output-model-diagram v-if="stratifiedModel" :model="stratifiedModel" />
				</AccordionTab>
				<AccordionTab header="Strata model">
					<tera-strata-model-diagram
						v-if="typedStrataModel"
						:strata-model="typedStrataModel"
						:show-reflexives-toolbar="false"
					/>
				</AccordionTab>
			</Accordion>
			<div>
				Saved as:
				<Button
					class=".p-button-link"
					:label="stratifiedModel?.header.name"
					icon="pi pi-pencil"
					iconPos="right"
					text
					@click="
						emit('open-asset', {
							pageType: AssetType.Model,
							assetId: stratifiedModel?.id ?? ''
						})
					"
				/>
			</div>
			<div class="add-model-config">
				<Button
					class="p-button-sm"
					label="Add configure model to the workflow"
					icon="pi pi-plus"
					@click="
						workflowEventBus.emit('add-node', {
							workflowId: node.workflowId,
							operation: ModelOperation,
							position: { x: node.x, y: node.y + node.height + 8 },
							state: { modelId: stratifiedModel?.id }
						})
					"
				/>
			</div>
		</section>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import {
	generateAgeStrataModel,
	generateLocationStrataModel
} from '@/services/models/stratification-service';
import { AssetType, Model, ModelConfiguration } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel, createModel, reconstructAMR } from '@/services/model';
import { stratify } from '@/model-representation/petrinet/petrinet-service';
import { workflowEventBus } from '@/services/workflow';
import { ModelOperation } from '@/workflow/ops/model/model-operation';
import TeraStrataModelDiagram from '@/components/model/petrinet/model-diagrams/tera-strata-model-diagram.vue';
import TeraTypedModelDiagram from '@/components/model/petrinet/model-diagrams/tera-typed-model-diagram.vue';
import TeraStratifyOutputModelDiagram from '@/components/model/petrinet/model-diagrams/tera-stratify-output-model-diagram.vue';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';

const props = defineProps<{
	node: WorkflowNode<any>;
}>();

const emit = defineEmits(['open-asset', 'add-model-config']);

enum StratifyView {
	Input = 'Input',
	Output = 'Output'
}
const view = ref(StratifyView.Input);
const viewOptions = ref([
	{ value: StratifyView.Input, icon: 'pi pi-sign-in' },
	{ value: StratifyView.Output, icon: 'pi pi-sign-out' }
]);
const stratifyStep = ref(1);
const strataType = ref();
const labels = ref();
const strataModel = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const model = ref<Model | null>(null);
const typedBaseModel = ref<Model | null>(null);
const typedStrataModel = ref<Model | null>(null);
const stratifiedModel = ref<Model | null>();
const numUntypedNodes = ref();
const initialState = {
	view: StratifyView.Input,
	stratifyStep: 1,
	strataType: null,
	labels: null,
	strataModel: null,
	model: null,
	typedBaseModel: null,
	typedStrataModel: null,
	stratifiedModel: null,
	numUntypedNodes: -1
};

function restoreState(state) {
	view.value = state.view;
	stratifyStep.value = state.stratifyStep;
	strataModel.value = state.strataModel;
	strataType.value = state.strataType;
	labels.value = state.labels;
	model.value = state.model;
	typedBaseModel.value = state.typedBaseModel;
	typedStrataModel.value = state.typedStrataModel;
	stratifiedModel.value = state.stratifiedModel;
	numUntypedNodes.value = state.numUntypedNodes;
}

function onAllNodesTyped(value: Model) {
	typedBaseModel.value = value;
	numUntypedNodes.value = 0;
}

function generateStrataModel() {
	if (strataType.value && labels.value) {
		const stateNames = labels.value.split(',');
		if (strataType.value === 'Age groups') {
			strataModel.value = generateAgeStrataModel(stateNames);
		} else if (strataType.value === 'Location-travel') {
			strataModel.value = generateLocationStrataModel(stateNames);
		}
		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state: { ...props.node.state, strataModel: strataModel.value }
		});
		restoreState(props.node.state);
	}
}

async function doStratify() {
	if (typedBaseModel.value && typedStrataModel.value) {
		const amrBase = (await stratify(typedBaseModel.value, typedStrataModel.value)) as Model;
		const amr = (await reconstructAMR({ model: amrBase })) as Model;

		// Put typing and span back in
		if (amr.semantics && amr.semantics.ode) {
			amr.semantics.span = _.cloneDeep(amrBase.semantics?.span);
			amr.semantics.typing = _.cloneDeep(amrBase.semantics?.typing);
		}
		stratifiedModel.value = amr;
		// Create model and asssociate
		const response = await createModel(amr);
		if (response) {
			stratifiedModel.value.id = response.id;
		}
		const newModelId = response?.id;
		if (newModelId) {
			await useProjects().addAsset('models', newModelId);
			view.value = StratifyView.Output;

			workflowEventBus.emitNodeStateChange({
				workflowId: props.node.workflowId,
				nodeId: props.node.id,
				state: {
					...props.node.state,
					view: view.value,
					stratifiedModel: stratifiedModel.value,
					typedStrataModel: typedStrataModel.value
				}
			});
		}
	}
}

function goBack() {
	if (stratifyStep.value > 1) {
		stratifyStep.value--;
	} else {
		strataModel.value = null;
		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state: { ...props.node.state, strataModel: strataModel.value }
		});
	}
}

watch(stratifyStep, () => {
	const state = { ...props.node.state, stratifyStep: stratifyStep.value };
	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
});

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
		if (!props.node.state) {
			workflowEventBus.emitNodeStateChange({
				workflowId: props.node.workflowId,
				nodeId: props.node.id,
				state: { ...initialState, model: model.value }
			});
		}
		restoreState(props.node.state);
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
	gap: 1.5rem;
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
	width: 24rem;
}

.buttons {
	display: flex;
	justify-content: space-between;
	margin-left: auto;
	gap: 1rem;
}

:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}

.generate-strata-model {
	padding: 0 0.5rem 0 0.5rem;
}

.output {
	padding-left: 0.5rem;
}

.output div:not(.p-accordion) {
	padding-left: 0.5rem;
}

:deep(.p-accordion .p-accordion-content) {
	padding: 0.5rem;
}

.saved-model-name {
	color: var(--text-color-primary);
	text-decoration: underline;
}

.p-button.p-button-text {
	padding: 0;
}

.add-model-config {
	display: flex;
}

.nodes-require-types {
	font-weight: var(--font-weight-semibold);
}

.buttons .p-button {
	white-space: nowrap;
}
</style>
