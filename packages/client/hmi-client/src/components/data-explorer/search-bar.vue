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
				/>
				<i
					class="pi pi-times clear-search"
					:class="{ hidden: isClearSearchButtonHidden }"
					style="font-size: 1rem"
					@click="clearText"
				></i>
			</span>
			<i class="pi pi-history" />
		</div>
		<div class="suggested-items" v-if="isSuggestedItemsVisible">
			<span>Suggested items</span>
			<ul>
				<li v-for="item in suggestedItems" :key="item">
					<span class="item" @click="addSearchTerm(item)">{{ item }}</span>
				</li>
			</ul>
			<i
				class="pi pi-times clear-search-terms"
				style="font-size: 1rem"
				@click="isSuggestedItemsVisible = false"
			></i>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';
import { useCurrentRoute, RoutePath } from '@/router';

const props = defineProps<{
	text?: string;
	relatedSearchTerms?: string[];
}>();

const emit = defineEmits(['search-text-changed']);

const route = useRoute();
const currentRoute = useCurrentRoute();

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
};

function addSearchTerm(term) {
	searchText.value = term;
	inputElement.value?.$el.focus();
}

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

watch(currentRoute, (newRoute) => {
	if (!newRoute.path.includes(RoutePath.DataExplorer)) {
		isSuggestedItemsVisible.value = false;
	}
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

.p-inputtext {
	height: 3rem;
	border-radius: 1.5rem;
	width: 100%;
}

.pi-history {
	color: var(--un-color-body-text-secondary);
}

.clear-search:hover {
	color: var(--un-color-body-text-primary);
	background-color: var(--un-color-body-surface-secondary);
	padding: 0.5rem;
	border-radius: 1rem;
	top: 1rem;
	right: 0.5rem;
}

.clear-search-terms {
	background-color: transparent;
	padding: 0.5rem;
	border-radius: 1rem;
}

.clear-search-terms:hover {
	color: var(--un-color-body-text-primary);
	background-color: var(--un-color-body-surface-secondary);
}

.clear-search.hidden {
	visibility: hidden;
}

ul {
	list-style: none;
	display: inline-flex;
}

.item {
	background-color: var(--un-color-body-surface-background);
	padding: 0.25rem;
	margin: 0.5rem;
	border-radius: 0.5rem;
}

.item:hover {
	background-color: var(--un-color-body-surface-secondary);
	cursor: pointer;
}
</style>
