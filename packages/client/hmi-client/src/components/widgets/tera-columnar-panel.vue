<template>
	<main>
		<slot />
	</main>
</template>

<script setup lang="ts">
import { onUnmounted } from 'vue';

onUnmounted(() => {
	console.log('testasdasdasdasdasdasd');
});
</script>
<style scoped>
main {
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	overflow: hidden;
	gap: var(--gap-small);
}
</style>
