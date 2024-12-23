<template>
	<h4 class="pb-2">{{ header.title }}</h4>

	<template v-if="header.question && header.description">
		<p class="header-question">{{ header.question }}</p>
		<p class="pt-2 pb-3">
			{{ header.description }}
		</p>
	</template>

	<template v-if="!_.isEmpty(header.examples)">
		<h6>Examples</h6>
		<ul class="examples">
			<li v-for="example in header.examples" :key="example">{{ example }}</li>
		</ul>
	</template>
	<div class="my-3">
		<p class="pb-2 label-text">What would you like to call this workflow?</p>
		<tera-input-text
			ref="nameInput"
			:model-value="scenarioInstance.workflowName"
			@update:model-value="scenarioInstance.setWorkflowName($event)"
			auto-focus
			@keydown.enter.stop.prevent="emit('save-workflow')"
			placeholder="Enter a name"
			style="height: 1.65rem"
		/>
	</div>
	<section class="flex" v-if="slots.inputs || slots.outputs">
		<article v-if="slots.inputs" class="col-5 flex flex-column">
			<h6>Inputs</h6>
			<slot name="inputs" />
		</article>
		<article class="col-1"></article>
		<article v-if="slots.outputs" class="col-5 flex flex-column">
			<h6>Outputs</h6>
			<slot name="outputs" />
		</article>
	</section>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import _ from 'lodash';
import { useSlots } from 'vue';
import { BaseScenario, ScenarioHeader } from './base-scenario';

const slots = useSlots();

defineProps<{
	header: ScenarioHeader;
	scenarioInstance: BaseScenario;
}>();

const emit = defineEmits(['save-workflow']);
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

.header-question {
	color: var(--primary-color);
	font-style: italic;
	font-size: 1.5rem;
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
.label-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}
</style>
