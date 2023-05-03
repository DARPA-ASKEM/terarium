<template>
	<Dropdown
		class="w-full md:w-14rem"
		v-model="selectedModel"
		:options="models"
		option-label="name"
		placeholder="Select a model"
	/>
	<template v-if="model">
		<h6>Initial values</h6>
		<ul>
			<li v-for="(s, i) of model.content.S" :key="i">
				<span>{{ s.sname }}</span>
				<InputText class="p-inputtext-sm" v-model="initialValues[s.sname]" />
			</li>
		</ul>
		<h6>Parameter values</h6>
		<ul>
			<li v-for="(t, i) of model.content?.T" :key="i">
				<span>{{ t.tname }}</span>
				<InputText class="p-inputtext-sm" v-model="parameterValues[t.tname]" />
			</li>
		</ul>
		<Button @click="run">Run</Button>
	</template>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { Model } from '@/types/Model';
import { modelOperation } from '@/components/workflow/model-operation';
import { getModel } from '@/services/model';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	models: Model[];
	node: WorkflowNode;
}>();

const emit = defineEmits(['append-output-port']);

interface StringValueMap {
	[key: string]: string;
}

const selectedModel = ref<Model>();
const model = ref<Model | null>();

const initialValues = ref<StringValueMap>({});
const parameterValues = ref<StringValueMap>({});

function run() {
	if (modelOperation.action) {
		console.log(
			modelOperation.action({
				model: model.value,
				initialValues: initialValues.value,
				parameterValues: parameterValues.value
			})
		);
		emit('append-output-port', props.node.id, {
			id: props.node.outputs.length.toString(),
			type: modelOperation.outputs[0].type,
			value: {
				model: model.value,
				initialValues: initialValues.value,
				parameterValues: parameterValues.value
			}
		});
	}
}

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			model.value = await getModel(selectedModel.value.id.toString());

			model.value?.content.S.forEach((s) => {
				initialValues.value[s.sname] = `${1}`;
			});

			model.value?.content.T.forEach((s) => {
				parameterValues.value[s.tname] = `${0.0005}`;
			});
		}
	}
);
</script>

<style scoped>
h6 {
	margin-top: 0.5rem;
}

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
