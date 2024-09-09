<template>
	<section class="search-bar-container">
		<div class="p-input-icon-left p-input-icon-right">
			<vue-feather type="message-square" size="1.25rem" stroke="var(--text-color-subdued)" />
			<Textarea
				v-model="query"
				:placeholder="placeholder ?? 'Search'"
				autoResize
				rows="1"
				@keydown.enter.prevent="initiateSearch"
			/>
			<i> <Button :class="{ hidden: !query }" rounded text icon="pi pi-times" @click="clearQuery" /></i>
		</div>
		<section v-if="isSearchByExampleVisible" class="search-by-example">
			<header>
				<h4>Search by example</h4>
				<Button class="p-button-rounded" icon="pi pi-times" @click="searchByExampleToggle = !searchByExampleToggle" />
			</header>
			<section
				class="search-drag-drop-area"
				@dragenter="isDraggedOver = true"
				@dragleave="isDraggedOver = false"
				@drop="onDrop()"
				@dragover.prevent
				@dragenter.prevent
			>
				<section :style="dragOverStyle">
					<template v-if="isAssetDropped">
						<tera-asset-card
							:asset="searchByExampleSelectedAsset"
							:resourceType="searchByExampleSelectedResourceType"
							:source="source"
							class="asset-card-in-searchByExample-dropzone"
						>
						</tera-asset-card>
						<Button
							label="Clear"
							size="small"
							text
							class="clear-search-by-example"
							@click="searchByExampleSelectedAsset = null"
						/>
					</template>
					<span v-else class="drop-zone">
						<i class="pi pi-upload big-icon" />
						Drag and drop a paper, model, or dataset here</span
					>
				</section>
			</section>
			<footer v-if="isAssetDropped">
				<div class="field-checkbox">
					<Checkbox name="similarContent" binary v-model="selectedSearchByExampleOptions.similarContent" />
					<label for="similarContent">Similar content</label>
				</div>
				<div class="field-checkbox">
					<Checkbox name="forwardCitation" binary v-model="selectedSearchByExampleOptions.forwardCitation" />
					<label for="forwardCitation">Forward citations</label>
				</div>
				<div class="field-checkbox">
					<Checkbox name="backwardCitation" binary v-model="selectedSearchByExampleOptions.backwardCitation" />
					<label for="backwardCitation">Backward citation</label>
				</div>
				<div class="field-checkbox">
					<Checkbox name="relatedContent" binary v-model="selectedSearchByExampleOptions.relatedContent" />
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
import Textarea from 'primevue/textarea';
import { RouteName } from '@/router/routes';
// import { getRelatedTerms, getAutocomplete } from '@/services/data';
import * as EventService from '@/services/event';
// import useResourcesStore from '@/stores/resources';
import { EventType } from '@/types/Types';
import Button from 'primevue/button';
import { useDragEvent } from '@/services/drag-drop';
import TeraAssetCard from '@/page/data-explorer/components/tera-asset-card.vue';
import Checkbox from 'primevue/checkbox';
import { SearchByExampleOptions } from '@/types/common';
import { useSearchByExampleOptions, extractResourceName } from '@/page/data-explorer/search-by-example';
import { getResourceID } from '@/utils/data-util';
import { useProjects } from '@/composables/project';

defineProps<{
	showSuggestions: boolean;
	source: string;
	placeholder?: string;
}>();

const route = useRoute();
const router = useRouter();
// const resources = useResourcesStore();

const query = ref<string>('');
const searchBarRef = ref();
// const autocompleteMenuItems = ref<string[]>([]);

const searchByExampleToggle = ref(false);
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
}

const initiateSearch = () => {
	router.push({ name: RouteName.DataExplorer, query: { q: query.value } });
	EventService.create(EventType.Search, useProjects().activeProject.value?.id, query.value);
};

function initiateSearchByExample() {
	searchByExampleItem.value = searchByExampleSelectedAsset.value;
	searchByExampleOptions.value = { ...selectedSearchByExampleOptions.value };
	searchByExampleToggle.value = false;

	// used to update the search bar text with the name of the search by example asset
	query.value = extractResourceName(searchByExampleItem.value);
	router.push({
		name: RouteName.DataExplorer,
		query: { resourceId: getResourceID(searchByExampleItem.value!) }
	});
}

function addToQuery(term: string) {
	query.value = query.value ? query.value.trim().concat(' ').concat(term).trim() : term;
	initiateSearch();
	// @ts-ignore
	searchBarRef.value?.$el.focus();
}
defineExpose({ addToQuery });

// FIXME: We may add some sort of auto complete back in later
// const fillAutocomplete = async () => {
// 	const appendToQuery = query.value[query.value.length - 1] === ' ' && props.showSuggestions;

// 	const promise: Promise<string[]> = appendToQuery
// 		? getRelatedTerms(query.value.trim(), resources.xddDataset) // Appends to what you're typing while searching for an xdd document
// 		: getAutocomplete(query.value); // Replaces what you're typing

// 	promise.then((response) => {
// 		autocompleteMenuItems.value = appendToQuery
// 			? response.map((term) => `${query.value}${term}`) // Append your input in front of suggested terms
// 			: response;
// 		// @ts-ignore
// 		searchBarRef.value?.$el.focus();
// 	});
// };

const isSearchByExampleVisible = computed(() => getDragData('asset') || searchByExampleToggle.value);
const isAssetDropped = computed(() => searchByExampleSelectedAsset.value && searchByExampleSelectedResourceType.value);

const dragOverStyle = computed(() => {
	let borderStyle: string = '';
	if (isDraggedOver.value) borderStyle = '2px solid var(--primary-color)';
	else if (isAssetDropped.value) borderStyle = '2px solid var(--surface-border)';
	return {
		border: borderStyle,
		backgroundColor: isDraggedOver.value || isAssetDropped.value ? 'var(--surface-highlight)' : ''
	};
});

const { getDragData } = useDragEvent();

function onDrop() {
	searchByExampleToggle.value = true; // Maintains open search by example popup once an asset is successfully dropped
	searchByExampleSelectedAsset.value = getDragData('asset');
	searchByExampleSelectedResourceType.value = getDragData('resourceType');
	isDraggedOver.value = false;
}

onMounted(() => {
	const { q } = route.query;
	query.value = q?.toString() ?? query.value;
});

// Clear search by example input once popup closes
watch(
	() => isSearchByExampleVisible.value,
	() => {
		if (!isSearchByExampleVisible.value) {
			searchByExampleSelectedAsset.value = null;
			searchByExampleSelectedResourceType.value = null;
		}
	}
);

// Clear the query if not on the data explorer view
watch(
	() => route.name,
	(name) => name !== RouteName.DataExplorer && clearQuery()
);
</script>

<style scoped>
.search-bar-container {
	display: flex;
	align-items: center;
	flex-direction: column;
}

.p-input-icon-left {
	width: 100%;
	display: flex;
	align-items: start;

	/* Make the chat and close icons stick to the top as the textarea grows */
	& i:first-of-type {
		margin-left: 0.5rem;
		top: 1.25rem;
	}
	& i:last-of-type {
		top: 12px;
		margin-right: 0.25rem;
	}
}

textarea.p-inputtext {
	width: 100%;
	padding-left: 2.5rem;
	padding-right: 2.5rem;
	border: 4px solid var(--primary-color);
	border-radius: var(--border-radius-medium);
	background: var(--surface-0);
	min-height: 2.75rem;

	&:enabled {
		&:hover {
			border: 4px solid var(--primary-color);
		}
		&:focus {
			box-shadow: none;
		}
	}
}

textarea.p-inputtext {
	&:focus {
		padding-bottom: 1rem;
	}
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
	box-shadow:
		0px 4px 6px -1px rgb(0 0 0 / 8%),
		0px 2px 4px -1px rgb(0 0 0 / 6%);

	& i {
		color: var(--text-color-subdued);
		margin-left: var(--gap-small);
		z-index: 1;
	}
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

.p-button[active='false'].search-by-example-button,
.p-button[active='false'].search-by-example-button:focus,
.p-button[active='false'].search-by-example-button:enabled {
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
