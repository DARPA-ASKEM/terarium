<script setup lang="ts">
import { toRefs, ref } from 'vue';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import Button from 'primevue/button';
import debounce from '@/utils/debounce-util';

interface SearchBarProps {
	/** Pass in the string ref that contains this search bar's state. */
	queryString: string;
	shouldShowSearchIcon?: boolean;
	/**
	 * (recommended)
	 * Whether or not search events will be triggered as the user types.
	 * Defaults to true.
	 */
	shouldSearchOnKeyup?: boolean;
	/**
	 * The number of milliseconds to wait after a keypress before emitting the search event.
	 * Reduces the number of discarded search responses when the user types quickly.
	 * Has no effect when `shouldSearchOnKeyup` is false.
	 * Defaults to 500ms.
	 */
	debounceDuration?: number;
	/** Set to true if `shouldSearchOnKeyup` is false or if you also want a visible search bar. */
	shouldShowSearchButton?: boolean;
	/** Optional. Defaults to "Search" */
	placeholderText?: string;
	shouldShowClearButton?: boolean;
	size?: 'default' | 'small';
}
const props = withDefaults(defineProps<SearchBarProps>(), {
	shouldSearchOnKeyup: true,
	shouldShowSearchIcon: true,
	shouldShowClearButton: false,
	debounceDuration: 500,
	placeholderText: 'Search',
	size: 'default'
});

const displayClearIcon = ref(false);

const { queryString } = toRefs(props);

const emit = defineEmits<{
	(e: 'search', queryString: string): void;
	(e: 'update:queryString', newValue: string): void;
}>();
const isWaitingToTriggerSearch = ref(false);
const triggerSearch = () => {
	isWaitingToTriggerSearch.value = false;
	emit('search', queryString.value);
};

const debouncedTriggerSearch = debounce(triggerSearch, props.debounceDuration);

const updateQueryString = (newValue: string | undefined) => {
	const newValueString = newValue ?? '';
	emit('update:queryString', newValueString);
	if (newValue === '') {
		triggerSearch();
		displayClearIcon.value = false;
	} else if (props.shouldSearchOnKeyup) {
		isWaitingToTriggerSearch.value = true;
		debouncedTriggerSearch();
	}

	if (newValue !== '') {
		displayClearIcon.value = true;
	}
};

const onKeyUp = (event: KeyboardEvent) => {
	if (event.key === 'Enter') {
		triggerSearch();
	}
};
</script>

<template>
	<div class="search-bar-container" :class="{ 'size-small': size === 'small' }">
		<IconField class="text-input">
			<InputIcon v-if="shouldShowSearchIcon" class="fa fa-search search-icon" />
			<InputText
				:modelValue="queryString"
				@update:modelValue="updateQueryString"
				@keyup="onKeyUp"
				:placeholder="placeholderText"
				class="input-element"
				:class="{ 'add-left-padding': shouldShowSearchIcon }"
			/>
			<InputIcon v-if="!isWaitingToTriggerSearch && displayClearIcon" class="clear-icon" @click="updateQueryString('')">
				<template #default>
					<FontAwesomeIcon icon="fa fa-xmark" />
				</template>
			</InputIcon>
			<InputIcon v-if="isWaitingToTriggerSearch">
				<template #default>
					<i class="pi pi-spin pi-spinner"></i>
				</template>
			</InputIcon>
		</IconField>

		<Button v-if="shouldShowSearchButton" label="Search" @click="triggerSearch" />
	</div>
</template>

<style scoped>
.search-bar-container {
	display: flex;
	gap: 0.125rem;
}

.search-icon {
	left: 0.75rem;
	height: var(--text-size);
	color: var(--text-color-secondary);
}

.text-input {
	flex: 1;
	min-width: 0;
}

.input-element {
	padding: 0.625rem 1rem;
}

.size-small .input-element {
	padding-top: 0.325rem;
	padding-bottom: 0.325rem;
	font-size: small;
	border-radius: 3px;
	background: var(--surface-b);
}

.add-left-padding {
	/*
	Copied from .p-icon-field > .p-input-icon, since IconField doesn't support icons on both sides.
	*/
	padding-left: 2.125rem;
}

.size-small .add-left-padding {
	padding-left: 2rem;
}

.clear-icon {
	cursor: pointer;
	color: var(--text-color-secondary);
	top: 19px;
	right: 0.85rem;
}
.clear-icon:hover {
	color: var(--primary-color);
}

:deep(.p-inputtext) {
	width: 100%;
}
</style>
