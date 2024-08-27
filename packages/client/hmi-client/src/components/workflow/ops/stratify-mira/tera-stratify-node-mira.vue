<template>
	<section>
		<tera-progress-spinner v-if="isStratifyInProgress" :font-size="2" is-centered>
			Processing...
		</tera-progress-spinner>
		<tera-model-diagram v-else-if="outputPreview" :model="outputPreview" :feature-config="{ isPreview: true }" />
		<tera-operator-placeholder v-else :node="node">
			<template v-if="!node.inputs[0].value">Attach a model</template>
		</tera-operator-placeholder>
		<Button v-if="node.inputs[0].value" @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import Button from 'primevue/button';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { StratifyOperationStateMira } from './stratify-mira-operation';

const emit = defineEmits(['open-drilldown']);
const outputPreview = ref<Model | null>();

const props = defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();

const isStratifyInProgress = computed(() => props.node.state.isStratifyInProgress);

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
