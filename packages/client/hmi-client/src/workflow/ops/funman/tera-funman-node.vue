<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<div class="flex gap-2">
				<Button
					@click="emit('open-drilldown')"
					label="Edit"
					severity="secondary"
					outlined
					class="w-full"
				/>
				<!--TODO: Run button isn't functional-->
				<Button
					label="Run"
					severity="secondary"
					icon="pi pi-play"
					outlined
					disabled
					class="w-full"
				/>
			</div>
		</template>
	</section>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { FunmanOperationState } from '@/workflow/ops/funman/funman-operation';
import Button from 'primevue/button';

const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();
</script>

<style scoped></style>
