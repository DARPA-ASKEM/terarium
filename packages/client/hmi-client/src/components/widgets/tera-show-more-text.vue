<template>
	<div class="container">
		<div
			ref="textContainerRef"
			v-bind:class="{ default: !expanded, expanded: expanded }"
			v-bind:style="`--lines: ${lines}`"
		>
			<div v-html="text"></div>
		</div>
		<a class="anchor" @click="collapseOrExpand">
			<template v-if="triggerShowMore">Show more</template>
			<template v-if="expanded">Show less</template>
		</a>
	</div>
</template>

<script setup lang="ts">
import { onMounted, onUpdated, ref, nextTick } from 'vue';

defineProps<{
	lines?: number;
	text?: string;
}>();

const expanded = ref(false);
const triggerShowMore = ref(false);
const textContainerRef = ref();

function determineShowMore() {
	triggerShowMore.value = !!(
		textContainerRef.value &&
		textContainerRef.value.offsetHeight < textContainerRef.value.scrollHeight
	);
}

function collapseOrExpand() {
	expanded.value = !expanded.value;
}
onMounted(async () => {
	await nextTick();
	determineShowMore();
});

onUpdated(async () => {
	await nextTick();
	determineShowMore();
});
</script>
<style scoped>
.default {
	--lines: 9999;
	white-space: pre-line;
	display: -webkit-box;
	max-width: 100%;
	-webkit-line-clamp: var(--lines);
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
}

.expanded {
	display: block;
	display: -webkit-box;
	white-space: pre-line;
}

.anchor {
	display: block;
	text-align: right;
	padding: 8px;
	margin-bottom: 4px;
	color: var(--primary-color);
	cursor: pointer;
}
</style>
