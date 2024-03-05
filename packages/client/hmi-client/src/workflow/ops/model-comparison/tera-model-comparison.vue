<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="Tabs.Wizard">
			<tera-drilldown-section>
				<Dropdown
					:editable="true"
					class="input"
					v-model="gollmQuestion"
					type="text"
					:placeholder="'What would you like to compare?'"
					@keydown.enter="submitGollmQuestion"
				/>
				<Panel v-if="llmAnswer || !isEmpty(llmThoughts)" header="Answer" toggleable>
					<template #togglericon="{ collapsed }">
						<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
					</template>
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
			<tera-drilldown-section>
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
				<Dropdown
					:editable="true"
					:disabled="!isKernelReady"
					class="input"
					v-model="beakerQuestion"
					type="text"
					:placeholder="!isKernelReady ? 'Please wait...' : 'What would you like to compare?'"
					@keydown.enter="submitBeakerQuestion"
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
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { onMounted, onUnmounted, ref, computed } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import Panel from 'primevue/panel';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { WorkflowNode } from '@/types/workflow';
import { getModel } from '@/services/model';
import type { Model } from '@/types/Types';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
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

const gollmQuestion = ref('');
const beakerQuestion = ref('');
const llmThoughts = ref<string[]>([]);
const llmAnswer = ref('');
const code = ref('');
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

const kernelManager = new KernelSessionManager();

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

function submitGollmQuestion() {
	console.log(gollmQuestion.value);
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

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('execute_input', (data) => {
			console.log(data);
		})
		.register('stream', (data) => {
			console.log('stream', data);
		})
		.register('error', (data) => {
			logger.error(`${data.content.ename}: ${data.content.evalue}`);
			console.log('error', data.content);
		});
}

function submitBeakerQuestion() {
	console.log(beakerQuestion.value);
	kernelManager
		.sendMessage('llm_request', {
			request: beakerQuestion.value
		})
		.register('llm_thought', (d) => {
			console.log(d.content);
			llmThoughts.value.push(d.content.thought);
		})
		.register('code_cell', (d) => {
			console.log(d);
			code.value = d.content.code;
		});
}

async function addModelForComparison(modelId: string) {
	const model = await getModel(modelId);
	if (model) modelsToCompare.value.push(model);
	if (modelsToCompare.value.length === 3) buildJupyterContext();
}

async function buildJupyterContext() {
	if (modelsToCompare.value.length < 3) {
		logger.warn('Cannot build Jupyter context without models');
		return;
	}

	console.log({
		models: modelsToCompare.value.map((model, index) => ({
			model_id: model.id,
			name: `model_${index + 1}`
		}))
	});

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

.toolbar-right-side {
	display: flex;
	justify-content: right;
}
</style>
