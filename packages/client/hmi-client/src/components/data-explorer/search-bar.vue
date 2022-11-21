<template>
	<div class="search-bar-container">
		<slot name="dataset"></slot>
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
		<button
			v-if="enableClearButton"
			type="button"
			class="search-and-clear-buttons button-padding"
			:class="{ 'button-disabled': isClearButtonDisabled }"
			:disabled="isClearButtonDisabled"
			@click="clearText"
		>
			<IconClose16 />Clear
		</button>
		<button
			v-if="enableSearchButton"
			type="button"
			class="search-and-clear-buttons button-padding"
			:class="{ 'button-disabled': isSearchButtonDisabled }"
			:disabled="isSearchButtonDisabled"
			@click="searchBtnHandler"
		>
			<IconSearch16 />Search
		</button>
		<slot name="sort"></slot>
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
		searchLabel: {
			type: String,
			default: ''
		},
		searchPlaceholder: {
			type: String,
			default: 'search term here...'
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
		const searchTerms = ref('');
		return {
			searchText,
			searchTerms
		};
	},
	computed: {
		isClearButtonDisabled() {
			return this.searchText === '' && this.searchTerms === '';
		},
		isSearchButtonDisabled() {
			return this.searchText === '' && this.searchTerms === '';
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
			this.searchTerms = '';
		},
		searchTextHandler(event: Event) {
			if (this.realtime) {
				this.searchTerms = (event.target as HTMLInputElement).value;
			}
		},
		addSearchTerm(event: Event) {
			if (!this.realtime) {
				const term = (event.target as HTMLInputElement).value;
				this.searchTerms = term;
				this.execSearch();
			}
		},
		searchBtnHandler() {
			this.searchTerms = this.searchText;
			this.execSearch();
		},
		execSearch() {
			this.$emit('search-text-changed', this.searchTerms);
		}
	}
});
</script>

<style scoped>
.search-bar-container {
	display: flex;
	background-color: transparent;
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

button {
	border-width: thin;
	border-color: white;
	border-radius: 6px;
}

.search-and-clear-buttons {
	color: var(--un-color-white);
}

.search-button {
	background-color: var(--un-color-accent);
}

.search-button-disabled,
.button-disabled {
	background-color: var(--un-color-accent-dark);
	cursor: not-allowed;
	color: gray;
}

.clear-button-disabled {
	background-color: gray;
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
}

.flex-aligned-item-delete-btn {
	color: red;
}

.flex-aligned-item-delete-btn:hover {
	cursor: pointer;
}

.search-label {
	color: white;
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

.search-and-clear-buttons {
	color: white;
}
</style>
