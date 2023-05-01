<template>
	<Dropdown
		class="w-full md:w-14rem"
		v-model="selectedModel"
		:options="models"
		option-label="name"
		placeholder="Select a model"
	/>
	<template v-if="model">
		<h4>Initial values</h4>
		<section>
			<div v-for="(s, i) of model.content.S" :key="i" class="row">
				<span>{{ s.sname }}</span>
				<InputText class="p-inputtext-sm" v-model="initialValues[s.sname]" />
			</div>
		</section>
		<h4>Parameter values</h4>
		<section>
			<div v-for="(t, i) of model.content?.T" :key="i" class="row">
				<span>{{ t.tname }}</span>
				<InputText class="p-inputtext-sm" v-model="parameterValues[t.tname]" />
			</div>
		</section>
	</template>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import { Model } from '@/types/Model';
// import { ModelOperation } from '@/types/workflow/model-operation';
import { getModel } from '@/services/model';

defineProps<{
	models?: Model[];
}>();

interface StringValueMap {
	[key: string]: string;
}

const selectedModel = ref<Model>();
const model = ref();

const initialValues = ref<StringValueMap>({});
const parameterValues = ref<StringValueMap>({});

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			model.value = await getModel(selectedModel.value.id.toString());
			console.log(model.value);
		}
	}
);
</script>
