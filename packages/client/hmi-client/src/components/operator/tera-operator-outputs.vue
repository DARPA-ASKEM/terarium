<template>
	<ul>
		<li
			v-for="output in selectedOutputs"
			:id="output.id"
			:key="output.id"
			:class="{ 'port-connected': output.status === WorkflowPortStatus.CONNECTED }"
			@mouseenter="emit('port-mouseover', $event)"
			@mouseleave="emit('port-mouseleave')"
			@click.stop="emit('port-selected', output, WorkflowDirection.FROM_OUTPUT)"
			@focus="() => {}"
			@focusout="() => {}"
		>
			<section>
				<div class="port-container">
					<div class="port" />
				</div>
				<label>{{ output.label }}</label>
				<Button
					class="unlink"
					label="Unlink"
					size="small"
					text
					@click.stop="emit('remove-edges', output.id)"
				/>
			</section>
			<!--TODO: We will see how to integrate port actions into this button later-->
			<!-- <Button
				size="small"
				text
				label="output.actionLabel"
				@click.stop="console.log('Do output.action() here')"
			/> -->
		</li>
	</ul>
</template>

<script setup lang="ts">
import { PropType, computed } from 'vue';
import { WorkflowPortStatus, WorkflowDirection, WorkflowOutput } from '@/types/workflow';
import Button from 'primevue/button';

const emit = defineEmits([
	'port-mouseover',
	'port-selected',
	'port-mouseover',
	'port-mouseleave',
	'remove-edges'
]);

const props = defineProps({
	outputs: {
		type: Array as PropType<WorkflowOutput<any>[]>,
		default: () => []
	}
});

const selectedOutputs = computed(
	() =>
		props.outputs?.filter((output) => {
			if (!('isSelected' in output)) return true;
			return output.isSelected;
		})
);
</script>

<style scoped>
ul {
	align-items: end;
}

li {
	padding-left: 0.75rem;
	border-radius: var(--border-radius) 0 0 var(--border-radius);
}

li > section {
	flex-direction: row-reverse;
}

li > *:not(:first-child) {
	margin-right: calc(var(--port-base-size) * 2);
}

.port-container {
	text-align: right;
}

.port {
	border-radius: var(--port-base-size) 0 0 var(--port-base-size);
	border: 2px solid var(--surface-border);
	border-right: none;
}

.port-connected .port {
	left: var(--port-base-size);
}
</style>
