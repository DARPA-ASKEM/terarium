<template>
	<section>
		<main>
			<slot />
		</main>
		<footer v-if="slots.footer">
			<slot name="footer" />
		</footer>
	</section>
</template>

<script setup lang="ts">
import { useSlots } from 'vue';

const slots = useSlots();
</script>

<style scoped>
footer {
	display: flex;
	justify-content: flex-end;
	gap: 0.5rem;
}

section {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	overflow: hidden;
}
main {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow-y: auto;
}
</style>
