<template>
	<template v-if="model">
		<h5>{{ model.name }}</h5>
		<h6>Initial values</h6>
		<ul>
			<li v-for="(s, i) of model.content.S" :key="i">
				<span>{{ s.sname }}</span>
				<InputNumber
					inputId="minmaxfraction"
					:minFractionDigits="0"
					:maxFractionDigits="10"
					class="p-inputtext-sm"
					v-model="initialValues[openedWorkflowNodeStore.selectedOutputIndex][s.sname]"
				/>
			</li>
		</ul>
		<h6>Parameter values</h6>
		<ul>
			<li v-for="(t, i) of model.content?.T" :key="i">
				<span>{{ t.tname }}</span>
				<InputNumber
					inputId="minmaxfraction"
					:minFractionDigits="0"
					:maxFractionDigits="10"
					class="p-inputtext-sm"
					v-model="parameterValues[openedWorkflowNodeStore.selectedOutputIndex][t.tname]"
				/>
			</li>
		</ul>
		<Button label="Add config" @click="createModelConfigOutput" />
	</template>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { Model } from '@/types/Model';
import { ModelOperation } from '@/components/workflow/model-operation';
import { getModel } from '@/services/model';
import { ModelConfig } from '@/types/ModelConfig';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { cloneDeep } from 'lodash';

const props = defineProps<{
	modelId: string;
}>();

const emit = defineEmits(['append-output-port']);

interface StringValueMap {
	[key: string]: number;
}

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const model = ref<Model | null>();

const initialValues = ref<StringValueMap[]>([{}]);
const parameterValues = ref<StringValueMap[]>([{}]);

function createModelConfigOutput() {
	if (ModelOperation.action) {
		emit('append-output-port', {
			type: ModelOperation.outputs[0].type,
			label: 'Config',
			value: {
				id: props.modelId,
				initialValues: initialValues.value[0],
				parameterValues: parameterValues.value[0]
			} as ModelConfig
		});
	}

	console.log(props.modelId, initialValues.value, parameterValues.value);
	addModelConfiguration();
}

function addModelConfiguration() {
	initialValues.value.push(cloneDeep(initialValues.value[initialValues.value.length - 1]));
	parameterValues.value.push(cloneDeep(parameterValues.value[parameterValues.value.length - 1]));
}

watch(
	() => [initialValues.value, parameterValues.value],
	() => {
		openedWorkflowNodeStore.setModelConfig(initialValues.value, parameterValues.value);
	},
	{ deep: true }
);

onMounted(async () => {
	model.value = await getModel(props.modelId);

	model.value?.content.S.forEach((s) => {
		initialValues.value[0][s.sname] = 1;
	});

	model.value?.content.T.forEach((s) => {
		parameterValues.value[0][s.tname] = 0.0005;
	});
});
</script>

<style scoped>
ul {
	list-style-type: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

li {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 100%;
}

.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
	margin-left: 1rem;
}
</style>
