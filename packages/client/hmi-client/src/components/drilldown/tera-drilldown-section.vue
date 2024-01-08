<template>
	<section>
		<main>
			<slot v-if="!isLoading" />
			<tera-progress-spinner v-else :font-size="2" is-centered />
		</main>
		<footer v-if="slots.footer">
			<slot name="footer" />
		</footer>
	</section>
</template>

<script setup lang="ts">
import { useSlots } from 'vue';
import TeraProgressSpinner from '../widgets/tera-progress-spinner.vue';

const slots = useSlots();

defineProps<{
	isLoading?: boolean;
}>();
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
	gap: 1.5rem;
	padding: 1.5rem 1.5rem 1.5rem 1rem;
}
</style>
