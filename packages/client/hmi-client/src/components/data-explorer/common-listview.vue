<template>
	<div class="search-listview-container">
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th><span class="left-cover" />NAME</th>
						<th>DESCRIPTION</th>
						<th>SOURCE</th>
						<th>PREVIEW<span class="right-cover" /></th>
					</tr>
				</thead>
				<tbody>
					<tr
						v-for="d in items"
						:key="d.id"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="name-col">
							<div class="name-layout">
								<div class="radio">
									<component :is="getResourceTypeIcon(d.type)" />
								</div>
								<div class="content">
									<div>{{ formatName(d) }}</div>
								</div>
							</div>
						</td>
						<td class="desc-col">
							<multiline-description :text="formatDescription(d)" />
						</td>
						<td class="source-col">
							<multiline-description :text="formatSource(d)" />
						</td>
						<td class="preview-col">
							<div class="preview-container">
								<!-- preview renderer -->
							</div>
						</td>
					</tr>
					<tr v-if="items.length === 0" class="tr-item">
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
import { ResourceType, ResultType, SearchResults } from '@/types/common';
import { XDDArticle } from '@/types/XDD';
import { Model } from '@/types/Model';
import { getResourceTypeIcon, isModel, isXDDArticle } from '@/utils/data-util';

export type GenericResult = {
	id: string;
	name: string;
	desc: string;
	source: string;
	type: string;
};

const props = defineProps({
	inputItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const expandedRowId = ref('');

const { inputItems } = toRefs(props);

const items = ref<GenericResult[]>([]);

watch(
	inputItems,
	() => {
		// transform incoming results of differnt types into a generic one
		const list: GenericResult[] = [];
		inputItems.value.forEach((item) => {
			if (item.searchSubsystem === ResourceType.XDD) {
				const results = item.results as XDDArticle[];
				results.forEach((article) => {
					list.push({
						// eslint-disable-next-line no-underscore-dangle
						id: article.gddid,
						name: article.title,
						desc: article.journal ?? article.abstractText ?? '', // FIXME: XDD should always return valid abstract
						source: article.publisher ?? article.author.map((a) => a.name).join('\n'),
						type: ResourceType.XDD
					});
				});
			}
			if (item.searchSubsystem === ResourceType.MODEL) {
				const results = item.results as Model[];
				results.forEach((model) => {
					list.push({
						id: model.id,
						name: model.name,
						desc: model.description,
						source: model.framework,
						type: model.type
					});
				});
			}
		});
		items.value = list;

		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const elem: any = document.getElementsByClassName('table-fixed-head');
		if (elem.length === 0) return;
		elem[0].scrollTop = 0;
	},
	{ immediate: true }
);

const isExpanded = (item: GenericResult) => expandedRowId.value === item.id;

const updateExpandedRow = (article: GenericResult) => {
	expandedRowId.value = expandedRowId.value === article.id ? '' : article.id;
};

const formatName = (item: GenericResult) => item.name;

const formatDescription = (item: GenericResult) => {
	const maxSize = 120;
	return isExpanded(item) || item.desc.length < maxSize
		? item.desc
		: `${item.desc.substring(0, maxSize)}...`;
};

const formatSource = (item: GenericResult) => {
	const maxSize = 40;
	return isExpanded(item) || item.source.length < maxSize
		? item.source
		: `${item.source.substring(0, maxSize)}...`;
};

const getOriginalItem = (item: GenericResult) => {
	// FIXME: make this func computed
	const originalResults = props.inputItems.find((itm) => itm.searchSubsystem === item.type);
	if (originalResults) {
		const idField = item.type === ResourceType.XDD ? 'gddid' : 'id'; // FIXME
		const originalItem = originalResults.results.find((resItem) => resItem[idField] === item.id);
		return originalItem;
	}
	return undefined;
};

const isSelected = (item: GenericResult) => {
	const originalItem: ResultType | undefined = getOriginalItem(item);
	if (originalItem) {
		return props.selectedSearchItems.find((searchItem) => {
			if (isModel(originalItem)) {
				const itemAsModel = originalItem as Model;
				const searchItemAsModel = searchItem as Model;
				return searchItemAsModel.id === itemAsModel.id;
			}
			if (isXDDArticle(originalItem)) {
				const itemAsArticle = originalItem as XDDArticle;
				const searchItemAsArticle = searchItem as XDDArticle;
				return searchItemAsArticle.title === itemAsArticle.title; // FIXME: should this be gddid
			}
			return false;
		});
	}
	return false;
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

tr {
	border: 2px solid var(--separator);
	cursor: pointer;
}

thead tr,
thead th {
	border: none;
}

td {
	background: var(--background-light-1);
	vertical-align: top;
	padding: 8px 16px;
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

.text-bold {
	font-weight: 500;
	margin-bottom: 5px;
}

.name-col {
	width: 20%;
}

.name-layout {
	display: flex;
	align-content: stretch;
	align-items: stretch;
}

.name-layout .radio {
	flex: 0 0 auto;
	align-self: flex-start;
	margin: 3px 5px 0 0;
}

.name-layout .content {
	flex: 1 1 auto;
	overflow-wrap: anywhere;
}

.desc-col {
	width: 33%;
	overflow-wrap: anywhere;
}

.source-col {
	width: 20%;
}

/* time series hidden until actually put into use */
.preview-col {
	padding-left: 5px;
	padding-right: 10px;
	width: 20%;
}

.preview-container {
	background-color: #f1f1f1;
	width: 100%;
	height: 50px;
}
</style>
