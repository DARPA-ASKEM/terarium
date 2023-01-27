<script setup lang="ts">
import IconClose16 from '@carbon/icons-vue/es/close/16';
import { ref, PropType, computed } from 'vue';
import { ResultType, SearchByExampleOptions } from '@/types/common';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import Checkbox from 'primevue/checkbox';
import Button from '@/components/Button.vue';

const props = defineProps({
	item: {
		type: Object as PropType<ResultType | null>,
		default: () => null
	}
});

const emit = defineEmits(['hide', 'search']);

const searchOptions = ref<SearchByExampleOptions>({
	similarContent: true,
	forwardCitation: false,
	bakcwardCitation: false,
	relatedContent: false
});

const onClose = () => {
	emit('hide');
};

function search() {
	emit('search', searchOptions.value);
	onClose();
}

const isSearchDisabled = computed(() => {
	if (!props.item) return true;
	if (
		!searchOptions.value.similarContent &&
		!searchOptions.value.forwardCitation &&
		!searchOptions.value.bakcwardCitation &&
		!searchOptions.value.relatedContent
	) {
		return true;
	}
	return false;
});

const getTitle = (item: ResultType) => (item as Model).name || (item as XDDArticle).title;
</script>

<template>
	<div class="search-by-example-card">
		<div class="header">
			<h3>Search by Example</h3>
			<IconClose16 class="hover-hand" @click="onClose" />
		</div>
		<div class="search-items-container">
			<!--
				list of dropped items here: this should be rendered component items
				FIXME: for now, render their title
			-->
			<div v-if="props.item">
				{{ getTitle(props.item) }}
			</div>
		</div>
		<footer>
			<Checkbox v-model="searchOptions.similarContent" value="Similar Content" />
			<Checkbox v-model="searchOptions.forwardCitation" value="Forward Citation" />
			<Checkbox v-model="searchOptions.bakcwardCitation" value="Backward Content" />
			<Checkbox v-model="searchOptions.relatedContent" value="Related Content" />

			<Button :class="{ disabled: isSearchDisabled }" :disabled="isSearchDisabled" @click="search"
				>Search</Button
			>
		</footer>
	</div>
</template>

<style scoped>
.search-by-example-card {
	border: 1px solid var(--surface-border);
	background-color: var(--surface-card);
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	border-radius: 0.5rem;
	transition: 0.2s;
	text-align: left;
	position: absolute;
	top: 48px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	color: black;
	padding: 1rem;
	width: 50%;
	z-index: 2;
}

.search-by-example-card > button {
	font-size: 1.25rem;
	font-weight: 500;
	border-radius: 0.5rem;
	width: 100%;
	height: 100%;
	margin: auto;
	justify-content: center;
	cursor: pointer;
	background-color: transparent;
	color: var(--text-color-disabled);
}

.search-by-example-card button:hover {
	background-color: var(--surface-secondary);
	color: var(--text-color-primary);
}

.search-by-example-card button:disabled {
	cursor: not-allowed;
	color: var(--text-color-primary);
}

.search-by-example-card button svg {
	color: var(--text-color-disabled);
}

.search-by-example-card button:hover svg {
	color: var(--text-color-primary);
}

.search-items-container {
	border-width: 2px;
	border-style: dotted;
	border-color: black;
	display: flex;
	flex-direction: column;
	gap: 4px;
	height: 100px;
	overflow-y: auto;
}

.header {
	margin-bottom: 1em;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

footer {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 1rem;
	margin-top: 2rem;
}

footer div {
	display: flex;
}

footer div:hover {
	cursor: pointer;
	text-decoration: underline;
}

.hover-hand {
	cursor: pointer;
}

.hover-hand:hover {
	color: red;
}
</style>
