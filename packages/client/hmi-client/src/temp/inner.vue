<template>
	{{ props.id }}
</template>

<script setup lang="ts">
import { onUnmounted } from 'vue';

const props = defineProps<{
	id: string;
}>();

onUnmounted(() => {
	console.log('unmounting ', props.id);
});
</script>
