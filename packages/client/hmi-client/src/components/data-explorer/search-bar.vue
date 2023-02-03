<template>
	<section class="search-bar-container">
		<div class="search">
			<AutoComplete
				:active="searchBarRef?.containerClass[1]['p-overlay-open']"
				placeholder="Search"
				v-model="query"
				:suggestions="autocompleteMenuItems"
				@complete="execSearch"
				@change="handleSearchEvent"
				ref="searchBarRef"
				scrollHeight="400px"
				removeTokenIcon="pi pi-times"
				loadingIcon="undefined"
			>
				<template #item="prop">
					<i class="pi pi-search" />
					<span>{{ prop.item.label }}</span>
				</template>
			</AutoComplete>
			<i
				class="pi pi-times clear-search"
				:class="{ hidden: isClearQueryButtonHidden }"
				style="font-size: 1rem"
				@click="clearQuery"
			/>
			<!-- <span class="p-input-icon-left p-input-icon-right">
			
				<InputText type="text" placeholder="Search" v-model="query" @keyup.enter="execSearch"
					@keyup.space="handleSearchEvent" class="input-text" @input="handleSearchEvent" ref="searchBarRef" />
				<Menu ref="autocompleteMenu" :model="autocompleteMenuItems" :popup="true"> </Menu>
			
			</span> -->
			<!-- <i class="pi pi-history" title="Search history" /> -->
			<!-- <i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" /> -->
		</div>
	</section>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { getRelatedTerms, getAutocomplete } from '@/services/data';
import { computed, onMounted, ref } from 'vue';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
// import Menu from 'primevue/menu';
// import InputText from 'primevue/inputtext';
import { EventType } from '@/types/EventType';

const emit = defineEmits(['update-related-terms', 'toggle-search-by-example']);

const route = useRoute();
const resources = useResourcesStore();

const props = defineProps<{
	suggestions: boolean;
}>();

const query = ref('');
const searchBarRef = ref();
const autocompleteMenuItems = ref([{}]);

const isClearQueryButtonHidden = computed(() => !query.value);

function clearQuery() {
	query.value = '';
	emit('update-related-terms');
}

const execSearch = () => {
	emit('update-related-terms', query.value);
	EventService.create(EventType.Search, resources.activeProject?.id, query.value);
};

// const toggleSearchByExample = () => {
// 	emit('toggle-search-by-example');
// };

function addToQuery(term: string) {
	query.value = query.value ? query.value.trim().concat(' ').concat(term).trim() : term;
	execSearch();
	// @ts-ignore
	searchBarRef.value?.$el.focus();
}
defineExpose({ addToQuery });

function replaceSearchTerm(term) {
	query.value = term;
	// @ts-ignore
	searchBarRef.value?.$el.focus();
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
			searchBarRef.value?.$el.focus();
		});
		searchBarRef.value.show(event);
	}
}

async function showSuggestions(event) {
	if (props.suggestions) {
		const promise = getRelatedTerms(query.value, resources.xddDataset);
		promise.then((response) => {
			autocompleteMenuItems.value = response.map((item) => ({
				label: item,
				icon: 'pi pi-search',
				command: () => {
					addToQuery(item);
				}
			}));
			// @ts-ignore
			searchBarRef.value?.$el.focus();
		});
		searchBarRef.value.show(event);
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

.p-autocomplete {
	border-color: var(--surface-border);
	height: 3rem;
	width: 100%;
}

.p-autocomplete:deep(.p-inputtext),
.p-autocomplete:deep(.p-inputtext:hover) {
	border-color: var(--surface-border);
	border-radius: 1.5rem;
	width: 100%;
}

.p-autocomplete:deep(.p-inputtext:focus) {
	box-shadow: none;
	border: 1px solid var(--surface-border);
}

.p-autocomplete[active='true']:deep(.p-inputtext) {
	border-bottom: none;
	border-bottom-left-radius: 0;
	border-bottom-right-radius: 0;
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

i {
	color: var(--text-color-subdued);
}

.pi-search {
	margin-right: 1rem;
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
