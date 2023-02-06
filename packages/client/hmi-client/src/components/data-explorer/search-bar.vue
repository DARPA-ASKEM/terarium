<template>
	<section class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<AutoComplete
					:active="searchBarRef?.overlayVisible"
					placeholder="Search"
					v-model="query"
					:suggestions="autocompleteMenuItems"
					:minLength="MIN_LENGTH"
					@keyup.enter="execSearch"
					@keyup.space="handleSearchEvent"
					@change="handleSearchEvent"
					ref="searchBarRef"
					scrollHeight="400px"
					removeTokenIcon="pi pi-times"
					loadingIcon="null"
				>
					<template #item="prop">
						<span><i class="pi pi-search" />{{ prop.item }}</span>
					</template>
				</AutoComplete>
				<i class="pi pi-times clear-search" :class="{ hidden: !query }" @click="clearQuery" />
			</span>
			<!-- <i class="pi pi-history" title="Search history" /> -->
			<!-- <i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" /> -->
		</div>
	</section>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
// import { isEmpty } from 'lodash';
import { getRelatedTerms, getAutocomplete } from '@/services/data';
import { onMounted, ref } from 'vue';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
import AutoComplete from 'primevue/autocomplete';
import { EventType } from '@/types/EventType';

const props = defineProps<{
	suggestions: boolean;
}>();

const emit = defineEmits(['update-related-terms', 'toggle-search-by-example']);

const MIN_LENGTH = 3;
const route = useRoute();
const resources = useResourcesStore();

const query = ref<string>('');
const searchBarRef = ref();
const autocompleteMenuItems = ref<string[]>([]);

// const toggleSearchByExample = () => {
// 	emit('toggle-search-by-example');
// };

function clearQuery() {
	query.value = '';
	emit('update-related-terms');
}

const execSearch = () => {
	console.log(query.value, autocompleteMenuItems.value, resources, EventService);
	emit('update-related-terms', query.value);
	EventService.create(EventType.Search, resources.activeProject?.id, query.value);
};

function addToQuery(term: string) {
	query.value = query.value ? query.value.trim().concat(' ').concat(term).trim() : term;
	execSearch();
	// @ts-ignore
	searchBarRef.value?.$el.focus();
}
defineExpose({ addToQuery });

async function fillAutocomplete() {
	const promise = getAutocomplete(query.value);
	promise.then((response) => {
		autocompleteMenuItems.value = response;
		// @ts-ignore
		searchBarRef.value?.$el.focus();
	});
}

async function fillSuggestions() {
	if (props.suggestions) {
		const promise = getRelatedTerms(query.value, resources.xddDataset);
		promise.then((response) => {
			autocompleteMenuItems.value = response;
			// .map((item) => ({
			// 	label: item,
			// 	command: () => {
			// 		addToQuery(item);
			// 	}
			// }));
			// @ts-ignore
			searchBarRef.value?.$el.focus();
		});
	}
}
const handleSearchEvent = (event) => {
	const keyboardEvent = event as KeyboardEvent;
	if (keyboardEvent.code === 'Space') {
		fillSuggestions();
	} else if (query.value.length >= MIN_LENGTH) {
		fillAutocomplete();
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

.p-autocomplete {
	border-color: var(--surface-border);
	height: 3rem;
	width: 100%;
}

.p-autocomplete:deep(.p-inputtext) {
	border-radius: 1.5rem;
	width: 100%;
}

.p-autocomplete:deep(.p-inputtext:focus) {
	box-shadow: none;
}

.p-autocomplete:deep(.p-inputtext),
.p-autocomplete:deep(.p-inputtext:hover),
.p-autocomplete:deep(.p-inputtext:focus) {
	border: 1px solid var(--surface-border);
	padding-left: 3rem;
}

.p-autocomplete[active='true']:deep(.p-inputtext) {
	border-bottom-left-radius: 0;
	border-bottom-right-radius: 0;
	border-bottom: none;
	/* Required for smooth transition */
	border-bottom-color: transparent;
}

.p-input-icon-left {
	margin-right: 1rem;
	flex: 1;
}

i {
	color: var(--text-color-subdued);
	z-index: 1;
}

.pi-search {
	margin-right: 1rem;
}

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
</style>
