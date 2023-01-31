<template>
	<section class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<InputText
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
				/>
			</span>
			<!-- <i class="pi pi-history" /> -->
			<!-- <i class="pi pi-image" title="Search by Example" @click="toggleSearchByExample" /> -->
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';
import * as EventService from '@/services/event';
import { EventType } from '@/types/EventType';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	text?: string;
	suggestedTerms?: string[];
}>();

const emit = defineEmits(['search-text-changed', 'toggle-search-by-example']);

const route = useRoute();
const resources = useResourcesStore();

const searchText = ref('');
const defaultText = computed(() => props.text);
const isClearSearchButtonHidden = computed(() => !searchText.value);

const clearText = () => {
	searchText.value = '';
};

const execSearch = () => {
	emit('search-text-changed', searchText.value);
	EventService.create(EventType.Search, resources.activeProject?.id, searchText.value);
};

// const toggleSearchByExample = () => {
// 	emit('toggle-search-by-example');
// };

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
</style>
