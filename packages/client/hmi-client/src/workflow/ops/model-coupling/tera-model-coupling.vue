<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="ModelCouplingTabgs.Wizard">
			<tera-drilldown-section> </tera-drilldown-section>
		</div>
		<div :tabName="ModelCouplingTabgs.Notebook">
			<tera-drilldown-section>
				<h4>Code Editor - Julia</h4>
				{{ modelids }}
				<v-ace-editor
					v-model:value="codeText"
					@init="initializeEditor"
					lang="julia"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
				/>

				<template #footer>
					<Button style="margin-right: auto" label="Run" @click="runCodeModelCoupling" />
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview>
				<div>
					<tera-model-diagram
						v-if="amr"
						ref="teraModelDiagramRef"
						:model="amr"
						:is-editable="false"
					/>
					<div v-else>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
						<h4>No Model Provided</h4>
					</div>
				</div>
				<template #footer>
					<InputText
						v-model="newModelName"
						placeholder="model name"
						type="text"
						class="input-small"
					/>
					<Button
						:disabled="!amr"
						outlined
						style="margin-right: auto"
						label="Save as new Model"
						@click="
							() => saveNewModel(newModelName, { addToProject: true, appendOutputPort: true })
						"
					/>
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { watch, ref, computed, onUnmounted, onMounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { Model, AssetType } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { getModel, createModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

/* Jupyter imports */
import { KernelSessionManager } from '@/services/jupyter';
import { ModelCouplingState } from './model-coupling-operation';

const props = defineProps<{
	node: WorkflowNode<ModelCouplingState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum ModelCouplingTabgs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface SaveOptions {
	addToProject?: boolean;
	appendOutputPort?: boolean;
}

const kernelManager = new KernelSessionManager();

const amr = ref<Model | null>(null);
const teraModelDiagramRef = ref();
const newModelName = ref('');

const modelids = computed(() => {
	const ids: string[] = [];
	const inputs = props.node.inputs;
	for (let i = 0; i < inputs.length; i++) {
		if (inputs[i].value) {
			ids.push(inputs[i].value?.[0]);
		}
	}
	return ids;
});

let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

// const resetModel = () => {
// 	if (!amr.value) return;
//
// 	kernelManager
// 		.sendMessage('reset_request', {})
// 		?.register('reset_response', handleResetResponse)
// 		?.register('model_preview', handleModelPreview);
// };

// const handleModelPreview = (data: any) => {
// 	amr.value = data.content['application/json'];
// };

const buildJupyterContext = () => {
	if (!amr.value) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}

	return {
		context: 'decapodes',
		language: 'julia-1.9',
		context_info: {
			modelA: '98124914-c432-49ec-98b6-5519c8aefb12'
			// id: amr.value.id
			// 'modelB': '98124914-c432-49ec-98b6-5519c8aefb12'
		}
	};
};

const inputChangeHandler = async () => {
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

	amr.value = await getModel(modelId);
	if (!amr.value) return;

	// Create a new session and context based on model
	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const saveNewModel = async (modelName: string, options: SaveOptions) => {
	if (!amr.value || !modelName) return;
	amr.value.header.name = modelName;

	const projectResource = useProjects();
	const modelData = await createModel(amr.value);
	const projectId = projectResource.activeProject.value?.id;

	if (!modelData) return;

	if (options.addToProject) {
		await projectResource.addAsset(AssetType.Models, modelData.id, projectId);
	}

	if (options.appendOutputPort) {
		emit('append-output-port', {
			id: uuidv4(),
			label: modelName,
			type: 'modelId',
			value: [modelData.id]
		});
		emit('close');
	}
};

const initializeEditor = (editorInstance: any) => {
	editor = editorInstance;
};

const runCodeModelCoupling = () => {
	const code = editor?.getValue();
	if (!code) return;

	// reset model
	kernelManager.sendMessage('reset_request', {});

	const messageContent = {
		silent: false,
		store_history: false,
		user_expressions: {},
		allow_stdin: true,
		stop_on_error: false,
		code
	};
	console.log('messageContent', messageContent);

	kernelManager
		.sendMessage('execute_request', messageContent)
		?.register('execute_input', (data) => {
			console.log('execute_input', data.content);
		})
		?.register('stream', (data) => {
			console.log('stream', data.content);
		})
		?.register('error', (data) => {
			console.log('error', data.content.evalue);
		})
		?.register('decapodes_preview', (data) => {
			console.log('decapodes_preview', data.content);
		});
};

// Set model, modelConfiguration, modelNodeOptions
watch(
	() => props.node.inputs,
	async () => {
		await inputChangeHandler();
	},
	{ immediate: true }
);

onMounted(() => {});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
.code-container {
	display: flex;
	flex-direction: column;
}

.input-small {
	padding: 0.5rem;
}

.code-executed-warning {
	background-color: #ffe6e6;
	color: #cc0000;
	padding: 10px;
	border-radius: 4px;
}
</style>
