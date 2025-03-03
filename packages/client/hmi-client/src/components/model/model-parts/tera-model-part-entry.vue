<template>
	<section class="flex flex-column">
		<ul v-if="!isEmpty(modelErrors)">
			<li v-for="err of modelErrors" :key="err.id">
				<span :class="err.severity">{{ err.content }}</span>
			</li>
		</ul>
		<div class="top-entry">
			<h6>{{ id }}</h6>
			<span v-if="!isTimePart" class="name">
				<template v-if="featureConfig.isPreview">{{ nameText }}</template>
				<tera-input-text v-else placeholder="Add a name" v-model="nameText" />
			</span>
			<span class="unit" :class="{ time: isTimePart }">
				<template v-if="subject || outcome">
					<span><label>Subject:</label> {{ subject !== '' ? subject : 'n/a' }}</span>
					<span><label>&nbsp;Outcome:</label> {{ outcome !== '' ? outcome : 'n/a' }}</span>
					<span v-if="controllers"><label>&nbsp;Controllers:</label> {{ controllers }}</span>
				</template>
				<!--amr_to_mmt doesn't like unit expressions with spaces, removing them here before they are saved to the amr-->
				<template v-else-if="showUnit">
					<template v-if="featureConfig.isPreview"><label>Unit</label>{{ unitExpression }}</template>
					<!-- we use a dropdown for units with time semantic-->
					<Dropdown
						v-else-if="isTimePart"
						v-model="unitExpression"
						placeholder="Add a time unit"
						option-label="label"
						option-value="value"
						:options="[
							{ label: 'Days', value: CalendarDateType.DATE },
							{ label: 'Months', value: CalendarDateType.MONTH },
							{ label: 'Years', value: CalendarDateType.YEAR }
						]"
					/>
					<tera-input-text
						v-else
						label="Unit"
						placeholder="Add a unit"
						:characters-to-reject="[' ']"
						v-model="unitExpression"
					/>
				</template>
			</span>
			<template v-if="!featureConfig.isPreview && !isTimePart">
				<!-- Three states of description buttons: Hide / Show / Add description -->
				<Button
					class="button-description"
					text
					size="small"
					:label="showDescription ? 'Hide description' : descriptionText ? 'Show description' : 'Add description'"
					@click="showDescription = !showDescription"
				/>
				<aside class="concept" v-if="!isTransitionPart">
					<tera-concept v-model="grounding" :is-preview="featureConfig.isPreview" />
				</aside>
			</template>
		</div>
		<katex-element
			v-if="expression"
			class="expression"
			:expression="stringToLatexExpression(expression)"
			:throw-on-error="false"
		/>
		<span v-if="!isTimePart" class="description" :class="{ 'mt-1': showDescription }">
			<template v-if="featureConfig.isPreview">{{ descriptionText }}</template>
			<tera-input-text v-else-if="showDescription" v-model="descriptionText" placeholder="Add a description" />
		</span>
	</section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { debounce, isEmpty } from 'lodash';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import TeraConcept from '@/components/widgets/tera-concept.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import type { FeatureConfig } from '@/types/common';
import { CalendarDateType } from '@/types/common';
import { ModelError, PartType } from '@/model-representation/service';
import { stringToLatexExpression } from '@/services/model';

const props = defineProps<{
	description?: string;
	name?: string;
	unitExpression?: string;
	id?: string;
	grounding?: any;
	expression?: string;
	subject?: string;
	outcome?: string;
	controllers?: string;
	featureConfig: FeatureConfig;
	partType: PartType;
	modelErrors: ModelError[];
}>();

const emit = defineEmits(['update-item']);

const makeDebouncer = (key: string) =>
	debounce((value: string) => {
		emit('update-item', { key, value });
	}, 300);

const unitExpressionDebouncer = makeDebouncer('unitExpression');
const nameDebouncer = makeDebouncer('name');
const groundingDebouncer = makeDebouncer('grounding');
const descriptionDebouncer = makeDebouncer('description');

const nameText = computed({
	get: () => props.name,
	set: (newName) => nameDebouncer(newName as string)
});
const unitExpression = computed({
	get: () => props.unitExpression,
	set: (newUnitExpression) => unitExpressionDebouncer(newUnitExpression as string)
});
const descriptionText = computed({
	get: () => props.description,
	set: (newDescription) => {
		descriptionDebouncer(newDescription as string);
		showDescription.value = !!newDescription;
	}
});
const showDescription = ref<boolean>(!!descriptionText.value);
const grounding = computed({
	get: () => props.grounding,
	set: (newGrounding) => groundingDebouncer(newGrounding as string)
});

// If we are in preview mode and there is no content, show nothing
const showUnit = computed(
	() => !(props.featureConfig.isPreview && !unitExpression.value) && props.partType !== PartType.TRANSITION
);

const isTimePart = props.partType === PartType.TIME;
const isTransitionPart = props.partType === PartType.TRANSITION;
</script>

<style scoped>
section {
	font-size: var(--font-caption);
}

.top-entry {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: var(--gap-3);
}

.button-description {
	margin-left: auto;
}

.concept {
	/* container-type: inline-size; */
	container-name: concept;
	margin-left: var(--gap-1);
}

@container concept (max-width: 25%) {
	.concept {
		margin-left: 0;
		flex-basis: 100%;
		width: 100%;
	}
}

label,
.description {
	color: var(--text-color-subdued);
}

h6::after {
	content: '|';
	color: var(--text-color-light);
	margin-left: var(--gap-2);
}

.unit {
	display: flex;
	align-items: center;
	gap: var(--gap-1);
}

.unit:not(.time) {
	overflow: auto;
}

.expression {
	padding-top: var(--gap-1);
	align-content: center;
	min-height: 2rem;
	max-height: 12rem;
	overflow: auto;
}

:deep(.unit .tera-input > main > input) {
	height: 1.25rem;
	font-size: var(--font-caption);
}

.warn {
	background-color: var(--surface-warning);
}

.error {
	background-color: var(--surface-error);
}
</style>
