<template>
	<tera-variables
		:variable-list="timeList"
		:disabled-inputs="['concept', 'description', 'name']"
		@update-variable="$emit('update-time', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import type { ModelUnit } from '@/types/Types';
import TeraVariables from '@/components/model/variables/tera-variables.vue';

defineEmits(['update-time']);

const props = defineProps<{
	time: { id: string; units: ModelUnit }[];
}>();

const timeList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	props.time.map(({ id, units }) => ({
		base: { id, unitExpression: units?.expression },
		children: [],
		isParent: false
	}))
);
</script>
