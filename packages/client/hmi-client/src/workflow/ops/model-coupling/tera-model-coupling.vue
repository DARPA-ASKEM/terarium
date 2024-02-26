<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="ModelCouplingTabgs.Wizard">
			<tera-drilldown-section> </tera-drilldown-section>
		</div>
		<div :tabName="ModelCouplingTabgs.Notebook">
			<tera-drilldown-section>
				<div class="notebook-toolbar">
					<div class="toolbar-left-side">
						<Dropdown
							v-model="selectedLanguage"
							optionLabel="name"
							:options="languages"
							outlined
							placeholder="Language"
							disabled
						></Dropdown>
					</div>
					<div class="toolbar-right-side">
						<Button
							class="mr-auto"
							icon="pi pi-play"
							label="Run"
							outlined
							severity="secondary"
							size="small"
							@click="runCodeModelCoupling"
						/>
					</div>
				</div>

				<v-ace-editor
					v-model:value="codeText"
					@init="initializeEditor"
					lang="julia"
					theme="chrome"
					style="flex-grow: 1; width: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>
				<!--
				<template #footer>
					<Button style="margin-right: auto" icon="pi pi-play" label="Run" size="large" @click="runCodeModelCoupling" />
				</template> -->
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview>
				<div>
					<div v-if="modelCouplingResult">
						{{ modelCouplingResult }}
					</div>
					<div v-else class="empty-state-container">
						<img src="@assets/svg/plants.svg" alt="" draggable="false" class="empty-state-image" />
						<p>No model provided</p>
					</div>
				</div>
				<template #footer>
					<InputText
						v-model="newModelName"
						placeholder="model name"
						type="text"
						class="input-small white-space-nowrap"
					/>
					<div class="w-full flex gap-2 justify-content-end">
						<Button
							:disabled="!modelCouplingResult"
							outlined
							class="white-space-nowrap"
							size="large"
							label="Save as new model"
							@click="
								() =>
									saveNewModel(newModelName, {
										addToProject: true,
										appendOutputPort: true
									})
							"
						/>
						<Button label="Close" size="large" @click="emit('close')" />
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { AssetType } from '@/types/Types';
import { createModel, getModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import Dropdown from 'primevue/dropdown';

/* Jupyter imports */
import { KernelSessionManager } from '@/services/jupyter';
import { ModelCouplingState } from './model-coupling-operation';

const props = defineProps<{
	node: WorkflowNode<ModelCouplingState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);
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

const modelMap = ref<{ [key: string]: string }>({});
const codeText = ref('');

/**
 * Scrub model name string to be compatible with Julia variable names and transporting
 * across JSON.
 * */
const scrubVariableName = (v: string) => v.replace(/[^\w\s]/gi, '');

let editor: VAceEditorInstance['_editor'] | null;

/** working example Jan 2024
# halfar: 'cde2b856-114e-4008-8493-b0d93361fa72',
# glen: '97eb6e11-05cb-4ffe-9556-980a8d287c36'

ice_dynamics_composition_diagram = @relation () begin
  dynamics(Γ,n)
  stress(Γ,n)
end
ice_dynamics_cospan = oapply(ice_dynamics_composition_diagram,
  [
		Open(halfar, [:Γ,:n]),
		Open(glen, [:Γ,:n])
	]
)

decapode = apex(ice_dynamics_cospan)
* */

const buildJupyterContext = () => ({
	context: 'decapodes',
	language: 'julia-1.10',
	context_info: modelMap.value
});

const initialize = async () => {
	if (kernelManager) {
		kernelManager.shutdown();
	}

	// Create a new session and context based on model
	try {
		await kernelManager.init('beaker_kernel', 'Beaker Kernel', buildJupyterContext());
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
		await projectResource.addAsset(AssetType.Model, modelData.id, projectId);
	}

	if (options.appendOutputPort) {
		emit('append-output', {
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

onUnmounted(() => {
	kernelManager.shutdown();
});

watch(
	() => props.node.inputs,
	async () => {
		const inputs = props.node.inputs;

		// reset
		modelMap.value = {};
		codeText.value = '# The following <variable>: <model> mappings are available\n';
		for (let i = 0; i < inputs.length; i++) {
			if (inputs[i].value) {
				// eslint-disable-next-line no-await-in-loop
				const model = await getModel(inputs[i].value?.[0]);
				if (!model) continue;

				// Save name:id mapping for context and comment message
				const safeName = scrubVariableName(model.header.name);
				modelMap.value[safeName] = model.id ?? '';
				codeText.value += `# - ${safeName}: ${model.header.name}\n`;
			}
		}
		initialize();
	},
	{ immediate: true }
);

const selectedLanguage = ref({ name: 'Julia' });
const languages = ref([{ name: 'Julia' }, { name: 'Python' }]);
</script>

<style scoped>
.code-container {
	display: flex;
	flex-direction: column;
}

.input-small {
	padding: 0.5rem;
	width: 100%;
}

.empty-state-container {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	gap: 1rem;
	min-height: calc(100vh - 18rem);
}

.empty-state-image {
	height: 10rem;
}

.notebook-toolbar {
	display: flex;
	flex-direction: row;
	gap: 1rem;
	justify-content: space-between;
}
</style>
