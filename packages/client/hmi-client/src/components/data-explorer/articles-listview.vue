<template>
	<div>
		<div class="table-fixed-head">
			<table>
				<tbody>
					<tr
						v-for="d in articles"
						:key="d.gddid"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="title-and-abstract-col">
							<div class="title-and-abstract-layout">
								<!-- in case of requesting multiple selection -->
								<div class="radio" @click.stop="updateSelection(d)">
									<span v-show="isSelected(d)">
										<IconCheckboxChecked20 />
									</span>
									<span v-show="!isSelected(d)">
										<IconCheckbox20 />
									</span>
								</div>
								<div class="content">
									<div class="text-bold">{{ formatTitle(d) }}</div>
									<multiline-description :text="formatDescription(d)" />
									<div>{{ d.publisher }}, {{ d.journal }}</div>
									<div v-if="isExpanded(d)" class="knobs">
										<multiline-description :text="formatArticleAuthors(d)" />
									</div>
									<div v-if="d.highlight" class="knobs">
										<div v-for="h in d.highlight" :key="h">
											<span v-html="h"></span>
										</div>
									</div>
									<div v-html="formatKnownTerms(d)"></div>
									<div class="related-docs" @click.stop="fetchRelatedDocument(d)">
										Related Documents
									</div>
									<div v-if="isExpanded(d) && d.relatedDocuments" class="related-docs-container">
										<div v-for="a in d.relatedDocuments" :key="a.gddid">
											{{ a.title }}
											<span class="item-select" @click.stop="updateSelection(a)"
												>{{ isSelected(a) ? 'Unselect' : 'Select' }}
											</span>
										</div>
									</div>
									<div
										v-if="isExpanded(d) && d.relatedDocuments && d.relatedDocuments.length === 0"
									>
										No related documents found!
									</div>
								</div>
							</div>
						</td>
					</tr>
					<tr v-if="articles.length === 0" class="tr-item">
						<td colspan="100%" style="text-align: center">No data available</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script setup lang="ts">
import { PropType, ref, toRefs, watch } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { XDDArticle } from '@/types/XDD';
import { ResultType } from '@/types/common';
import { isXDDArticle } from '@/utils/data-util';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';
import { getRelatedDocuments } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import { ConceptFacets } from '@/types/Concept';

const props = defineProps({
	articles: {
		type: Array as PropType<XDDArticle[]>,
		default: () => []
	},
	rawConceptFacets: {
		type: Object as PropType<ConceptFacets | null>,
		default: () => null
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['toggle-article-selected']);

const expandedRowId = ref('');

const resources = useResourcesStore();

const { articles, selectedSearchItems } = toRefs(props);

watch(
	articles,
	() => {
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const elem: any = document.getElementsByClassName('table-fixed-head');
		if (elem.length === 0) return;
		elem[0].scrollTop = 0;
	},
	{ immediate: true }
);

const isExpanded = (article: XDDArticle) => expandedRowId.value === article.title;

const updateExpandedRow = (article: XDDArticle) => {
	expandedRowId.value = expandedRowId.value === article.title ? '' : article.title;
};

const formatTitle = (d: XDDArticle) => (d.title ? d.title : d.title);

const formatArticleAuthors = (d: XDDArticle) => d.author.map((a) => a.name).join('\n');

const isSelected = (article: XDDArticle) =>
	selectedSearchItems.value.find((item) => {
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			return itemAsArticle.title === article.title;
		}
		return false;
	});

const updateSelection = (article: XDDArticle) => {
	emit('toggle-article-selected', article);
};

const fetchRelatedDocument = async (article: XDDArticle) => {
	if (!isExpanded(article)) {
		updateExpandedRow(article);
	}
	if (!article.relatedDocuments) {
		article.relatedDocuments = await getRelatedDocuments(article.gddid, resources.xddDataset);
	}
};

const formatDescription = (d: XDDArticle) => {
	if (!d.abstractText || typeof d.abstractText !== 'string') return '';
	return isExpanded(d) || d.abstractText.length < 140
		? d.abstractText
		: `${d.abstractText.substring(0, 140)}...`;
};

const formatKnownTerms = (d: XDDArticle) => {
	let knownTerms = '';
	if (d.knownTerms) {
		d.knownTerms.forEach((term) => {
			knownTerms += `<b>${Object.keys(term).flat().join(' ')}</b>`;
			knownTerms += '<br />';
			knownTerms += Object.values(term).flat().join(' ');
			knownTerms += '<br />';
		});
	}
	return knownTerms;
};
</script>

<style scoped>
table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

td {
	padding: 8px 16px;
	background: var(--background-light-1);
	vertical-align: top;
}

tbody tr {
	border-top: 2px solid var(--separator);
	cursor: pointer;
}

tbody tr:first-child {
	border-top-width: 0;
}

.table-fixed-head {
	overflow-y: auto;
	overflow-x: hidden;
	height: 100%;
	width: 100%;
}

.table-fixed-head thead th {
	position: sticky;
	top: -1px;
	z-index: 1;
	background-color: aliceblue;
}

.tr-item {
	height: 50px;
}

.tr-item.selected td {
	background-color: var(--un-color-accent-lighter);
}

.text-bold {
	font-weight: 500;
	margin-bottom: 5px;
}

.title-and-abstract-col {
	width: 40%;
}

.title-and-abstract-layout {
	display: flex;
	align-content: stretch;
	align-items: stretch;
}

.title-and-abstract-layout .radio {
	flex: 0 0 auto;
	align-self: flex-start;
	margin: 3px 5px 0 0;
}

.title-and-abstract-layout .content {
	flex: 1 1 auto;
	overflow-wrap: anywhere;
}

.title-and-abstract-layout .content .knobs {
	margin-top: 10px;
}

.title-and-abstract-layout .content .related-docs {
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
