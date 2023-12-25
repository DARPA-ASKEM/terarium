<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="ConfigTabs.Wizard">
			<tera-drilldown-section>
				<div>
					<Steps :model="formSteps" :readonly="false" @update:active-step="activeIndex = $event" />
				</div>
				<div v-if="activeIndex === 0" class="form-section">
					<h3>Name</h3>
					<InputText
						placeholder="Enter a name for this configuration"
						v-model="configName"
						@update:model-value="() => debouncedUpdateState(configName)"
					/>
					<h3>Description</h3>
					<Textarea
						placeholder="Enter a description"
						v-model="configDescription"
						@update:model-value="() => debouncedUpdateState(configDescription)"
					/>
				</div>
				<div v-else-if="activeIndex === 1">
					<tera-model-config-editor v-if="model" :model="model" />
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="ConfigTabs.Notebook">
			<h4>TODO</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview title="Output"> </tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="saveConfiguration"
			/>
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { onMounted, ref } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Steps from 'primevue/steps';
import Textarea from 'primevue/textarea';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import { ModelConfigOperationState } from './model-config-operation';
import teraModelConfigEditor from './tera-model-config-editor.vue';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['update-state', 'close']);

enum ConfigTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const formSteps = ref([
	{
		label: 'Context'
	},
	{
		label: 'Set values'
	}
]);

const activeIndex = ref<number>(0);
const configName = ref<string>('');
const configDescription = ref<string>('');
const model = ref<Model>();

onMounted(async () => {
	const input = props.node.inputs[0];
	if (input.value) {
		const m = await getModel(input.value[0]);
		if (m) {
			model.value = m;
		}
	}
});

const updateState = (val) => {
	console.log(val);
};

const saveConfiguration = () => {
	console.log('save');
};

const debouncedUpdateState = _.debounce(updateState, 500);
</script>

<style scoped>
.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}
</style>
