<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
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
		<div>
			<tera-drilldown-section :is-loading="assetLoading">
				<header class="header-group">
					<p>These equations will be used to create your model.</p>
					<Button label="Add an equation" icon="pi pi-plus" text @click="addEquation" />
				</header>
				<ul class="blocks-container">
					<li v-for="(equation, i) in clonedState.equations" :key="i">
						<tera-asset-block
							:is-included="equation.includeInProcess"
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
					<span>
						<label>Model framework</label>
						<Dropdown
							class="w-full md:w-14rem ml-2"
							v-model="clonedState.modelFramework"
							:options="modelFrameworks"
							@change="onChangeModelFramework"
						/>
					</span>
					<!--					<span class="ml-3 mr-auto">-->
					<!--						<label>Service</label>-->
					<!--						<Dropdown-->
					<!--							size="small"-->
					<!--							v-model="clonedState.modelService"-->
					<!--							:options="modelServices"-->
					<!--							@change="emit('update-state', clonedState)"-->
					<!--							class="ml-2"-->
					<!--						/>-->
					<!--					</span>-->
				</template>
			</tera-drilldown-section>
		</div>
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
				<template #footer>
					<Button
						class="mr-auto"
						label="Save as new model"
						:disabled="!selectedModel"
						outlined
						:loading="savingAsset"
						@click="isNewModelModalVisible = true"
					></Button>
					<Button label="Close" @click="emit('close')" severity="secondary" outlined size="large" />
					<Button
						label="Run"
						@click="onRun"
						:diabled="assetLoading"
						:loading="loadingModel"
						size="large"
					></Button>
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-model-modal
		:modelId="selectedModel?.id"
		:is-visible="isNewModelModalVisible"
		@close-modal="onCloseModelModal"
		@update="onAddModel"
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
import InputText from 'primevue/inputtext';
import TeraModelModal from '@/page/project/components/tera-model-modal.vue';
import { ModelServiceType } from '@/types/common';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import {
	EquationBlock,
	EquationFromImageBlock,
	instanceOfEquationFromImageBlock,
	ModelFromDocumentState
} from './model-from-document-operation';

const emit = defineEmits([
	'close',
	'update-state',
	'append-output',
	'select-output',
	'update-output-port'
]);
const props = defineProps<{
	node: WorkflowNode<ModelFromDocumentState>;
}>();

enum ModelFramework {
	Petrinet = 'petrinet',
	Regnet = 'regnet'
}

const outputs = computed(() => {
	const activeProjectModelIds = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((model) => model.id);

	const savedOutputs: WorkflowOutput<ModelFromDocumentState>[] = [];
	const unsavedOutputs: WorkflowOutput<ModelFromDocumentState>[] = [];

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

	const groupedOutputs: { label: string; items: WorkflowOutput<ModelFromDocumentState>[] }[] = [];

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

const modelFrameworks = Object.values(ModelFramework);
// const modelServices = Object.values(ModelServiceType);
const clonedState = ref<ModelFromDocumentState>({
	equations: [],
	text: '',
	modelFramework: ModelFramework.Petrinet,
	modelId: null,
	modelService: ModelServiceType.TA1
});
const document = ref<DocumentAsset | null>();
const assetLoading = ref(false);
const loadingModel = ref(false);
const selectedModel = ref<Model | null>(null);
const card = ref<Card | null>(null);
const goLLMCard = computed<any>(() => document.value?.metadata?.gollmCard);

const isNewModelModalVisible = ref(false);
const savingAsset = ref(false);
const isGeneratingCard = ref(false);
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

	const res = await equationsToAMR('latex', equations, clonedState.value.modelFramework);

	if (!res) {
		return;
	}

	const modelId = res.job_result?.tds_model_id;

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
	isNewModelModalVisible.value = false;
}

function updateNodeLabel(id: string, label: string) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.label = label;
	emit('update-output-port', outputPort);
}

function addEquation() {
	clonedState.value.equations.push({
		name: 'Equation',
		includeInProcess: true,
		asset: {
			text: ''
		}
	});
	emit('update-state', clonedState.value);
}

function removeEquation(index: number) {
	clonedState.value.equations.splice(index, 1);
	emit('update-state', clonedState.value);
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
