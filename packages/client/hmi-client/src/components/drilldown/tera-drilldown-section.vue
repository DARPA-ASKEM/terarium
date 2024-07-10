<template>
	<section>
		<header>
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

header {
	display: inline-flex;
	justify-content: space-between;
	margin-top: 1em;
	gap: 0.75rem;
}

header > div {
	display: inline-flex;
	gap: var(--gap-small);
	align-items: center;
}

section {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
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
