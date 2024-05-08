<template>
	<tera-drilldown
		:node="node"
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
				:is-loading="isProcessing"
				is-selectable
			/>
		</template>
		<div tabName="Wizard" class="p-3">
			<tera-drilldown-section :isLoading="fetchingInputBlocks">
				<header>
					<section class="flex items-center gap-3">
						<Dropdown
							class="w-full md:w-14rem"
							v-model="clonedState.codeLanguage"
							:options="programmingLanguages"
							@change="setKernelContext"
						/>
						<span
							><label>Model framework</label
							><Dropdown
								size="small"
								v-model="clonedState.modelFramework"
								:options="modelFrameworks"
								@change="setKernelContext"
						/></span>
						<span class="mr-auto">
							<label>Service</label>
							<Dropdown
								size="small"
								v-model="clonedState.modelService"
								:options="modelServices"
								@change="emit('update-state', clonedState)"
							/>
						</span>
						<Button
							label="Add code block"
							icon="pi pi-plus"
							text
							@click="addCodeBlock"
							:disabled="
								clonedState.modelFramework === ModelFramework.Decapodes && !isEmpty(allCodeBlocks)
							"
						/>
					</section>
				</header>
				<tera-operator-placeholder
					v-if="allCodeBlocks.length === 0"
					:operation-type="node.operationType"
					style="height: 100%"
				>
					Please attach a code asset.
				</tera-operator-placeholder>
				<template v-else>
					<tera-asset-block
						v-for="({ name, asset }, i) in allCodeBlocks"
						:key="i"
						:is-editable="asset.type !== CodeBlockType.INPUT"
						:is-deletable="asset.type !== CodeBlockType.INPUT"
						@delete="removeCodeBlock(i - inputCodeBlocks.length)"
						:is-included="allCodeBlocks[i].includeInProcess"
						@update:is-included="
							allCodeBlocks[i].includeInProcess = !allCodeBlocks[i].includeInProcess;
							emit('update-state', clonedState);
						"
					>
						<template #header>
							<h5>{{ name }}</h5>
						</template>
						<v-ace-editor
							v-model:value="allCodeBlocks[i].asset.codeContent"
							@update:value="emit('update-state', clonedState)"
							:lang="asset.codeLanguage"
							theme="chrome"
							style="height: calc(100vh - 25rem); width: 100%"
							class="ace-editor"
							:readonly="asset.type === CodeBlockType.INPUT"
							:options="{ showPrintMargin: false }"
						/>
					</tera-asset-block>
				</template>
			</tera-drilldown-section>
		</div>
		<div tabName="Notebook">
			<!--Notebook section if we decide we need one-->
		</div>
		<template #preview>
			<tera-drilldown-preview :is-loading="isProcessing" class="pt-3 pb-2 pl-2 pr-4">
				<section v-if="selectedModel">
					<template v-if="selectedOutput?.state?.modelFramework === ModelFramework.Petrinet">
						<tera-model-description
							:model="selectedModel"
							:feature-config="{
								isPreview: true
							}"
							:is-generating-card="isGeneratingCard"
						/>
					</template>
					<template v-if="selectedOutput?.state?.modelFramework === ModelFramework.Decapodes">
						<span>Decapodes created: {{ selectedModel?.id ?? '' }}</span>
					</template>
				</section>
				<tera-operator-placeholder
					v-else
					:operation-type="node.operationType"
					style="height: 100%"
				/>
				<template #footer>
					<Button
						:loading="savingAsset"
						:disabled="isSaveModelDisabled()"
						label="Save as new model"
						severity="secondary"
						outlined
						@click="openModal"
						class="mr-auto"
					/>
					<Button label="Cancel" severity="secondary" @click="emit('close')" outlined />
					<Button
						:disabled="isProcessing || allCodeBlocks.length === 0"
						label="Run"
						icon="pi pi-play"
						@click="handleCode"
					/>
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-save-model-modal
		v-if="selectedModel"
		:model="selectedModel"
		:is-visible="showSaveModelModal"
		@close-modal="onCloseModelModal"
		@on-save="onAddModel"
	/>
</template>

<script setup lang="ts">
import '@/ace-config';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';

import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { useProjects } from '@/composables/project';
import TeraSaveModelModal from '@/page/project/components/tera-save-model-modal.vue';
import { getCodeAsset } from '@/services/code';
import { getDocumentAsset } from '@/services/document-assets';
import { KernelSessionManager } from '@/services/jupyter';
import { codeBlocksToAmr } from '@/services/knowledge';
import { createModel, generateModelCard, getModel, updateModel } from '@/services/model';
import type { Card, Code, DocumentAsset, Model } from '@/types/Types';
import { AssetType, ProgrammingLanguage } from '@/types/Types';
import { ModelServiceType } from '@/types/common';
import { AssetBlock, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { CodeBlock, CodeBlockType, getCodeBlocks } from '@/utils/code-asset';
import { extensionFromProgrammingLanguage } from '@/utils/data-util';
import { logger } from '@/utils/logger';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { ModelFromCodeState } from './model-from-code-operation';

const props = defineProps<{
	node: WorkflowNode<ModelFromCodeState>;
}>();

const emit = defineEmits([
	'close',
	'update-state',
	'select-output',
	'append-output',
	'update-output-port'
]);

enum ModelFramework {
	Petrinet = 'Petrinet',
	Decapodes = 'Decapodes'
}
const showSaveModelModal = ref(false);
const isProcessing = ref(false);
const fetchingInputBlocks = ref(false);

const programmingLanguages = Object.values(ProgrammingLanguage);
const modelFrameworks = Object.values(ModelFramework);
const modelServices = Object.values(ModelServiceType);
const decapodesModelValid = ref(false);
const kernelManager = new KernelSessionManager();

const selectedModel = ref<Model | null>(null);
const documentId = computed(() => props.node.inputs?.[1]?.value?.[0]?.documentId);

const document = ref<DocumentAsset | null>(null);

const goLLMCard = computed<any>(() => document.value?.metadata?.gollmCard);

const inputCodeBlocks = ref<AssetBlock<CodeBlock>[]>([]);

const allCodeBlocks = computed<AssetBlock<CodeBlock>[]>(() => {
	const blocks: AssetBlock<CodeBlock>[] = [];

	blocks.push(...inputCodeBlocks.value);
	if (
		clonedState.value.modelFramework === ModelFramework.Decapodes &&
		!isEmpty(clonedState.value.codeBlocks)
	) {
		// show only first added code block if Decapodes
		if (isEmpty(inputCodeBlocks.value)) blocks.push(clonedState.value.codeBlocks[0]);
	} else {
		blocks.push(...clonedState.value.codeBlocks);
	}

	return blocks;
});

const savingAsset = ref(false);
const isGeneratingCard = ref(false);

const clonedState = ref<ModelFromCodeState>({
	codeLanguage: ProgrammingLanguage.Python,
	modelFramework: ModelFramework.Petrinet,
	codeBlocks: [],
	modelId: '',
	modelService: ModelServiceType.TA1
});

const outputs = computed(() => {
	const activeProjectModelIds = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((model) => model.id);

	const savedOutputs: WorkflowOutput<ModelFromCodeState>[] = [];
	const unsavedOutputs: WorkflowOutput<ModelFromCodeState>[] = [];

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

	const groupedOutputs: { label: string; items: WorkflowOutput<ModelFromCodeState>[] }[] = [];

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
const selectedOutput = computed<WorkflowOutput<ModelFromCodeState> | undefined>(
	() => props.node.outputs?.find((output) => selectedOutputId.value === output.id)
);

const card = ref<Card | null>(null);

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);

	if (selectedOutputId.value) {
		onSelection(selectedOutputId.value);
	}

	if (documentId.value) {
		document.value = await getDocumentAsset(documentId.value);
	}

	fetchingInputBlocks.value = true;
	await getInputCodeBlocks();
	fetchingInputBlocks.value = false;

	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
});

onUnmounted(() => {
	// Get rid of session/kernels on unmount, otherwise we end upwith lots of kernels hanging
	// around using up memory.
	kernelManager.shutdown();
});

function buildJupyterContext() {
	const contextName =
		clonedState.value.modelFramework === ModelFramework.Decapodes ? 'decapodes' : null;
	const languageName =
		clonedState.value.codeLanguage === ProgrammingLanguage.Julia ? 'julia-1.10' : null;

	return {
		context: contextName,
		language: languageName,
		context_info: {}
	};
}

function setKernelContext() {
	const jupyterContext = buildJupyterContext();
	if (jupyterContext) {
		kernelManager.sendMessage('context_setup_request', jupyterContext);
	}
	emit('update-state', clonedState.value);
}

async function handleCode() {
	isProcessing.value = true;

	if (clonedState.value.modelFramework === ModelFramework.Petrinet) {
		const codeContent = allCodeBlocks.value
			.filter((block) => block.includeInProcess)
			.reduce((acc, block) => `${acc}${block.asset.codeContent}\n`, '');

		const fileName = `tempFile.${extensionFromProgrammingLanguage(clonedState.value.codeLanguage)}`;
		const file = new File([codeContent], fileName);
		const newCode: Code = {
			name: 'tempCode',
			description: 'tempDescription',
			files: {
				[fileName]: {
					language: clonedState.value.codeLanguage,
					dynamics: {
						name: 'dynamic',
						description: 'dynamic description',
						block: []
					}
				}
			}
		};

		const model: Model | null = await codeBlocksToAmr(newCode, file);

		if (!model || !model.id) {
			isProcessing.value = false;
			return;
		}

		generateCard(documentId.value, model.id);

		clonedState.value.modelId = model.id;

		emit('append-output', {
			label: `Output - ${props.node.outputs.length + 1}`,
			state: cloneDeep(clonedState.value),
			isSelected: false,
			type: 'modelId',
			value: [clonedState.value.modelId]
		});
		isProcessing.value = false;
	}

	if (clonedState.value.modelFramework === ModelFramework.Decapodes) {
		if (isEmpty(allCodeBlocks.value)) return;
		// we only use one code block for decapodes at the moment
		const code = allCodeBlocks.value[0].asset.codeContent;
		const messageContent = {
			declaration: code
		};

		decapodesModelValid.value = false;

		kernelManager
			.sendMessage('compile_expr_request', messageContent)
			.register('compile_expr_response', handleCompileExprResponse)
			.register('decapodes_preview', handleDecapodesPreview);
	}
}

function openModal() {
	if (selectedModel.value) showSaveModelModal.value = true;
}

function onAddModel(modelName: string) {
	if (!modelName || !selectedOutputId.value) return;
	updateNodeLabel(selectedOutputId.value, modelName);
}
function onCloseModelModal() {
	showSaveModelModal.value = false;
}

function handleCompileExprResponse() {
	decapodesModelValid.value = true;
}

// FIXME: This saves the preview as AMR, revisit when we have temporary-assets
async function handleDecapodesPreview(data: any) {
	console.log('Decapode preview', data);
	const decapodeJSON = data.content['application/json'];
	const header = {
		description: 'new description',
		name: 'new model',
		_type: 'Header',
		model_version: 'v1.0',
		schema: 'modelreps.io/DecaExpr',
		schema_name: 'decapode'
	};
	const amr = {
		header,
		model: decapodeJSON
	};

	const response = await createModel(amr);
	if (response && response.id) {
		const m = await getModel(response.id);
		if (m && m.id) {
			clonedState.value.modelId = m.id;
			emit('append-output', {
				label: `Output - ${props.node.outputs.length + 1}`,
				state: cloneDeep(clonedState.value),
				isSelected: false,
				type: 'modelId',
				value: [clonedState.value.modelId]
			});
		}
		isProcessing.value = false;
	}
}

// The format of code blocks is a real pain to work with...
async function getInputCodeBlocks() {
	const codeAssetId: string = props.node.inputs?.[0]?.value?.[0];
	if (!codeAssetId) return;
	const codeAsset = await getCodeAsset(codeAssetId);
	if (!codeAsset) return;
	inputCodeBlocks.value = await getCodeBlocks(codeAsset);
}

function addCodeBlock() {
	const codeBlock: AssetBlock<CodeBlock> = {
		includeInProcess: false,
		name: 'Code block',
		asset: {
			codeContent: '',
			codeLanguage: clonedState.value.codeLanguage
		}
	};
	clonedState.value.codeBlocks.push(codeBlock);
	emit('update-state', clonedState.value);
}

function removeCodeBlock(index: number) {
	clonedState.value.codeBlocks.splice(index, 1);
	emit('update-state', clonedState.value);
}

async function fetchModel() {
	if (!clonedState.value.modelId) {
		selectedModel.value = null;
		return;
	}
	isProcessing.value = true;
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
	isProcessing.value = false;
}

function isSaveModelDisabled(): boolean {
	const activeProjectModelIds = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((model) => model.id);

	return !selectedModel.value || !!activeProjectModelIds?.includes(selectedModel.value.id);
}

// generates the model card and fetches the model when finished
async function generateCard(docId, modelId) {
	if (!docId || !modelId) return;

	if (clonedState.value.modelService === ModelServiceType.TA1 && card.value) {
		return;
	}

	if (clonedState.value.modelService === ModelServiceType.TA4 && goLLMCard.value) {
		return;
	}

	isGeneratingCard.value = true;
	await generateModelCard(docId, modelId, clonedState.value.modelService);
	isGeneratingCard.value = false;
	fetchModel();
}

watch(
	() => props.node.active,
	() => {
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
	},
	{ immediate: true }
);

// watch for model id changes on state
watch(
	() => clonedState.value.modelId,
	async () => {
		await fetchModel();
	}
);

// watch for prop state changes
watch(
	() => props.node.state,
	() => {
		clonedState.value = cloneDeep(props.node.state);
	},
	{ deep: true }
);

const onSelection = (id: string) => {
	emit('select-output', id);
};

function updateNodeLabel(id: string, label: string) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.label = label;
	emit('update-output-port', outputPort);
}
</script>

<style scoped>
span {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.p-dropdown {
	max-height: 40px;
}

ul {
	list-style: none;
	display: flex;
	overflow: auto;
	flex-direction: column;
	gap: 0.5rem;
	flex: 1;
}

.ace-editor {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
}
</style>
