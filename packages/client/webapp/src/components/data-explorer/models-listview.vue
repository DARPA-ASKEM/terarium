<template>
	<div class="search-listview-container">
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th><span class="left-cover" />NAME and DESCRIPTION</th>
						<th>SOURCE</th>
						<th>CATEGORY</th>
						<th>PREVIEW<span class="right-cover" /></th>
					</tr>
				</thead>
				<tbody>
					<tr
						v-for="d in models"
						:key="d.id"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="name-and-desc-col">
							<div class="name-and-desc-layout">
								<!-- in case of requesting multiple selection -->
								<div class="radio" @click.stop="updateSelection(d)">
									<template v-if="enableMultipleSelection">
										<span v-show="isSelected(d)" :class="{ disabled: isDisabled(d) }"
											><i class="fa-lg fa-regular fa-square-check"></i
										></span>
										<span v-show="!isSelected(d)" :class="{ disabled: isDisabled(d) }"
											><i class="fa-lg fa-regular fa-square"></i
										></span>
									</template>
									<template v-else>
										<span v-show="isSelected(d)" :class="{ disabled: isDisabled(d) }"
											><i class="fa-lg fa-regular fa-circle"></i
										></span>
										<span v-show="!isSelected(d)" :class="{ disabled: isDisabled(d) }"
											><i class="fa-lg fa-regular fa-circle-xmark"></i
										></span>
									</template>
									<i class="fa-regular fa-lg fa-fw" :class="getTypeIcon(d)" />
								</div>
								<div class="content">
									<button v-if="isNotPublished(d)" type="button" class="not-ready-label">
										Not Published
									</button>
									<button v-if="isDeprecated(d)" type="button" class="not-ready-label">
										Deprecated
									</button>
									<button v-if="isProcessing(d)" type="button" class="not-ready-label">
										Processing
									</button>
									<div class="text-bold">{{ formatOutputName(d) }}</div>
									<multiline-description :text="formatDescription(d)" />
								</div>
							</div>
						</td>
						<td class="source-col">
							<div class="text-bold">{{ d.source }}</div>
						</td>
						<td class="category-col">
							<div class="text-bold">{{ d.category }}</div>
						</td>
						<td class="preview-col">
							<div class="preview-container">
								<!-- preview renderer -->
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, toRefs, watch } from 'vue';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { Model } from '@/types/Model';
import { ResourceType } from '@/types/common';

/**
 * name: string;
	description: string;
	source: string; // author and affailiation
	status: string;
	category: string;
 */

export default defineComponent({
	name: 'ModelsListview',
	components: {
		MultilineDescription
	},
	props: {
		models: {
			type: Array as PropType<Model[]>,
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
	emits: ['toggle-model-selected', 'set-model-selected'],
	setup(props) {
		const expandedRowId = ref('');

		const { models } = toRefs(props);

		watch(
			models,
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
		isDisabled(model: Model) {
			return model.status === 'deprecated';
		},
		isProcessing(model: Model) {
			return model.status === 'processing';
		},
		isNotPublished(model: Model) {
			return model.status === 'registered';
		},
		isDeprecated(model: Model) {
			return model.status === 'deprecated';
		},
		isExpanded(model: Model) {
			return this.expandedRowId === model.id;
		},
		updateExpandedRow(model: Model) {
			this.expandedRowId = this.expandedRowId === model.id ? '' : model.id;
		},
		formatOutputName(d: Model) {
			return d.name;
		},
		formatOutputDescription(d: Model) {
			return d.description;
		},
		isSelected(model: Model) {
			return this.selectedSearchItems.find((item) => item === model.id) !== undefined;
		},
		updateSelection(model: Model) {
			if (!this.isDisabled(model)) {
				const item = model.id;
				if (this.enableMultipleSelection) {
					// if the model is not in the list add it, otherwise remove it
					this.$emit('toggle-model-selected', item);
				} else {
					// only one selection is allowed, so replace the entire array
					this.$emit('set-model-selected', item);
				}
			}
		},
		formatDescription(d: Model) {
			if (!d.description) return '';
			return this.isExpanded(d) || d.description.length < 140
				? d.description
				: `${d.description.substring(0, 140)}...`;
		},
		getTypeIcon(d: Model) {
			return `fa-regular ${
				d.type === ResourceType.MODEL ? 'fa-brands fa-connectdevelop' : 'fa-solid fa-table-cells'
			}`;
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
		.name-and-desc-col {
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
	.name-and-desc-col {
		width: 45%;
		.name-and-desc-layout {
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
	.source-col {
		width: 33%;
		overflow-wrap: anywhere;
	}
	.category-col {
		width: 10%;
	}
	// time series hidden until actually put into use
	.preview-col {
		padding-left: 5px;
		padding-right: 10px;
	}
	.preview-container {
		background-color: #f1f1f1;
		width: 100%;
		height: 50px;
	}
}
</style>
