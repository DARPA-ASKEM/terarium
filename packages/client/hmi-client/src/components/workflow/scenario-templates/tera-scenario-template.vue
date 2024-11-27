<template>
	<h4 class="pb-3">{{ scenarioClass.header.title }}</h4>

	<template v-if="scenarioClass.header.question && scenarioClass.header.description">
		<h5>{{ scenarioClass.header.question }}</h5>
		<p class="pt-2 pb-3">
			{{ scenarioClass.header.description }}
		</p>
	</template>

	<template v-if="!_.isEmpty(scenarioClass.header.examples)">
		<h6>Examples</h6>
		<ul class="examples">
			<li v-for="example in scenarioClass.header.examples" :key="example">{{ example }}</li>
		</ul>
	</template>

	<label class="pb-2">What would you like to call this workflow?</label>
	<tera-input-text
		ref="nameInput"
		:model-value="scenarioInstance.workflowName"
		@update:model-value="scenarioInstance.setWorkflowName($event)"
		auto-focus
	/>

	<section class="grid" v-if="slots.inputs || slots.outputs">
		<article v-if="slots.inputs" class="col-6 flex flex-column">
			<h6>Inputs</h6>
			<slot name="inputs" />
		</article>

		<article v-if="slots.outputs" class="col-6 flex flex-column">
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
ul {
	padding-bottom: var(--gap-3);

	&.examples {
		gap: 0;
		padding-left: var(--gap-5);
		> li::marker {
			font-size: xx-small;
		}
	}
}

:deep(article) {
	label {
		color: var(--text-color-subdued);
		font-size: var(--font-caption);
		padding-top: var(--gap-3);
		padding-bottom: var(--gap-1);
	}

	img {
		padding-top: var(--gap-3);
	}
}
</style>
