<template>
	<aside class="overlay-container">
		<section>
			<tera-drilldown-header
				:active-index="selectedViewIndex"
				:views="views"
				:tooltip="tooltip"
				@tab-change="handleTabChange"
				@close="emit('on-close-clicked')"
				>{{ props.title }}</tera-drilldown-header
			>
			<main>
				<template v-for="(view, index) in views">
					<slot v-if="selectedViewIndex === index" :name="view" />
				</template>
			</main>
		</section>
	</aside>
</template>

<script setup lang="ts">
import TeraDrilldownHeader from '@/components/drilldown/tera-drilldown-header.vue';
import { TabViewChangeEvent } from 'primevue/tabview';
import { computed, ref, useSlots } from 'vue';

const props = defineProps<{
	title: string;
	tooltip?: string;
}>();

const emit = defineEmits(['on-close-clicked']);
const slots = useSlots();

const views = computed(() => Object.keys(slots));

const selectedViewIndex = ref<number>(0);

const handleTabChange = (event: TabViewChangeEvent) => {
	selectedViewIndex.value = event.index;
};
</script>

<style scoped>
.overlay-container {
	isolation: isolate;
	z-index: var(--z-index, var(--z-index-modal));
	position: fixed;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.32);
}

/* There is a performance issue with these large modals. 
When scrolling it takes time to render the content, paticularly heavy content such as the LLM integrations. This will show
us the main application behind the modal temporarily as content loads when scrolling which is a bit of an eye sore.
An extra div here is used to alleviate the impact of these issues a little by allowing us to see the overlay container rather
than the main application behind the modal when these render issues come, however this is still an issue regardless.
*/
.overlay-container > section {
	height: calc(100vh - 1rem);
	margin: 0.5rem;
	background: #fff;
	border-radius: var(--modal-border-radius);
	overflow: hidden;
}

main {
	margin: 0 0 0.5rem;
	max-width: inherit;
	/* contentHeight = fullscreen - modalMargin - headerHeight*/
	height: calc(100vh - 1rem - 108px);
	display: flex;
	flex-direction: column;
}
</style>
