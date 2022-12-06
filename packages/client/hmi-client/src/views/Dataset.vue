<script setup lang="ts">
import { getDataset } from '@/services/dataset';
import { Dataset } from '@/types/Dataset';
import { ref, watch } from 'vue';

const props = defineProps<{
	datasetId: string;
}>();

const dataset = ref<Dataset | null>(null);
// Whenever selectedModelId changes, fetch model with that ID
watch(
	() => [props.datasetId],
	async () => {
		dataset.value = await getDataset(props.datasetId);
	},
	{ immediate: true }
);

const formatFeatures = (d: Dataset) => {
	const features = d.annotations.annotations.feature ?? [];
	if (!features || features.length === 0) return [];
	return features.map((f) => (f.display_name !== '' ? f.display_name : f.name));
};
</script>

<template>
	<section class="dataset">
		<div class="dataset-attributes-container">
			<h3><b>Name:</b> {{ dataset?.name ?? '' }}</h3>
			<div><b>Description:</b> {{ dataset?.description ?? '' }}</div>
			<div><b>Maintainer:</b> {{ dataset?.maintainer ?? '' }}</div>
			<div><b>Quality:</b> {{ dataset?.quality ?? '' }}</div>
			<div><b>URL:</b> {{ dataset?.url ?? '' }}</div>
			<div><b>Geospatial Resolution:</b> {{ dataset?.geospatialResolution ?? '' }}</div>
			<div><b>Temporal Resolution:</b> {{ dataset?.temporalResolution ?? '' }}</div>
			<ul v-if="dataset !== null">
				Geo Annotations:
				<li v-for="annotation in dataset.annotations.annotations.geo" :key="annotation.name">
					<strong>Name: </strong> {{ annotation.name }} <strong>Description: </strong>
					{{ annotation.description }} <strong>GADM Level: </strong> {{ annotation.gadm_level }}
				</li>
			</ul>
			<ul v-if="dataset !== null">
				Temporal Annotations:
				<li v-for="annotation in dataset.annotations.annotations.date" :key="annotation.name">
					<strong>Name: </strong> {{ annotation.name }} <strong>Description: </strong>
					{{ annotation.description }} <strong>Time Format: </strong> {{ annotation.time_format }}
				</li>
			</ul>
		</div>
		<aside>
			<p class="description">{{ dataset?.description ?? '' }}</p>
			<h4>Features</h4>
			<ul v-if="dataset !== null">
				<li v-for="feature in formatFeatures(dataset)" :key="feature">
					<strong>{{ feature }}</strong>
				</li>
			</ul>
		</aside>
	</section>
</template>

<style scoped>
.dataset {
	margin: 10px;
	display: flex;
	padding-left: 2rem;
	padding-top: 1rem;
}

.dataset-attributes-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

aside {
	width: 400px;
	margin-left: 10px;
	background: var(--un-color-black-5);
	padding: 10px;
}

h3 {
	margin-bottom: 10px;
}

h4 {
	margin-top: 30px;
	margin-bottom: 10px;
}

.description,
ul {
	max-height: 400px;
	overflow-y: auto;
}

li {
	display: block;
}

strong {
	font-weight: bold;
}
</style>
