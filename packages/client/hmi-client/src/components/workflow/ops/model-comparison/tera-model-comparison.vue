<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div :tabName="Tabs.Wizard">
			<tera-drilldown-section>
				<!-- LLM generated overview -->
				<section class="comparison-overview">
					<Accordion :activeIndex="0">
						<AccordionTab header="Overview">
							<p v-if="isEmpty(overview)" class="subdued">
								<i class="pi pi-spin pi-spinner mr-1" />
								Analyzing models metadata to generate a detailed comparison analysis...
							</p>
							<p v-html="overview" v-else class="markdown-text" />
						</AccordionTab>
					</Accordion>
				</section>
				<!-- Model comparison table -->
				<div class="p-datatable-wrapper">
					<table class="p-datatable-table p-datatable-scrollable-table">
						<thead class="p-datatable-thead">
							<tr>
								<th></th>
								<th v-for="model in modelsToCompare" :key="model.id" class="text-lg">
									{{ model.header.name }}
								</th>
							</tr>
						</thead>
						<tbody v-if="fields" class="p-datatable-tbody">
							<tr>
								<td class="field">Diagram</td>
								<td v-for="(model, index) in modelsToCompare" :key="index">
									<tera-model-diagram :model="model" class="diagram" />
								</td>
							</tr>
							<tr v-for="field in fields" :key="field">
								<td class="field">{{ formatField(field) }}</td>
								<td v-for="(card, index) in modelCardsToCompare" :key="index">
									<template v-if="!card?.[field]"> Not found </template>
									<template v-else-if="typeof card[field] === 'object'">
										<template v-for="(value, j) in Object.values(card[field])">
											<template v-if="Array.isArray(value)">
												{{ value.join(', ') }}
											</template>
											<div class="value" v-else :key="j">{{ value }}</div>
										</template>
									</template>
									<template v-else-if="Array.isArray(card[field])">{{ card[field].join(', ') }}</template>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</tera-drilldown-section>
		</div>
		<tera-columnar-panel :tabName="Tabs.Notebook">
			<!--TODO: The notebook input and buttons works well here, but it the html/css
				organization should be refactored here (same for tera-model-edit)-->
			<tera-drilldown-section class="notebook-section">
				<div class="toolbar">
					<tera-notebook-jupyter-input
						:kernelManager="kernelManager"
						:defaultOptions="sampleAgentQuestions"
						@llm-output="appendCode"
						@llm-thought-output="(data: any) => llmThoughts.push(data)"
						@question-asked="llmThoughts = []"
						:context-language="contextLanguage"
					>
						<template #toolbar-right-side>
							<Button label="Reset" outlined severity="secondary" size="small" @click="resetNotebook" />
							<Button icon="pi pi-play" label="Run" size="small" @click="runCode" />
						</template>
					</tera-notebook-jupyter-input>
				</div>
				<v-ace-editor
					v-model:value="code"
					@init="initializeAceEditor"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>
			</tera-drilldown-section>
			<tera-drilldown-preview>
				<tera-progress-spinner
					v-if="isLoadingStructuralComparisons && isEmpty(structuralComparisons)"
					is-centered
					:font-size="3"
				/>
				<ul>
					<li v-for="(image, index) in structuralComparisons" :key="index">
						<label>Comparison {{ index + 1 }}</label>
						<Image id="img" :src="image" :alt="`Structural comparison ${index + 1}`" preview />
					</li>
				</ul>

				<!-- Legend -->
				<template #footer v-if="isLoadingStructuralComparisons || !isEmpty(structuralComparisons)">
					<div class="legend flex align-items-center gap-4">
						<span class="flex gap-3">
							<span class="flex align-items-center gap-2">
								<span class="legend-circle subdued">Name</span>
								<span>State variable nodes</span>
							</span>
							<span class="flex align-items-center gap-2">
								<span class="legend-square subdued">Name</span>
								<span>Transition nodes</span>
							</span>
						</span>
						<span class="flex gap-4">
							<span class="legend-line blue">Model 1</span>
							<span class="legend-line green">Model 2</span>
							<span class="legend-line orange">Common to both models</span>
							<span class="legend-line red">Equal or related transitions</span>
						</span>
					</div>
				</template>
			</tera-drilldown-preview>
		</tera-columnar-panel>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import markdownit from 'markdown-it';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { compareModels } from '@/services/goLLM';
import { KernelSessionManager } from '@/services/jupyter';
import { getModel } from '@/services/model';
import { ClientEvent, ClientEventType, TaskResponse, TaskStatus, type Model } from '@/types/Types';
import { OperatorStatus, WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import { onMounted, onUnmounted, ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';

import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import Image from 'primevue/image';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

import { saveCodeToState } from '@/services/notebook';
import { getImages, addImage, deleteImages } from '@/services/image';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import { b64DecodeUnicode } from '@/utils/binary';
import { useClientEvent } from '@/composables/useClientEvent';
import { ModelComparisonOperationState } from './model-comparison-operation';

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

const emit = defineEmits(['update-state', 'update-status', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

let editor: VAceEditorInstance['_editor'] | null;
const kernelManager = new KernelSessionManager();
const sampleAgentQuestions = [
	'Compare models',
	'Compare the three models and visualize and display them.',
	'Compare the two models and visualize and display them.'
];
let compareModelsTaskId = '';

const modelsToCompare = ref<Model[]>([]);
const modelCardsToCompare = ref<any[]>([]);
const fields = ref<string[]>([]);

const isLoadingStructuralComparisons = ref(false);
const overview = ref<string | null>(null);
const structuralComparisons = ref<string[]>([]);
const code = ref(props.node.state.notebookHistory?.[0]?.code ?? '');
const llmThoughts = ref<any[]>([]);
const isKernelReady = ref(false);
const contextLanguage = ref<string>('python3');

const initializeAceEditor = (editorInstance: any) => {
	editor = editorInstance;
};

function formatField(field: string) {
	const result = field
		.replace(/([A-Z])/g, ' $1')
		.trim()
		.toLowerCase();
	return result.charAt(0).toUpperCase() + result.slice(1);
}

function updateImagesState(operationType: string, newImageId: string | null = null) {
	const state = cloneDeep(props.node.state);
	if (operationType === 'add' && newImageId) state.comparisonImageIds.push(newImageId);
	else if (operationType === 'clear') state.comparisonImageIds = [];
	emit('update-state', state);
}

function updateCodeState() {
	const state = saveCodeToState(props.node, code.value, true);
	emit('update-state', state);
}

function emptyImages() {
	deleteImages(props.node.state.comparisonImageIds); // Delete images from S3
	updateImagesState('clear'); // Then their ids can be removed from the state
	structuralComparisons.value = [];
}

function resetNotebook() {
	code.value = '';
	emptyImages();
	updateCodeState();
}

function runCode() {
	const messageContent = {
		silent: false,
		store_history: false,
		user_expressions: {},
		allow_stdin: true,
		stop_on_error: false,
		code: editor?.getValue() as string
	};
	isLoadingStructuralComparisons.value = true;
	emptyImages();
	updateCodeState();

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('display_data', (data) => {
			const newImageId = uuidv4();
			const newImage = `data:image/png;base64,${data.content.data['image/png']}`;
			structuralComparisons.value.push(newImage);
			addImage(newImageId, newImage);
			updateImagesState('add', newImageId);
			isLoadingStructuralComparisons.value = false;
		})
		.register('error', (data) => {
			logger.error(`${data.content.ename}: ${data.content.evalue}`);
			isLoadingStructuralComparisons.value = false;
		});
}

function appendCode(data: any) {
	const newCode = data.content.code;
	if (newCode) code.value = newCode;
	else logger.error('No code to append');
}

async function buildJupyterContext() {
	if (modelsToCompare.value.length < 2) {
		logger.warn('Cannot build Jupyter context without models');
		return;
	}
	try {
		const jupyterContext = {
			context: 'mira',
			language: 'python3',
			context_info: {
				models: modelsToCompare.value.map((model) => ({
					model_id: model.id,
					name: model.header.name.replace(/[^a-zA-Z0-9_]/g, '_') // Acceptable characters for python variable name
				}))
			}
		};
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
			isKernelReady.value = true;
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
}

// Generate once the comparison task has been completed
function generateOverview(output: string) {
	overview.value = markdownit().render(JSON.parse(b64DecodeUnicode(output)).response);
	emit('update-status', OperatorStatus.DEFAULT); // This is a custom way of granting a default status to the operator, since it has no output
}

// Create a task to compare the models
async function processCompareModels(modelIds: string[]) {
	const taskRes = await compareModels(modelIds, props.node.workflowId, props.node.id);
	compareModelsTaskId = taskRes.id;
	if (taskRes.status === TaskStatus.Success) {
		generateOverview(taskRes.output);
	}
}

// Listen for the task completion event
useClientEvent(ClientEventType.TaskGollmCompareModel, (event: ClientEvent<TaskResponse>) => {
	if (
		!event.data ||
		event.data.id !== compareModelsTaskId ||
		!isEmpty(overview.value) ||
		event.data.status !== TaskStatus.Success
	) {
		return;
	}
	generateOverview(event.data.output);
});

onMounted(async () => {
	if (!isEmpty(props.node.state.comparisonImageIds)) {
		isLoadingStructuralComparisons.value = true;
		structuralComparisons.value = await getImages(props.node.state.comparisonImageIds);
		isLoadingStructuralComparisons.value = false;
	}

	const modelIds: string[] = props.node.inputs
		.filter((input) => input.status === WorkflowPortStatus.CONNECTED)
		.map((input) => input.value?.[0]);

	modelsToCompare.value = (await Promise.all(modelIds.map(async (modelId) => getModel(modelId)))).filter(
		Boolean
	) as Model[];
	modelCardsToCompare.value = modelsToCompare.value.map(({ metadata }) => metadata?.gollmCard);
	fields.value = [...new Set(modelCardsToCompare.value.flatMap((card) => (card ? Object.keys(card) : [])))];

	await buildJupyterContext();
	await processCompareModels(modelIds);
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
.p-datatable-wrapper {
	margin: 0 var(--gap-4);
}

table {
	table-layout: fixed;
	width: 100%;

	& td,
	th {
		vertical-align: top;
		text-align: left;
		padding: var(--gap-2);
		overflow: auto;
		text-overflow: ellipsis;
	}

	& td {
		border-top: 1px solid var(--surface-border-light);
	}

	& th:first-child,
	td:first-child {
		width: 8%;
		padding: var(--gap-small) 0;
		font-weight: 600;
	}

	& td:not(:first-child) {
		padding: var(--gap-small);
	}

	& .value {
		padding-bottom: 1rem;
	}
}

.input:deep(input) {
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: 12px;
	background-repeat: no-repeat;
	text-indent: 30px;
}

ul {
	display: flex;
	flex-direction: column;
	gap: var(--gap);

	& > li {
		display: flex;
		flex-direction: column;
		gap: var(--gap-xsmall);

		& > span {
			width: fit-content;
			margin-right: var(--gap-xxlarge);
		}
	}
}

/* TODO: Improve this pattern later same in (tera-model-input) */

.notebook-section {
	background-color: var(--surface-disabled);
	border-right: 1px solid var(--surface-border-dark);
	padding: var(--gap);
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
}

.toolbar-right-side {
	position: absolute;
	top: var(--gap);
	right: 0;
	gap: var(--gap-small);
	display: flex;
	align-items: center;
}

.comparison-overview {
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius-medium);
	padding: var(--gap-2);
	margin: var(--gap-4) var(--gap-4) 0;
}

.subdued {
	color: var(--text-color-secondary);
}

.diagram {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

.legend {
	font-size: var(--font-caption);
	flex: 1;
	margin-bottom: var(--gap-4);
	overflow-x: auto;
	padding: 0 var(--gap-4);
}

.legend-circle {
	padding: var(--gap-small) var(--gap);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
	border-radius: 50%;
	font-family: 'Times New Roman', Times, serif;
}

.legend-square {
	padding: var(--gap-xsmall) var(--gap);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
	font-family: 'Times New Roman', Times, serif;
}

.legend-line {
	position: relative;
}

.legend-line::before {
	content: '';
	position: absolute;
	top: 50%;
	left: 0;
	width: 2px;
	height: 24px;
	transform: translate(-10px, -10px);
}
.legend-line.red::before {
	background-color: red;
}
.legend-line.orange::before {
	background-color: orange;
}
.legend-line.blue::before {
	background-color: blue;
}
.legend-line.green::before {
	background-color: lightgreen;
}
</style>
