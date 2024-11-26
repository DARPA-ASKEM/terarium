<template>
	<div v-if="scenarioClass.header.title && scenarioClass.header.description">
		<h5>{{ scenarioClass.header.title }}</h5>
		<p>
			{{ scenarioClass.header.description }}
		</p>
	</div>
	<div v-if="!_.isEmpty(scenarioClass.examples)">
		<h6>Examples</h6>
		<ul class="pl-5">
			<li v-for="example in scenarioClass.examples" :key="example">{{ example }}</li>
		</ul>
	</div>

	<div>
		<label>What would you like to call this workflow?</label>
		<tera-input-text
			:model-value="scenarioInstance.workflowName"
			@update:model-value="scenarioInstance.setWorkflowName($event)"
			auto-focus
		/>
	</div>

	<div class="grid" v-if="slots.inputs || slots.outputs">
		<div v-if="slots.inputs" class="col-6 flex flex-column gap-2">
			<h6>Inputs</h6>
			<slot name="inputs" />
		</div>

		<div v-if="slots.outputs" class="col-6 flex flex-column gap-2">
			<h6>Outputs</h6>
			<slot name="outputs" />
		</div>
	</div>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import _ from 'lodash';
import { useSlots } from 'vue';
import { BaseScenario } from './base-scenario';

const slots = useSlots();

defineProps<{
	scenarioClass: typeof BaseScenario;
	scenarioInstance: BaseScenario;
}>();
</script>
