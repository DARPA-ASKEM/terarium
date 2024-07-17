<template>
	<tera-model-part
		:model-part-items="timeList"
		:disabled-inputs="['concept']"
		@update-item="$emit('update-time', $event)"
	/>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ModelPartItem } from '@/types/Model';
import type { State } from '@/types/Types';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';

defineEmits(['update-time']);

const props = defineProps<{
	time: State[];
}>();

const timeList = computed<
	{
		base: ModelPartItem;
		children: ModelPartItem[];
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
