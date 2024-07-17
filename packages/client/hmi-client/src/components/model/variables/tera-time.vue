<template>
	<tera-variables
		:variable-list="timeList"
		:disabled-inputs="['concept']"
		@update-variable="$emit('update-time', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import type { State } from '@/types/Types';
import TeraVariables from '@/components/model/variables/tera-variables.vue';

defineEmits(['update-time']);

const props = defineProps<{
	time: State[];
}>();

const timeList = computed<
	{
		base: ModelVariable;
		children: ModelVariable[];
		isParent: boolean;
	}[]
>(() =>
	props.time.map(({ id, name, description, grounding, units }) => ({
		base: { id, name, description, grounding, unitExpression: units?.expression },
		children: [],
		isParent: false
	}))
);
</script>
