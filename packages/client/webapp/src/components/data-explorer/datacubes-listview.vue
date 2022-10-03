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
						v-for="d in datacubes"
						:key="d.id"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="output-col">
							<div class="output-layout">
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
						<td class="desc-col">
							<div class="text-bold">{{ d.source }}</div>
						</td>
						<td class="period-col">
							<div class="text-bold">{{ d.category }}</div>
						</td>
						<td class="timeseries-col">
							<div class="timeseries-container">
								<!-- timeseries renderer -->
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
import { Datacube } from '@/types/Datacube';

/**
 * name: string;
	description: string;
	source: string; // author and affailiation
	status: string;
	category: string;
 */

export default defineComponent({
	name: 'DatacubesListview',
	components: {
		MultilineDescription
	},
	props: {
		datacubes: {
			type: Array as PropType<Datacube[]>,
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
	emits: ['toggle-datacube-selected', 'set-datacube-selected'],
	setup(props) {
		const expandedRowId = ref('');

		const { datacubes } = toRefs(props);

		watch(
			datacubes,
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
		isDisabled(datacube: Datacube) {
			return datacube.status === 'deprecated';
		},
		isProcessing(datacube: Datacube) {
			return datacube.status === 'processing';
		},
		isNotPublished(datacube: Datacube) {
			return datacube.status === 'registered';
		},
		isDeprecated(datacube: Datacube) {
			return datacube.status === 'deprecated';
		},
		isExpanded(datacube: Datacube) {
			return this.expandedRowId === datacube.id;
		},
		updateExpandedRow(datacube: Datacube) {
			this.expandedRowId = this.expandedRowId === datacube.id ? '' : datacube.id;
		},
		formatOutputName(d: Datacube) {
			return d.display_name ? d.display_name : d.name;
		},
		formatOutputDescription(d: Datacube) {
			return d.description;
		},
		isSelected(datacube: Datacube) {
			return this.selectedSearchItems.find((item) => item === datacube.id) !== undefined;
		},
		updateSelection(datacube: Datacube) {
			if (!this.isDisabled(datacube)) {
				const item = datacube.id;
				if (this.enableMultipleSelection) {
					// if the datacube is not in the list add it, otherwise remove it
					this.$emit('toggle-datacube-selected', item);
				} else {
					// only one selection is allowed, so replace the entire array
					this.$emit('set-datacube-selected', item);
				}
			}
		},
		formatDescription(d: Datacube) {
			if (!d.description) return '';
			return this.isExpanded(d) || d.description.length < 140
				? d.description
				: `${d.description.substring(0, 140)}...`;
		},
		getTypeIcon(d: Datacube) {
			return `fa-regular ${
				d.type === 'model' ? 'fa-brands fa-connectdevelop' : 'fa-solid fa-table-cells'
			}`;
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
		height: 600px;
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
		.output-col {
			border-left: 4px solid $selected;
		}
		td {
			background-color: $tinted-background;
		}
	}
	.text-bold {
		font-size: $font-size-large;
		font-weight: 600;
		margin-bottom: 5px;
	}
	.output-col {
		width: 33%;
		.output-layout {
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
	.region-col {
		width: 200px;
	}
	.period-col {
		width: 120px;
	}
	// time series hidden until actually put into use
	.timeseries-col {
		padding-left: 5px;
		padding-right: 10px;
	}
	.timeseries-container {
		background-color: #f1f1f1;
		width: 110px;
		height: 50px;
	}
}
</style>
