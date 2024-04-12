<template>
	<section>
		<section class="matrix">
			<ul>
				<li v-for="(row, rowIdx) in matrix" :key="rowIdx">
					{{ row[0].rowCriteria }}
				</li>
			</ul>
			<div class="grid">
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
import { ref, onMounted, computed } from 'vue';
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
onMounted(() => {
	const templatesMap = collapseTemplates(props.mmt).matrixMap;
	const transitionMatrix = templatesMap.get(props.id);
	if (!transitionMatrix) {
		logger.error('Failed to generate transition matrix');
		return;
	}
	matrix.value = extractTemplateMatrix(transitionMatrix).matrix;
});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	align-items: center;

	& > .matrix {
		display: flex;
		flex-direction: row;
	}

	& > .grid {
		display: grid;
		grid-template-columns: repeat(v-bind(matrixColLen), 2rem);

		& > .cell {
			border: 1px solid black;
			text-align: center;
			height: 2rem;
		}

		& > .filled-cell {
			background-color: var(--primary-color);
			color: white;
		}
	}
}

span {
	color: var(--primary-color);
	padding-top: var(--gap-small);
}
</style>
