<template>
	<tera-model-part
		:variable-list="timeList"
		:disabled-inputs="['concept']"
		@update-variable="$emit('update-time', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelVariable } from '@/types/Model';
import type { State } from '@/types/Types';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';

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
