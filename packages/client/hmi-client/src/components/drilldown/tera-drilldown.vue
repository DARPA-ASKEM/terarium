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
			<tera-columnar-panel>
				<template v-for="(tab, index) in tabs" :key="index">
					<component :is="tab" v-show="selectedViewIndex === index" />
				</template>

				<section v-if="slots.preview">
					<slot name="preview" />
				</section>
			</tera-columnar-panel>
			<footer v-if="slots.footer">
				<slot name="footer" />
			</footer>
		</section>
	</aside>
</template>

<script setup lang="ts">
import TeraDrilldownHeader from '@/components/drilldown/tera-drilldown-header.vue';
import { TabViewChangeEvent } from 'primevue/tabview';
import { computed, ref, useSlots } from 'vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';

const props = defineProps<{
	title: string;
	tooltip?: string;
}>();

const emit = defineEmits(['on-close-clicked']);
const slots = useSlots();

/**
 * This will retrieve and filter all top level components in the default slot if they have the tabName prop.
 */
const tabs = computed(() => {
	if (slots.default?.()) {
		if (slots.default().length === 1) {
			// if there is only 1 component we don't need to know the tab name and we can render it.
			return slots.default();
		}

		return slots.default().filter((vnode) => vnode.props?.tabName);
	}
	return [];
});

const views = computed(() => tabs.value.map((vnode) => vnode.props?.tabName));

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
	height: calc(100% - 1rem);
	margin: 1rem 1.5rem 0rem 1.5rem;
	background: var(--surface-0);
	border-radius: var(--modal-border-radius) var(--modal-border-radius) 0 0;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

main {
	flex-grow: 1;
	padding: 0 0 0 var(--gap-small);
	gap: var(--gap-small);
}

main > :deep(*) {
	display: grid;
	grid-auto-flow: column;
	height: 100%;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	gap: 0.5rem;
	overflow: hidden;
}

footer {
	padding: 0 1.5rem 1rem 1.5rem;
	display: flex;
	justify-content: flex-end;
	gap: 0.5rem;
}
</style>
