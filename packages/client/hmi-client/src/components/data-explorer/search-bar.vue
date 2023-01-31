<template>
	<div class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<InputText
					ref="inputElement"
					type="text"
					placeholder="Search"
					v-model="searchText"
					@keyup.enter="execSearch"
					@keyup.space="handleSearchEvent"
					class="input-text"
					@input="handleSearchEvent"
				/>
				<Menu ref="autocompleteMenu" :model="autocompleteMenuItems" :popup="true"> </Menu>
				<i
					class="pi pi-times clear-search"
					:class="{ hidden: isClearSearchButtonHidden }"
					style="font-size: 1rem"
					@click="clearText"
				></i>
			</span>
			<i class="pi pi-history" />
			<i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" />
		</div>
		<span class="suggested-terms" v-if="suggestedTerms && suggestedTerms[0]"
			>Suggested terms:<Chip
				v-for="item in suggestedTerms"
				:key="item"
				removable
				remove-icon="pi pi-times"
			>
				<span @click="addSearchTerm(item)">{{ item }}</span>
			</Chip>
		</span>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';
import Chip from 'primevue/chip';
import * as EventService from '@/services/event';
import { EventType } from '@/types/EventType';
import useResourcesStore from '@/stores/resources';
import { ResourceType } from '@/types/common';
import { getRelatedWords, getAutocomplete } from '@/services/data';
import Menu from 'primevue/menu';

const props = defineProps<{
	text?: string;
	suggestedTerms?: string[];
	resultType: string;
}>();

const autocompleteMenu = ref();
const autocompleteMenuItems = ref([{}]);

const emit = defineEmits(['search-text-changed', 'toggle-search-by-example']);

const route = useRoute();

const searchText = ref('');
const defaultText = computed(() => props.text);
const isClearSearchButtonHidden = computed(() => !searchText.value);
const inputElement = ref<HTMLInputElement | null>(null);
// const suggestedItems = computed(() => props.relatedSearchTerms?.slice(0, maxSuggestedTerms));

const resources = useResourcesStore();
const xddDataset = computed(() =>
	props.resultType === ResourceType.XDD ? resources.xddDataset : ''
);

const clearText = () => {
	searchText.value = '';
};

const execSearch = () => {
	emit('search-text-changed', searchText.value);
	EventService.create(EventType.Search, resources.activeProject?.id, searchText.value);
};

function addSearchTerm(term) {
	searchText.value = searchText.value ? searchText.value.concat(' ').concat(term) : term;
	// @ts-ignore
	inputElement.value?.$el.focus();
}

function addSearchTermNoSpace(term) {
	searchText.value = searchText.value ? searchText.value.concat(term) : term;
	// @ts-ignore
	inputElement.value?.$el.focus();
}

function replaceSearchTerm(term) {
	searchText.value = term;
	// @ts-ignore
	inputElement.value?.$el.focus();
}

const toggleSearchByExample = () => {
	emit('toggle-search-by-example');
};

async function showAutocomplete(event) {
	if (searchText.value.length >= 3) {
		const promise = getAutocomplete(searchText.value);
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
		const promise = getRelatedWords(searchText.value, xddDataset.value);
		promise.then((response) => {
			autocompleteMenuItems.value = response.map((item) => ({
				label: item,
				icon: 'pi pi-search',
				command: () => {
					addSearchTermNoSpace(item);
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
	searchText.value = q?.toString() ?? searchText.value;
});

watch(defaultText, (newText) => {
	searchText.value = newText || searchText.value;
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

.suggested-terms {
	margin: 0.75rem 0 0.25rem 0;
	display: inline-flex;
	gap: 0.5rem;
	margin-left: 0.25rem;
	margin-right: auto;
	align-items: center;
	overflow: hidden;
	white-space: nowrap;
}

.suggested-terms,
.p-chip {
	font-size: small;
	font-weight: bold;
	color: var(--text-color-subdued);
}

.p-chip {
	padding: 0 0.75rem;
	background-color: var(--surface-200);
}

.p-chip span {
	margin: 0.25rem 0;
	cursor: pointer;
}

.p-chip::v-deep .p-chip-remove-icon {
	font-size: 0.75rem;
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

.clear-search:hover {
	background-color: var(--surface-hover);
	padding: 0.5rem;
	border-radius: 1rem;
	top: 1rem;
	right: 0.5rem;
}

.clear-search-terms:enabled {
	color: var(--text-color-secondary);
	background-color: transparent;
}

.clear-search-terms:enabled:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-secondary);
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
