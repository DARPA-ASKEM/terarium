<template>
	<Dropdown
		class="w-full"
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
				<InputNumber
					inputId="minmaxfraction"
					:minFractionDigits="0"
					:maxFractionDigits="10"
					class="p-inputtext-sm"
					v-model="initialValues[s.sname]"
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
					v-model="parameterValues[t.tname]"
				/>
			</li>
		</ul>
		<Button @click="run">Run</Button>
	</template>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { Model } from '@/types/Model';
import { ModelOperation } from '@/components/workflow/model-operation';
import { getModel } from '@/services/model';
import { ModelConfig } from '@/types/ModelConfig';

defineProps<{
	models: Model[];
}>();

const emit = defineEmits(['append-output-port']);

interface StringValueMap {
	[key: string]: number;
}

const selectedModel = ref<Model>();
const model = ref<Model | null>();

const initialValues = ref<StringValueMap>({});
const parameterValues = ref<StringValueMap>({});

function run() {
	if (ModelOperation.action) {
		console.log(
			ModelOperation.action({
				model: model.value,
				initialValues: initialValues.value,
				parameterValues: parameterValues.value
			})
		);
		emit('append-output-port', {
			type: ModelOperation.outputs[0].type,
			value: {
				model: model.value,
				initialValues: initialValues.value,
				parameterValues: parameterValues.value
			} as ModelConfig
		});
	}
}

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			model.value = await getModel(selectedModel.value.id.toString());

			model.value?.content.S.forEach((s) => {
				initialValues.value[s.sname] = 1;
			});

			model.value?.content.T.forEach((s) => {
				parameterValues.value[s.tname] = 0.0005;
			});
		}
	}
);
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
