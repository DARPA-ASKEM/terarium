<template>
	<div class="table-fixed-head">
		<table>
			<tbody>
				<tr
					v-for="d in articles"
					:key="d.gddId"
					class="tr-item"
					:class="{ selected: isSelected(d) }"
					@click="togglePreview(d)"
				>
					<td>
						<div class="content-container">
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
								<!-- FIXME: isExpanded is not longer possible since click now opens a preview window -->
								<div v-if="isExpanded()" class="knobs">
									<b>Author(s):</b>
									<div>
										{{ formatArticleAuthors(d) }}
									</div>
									<div
										v-if="d.knownEntities && d.knownEntities.urlExtractions.length > 0"
										class="url-extractions"
									>
										<b>URL Extractions(s):</b>
										<div v-for="ex in d.knownEntities.urlExtractions" :key="ex.url">
											<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{
												ex.resourceTitle
											}}</a>
										</div>
									</div>
								</div>
								<div v-if="d.highlight" class="knobs">
									<span v-for="h in d.highlight" :key="h">
										<span v-html="h"></span>
									</span>
								</div>
								<div v-html="formatKnownTerms(d)"></div>
								<div class="related-docs" @click.stop="fetchRelatedDocument(d)">
									Related Documents
								</div>
								<!-- FIXME: remove once the UI menu is added to the document card allowing the user to do search-by-example from there -->
								<div class="search-by-example" @click.stop="addToSearchByExample(d)">
									Search by Example
									<IconImageSearch16 />
								</div>
								<div v-if="isExpanded() && d.relatedDocuments" class="related-docs-container">
									<div v-for="a in d.relatedDocuments" :key="a.gddId">
										{{ a.title }}
										<span class="item-select" @click.stop="updateSelection(a)"
											>{{ isSelected(a) ? 'Unselect' : 'Select' }}
										</span>
									</div>
								</div>
								<div v-if="isExpanded() && d.relatedDocuments && d.relatedDocuments.length === 0">
									No related documents found!
								</div>
							</div>
							<div v-if="d.relatedExtractions" class="content content-extractions">
								<div class="related-docs" @click.stop="togglePreview(d)">
									Extractions ({{ d.relatedExtractions?.length }})
								</div>
								<!-- FIXME: isExpanded is not longer possible since click now opens a preview window -->
								<div v-if="d.relatedExtractions && isExpanded()" class="extractions-container">
									<!-- FIXME: the content of this div is copied and adapted from Document.vue -->
									<div v-for="ex in d.relatedExtractions" :key="ex.askemId">
										<template
											v-if="
												ex.properties.image &&
												(ex.askemClass === XDDExtractionType.Figure ||
													ex.askemClass === XDDExtractionType.Table ||
													ex.askemClass === XDDExtractionType.Equation)
											"
										>
											<!-- render figure -->
											{{
												ex.properties.caption ? ex.properties.caption : ex.properties.contentText
											}}
											<img
												id="img"
												:src="'data:image/jpeg;base64,' + ex.properties.image"
												:alt="''"
											/>
										</template>
										<template v-else>
											<!-- render textual content -->
											<b>{{ ex.properties.title }}</b>
											{{ ex.properties.caption }}
											{{ ex.properties.abstractText }}
											{{ ex.properties.contentText }}
										</template>
									</div>
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
</template>

<script setup lang="ts">
import { PropType, ref, toRefs, watch } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { ResultType } from '@/types/common';
import { isXDDArticle } from '@/utils/data-util';
import { getRelatedDocuments } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import { ConceptFacets } from '@/types/Concept';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';

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

// clicked: make the item shown in the preview
// selected: add the item to the cart
const emit = defineEmits(['toggle-article-selected']);

const resources = useResourcesStore();

const previewedArticle = ref<XDDArticle | null>(null);

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

const isExpanded = () => false;

const formatTitle = (d: XDDArticle) => (d.title ? d.title : d.title);

const formatArticleAuthors = (d: XDDArticle) => d.author.map((a) => a.name).join(', ');

const isSelected = (article: XDDArticle) =>
	selectedSearchItems.value.find((item) => {
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			return itemAsArticle.title === article.title;
		}
		return false;
	});

const updateSelection = (article: XDDArticle) => {
	emit('toggle-article-selected', { item: article, type: 'selected' });
};

const togglePreview = (article: XDDArticle) => {
	emit('toggle-article-selected', { item: article, type: 'clicked' });
	previewedArticle.value = previewedArticle.value === article ? null : article;
};

const fetchRelatedDocument = async (article: XDDArticle) => {
	togglePreview(article);
	if (!article.relatedDocuments) {
		article.relatedDocuments = await getRelatedDocuments(
			// eslint-disable-next-line no-underscore-dangle
			article.gddId,
			resources.xddDataset
		);
	}
};

const formatDescription = (d: XDDArticle) => {
	if (!d.abstractText || typeof d.abstractText !== 'string') return '';
	return d.abstractText.length < 140 ? d.abstractText : `${d.abstractText.substring(0, 140)}...`;
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
	background: var(--gray-0);
	vertical-align: top;
}

tbody tr {
	border-top: 2px solid var(--gray-300);
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

.tr-item {
	/* height: 50px; */
}

.tr-item.selected td {
	background-color: var(--primary-color-lighter);
}

.text-bold {
	font-weight: 500;
	margin-bottom: 5px;
}

.content-container {
	display: flex;
	align-content: stretch;
	align-items: stretch;
}

.content-container .radio {
	flex: 0 0 auto;
	align-self: flex-start;
	margin: 3px 5px 0 0;
}

.content-container .content {
	flex: 1 1 auto;
	overflow-wrap: anywhere;
}

.content-container .content-extractions {
	width: 100%;
	overflow-y: auto;
	max-height: 500px;
}

.content-container .content .knobs {
	margin-top: 10px;
}

.content-container .content .knobs .url-extractions {
	display: flex;
	flex-direction: column;
}

.content-container .content .related-docs {
	margin-top: 1rem;
	color: blue;
}

.content-container .content .related-docs:hover {
	text-decoration: underline;
}

.content-container .content .related-docs-container {
	display: flex;
	flex-direction: column;
	gap: 4px;
	margin-left: 1rem;
}

.content-container .content .related-docs-container .item-select {
	color: green;
	font-weight: bold;
}

.content-container .content .related-docs-container .item-select:hover {
	text-decoration: underline;
}

.content-container .content .search-by-example {
	margin-top: 1rem;
	color: black;
	display: flex;
	align-items: center;
	gap: 4px;
}

.content-container .content .search-by-example:hover {
	text-decoration: underline;
}

.extractions-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	width: 100%;
	height: 100%;
	overflow-y: auto;
}
</style>
