<template>
	<div class="search-bar-container">
		<span class="p-input-icon-left p-input-icon-right">
			<i class="pi pi-search" />
			<InputText
				type="text"
				placeholder="Search"
				v-model="searchText"
				@keyup.enter="addSearchTerm"
			/>
			<i class="pi pi-times clear-search" style="font-size: 1rem" @click="clearText"></i>
		</span>
		<i class="pi pi-history" />
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import InputText from 'primevue/inputtext';

const props = defineProps({
	realtime: {
		type: Boolean,
		default: false
	},
	searchLabel: {
		type: String,
		default: ''
	},
	searchPlaceholder: {
		type: String,
		default: 'Search for resources'
	},
	focusInput: {
		type: Boolean,
		default: true
	}
});

const emit = defineEmits(['search-text-changed']);

const inputElement = ref<HTMLInputElement | null>(null);

const searchText = ref('');
const searchTerms = ref('');

const clearText = () => {
	searchText.value = '';
	searchTerms.value = '';
};

const execSearch = () => {
	emit('search-text-changed', searchTerms.value);
};

const addSearchTerm = (event: Event) => {
	if (!props.realtime) {
		const term = (event.target as HTMLInputElement).value;
		searchTerms.value = term;
		execSearch();
	}
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}
});

watch(searchTerms, () => {
	execSearch();
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

.p-inputtext {
	height: 3rem;
	border-radius: 1.5rem;
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
</style>
