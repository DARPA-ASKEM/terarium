<template>
	<section>
		<header v-if="hasHeaderSlots">
			<div>
				<slot name="header-controls-left" />
			</div>
			<div>
				<slot name="header-controls-right" />
			</div>
		</header>
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
import { computed, useSlots } from 'vue';
import TeraProgressSpinner from '../widgets/tera-progress-spinner.vue';

defineProps<{
	isLoading?: boolean;
}>();

const slots = useSlots();
const hasHeaderSlots = computed(() => !!slots['header-controls-left'] || !!slots['header-controls-right']);
</script>

<style scoped>
footer {
	display: flex;
	justify-content: flex-end;
	gap: var(--gap-2);
}

header {
	display: inline-flex;
	justify-content: space-between;
	padding: var(--gap-4) 0;
	gap: var(--gap-3);
}

header > div {
	display: inline-flex;
	gap: var(--gap-1);
	align-items: start;
}

section {
	display: flex;
	flex-direction: column;
	gap: var(--gap-1);
	overflow: hidden;
	height: 100%;
}
main {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow-y: auto;
}
</style>
