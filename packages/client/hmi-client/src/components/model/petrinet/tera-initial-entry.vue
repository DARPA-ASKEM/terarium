<template>
	<div>
		<header>
			<div>
				<strong>{{ initialId }}</strong>
				<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
				<template v-if="unit">
					<label class="ml-2">Unit</label>
					<span class="ml-1">{{ unit }}</span>
				</template>
			</div>
			<span v-if="description" class="ml-4">{{ description }}</span>
		</header>
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
	</div>
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
import { DistributionType } from '@/services/distribution';
import { Model, ModelConfiguration } from '@/types/Types';
import { getInitialExpression, getInitialSource, getOtherValues } from '@/services/model-configurations';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInitialOtherValueModal from '@/components/model/petrinet/tera-initial-other-value-modal.vue';
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import { getInitialDescription, getInitialName, getInitialUnits } from '@/model-representation/service';

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

const sourceOpen = ref(false);
const showOtherConfigValueModal = ref(false);

function getSourceLabel(initialId) {
	if (sourceOpen.value) return 'Hide source';
	if (!getInitialSource(props.modelConfiguration, initialId)) return 'Add source';
	return 'Show source';
}

const getOtherValuesLabel = computed(() => `Other Values(${otherValueList.value?.length})`);
</script>

<style scoped>
header {
	display: flex;
	flex-direction: column;
	padding-bottom: var(--gap-small);
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
