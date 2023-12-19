<template>
	<ul>
		<li
			v-for="input in inputs"
			:id="input.id"
			:key="input.id"
			:class="{ 'port-connected': input.status === WorkflowPortStatus.CONNECTED }"
			@mouseenter="emit('port-mouseover', $event)"
			@mouseleave="emit('port-mouseleave')"
			@click.stop="emit('port-selected', input, WorkflowDirection.FROM_INPUT)"
			@focus="() => {}"
			@focusout="() => {}"
		>
			<section>
				<div class="port-container">
					<div class="port" />
				</div>
				<span>
					<label>{{ getPortLabel(input) }}</label>
					<!--TODO: label is a string type not an array consider adding this back in if we support an array of labels-->
					<!-- <label v-for="(label, labelIdx) in input.label?.split(',') ?? []" :key="labelIdx">
					{{ label }}
				</label> -->
				</span>
				<Button
					class="unlink"
					label="Unlink"
					size="small"
					text
					@click.stop="emit('remove-edges', input.id)"
				/>
			</section>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { WorkflowPort, WorkflowPortStatus, WorkflowDirection } from '@/types/workflow';
import { getPortLabel } from '@/services/workflow';
import Button from 'primevue/button';

const emit = defineEmits([
	'port-mouseover',
	'port-selected',
	'port-mouseover',
	'port-mouseleave',
	'remove-edges'
]);

defineProps({
	inputs: {
		type: Array as PropType<WorkflowPort[]>,
		default: () => []
	}
});
</script>

<style scoped>
li {
	padding-right: 0.75rem;
	border-radius: 0 var(--border-radius) var(--border-radius) 0;
}

.port {
	border-radius: 0 var(--port-base-size) var(--port-base-size) 0;
	border: 2px solid var(--surface-border);
	border-left: none;
}

.port-connected .port {
	left: calc(-1 * var(--port-base-size));
}

label:not(:last-child)::after {
	content: ', ';
}
</style>
