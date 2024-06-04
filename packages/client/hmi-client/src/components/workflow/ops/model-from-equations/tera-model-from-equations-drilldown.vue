<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
			<tera-output-dropdown
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="assetLoading"
				is-selectable
			/>
		</template>
		<tera-drilldown-section :is-loading="assetLoading">
			<template #header-controls-left>
				<section class="align-items-center pl-3">
					<h5>Equation conversions</h5>
					<p>These equations will be used to create your model.</p>
				</section>
			</template>
			<template #header-controls-right>
				<p class="inline-flex align-items-center">Framework</p>
				<Dropdown
					class="w-full md:w-14rem ml-2"
					v-model="clonedState.modelFramework"
					:options="modelFrameworks"
					option-label="label"
					option-value="value"
					option-disabled="disabled"
					@change="onChangeModelFramework"
				/>
				<!--				<Button-->
				<!--					class="mr-auto"-->
				<!--					label="Save as new model"-->
				<!--					:disabled="!selectedModel"-->
				<!--					outlined-->
				<!--					:loading="savingAsset"-->
				<!--					@click="showSaveModelModal = true"-->
				<!--				></Button>-->
				<!--				<Button label="Close" @click="emit('close')" severity="secondary" outlined size="large" />-->
				<Button label="Run" @click="onRun" :diabled="assetLoading" :loading="loadingModel"></Button>
			</template>
			<header class="header-group pl-3">
				<Textarea
					v-model="multipleEquations"
					autoResize
					rows="1"
					cols="85"
					placeholder="Add an expression(s) with LaTex"
				/>
				<Button label="Add" @click="getEquations" />
			</header>
			<section>
				<Button text @click="toggleCollapseAll">{{ getCollapsedLabel() }}</Button>
				<Button text @click="toggleIncludedEquations">{{ getIncludedEquationLabel() }}</Button>
			</section>
			<ul class="blocks-container ml-3">
				<li v-for="(equation, i) in clonedState.equations" :key="i">
					<tera-asset-block
						:is-included="equation.includeInProcess"
						:collapsed="equation.collapsed"
						@update:collapsed="(isCollapsed) => changeCollapsed(equation, isCollapsed)"
						@update:is-included="onUpdateInclude(equation)"
						:is-deletable="!instanceOfEquationFromImageBlock(equation.asset)"
						@delete="removeEquation(i)"
					>
						<template #header>
							<h5>{{ equation.name }}</h5>
						</template>
						<div class="block-container">
							<template v-if="instanceOfEquationFromImageBlock(equation.asset)">
								<label>Extracted Image:</label>
								<Image
									id="img"
									:src="getAssetUrl(equation as AssetBlock<EquationFromImageBlock>)"
									:alt="''"
									preview
								/>
							</template>
							<tera-math-editor
								v-if="equation.asset.text"
								:latex-equation="equation.asset.text"
								:is-editable="false"
							/>
							<div v-else class="mt-2" />
							<InputText
								v-model="equation.asset.text"
								placeholder="Add an expression with LaTeX"
								@update:model-value="emit('update-state', clonedState)"
							/>
						</div>
					</tera-asset-block>
				</li>
			</ul>
			<template #footer>
				<span class="mb-2"> </span>
			</template>
		</tera-drilldown-section>
		<template #preview>
			<tera-drilldown-preview>
				<section v-if="selectedModel">
					<tera-model-description
						:model="selectedModel"
						:feature-config="{
							isPreview: true
						}"
						:generating-card="isGeneratingCard"
					/>
				</section>
				<tera-operator-placeholder
					v-else
					:operation-type="node.operationType"
					style="height: 100%"
				/>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-save-asset-modal
		v-if="selectedModel"
		:model="selectedModel"
		:is-visible="showSaveModelModal"
		@close-modal="onCloseModelModal"
		@on-save="onAddModel"
	/>
</template>

<script setup lang="ts">
import { AssetBlock, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { computed, onMounted, ref, watch } from 'vue';
import { getDocumentAsset, getEquationFromImageUrl } from '@/services/document-assets';
import type { Card, DocumentAsset, DocumentExtraction, Model } from '@/types/Types';
import { AssetType } from '@/types/Types';
import { cloneDeep, isEmpty, unionBy } from 'lodash';
import Image from 'primevue/image';
import { equationsToAMR } from '@/services/knowledge';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { generateModelCard, getModel, updateModel } from '@/services/model';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { useProjects } from '@/composables/project';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import TeraSaveAssetModal from '@/page/project/components/tera-save-asset-modal.vue';
import { ModelServiceType } from '@/types/common';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';

import * as textUtils from '@/utils/text';
import {
	EquationBlock,
	EquationFromImageBlock,
	instanceOfEquationFromImageBlock,
	ModelFromEquationsState
} from './model-from-equations-operation';

const emit = defineEmits([
	'close',
	'update-state',
	'append-output',
	'select-output',
	'update-output-port'
]);
const props = defineProps<{
	node: WorkflowNode<ModelFromEquationsState>;
}>();

enum ModelFramework {
	PetriNet = 'petrinet',
	RegNet = 'regnet',
	Decapode = 'decapode',
	GeneralizedAMR = 'gamr',
	MathExpressionTree = 'met'
}

const outputs = computed(() => {
	const activeProjectModelIds = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((model) => model.id);

	const savedOutputs: WorkflowOutput<ModelFromEquationsState>[] = [];
	const unsavedOutputs: WorkflowOutput<ModelFromEquationsState>[] = [];

	props.node.outputs.forEach((output) => {
		const modelId = output.state?.modelId;
		if (modelId) {
			const isSaved = activeProjectModelIds?.includes(modelId);
			if (isSaved) {
				savedOutputs.push(output);
				return;
			}
		}
		unsavedOutputs.push(output);
	});

	const groupedOutputs: { label: string; items: WorkflowOutput<ModelFromEquationsState>[] }[] = [];

	if (!isEmpty(unsavedOutputs)) {
		groupedOutputs.push({
			label: 'Select outputs to display in operator',
			items: unsavedOutputs
		});
	}
	if (!isEmpty(savedOutputs)) {
		groupedOutputs.push({
			label: 'Saved models',
			items: savedOutputs
		});
	}

	return groupedOutputs;
});

const selectedOutputId = ref<string>('');

const modelFrameworks = Object.entries(ModelFramework).map(([key, value]) => ({
	label: textUtils.pascalCaseToCapitalSentence(key),
	value,
	disabled: [
		ModelFramework.Decapode,
		ModelFramework.GeneralizedAMR,
		ModelFramework.MathExpressionTree
	].includes(value)
}));

const clonedState = ref<ModelFromEquationsState>({
	equations: [],
	text: '',
	modelFramework: ModelFramework.PetriNet,
	modelId: null,
	modelService: ModelServiceType.TA1
});
const document = ref<DocumentAsset | null>();
const assetLoading = ref(false);
const loadingModel = ref(false);
const selectedModel = ref<Model | null>(null);
const card = ref<Card | null>(null);
const goLLMCard = computed<any>(() => document.value?.metadata?.gollmCard);

const showSaveModelModal = ref(false);
// const savingAsset = ref(false);
const isGeneratingCard = ref(false);
const multipleEquations = ref<string>('');

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);
	if (selectedOutputId.value) {
		onSelection(selectedOutputId.value);
	}

	const documentId = props.node.inputs?.[0]?.value?.[0]?.documentId;
	const equations: AssetBlock<DocumentExtraction>[] =
		props.node.inputs?.[0]?.value?.[0]?.equations?.filter((e) => e.includeInProcess);
	assetLoading.value = true;
	if (documentId) {
		document.value = await getDocumentAsset(documentId);

		const state = cloneDeep(props.node.state);

		// we want to add any new equation from images to the current state without running the image -> equations for the ones that already ran
		const nonRunEquations = equations?.filter((e) => {
			const foundEquation = state.equations.find(
				(eq) => instanceOfEquationFromImageBlock(eq.asset) && eq.asset.fileName === e.asset.fileName
			);
			return !foundEquation;
		});
		const promises =
			nonRunEquations?.map(async (e) => {
				const equationText = await getEquationFromImageUrl(documentId, e.asset.fileName);
				const equationBlock: EquationFromImageBlock = {
					...e.asset,
					text: equationText ?? '',
					extractionError: !equationText
				};

				const assetBlock: AssetBlock<EquationFromImageBlock> = {
					name: e.name,
					includeInProcess: e.includeInProcess,
					asset: equationBlock
				};

				return assetBlock;
			}) ?? [];

		const newEquations = await Promise.all(promises);

		let extractedEquations = state.equations.filter((e) =>
			instanceOfEquationFromImageBlock(e.asset)
		);
		extractedEquations = unionBy(newEquations, extractedEquations, 'asset.fileName');
		const inputEquations = state.equations.filter(
			(e) => !instanceOfEquationFromImageBlock(e.asset)
		);
		state.equations = [...extractedEquations, ...inputEquations];
		state.text = document.value?.text ?? '';
		emit('update-state', state);
	}
	assetLoading.value = false;
});

function onUpdateInclude(asset: AssetBlock<EquationBlock | EquationFromImageBlock>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update-state', clonedState.value);
}

const onSelection = (id: string) => {
	emit('select-output', id);
};

async function onRun() {
	const equations = clonedState.value.equations
		.filter((e) => e.includeInProcess && !e.asset.extractionError)
		.map((e) => e.asset.text);

	const modelId = await equationsToAMR(equations, clonedState.value.modelFramework);
	if (!modelId) return;

	generateCard(document.value?.id, modelId);

	clonedState.value.modelId = modelId;
	emit('append-output', {
		label: `Output - ${props.node.outputs.length + 1}`,
		state: cloneDeep(clonedState.value),
		isSelected: false,
		type: 'modelId',
		value: [clonedState.value.modelId]
	});
}

function onChangeModelFramework() {
	emit('update-state', clonedState.value);
}

async function fetchModel() {
	if (!clonedState.value.modelId) {
		selectedModel.value = null;
		return;
	}
	loadingModel.value = true;
	let model = await getModel(clonedState.value.modelId);

	if (model) {
		if (!model.metadata) {
			model.metadata = {};
		}

		if (!model.metadata?.card && card.value) {
			model.metadata.card = card.value;
		}

		if (!model.metadata?.gollmCard && goLLMCard.value) {
			model.metadata.gollmCard = goLLMCard.value;
		}

		model = await updateModel(model);
	}
	card.value = model?.metadata?.card ?? null;
	selectedModel.value = model;
	loadingModel.value = false;
}

// since AWS links expire we need to use the refetched document image urls to display the images
function getAssetUrl(asset: AssetBlock<EquationFromImageBlock>): string {
	const foundAsset = document.value?.assets?.find((a) => a.fileName === asset.asset.fileName);
	if (!foundAsset) return '';
	return foundAsset.metadata?.url;
}

function onAddModel(modelName: string) {
	if (!modelName || !selectedOutputId.value) return;
	updateNodeLabel(selectedOutputId.value, modelName);
}

function onCloseModelModal() {
	showSaveModelModal.value = false;
}

function updateNodeLabel(id: string, label: string) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.label = label;
	emit('update-output-port', outputPort);
}

// function addEquation() {
// 	clonedState.value.equations.push({
// 		name: 'Equation',
// 		collapsed: false,
// 		includeInProcess: true,
// 		asset: {
// 			text: ''
// 		}
// 	});
// 	emit('update-state', clonedState.value);
// }

function removeEquation(index: number) {
	clonedState.value.equations.splice(index, 1);
	emit('update-state', clonedState.value);
}

function getEquations() {
	const newEquations = multipleEquations.value.split('\n');
	newEquations.forEach((equation) => {
		clonedState.value.equations.push({
			name: 'Equation',
			includeInProcess: true,
			asset: {
				text: equation
			}
		});
	});
	emit('update-state', clonedState.value);
}

const allEquationCollapsed = computed(
	() => !clonedState.value.equations.some((equation) => !equation.collapsed)
);

const allEquationsInProcess = computed(
	() => !clonedState.value.equations.some((equation) => !equation.includeInProcess)
);

function getCollapsedLabel() {
	return allEquationCollapsed.value ? 'Expand All' : 'Collapse all';
}

function getIncludedEquationLabel() {
	return allEquationsInProcess.value ? 'Remove all from process' : 'Include all in process';
}

function changeCollapsed(equation, isCollapsed = false) {
	equation.collapsed = isCollapsed;
}

function toggleCollapseAll() {
	const collapseEquations = allEquationCollapsed.value;
	clonedState.value.equations.forEach((equation) => {
		changeCollapsed(equation, !collapseEquations);
	});
}

function toggleIncludedEquations() {
	const allEquationsIncluded = allEquationsInProcess.value;
	clonedState.value.equations.forEach((equation) => {
		equation.includeInProcess = !allEquationsIncluded;
	});
}

// generates the model card and fetches the model when finished
async function generateCard(docId, modelId) {
	if (!docId || !modelId) return;
	//
	// if (clonedState.value.modelService === ModelServiceType.TA1 && card.value) {
	// 	return;
	// }
	//
	// if (clonedState.value.modelService === ModelServiceType.TA4 && goLLMCard.value) {
	// 	return;
	// }

	isGeneratingCard.value = true;
	// await generateModelCard(docId, modelId, clonedState.value.modelService);
	await generateModelCard(docId, modelId, ModelServiceType.TA1);
	isGeneratingCard.value = false;
	fetchModel();
}

watch(
	() => props.node.state,
	() => {
		clonedState.value = cloneDeep(props.node.state);
	},
	{ deep: true }
);

// watch for model id changes on state
watch(
	() => clonedState.value.modelId,
	async () => {
		await fetchModel();
	}
);

watch(
	() => props.node.active,
	() => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
:deep(.p-panel section) {
	display: flex;
	align-items: flex-start;
}

.block-container {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
}

.header-group {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.equation-view {
	display: flex;
	gap: var(--gap-small);
	flex-direction: column;
	overflow-y: hidden;
}

.blocks-container {
	overflow-y: auto;

	> li:not(:last-child) {
		margin-bottom: var(--gap-small);
	}
}

.p-panel:deep(.p-panel-footer) {
	display: none;
}
</style>
