<template>
	<div class="search-bar-container">
		<slot name="dataset"></slot>
		<div class="input-container">
			<IconSearch16 class="search-icon" />
			<label v-if="searchLabel !== ''" for="search" class="search-label">{{ searchLabel }}</label>
			<input
				id="search"
				v-model="searchText"
				ref="inputElement"
				type="text"
				name="search"
				:placeholder="searchPlaceholder"
				@keyup.enter="addSearchTerm"
				@input="searchTextHandler"
			/>
			<IconClose16 class="clear-icon" @click="clearText" />
		</div>
		<slot name="sort"></slot>
		<slot name="params"></slot>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import IconClose16 from '@carbon/icons-vue/es/close/16';
import IconSearch16 from '@carbon/icons-vue/es/search/16';

const props = defineProps({
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
		default: 'enter search term or doi here...'
	},
	focusInput: {
		type: Boolean,
		default: true
	}
});

const emit = defineEmits(['search-text-changed']);

const inputElement = ref<HTMLInputElement | null>(null);

const searchText = ref('');
const searchTerms = ref('');

const clearText = () => {
	searchText.value = '';
	searchTerms.value = '';
};

const searchTextHandler = (event: Event) => {
	if (props.realtime) {
		searchTerms.value = (event.target as HTMLInputElement).value;
	}
};

const execSearch = () => {
	emit('search-text-changed', searchTerms.value);
};

const addSearchTerm = (event: Event) => {
	if (!props.realtime) {
		const term = (event.target as HTMLInputElement).value;
		searchTerms.value = term;
		execSearch();
	}
};

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}
});

watch(searchTerms, () => {
	execSearch();
});
</script>

<style scoped>
.search-bar-container {
	display: flex;
	background-color: transparent;
	justify-content: center;
	color: white;
	flex: 1;
}

.search-label {
	font-weight: bold;
	padding: 8px;
}

.input-container {
	position: relative;
	margin-left: 1rem;
	margin-right: 1rem;
	flex: 1;
}

.input-container .search-icon {
	height: 100%;
	position: absolute;
	top: 0;
	bottom: 0;
	color: white;
	margin-left: 4px;
}

.input-container .clear-icon {
	height: 100%;
	position: absolute;
	top: 0;
	bottom: 0;
	right: 0;
	color: red;
	cursor: pointer;
	margin-right: 4px;
}

input[type='text'] {
	padding: 6px;
	border: none;
	font-size: 18px;
	padding-left: 2rem;
	padding-right: 2rem;
	outline: none;
	width: 100%;
}

input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
input:-webkit-autofill:active {
	transition: background-color 5000s ease-in-out 0s;
}
</style>
