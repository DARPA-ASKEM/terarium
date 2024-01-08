<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<Steps :model="formSteps" :readonly="false" @update:active-step="activeIndex = $event" />
				<div v-if="activeIndex === 0" class="form-section">
					<h3>Name</h3>
					<InputText
						placeholder="Enter a name for this configuration"
						v-model="configName"
						@update:model-value="() => debouncedUpdateState({ configName })"
					/>
					<h3>Description</h3>
					<Textarea
						placeholder="Enter a description"
						v-model="configDescription"
						@update:model-value="() => debouncedUpdateState({ configDescription })"
					/>
				</div>
				<div v-else-if="activeIndex === 1">
					<tera-model-config-editor
						v-if="model"
						:model="model"
						:initials="configInitials"
						:parameters="configParams"
						@update-param="updateConfigParam"
						@update-initial="updateConfigInitial"
					/>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<h4>TODO</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				is-selectable
			>
				<tera-model-semantic-tables
					v-if="configCache[selectedConfigId]"
					:model="configCache[selectedConfigId].configuration"
					:is-editable="false"
				/>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="saveConfiguration"
			/>
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Steps from 'primevue/steps';
import Textarea from 'primevue/textarea';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraModelSemanticTables from '@/components/model/petrinet/tera-model-semantic-tables.vue';
import { getModel } from '@/services/model';
import {
	createModelConfiguration,
	getModelConfigurationById
} from '@/services/model-configurations';
import { Model, ModelConfiguration, Initial, ModelParameter } from '@/types/Types';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';
import teraModelConfigEditor from './tera-model-config-editor.vue';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits([
	'append-output-port',
	'update-state',
	'select-output',
	'update-output-port',
	'close'
]);

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const formSteps = ref([
	{
		label: 'Context'
	},
	{
		label: 'Set values'
	}
]);

const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const configCache = ref<Record<string, ModelConfiguration>>({});

const activeIndex = ref<number>(0);
const configName = ref<string>(props.node.state.configName);
const configDescription = ref<string>(props.node.state.configDescription);
const model = ref<Model>();

const configInitials = ref<Initial[]>();
const configParams = ref<ModelParameter[]>();

onMounted(async () => {
	const input = props.node.inputs[0];
	if (input.value) {
		const m = await getModel(input.value[0]);
		if (m) {
			model.value = m;

			// TODO: set this only if configs don't exist already
			// set the state with the model initials and params
			const state = _.cloneDeep(props.node.state);
			state.configInitials = m.semantics?.ode.initials;
			state.configParams = m.semantics?.ode.parameters;
			emit('update-state', state);
		}
	}
});

const updateState = (updatedField) => {
	let state = _.cloneDeep(props.node.state);
	state = { ...state, ...updatedField };
	emit('update-state', state);
};

const updateConfigParam = (param) => {
	const state = _.cloneDeep(props.node.state);
	// find param with the same id and update it
	state.configParams = state.configParams?.map((p) => {
		if (p.id === param.id) {
			return param;
		}
		return p;
	});
	emit('update-state', state);
};

const updateConfigInitial = (initial) => {
	const state = _.cloneDeep(props.node.state);
	// find initial with the same target and update it
	state.configInitials = state.configInitials?.map((i) => {
		if (i.target === initial.target) {
			return initial;
		}
		return i;
	});
	emit('update-state', state);
};

const saveConfiguration = async () => {
	if (model.value) {
		const newModel = _.cloneDeep(model.value);
		if (newModel.semantics) {
			newModel.semantics.ode.initials = configInitials.value;
			newModel.semantics.ode.parameters = configParams.value;
		}

		const data = await createModelConfiguration(
			model.value.id,
			configName.value,
			configDescription.value,
			newModel
		);

		setTimeout(async () => {
			const modelConfig = await getModelConfigurationById(data.id);
			configCache.value[modelConfig.id] = modelConfig;

			emit('append-output-port', {
				type: ModelConfigOperation.outputs[0].type,
				label: modelConfig.name,
				value: modelConfig.id,
				isSelected: false,
				state: {
					modelId: modelConfig.modelId,
					configName: modelConfig.name,
					configDescription: modelConfig.description,
					configInitials: modelConfig.configuration.semantics?.ode.initials,
					configParams: modelConfig.configuration.semantics?.ode.parameters
				}
			});
		}, 1000);
	}
};

const debouncedUpdateState = _.debounce(updateState, 500);

watch(
	() => props.node.state,
	() => {
		configInitials.value = props.node.state.configInitials;
		configParams.value = props.node.state.configParams;
	},
	{ immediate: true }
);

const onUpdateOutput = (id) => {
	emit('select-output', id);
};

const onUpdateSelection = (id) => {
	const outputPort = _.cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
};

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
		// Fill the form with the config data
		configName.value = props.node.state.configName;
		configDescription.value = props.node.state.configDescription;
		configInitials.value = props.node.state.configInitials;
		configParams.value = props.node.state.configParams;
	},
	{ immediate: true }
);

const lazyLoadModelConfig = async (configId: string) => {
	if (!configId || configCache.value[configId]) return;

	const config = await getModelConfigurationById(configId);
	if (config) {
		configCache.value[configId] = config;
	}
};

watch(
	() => selectedConfigId.value,
	() => {
		lazyLoadModelConfig(selectedConfigId.value);
	},
	{ immediate: true }
);
</script>

<style scoped>
.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}
</style>
