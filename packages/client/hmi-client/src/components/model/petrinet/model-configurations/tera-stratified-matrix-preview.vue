<template>
	<section>
		<section class="matrix">
			<div></div>
			<div class="col-labels">
				<label v-for="(col, colIdx) in matrix[0]" :key="colIdx">
					{{ col.colCriteria }}
				</label>
			</div>
			<div class="row-labels">
				<label v-for="(row, rowIdx) in matrix" :key="rowIdx">
					{{ row[0].rowCriteria }}
				</label>
			</div>
			<div class="matrix-grid">
				<template v-for="row in matrix">
					<div
						class="cell"
						:class="{ 'filled-cell': cell.content.id }"
						v-for="(cell, colIdx) in row"
						:key="colIdx"
					></div>
				</template>
			</div>
		</section>
		<span>Click to open</span>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { StratifiedMatrix } from '@/types/Model';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { extractTemplateMatrix } from '@/model-representation/mira/mira-util';
import { collapseTemplates } from '@/model-representation/mira/mira';
import { logger } from '@/utils/logger';

const props = defineProps<{
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	id: string;
	stratifiedMatrixType: StratifiedMatrix;
}>();

const matrix = ref<any>([]);

const matrixColLen = computed(() => matrix.value[0]?.length ?? 0);

// Currently only supports transition/rate matrices
watch(
	() => props.id,
	() => {
		const templatesMap = collapseTemplates(props.mmt).matrixMap;
		const transitionMatrix = templatesMap.get(props.id);
		if (!transitionMatrix) {
			logger.error('Failed to generate transition matrix');
			return;
		}
		matrix.value = extractTemplateMatrix(transitionMatrix).matrix;
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: var(--gap-small);

	& > .matrix {
		display: grid;
		grid-template-columns: auto auto;
		gap: var(--gap-small);
	}

	& > .matrix-grid {
		display: grid;
		grid-template-columns: repeat(v-bind(matrixColLen), 2rem);
		border: 1px solid black;

		& > .cell {
			border: 1px solid black;
			text-align: center;
			height: 2rem;
		}

		& > .filled-cell {
			background-color: rgb(134, 146, 164);
			color: var(--surface-section);
		}
	}
}

.row-labels,
.col-labels {
	display: flex;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	& > label {
		display: flex;
		align-items: center;
	}
}

.row-labels {
	flex-direction: column;
	& > label {
		justify-content: end;
		height: 2rem;
	}
}

.col-labels > label {
	writing-mode: vertical-lr;
	transform: scale(-1);
	width: 2rem;
}

span {
	font-size: var(--font-caption);
	color: var(--primary-color);
	padding: var(--gap-small) 0;
}
</style>
