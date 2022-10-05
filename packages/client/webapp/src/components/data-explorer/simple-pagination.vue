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

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
	name: 'SimplePagination',
	props: {
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
	},
	emits: ['next-page', 'prev-page'],
	computed: {
		hasPrev(): boolean {
			return this.pageCount === 0;
		},
		hasNext(): boolean {
			return this.pageSize > this.currentPageLength;
		},
		currentStart(): string {
			const offset = this.currentPageLength > 0 ? 1 : 0;
			return (this.pageCount * this.pageSize + offset).toLocaleString();
		},
		currentFinish(): string {
			return (this.pageCount * this.pageSize + this.currentPageLength).toLocaleString();
		}
	},
	methods: {
		nextPage() {
			this.$emit('next-page');
		},
		prevPage() {
			this.$emit('prev-page');
		}
	}
});
</script>

<style>
.data-pagination {
	padding: 8px;
	text-align: center;
	width: 100%;
}
</style>
