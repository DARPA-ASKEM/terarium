<template>
	<section>
		<!-- Top line -->
		<header class="subdued">
			<span>{{ stats.type.toUpperCase() }}</span>
			<span v-if="stats.type === 'numeric'">STD: {{ displayNumber(stats.std.toString()) }}</span>
			<span v-if="stats.type === 'categorical'">{{ stats.num_unique_entries }} unique</span>
			<span>{{ stats.num_null_entries }} nulls</span>
		</header>
		<!-- This draws a box-plot with the stats -->
		<figure v-if="stats.type === 'numeric'" class="tnum">
			<span>{{ displayNumber(stats.min.toString()) }}</span>
			<div class="graph">
				<span class="line" :style="getBoxplotPartialWidth(stats.quantile_25, stats.max)" />
				<span class="box-left" :style="getBoxplotPartialWidth(stats.quantile_50 - stats.quantile_25, stats.max)" />
				<span class="box-middle">
					<span class="centered-text below">{{ displayNumber(stats.mean.toString()) }}</span>
				</span>
				<span class="box-right" :style="getBoxplotPartialWidth(stats.quantile_75 - stats.quantile_25, stats.max)" />
				<span class="line" :style="getBoxplotPartialWidth(stats.max - stats.quantile_75, stats.max)" />
			</div>
			<span>{{ displayNumber(stats.max.toString()) }}</span>
		</figure>
		<!-- This draws a list of the most common entries, sorted by value then by name -->
		<figure v-if="stats.type === 'categorical'" class="flex-wrap">
			<span class="white-space-nowrap" v-for="(entry, index) in sortedMostCommonEntries" :key="index">
				{{ entry[0] }}<span class="subdued ml-1">({{ entry[1] }})</span
				>{{ index !== Object.entries(stats.most_common_entries).length - 1 ? ',' : '' }}&nbsp;
			</span>
		</figure>
	</section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { displayNumber } from '@/utils/number';

const props = defineProps<{
	stats: {
		type: string;
		min: number;
		max: number;
		quantile_25: number;
		quantile_50: number;
		quantile_75: number;
		mean: number;
		std: number;
		num_unique_entries: number;
		num_null_entries: number;
		most_common_entries: Record<string, number>;
	};
}>();

// list of the most common entries, sorted by value then by name
const sortedMostCommonEntries = computed(() =>
	Object.entries(props.stats.most_common_entries).sort((a, b) => {
		if (a[1] < b[1]) return -1;
		if (a[1] > b[1]) return 1;
		if (a[0] < b[0]) return -1;
		if (a[0] > b[0]) return 1;
		return 0;
	})
);

function getBoxplotPartialWidth(n1: number, n2: number) {
	if (n2 === 0) return `width: 1%;`; // Prevent division by zero (n2 can be 0 if all values are the same)
	return `width: ${(n1 / n2) * 100}%;`;
}
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	font-size: var(--font-caption);
	text-wrap: nowrap;
}

header,
figure {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	gap: var(--gap-1);
}

.subdued {
	color: var(--text-color-subdued);
}

.line {
	border-top: 2px solid var(--text-color-subdued);
}

.tnum {
	font-feature-settings: 'tnum';
}

.graph {
	width: 100%;
	display: flex;
	align-items: center;
}

.box-left {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	border-left: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	height: 18px;
}
.box-middle {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	width: 2px;
	height: 18px;
	text-align: center;
	position: relative;
}

.box-middle:before {
	content: '';
	position: absolute;
	top: 50%;
	left: 50%;
	width: 2px;
	height: 100%;
	background-color: var(--text-color-subdued);
	transform: translate(-50%, -50%);
}

.box-middle .centered-text {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, 65%);
	white-space: nowrap;
	color: var(--text-color-subdued);
}

.box-right {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	border-right: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	height: 18px;
}
</style>
