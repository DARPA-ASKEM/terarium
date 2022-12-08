<template>
	<div class="autocomplete-container" ref="containerElement">
		<label for="autocomplete"></label>
		<input
			ref="inputElement"
			id="autocomplete"
			v-model="searchTerm"
			type="text"
			autocomplete="off"
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

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';

const props = defineProps({
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
});

const emit = defineEmits(['item-selected']);
const inputStyle = computed(() => `--placeholder-color:${props.placeholderColor}`);
const suggestionsResultStyle = computed(
	() => `position: ${props.styleResults ? 'absolute' : 'relative'}`
);

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);

const searchTerm = ref('');
const suggestions = ref([]);
const selectedIndex = ref(-1);
const showSuggestions = ref(false);

const onArrowDown = () => {
	if (selectedIndex.value < suggestions.value.length - 1) {
		selectedIndex.value += 1;
	}
};

const onArrowUp = () => {
	if (selectedIndex.value > 0) {
		selectedIndex.value -= 1;
	}
};

const onChange = async () => {
	suggestions.value = await props.searchFn(searchTerm.value);
	if (suggestions.value.length > 0 && searchTerm.value !== '') {
		showSuggestions.value = true;
	}
};

const onEnter = () => {
	const suggestion = suggestions.value[selectedIndex.value];
	selectedIndex.value = -1;
	emit('item-selected', suggestion);
	showSuggestions.value = false;
	searchTerm.value = '';
};

const onClickOutside = (event: MouseEvent) => {
	// input just lost focus, but was that because the user clicked on one of the suggestions?
	if (event.target instanceof Element && containerElement.value?.contains(event.target)) {
		// Click was within this element, so do nothing
		return;
	}
	showSuggestions.value = false;
};

const setSearchTerm = (suggestion: string) => {
	searchTerm.value = '';
	emit('item-selected', suggestion);
	showSuggestions.value = false;
};

onMounted(() => {
	onChange();
	document.addEventListener('click', onClickOutside);

	if (props.focusInput) {
		inputElement.value?.focus();
	}
});

onUnmounted(() => {
	document.removeEventListener('click', onClickOutside);
});
</script>

<style scoped>
.autocomplete-container {
	position: relative;
	flex-grow: 1;
	height: auto;
}

.autocomplete-container .autocomplete-results-container {
	overflow-y: scroll;
	flex-grow: 1;
	width: 100%;
	padding-right: 10px;
	/* Overlap the button slightly */
	top: 85%;
	max-height: 220px;
	/* suggestion list on top of the other UI */
	z-index: 2;
	top: 100%;
}

.autocomplete-container .autocomplete-results {
	padding: 0;
	margin-left: 1rem;
	background-color: var(--background-light-1);
}

.autocomplete-results .is-active {
	background-color: var(--background-light-3);
}

.autocomplete-container :deep(.autocomplete-result) {
	list-style: none;
	text-align: left;
	word-break: break-all;
}
</style>
