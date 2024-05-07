<template>
	<p>To Discuss if this should exist</p>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';

const props = defineProps<{
	thought?: any;
}>();

onMounted(async () => {
	console.log(props.thought);
});

watch(
	() => props.thought,
	() => {
		console.log('Just being used to display thought when its given atm:');
		console.log(props.thought);
	}
);
</script>

<style scoped></style>
