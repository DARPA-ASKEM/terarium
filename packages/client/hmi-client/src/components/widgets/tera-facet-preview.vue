<template>
	<ul>
		<li v-for="(facet, index) in facets" :key="index">
			<div class="bar">
				<div
					class="fill"
					:class="{ 'above-75': facet.value >= 75 }"
					:style="{ width: `${facet.value}%` }"
				></div>
			</div>
			<label>
				<span>{{ facet.key }}</span>
				<span>{{ facet.value }}%</span>
			</label>
		</li>
	</ul>
</template>

<script setup lang="ts">
defineProps<{
	facets: { key: string; value: number }[];
}>();
</script>

<style scoped>
ul {
	font-size: var(--font-caption);
	list-style: none;
	margin: 0 0.5rem;
}

.bar {
	width: 100%;
	border-radius: var(--border-radius);
	margin: 0.5rem 0 3px 0;
	background-color: #eaebec; /* Color copied from facet package*/
}

.fill {
	height: 0.25rem;
	border-bottom-left-radius: var(--border-radius);
	border-top-left-radius: var(--border-radius);
	background-color: var(--gray-400);
	width: 0;
}

.above-75 {
	background-color: var(--primary-color);
}

label {
	display: flex;
	justify-content: space-between;
}
</style>
