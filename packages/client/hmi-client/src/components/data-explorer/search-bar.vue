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
					@complete="fillAutoComplete"
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
			<!-- <i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" /> 
					 @keyup.space="fillAutoComplete" @change="fillAutoComplete"-->
		</div>
	</section>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { getRelatedTerms, getAutocomplete } from '@/services/data';
import { RouteName } from '@/router/routes';
import { onMounted, ref } from 'vue';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
import AutoComplete from 'primevue/autocomplete';
import { EventType } from '@/types/EventType';

const props = defineProps<{
	suggestions: boolean;
}>();

const emit = defineEmits(['update-related-terms', 'toggle-search-by-example']);

const route = useRoute();
const router = useRouter();
const resources = useResourcesStore();

const query = ref<string>('');
const searchBarRef = ref();
const autocompleteMenuItems = ref<string[]>([]);

function clearQuery() {
	query.value = '';
	emit('update-related-terms');
}

const initiateSearch = () => {
	emit('update-related-terms', query.value);
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

const fillAutoComplete = async (event) => {
	const keyboardEvent = event as KeyboardEvent;
	let promise: Promise<string[]>;
	if (keyboardEvent.code === 'Space' && props.suggestions) {
		promise = getRelatedTerms(query.value, resources.xddDataset);
	} else {
		promise = getAutocomplete(query.value);
	}
	promise.then((response) => {
		autocompleteMenuItems.value = response;
		// @ts-ignore
		searchBarRef.value?.$el.focus();
	});
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
