<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
		<div :tabName="Tabs.Wizard">
			<tera-drilldown-section>
				<Panel v-if="llmAnswer" header="Comparison overview" toggleable>
					<template #togglericon="{ collapsed }">
						<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
					</template>
					<p>{{ llmAnswer }}</p>
				</Panel>
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
									<tera-model-diagram :model="model" :is-editable="false" is-preview />
								</td>
							</tr>
							<template v-for="field in fields" :key="field">
								<tr>
									<td class="field">{{ formatField(field) }}</td>
									<td v-for="(card, index) in modelCardsToCompare" :key="index">
										<template v-if="typeof card[field] === 'object'">
											<template v-for="(value, j) in Object.values(card[field])">
												<template v-if="Array.isArray(value)">
													{{ value.join(', ') }}
												</template>
												<div class="value" v-else :key="j">
													{{ value }}
												</div>
											</template>
										</template>
										<template v-if="Array.isArray(card[field])">
											{{ card[field].join(', ') }}
										</template>
									</td>
								</tr>
							</template>
						</tbody>
					</table>
				</div>
			</tera-drilldown-section>
		</div>
		<div :tabName="Tabs.Notebook">
			<!--TODO: The notebook input and buttons works well here, but it the html/css
				organization should be refactored here (same for tera-model-edit)-->
			<tera-drilldown-section class="notebook-section">
				<div class="toolbar-right-side">
					<Button
						icon="pi pi-play"
						label="Run"
						outlined
						severity="secondary"
						size="small"
						@click="runCode"
					/>
				</div>
				<tera-notebook-jupyter-input
					:kernelManager="kernelManager"
					:defaultOptions="sampleAgentQuestions"
					@llm-output="appendCode"
				/>
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
			</tera-drilldown-preview>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { compareModels } from '@/services/goLLM';
import { KernelSessionManager } from '@/services/jupyter';
import { getModel } from '@/services/model';
import type { Model } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import Panel from 'primevue/panel';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import Image from 'primevue/image';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { ModelComparisonOperationState } from './model-comparison-operation';

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

let editor: VAceEditorInstance['_editor'] | null;
const kernelManager = new KernelSessionManager();
const sampleAgentQuestions = [
	'Compare the three models and visualize and display them.',
	'Compare the two models and visualize and display them.'
];

const isLoadingStructuralComparisons = ref(false);
const structuralComparisons = ref(props.node.state.structuralComparisons ?? []);
const llmAnswer = ref('');
const code = ref(props.node.state.notebookHistory?.[0]?.code ?? '');
const isKernelReady = ref(false);
const modelsToCompare = ref<Model[]>([]);

const modelCardsToCompare = computed(() =>
	modelsToCompare.value.map(({ metadata }) => metadata?.gollmCard)
);
const fields = computed(
	() =>
		[
			...new Set(modelCardsToCompare.value.reduce((acc, card) => acc.concat(Object.keys(card)), []))
		] as string[]
);
const cellWidth = computed(() => `${85 / modelsToCompare.value.length}vw`);

const initializeAceEditor = (editorInstance: any) => {
	editor = editorInstance;
};

async function addModelForComparison(modelId: Model['id']) {
	if (!modelId) return;
	const model = await getModel(modelId);
	if (model) modelsToCompare.value.push(model);
	if (
		modelsToCompare.value.length === props.node.inputs.length - 1 &&
		modelsToCompare.value.length > 1
	) {
		buildJupyterContext();
	}
}

function formatField(field: string) {
	const result = field
		.replace(/([A-Z])/g, ' $1')
		.trim()
		.toLowerCase();
	return result.charAt(0).toUpperCase() + result.slice(1);
}

// function submitGollmQuestion() {
// 	console.log(gollmQuestion.value);
// }

// FIXME: Copy pasted in 3 locations, could be written cleaner and in a service
const saveCodeToState = (hasCodeRun: boolean) => {
	const state = cloneDeep(props.node.state);
	state.hasCodeRun = hasCodeRun;
	console.log(state);
	// for now only save the last code executed, may want to save all code executed in the future
	const notebookHistoryLength = props.node.state.notebookHistory?.length ?? 0;
	const timestamp = Date.now();
	if (notebookHistoryLength > 0) {
		state.notebookHistory[0] = { code: code.value, timestamp };
	} else {
		state.notebookHistory.push({ code: code.value, timestamp });
	}
	return state;
};

function saveState() {
	const state = saveCodeToState(true);
	state.structuralComparisons = structuralComparisons.value;
	emit('update-state', state);
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
	console.log(messageContent, structuralComparisons.value);
	isLoadingStructuralComparisons.value = true;

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('display_data', (data) => {
			structuralComparisons.value.push(`data:image/png;base64,${data.content.data['image/png']}`);
			isLoadingStructuralComparisons.value = false;
			saveState();
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

function processCompareModels(modelIds) {
	compareModels(modelIds).then((response) => {
		llmAnswer.value = response.response;
	});
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
				models: modelsToCompare.value.map((model, index) => ({
					model_id: model.id,
					name: `model_${index + 1}`
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

onMounted(async () => {
	props.node.inputs.forEach((input) => {
		if (input.value) {
			addModelForComparison(input.value[0]);
		}
	});

	const modelIds = props.node.inputs
		.filter((input) => input.status === 'connected')
		.map((input) => input.value?.[0]);
	processCompareModels(modelIds);
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
table {
	& td,
	th {
		vertical-align: top;
		text-align: left;
		padding: 0 var(--gap) var(--gap) var(--gap-small);
		max-width: v-bind('cellWidth');
		overflow: auto;
		text-overflow: ellipsis;
	}

	& td {
		border-top: 1px solid var(--surface-border-light);
	}

	& td:first-child {
		width: 10%;
		padding: var(--gap-small) 0;
		font-weight: 600;
	}

	& td:not(:first-child) {
		padding: var(--gap-small);
		padding-right: var(--gap);
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
			margin-right: calc(var(--gap-xxlarge) * 3);
		}
	}
}

/* TODO: Improve this pattern later same in (tera-model-input) */
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
</style>
