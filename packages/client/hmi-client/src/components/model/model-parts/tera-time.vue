<template>
	<tera-model-part
		is-time-part
		:items="timeList"
		:feature-config="featureConfig"
		@update-item="$emit('update-time', $event)"
	/>
</template>

<script setup lang="ts">
import { ModelPartItem } from '@/types/Model';
import type { State } from '@/types/Types';
import TeraModelPart from '@/components/model/model-parts/tera-model-part.vue';
import type { FeatureConfig } from '@/types/common';

defineEmits(['update-time']);

const props = defineProps<{
	time: State[];
	featureConfig: FeatureConfig;
}>();

const timeList: {
	base: ModelPartItem;
	children: ModelPartItem[];
	isParent: boolean;
}[] = props.time.map(({ id, name, description, grounding, units }) => ({
	base: { id, name, description, grounding, unitExpression: units?.expression },
	children: [],
	isParent: false
}));
</script>
