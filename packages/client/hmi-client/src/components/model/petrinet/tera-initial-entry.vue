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
			<Button :label="getOtherValuesLabel(initialId)" text size="small" @click="openModal()" />
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
		<TeraModelOtherValueEntry
			v-if="showOtherConfigValueModal"
			:initial-id="initialId"
			:tableData="tableData"
			@modal-mask-clicked="showOtherConfigValueModal = false"
			@update-expression="emit('update-expression', $event)"
			@update-source="emit('update-source', $event)"
			@close-modal="showOtherConfigValueModal = false"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { Model, ModelConfiguration } from '@/types/Types';
import { getInitialExpression, getInitialSource } from '@/services/model-configurations';
import TeraInput from '@/components/widgets/tera-input.vue';
import TeraModelOtherValueEntry from '@/components/model/petrinet/tera-model-other-value-entry.vue';
import { ref } from 'vue';
import Button from 'primevue/button';
import {
	getInitialDescription,
	getInitialName,
	getInitialUnits
} from '@/model-representation/service';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
	initialId: string;
}>();

const tableData = ref();

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

function getOtherValuesLabel(initialId) {
	tableData.value = [];
	const modelConfigTableData = props.modelConfigurations.map((modelConfig) => ({
		name: modelConfig.name,
		initialSemanticList: modelConfig.initialSemanticList
	}));
	modelConfigTableData.forEach((modelConfig) => {
		const config = modelConfig.initialSemanticList.filter((item) => item.target === initialId)[0];
		if (config && modelConfig.name) {
			const data = { name: modelConfig.name, ...config };
			tableData.value.push(data);
		}
	});
	return `Other Values(${tableData.value.length})`;
}

function openModal() {
	showOtherConfigValueModal.value = true;
}
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
