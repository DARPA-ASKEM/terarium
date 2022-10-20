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
					<tr v-for="d in items" :key="d.id" class="tr-item" @click="updateExpandedRow(d)">
						<td class="name-col">
							<div class="name-layout">
								<div class="radio">
									<i
										class="fa-regular fa-lg fa-fw"
										:class="getResourceTypeIcon(d.type)"
										style="margin-left: 4px; margin-right: 4px"
									></i>
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

<script lang="ts">
import { defineComponent, PropType, ref, toRefs, watch } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { ResourceType, SearchResults } from '@/types/common';
import { XDDArticle } from '@/types/XDD';
import { Model } from '@/types/Model';
import { getResourceTypeIcon } from '@/utils/data-util';

type GenericResult = {
	id: string;
	name: string;
	desc: string;
	source: string;
	type: string;
};

export default defineComponent({
	name: 'CommonListview',
	components: {
		MultilineDescription
	},
	props: {
		inputItems: {
			type: Array as PropType<SearchResults[]>,
			default: () => []
		}
	},
	setup(props) {
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
								id: article._gddid,
								name: article.title,
								desc: article.journal ?? article.abstract ?? '', // FIXME: XDD should always return valid abstract
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
								source: model.source,
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

		return {
			expandedRowId,
			items,
			getResourceTypeIcon
		};
	},
	methods: {
		isExpanded(item: GenericResult) {
			return this.expandedRowId === item.id;
		},
		updateExpandedRow(article: GenericResult) {
			this.expandedRowId = this.expandedRowId === article.id ? '' : article.id;
		},
		formatName(item: GenericResult) {
			return item.name;
		},
		formatDescription(item: GenericResult) {
			const maxSize = 120;
			return this.isExpanded(item) || item.desc.length < maxSize
				? item.desc
				: `${item.desc.substring(0, maxSize)}...`;
		},
		formatSource(item: GenericResult) {
			const maxSize = 40;
			return this.isExpanded(item) || item.source.length < maxSize
				? item.source
				: `${item.source.substring(0, maxSize)}...`;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
.search-listview-container {
	background: $background-light-2;
	color: black;
	width: 100%;
	table {
		border-collapse: collapse;
		width: 100%;
		vertical-align: top;
	}
	th,
	td {
		padding: 8px 16px;
	}
	tr {
		border: 2px solid $separator;
		cursor: pointer;
	}
	thead {
		tr {
			border: none;
		}

		th {
			border: none;
		}
	}
	td {
		background: $background-light-1;
		vertical-align: top;
	}
	tr th {
		font-size: $font-size-small;
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
		// Cover left and right gap in the fixed table header
		position: absolute;
		height: 100%;
		width: 2px;
		left: -2px;
		background: $background-light-2;
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
		border: 2px double $selected;
		.name-col {
			border-left: 4px solid $selected;
		}
		td {
			background-color: $tinted-background;
		}
	}
	.text-bold {
		font-weight: 500;
		margin-bottom: 5px;
	}
	.name-col {
		width: 20%;
		.name-layout {
			display: flex;
			align-content: stretch;
			align-items: stretch;
			.radio {
				flex: 0 0 auto;
				align-self: flex-start;
				margin: 3px 5px 0 0;
				.disabled {
					color: $background-light-3;
				}
			}
			.content {
				flex: 1 1 auto;
				overflow-wrap: anywhere;
				.not-ready-label {
					font-weight: 600;
					border: none;
					border-radius: 5px;
					background-color: $background-light-3;
					color: darkgray;
					padding: 6px;
					float: right;
				}
				.knobs {
					margin-top: 10px;
				}
			}
		}
	}
	.desc-col {
		width: 33%;
		overflow-wrap: anywhere;
	}
	.source-col {
		width: 20%;
	}
	// time series hidden until actually put into use
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
}
</style>
