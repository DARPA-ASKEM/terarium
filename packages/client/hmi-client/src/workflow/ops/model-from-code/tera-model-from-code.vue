<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div tabName="Wizard">
			<tera-drilldown-section :isLoading="fetchingInputBlocks">
				<header>
					<h5>Code</h5>
					<Dropdown
						class="w-full md:w-14rem"
						v-model="clonedState.codeLanguage"
						:options="programmingLanguages"
						@change="setKernelContext"
					/>
					<Button label="Add code block" icon="pi pi-plus" text @click="addCodeBlock" disabled />
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
						@delete="removeCodeBlock(i)"
						:is-included="allCodeBlocks[i].includeInProcess"
						@update:is-included="
							allCodeBlocks[i].includeInProcess = !allCodeBlocks[i].includeInProcess
						"
					>
						<template #header>
							<h5>{{ name }}</h5>
						</template>
						<v-ace-editor
							v-model:value="allCodeBlocks[i].asset.codeContent"
							:lang="asset.codeLanguage"
							theme="chrome"
							style="height: 10rem; width: 100%"
							class="ace-editor"
							:readonly="asset.type === CodeBlockType.INPUT"
						/>
					</tera-asset-block>
				</template>
				<template #footer>
					<span style="margin-right: auto"
						><label>Model framework:</label
						><Dropdown
							class="w-full md:w-14rem"
							v-model="clonedState.modelFramework"
							:options="modelFrameworks"
							@change="setKernelContext"
					/></span>
					<Button
						:disabled="isProcessing || allCodeBlocks.length === 0"
						label="Run"
						icon="pi pi-play"
						severity="secondary"
						outlined
						@click="handleCode"
					/>
				</template>
			</tera-drilldown-section>
		</div>
		<div tabName="Notebook">
			<!--Notebook section if we decide we need one-->
		</div>
		<template #preview>
			<tera-drilldown-preview
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				:is-loading="isProcessing"
				is-selectable
			>
				<section v-if="selectedModel">
					<template v-if="selectedOutput?.state?.modelFramework === ModelFramework.Petrinet">
						<tera-model-diagram :model="selectedModel" :is-editable="false"></tera-model-diagram>
						<tera-model-semantic-tables :model="selectedModel" readonly />
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
						style="margin-right: auto"
					/>
					<Button label="Cancel" severity="secondary" @click="emit('close')" outlined />
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import type { Model } from '@/types/Types';
import { AssetType, ProgrammingLanguage } from '@/types/Types';
import { AssetBlock, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { createModel, getModel } from '@/services/model';
import { useProjects } from '@/composables/project';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getCodeAsset } from '@/services/code';
import { codeToAMR } from '@/services/knowledge';
import { CodeBlock, CodeBlockType, getCodeBlocks } from '@/utils/code-asset';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { ModelFromCodeState } from './model-from-code-operation';

const props = defineProps<{
	node: WorkflowNode<ModelFromCodeState>;
}>();

const emit = defineEmits([
	'close',
	'update-state',
	'select-output',
	'append-output-port',
	'update-output-port'
]);

enum ModelFramework {
	Petrinet = 'Petrinet',
	Decapodes = 'Decapodes'
}
const isNewModelModalVisible = ref(false);
const isProcessing = ref(false);
const fetchingInputBlocks = ref(false);

const programmingLanguages = Object.values(ProgrammingLanguage);
const modelFrameworks = Object.values(ModelFramework);
const decapodesModelValid = ref(false);
const kernelManager = new KernelSessionManager();

const selectedModel = ref<Model | null>(null);

const inputCodeBlocks = ref<AssetBlock<CodeBlock>[]>([]);

const allCodeBlocks = computed<AssetBlock<CodeBlock>[]>(() => [
	...inputCodeBlocks.value,
	...clonedState.value.codeBlocks
]);

const savingAsset = ref(false);

const clonedState = ref<ModelFromCodeState>({
	codeLanguage: ProgrammingLanguage.Python,
	modelFramework: ModelFramework.Petrinet,
	codeBlocks: [],
	modelId: ''
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
const selectedOutputId = ref<string>();
const selectedOutput = computed<WorkflowOutput<ModelFromCodeState> | undefined>(
	() => props.node.outputs?.find((output) => selectedOutputId.value === output.id)
);

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);
	if (selectedOutputId.value) {
		onUpdateOutput(selectedOutputId.value);
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
		clonedState.value.codeLanguage === ProgrammingLanguage.Julia ? 'julia-1.9' : null;

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
}

async function handleCode() {
	isProcessing.value = true;

	if (clonedState.value.modelFramework === ModelFramework.Petrinet) {
		const modelId = await codeToAMR(
			props.node.inputs[0].value?.[0],
			'temp model',
			'temp model description',
			true
		);
		clonedState.value.modelId = modelId;
		emit('append-output-port', {
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
	isNewModelModalVisible.value = true;
}

function onAddModel(modelName: string) {
	if (!modelName || !selectedOutputId.value) return;
	updateNodeLabel(selectedOutputId.value, modelName);
}
function onCloseModelModal() {
	isNewModelModalVisible.value = false;
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
			emit('append-output-port', {
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
		name: 'Code Block',
		asset: {
			codeContent: '',
			codeLanguage: clonedState.value.codeLanguage
		}
	};
	clonedState.value.codeBlocks.push(codeBlock);
}

function removeCodeBlock(index: number) {
	clonedState.value.codeBlocks.splice(index, 1);
}

async function fetchModel() {
	if (!clonedState.value.modelId) {
		selectedModel.value = null;
		return;
	}
	isProcessing.value = true;
	const model = await getModel(clonedState.value.modelId);
	selectedModel.value = model;
	isProcessing.value = false;
}

function isSaveModelDisabled(): boolean {
	const activeProjectModelIds = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((model) => model.id);

	return !selectedModel.value || !!activeProjectModelIds?.includes(selectedModel.value.id);
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

function onUpdateOutput(id) {
	emit('select-output', id);
}

function updateNodeLabel(id: string, label: string) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.label = label;
	emit('update-output-port', outputPort);
}

function onUpdateSelection(id) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
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
