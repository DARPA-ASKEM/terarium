<template>
	<div class="search-listview-container">
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th><span class="left-cover" />TITLE and ABSTRACT</th>
						<th>PUBLISHER and AUTHOR</th>
						<th>JOURNAL</th>
						<th>KNOWN TERMS</th>
						<th>PREVIEW<span class="right-cover" /></th>
					</tr>
				</thead>
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
									<component :is="getResourceTypeIcon(ResourceType.XDD)" />
									<!-- Not sure if I should just make this the icon, there might have been a reason this was dynamic before-->
								</div>
								<div class="content">
									<div class="text-bold">{{ formatTitle(d) }}</div>
									<multiline-description :text="formatDescription(d)" />
								</div>
							</div>
						</td>
						<td class="publisher-and-author-col">
							<div class="text-bold">{{ d.publisher }}</div>
							<div v-if="isExpanded(d)" class="knobs">
								<multiline-description :text="formatArticleAuthors(d)" />
							</div>
						</td>
						<td class="journal-col">
							<div class="text-bold">{{ d.journal }}</div>
							<div>{{ d.type ?? '' }}</div>
						</td>
						<td class="known-terms-col">
							<div v-html="formatKnownTerms(d)"></div>
						</td>
						<td class="preview-col">
							<div class="preview-container">
								<!-- preview renderer -->
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
import { ResourceType, ResultType } from '@/types/common';
import { getResourceTypeIcon, isXDDArticle } from '@/utils/data-util';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';

const props = defineProps({
	articles: {
		type: Array as PropType<XDDArticle[]>,
		default: () => []
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['toggle-article-selected']);

const expandedRowId = ref('');

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
.search-listview-container {
	background: var(--background-light-2);
	color: black;
	width: 100%;
}

table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

th {
	padding: 8px 16px;
}

td {
	padding: 8px 16px;
	background: var(--background-light-1);
	vertical-align: top;
}

tr {
	border: 2px solid var(--separator);
	cursor: pointer;
}

thead tr,
thead td {
	border: none;
}

tr th {
	font-size: var(--font-size-small);
	font-weight: normal;
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

.left-cover,
.right-cover {
	/* Cover left and right gap in the fixed table header */
	position: absolute;
	height: 100%;
	width: 2px;
	left: -2px;
	background: var(--background-light-2);
	top: 0;
}

.right-cover {
	left: unset;
	right: -2px;
}

.tr-item {
	height: 50px;
}

.tr-item.selected {
	border: 2px double var(--un-color-accent-lighter);
}

.tr-item.selected .title-and-abstract-col {
	border-left: 2px solid var(--un-color-accent-lighter);
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

.publisher-and-author-col {
	width: 33%;
	overflow-wrap: anywhere;
}

.known-terms-col {
	width: 20%;
}

.journal-col {
	width: 120px;
}

/* time series hidden until actually put into use */
.preview-col {
	padding-left: 5px;
	padding-right: 10px;
}

.preview-container {
	background-color: #f1f1f1;
	width: 100px;
	height: 50px;
}
</style>
