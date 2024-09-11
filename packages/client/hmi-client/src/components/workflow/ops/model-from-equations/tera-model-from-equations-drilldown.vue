<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #content>
			<tera-slider-panel v-model:is-open="isDocViewerOpen" header="Document Viewer" content-width="100%">
				<template #content> </template>
			</tera-slider-panel>

			<tera-slider-panel v-model:is-open="isInputOpen" header="Input" content-width="100%">
				<template #content>
					<header class="pb-2">
						<nav class="change-to-tailwind">
							<section class="ctt-checkbox">Specify which equations to use for this model.</section>
							<section>
								<Button class="mr-1" label="Reset" severity="secondary" outlined></Button>
								<Button label="Run" @click="onRun" :diabled="assetLoading" :loading="loadingModel"></Button>
							</section>
						</nav>

						<section class="header-group">
							<Textarea
								v-model="multipleEquations"
								autoResize
								rows="1"
								placeholder="Add one or more LaTex equations, or paste in a screenshot"
								class="w-full"
							/>
							<Button label="Add" @click="getEquations" class="ml-2" :disabled="isEmpty(multipleEquations)" />
						</section>

						<!-- <section class="header-group">
							<div class="inline-flex align-items-center">
								<h6>Equations</h6>
								<span class="pl-1">{{ getEquationSelectedLabel() }}</span>
							</div>
							<div>
								<Button text @click="toggleCollapseAll">{{ getCollapsedLabel() }}</Button>
								<Button text @click="toggleIncludedEquations">{{ getIncludedEquationLabel() }}</Button>
							</div>
						</section> -->
					</header>
					<Accordion :active-index="0">
						<AccordionTab header="Model equations">
							<h6 class="pb-2">Use these equation</h6>
							<ul class="blocks-container ml-3">
								<li
									v-for="(equation, i) in includedEquations"
									:key="i"
									@click.capture="selectItem(equation.name, $event)"
								>
									<input type="radio" v-model="selectedItem" :value="equation.name" style="display: none" />
									<tera-asset-block
										:is-toggleable="false"
										:is-permitted="false"
										:use-default-style="false"
										:class="selectedItem === equation.name ? 'currenly-selected' : 'asset-panel'"
									>
										<section class="ctt-checkbox">
											<Checkbox v-model="equation.includeInProcess" :binary="true" />
											<div class="block-container">
												<tera-math-editor
													v-if="equation.asset.text"
													:latex-equation="equation.asset.text"
													:is-editable="false"
												/>
												<div v-if="!equation.asset.text" class="no-extract-equation">
													{{ getEquationErrorLabel(equation) }}
												</div>
											</div>
										</section>
										<tera-input-text
											v-if="selectedItem === equation.name"
											v-model="equation.asset.text"
											placeholder="Add an expression with LaTeX"
											@update:model-value="emit('update-state', clonedState)"
										/>
									</tera-asset-block>
								</li>
							</ul>

							<h6 class="pt-3 pb-2">Other equations extracted from document</h6>
							<ul class="blocks-container ml-3">
								<li
									v-for="(equation, i) in notIncludedEquations"
									:key="i"
									@click.capture="selectItem(equation.name, $event)"
								>
									<input type="radio" v-model="selectedItem" :value="equation.name" style="display: none" />
									<tera-asset-block
										:is-toggleable="false"
										:is-permitted="false"
										:use-default-style="false"
										:class="selectedItem === equation.name ? 'currenly-selected' : 'asset-panel'"
									>
										<section class="ctt-checkbox">
											<Checkbox v-model="equation.includeInProcess" :binary="true" />
											<div class="block-container">
												<tera-math-editor
													v-if="equation.asset.text"
													:latex-equation="equation.asset.text"
													:is-editable="false"
												/>
												<div v-if="!equation.asset.text" class="no-extract-equation">
													{{ getEquationErrorLabel(equation) }}
												</div>
											</div>
										</section>
										<tera-input-text
											v-if="selectedItem === equation.name"
											v-model="equation.asset.text"
											placeholder="Add an expression with LaTeX"
											@update:model-value="emit('update-state', clonedState)"
										/>
									</tera-asset-block>
								</li>
							</ul>
						</AccordionTab>
					</Accordion>
				</template>
			</tera-slider-panel>

			<tera-slider-panel v-model:is-open="isOutputOpen" header="Output" content-width="100%">
				<template #content>
					<div class="flex align-items-center font-bold pl-3 text-lg">Equation conversions</div>
					<tera-drilldown-preview>
						<tera-model-description v-if="selectedModel" :model="selectedModel" :generating-card="isGeneratingCard" />
						<tera-operator-placeholder v-else :node="node" style="height: 100%" />
					</tera-drilldown-preview>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
	<tera-save-asset-modal
		v-if="selectedModel"
		:asset="selectedModel"
		:is-visible="showSaveModelModal"
		@close-modal="onCloseModelModal"
		@on-save="onAddModel"
	/>
</template>

<script setup lang="ts">
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { AssetBlock, WorkflowNode } from '@/types/workflow';
import Checkbox from 'primevue/checkbox';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { computed, onMounted, ref, watch } from 'vue';
import { getDocumentAsset, getEquationFromImageUrl } from '@/services/document-assets';
import type { Card, DocumentAsset, DocumentExtraction, Model } from '@/types/Types';
import { cloneDeep, isEmpty, unionBy } from 'lodash';
import { equationsToAMR, type EquationsToAMRRequest } from '@/services/knowledge';
import Button from 'primevue/button';
import { getModel, updateModel } from '@/services/model';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { useProjects } from '@/composables/project';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Textarea from 'primevue/textarea';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import { modelCard } from '@/services/goLLM';
// import * as textUtils from '@/utils/text';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import {
	// EquationBlock,
	EquationFromImageBlock,
	instanceOfEquationFromImageBlock,
	ModelFromEquationsState
} from './model-from-equations-operation';

const emit = defineEmits(['close', 'update-state', 'append-output', 'select-output', 'update-output-port']);
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

const selectedOutputId = ref<string>('');

// const modelFrameworks = Object.entries(ModelFramework).map(([key, value]) => ({
// 	label: textUtils.pascalCaseToCapitalSentence(key),
// 	value,
// 	disabled: [ModelFramework.Decapode, ModelFramework.GeneralizedAMR, ModelFramework.MathExpressionTree].includes(value)
// }));

const clonedState = ref<ModelFromEquationsState>({
	equations: [],
	text: '',
	modelFramework: ModelFramework.PetriNet,
	modelId: null
});

const includedEquations = computed(() =>
	clonedState.value.equations.filter((equation) => equation.includeInProcess === true)
);
const notIncludedEquations = computed(() =>
	clonedState.value.equations.filter((equation) => equation.includeInProcess === false)
);
const selectedItem = ref('');

const selectItem = (item, event) => {
	selectedItem.value = item;

	// Prevent the child’s click handler from firing
	event.stopImmediatePropagation();
};

const document = ref<DocumentAsset | null>();
const assetLoading = ref(false);
const loadingModel = ref(false);
const selectedModel = ref<Model | null>(null);
const card = ref<Card | null>(null);
const goLLMCard = computed<any>(() => document.value?.metadata?.gollmCard);

const showSaveModelModal = ref(false);
const isGeneratingCard = ref(false);
const multipleEquations = ref<string>('');

const isDocViewerOpen = ref(true);
const isInputOpen = ref(true);
const isOutputOpen = ref(true);

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);
	if (selectedOutputId.value) {
		onSelection(selectedOutputId.value);
	}

	const documentId = props.node.inputs?.[0]?.value?.[0]?.documentId;
	const equations: AssetBlock<DocumentExtraction>[] = props.node.inputs?.[0]?.value?.[0]?.equations?.filter(
		(e) => e.includeInProcess
	);
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

		let extractedEquations = state.equations.filter((e) => instanceOfEquationFromImageBlock(e.asset));
		extractedEquations = unionBy(newEquations, extractedEquations, 'asset.fileName');
		const inputEquations = state.equations.filter((e) => !instanceOfEquationFromImageBlock(e.asset));
		state.equations = [...extractedEquations, ...inputEquations];
		state.text = document.value?.text ?? '';
		emit('update-state', state);
	}
	assetLoading.value = false;
});

// function onUpdateInclude(asset: AssetBlock<EquationBlock | EquationFromImageBlock>) {
// 	asset.includeInProcess = !asset.includeInProcess;
// 	emit('update-state', clonedState.value);
// }

const onSelection = (id: string) => {
	emit('select-output', id);
};

async function onRun() {
	const equations = clonedState.value.equations
		.filter((e) => e.includeInProcess && !e.asset.extractionError)
		.map((e) => e.asset.text);

	const request: EquationsToAMRRequest = {
		equations,
		framework: clonedState.value.modelFramework,
		documentId: document.value?.id
	};
	const modelId = await equationsToAMR(request);
	if (!modelId) return;

	if (document.value?.id) await generateCard(document.value.id);

	clonedState.value.modelId = modelId;
	emit('append-output', {
		label: `Output - ${props.node.outputs.length + 1}`,
		state: cloneDeep(clonedState.value),
		isSelected: false,
		type: 'modelId',
		value: [clonedState.value.modelId]
	});
}

// function onChangeModelFramework() {
// 	emit('update-state', clonedState.value);
// }

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

		if (useProjects().hasEditPermission()) {
			model = await updateModel(model);
		}
	}
	card.value = model?.metadata?.card ?? null;
	selectedModel.value = model;
	loadingModel.value = false;
}

// since AWS links expire we need to use the refetched document image urls to display the images
// function getAssetUrl(asset: AssetBlock<EquationFromImageBlock>): string {
// 	const foundAsset = document.value?.assets?.find((a) => a.fileName === asset.asset.fileName);
// 	if (!foundAsset) return '';
// 	return foundAsset.metadata?.url;
// }

function onAddModel(model: Model) {
	if (!model?.name || !selectedOutputId.value) return;
	updateNodeLabel(selectedOutputId.value, model.name);
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

// function removeEquation(index: number) {
// 	clonedState.value.equations.splice(index, 1);
// 	emit('update-state', clonedState.value);
// }

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
	multipleEquations.value = '';
}

// const allEquationCollapsed = computed(() => !clonedState.value.equations.some((equation) => !equation.isCollapsed));

// const allEquationsInProcess = computed(
// 	() => !clonedState.value.equations.some((equation) => !equation.includeInProcess)
// );

// const selectedEquations = computed(() => clonedState.value.equations.filter((equation) => equation.includeInProcess));

function getEquationErrorLabel(equation) {
	return equation.asset.extractionError ? "Couldn't extract equation" : '';
}

// function getEquationSelectedLabel() {
// 	const total = clonedState.value.equations.length;
// 	return `(${selectedEquations.value.length}/${total} selected)`;
// }

// function getCollapsedLabel() {
// 	return allEquationCollapsed.value ? 'Expand All' : 'Collapse all';
// }

// function getIncludedEquationLabel() {
// 	return allEquationsInProcess.value ? 'Remove all from process' : 'Include all in process';
// }

// function changeCollapsed(equation, isCollapsed = false) {
// 	equation.isCollapsed = isCollapsed;
// }

// function toggleCollapseAll() {
// 	const collapseEquations = allEquationCollapsed.value;
// 	clonedState.value.equations.forEach((equation) => {
// 		changeCollapsed(equation, !collapseEquations);
// 	});
// }

// function toggleIncludedEquations() {
// 	const allEquationsIncluded = allEquationsInProcess.value;
// 	clonedState.value.equations.forEach((equation) => {
// 		equation.includeInProcess = !allEquationsIncluded;
// 	});
// }

// generates the model card and fetches the model when finished
async function generateCard(docId: string) {
	if (!docId) return;
	isGeneratingCard.value = true;
	await modelCard(docId);
	isGeneratingCard.value = false;
	await fetchModel();
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
:deep(.p-panel-header) {
	display: none;
}

.no-extract-equation {
	padding: 1rem;
	background: #f5f5f5;
	font-size: 12px;
	color: gray;
	border-radius: 3px;
}

.change-to-tailwind {
	display: flex;
	justify-content: space-between;
	padding-left: var(--gap-small);
	padding-right: var(--gap-small);
	padding-bottom: 1em;
}

.currenly-selected {
	padding-top: 0.75rem;
	border-radius: var(--border-radius-medium);
	border: 1px solid var(--surface-border-light);
	border-left: 0.25rem solid var(--primary-color);
}
.asset-panel {
	padding-top: 0.75rem;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
}

.ctt-checkbox {
	display: flex;
	align-items: center;
	padding-bottom: 0.25em;
	padding-top: 0.25em;
}

.block-container {
	display: flex;
	flex-direction: column;
}

.equation-image {
	border-style: dashed;
	border-color: var(--primary-color);
	border-width: thin;
}

.header-group {
	display: flex;
	padding-left: 0.75em;
	padding-right: 0.5em;
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
		/* margin-bottom: var(--gap-small); */
	}
}

.p-panel:deep(.p-panel-footer) {
	display: none;
}
</style>
