<template>
	<div ref="containerElement" class="dropdown-button-container">
		<button type="button" class="btn dropdown-btn" @click="isDropdownOpen = !isDropdownOpen">
			<span>
				{{ innerButtonLabel ? `${innerButtonLabel}: ` : '' }}
				<strong>{{ selectedItemDisplayName }}</strong>
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

<script lang="ts">
import { computed, defineComponent, PropType, ref, toRefs, watchEffect } from 'vue';
import { cloneDeep } from 'lodash';
import DropdownControl from '@/components/widgets/dropdown-control.vue';
import IconChevronDown16 from '@carbon/icons-vue/es/chevron--down/16';
import IconCheckmark16 from '@carbon/icons-vue/es/checkmark/24';

export type DropdownItem = {
	displayName: string;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	value: any;
	selected?: boolean;
};

export default defineComponent({
	name: 'DropdownButton',
	components: {
		DropdownControl,
		IconChevronDown16,
		IconCheckmark16
	},
	props: {
		items: {
			type: Array as PropType<(string | DropdownItem)[]>,
			default: () => []
		},
		// eslint-disable-next-line vue/require-prop-types
		selectedItem: {
			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			default: null as any
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
	},
	emits: ['item-selected', 'items-selected'],
	setup(props, { emit }) {
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

		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const emitItemSelection = (item: any) => {
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

		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const isSelectedItem = (item: any) =>
			isMultiSelect.value ? selectedItems.value.includes(item) : selectedItem.value === item;

		return {
			isDropdownOpen,
			emitItemSelection,
			dropdownItems,
			selectedItemDisplayName,
			containerElement,
			isSelectedItem
		};
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';

.dropdown-button-container {
	position: relative;
}

.dropdown-control {
	position: absolute;
	right: 0;
	top: 90%; // Overlap the button slightly
	max-height: 400px;
	overflow-y: auto;

	&.left-aligned {
		left: 0;
		right: auto;
	}

	&.above {
		bottom: 90%;
		top: auto;
	}
}

.dropdown-option {
	white-space: nowrap;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.dropdown-option-selected {
	color: $selected-dark;
}

.dropdown-btn {
	display: flex;
	align-items: center;
	font-weight: normal;
	// padding: 5px;
}
</style>
