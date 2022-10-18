<template>
	<div class="breakdown-pane-container">
		<div class="selected-title">{{ selectedSearchItems.length }} selected</div>
		<div class="add-to-title">Add to:</div>
		<div class="add-selected-buttons">
			<Button action>Current Project</Button>
			<Button action>Other Project</Button>
		</div>
		<div class="selected-items-container">
			<div v-for="(item, indx) in selectedSearchItems" class="selected-item" :key="`item-${indx}`">
				<div class="item-header">
					<div class="title-and-checkbox">
						<span v-show="isSelected(item)"><i class="fa-lg fa-regular fa-square-check"></i></span>
						<span v-show="!isSelected(item)"><i class="fa-lg fa-regular fa-square"></i></span>
						<div class="item-title" :title="getTitle(item)">{{ formatTitle(item) }}</div>
					</div>
					<i
						:class="getResourceTypeIcon(getType(item))"
						style="margin-left: 4px; margin-right: 4px"
					></i>
				</div>
				<div class="content">
					<multiline-description :text="formatDescription(item)" />
				</div>
			</div>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import Button from '@/components/Button.vue';
import { getResourceTypeIcon, isModel, isXDDArticle } from '@/utils/data-util';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';

export default defineComponent({
	name: 'SelectedResourcesOptionsPane',
	components: {
		// eslint-disable-next-line vue/no-reserved-component-names
		Button,
		MultilineDescription
	},
	props: {
		selectedSearchItems: {
			type: Array as PropType<ResultType[]>,
			required: true
		}
	},
	setup() {
		return {
			getResourceTypeIcon
		};
	},
	methods: {
		getTitle(item: ResultType) {
			return (item as Model).name || (item as XDDArticle).title;
		},
		formatTitle(item: ResultType) {
			const maxSize = 15;
			const itemTitle = this.getTitle(item);
			return itemTitle.length < maxSize ? itemTitle : `${itemTitle.substring(0, maxSize)}...`;
		},
		formatDescription(item: ResultType) {
			const maxSize = 120;
			const itemDesc = (item as Model).description || (item as XDDArticle).abstract || '[No Desc]';
			return itemDesc.length < maxSize ? itemDesc : `${itemDesc.substring(0, maxSize)}...`;
		},
		isSelected(item: ResultType) {
			return this.selectedSearchItems.find((searchItem) => {
				if (isModel(item)) {
					const itemAsModel = item as Model;
					const searchItemAsModel = searchItem as Model;
					return searchItemAsModel.id === itemAsModel.id;
				}
				if (isXDDArticle(item)) {
					const itemAsArticle = item as XDDArticle;
					const searchItemAsArticle = searchItem as XDDArticle;
					return searchItemAsArticle.title === itemAsArticle.title;
				}
				return false;
			});
		},
		getType(item: ResultType) {
			if (isModel(item)) {
				return (item as Model).type;
			}
			if (isXDDArticle(item)) {
				return ResourceType.XDD;
			}
			return ResourceType.ALL;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';

.selected-title {
	margin-bottom: 5px;
	font-size: larger;
	text-align: center;
	font-weight: bold;
	color: var(--un-color-accent);
}

.add-to-title {
	font-weight: 500;
}

.add-selected-buttons {
	display: flex;
	flex-direction: column;

	button {
		margin-bottom: 5px;
		padding-top: 4px;
		padding-bottom: 4px;
	}
}

.breakdown-pane-container {
	margin-bottom: 40px;
	min-height: 0;
	display: flex;
	flex-direction: column;
}

.selected-items-container {
	margin: 2px;
	display: flex;
	flex-direction: column;
	overflow-y: auto;

	.selected-item {
		border-style: solid;
		border-width: 2px;
		border-color: lightgray;
		padding: 3px;
	}

	.item-header {
		display: flex;
		flex-direction: row;
		justify-content: space-between;

		.title-and-checkbox {
			display: flex;
			.item-title {
				font-weight: 500;
				margin-bottom: 5px;
				margin-left: 4px;
			}
		}
	}
}
</style>
