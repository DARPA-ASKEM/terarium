<template>
	<section class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<InputText
					type="text"
					placeholder="Search"
					v-model="query"
					@keyup.enter="execSearch"
					@keyup.space="handleSearchEvent"
					class="input-text"
					@input="handleSearchEvent"
				/>
				<Menu ref="autocompleteMenu" :model="autocompleteMenuItems" :popup="true"> </Menu>
				<i
					class="pi pi-times clear-search"
					:class="{ hidden: isClearQueryButtonHidden }"
					style="font-size: 1rem"
					@click="clearQuery"
				/>
			</span>
			<!-- <i class="pi pi-history" title="Search history" /> -->
			<!-- <i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" /> -->
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
import { ResourceType } from '@/types/common';
import { getRelatedWords, getAutocomplete } from '@/services/data';
import Menu from 'primevue/menu';
import { EventType } from '@/types/EventType';

const emit = defineEmits(['query-changed', 'toggle-search-by-example']);

const route = useRoute();
const resources = useResourcesStore();

const props = defineProps<{
	text: string;
	resultType: string;
}>();

const query = ref('');
const autocompleteMenu = ref();
const autocompleteMenuItems = ref([{}]);
const inputElement = ref<HTMLInputElement | null>(null);

const xddDataset = computed(() =>
	props.resultType === ResourceType.XDD ? resources.xddDataset : ''
);

const isClearQueryButtonHidden = computed(() => !query.value);

function clearQuery() {
	query.value = '';
	emit('query-changed');
}

const execSearch = () => {
	emit('query-changed', query.value);
	EventService.create(EventType.Search, resources.activeProject?.id, query.value);
};

// const toggleSearchByExample = () => {
// 	emit('toggle-search-by-example');
// };

function addToQuery(term: string) {
	query.value = query.value ? query.value.concat(' ').concat(term).trim() : term;
	execSearch();
	inputElement.value?.$el.focus();
}
defineExpose({ addToQuery });

function replaceSearchTerm(term) {
	query.value = term;
	// @ts-ignore
	inputElement.value?.$el.focus();
}

async function showAutocomplete(event) {
	if (query.value.length >= 3) {
		const promise = getAutocomplete(query.value);
		promise.then((response) => {
			autocompleteMenuItems.value = response.map((item) => ({
				label: item,
				icon: 'pi pi-search',
				command: () => {
					replaceSearchTerm(item);
				}
			}));
			// @ts-ignore
			inputElement.value?.$el.focus();
		});
		autocompleteMenu.value.show(event);
	}
}

async function showSuggestions(event) {
	if (xddDataset.value) {
		const promise = getRelatedWords(query.value, xddDataset.value);
		promise.then((response) => {
			autocompleteMenuItems.value = response.map((item) => ({
				label: item,
				icon: 'pi pi-search',
				command: () => {
					addToQuery(item);
				}
			}));
			// @ts-ignore
			inputElement.value?.$el.focus();
		});
		autocompleteMenu.value.show(event);
	}
}

const handleSearchEvent = (event) => {
	const keyboardEvent = event as KeyboardEvent;
	if (keyboardEvent.code === 'Space') {
		showSuggestions(event);
	} else {
		showAutocomplete(event);
	}
};

onMounted(() => {
	const { q } = route.query;
	query.value = q?.toString() ?? query.value;
});
</script>

<style scoped>
.search-bar-container {
	display: flex;
	align-items: center;
	flex-direction: column;
	max-width: 50%;
	overflow: hidden;
}

.search {
	display: flex;
	align-items: center;
	width: 100%;
}

.p-input-icon-left {
	margin-right: 1rem;
	flex: 1;
}

.input-text {
	border-color: var(--surface-border);
	border-radius: 1.5rem;
	padding: 12px;
	width: 100%;
	height: 3rem;
}

/* 
.pi-history {
	color: var(--text-color-secondary);
}

.pi-image {
	color: var(--text-color-secondary);
	margin-left: 1rem;
}

.pi-image:hover {
	color: var(--text-color-primary);
	cursor: pointer;
} 
*/

.clear-search:hover {
	background-color: var(--surface-hover);
	padding: 0.5rem;
	border-radius: 1rem;
	top: 1rem;
	right: 0.5rem;
}

.clear-search.hidden {
	visibility: hidden;
}

.item {
	background-color: var(--surface-secondary);
	color: var(--text-color-secondary);
	padding: 0 0.5rem 0 0.5rem;
	margin: 0.5rem;
}

.item:enabled:hover {
	color: var(--text-color-secondary);
	background-color: var(--surface-hover);
}

.p-menu.autocomplete {
	border-radius: 0 0 1.5rem 1.5rem;
}
</style>
