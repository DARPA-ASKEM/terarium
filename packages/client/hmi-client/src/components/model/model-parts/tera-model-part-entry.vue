<template>
	<section class="flex flex-column">
		<span class="flex align-items-center gap-3">
			<h6>{{ id }}</h6>
			<span v-if="!isTimePart" class="name">
				<template v-if="featureConfig.isPreview">{{ nameText }}</template>
				<tera-input-text v-else placeholder="Add a name" v-model="nameText" />
			</span>
			<span class="unit" :class="{ time: isTimePart }">
				<template v-if="input || output">
					<span><label>Input:</label> {{ input }}</span>
					<span><label>Output:</label> {{ output }}</span>
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
			<span v-if="!featureConfig.isPreview && !isTimePart" class="flex ml-auto gap-3">
				<!-- Three states of description buttons: Hide / Show / Add description -->
				<Button
					text
					size="small"
					:label="showDescription ? 'Hide description' : descriptionText ? 'Show description' : 'Add description'"
					@click="showDescription = !showDescription"
				/>
				<tera-concept class="concept" v-model:grounding="grounding" :is-preview="featureConfig.isPreview" />
			</span>
		</span>
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
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import TeraConcept from '@/components/widgets/tera-concept.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import type { FeatureConfig } from '@/types/common';
import { CalendarDateType } from '@/types/common';
import { PartType } from '@/model-representation/service';
import { stringToLatexExpression } from '@/services/model';

const props = defineProps<{
	description?: string;
	name?: string;
	unitExpression?: string;
	templateId?: string;
	id?: string;
	grounding?: any;
	expression?: string;
	input?: any;
	output?: any;
	featureConfig: FeatureConfig;
	partType: PartType;
}>();

const emit = defineEmits(['update-item']);

const nameText = computed({
	get: () => props.name,
	set: (newName) => emit('update-item', { key: 'name', value: newName })
});
const unitExpression = computed({
	get: () => props.unitExpression,
	set: (newUnitExpression) => emit('update-item', { key: 'unitExpression', value: newUnitExpression })
});
const descriptionText = computed({
	get: () => props.description,
	set: (newDescription) => {
		emit('update-item', { key: 'description', value: newDescription });
		showDescription.value = !!newDescription;
	}
});
const showDescription = ref<boolean>(!!descriptionText.value);
const grounding = computed({
	get: () => props.grounding,
	set: (newGrounding) => emit('update-item', { key: 'grounding', value: newGrounding })
});

// If we are in preview mode and there is no content, show nothing
const showUnit = computed(
	() => !(props.featureConfig.isPreview && !unitExpression.value) && props.partType !== PartType.TRANSITION
);

const isTimePart = props.partType === PartType.TIME;
</script>

<style scoped>
section {
	font-size: var(--font-caption);
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

.unit,
.concept {
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
:deep(.p-autocomplete-input) {
	height: 2rem;
	font-size: var(--font-caption);
}
</style>
