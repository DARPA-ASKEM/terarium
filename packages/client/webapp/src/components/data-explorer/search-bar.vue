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
		<button v-if="enableClearButton" type="button" class="btn clear-button" @click="clearText">
			<i class="fa fa-remove" />&nbsp;Clear
		</button>
		<button
			v-if="enableSearchButton"
			type="button"
			class="btn clear-button search-button"
			@click="execSearch"
		>
			<i class="fa fa-search" />&nbsp;Search
		</button>
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
	watch: {
		searchTerms() {
			this.execSearch();
		}
	},
	methods: {
		clearText() {
			this.searchText = '';
			this.searchTerms = [];
		},
		searchTextHandler(event) {
			if (this.realtime) {
				this.searchTerms = [event.target.value];
			}
		},
		removeSearchTerm(term: string) {
			this.searchTerms = this.searchTerms.filter((t) => t !== term);
		},
		addSearchTerm(event) {
			if (!this.realtime) {
				const term = event.target.value;
				this.searchTerms = this.enableMultiTermSearch ? [...this.searchTerms, term] : [term];
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
