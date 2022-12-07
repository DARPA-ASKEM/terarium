<template>
	<div>
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th><span class="left-cover" />Name</th>
						<th>Description</th>
						<th>Feature(s)</th>
						<th>Preview<span class="right-cover" /></th>
					</tr>
				</thead>
				<tbody>
					<tr
						v-for="d in datasets"
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
								</div>
								<div class="content">
									<div class="text-bold">{{ formatOutputName(d) }}</div>
									<template v-if="isExpanded(d)">
										<br />
										<div><b>Concepts</b></div>
										<ul>
											<li v-for="tag in getConceptTags(d)" :key="tag">
												<span
													v-if="searchTerm.toLowerCase() === tag.toLowerCase()"
													class="highlight-concept"
													>{{ tag }}</span
												>
												<span v-else>{{ tag }}</span>
											</li>
										</ul>
									</template>
								</div>
							</div>
						</td>
						<td class="desc-col">
							<p class="max-content">{{ formatDescription(d) }}</p>
						</td>
						<td class="maintainer-col">
							<div v-for="feature in formatFeatures(d)" :key="feature">
								{{ feature }}
							</div>
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
import { ResultType } from '@/types/common';
import IconCheckbox20 from '@carbon/icons-vue/es/checkbox/20';
import IconCheckboxChecked20 from '@carbon/icons-vue/es/checkbox--checked/20';
import { ConceptFacets } from '@/types/Concept';
import { Dataset } from '@/types/Dataset';
import { isDataset } from '@/utils/data-util';

const props = defineProps({
	datasets: {
		type: Array as PropType<Dataset[]>,
		default: () => []
	},
	rawConceptFacets: {
		type: Object as PropType<ConceptFacets | null>,
		default: () => null
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	},
	searchTerm: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['toggle-dataset-selected']);

const expandedRowId = ref<string | number>('');

const { datasets, selectedSearchItems } = toRefs(props);

watch(
	datasets,
	() => {
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const elem: any = document.getElementsByClassName('table-fixed-head');
		if (elem.length === 0) return;
		elem[0].scrollTop = 0;
	},
	{ immediate: true }
);

const getConceptTags = (dataset: Dataset) => {
	const tags = [] as string[];
	if (props.rawConceptFacets) {
		const datasetConcepts = props.rawConceptFacets.results.filter(
			(conceptResult) => conceptResult.id === dataset.id
		);
		tags.push(...datasetConcepts.map((c) => c.name ?? c.curie));
	}
	return tags;
};

const isExpanded = (dataset: Dataset) => expandedRowId.value === dataset.id;

const updateExpandedRow = (dataset: Dataset) => {
	expandedRowId.value = expandedRowId.value === dataset.id ? '' : dataset.id;
};

const formatOutputName = (d: Dataset) => d.name;

const isSelected = (dataset: Dataset) =>
	selectedSearchItems.value.find((item) => {
		if (isDataset(item)) {
			const itemAsDataset = item as Dataset;
			return itemAsDataset.id === dataset.id;
		}
		return false;
	});

const updateSelection = (dataset: Dataset) => {
	emit('toggle-dataset-selected', dataset);
};

const formatDescription = (d: Dataset) => {
	if (!d.description) return '';
	return isExpanded(d) || d.description.length < 140
		? d.description
		: `${d.description.substring(0, 140)}...`;
};

const formatFeatures = (d: Dataset) => {
	const features = d.annotations.annotations.feature ?? [];
	if (!features || features.length === 0) return [];
	const featuresNames = features.map((f) => (f.display_name !== '' ? f.display_name : f.name));
	const max = 5;
	return isExpanded(d) || featuresNames.length < max ? featuresNames : featuresNames.slice(0, max);
};
</script>

<style scoped>
table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

th {
	padding: 8px 16px;
	text-align: left;
}

tbody tr {
	border-top: 2px solid var(--separator);
	cursor: pointer;
}

tbody tr:first-child {
	border-top-width: 0;
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
	/* FIXME: shouldn't need to be manually kept in sync with data explorer bg colour */
	background-color: var(--un-color-body-surface-background);
}

.tr-item {
	height: 50px;
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

.maintainer-col {
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

.highlight-concept {
	background-color: yellow;
}
</style>
