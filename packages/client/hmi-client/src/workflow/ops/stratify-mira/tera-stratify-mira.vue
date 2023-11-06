<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === StratifyTabs.wizard"
			@click="activeTab = StratifyTabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === StratifyTabs.notebook"
			@click="activeTab = StratifyTabs.notebook"
		/>
	</div>
	<div v-if="activeTab === StratifyTabs.wizard" class="container">
		<div class="left-side">
			<h4>Stratify Model <i class="pi pi-info-circle" /></h4>
			<p>The model will be stratified with the following settings.</p>
			<tera-stratification-group-form
				v-for="(cfg, index) in node.state.strataGroups"
				:key="index"
				:modelNodeOptions="modelNodeOptions"
				:config="cfg"
				:index="index"
				@delete-self="deleteStratifyGroupForm"
				@update-self="updateStratifyGroupForm"
			/>
			<Button label="Add another strata group" size="small" @click="addGroupForm" />
			<Button label="Stratify" size="small" @click="stratifyModel" />

			<div>
				<p>{{ node.state }}</p>
			</div>
		</div>
		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Model Provided</h4>
			</div>
		</div>
	</div>
	<div v-else class="container">
		<div class="left-side">
			<Suspense>
				<tera-mira-notebook />
			</Suspense>
		</div>
		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Model Provided</h4>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { watch, ref, onMounted, onUnmounted } from 'vue';
import Button from 'primevue/button';
import teraStratificationGroupForm from '@/components/stratification/tera-stratification-group-form.vue';
import teraMiraNotebook from '@/components/stratification/tera-mira-notebook.vue';
import { Model, ModelConfiguration } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';

/* Jupyter imports */
import { newSession, JupyterMessage, createMessageId } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { StratifyOperationStateMira, StratifyGroup } from './stratify-mira-operation';

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();

enum StratifyTabs {
	wizard,
	notebook
}

const jupyterSession = ref<SessionContext | null>(null);

const activeTab = ref(StratifyTabs.wizard);
const modelConfiguration = ref<ModelConfiguration>();
const model = ref<Model | null>(null);
const modelNodeOptions = ref<string[]>([]);
const teraModelDiagramRef = ref();

const addGroupForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: StratifyGroup = {
		borderColour: '#00c387',
		name: '',
		selectedVariables: [],
		groupLabels: '',
		cartesianProduct: true,
		isPending: true
	};
	state.strataGroups.push(newGroup);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const deleteStratifyGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.strataGroups.splice(data.index, 1);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const updateStratifyGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	state.strataGroups[data.index] = data.updatedConfig;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const stratifyModel = () => {};

// Set model, modelConfiguration, modelNodeOptions
watch(
	() => props.node.inputs[0],
	async () => {
		const modelConfigurationId = props.node.inputs[0].value?.[0];
		if (modelConfigurationId) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
			if (modelConfiguration.value) {
				model.value = await getModel(modelConfiguration.value.modelId);

				const modelColumnNameOptions: string[] =
					modelConfiguration.value.configuration.model.states.map((state) => state.id);
				// add observables
				if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
					modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
						modelColumnNameOptions.push(o.id);
					});
				}
				modelNodeOptions.value = modelColumnNameOptions;
			}
		}
	},
	{ immediate: true }
);

const stratifyRequest = () => {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) return;

	console.log('test stratification');

	const messageBody = {
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			stratify_args: {
				key: 'city',
				strata: ['nyc', 'boston', 'toronto', 'vancouver', 'calgary']
			}
		},
		msgType: 'stratify_request',
		msgId: createMessageId('stratify_request')
	};
	const contextMessage: JupyterMessage = createMessage(messageBody);
	kernel.sendJupyterMessage(contextMessage);
};

const setKernelContext = () => {
	const kernel = jupyterSession.value?.session?.kernel;
	if (!kernel) {
		return;
	}

	const messageBody = {
		session: jupyterSession?.value?.name || '',
		channel: 'shell',
		content: {
			context: 'mira_model',
			language: 'python3',
			context_info: {
				id: 'sir-model-id'
			}
		},
		msgType: 'context_setup_request',
		msgId: createMessageId('context_setup')
	};
	const contextMessage: JupyterMessage = createMessage(messageBody);
	kernel.sendJupyterMessage(contextMessage);
};

const iopubMessageHandler = (_session, message: any) => {
	if (message.header.msg_type === 'status') {
		return;
	}
	console.log('');
	console.log('message received', message);
	console.log('');

	const msgType = message.header.msg_type;

	if (msgType === 'stratify_response') {
		console.log(message.content);
	} else if (msgType === 'model_preview') {
		console.log(message.content);
	}

	// if (message.header.msg_type === 'compile_expr_response') {
	// 	// TODO
	// } else if (message.header.msg_type === 'decapodes_preview') {
	// 	// TODO
	// } else if (message.header.msg_type === 'construct_amr_response') {
	// 	// TODO
	// } else if (message.header.msg_type === 'save_model_response') {
	// 	// TODO
	// }
};

onMounted(async () => {
	const session = await newSession('beaker', 'Beaker');
	jupyterSession.value = session;

	session.kernelChanged.connect((_context, kernelInfo) => {
		if (!kernelInfo.newValue) return;

		const kernel = kernelInfo.newValue;
		if (kernel?.name === 'beaker') {
			session.iopubMessage.connect(iopubMessageHandler);
			setKernelContext();
		}

		setTimeout(() => {
			stratifyRequest();
		}, 3000);
	});
});

onUnmounted(async () => {
	await jupyterSession.value?.shutdown();
});
</script>

<style scoped>
.container {
	display: flex;
	margin-top: 1rem;
}

.left-side {
	width: 45%;
	padding-right: 2.5%;
}
.left-side h1 {
	color: var(--text-color-primary);
	font-family: Inter;
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem; /* 150% */
	letter-spacing: 0.03125rem;
}
.left-side p {
	color: var(--Text-Secondary);
	/* Body Small/Regular */
	font-family: Figtree;
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}

.right-side {
	width: 45%;
	padding-left: 2.5%;
}
</style>
