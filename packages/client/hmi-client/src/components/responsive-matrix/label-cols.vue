<template>
	<div class="label-container col" :style="labelStyle">
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
		numCols: {
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
		selectedCols: {
			type: Array as PropType<CellStatus[]>,
			default() {
				return [];
			}
		},
		labelColList: {
			type: Array as PropType<number[] | string[]>,
			default() {
				return [];
			}
		},
		microColSettings: {
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
		this.getColLabelStyle();
		this.buildLabelData();
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	watch: {
		move() {
			this.getColLabelStyle();
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
			const viewportColDensity =
				((visibleBounds?.width || 0) / (this.viewport?.worldWidth || 1)) * this.numCols;

			const thresholdLabelDensity = 8;
			const labelStride = Math.max(1, Math.ceil(viewportColDensity / thresholdLabelDensity));

			this.labelPositions = [];
			this.labelValues = [];
			let pos = 0;
			for (let i = 0; i < this.selectedCols.length; i++) {
				const len = this.microColSettings[this.selectedCols[i]];

				if (!(i % labelStride)) {
					this.labelPositions.push(pos + len / 2);
					this.labelValues.push(this.labelColList[i]);
				}

				pos += len;
			}

			this.labelPositions = this.labelPositions.map((v) => (v / pos) * 100);
		},

		getLabelStyle(idx) {
			const position = this.labelPositions[idx];
			return {
				left: `${position}%`,
				right: `${100 - position}%`
			};
		},

		getColLabelStyle() {
			const topLeft = this.viewport?.toScreen(0, 0) || EMPTY_POINT;
			const topRight = this.viewport?.toScreen(this.viewport.worldWidth, 0) || EMPTY_POINT;

			this.labelStyle = {
				top: `${Math.max(topLeft.y, 0)}px`,
				left: `${topLeft.x}px`,
				right: `${topRight.x}px`,
				width: `${topRight.x - topLeft.x}px`,
				height: '30px'
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
	height: 100%;
	color: white;
	text-shadow: 0px 0px 4px #000;
}
</style>
