<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="ModelCouplingTabgs.Wizard">
			<tera-drilldown-section> </tera-drilldown-section>
		</div>
		<div :tabName="ModelCouplingTabgs.Notebook">
			<tera-drilldown-section>
				<h4>Code Editor - Julia</h4>
				{{ modelMap }}
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
					<div v-if="modelCouplingResult">
						{{ modelCouplingResult }}
					</div>
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
						:disabled="!modelCouplingResult"
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
import { ref, computed, onUnmounted, onMounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { AssetType } from '@/types/Types';
import { createModel } from '@/services/model';
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
const modelCouplingResult = ref<any>(null);

enum ModelCouplingTabgs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface SaveOptions {
	addToProject?: boolean;
	appendOutputPort?: boolean;
}

const kernelManager = new KernelSessionManager();

const newModelName = ref('');

// Generates name-id pairings for beaker kernel context_info
const modelMap = computed(() => {
	const map: { [key: string]: string } = {};
	const inputs = props.node.inputs;
	for (let i = 0; i < inputs.length; i++) {
		if (inputs[i].value) {
			map[`model_${i}`] = inputs[i].value?.[0];
		}
	}
	return map;
});

let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

/**
ice_dynamics_composition_diagram = @relation () begin
  dynamics(Γ,n)
  stress(Γ,n)
end
ice_dynamics_cospan = oapply(ice_dynamics_composition_diagram,
  [
		Open(halfar_eq2, [:Γ,:n]),
		Open(glens_law, [:Γ,:n])
	]
)

decapode = apex(ice_dynamics_cospan)


# halfar: 'cde2b856-114e-4008-8493-b0d93361fa72',
# glen: '97eb6e11-05cb-4ffe-9556-980a8d287c36'
* */

const buildJupyterContext = () => ({
	context: 'decapodes',
	language: 'julia-1.9',
	context_info: modelMap.value
});

const initialize = async () => {
	const modelId = props.node.inputs[0].value?.[0];
	if (!modelId) return;

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
	const projectResource = useProjects();
	const projectId = projectResource.activeProject.value?.id;

	const header = {
		description: modelName,
		name: modelName,
		_type: 'Header',
		model_version: 'v1.0',
		schema: 'modelreps.io/DecaExpr',
		schema_name: 'decapode'
	};
	const amr = {
		header,
		model: modelCouplingResult.value
	};
	const modelData = await createModel(amr);

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

	// FIXME: reset model doesn't exist yet, wait for beaker update
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
			modelCouplingResult.value = data.content['application/json'];
		});
};

onMounted(() => {
	initialize();
});

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
</style>
