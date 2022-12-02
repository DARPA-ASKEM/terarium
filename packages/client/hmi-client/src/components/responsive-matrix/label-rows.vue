<template>
	<div class="label-container row" :style="labelStyle">
		<div class="label" v-for="(value, idx) in labelValues" :key="idx" :style="getLabelStyle(idx)">
			{{ value }}
		</div>
	</div>
</template>

<script lang="ts">
import { PropType } from 'vue';

import { Point } from 'pixi.js';

import { CellStatus } from '@/types/ResponsiveMatrix';

// EMPTY_POINT used as a fallback value
const EMPTY_POINT = new Point();

export default {
	// ---------------------------------------------------------------------------- //
	// props                                                                        //
	// ---------------------------------------------------------------------------- //

	props: {
		viewport: {
			type: null,
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
			labelStyle: {},
			labelPositions: [] as number[],
			labelValues: [] as any[]
		};
	},

	// ---------------------------------------------------------------------------- //
	// mounted                                                                      //
	// ---------------------------------------------------------------------------- //

	mounted() {
		this.getRowLabelStyle();
		this.buildLabelData();
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	watch: {
		move() {
			this.getRowLabelStyle();
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
			const visibleBounds = this.viewport?.getVisibleBounds();
			const viewportRowDensity =
				((visibleBounds?.height || 0) / (this.viewport?.worldHeight || 1)) * this.numRows;

			const thresholdLabelDensity = 8;
			const labelStride = Math.max(1, Math.ceil(viewportRowDensity / thresholdLabelDensity));

			this.labelPositions = [];
			this.labelValues = [];
			let pos = 0;
			for (let i = 0; i < this.selectedRows.length; i++) {
				const len = this.microRowSettings[this.selectedRows[i]];

				if (!(i % labelStride)) {
					this.labelPositions.push(pos + len / 2);
					this.labelValues.push(this.labelRowList[i]);
				}

				pos += len;
			}

			this.labelPositions = this.labelPositions.map((v) => (v / pos) * 100);
		},

		getLabelStyle(idx) {
			const position = this.labelPositions[idx];
			return {
				top: `${position}%`,
				bottom: `${100 - position}%`
			};
		},

		getRowLabelStyle() {
			const topLeft = this.viewport?.toScreen(0, 0) || EMPTY_POINT;
			const bottomLeft = this.viewport?.toScreen(0, this.viewport.worldHeight) || EMPTY_POINT;

			this.labelStyle = {
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
