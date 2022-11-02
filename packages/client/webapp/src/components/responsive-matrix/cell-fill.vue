<template>
	<div class="cell cell-fill" :style="respCellFillStyle"></div>
</template>

<script lang="ts">
	export default {

		// ---------------------------------------------------------------------------- //
		// props                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		props: {
			cellData: {
				type: null,
				default() {
					return {};
				},
			},
			parameters: {
				type: Array,
				default() {
					return [];
				},
			},
			parametersMin: {
				type: Object,
				default() {
					return {};
				},
			},
			parametersMax: {
				type: Object,
				default() {
					return {};
				},
			},
			colorFn: {
				type: Function,
				default() {
					return '#FF00FF';
				},
			},
		},



		// ---------------------------------------------------------------------------- //
		// computed                                                                     //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		computed: {
			respCellFillStyle(): any {
				return {
					backgroundColor: this.colorFn(
						this.cellData,
						this.parametersMin,
						this.parametersMax,
						this.parameters,
					),
				};
			},
		},
	}
</script>