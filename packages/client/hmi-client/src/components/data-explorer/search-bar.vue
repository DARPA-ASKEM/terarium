<template>
	<div class="search-bar-container">
		<div class="search">
			<span class="p-input-icon-left p-input-icon-right">
				<i class="pi pi-search" />
				<InputText
					ref="input"
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
		<div class="suggested-items">
			<span>Suggested items</span>
			<ul>
				<li v-for="term in relatedSearchTerms" :key="term">
					<span class="term" @click="addSearchTerm(term)">{{ term }}</span>
				</li>
			</ul>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';

const props = defineProps<{
	text?: string;
	relatedSearchTerms?: string[];
}>();

const emit = defineEmits(['search-text-changed']);

const route = useRoute();

const searchText = ref('');
const defaultText = computed(() => props.text);
const isClearSearchButtonHidden = computed(() => !searchText.value);
const input = ref<HTMLInputElement | null>(null);

const clearText = () => {
	searchText.value = '';
};

const execSearch = () => {
	emit('search-text-changed', searchText.value);
};

function addSearchTerm(term) {
	searchText.value = term;
	input?.value?.focus();
}

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
}

.search {
	display: flex;
	align-items: center;
	width: 100%;
}

.suggested-items {
	padding-top: 0.5rem;
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

.clear-search.hidden {
	visibility: hidden;
}

li {
	/* display: flex; */
}

ul {
	list-style: none;
	display: inline-flex;
}

.term {
	background-color: var(--un-color-body-surface-background);
	padding: 0.25rem;
	margin: 0.5rem;
	border-radius: 0.5rem;
}

.term:hover {
	background-color: var(--un-color-body-surface-secondary);
	cursor: pointer;
}
</style>
