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
					<IconClose16 />
				</span>
			</div>
		</div>
		<button
			v-if="enableClearButton"
			type="button"
			class="btn clear-button button-padding"
			:class="{ 'clear-button-disabled': isClearButtonDisabled }"
			:disabled="isClearButtonDisabled"
			@click="clearText"
		>
			<IconClose16 />Clear
		</button>
		<button
			v-if="enableSearchButton"
			type="button"
			class="btn button-padding search-button"
			:class="{ 'search-button-disabled': isSearchButtonDisabled }"
			:disabled="isSearchButtonDisabled"
			@click="searchBtnHandler"
		>
			<IconSearch16 />Search
		</button>
		<slot v-if="showSortedResults" name="sort"></slot>
	</div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import IconClose16 from '@carbon/icons-vue/es/close/16';
import IconSearch16 from '@carbon/icons-vue/es/search/16';

export default defineComponent({
	name: 'SearchBar',
	components: {
		IconClose16,
		IconSearch16
	},
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

.button-padding {
	padding: 4px;
	padding-left: 8px;
	padding-right: 8px;
	margin: 4px;
	cursor: pointer;
}

.clear-button {
	color: white;
}

.search-button {
	background-color: #2d8e2dff;
}

.search-button-disabled {
	background-color: darken($color: #92e192ff, $amount: 50%);
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
	color: var(--un-color-accent-darker);

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
