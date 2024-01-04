<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section :is-loading="assetLoading">
				<Steps
					:model="formSteps"
					:readonly="false"
					@update:active-step="activeStepperIndex = $event"
				/>

				<div class="equation-view" v-if="activeStepperIndex === 0">
					<div class="header-group">
						<p>These equations will be used to create your model.</p>
						<Button label="Add an equation" icon="pi pi-plus" text />
					</div>
					<tera-asset-block
						v-for="(equation, i) in clonedState.equations"
						:key="i"
						:is-included="equation.includeInProcess"
						@update:is-included="onUpdateInclude(equation)"
					>
						<template #header>
							<h5>{{ equation.name }}</h5>
						</template>
						<div class="block-container">
							<label>Extracted Image:</label>
							<Image id="img" :src="getAssetUrl(equation)" :alt="''" preview />
							<template v-if="equation.asset.text">
								<label>Interpreted As:</label>
								<tera-math-editor :latex-equation="equation.asset.text"> </tera-math-editor>

								<InputText v-model="equation.asset.text" />
							</template>
							<span v-else>Could not extract LaTeX for image</span>
						</div>
					</tera-asset-block>
				</div>
				<div v-if="activeStepperIndex === 1">
					<Textarea v-model="clonedState.text" autoResize disabled style="width: 100%" />
				</div>
				<template #footer>
					<span style="margin-right: auto"
						><label>Model framework:</label
						><Dropdown
							class="w-full md:w-14rem"
							v-model="clonedState.modelFramework"
							:options="modelFrameworks"
							@change="onChangeModelFramework"
					/></span>
					<Button
						label="Run"
						@click="onRun"
						:diabled="assetLoading"
						:loading="loadingModel"
						outlined
					></Button>
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				:is-loading="loadingModel"
				is-selectable
			>
				<section v-if="selectedModel">
					<tera-model-diagram :model="selectedModel" :is-editable="false"></tera-model-diagram>
					<tera-model-semantic-tables :model="selectedModel" readonly />
				</section>
				<tera-operator-placeholder
					v-else
					:operation-type="node.operationType"
					style="height: 100%"
				/>
				<template #footer>
					<Button
						style="margin-right: auto"
						label="Save as new model"
						:disabled="!selectedModel"
						outlined
					></Button>
					<Button label="Close" @click="emit('close')"></Button>
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { AssetBlock, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { computed, onMounted, ref, watch } from 'vue';
import { getDocumentAsset, getEquationFromImageUrl } from '@/services/document-assets';
import { DocumentAsset, ExtractionAssetType, Model } from '@/types/Types';
import { cloneDeep, isEmpty, unionBy } from 'lodash';
import Image from 'primevue/image';
import { equationsToAMR } from '@/services/knowledge';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { logger } from '@/utils/logger';
import { getModel } from '@/services/model';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { useProjects } from '@/composables/project';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import InputText from 'primevue/inputtext';
import Steps from 'primevue/steps';
import Textarea from 'primevue/textarea';
import { EquationFromImageBlock, ModelFromDocumentState } from './model-from-document-operation';

const emit = defineEmits([
	'close',
	'update-state',
	'append-output-port',
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
	const activeProjectModelIds = useProjects().activeProject.value?.assets?.models?.map(
		(model) => model.id
	);

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

const selectedOutputId = ref<string>();

const modelFrameworks = Object.values(ModelFramework);

const clonedState = ref<ModelFromDocumentState>({
	equations: [],
	text: '',
	modelFramework: ModelFramework.Petrinet,
	modelId: null
});
const document = ref<DocumentAsset | null>();
const assetLoading = ref(false);
const loadingModel = ref(false);
const selectedModel = ref<Model | null>(null);

const formSteps = ref([
	{
		label: 'Equations'
	},
	{
		label: 'Text'
	}
]);

const activeStepperIndex = ref<number>(0);

onMounted(async () => {
	clonedState.value = cloneDeep(props.node.state);
	if (selectedOutputId.value) {
		onUpdateOutput(selectedOutputId.value);
	}

	const documentId = props.node.inputs?.[0]?.value?.[0];
	assetLoading.value = true;
	if (documentId) {
		document.value = await getDocumentAsset(documentId);
		const equations = document.value?.assets?.filter(
			(a) => a.assetType === ExtractionAssetType.Equation
		);

		const state = cloneDeep(props.node.state);

		// equations that not been run in image -> equation
		const nonRunEquations = equations?.filter((e) => {
			const foundEquation = state.equations.find((eq) => eq.asset.fileName === e.fileName);
			return !foundEquation;
		});

		if (isEmpty(nonRunEquations)) {
			assetLoading.value = false;
			return;
		}
		const promises = nonRunEquations?.map(async (e, i) => {
			const equationText = await getEquationFromImageUrl(documentId, e.fileName);
			const equationBlock: EquationFromImageBlock = {
				...e,
				text: equationText ?? '',
				extractionError: !equationText
			};

			const assetBlock: AssetBlock<EquationFromImageBlock> = {
				name: `Equation ${i + 1}`,
				includeInProcess: true,
				asset: equationBlock
			};

			return assetBlock;
		});

		if (!promises) return;

		const newEquations = await Promise.all(promises);

		state.equations = unionBy(newEquations, state.equations, 'asset.fileName');
		state.text = document.value?.text ?? '';
		emit('update-state', state);
	}
	assetLoading.value = false;
});

function onUpdateInclude(asset: AssetBlock<EquationFromImageBlock>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update-state', clonedState.value);
}

function onUpdateOutput(id) {
	emit('select-output', id);
}

function onUpdateSelection(id) {
	const outputPort = cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
}

async function onRun() {
	let equations = clonedState.value.equations
		.filter((e) => e.includeInProcess && e.asset.text)
		.map((e) => e.asset.text);

	equations = [
		`\\frac{dS}{dt} = - \\beta I \\frac{S}{N}`,
		`\\frac{dE}{dt} = \\beta I \\frac{S}{N} - r_{EI}E`,
		`\\frac{dI}{dt} = r_{EI}E - r_{IR}p_{IR}I - r_{IH}p_{IH}I`,
		`\\frac{dH}{dt} = r_{IH}p_{IH}I - r_{HR}p_{HR}H - r_{HD}p_{HD}H`,
		`\\frac{dR}{dt} = r_{IR}p_{IR}I + r_{HR}p_{HR}H`,
		`\\frac{dD}{dt} = r_{HD}p_{HD}H`
	];
	const res = await equationsToAMR('latex', equations, clonedState.value.modelFramework);

	if (!res) {
		logger.error('error creating amr');
		return;
	}

	const modelId = res.job_result?.tds_model_id;

	if (!modelId) return;

	clonedState.value.modelId = modelId;
	emit('append-output-port', {
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
	const model = await getModel(clonedState.value.modelId);
	selectedModel.value = model;
	loadingModel.value = false;
}

// since AWS links expire we need to use the refetched document image urls to display the images
function getAssetUrl(asset: AssetBlock<EquationFromImageBlock>): string {
	const foundAsset = document.value?.assets?.find((a) => a.fileName === asset.asset.fileName);
	if (!foundAsset) return '';
	return foundAsset.metadata?.url;
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
	gap: 0.5rem;
}

.header-group {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.equation-view {
	display: flex;
	gap: 0.5rem;
	flex-direction: column;
}

:deep(.math-editor) {
	background-color: var(--surface-disabled);
}
</style>
