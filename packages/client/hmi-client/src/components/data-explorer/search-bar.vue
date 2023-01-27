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
					class="input-text"
				/>
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
		<div class="suggested-items" v-if="isSuggestedItemsVisible">
			Suggested items
			<ul>
				<li v-for="item in suggestedItems" :key="item">
					<Button :label="item" class="item" @click="addSearchTerm(item)" />
				</li>
			</ul>
			<Button
				icon="pi pi-times"
				class="p-button-rounded clear-search-terms"
				@click="isSuggestedItemsVisible = false"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import * as EventService from '@/services/event';
import { EventType } from '@/types/EventType';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	text?: string;
	relatedSearchTerms?: string[];
}>();

const emit = defineEmits(['search-text-changed', 'toggle-search-by-example']);

const route = useRoute();
const resources = useResourcesStore();

const searchText = ref('');
const defaultText = computed(() => props.text);
const isClearSearchButtonHidden = computed(() => !searchText.value);
const isSuggestedItemsVisible = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const suggestedItems = computed(() => props.relatedSearchTerms);

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
const toggleSearchByExample = () => {
	emit('toggle-search-by-example');
};

onMounted(() => {
	const { q } = route.query;
	searchText.value = q?.toString() ?? searchText.value;
	isSuggestedItemsVisible.value = suggestedItems.value ? suggestedItems.value.length > 0 : false;
});

watch(defaultText, (newText) => {
	searchText.value = newText || searchText.value;
});

watch(suggestedItems, (newItems) => {
	isSuggestedItemsVisible.value = newItems ? newItems.length > 0 : false;
});
</script>

<style scoped>
.search-bar-container {
	display: flex;
	align-items: center;
	flex-direction: column;
}

.search {
	display: flex;
	align-items: center;
	width: 100%;
}

.suggested-items {
	margin: 1rem 0 0.5rem 0;
	height: 2rem;
	display: flex;
	align-items: center;
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

.p-button.p-button-icon-only.p-button-rounded {
	height: 2rem;
	width: 2rem;
}

ul {
	list-style: none;
	display: inline-flex;
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
</style>
