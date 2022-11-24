<template>
	<div class="search-listview-container">
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th><span class="left-cover" />NAME</th>
						<th>DESCRIPTION</th>
						<th>FRAMEWORK</th>
						<th>PREVIEW<span class="right-cover" /></th>
					</tr>
				</thead>
				<tbody>
					<tr
						v-for="d in models"
						:key="d.id"
						class="tr-item"
						:class="{ selected: isSelected(d) }"
						@click="updateExpandedRow(d)"
					>
						<td class="name-and-desc-col">
							<div class="name-and-desc-layout">
								<!-- in case of requesting multiple selection -->
								<div class="radio" @click.stop="updateSelection(d)">
									<span v-show="isSelected(d)">
										<IconCheckboxChecked20 />
									</span>
									<span v-show="!isSelected(d)">
										<IconCheckbox20 />
									</span>
									<component :is="getResourceTypeIcon(d.type)" />
								</div>
								<div class="content">
									<div class="text-bold">{{ formatOutputName(d) }}</div>
								</div>
							</div>
						</td>
						<td class="desc-col">
							<p class="max-content">{{ formatDescription(d) }}</p>
						</td>
						<td class="framework-col">
							<div class="text-bold">{{ d.framework }}</div>
						</td>
						<td class="preview-col">
							<div class="preview-container">
								<!-- preview renderer -->
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script setup lang="ts">
import { PropType, ref, toRefs, watch } from 'vue';
import { Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { getResourceTypeIcon, isModel } from '@/utils/data-util';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';

const props = defineProps({
	models: {
		type: Array as PropType<Model[]>,
		default: () => []
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['toggle-model-selected']);

const expandedRowId = ref('');

const { models, selectedSearchItems } = toRefs(props);

watch(
	models,
	() => {
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const elem: any = document.getElementsByClassName('table-fixed-head');
		if (elem.length === 0) return;
		elem[0].scrollTop = 0;
	},
	{ immediate: true }
);

const isExpanded = (model: Model) => expandedRowId.value === model.id;

const updateExpandedRow = (model: Model) => {
	expandedRowId.value = expandedRowId.value === model.id ? '' : model.id;
};

const formatOutputName = (d: Model) => d.name;

const isSelected = (model: Model) =>
	selectedSearchItems.value.find((item) => {
		if (isModel(item)) {
			const itemAsModel = item as Model;
			return itemAsModel.id === model.id;
		}
		return false;
	});

const updateSelection = (model: Model) => {
	emit('toggle-model-selected', model);
};

const formatDescription = (d: Model) => {
	if (!d.description) return '';
	return isExpanded(d) || d.description.length < 140
		? d.description
		: `${d.description.substring(0, 140)}...`;
};
</script>

<style scoped>
.search-listview-container {
	background: var(--background-light-2);
	color: black;
	width: 100%;
}

table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

th {
	padding: 8px 16px;
}

tr {
	border: 2px solid var(--separator);
	cursor: pointer;
}

thead tr,
thead th {
	border: none;
}

td {
	background: var(--background-light-1);
	vertical-align: top;
	padding: 8px 16px;
}

tr th {
	font-size: var(--font-size-small);
	font-weight: normal;
}

.table-fixed-head {
	overflow-y: auto;
	overflow-x: hidden;
	height: 100%;
	width: 100%;
}

.table-fixed-head thead th {
	position: sticky;
	top: -1px;
	z-index: 1;
	background-color: aliceblue;
}

.left-cover,
.right-cover {
	/* Cover left and right gap in the fixed table header */
	position: absolute;
	height: 100%;
	width: 2px;
	left: -2px;
	background: var(--background-light-2);
	top: 0;
}

.right-cover {
	left: unset;
	right: -2px;
}

.tr-item {
	height: 50px;
}

.tr-item.selected {
	border: 2px double var(--un-color-accent-lighter);
}

.tr-item.selected .name-and-desc-col {
	border-left: 2px solid var(--un-color-accent-lighter);
}

.tr-item.selected td {
	background-color: var(--un-color-accent-lighter);
}

.text-bold {
	font-weight: 500;
	margin-bottom: 5px;
}

.name-and-desc-col {
	width: 25%;
	overflow-wrap: break-word;
}

.name-and-desc-layout {
	display: flex;
	align-content: stretch;
	align-items: stretch;
}

.name-and-desc-layout .radio {
	flex: 0 0 auto;
	align-self: flex-start;
	margin: 3px 5px 0 0;
}

.name-and-desc-layout .radio .disabled {
	color: var(--background-light-3);
}

.name-and-desc-layout .content {
	flex: 1 1 auto;
	overflow-wrap: break-word;
}

.name-and-desc-layout .content .not-ready-label {
	font-weight: 600;
	border: none;
	border-radius: 5px;
	background-color: var(--background-light-3);
	color: darkgray;
	padding: 6px;
	float: right;
}

.desc-col {
	width: 45%;
}

.framework-col {
	width: 10%;
	overflow-wrap: break-word;
}

.max-content {
	max-height: 250px;
	overflow-y: auto;
}

/* time series hidden until actually put into use */
.preview-col {
	padding-left: 5px;
	padding-right: 10px;
}

.preview-container {
	background-color: #f1f1f1;
	width: 100%;
	height: 50px;
}
</style>
