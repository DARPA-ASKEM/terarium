<template>
	<div class="autocomplete-container" ref="containerElement">
		<label for="autocomplete"></label>
		<input
			ref="inputElement"
			id="autocomplete"
			v-model="searchTerm"
			type="text"
			class="form-control"
			:style="inputStyle"
			:placeholder="placeholderMessage"
			@input="onChange"
			@keydown.down="onArrowDown"
			@keydown.up="onArrowUp"
			@keydown.enter="onEnter"
		/>
		<div
			v-if="showSuggestions"
			class="autocomplete-results-container"
			:style="suggestionsResultStyle"
		>
			<ul class="autocomplete-results">
				<li
					v-for="(suggestion, i) in suggestions"
					:key="i"
					class="autocomplete-result dropdown-option"
					:class="{ 'is-active': i === selectedIndex || selectedFn(suggestion) }"
					@click.stop.prevent="setSearchTerm(suggestion)"
				>
					<div>
						{{ suggestion }}
					</div>
				</li>
			</ul>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
	name: 'AutoComplete',
	props: {
		placeholderMessage: {
			type: String,
			required: false,
			default: () => ''
		},
		focusInput: {
			type: Boolean,
			default: false
		},
		styleResults: {
			type: Boolean,
			default: false
		},
		placeholderColor: {
			type: String,
			default: 'black'
		},
		searchFn: {
			type: Function,
			required: true
		},
		selectedFn: {
			type: Function,
			default: () => false
		}
	},
	emits: ['item-selected'],
	computed: {
		inputStyle(): string {
			return `--placeholder-color:${this.placeholderColor}`;
		},
		suggestionsResultStyle(): string {
			return `position: ${this.styleResults ? 'absolute' : 'relative'}`;
		}
	},
	data: () => ({
		searchTerm: '',
		suggestions: [],
		selectedIndex: -1,
		showSuggestions: false
	}),
	mounted() {
		this.onChange();
		document.addEventListener('click', this.onClickOutside);

		if (this.focusInput) {
			(this.$refs.inputElement as HTMLInputElement).focus();
		}
	},
	unmounted() {
		document.removeEventListener('click', this.onClickOutside);
	},
	methods: {
		onArrowDown() {
			if (this.selectedIndex < this.suggestions.length - 1) {
				this.selectedIndex += 1;
			}
		},
		onArrowUp() {
			if (this.selectedIndex > 0) {
				this.selectedIndex -= 1;
			}
		},
		async onChange() {
			this.suggestions = await this.searchFn(this.searchTerm);
			if (this.suggestions.length > 0 && this.searchTerm !== '') {
				this.showSuggestions = true;
			}
		},
		onEnter() {
			const suggestion = this.suggestions[this.selectedIndex];
			this.selectedIndex = -1;
			this.$emit('item-selected', suggestion);
			this.showSuggestions = false;
			this.searchTerm = '';
		},
		onClickOutside(event: MouseEvent) {
			const containerElement = this.$refs.containerElement as HTMLElement;
			// input just lost focus, but was that because the user clicked on one of the suggestions?
			if (event.target instanceof Element && containerElement.contains(event.target)) {
				// Click was within this element, so do nothing
				return;
			}
			this.showSuggestions = false;
		},
		setSearchTerm(suggestion: string) {
			this.searchTerm = '';
			this.$emit('item-selected', suggestion);
			this.showSuggestions = false;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';

$input-element-height: 37px;

.autocomplete-container {
	position: relative;
	flex-grow: 1;
	height: auto;

	.autocomplete-results-container {
		overflow-y: scroll;
		flex-grow: 1;
		width: 100%;
		padding-right: 10px;
		top: 85%; // Overlap the button slightly
		max-height: 220px;
		z-index: 2; // suggestion list on top of the other UI
	}

	.autocomplete-results {
		padding: 0;
		margin-left: 1rem;
		background-color: var(--background-light-1);

		.is-active {
			background-color: var(--background-light-3);
		}
	}

	:deep(.autocomplete-result) {
		list-style: none;
		text-align: left;
		word-break: break-all;
	}
}

.form-control {
	width: calc(100% - 20px); // 20px = 2*margin
	margin-left: 10px;
	margin-right: 10px;
	padding: 0 10px;
	--placeholder-color: black;
}

.form-control::placeholder {
	color: var(--placeholder-color);
}
</style>
