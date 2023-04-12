<template>
	<div class="card flex justify-content-center">
		<Dropdown
			v-model="selectedModel"
			:options="models"
			optionLabel="name"
			placeholder="Select a Model"
			class="w-full md:w-14rem"
		/>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';

const selectedModel = ref();
const models = ref([
	{ name: 'SIR', code: 2 },
	{ name: 'SIR RODA', code: 5 }
]);
</script>
