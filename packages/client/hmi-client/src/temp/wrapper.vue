<template>
	<template v-for="(tab, idx) in tabs" :key="idx">
		<div style="display: flex; flex-direction: row">
			wrapper:
			<component :is="tab" />
		</div>
	</template>
</template>

<script setup lang="ts">
import { computed, useSlots, onUnmounted } from 'vue';
const slots = useSlots();

const tabs = computed(() => {
	if (slots.default?.()) {
		if (slots.default().length === 1) {
			return slots.default();
		}
		return slots.default().filter((vnode) => vnode.props?.name);
	}
	return [];
});

onUnmounted(() => {
	console.log('unmounting wrapper');
});
</script>
