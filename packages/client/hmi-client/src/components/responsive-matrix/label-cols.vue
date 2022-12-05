<template>
	<div class="label-container col" :style="labelContainerStyle">
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
			const viewportColDensity =
				((visibleBounds?.width || 0) / (this.viewport.worldWidth || 1)) * this.numCols;

			const thresholdLabelDensity = 8;
			const labelStride = Math.max(1, Math.ceil(viewportColDensity / thresholdLabelDensity));

			this.labels = makeLabels(
				this.labelColList,
				this.selectedCols,
				this.microColSettings,
				labelStride
			);
		},

		getLabelStyle(idx) {
			const label = this.labels[idx];
			return {
				left: `${label.position}%`,
				right: `${100 - label.position}%`
			};
		},

		setLabelContainerStyle() {
			if (!this.viewport) return;
			const { topLeft, topRight } = viewport2Screen(this.viewport);

			this.labelContainerStyle = {
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
