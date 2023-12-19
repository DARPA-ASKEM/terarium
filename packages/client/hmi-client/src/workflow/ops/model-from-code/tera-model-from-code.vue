<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div tabName="Wizard">
			<tera-drilldown-section>
				<header>
					<h5>Code</h5>
					<Dropdown
						class="w-full md:w-14rem"
						v-model="clonedState.codeLanguage"
						:options="programmingLanguages"
						@change="setKernelContext"
					/>
					<Button label="Add code block" icon="pi pi-plus" text @click="addCodeBlock" />
				</header>
				<ul>
					<li v-for="({ name, codeLanguage }, i) in codeBlocks" :key="i">
						<Panel toggleable>
							<template #header>
								<section>
									<h5>{{ name }}</h5>
									<Button icon="pi pi-pencil" text rounded />
								</section>
							</template>
							<template #icons>
								<span>
									<label>Include in process</label>
									<InputSwitch v-model="codeBlocks[i].includeInProcess" />
								</span>
								<Button icon="pi pi-trash" text rounded @click="removeCodeBlock(i)" />
							</template>
							<!--FIXME: togglericon slot isn't recognized for some reason maybe update prime vue?
						<template #togglericon="{ collapsed }">
							<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
						</template> -->
							<v-ace-editor
								v-model:value="codeBlocks[i].codeContent"
								@init="initialize"
								:lang="codeLanguage"
								theme="chrome"
								style="height: 10rem; width: 100%"
								class="ace-editor"
							/>
						</Panel>
					</li>
				</ul>
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
						:disabled="isProcessing"
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
				<tera-operator-placeholder
					v-if="!selectedModel && !previewHTML"
					:operation-type="node.operationType"
					style="height: 100%"
				/>
				<section>
					<template v-if="clonedState.modelFramework === ModelFramework.Petrinet && selectedModel">
						<tera-model-diagram :model="selectedModel" :is-editable="false"></tera-model-diagram>
						<tera-model-semantic-tables :model="selectedModel" readonly />
					</template>
					<template v-if="clonedState.modelFramework === ModelFramework.Decapodes && previewHTML">
						<div :innerHTML="previewHTML" />
					</template>
				</section>
				<template #footer>
					<Button
						:loading="savingAsset"
						:disabled="!isModelValid()"
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
	<tera-modal v-if="isNewModelModalVisible">
		<template #header>
			<h4>New Model</h4>
		</template>
		<form @submit.prevent>
			<label for="new-model">Enter a unique name for your model</label>
			<InputText id="new-model" type="text" v-model="newModelName" placeholder="New model" />
		</form>
		<template #footer>
			<Button @click="saveAsNewModel">Create model</Button>
			<Button class="p-button-secondary" @click="isNewModelModalVisible = false">Cancel</Button>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Dropdown from 'primevue/dropdown';
import Panel from 'primevue/panel';
import Button from 'primevue/button';
import InputSwitch from 'primevue/inputswitch';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import { AssetType, Model, ProgrammingLanguage } from '@/types/Types';
import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { createModel, getModel, updateModel, validateModelName } from '@/services/model';
import { addAsset } from '@/services/project';
import { useProjects } from '@/composables/project';
import { useToastService } from '@/services/toast';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import { getCodeAsset, getCodeFileAsText } from '@/services/code';
import { codeToAMR } from '@/services/knowledge';
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
const newModelName = ref('');
const isProcessing = ref(false);

const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const programmingLanguages = Object.values(ProgrammingLanguage);
const modelFrameworks = Object.values(ModelFramework);
const decapodesModelValid = ref(false);
const kernelManager = new KernelSessionManager();
const previewHTML = ref('');

const selectedModel = ref<Model | null>(null);
const codeBlock = {
	...props.node.state,
	name: 'Code block 1',
	includeInProcess: true
};

const savingAsset = ref(false);

const codeBlocks = ref([codeBlock]);

const clonedState = ref<ModelFromCodeState>({
	codeLanguage: ProgrammingLanguage.Python,
	modelFramework: ModelFramework.Petrinet,
	codeContent: '',
	modelId: ''
});

const outputs = computed(() => {
	const activeProjectModelIds = useProjects().activeProject.value?.assets?.MODEL?.map(
		(model) => model.id
	);

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

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);
	if (selectedOutputId.value) {
		onUpdateOutput(selectedOutputId.value);
	}

	if (props.node.inputs[0].value?.[0]) {
		const codeAsset = await getCodeAsset(props.node.inputs[0].value[0]);

		// for now get the first file's source code
		if (codeAsset?.id && codeAsset?.files) {
			const filename = Object.keys(codeAsset.files)[0];
			const codeContent = await getCodeFileAsText(codeAsset.id, filename);
			codeBlocks.value[0].codeContent = codeContent ?? '';
		}
	}
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
		const modelId = await codeToAMR(props.node.inputs[0].value?.[0]);
		clonedState.value.modelId = modelId;
		emit('append-output-port', {
			label: `Output ${(props.node.outputs?.length ?? 0) + 1}`,
			state: cloneDeep(clonedState.value),
			isSelected: false
		});
		isProcessing.value = false;
	}

	if (clonedState.value.modelFramework === ModelFramework.Decapodes) {
		const code = editor.value?.getValue();
		const messageContent = {
			declaration: code
		};

		decapodesModelValid.value = false;

		kernelManager
			.sendMessage('compile_expr_request', messageContent)
			?.register('compile_expr_response', handleCompileExprResponse)
			?.register('decapodes_preview', handleDecapodesPreview);
	}
}

function openModal() {
	isNewModelModalVisible.value = true;
}

async function saveAsNewModel() {
	if (!validateModelName(newModelName.value)) return;
	if (clonedState.value.modelFramework === ModelFramework.Petrinet) {
		savingAsset.value = true;
		// 1. Update model name
		const model = selectedModel.value;
		if (!model) return;
		model.header.name = newModelName.value;
		const updateResponse = await updateModel(model);
		if (!updateResponse) return;

		// 2. Save asset to project
		const projectId = useProjects().activeProject.value?.id;
		if (!projectId || !selectedModel.value) return;
		const response = await addAsset(projectId, AssetType.Model, selectedModel.value.id);
		savingAsset.value = false;

		if (!response) {
			logger.error('Could not save asset to project');
			return;
		}

		// 3. Append Model to output port
		emit('append-output-port', {
			label: selectedModel.value.header?.name,
			state: cloneDeep(clonedState.value),
			isSelected: false
		});
		useToastService().success('', 'Model saved successfully.');
	}
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
	if (response) {
		const m = await getModel(response.id);
		if (m) {
			selectedModel.value = m;
			previewHTML.value = `Decapode created ${m.id}`;
		}
		isProcessing.value = false;
	}
}

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
}

function addCodeBlock() {
	codeBlocks.value.push(cloneDeep(codeBlock));
}

function removeCodeBlock(index: number) {
	codeBlocks.value.splice(index, 1);
}

function isModelValid() {
	if (clonedState.value.modelFramework === ModelFramework.Petrinet) {
		return !!selectedModel.value;
	}

	if (clonedState.value.modelFramework === ModelFramework.Decapodes) {
		return decapodesModelValid.value;
	}

	return false;
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
	}
);

function onUpdateOutput(id) {
	emit('select-output', id);
}

function onUpdateSelection(id) {
	const outputPort = props.node.outputs?.find((port) => port.id === id);
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
.p-panel {
	border: 1px solid var(--surface-border-light);
}

.p-panel:deep(section) {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.p-panel:deep(.p-panel-icons) {
	display: flex;
	gap: 1rem;
	align-items: center;
}

li > header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

li > header > section {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.ace-editor {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
}
</style>
