<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<Accordion multiple :active-index="[0, 1, 2, 3]">
					<AccordionTab header="Context">
						<template #header>
							<tera-output-dropdown
								@click.stop
								style="margin-left: auto"
								:output="selectedOutputId"
								is-selectable
								:options="outputs"
								@update:output="onUpdateOutput"
								@update:selection="onUpdateSelection"
							/>
						</template>
						<h3>Name</h3>
						<InputText
							class="context-item"
							placeholder="Enter a name for this configuration"
							v-model="configName"
							@update:model-value="() => debouncedUpdateState({ name: configName })"
						/>
						<h3>Description</h3>
						<Textarea
							class="context-item"
							placeholder="Enter a description"
							v-model="configDescription"
							@update:model-value="() => debouncedUpdateState({ description: configDescription })"
						/>
					</AccordionTab>
					<AccordionTab header="Diagram">
						<tera-model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab header="Initials">
						<tera-model-config-table
							v-if="modelConfiguration"
							:model-configuration="modelConfiguration"
							:data="tableFormattedInitials"
							@update-value="updateConfigInitial"
							@update-configuration="
								(configToUpdate: ModelConfiguration) => {
									updateFromConfig(configToUpdate);
								}
							"
						/>
					</AccordionTab>
					<AccordionTab header="Parameters">
						<tera-model-config-table
							v-if="modelConfiguration"
							:model-configuration="modelConfiguration"
							:data="tableFormattedParams"
							@update-value="updateConfigParam"
							@update-configuration="
								(configToUpdate: ModelConfiguration) => {
									updateFromConfig(configToUpdate);
								}
							"
						/>
					</AccordionTab>
				</Accordion>
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<h4>TODO</h4>
		</section>
		<template #footer>
			<Button
				outlined
				:disabled="!configName"
				label="Run"
				icon="pi pi-play"
				@click="createConfiguration"
			/>
			<Button style="margin-left: auto" label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _, { cloneDeep, isEmpty } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
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
import { ModelConfigTableData, ParamType } from '@/types/common';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import {
	getUnstratifiedInitials,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { useToastService } from '@/services/toast';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';
import { logger } from '@/utils/logger';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
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

const selectedOutputId = ref<string>('');
const selectedConfigId = computed(
	() => props.node.outputs?.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const configCache = ref<Record<string, ModelConfiguration>>({});

const configName = ref<string>(props.node.state.name);
const configDescription = ref<string>(props.node.state.description);
const model = ref<Model>();

const modelConfiguration = computed<ModelConfiguration | null>(() => {
	if (!model.value) return null;

	const cloneModel = _.cloneDeep(model.value);
	if (cloneModel.semantics) {
		if (!cloneModel.metadata || !cloneModel.metadata.timeseries) {
			cloneModel.metadata = {
				...cloneModel.metadata,
				timeseries: {}
			};
		}
		cloneModel.semantics.ode.initials = configInitials.value;
		cloneModel.semantics.ode.parameters = configParams.value;
		cloneModel.metadata.timeseries = configTimeSeries.value;
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
const configTimeSeries = ref<{ [index: string]: any }>();

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

const tableFormattedInitials = computed<ModelConfigTableData[]>(() => {
	const formattedInitials: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		initials.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
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

const tableFormattedParams = computed<ModelConfigTableData[]>(() => {
	const formattedParams: ModelConfigTableData[] = [];

	if (stratifiedModelType.value) {
		parameters.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const param = configParams.value?.find((i) => i.id === v);
				const paramType = getParamType(param);
				const timeseriesValue = configTimeSeries.value?.[param!.id];
				return {
					id: v,
					name: v,
					type: paramType,
					value: param,
					source: '',
					visibility: false,
					timeseries: timeseriesValue
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
			const paramType = getParamType(param);

			const timeseriesValue = configTimeSeries.value?.[param!.id];
			formattedParams.push({
				id: init,
				name: init,
				type: paramType,
				value: param,
				source: '',
				visibility: false,
				timeseries: timeseriesValue
			});
		});
	}

	return formattedParams;
});

const getParamType = (param: ModelParameter | undefined) => {
	let type = ParamType.CONSTANT;
	if (!param) return type;
	if (
		modelConfiguration.value?.configuration.metadata?.timeseries?.[param.id] ||
		modelConfiguration.value?.configuration.metadata.timeseries?.[param.id] === ''
	) {
		type = ParamType.TIME_SERIES;
	} else if (param?.distribution) {
		type = ParamType.DISTRIBUTION;
	}
	return type;
};

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

const updateFromConfig = (config: ModelConfiguration) => {
	const state = cloneDeep(props.node.state);
	state.initials = config.configuration.semantics?.ode.initials ?? [];
	state.parameters = config.configuration.semantics?.ode.parameters ?? [];
	state.timeseries = config.configuration?.metadata?.timeseries ?? {};
	emit('update-state', state);
};

const createConfiguration = async () => {
	if (!model.value) return;

	const state = _.cloneDeep(props.node.state);

	const data = await createModelConfiguration(
		model.value.id,
		configName.value,
		configDescription.value,
		modelConfiguration.value?.configuration
	);

	if (!data) {
		logger.error('Failed to create model configuration');
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
		configTimeSeries.value = props.node.state.timeseries;
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
		configTimeSeries.value = props.node.state.timeseries;
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
			if (isEmpty(outputs.value)) {
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

.context-item {
	width: 100%;
}
</style>
