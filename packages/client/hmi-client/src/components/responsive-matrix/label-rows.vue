<template>
	<div class="label-container row" :style="labelContainerStyle">
		<div class="label" v-for="(label, idx) in labels" :key="idx" :style="getLabelStyle(idx)">
			{{ label.value }}
		</div>
	</div>
</template>

<script lang="ts">
import { PropType } from 'vue';

import { Viewport } from 'pixi-viewport';

import { CellStatus } from '@/types/ResponsiveMatrix';
import { viewport2Screen } from './pixi-utils';
import { makeLabels } from './matrix-util';

export default {
	// ---------------------------------------------------------------------------- //
	// props                                                                        //
	// ---------------------------------------------------------------------------- //

	props: {
		viewport: {
			type: Viewport as PropType<Viewport>,
			default() {
				return null;
			}
		},
		numRows: {
			type: Number,
			default() {
				return 0;
			}
		},
		update: {
			type: Number,
			default() {
				return 0;
			}
		},
		move: {
			type: Number,
			default() {
				return 0;
			}
		},
		selectedRows: {
			type: Array as PropType<CellStatus[]>,
			default() {
				return [];
			}
		},
		labelRowList: {
			type: Array as PropType<number[] | string[]>,
			default() {
				return [];
			}
		},
		microRowSettings: {
			type: Array as PropType<number[]>,
			default() {
				return [];
			}
		}
	},

	// ---------------------------------------------------------------------------- //
	// data                                                                         //
	// ---------------------------------------------------------------------------- //

	data() {
		return {
			labelContainerStyle: {},
			labels: [] as { value: string; position: number }[]
		};
	},

	// ---------------------------------------------------------------------------- //
	// mounted                                                                      //
	// ---------------------------------------------------------------------------- //

	mounted() {
		this.setLabelContainerStyle();
		this.buildLabelData();
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	watch: {
		move() {
			this.setLabelContainerStyle();
		},
		update() {
			this.buildLabelData();
		}
	},

	// ---------------------------------------------------------------------------- //
	// methods                                                                      //
	// ---------------------------------------------------------------------------- //

	methods: {
		buildLabelData() {
			const visibleBounds = this.viewport.getVisibleBounds();
			const viewportRowDensity =
				((visibleBounds?.height || 0) / (this.viewport.worldHeight || 1)) * this.numRows;

			const thresholdLabelDensity = 8;
			const labelStride = Math.max(1, Math.ceil(viewportRowDensity / thresholdLabelDensity));

			this.labels = makeLabels(
				this.labelRowList,
				this.selectedRows,
				this.microRowSettings,
				labelStride
			);
		},

		getLabelStyle(idx) {
			const label = this.labels[idx];
			return {
				top: `${label.position}%`,
				bottom: `${100 - label.position}%`
			};
		},

		setLabelContainerStyle() {
			if (!this.viewport) return;
			const { topLeft, bottomLeft } = viewport2Screen(this.viewport);

			this.labelContainerStyle = {
				left: `${Math.max(topLeft.x, 0)}px`,
				top: `${topLeft.y}px`,
				bottom: `${bottomLeft.y}px`,
				height: `${bottomLeft.y - topLeft.y}px`,
				width: '30px'
			};
		}
	}
};
</script>

<style scoped>
.label-container {
	position: absolute;
	pointer-events: none;
	user-select: none;
}

.label {
	position: absolute;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	color: white;
	text-shadow: 0px 0px 4px #000;
	transform: rotate(90deg);
}
</style>
