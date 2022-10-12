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
						:key="d._gddid"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="title-and-abstract-col">
							<div class="title-and-abstract-layout">
								<!-- in case of requesting multiple selection -->
								<div class="radio" @click.stop="updateSelection(d)">
									<template v-if="enableMultipleSelection">
										<span v-show="isSelected(d)"
											><i class="fa-lg fa-regular fa-square-check"></i
										></span>
										<span v-show="!isSelected(d)"><i class="fa-lg fa-regular fa-square"></i></span>
									</template>
									<template v-else>
										<span v-show="isSelected(d)"><i class="fa-lg fa-regular fa-circle"></i></span>
										<span v-show="!isSelected(d)"
											><i class="fa-lg fa-regular fa-circle-xmark"></i
										></span>
									</template>
									<i
										class="fa-lg fa-solid fa-file-lines"
										style="margin-left: 4px; margin-right: 4px"
									></i>
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

<script lang="ts">
// import moment from 'moment';
import { defineComponent, PropType, ref, toRefs, watch } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { XDDArticle } from '@/types/XDD';

export default defineComponent({
	name: 'ArticlesListview',
	components: {
		MultilineDescription
	},
	props: {
		articles: {
			type: Array as PropType<XDDArticle[]>,
			default: () => []
		},
		selectedSearchItems: {
			type: Array as PropType<string[]>,
			required: true
		},
		enableMultipleSelection: {
			type: Boolean,
			default: false
		}
	},
	emits: ['toggle-article-selected', 'set-article-selected'],
	setup(props) {
		const expandedRowId = ref('');

		const { articles } = toRefs(props);

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

		return {
			expandedRowId
		};
	},
	methods: {
		isExpanded(article: XDDArticle) {
			return this.expandedRowId === article.title;
		},
		updateExpandedRow(article: XDDArticle) {
			this.expandedRowId = this.expandedRowId === article.title ? '' : article.title;
		},
		formatTitle(d: XDDArticle) {
			return d.title ? d.title : d.title;
		},
		formatArticleAuthors(d: XDDArticle) {
			return d.author.map((a) => a.name).join('\n');
		},
		isSelected(article: XDDArticle) {
			return this.selectedSearchItems.find((item) => item === article.title) !== undefined;
		},
		updateSelection(article: XDDArticle) {
			const item = article.title;
			if (this.enableMultipleSelection) {
				// if the article is not in the list add it, otherwise remove it
				this.$emit('toggle-article-selected', item);
			} else {
				// only one selection is allowed, so replace the entire array
				this.$emit('set-article-selected', item);
			}
		},
		formatDescription(d: XDDArticle) {
			if (!d.abstract) return '';
			return this.isExpanded(d) || d.abstract.length < 140
				? d.abstract
				: `${d.abstract.substring(0, 140)}...`;
		},
		formatKnownTerms(d: XDDArticle) {
			let knownTerms = '';
			if (d.known_terms) {
				d.known_terms.forEach((term) => {
					knownTerms += `<b>${Object.keys(term).flat().join(' ')}</b>`;
					knownTerms += '<br />';
					knownTerms += Object.values(term).flat().join(' ');
				});
			}
			return knownTerms;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
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
		.title-and-abstract-col {
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
	.title-and-abstract-col {
		width: 40%;
		.title-and-abstract-layout {
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
	// time series hidden until actually put into use
	.preview-col {
		padding-left: 5px;
		padding-right: 10px;
	}
	.preview-container {
		background-color: #f1f1f1;
		width: 100px;
		height: 50px;
	}
}
</style>
