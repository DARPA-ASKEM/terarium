<template>
	<section>
		<tera-operator-placeholder v-if="!outputPreview" :node="node">
			<template v-if="!node.inputs[0].value">Attach a model</template>
		</tera-operator-placeholder>
		<tera-model-diagram v-if="outputPreview" :model="outputPreview" :feature-config="{ isPreview: true }" />
		<Button v-if="node.inputs[0].value" @click="emit('open-drilldown')" label="Open" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { ref, watch } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';
import Button from 'primevue/button';
import { getModel, createModel } from '@/services/model';
import { getModelConfigurationById, getAsConfiguredModel } from '@/services/model-configurations';
import { Model } from '@/types/Types';
// import { useProjects } from '@/composables/project';
import { StratifyOperationStateMira, StratifyMiraOperation } from './stratify-mira-operation';

const emit = defineEmits(['open-drilldown', 'append-output']);
const outputPreview = ref<Model | null>();

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();

// /models/from-model-configuration/:config-id

watch(
	() => props.node.inputs,
	async () => {
		const input = props.node.inputs[0];
		// Create a default if we dont have an output yet:
		// if (input && !props.node.outputs[0].value) {
		if (input && input.value) {
			let baseModelId = '';

			if (input.type === 'modelId') {
				baseModelId = input.value[0];
			} else if (input.type === 'modelConfigId') {
				const modelConfigId = input.value?.[0];

				const modelConfiguration = await getModelConfigurationById(modelConfigId);
				const model = (await getAsConfiguredModel(modelConfigId)) as Model;

				// Mark the model as orginating from the config
				model.temporary = true;
				model.name += ` (${modelConfiguration.name})`;
				model.header.name = model.name as string;

				const res = await createModel(model, modelConfiguration.id);
				if (res) {
					baseModelId = res.id as string;
				}
				// await useProjects().addAsset(AssetType.Model, baseModelId, useProjects().activeProject.value?.id);
			}
			if (!baseModelId) return;

			const model = await getModel(baseModelId);
			const modelName = model?.name;

			const state = cloneDeep(props.node.state);
			state.baseModelId = baseModelId;

			emit(
				'append-output',
				{
					type: StratifyMiraOperation.outputs[0].type,
					label: modelName ?? 'Default Model',
					value: baseModelId,
					state: {
						strataGroup: cloneDeep(props.node.state.strataGroup),
						strataCodeHistory: cloneDeep(props.node.state.strataCodeHistory)
					}
				},
				state
			);
		}
	},
	{ deep: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		const port = props.node.outputs.find((d) => d.id === active);
		if (!port) return;
		outputPreview.value = await getModel(port.value?.[0]);
	},
	{ immediate: true }
);
</script>

<style scoped></style>
