<template>
	<div class="search-container">
		<datacubes-listview
			v-if="resultType === 'datacubes'"
			class="list-view"
			:datacubes="filteredDatacubes"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-datacube-selected="toggleDataItemSelected"
			@set-datacube-selected="setDataItemSelected"
		/>
		<articles-listview
			v-if="resultType === 'articles'"
			class="list-view"
			:articles="filteredArticles"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-article-selected="toggleDataItemSelected"
			@set-article-selected="setDataItemSelected"
		/>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import DatacubesListview from '@/components/data-explorer/datacubes-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import { Datacube } from '@/types/Datacube';
import { XDDArticle } from '@/types/XDD';

export default defineComponent({
	name: 'Search',
	components: {
		DatacubesListview,
		ArticlesListview
	},
	props: {
		filteredDataItems: {
			type: Array as PropType<(Datacube | XDDArticle)[]>,
			default: () => []
		},
		enableMultipleSelection: {
			type: Boolean,
			default: false
		},
		selectedSearchItems: {
			type: Array as PropType<string[]>,
			required: true
		}
	},
	emits: ['toggle-data-item-selected', 'set-data-item-selected'],
	computed: {
		resultType(): string {
			return this.filteredDataItems.length > 0 && this.filteredDataItems[0].outputs !== undefined
				? 'datacubes'
				: 'articles';
		},
		filteredDatacubes() {
			return this.filteredDataItems as Datacube[];
		},
		filteredArticles() {
			return this.filteredDataItems as XDDArticle[];
		}
	},
	methods: {
		toggleDataItemSelected(item: string) {
			this.$emit('toggle-data-item-selected', item);
		},
		setDataItemSelected(item: string) {
			this.$emit('set-data-item-selected', item);
		}
	}
});
</script>

<style lang="scss" scoped>
.search-container {
	min-height: 0px;
	width: 100%;
	display: flex;
	flex-direction: column;
	padding-right: 10px;
	gap: 5px;

	.list-view {
		flex: 1;
		min-height: 0;
	}
}
</style>
