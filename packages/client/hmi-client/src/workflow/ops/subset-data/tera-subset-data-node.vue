<template>
	<section>
		<tera-operator-placeholder v-if="!node.inputs[0].value" :operation-type="node.operationType">
			Attach a dataset
		</tera-operator-placeholder>
		<Button v-else label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { SubsetDataState } from './subset-data-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

defineProps<{
	node: WorkflowNode<SubsetDataState>;
}>();
</script>
