<template>
	<Dropdown
		class="w-full md:w-14rem"
		v-model="selectedModel"
		:options="models"
		option-label="name"
		placeholder="Select a model"
	/>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
// import { Operation } from '@/types/workflow';
import { Model } from '@/types/Model';

defineProps<{
	models: Model[];
}>();

const selectedModel = ref();
</script>
