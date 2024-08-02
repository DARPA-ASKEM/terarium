<template>
	<div class="asset-card" draggable="true" @dragstart="startDrag(asset, resourceType)" @dragend="endDrag">
		<main>
			<div class="type-and-filters">
				{{ resourceType !== ResourceType.DOCUMENT ? resourceType.toUpperCase() : 'DOCUMENT' }}
				<div v-if="resourceType === ResourceType.MODEL">
					{{ (asset as Model).header.schema_name }}
				</div>
				<ul>
					<li v-for="(project, index) in foundInProjects" :key="index">
						<a>{{ project }}</a>
					</li>
				</ul>
			</div>
			<header class="title" v-html="title" />
			<div class="details" v-html="formatDetails" />
			<div
				class="description"
				v-if="resourceType === ResourceType.MODEL"
				v-html="highlightSearchTerms((asset as Model).header.description)"
			/>
			<div
				class="description"
				v-else-if="resourceType === ResourceType.DATASET"
				v-html="highlightSearchTerms((asset as Dataset).description)"
			/>
			<div
				class="description"
				v-else-if="resourceType === ResourceType.DOCUMENT"
				v-html="highlightSearchTerms((asset as DocumentAsset).description)"
			/>
			<div class="parameters" v-if="resourceType === ResourceType.MODEL && (asset as Model).semantics?.ode?.parameters">
				PARAMETERS:
				{{ (asset as Model).semantics?.ode.parameters }}
				<!--may need a formatting function this attribute is always undefined at the moment-->
			</div>
			<div class="features" v-else-if="resourceType === ResourceType.DATASET">
				FEATURES:
				<span v-for="(feature, index) in formatFeatures()" :key="index"> {{ feature }}, </span>
			</div>
			<footer><!--pill tags if already in another project--></footer>
		</main>
		<!--Add button from tera-search-item-->
		<slot />
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DocumentAsset, Dataset, Model } from '@/types/Types';
import { ResourceType, ResultType } from '@/types/common';
import * as textUtil from '@/utils/text';
import { useDragEvent } from '@/services/drag-drop';

const props = defineProps<{
	asset: ResultType;
	resourceType: ResourceType;
	source: string;
	highlight?: string;
}>();

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const foundInProjects = computed(() => [] /* ['project 1', 'project 2'] */);

const title = computed(() => {
	let value = '';
	if (props.resourceType === ResourceType.DOCUMENT) {
		value = (props.asset as DocumentAsset).name ?? '';
	} else if (props.resourceType === ResourceType.MODEL) {
		value = (props.asset as Model).header.name;
	} else if (props.resourceType === ResourceType.DATASET) {
		value = (props.asset as Dataset).name ?? '';
	}
	return highlightSearchTerms(value);
});

// Return formatted author, year, journal
const formatDetails = computed(() => {
	if (props.resourceType === ResourceType.DATASET) {
		return (props.asset as Dataset).datasetUrl;
	}
	return null;
});

// Format features for dataset type
const formatFeatures = () => {
	if (props.resourceType === ResourceType.DATASET) {
		// TODO: Once we have enrichment and extraction working we can see what annotations will look like
		/* const columns = props.asset.columns ?? [];
		if (!columns || columns.length === 0) return [];
		const annotations = columns.map((c) => (c.annotations ? c.annotations : []));
		const annotationNames = annotations.map((a) => (a.name ? a.name : ''));
		const max = 5;
		return annotationNames.length < max ? annotationNames : annotationNames.slice(0, max); */
	}
	return [];
};

const { setDragData, deleteDragData } = useDragEvent();

function startDrag(asset, resourceType) {
	setDragData('asset', asset);
	setDragData('resourceType', resourceType);
}

function endDrag() {
	deleteDragData('asset');
	deleteDragData('resourceType');
}
</script>

<style scoped>
.asset-card {
	background-color: var(--surface-a);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	color: var(--text-color-subdued);
	display: flex;
	font-size: var(--font-caption);
	justify-content: space-between;
	margin: 1px;
	min-height: 5rem;
	padding: 0.5rem 0.625rem 0.625rem;

	& > main {
		width: 100%;
	}
}

.asset-card:hover {
	border-color: var(--surface-border);
	cursor: pointer;
}

.asset-card[active='true'] {
	outline: 1px solid var(--primary-color-dark);
}

.type-and-filters {
	display: flex;
	align-items: center;
	gap: 2rem;
	font-size: 0.75rem;

	& > ul {
		margin-left: auto;
		list-style: none;
		display: flex;
		margin-right: 0.5rem;
		gap: 0.5rem;

		/* & > li:not(:last-child)::after {
			content: '-';
		} */
	}
}

aside {
	display: flex;
	gap: 0.5rem;
}

.title,
.details,
.description,
.parameters,
.features,
.snippets li {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.asset-filters {
	display: flex;
	gap: 0.5rem;
}

.title {
	color: var(--text-color-primary);
	font-size: var(--font-body-medium);
	margin: 0.1rem 0 0.1rem 0;
}

.details {
	margin: 0rem 0 0.25rem 0;
	font-size: var(--font-size);
}

i {
	padding: 0.2rem;
	border-radius: 3px;
	font-size: var(--font-caption);
}

.pi[active='true'] {
	background-color: var(--primary-color-light);
}

i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
}

.snippets {
	list-style: none;
}

/* Add ellipsis around a snippet */
.snippets li::before,
.snippets li::after {
	content: '…';
}
</style>
