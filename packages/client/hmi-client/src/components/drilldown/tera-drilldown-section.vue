<template>
	<section>
		<header v-if="hasHeaderSlots" :class="{ shadow: hasScrolled }">
			<div>
				<slot name="header-controls-left" />
			</div>
			<div>
				<slot name="header-controls-right" />
			</div>
		</header>
		<main ref="main" @scroll="handleScroll">
			<slot v-if="!isLoading" />
			<tera-progress-spinner v-else :font-size="2" is-centered />
		</main>
		<footer v-if="slots.footer">
			<slot name="footer" />
		</footer>
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, useSlots } from 'vue';
import TeraProgressSpinner from '../widgets/tera-progress-spinner.vue';

defineProps<{
	isLoading?: boolean;
}>();

const slots = useSlots();
const hasHeaderSlots = computed(() => !!slots['header-controls-left'] || !!slots['header-controls-right']);

/* This is for adding a shadow to the header if user has scrolled */
const main = ref<HTMLElement | null>(null);
const hasScrolled = ref(false);
const handleScroll = () => {
	if (main.value) {
		hasScrolled.value = main.value.scrollTop > 20; // Change 20 to whatever threshold you deem appropriate
	}
};

onMounted(() => {
	if (main.value) {
		main.value.addEventListener('scroll', handleScroll);
	}
});

onUnmounted(() => {
	if (main.value) {
		main.value.removeEventListener('scroll', handleScroll);
	}
});
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
	padding: var(--gap-3) 0;
	gap: var(--gap-3);
}

header > div {
	display: inline-flex;
	gap: var(--gap-1);
	align-items: center;

	&:first-child {
		flex: 1;
	}
}
header.shadow {
	box-shadow:
		0px 10px 6px -11px var(--surface-500),
		0px 9px 9px -11px var(--surface-400);
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

.notebook-section {
	background-color: var(--surface-disabled);
	border-right: 1px solid var(--surface-border-dark);
	padding: var(--gap-4);
}
</style>
