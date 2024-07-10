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
				<tera-input
					label="Expression"
					:model-value="getInitialExpression(modelConfiguration, initialId)"
					@update:model-value="emit('update-expression', { id: initialId, value: $event })"
				/>
			</span>
			<Button
				:label="getSourceLabel(initialId)"
				text
				size="small"
				@click="sourceOpen = !sourceOpen"
			/>
			<Button
				:label="getOtherValuesLabel"
				text
				size="small"
				@click="showOtherConfigValueModal = true"
			/>
		</main>
		<footer v-if="sourceOpen">
			<tera-input
				placeholder="Add a source"
				:model-value="getInitialSource(modelConfiguration, initialId)"
				@update:model-value="emit('update-source', { id: initialId, value: $event })"
			/>
		</footer>
	</div>
	<Teleport to="body">
		<tera-initial-other-value-modal
			v-if="showOtherConfigValueModal"
			:id="initialId"
			:updateEvent="'update-expression'"
			:tableData="tableData"
			:otherValuesInputTypes="OtherValuesInputTypes.constant"
			@modal-mask-clicked="showOtherConfigValueModal = false"
			@update-expression="emit('update-expression', $event)"
			@update-source="emit('update-source', $event)"
			@close-modal="showOtherConfigValueModal = false"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { Model, ModelConfiguration, OtherValuesInputTypes } from '@/types/Types';
import { getInitialExpression, getInitialSource } from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import TeraInitialOtherValueModal from '@/components/model/petrinet/tera-initial-other-value-modal.vue';
import { computed, onMounted, ref } from 'vue';
import Button from 'primevue/button';
import {
	getInitialDescription,
	getInitialName,
	getInitialUnits
} from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	initialId: string;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
}>();

const tableData = ref();
onMounted(() => setTableData(props.initialId));

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

const setTableData = (id, key = 'target', otherValueList = 'initialSemanticList') => {
	tableData.value = [];

	const modelConfigTableData = props.modelConfigurations.map((modelConfig) => ({
		name: modelConfig.name,
		list: modelConfig[otherValueList]
	}));

	modelConfigTableData.forEach((modelConfig) => {
		const config = modelConfig.list.filter((item) => item[key] === id)[0];
		if (config && modelConfig.name) {
			const data = { name: modelConfig.name, ...config };
			tableData.value.push(data);
		}
	});
};

const getOtherValuesLabel = computed(() => `Other Values(${tableData.value?.length})`);
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
