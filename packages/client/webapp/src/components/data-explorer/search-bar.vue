<template>
	<div class="search-bar-container">
		<div>
			<label v-if="searchLabel !== ''" for="search" class="search-label">{{ searchLabel }}</label>
			<input
				id="search"
				v-model="searchText"
				type="text"
				name="search"
				:placeholder="searchPlaceholder"
				@keyup.enter="addSearchTerm"
				@input="searchTextHandler"
			/>
		</div>
		<div v-if="!realtime" class="flex-aligned" style="margin-bottom: 5px">
			<div v-for="searchTerm in searchTerms" :key="searchTerm" class="flex-aligned-item">
				{{ searchTerm }}
				<span class="flex-aligned-item-delete-btn" @click.stop="removeSearchTerm(searchTerm)">
					<i class="fa fa-fw fa-close" />
				</span>
			</div>
		</div>
		<button
			v-if="enableClearButton"
			type="button"
			class="btn clear-button"
			:class="{ 'clear-button-disabled': isClearButtonDisabled }"
			:disabled="isClearButtonDisabled"
			@click="clearText"
		>
			<i class="fa fa-remove" />&nbsp;Clear
		</button>
		<button
			v-if="enableSearchButton"
			type="button"
			class="btn clear-button search-button"
			:class="{ 'search-button-disabled': isSearchButtonDisabled }"
			:disabled="isSearchButtonDisabled"
			@click="searchBtnHandler"
		>
			<i class="fa fa-search" />&nbsp;Search
		</button>
		<slot v-if="showSortedResults" name="sort"></slot>
	</div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
	name: 'SearchBar',
	props: {
		realtime: {
			type: Boolean,
			default: false
		},
		enableMultiTermSearch: {
			type: Boolean,
			default: false
		},
		searchLabel: {
			type: String,
			default: 'Search'
		},
		searchPlaceholder: {
			type: String,
			default: 'search text here...'
		},
		enableClearButton: {
			type: Boolean,
			default: true
		},
		enableSearchButton: {
			type: Boolean,
			default: true
		},
		showSortedResults: {
			type: Boolean,
			default: false
		}
	},
	emits: ['search-text-changed'],
	setup() {
		const searchText = ref('');
		const searchTerms = ref<string[]>([]);
		return {
			searchText,
			searchTerms
		};
	},
	computed: {
		isClearButtonDisabled() {
			return this.searchText === '' && this.searchTerms.length === 0;
		},
		isSearchButtonDisabled() {
			return this.searchText === '' && this.searchTerms.length === 0;
		}
	},
	watch: {
		searchTerms() {
			this.execSearch();
		}
	},
	methods: {
		clearText() {
			this.searchText = '';
			if (this.searchTerms.length > 0) {
				this.searchTerms = [];
			}
		},
		searchTextHandler(event: Event) {
			if (this.realtime) {
				this.searchTerms = [(event.target as HTMLInputElement).value];
			}
		},
		removeSearchTerm(term: string) {
			this.searchTerms = this.searchTerms.filter((t) => t !== term);
			this.searchText = '';
		},
		addSearchTerm(event: Event) {
			if (!this.realtime) {
				const term = (event.target as HTMLInputElement).value;
				this.searchTerms = this.enableMultiTermSearch ? [...this.searchTerms, term] : [term];
			}
		},
		searchBtnHandler() {
			if (this.searchTerms.length === 0) {
				this.searchTerms = [this.searchText];
			} else {
				this.execSearch();
			}
		},
		execSearch() {
			this.$emit('search-text-changed', this.searchTerms);
		}
	}
});
</script>

<style lang="scss" scoped>
.search-bar-container {
	display: flex;
	background-color: darkgray;
	align-items: baseline;
	justify-content: center;
}

.clear-button {
	color: white;
	padding: 4px;
	padding-left: 8px;
	padding-right: 8px;
	margin: 4px;
}

.search-button {
	background-color: dodgerblue;
}
.search-button-disabled {
	background-color: darken($color: dodgerblue, $amount: 25%);
	cursor: not-allowed;
}
.clear-button-disabled {
	background-color: darken($color: white, $amount: 50%);
	cursor: not-allowed;
}

.flex-aligned {
	display: flex;
	align-items: baseline;
}

.flex-aligned-item {
	display: flex;
	align-items: center;
	color: blue;

	.flex-aligned-item-delete-btn {
		color: red;
	}
	.flex-aligned-item-delete-btn:hover {
		cursor: pointer;
	}
}

.search-label {
	color: black;
	font-weight: bold;
	padding: 8px;
}

input[type='text'] {
	padding: 6px;
	border: none;
	margin-top: 8px;
	margin-right: 16px;
	font-size: 17px;
	min-width: 300px;
}
</style>
