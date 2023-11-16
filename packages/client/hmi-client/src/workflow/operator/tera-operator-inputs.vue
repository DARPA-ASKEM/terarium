<template>
	<ul v-if="!isEmpty(inputs)" class="inputs">
		<li
			v-for="(input, index) in inputs"
			:key="index"
			:class="{ 'port-connected': input.status === WorkflowPortStatus.CONNECTED }"
		>
			<div
				class="port-container"
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
					<span
						v-for="(label, labelIdx) in input.label?.split(',') ?? []"
						:key="labelIdx"
						class="input-label"
					>
						{{ label }}
					</span>
				</div>
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
ul {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	margin: 0.25rem 0;
	list-style: none;
	font-size: var(--font-caption);
}

ul li {
	display: flex;
	gap: 0.5rem;
	align-items: center;
}

.inputs,
.outputs {
	color: var(--text-color-secondary);
}
.input-port-container,
.output-port-container {
	display: flex;
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	gap: 0.5rem;
}

.input-port-container {
	padding-right: 0.5rem;
}

.output-port-container {
	margin-left: 0.5rem;
	flex-direction: row-reverse;
	padding-left: 0.75rem;
	border-radius: var(--border-radius) 0 0 var(--border-radius);
}

.output-port-container:hover {
	cursor: pointer;
	background-color: var(--surface-highlight);
}
</style>
