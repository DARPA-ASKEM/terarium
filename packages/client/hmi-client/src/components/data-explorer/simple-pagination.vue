<template>
	<div class="data-pagination">
		<button type="button" class="btn btn-primary" :disabled="hasPrev" @click="prevPage">
			Previous
		</button>
		Viewing {{ currentStart }} to {{ currentFinish }}
		<button type="button" class="btn btn-primary" :disabled="hasNext" @click="nextPage">
			Next
		</button>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
	pageCount: {
		type: Number,
		default: 0
	},
	pageSize: {
		type: Number,
		default: 100
	},
	currentPageLength: {
		type: Number,
		default: 100
	}
});
const emit = defineEmits(['next-page', 'prev-page']);

const hasPrev = computed(() => props.pageCount === 0);
const hasNext = computed(() => props.pageSize > props.currentPageLength);
const currentStart = computed(() => {
	const offset = props.currentPageLength > 0 ? 1 : 0;
	return (props.pageCount * props.pageSize + offset).toLocaleString();
});
const currentFinish = computed(() =>
	(props.pageCount * props.pageSize + props.currentPageLength).toLocaleString()
);

const nextPage = () => {
	emit('next-page');
};
const prevPage = () => {
	emit('prev-page');
};
</script>

<style>
.data-pagination {
	padding: 8px;
	text-align: center;
	width: 100%;
}
</style>
