<template>
	<div class="flex align-items-center">
		<p class="p-text-secondary pr-2">Topic Filter</p>
		<Dropdown
			class="topic-dropdown"
			:modelValue="topic"
			:options="topicOptions"
			optionLabel="label"
			placeholder="Select a Topic"
			@update:modelValue="onTopicChange"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Dropdown from 'primevue/dropdown';
import useResourcesStore from '@/stores/resources';

const resources = useResourcesStore();
const props = defineProps({
	topicOptions: {
		type: Array<{ label: string; value: string }>,
		default: []
	}
});

const emit = defineEmits(['filter-changed']);

const topic = computed(() =>
	props.topicOptions.find((option) => option.value === resources.getXddDataset)
);
const onTopicChange = (e) => {
	resources.setXDDDataset(e.value);
	emit('filter-changed');
};
</script>

<style scoped>
.topic-dropdown {
	min-width: 210px;
	border-radius: 6px;
}
.topic-dropdown:deep(.p-inputtext) {
	padding: 0.5rem;
}
</style>
