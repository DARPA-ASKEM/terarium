<script setup lang="ts">
import { XDDArticle } from '@/types/XDD';
import { isXDDArticle } from '@/utils/data-util';
import IconAdd24 from '@carbon/icons-vue/es/add/24';
import IconCheckmark24 from '@carbon/icons-vue/es/checkmark/24';
import { ResultType } from '@/types/common';

const props = defineProps<{
	d: XDDArticle;
	previewedArticle: XDDArticle | null;
	selectedSearchItems: ResultType[];
}>();

const emit = defineEmits(['toggle-article-selected', 'toggle-article-preview']);

const isSelected = () =>
	props.selectedSearchItems.find((item) => {
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			return itemAsArticle.title === props.d.title;
		}
		return false;
	});

const formatTitle = () => (props.d.title ? props.d.title : props.d.title);
const formatArticleAuthors = () => props.d.author.map((a) => a.name).join(', ');
</script>

<template>
	<div class="search-item" :active="previewedArticle === d" @click="emit('toggle-article-preview')">
		<div>
			<div>ARTICLE</div>
			<div class="title">{{ formatTitle() }}</div>
			<div class="details">{{ formatArticleAuthors() }} ({{ d.year }}) {{ d.journal }}</div>
			<ul class="snippets" v-if="d.highlight">
				<li v-for="h in d.highlight" :key="h">...<span v-html="h"></span>...</li>
			</ul>
		</div>
		<div @click.stop="emit('toggle-article-selected')">
			<IconAdd24 v-show="!isSelected()" />
			<IconCheckmark24 class="checkmark-color" v-show="isSelected()" />
		</div>
	</div>
</template>

<style>
.search-item {
	background-color: white;
	color: var(--un-color-body-text-secondary);
	padding: 1rem;
	margin: 1px;
	display: flex;
	align-content: stretch;
	align-items: stretch;
	justify-content: space-between;
}

.search-item:hover {
	background-color: var(--un-color-feedback-success-lighter);
}

.search-item[active='true'] {
	outline: 1px solid var(--un-color-feedback-success);
}

.title {
	font-weight: 500;
	color: var(--un-color-body-text-primary);
	margin: 0.5rem 0 0.25rem 0;
}

.details {
	margin: 0.25rem 0 0.5rem 0;
}

svg {
	cursor: pointer;
}

.checkmark-color {
	color: var(--un-color-feedback-success);
}

.snippets {
	list-style: none;
}
</style>
