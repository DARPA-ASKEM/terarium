<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<main>
			<tera-drilldown-section>
				<div class="toolbar-right-side">
					<Button
						icon="pi pi-play"
						label="Run"
						outlined
						severity="secondary"
						@click="runFromCode"
					/>
				</div>
				<Suspense>
					<tera-notebook-jupyter-input
						:kernel-manager="kernelManager"
						:defaultOptions="sampleAgentQuestions"
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
				<div>{{ notebookResponse }}</div>
			</tera-drilldown-preview>
		</main>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { KernelSessionManager } from '@/services/jupyter';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import { DecapodesOperationState } from './decapodes-operation';

const props = defineProps<{
	node: WorkflowNode<DecapodesOperationState>;
}>();

const emit = defineEmits(['close']);
const kernelManager = new KernelSessionManager();
let editor: VAceEditorInstance['_editor'] | null;

const modelId = computed(() => props.node?.inputs?.[0]?.value?.[0]);
const notebookResponse = ref();
const sampleAgentQuestions = ['Convert the model to equations please'];

const initializeEditor = (editorInstance: any) => {
	editor = editorInstance;
};
const codeText = ref(
	'# This environment contains the variable "model_config" to be read and updated'
);

const buildJupyterContext = () => ({
	context: 'decapodes',
	language: 'julia-1.10',
	context_info: {
		halfar: modelId.value ?? ''
	}
});

const appendCode = (data: any, property: string) => {
	codeText.value = codeText.value.concat(' \n', data.content[property] as string);
	runFromCode();
};

const runFromCode = () => {
	const code = editor?.getValue();
	if (!code) return;

	const messageContent = {
		silent: false,
		store_history: false,
		code
	};

	kernelManager
		.sendMessage('execute_request', messageContent)
		.register('execute_input', (data) => {
			console.log('execute_input', data.content);
		})
		.register('stream', (data) => {
			console.log('stream', data.content);
		})
		.register('error', (data) => {
			console.log('error', data.content.evalue);
		})
		.register('decapodes_preview', (data) => {
			console.log('decapodes_preview', data.content);
			notebookResponse.value = data.content['application/json'];
		});
};

const initialize = async () => {
	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				// when coming from output dropdown change we should shutdown first
				await kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

onMounted(async () => {
	await initialize();
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>
