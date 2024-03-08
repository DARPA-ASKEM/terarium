<template>
	<section>
		<!-- Top line -->
		<div class="flex flex-row justify-content-between mb-2">
			<div class="caption subdued">{{ stats.type.toUpperCase() }}</div>
			<div v-if="stats.type === 'numeric'" class="caption subdued">
				STD: {{ stats.std.toFixed(3) }}
			</div>
			<div v-if="stats.type === 'categorical'" class="caption subdued">
				{{ stats.num_unique_entries }} unique
			</div>
			<div class="caption subdued">{{ stats.num_null_entries }} nulls</div>
		</div>
		<!-- This draws a box-plot with the stats -->
		<div v-if="stats.type === 'numeric'" class="flex flex-row align-items-center tnum mb-1">
			<div class="caption mr-1">{{ stats.min }}</div>
			<div class="line" :style="getBoxplotPartialWidth(stats.quantile_25, stats.max)" />
			<div
				class="box-left"
				:style="getBoxplotPartialWidth(stats.quantile_50 - stats.quantile_25, stats.max)"
			/>
			<div class="caption box-middle">
				<div class="centered-text below">{{ stats.mean.toFixed(2) }}</div>
			</div>
			<div
				class="box-right"
				:style="getBoxplotPartialWidth(stats.quantile_75 - stats.quantile_25, stats.max)"
			></div>
			<div
				class="line"
				:style="getBoxplotPartialWidth(stats.max - stats.quantile_75, stats.max)"
			></div>
			<div class="caption ml-1">{{ stats.max }}</div>
		</div>

		<!-- This draws a list of the most common entries, sorted by value then by name -->
		<div v-if="stats.type === 'categorical'">
			<div class="flex flex-row flex-wrap">
				<span
					class="white-space-nowrap"
					v-for="(entry, index) in Object.entries(stats.most_common_entries).sort((a, b) => {
						if (a[1] < b[1]) return -1;
						if (a[1] > b[1]) return 1;
						if (a[0] < b[0]) return -1;
						if (a[0] > b[0]) return 1;
						return 0;
					})"
					:key="index"
				>
					{{ entry[0] }}<span class="caption subdued ml-1">({{ entry[1] }})</span
					>{{ index !== Object.entries(stats.most_common_entries).length - 1 ? ',' : '' }}&nbsp;
				</span>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
defineProps<{
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

function getBoxplotPartialWidth(n1: number, n2: number) {
	if (n2 === 0) return `width: 1%;`; // Prevent division by zero (n2 can be 0 if all values are the same)
	return `width: ${(n1 / n2) * 100}%;`;
}
</script>

<style scoped>
.caption {
	font-size: var(--font-caption);
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
