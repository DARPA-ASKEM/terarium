<template>
	<div class="label-container col" :style="labelContainerStyle">
		<div
			class="label"
			v-for="(label, idx) in labels"
			:key="idx"
			:title="label.alt"
			:style="getLabelStyle(idx)"
		>
			{{ label.value }}
		</div>
	</div>
</template>

<script lang="ts">
import { PropType } from 'vue';
import { NumberValue } from 'd3';
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
		margin: {
			type: Number,
			default() {
				return 0;
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
			type: Array as PropType<number[] | Date[]>,
			default() {
				return [];
			}
		},
		labelColAltList: {
			type: Array as PropType<string[]>,
			default() {
				return [];
			}
		},
		microColSettings: {
			type: Array as PropType<number[]>,
			default() {
				return [];
			}
		},
		labelColFormatFn: {
			type: Function as PropType<(value: NumberValue, index: number) => string>,
			default(v) {
				return v;
			}
		}
	},

	// ---------------------------------------------------------------------------- //
	// data                                                                         //
	// ---------------------------------------------------------------------------- //

	data() {
		return {
			labelContainerStyle: {},
			labels: [] as { value: string; alt: string; position: number }[]
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

			const thresholdLabelDensity = 16;
			const labelStride = Math.max(1, Math.ceil(viewportColDensity / thresholdLabelDensity));

			this.labels = makeLabels(
				this.labelColList,
				this.labelColAltList,
				this.selectedCols,
				this.microColSettings,
				labelStride,
				this.labelColFormatFn
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
				left: `${topLeft.x + this.margin}px`,
				right: `${topRight.x - this.margin}px`,
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
	/* pointer-events: none; */
	user-select: none;
}

.label {
	position: absolute;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 100%;
	color: var(--un-color-black-100);
}
</style>
