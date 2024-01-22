<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<tera-output-dropdown
					style="margin-left: auto"
					:output="selectedOutputId"
					is-selectable
					:options="outputs"
					@update:output="onUpdateOutput"
					@update:selection="onUpdateSelection"
				/>
				<Steps :model="formSteps" :readonly="false" @update:active-step="activeIndex = $event" />
				<div v-if="activeIndex === 0" class="form-section">
					<h3>Name</h3>
					<InputText
						placeholder="Enter a name for this configuration"
						v-model="configName"
						@update:model-value="() => debouncedUpdateState({ name: configName })"
					/>
					<h3>Description</h3>
					<Textarea
						placeholder="Enter a description"
						v-model="configDescription"
						@update:model-value="() => debouncedUpdateState({ description: configDescription })"
					/>
				</div>
				<div v-else-if="activeIndex === 1">
					<Accordion multiple :active-index="[0, 1]">
						<AccordionTab header="Initials">
							<tera-model-config-table
								v-if="modelConfiguration"
								:model-configuration="modelConfiguration"
								:data="tableFormattedInitials"
								:stratified-model-type="stratifiedModelType"
								:table-type="StratifiedMatrix.Initials"
								@update-value="updateConfigInitial"
							/>
						</AccordionTab>
						<AccordionTab header="Parameters">
							<tera-model-config-table
								v-if="modelConfiguration"
								:model-configuration="modelConfiguration"
								:data="tableFormattedParams"
								:stratified-model-type="stratifiedModelType"
								:table-type="StratifiedMatrix.Parameters"
								@update-value="updateConfigParam"
							/>
						</AccordionTab>
					</Accordion>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<h4>TODO</h4>
		</section>
		<template #footer>
			<Button
				outlined
				:disabled="!configName"
				label="Run Configuration"
				icon="pi pi-play"
				@click="createConfiguration"
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
import { getModel } from '@/services/model';
import {
	createModelConfiguration,
	getModelConfigurationById
} from '@/services/model-configurations';
import type { Model, ModelConfiguration, Initial, ModelParameter } from '@/types/Types';
import { ParamType } from '@/types/common';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import {
	getUnstratifiedInitials,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';
import { StratifiedMatrix } from '@/types/Model';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraOutputDropdown from '@/components/widgets/tera-output-dropdown.vue';
import { useToastService } from '@/services/toast';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';
import TeraModelConfigTable from './tera-model-config-table.vue';

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();

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

const emit = defineEmits([
	'append-output-port',
	'update-state',
	'select-output',
	'update-output-port',
	'close'
]);

const formSteps = ref([{ label: 'Context' }, { label: 'Set values' }]);

const selectedOutputId = ref<string>();
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const configCache = ref<Record<string, ModelConfiguration>>({});

const activeIndex = ref<number>(0);
const configName = ref<string>(props.node.state.name);
const configDescription = ref<string>(props.node.state.description);
const model = ref<Model>();

const modelConfiguration = computed<ModelConfiguration | null>(() => {
	if (!model.value) return null;

	const cloneModel = _.cloneDeep(model.value);
	if (cloneModel.semantics) {
		cloneModel.semantics.ode.initials = configInitials.value;
		cloneModel.semantics.ode.parameters = configParams.value;
	}
	const modelConfig: ModelConfiguration = {
		id: '',
		name: '',
		modelId: cloneModel.id,
		configuration: cloneModel
	};
	return modelConfig;
});

const configInitials = ref<Initial[]>();
const configParams = ref<ModelParameter[]>();

const stratifiedModelType = computed(() => {
	if (!model.value) return null;
	return getStratificationType(model.value);
});

const parameters = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	return getUnstratifiedParameters(model.value);
});

const initials = computed<Map<string, string[]>>(() => {
	if (!model.value) return new Map();
	return getUnstratifiedInitials(model.value);
});

const tableFormattedInitials = computed(() => {
	const formattedInitials: any[] = [];

	if (stratifiedModelType.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix = vals.map((v) => {
				const initial = configInitials.value?.find((i) => i.target === v);
				return {
					id: v,
					name: v,
					type: ParamType.EXPRESSION,
					value: initial,
					source: '',
					visibility: false
				};
			});
			formattedInitials.push({
				id: init,
				name: init,
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				values: vals,
				tableFormattedMatrix
			});
		});
	} else {
		initials.value.forEach((vals, init) => {
			formattedInitials.push({
				id: init,
				name: init,
				type: ParamType.EXPRESSION,
				value: configInitials.value?.find((i) => i.target === vals[0]),
				source: '',
				visibility: false
			});
		});
	}

	return formattedInitials;
});

const tableFormattedParams = computed(() => {
	const formattedParams: any[] = [];

	if (stratifiedModelType.value) {
		parameters.value.forEach((vals, init) => {
			const tableFormattedMatrix = vals.map((v) => {
				const param = configParams.value?.find((i) => i.id === v);
				const paramType = param?.distribution ? ParamType.DISTRIBUTION : ParamType.CONSTANT;
				return {
					id: v,
					name: v,
					type: paramType,
					value: param,
					source: '',
					visibility: false
				};
			});
			formattedParams.push({
				id: init,
				name: init,
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				tableFormattedMatrix
			});
		});
	} else {
		parameters.value.forEach((vals, init) => {
			const param = configParams.value?.find((i) => i.id === vals[0]);
			const paramType = param?.distribution ? ParamType.DISTRIBUTION : ParamType.CONSTANT;
			formattedParams.push({
				id: init,
				name: init,
				type: paramType,
				value: param,
				source: '',
				visibility: false
			});
		});
	}

	return formattedParams;
});

const updateState = (updatedField) => {
	let state = _.cloneDeep(props.node.state);
	state = { ...state, ...updatedField };
	emit('update-state', state);
};

const updateConfigParam = (params: ModelParameter[]) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.parameters) return;

	for (let i = 0; i < state.parameters.length; i++) {
		const foundParam = params.find((p) => p.id === state.parameters![i].id);
		if (foundParam) {
			state.parameters[i] = foundParam;
		}
	}
	emit('update-state', state);
};

const updateConfigInitial = (inits: Initial[]) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.initials) return;

	for (let i = 0; i < state.initials.length; i++) {
		const foundInitial = inits.find((init) => init.target === state.initials![i].target);
		if (foundInitial) {
			state.initials[i] = foundInitial;
		}
	}
	emit('update-state', state);
};

const createConfiguration = async () => {
	if (!model.value) return;

	const state = _.cloneDeep(props.node.state);

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

	if (!data) {
		useToastService().error('', 'Failed to create model configuration');
		return;
	}

	useToastService().success('', 'Created model configuration');
	emit('append-output-port', {
		type: ModelConfigOperation.outputs[0].type,
		label: state.name,
		value: data.id,
		isSelected: false,
		state
	});

	setTimeout(async () => {
		const modelConfig = await getModelConfigurationById(data.id);
		// TODO: do we need to cache here at all?
		// model configs aren't too large so we could potentially just fetch each time
		configCache.value[modelConfig.id] = modelConfig;
	}, 1000);
};

const debouncedUpdateState = _.debounce(updateState, 500);

const lazyLoadModelConfig = async (configId: string) => {
	if (!configId || configCache.value[configId]) return;

	const config = await getModelConfigurationById(configId);
	if (config) {
		configCache.value[configId] = config;
	}
};

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
	() => props.node.state,
	() => {
		configInitials.value = props.node.state.initials;
		configParams.value = props.node.state.parameters;
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
		// Fill the form with the config data
		configName.value = props.node.state.name;
		configDescription.value = props.node.state.description;
		configInitials.value = props.node.state.initials;
		configParams.value = props.node.state.parameters;
	},
	{ immediate: true }
);

watch(
	() => selectedConfigId.value,
	() => {
		lazyLoadModelConfig(selectedConfigId.value);
	},
	{ immediate: true }
);

onMounted(async () => {
	const input = props.node.inputs[0];
	if (input.value) {
		const m = await getModel(input.value[0]);
		if (m) {
			model.value = m;
			if (outputs.value.length === 0) {
				const state = _.cloneDeep(props.node.state);
				state.initials = m.semantics?.ode.initials;
				state.parameters = m.semantics?.ode.parameters;
				emit('update-state', state);
			}
		}
	}
});
</script>

<style scoped>
.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}
</style>
