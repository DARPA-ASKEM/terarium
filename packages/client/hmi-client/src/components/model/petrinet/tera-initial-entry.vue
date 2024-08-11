<template>
	<header>
		<div class="flex">
			<strong>{{ initialId }}</strong>
			<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
			<template v-if="unit">
				<label class="ml-2">Unit</label>
				<span class="ml-1">{{ unit }}</span>
			</template>
			<template v-if="concept">
				<label class="ml-auto">Concept</label>
				<span class="ml-1">{{ concept }}</span>
			</template>
		</div>
		<span v-if="description" class="description">{{ description }}</span>
	</header>
	<template v-if="isEmpty(modelConfiguration.inferredParameterList)">
		<main>
			<span class="expression">
				<tera-input-text
					label="Expression"
					:model-value="getInitialExpression(modelConfiguration, initialId)"
					@update:model-value="emit('update-expression', { id: initialId, value: $event })"
				/>
			</span>
			<Button :label="getSourceLabel(initialId)" text size="small" @click="sourceOpen = !sourceOpen" />
			<Button :label="getOtherValuesLabel" text size="small" @click="showOtherConfigValueModal = true" />
		</main>
		<footer v-if="sourceOpen">
			<tera-input-text
				placeholder="Add a source"
				:model-value="getInitialSource(modelConfiguration, initialId)"
				@update:model-value="emit('update-source', { id: initialId, value: $event })"
			/>
		</footer>
	</template>
	<katex-element
		v-else
		class="expression"
		:expression="stringToLatexExpression(getInitialExpression(modelConfiguration, initialId))"
		:throw-on-error="false"
	/>
	<tera-initial-other-value-modal
		v-if="showOtherConfigValueModal"
		:id="initialId"
		:updateEvent="'update-expression'"
		:otherValueList="otherValueList"
		:otherValuesInputTypes="DistributionType.Constant"
		@modal-mask-clicked="showOtherConfigValueModal = false"
		@update-expression="emit('update-expression', $event)"
		@update-source="emit('update-source', $event)"
		@close-modal="showOtherConfigValueModal = false"
	/>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref, onMounted } from 'vue';
import { DistributionType } from '@/services/distribution';
import { Model, ModelConfiguration } from '@/types/Types';
import { getInitialExpression, getInitialSource, getOtherValues } from '@/services/model-configurations';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInitialOtherValueModal from '@/components/model/petrinet/tera-initial-other-value-modal.vue';
import Button from 'primevue/button';
import { getInitialDescription, getInitialName, getInitialUnits, getStates } from '@/model-representation/service';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';
import { stringToLatexExpression } from '@/services/model';

const props = defineProps<{
	model: Model;
	initialId: string;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
}>();

const otherValueList = computed(() =>
	getOtherValues(props.modelConfigurations, props.initialId, 'target', 'initialSemanticList')
);

const emit = defineEmits(['update-expression', 'update-source']);

const name = getInitialName(props.model, props.initialId);
const unit = getInitialUnits(props.model, props.initialId);
const description = getInitialDescription(props.model, props.initialId);

const concept = ref('');
const sourceOpen = ref(false);
const showOtherConfigValueModal = ref(false);

const getOtherValuesLabel = computed(() => `Other Values(${otherValueList.value?.length})`);

function getSourceLabel(initialId) {
	if (sourceOpen.value) return 'Hide source';
	if (!getInitialSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}

onMounted(async () => {
	const identifiers = getStates(props.model).find((state) => state.id === props.initialId)?.grounding?.identifiers;
	if (identifiers) concept.value = await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifiers));
});
</script>

<style scoped>
header {
	display: flex;
	flex-direction: column;
	padding-bottom: var(--gap-2);
	gap: var(--gap-2);
}
.description {
	color: var(--text-color-subdued);
}
.expression {
	flex-grow: 1;
}
main {
	display: flex;
	justify-content: space-between;
	padding-bottom: var(--gap-small);
}

label {
	color: var(--text-color-subdued);
}
</style>
