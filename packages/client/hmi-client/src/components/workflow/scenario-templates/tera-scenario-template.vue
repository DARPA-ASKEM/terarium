<template>
	<h5>{{ scenarioClass.header.title }}</h5>
	<p class="pt-2">
		{{ scenarioClass.header.description }}
	</p>

	<template v-if="!_.isEmpty(scenarioClass.examples)">
		<h6 class="pt-3">Examples</h6>
		<ul class="examples">
			<li v-for="example in scenarioClass.examples" :key="example">{{ example }}</li>
		</ul>
	</template>

	<label class="pt-3 pb-2">What would you like to call this workflow?</label>
	<tera-input-text
		ref="nameInput"
		:model-value="scenarioInstance.workflowName"
		@update:model-value="scenarioInstance.setWorkflowName($event)"
		auto-focus
	/>

	<section class="grid" v-if="slots.inputs || slots.outputs">
		<article v-if="slots.inputs" class="col-6 flex flex-column gap-2">
			<h6>Inputs</h6>
			<slot name="inputs" />
		</article>

		<article v-if="slots.outputs" class="col-6 flex flex-column gap-2">
			<h6>Outputs</h6>
			<slot name="outputs" />
		</article>
	</section>
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

<style scoped>
ul.examples {
	gap: 0;
	padding-left: var(--gap-5);
	> li::marker {
		font-size: xx-small;
	}
}

article :deep(label) {
	color: var(--text-color-subdued);
}
</style>
