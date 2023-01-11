<script setup lang="ts">
import { ref } from 'vue';
import { XDDArticle } from '@/types/XDD';
import { isXDDArticle } from '@/utils/data-util';
import IconAdd24 from '@carbon/icons-vue/es/add/24';
import IconCheckmark24 from '@carbon/icons-vue/es/checkmark/24';
import { ResultType } from '@/types/common';

const props = defineProps<{
	d: XDDArticle;
	selectedSearchItems: ResultType[];
}>();

const emit = defineEmits(['toggle-article-selected']);

const expandedRowId = ref('');

const isExpanded = () => expandedRowId.value === props.d.title;

const updateExpandedRow = () => {
	expandedRowId.value = expandedRowId.value === props.d.title ? '' : props.d.title;
};

const isSelected = () =>
	props.selectedSearchItems.find((item) => {
		console.log(props.d);
		console.log(props.selectedSearchItems);
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
	<div class="search-item" @click="updateExpandedRow()">
		<div>
			<div>ARTICLE</div>
			<div class="title">{{ formatTitle() }}</div>
			<div class="details">{{ formatArticleAuthors() }} ({{ d.year }}) {{ d.journal }}</div>

			<div v-if="isExpanded()" class="knobs">
				<div
					v-if="d.knownEntities && d.knownEntities.url_extractions.length > 0"
					class="url-extractions"
				>
					<b>URL Extractions(s):</b>
					<div v-for="ex in d.knownEntities.url_extractions" :key="ex.url">
						<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{ ex.resource_title }}</a>
					</div>
				</div>
			</div>

			<ul class="snippets" v-if="d.highlight">
				<li v-for="h in d.highlight" :key="h">...<span v-html="h"></span>...</li>
			</ul>
			<!-- <div v-html="formatKnownTerms()"></div> -->
			<footer></footer>
		</div>
		<div>
			<IconAdd24 v-show="!isSelected()" @click.stop="emit('toggle-article-selected')" />
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

.search-item:focus {
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

/* .content {
	flex: 1 1 auto;
	overflow-wrap: anywhere;
}

.content .knobs {
	margin-top: 10px;
} */

/* .content .knobs .url-extractions {
	display: flex;
	flex-direction: column;
}

.search-item .content .related-docs {
	margin-top: 1rem;
	color: blue;
}

.content .related-docs:hover {
	text-decoration: underline;
}

.content .related-docs-container {
	display: flex;
	flex-direction: column;
	gap: 4px;
	margin-left: 1rem;
}

.content .related-docs-container .item-select {
	color: green;
	font-weight: bold;
}

.content .related-docs-container .item-select:hover {
	text-decoration: underline;
} */
</style>
