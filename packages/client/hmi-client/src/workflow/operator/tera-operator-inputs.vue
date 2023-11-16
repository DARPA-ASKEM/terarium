<template>
	<ul v-if="!isEmpty(inputs)">
		<li
			v-for="(input, index) in inputs"
			:key="index"
			:class="{ 'port-connected': input.status === WorkflowPortStatus.CONNECTED }"
			@mouseenter="emit('port-mouseover', $event)"
			@mouseleave="emit('port-mouseleave')"
			@click.stop="emit('port-selected', input, WorkflowDirection.FROM_INPUT)"
			@focus="() => {}"
			@focusout="() => {}"
		>
			<div class="port" />
			<div>
				<!-- if input is empty, show the type. TODO: Create a human readable 'type' to display here -->
				<span v-if="!input.label">{{ input.type }}</span>
				<label v-for="(label, labelIdx) in input.label?.split(',') ?? []" :key="labelIdx">
					{{ label }}
				</label>
			</div>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { WorkflowPort, WorkflowPortStatus, WorkflowDirection } from '@/types/workflow';
import { isEmpty } from 'lodash';

const emit = defineEmits(['port-mouseover', 'port-selected', 'port-mouseover', 'port-mouseleave']);

defineProps({
	inputs: {
		type: Array as PropType<WorkflowPort[]>,
		default: () => []
	}
});
</script>

<style scoped>
li {
	padding: 0.5rem 0;
	padding-right: 0.75rem;
	border-radius: 0 var(--border-radius) var(--border-radius) 0;
}

.port {
	border-radius: 0 8px 8px 0;
	border: 2px solid var(--surface-border);
	border-left: none;
}

.port-connected .port {
	left: calc(-1 * var(--port-base-size));
}

label::after {
	color: var(--text-color-primary);
	content: ', ';
}

label:last-child::after {
	content: '';
}
</style>
