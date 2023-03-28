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
					@dragover.prevent
					@dragenter.prevent
					@drop="
						{
							isSearchByExampleVisible = true;
							onDrop();
						}
					"
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
			<Button
				class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small search-by-example-button"
				icon="pi pi-upload"
				@click="isSearchByExampleVisible = !isSearchByExampleVisible"
				:active="isSearchByExampleVisible"
			/>
		</div>
		<section v-if="isSearchByExampleVisible" class="search-by-example">
			<header>
				<h4>Search by example</h4>
				<Button
					class="p-button-rounded"
					icon="pi pi-times"
					@click="isSearchByExampleVisible = !isSearchByExampleVisible"
				></Button>
			</header>
			<section class="search-drag-drop-area">
				<section
					@dragenter="isDraggedOver = true"
					@dragleave="isDraggedOver = false"
					@drop="onDrop()"
					@dragover.prevent
					@dragenter.prevent
					:style="dragOverStyle"
				>
					<asset-card
						v-if="searchByExampleSelectedAsset && searchByExampleSelectedResourceType"
						:asset="searchByExampleSelectedAsset"
						:resourceType="searchByExampleSelectedResourceType"
						class="asset-card-in-searchByExample-dropzone"
					>
					</asset-card>
					<Button
						label="Clear"
						size="small"
						text
						v-if="searchByExampleSelectedAsset && searchByExampleSelectedResourceType"
						class="clear-search-by-example"
						@click="searchByExampleSelectedAsset = null"
					>
					</Button>
					<span v-else class="drop-zone">
						<i class="pi pi-upload big-icon" />
						Drag and drop a paper, model, or dataset here</span
					>
				</section>
			</section>
			<footer v-if="searchByExampleSelectedAsset && searchByExampleSelectedResourceType">
				<div class="field-checkbox">
					<Checkbox
						name="similarContent"
						binary
						v-model="selectedSearchByExampleOptions.similarContent"
					/>
					<label for="similarContent">Similar content</label>
				</div>
				<div class="field-checkbox">
					<Checkbox
						name="forwardCitation"
						binary
						v-model="selectedSearchByExampleOptions.forwardCitation"
					/>
					<label for="forwardCitation">Forward citations</label>
				</div>
				<div class="field-checkbox">
					<Checkbox
						name="backwardCitation"
						binary
						v-model="selectedSearchByExampleOptions.backwardCitation"
					/>
					<label for="backwardCitation">Backward citation</label>
				</div>
				<div class="field-checkbox">
					<Checkbox
						name="relatedContent"
						binary
						v-model="selectedSearchByExampleOptions.relatedContent"
					/>
					<label for="relatedContent">Related resources</label>
				</div>
				<Button label="Search" @click="initiateSearchByExample()" />
			</footer>
		</section>
	</section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AutoComplete from 'primevue/autocomplete';
import { RouteName } from '@/router/routes';
import { getRelatedTerms, getAutocomplete } from '@/services/data';
import * as EventService from '@/services/event';
import useResourcesStore from '@/stores/resources';
import { EventType } from '@/types/Types';
import Button from 'primevue/button';
import { useDragEvent } from '@/services/drag-drop';
import AssetCard from '@/page/data-explorer/components/asset-card.vue';
import Checkbox from 'primevue/checkbox';
import { SearchByExampleOptions } from '@/types/common';
import { useSearchByExampleOptions } from '../search-by-example';

const props = defineProps<{
	showSuggestions: boolean;
}>();

const emit = defineEmits(['query-changed']);

const route = useRoute();
const router = useRouter();
const resources = useResourcesStore();

const query = ref<string>('');
const searchBarRef = ref();
const autocompleteMenuItems = ref<string[]>([]);

const isSearchByExampleVisible = ref(false);
const isDraggedOver = ref(false);
const searchByExampleSelectedAsset = ref();
const searchByExampleSelectedResourceType = ref();
const selectedSearchByExampleOptions = ref<SearchByExampleOptions>({
	similarContent: true,
	forwardCitation: false,
	backwardCitation: false,
	relatedContent: false
});
const { searchByExampleOptions, searchByExampleItem } = useSearchByExampleOptions();

function clearQuery() {
	query.value = '';
	emit('query-changed');
}

const initiateSearch = () => {
	emit('query-changed', query.value);
	router.push({ name: RouteName.DataExplorerRoute, query: { q: query.value } });
	EventService.create(EventType.Search, resources.activeProject?.id, query.value);
};

function initiateSearchByExample() {
	searchByExampleOptions.value = { ...selectedSearchByExampleOptions.value };
	isSearchByExampleVisible.value = false;
}

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

const dragOverStyle = computed(() => {
	if (isDraggedOver.value) {
		return {
			border: `2px solid var(--primary-color)`,
			'background-color': 'var(--surface-highlight)'
		};
	}
	if (searchByExampleSelectedAsset.value && searchByExampleSelectedResourceType.value) {
		return {
			border: `2px solid var(--surface-border)`,
			'background-color': 'var(--surface-highlight)'
		};
	}

	return {};
});

const { getDragData } = useDragEvent();

function onDrop() {
	searchByExampleSelectedAsset.value = getDragData('asset');
	searchByExampleSelectedResourceType.value = getDragData('resourceType');
	searchByExampleItem.value = searchByExampleSelectedAsset.value;
	isDraggedOver.value = false;
}

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

*:deep(.p-autocomplete-panel) {
	width: 686px;
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

.pi.pi-times.clear-search {
	padding: 0.5rem;
	border-radius: var(--border-radius-bigger);
	top: 1rem;
	right: 4rem;
}

.clear-search:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-primary);
	padding: 0.5rem;
	border-radius: var(--border-radius-bigger);
	top: 1rem;
	right: 4rem;
}

.clear-search.hidden {
	visibility: hidden;
	right: 4rem;
}

.search-by-example {
	display: flex;
	flex-direction: column;
	position: absolute;
	top: 0.5rem;
	max-width: 58rem;
	width: 47%;
	min-height: 16rem;
	background-color: var(--surface-section);
	border-radius: 2rem;
	border: 1px solid var(--surface-border);
	z-index: 2;
	row-gap: 1rem;
	padding: 0 1.5rem 2rem 1.5rem;
	margin-right: 3rem;
	box-shadow: 0px 4px 6px -1px rgb(0 0 0 / 8%), 0px 2px 4px -1px rgb(0 0 0 / 6%);
}

.search-by-example header {
	display: flex;
	justify-content: space-between;
}

.search-by-example header * {
	margin-top: 1rem;
}

.search-by-example header .p-button {
	color: var(--text-color-secondary);
	background-color: transparent;
}

.search-by-example header .p-button:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-primary);
}

.search-drag-drop-area {
	height: 100%;
}

.search-drag-drop-area section {
	border: 1px dashed var(--surface-border);
	border-radius: 1rem;
	min-height: 9rem;
	display: flex;
	align-items: top;
	justify-content: center;
	padding: 1rem;
	background-color: var(--gray-50);
	gap: 1rem;
}

.big-icon {
	font-size: 2.5rem;
	color: var(--primary-color-light);
}

.drop-zone {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	pointer-events: none;
}
.asset-card-in-searchByExample-dropzone {
	width: 100%;
}
.clear-search-by-example {
	height: fit-content;
}

.search-by-example footer {
	display: flex;
	justify-content: space-between;
}

.field-checkbox {
	font-size: var(--font-small);
	color: var(--text-color-primary);
	margin-bottom: 0rem;
}

.p-button.search-by-example-button {
	right: 4rem;
}

.p-button[active='false'].search-by-example-button,
.p-button[active='false'].search-by-example-button:focus,
.p-button[active='false'].search-by-example-button:enabled {
	background-color: var(--surface-section);
	border-radius: var(--border-radius-bigger);
	color: var(--text-color-subdued);
}

.p-button[active='false'].search-by-example-button:hover {
	background-color: var(--surface-100);
	color: var(--text-color-subdued);
}

.p-button[active='true'].search-by-example-button,
.p-button[active='true'].search-by-example-button:focus,
.p-button[active='true'].search-by-example-button:enabled {
	background-color: var(--surface-highlight);
	color: var(--text-color-subdued);
}

.p-button[active='true'].search-by-example-button:hover {
	background-color: var(--surface-highlight);
	color: var(--text-color-subdued);
}

h4 {
	font-weight: var(--font-weight-semibold);
}
</style>
