<script setup lang="ts">
import { ref } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
// import { getRelatedDocuments } from '@/services/data';
// import useResourcesStore from '@/stores/resources';
import { XDDArticle } from '@/types/XDD';
// import { isXDDArticle } from '@/utils/data-util';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';
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

const isSelected = () => true;
// props.selectedSearchItems.find((item) => {
//     console.log(props.d)
//     console.log(props.selectedSearchItems)
//     if (isXDDArticle(item)) {
//         const itemAsArticle = item as XDDArticle;
//         return itemAsArticle.title === props.d.title;
//     }
//     return false;
// });

const fetchRelatedDocument = async () => {
	if (!isExpanded()) {
		updateExpandedRow();
	}
	if (!props.d.relatedDocuments) {
		// props.d.relatedDocuments = await getRelatedDocuments(props.d.gddid, resources.xddDataset);
	}
};

const formatTitle = () => (props.d.title ? props.d.title : props.d.title);
const formatArticleAuthors = () => props.d.author.map((a) => a.name).join(', ');

const formatDescription = () => {
	if (!props.d.abstractText || typeof props.d.abstractText !== 'string') return '';
	return isExpanded() || props.d.abstractText.length < 140
		? props.d.abstractText
		: `${props.d.abstractText.substring(0, 140)}...`;
};

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
	<td class="title-and-abstract-col" @click="updateExpandedRow()">
		<div class="title-and-abstract-layout">
			<!-- in case of requesting multiple selection -->
			<div class="radio" @click.stop="emit('toggle-article-selected', d)">
				<span v-show="isSelected()">
					<IconCheckboxChecked20 />
				</span>
				<span v-show="!isSelected()">
					<IconCheckbox20 />
				</span>
			</div>
			<div class="content">
				<div class="text-bold">{{ formatTitle() }}</div>
				<multiline-description :text="formatDescription()" />
				<div>{{ d.publisher }}, {{ d.journal }}</div>
				<div v-if="isExpanded()" class="knobs">
					<b>Author(s):</b>
					<div>
						{{ formatArticleAuthors() }}
					</div>
					<div
						v-if="d.knownEntities && d.knownEntities.url_extractions.length > 0"
						class="url-extractions"
					>
						<b>URL Extractions(s):</b>
						<div v-for="ex in d.knownEntities.url_extractions" :key="ex.url">
							<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{
								ex.resource_title
							}}</a>
						</div>
					</div>
				</div>
				<div v-if="d.highlight" class="knobs">
					<span v-for="h in d.highlight" :key="h">
						<span v-html="h"></span>
					</span>
				</div>
				<div v-html="formatKnownTerms()"></div>
				<div class="related-docs" @click.stop="fetchRelatedDocument()">Related Documents</div>
				<div v-if="isExpanded() && d.relatedDocuments" class="related-docs-container">
					<!-- <div v-for="a in d.relatedDocuments" :key="a.gddid">
                        {{ a.title }}
                        <span class="item-select" @click.stop="updateSelection(a)">{{
                            isSelected(a) ?'Unselect':
                                'Select'
                        }}
                        </span>
                    </div> -->
				</div>
				<div v-if="isExpanded() && d.relatedDocuments && d.relatedDocuments.length === 0">
					No related documents found!
				</div>
			</div>
		</div>
	</td>
</template>

<style>
title-and-abstract-col {
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

.title-and-abstract-layout .content .knobs .url-extractions {
	display: flex;
	flex-direction: column;
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
