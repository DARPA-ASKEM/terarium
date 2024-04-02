<!-- FIXME: decapodes with LLM isnt entirely functional, this is more of a placeholder at the moment -->
<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-output-dropdown
				@click.stop
				:output="selectedOutputId"
				is-selectable
				:options="outputs"
			/>
		</template>
		<main>
			<tera-drilldown-section>
				<template #footer>
					<Button
						icon="pi pi-play"
						label="Run"
						outlined
						severity="secondary"
						@click="runFromCode"
						:loading="isRunningCode"
					/>
				</template>
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:defaultOptions="sampleAgentQuestions"
						:context-language="contextLanguage"
						@llm-output="(data: any) => appendCode(data, 'code')"
					/>
				</Suspense>
				<v-ace-editor
					v-model:value="codeText"
					@init="initializeEditor"
					lang="python"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>
			</tera-drilldown-section>

			<tera-drilldown-preview title="Output Preview">
				<tera-notebook-error
					v-if="executeResponse.status === OperatorStatus.ERROR"
					:name="executeResponse.name"
					:value="executeResponse.value"
					:traceback="executeResponse.traceback"
				/>
				<div v-if="executeResponse.status !== OperatorStatus.ERROR">{{ notebookResponse }}</div>
			</tera-drilldown-preview>
		</main>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import { VAceEditor } from 'vue3-ace-editor';
import { WorkflowNode, OperatorStatus } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { KernelSessionManager } from '@/services/jupyter';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import { getModel, getModelType } from '@/services/model';
import { AMRSchemaNames } from '@/types/common';
import { DecapodesOperationState } from './decapodes-operation';

const props = defineProps<{
	node: WorkflowNode<DecapodesOperationState>;
}>();

const emit = defineEmits(['close', 'update-state']);
const kernelManager = new KernelSessionManager();
let editor: VAceEditorInstance['_editor'] | null;

const modelId = computed(() => props.node.inputs?.[0]?.value?.[0]);
const notebookResponse = ref();
const executeResponse = ref({
	status: OperatorStatus.DEFAULT,
	name: '',
	value: '',
	traceback: ''
});

const sampleAgentQuestions = ['Convert the model to equations please'];
const contextLanguage = 'julia-1.10';
const isRunningCode = ref<boolean>(false);
const selectedOutputId = ref<string>(props.node.active ?? '');
const defaultCodeText =
	'# This environment allows you to work with a decapode model that was passed in.';
const outputs = computed(() => {
	if (!isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const initializeEditor = (editorInstance: any) => {
	editor = editorInstance;
};
const codeText = ref();

const buildJupyterContext = () => ({
	context: 'decapodes',
	language: 'julia-1.10',
	context_info: {
		decapode_id: modelId.value ?? ''
	}
});

const appendCode = (data: any, property: string) => {
	codeText.value = codeText.value.concat(' \n', data.content[property] as string);
	saveCodeToState(codeText.value);
};

const runFromCode = () => {
	const code = editor?.getValue();
	if (!code) return;

	const messageContent = {
		silent: false,
		store_history: false,
		code
	};

	isRunningCode.value = true;

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('execute_input', (data) => {
			console.log('execute_input', data.content);
		})
		.register('stream', (data) => {
			console.log('stream', data.content);
		})
		// FIXME: There isnt really a proper response we receive for llm output for decapodes
		.register('decapodes_preview', (data) => {
			console.log('decapodes_preview', data.content);
			notebookResponse.value = data.content['application/json'];
			isRunningCode.value = false;
		})
		.register('any_execute_reply', (data) => {
			isRunningCode.value = false;
			let status = OperatorStatus.DEFAULT;
			if (data.msg.content.status === 'ok') status = OperatorStatus.SUCCESS;
			if (data.msg.content.status === 'error') status = OperatorStatus.ERROR;
			executeResponse.value = {
				status,
				name: data.msg.content.ename ? data.msg.content.ename : '',
				value: data.msg.content.evalue ? data.msg.content.evalue : '',
				traceback: data.msg.content.traceback ? data.msg.content.traceback : ''
			};
		});
};

const saveCodeToState = (code: string) => {
	const state = cloneDeep(props.node.state);

	// for now only save the last code executed, may want to save all code executed in the future
	const codeHistoryLength = props.node.state.codeHistory.length;
	const timestamp = Date.now();
	if (codeHistoryLength > 0) {
		state.codeHistory[0] = { code, timestamp };
	} else {
		state.codeHistory.push({ code, timestamp });
	}

	emit('update-state', state);
};

const inputChangeHandler = async () => {
	if (!modelId.value) return;

	const model = await getModel(modelId.value);

	if (getModelType(model) !== AMRSchemaNames.DECAPODES) return;

	codeText.value = props.node.state.codeHistory?.[0]?.code ?? defaultCodeText;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;

			await inputChangeHandler();
		}
	},
	{ immediate: true }
);

onMounted(async () => {
	await inputChangeHandler();
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>
