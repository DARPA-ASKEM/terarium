<template>
	<div class="search-bar-container">
		<span class="p-input-icon-left p-input-icon-right">
			<i class="pi pi-search" />
			<InputText
				type="text"
				class="input-text"
				placeholder="Search"
				v-model="searchText"
				@keyup.enter="addSearchTerm"
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
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import InputText from 'primevue/inputtext';

const props = defineProps<{
	text?: string;
}>();

const emit = defineEmits(['search-text-changed']);

const route = useRoute();

const searchText = ref('');
const defaultText = computed(() => props.text);
const isClearSearchButtonHidden = computed(() => !searchText.value);

const clearText = () => {
	searchText.value = '';
};

const execSearch = () => {
	emit('search-text-changed', searchText.value);
};

const addSearchTerm = () => {
	execSearch();
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
}

.pi-history {
	color: var(--text-color-secondary);
}

.clear-search:hover {
	color: var(--text-color-primary);
	background-color: var(--surface-secondary);
	padding: 0.5rem;
	border-radius: 1rem;
	top: 1rem;
	right: 0.5rem;
}

.clear-search.hidden {
	visibility: hidden;
}
</style>
