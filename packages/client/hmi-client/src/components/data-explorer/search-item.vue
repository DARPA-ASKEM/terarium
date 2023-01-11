<script setup lang="ts">
import { ref } from 'vue';
// import { getRelatedDocuments } from '@/services/data';
// import useResourcesStore from '@/stores/resources';
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

// const resources = useResourcesStore();

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

// const fetchRelatedDocument = async () => {
// 	if (!isExpanded()) {
// 		updateExpandedRow();
// 	}
// 	if (!props.d.relatedDocuments) {
// 		// props.d.relatedDocuments = await getRelatedDocuments(props.d.gddid, resources.xddDataset);
// 	}
// };

const formatTitle = () => (props.d.title ? props.d.title : props.d.title);
const formatArticleAuthors = () => props.d.author.map((a) => a.name).join(', ');

// const formatDescription = () => {
// 	if (!props.d.abstractText || typeof props.d.abstractText !== 'string') return '';
// 	return isExpanded() || props.d.abstractText.length < 140
// 		? props.d.abstractText
// 		: `${props.d.abstractText.substring(0, 140)}...`;
// };

const formatKnownTerms = () => {
	let knownTerms = '';
	if (props.d.knownTerms) {
		props.d.knownTerms.forEach((term) => {
			knownTerms += `<b>${Object.keys(term).flat().join(' ')}</b>`;
			knownTerms += '<br />';
			knownTerms += Object.values(term).flat().join(' ');
			knownTerms += '<br />';
		});
	}
	return knownTerms;
};
</script>

<template>
	<div class="search-item" @click="updateExpandedRow()">
		<div class="content">
			<div>ARTICLE</div>
			<div class="title">{{ formatTitle() }}</div>
			<div>{{ formatArticleAuthors() }} ({{ d.year }}) {{ d.journal }}</div>
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
			<div v-if="d.highlight" class="knobs">
				<span v-for="h in d.highlight" :key="h">
					<span v-html="h"></span>
				</span>
			</div>
			<div v-html="formatKnownTerms()"></div>
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
	display: flex;
	align-content: stretch;
	align-items: stretch;
	justify-content: space-between;
}

.title {
	font-weight: 500;
	color: var(--un-color-body-text-primary);
}

svg {
	cursor: pointer;
}

.checkmark-color {
	color: var(--un-color-feedback-success);
}

.title-and-abstract-layout .content {
	flex: 1 1 auto;
	overflow-wrap: anywhere;
}

.title-and-abstract-layout .content .knobs {
	margin-top: 10px;
}

.title-and-abstract-layout .content .knobs .url-extractions {
	display: flex;
	flex-direction: column;
}

.search-item .content .related-docs {
	margin-top: 1rem;
	color: blue;
}

.title-and-abstract-layout .content .related-docs:hover {
	text-decoration: underline;
}

.title-and-abstract-layout .content .related-docs-container {
	display: flex;
	flex-direction: column;
	gap: 4px;
	margin-left: 1rem;
}

.title-and-abstract-layout .content .related-docs-container .item-select {
	color: green;
	font-weight: bold;
}

.title-and-abstract-layout .content .related-docs-container .item-select:hover {
	text-decoration: underline;
}
</style>
