<template>
	<div ref="containerElement" class="dropdown-button-container">
		<button type="button" class="btn dropdown-btn" @click="isDropdownOpen = !isDropdownOpen">
			<span>
				{{ innerButtonLabel ? `${innerButtonLabel}: ` : '' }}
				<b>{{ selectedItemDisplayName }}</b>
			</span>
			<IconChevronDown16 />
		</button>
		<dropdown-control
			v-if="isDropdownOpen"
			class="dropdown-control"
			:class="{
				'left-aligned': isDropdownLeftAligned,
				above: isDropdownAbove
			}"
		>
			<template #content>
				<div
					v-for="item in dropdownItems"
					:key="item.value"
					class="dropdown-option"
					:class="{
						'dropdown-option-selected': isSelectedItem(item.value)
					}"
					@click="emitItemSelection(item.value)"
				>
					{{ item.displayName }}
					<IconCheckmark16 v-if="item.selected" />
				</div>
			</template>
		</dropdown-control>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType, ref, toRefs, watchEffect } from 'vue';
import { cloneDeep } from 'lodash';
import DropdownControl from '@/components/widgets/dropdown-control.vue';
import IconChevronDown16 from '@carbon/icons-vue/es/chevron--down/16';
import IconCheckmark16 from '@carbon/icons-vue/es/checkmark/24';

export type DropdownItem = {
	displayName: string;
	value: string;
	selected?: boolean;
};

const props = defineProps({
	items: {
		type: Array as PropType<(string | DropdownItem)[]>,
		default: () => []
	},
	selectedItem: {
		type: String as PropType<string | DropdownItem | null>,
		default: null
	},
	innerButtonLabel: {
		type: String as PropType<string | null>,
		default: null
	},
	isDropdownLeftAligned: {
		type: Boolean,
		default: false
	},
	isDropdownAbove: {
		type: Boolean,
		default: false
	},
	// the following two props are added to enable multi-select mode
	isMultiSelect: {
		type: Boolean,
		default: false
	},
	selectedItems: {
		type: Array as PropType<string[]>,
		default: () => []
	}
});

const emit = defineEmits(['item-selected', 'items-selected']);

const { items, selectedItem, isMultiSelect, selectedItems } = toRefs(props);

const isDropdownOpen = ref(false);
const containerElement = ref<HTMLElement | null>(null);

const onClickOutside = (event: MouseEvent) => {
	if (event.target instanceof Element && containerElement.value?.contains(event.target)) {
		// Click was within this element, so do nothing
		return;
	}
	isDropdownOpen.value = false;
};

watchEffect(() => {
	if (isDropdownOpen.value) {
		window.document.addEventListener('click', onClickOutside);
	} else {
		window.document.removeEventListener('click', onClickOutside);
	}
});

// This component can accept a list of strings or a list of DropdownItems.
//  This computed property standardizes by converting strings to DropdownItems.
const dropdownItems = computed<DropdownItem[]>(() => {
	const standardizedDropdownItems = items.value.map((item) =>
		typeof item === 'string' ? { displayName: item, value: item } : item
	);
	if (isMultiSelect.value) {
		standardizedDropdownItems.forEach((dropdownItem) => {
			// eslint-disable-next-line no-param-reassign
			dropdownItem.selected = selectedItems.value.includes(dropdownItem.value);
		});
	}
	return standardizedDropdownItems;
});

const selectedItemDisplayName = computed(() =>
	// eslint-disable-next-line no-nested-ternary
	isMultiSelect.value && selectedItems.value.length > 0
		? selectedItems.value.length > 1
			? '(multiple)'
			: selectedItems.value[0]
		: dropdownItems.value.find((item) => item.value === selectedItem.value)?.displayName ??
		  selectedItem.value
);

const emitItemSelection = (item: string | DropdownItem) => {
	if (!isMultiSelect.value) {
		isDropdownOpen.value = false;
		emit('item-selected', item);
	} else {
		// keep the dropdown menu open in the mode if multi-select, and emit all selected
		const updatedDropdownItems = cloneDeep(dropdownItems);
		const itemToUpdate = updatedDropdownItems.value.find((i) => i.value === item);
		if (itemToUpdate) {
			itemToUpdate.selected = !itemToUpdate.selected;
		}
		emit(
			'items-selected',
			updatedDropdownItems.value.filter((itm) => itm.selected).map((item2) => item2.value)
		);
	}
};

const isSelectedItem = (item: string) => {
	if (isMultiSelect.value) {
		return selectedItems.value.includes(item);
	}
	if (selectedItem.value && selectedItem.value === item) {
		return true;
	}
	return false;
};
</script>

<style scoped>
.dropdown-button-container {
	position: relative;
}

.dropdown-control {
	position: absolute;
	top: 90%;
	/* Overlap the button slightly */
	right: 0;
	max-height: 400px;
	overflow-y: auto;
}

.dropdown-control.left-aligned {
	left: 0;
	right: auto;
}

.dropdown-control.above {
	bottom: 90%;
	top: auto;
}

.dropdown-option {
	white-space: nowrap;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.dropdown-option-selected {
	color: var(--un-color-accent-dark);
}

.dropdown-btn {
	display: flex;
	align-items: center;
	font-weight: normal;
	padding: 5px;
}
.dropdown-btn span {
	padding-left: 6px;
	padding-right: 4px;
}
</style>
