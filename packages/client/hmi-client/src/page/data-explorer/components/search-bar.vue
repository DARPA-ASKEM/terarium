<template>
	<section class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<AutoComplete
					ref="searchBarRef"
					:active="searchBarRef?.overlayVisible"
					v-model="query"
					:suggestions="autocompleteMenuItems"
					placeholder="Search"
					:autoOptionFocus="false"
					:minLength="3"
					scrollHeight="400px"
					loadingIcon="null"
					@complete="fillAutocomplete"
					@keyup.enter="initiateSearch"
					@item-select="initiateSearch"
				>
					<template #option="prop">
						<span class="auto-complete-term">
							<i class="pi pi-search" />
							<span>{{ prop.option }}</span>
							<i class="pi pi-arrow-right"
						/></span>
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
import { onMounted, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AutoComplete from 'primevue/autocomplete';
import { RouteName } from '@/router/routes';
import { getRelatedTerms, getAutocomplete } from '@/services/data';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
import { EventType } from '@/types/EventType';

const props = defineProps<{
	showSuggestions: boolean;
}>();

const emit = defineEmits(['query-changed', 'toggle-search-by-example']);

const route = useRoute();
const router = useRouter();
const resources = useResourcesStore();

const query = ref<string>('');
const searchBarRef = ref();
const autocompleteMenuItems = ref<string[]>([]);

function clearQuery() {
	query.value = '';
	emit('query-changed');
}

const initiateSearch = () => {
	emit('query-changed', query.value);
	router.push({ name: RouteName.DataExplorerRoute, query: { q: query.value } });
	EventService.create(EventType.Search, resources.activeProject?.id, query.value);
};

function addToQuery(term: string) {
	query.value = query.value ? query.value.trim().concat(' ').concat(term).trim() : term;
	initiateSearch();
	// @ts-ignore
	searchBarRef.value?.$el.focus();
}
defineExpose({ addToQuery });

const fillAutocomplete = async () => {
	const appendToQuery = query.value[query.value.length - 1] === ' ' && props.showSuggestions;

	const promise: Promise<string[]> = appendToQuery
		? getRelatedTerms(query.value.trim(), resources.xddDataset) // Appends to what you're typing while searching for an xdd document
		: getAutocomplete(query.value); // Replaces what you're typing

	promise.then((response) => {
		autocompleteMenuItems.value = appendToQuery
			? response.map((term) => `${query.value}${term}`) // Append your input in front of suggested terms
			: response;
		// @ts-ignore
		searchBarRef.value?.$el.focus();
	});
};

onMounted(() => {
	const { q } = route.query;
	query.value = q?.toString() ?? query.value;
});

// Clear the query if not on the data explorer view
watch(
	() => route.name,
	(name) => name !== RouteName.DataExplorerRoute && clearQuery()
);
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

.auto-complete-term {
	display: inline-flex;
	width: 100%;
}

.pi-search {
	margin-right: 1rem;
}

.pi-arrow-right {
	margin-left: auto;
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
