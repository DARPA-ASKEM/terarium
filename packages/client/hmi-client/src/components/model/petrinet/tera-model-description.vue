<template>
	<main>
		<section>
			<table class="bibliography">
				<tr>
					<th>Framework</th>
					<th>Model version</th>
					<th>Date created</th>
					<th>Created by</th>
					<th>Source</th>
					<th>Author email</th>
					<th>Institution</th>
					<th>License</th>
					<th>Complexity</th>
				</tr>
				<tr>
					<td class="framework">{{ model?.header?.schema_name }}</td>
					<td>{{ model?.header?.model_version }}</td>
					<td>{{ model?.metadata?.processed_at ?? card?.date }}</td>
					<td>
						{{ card?.authorAuthor }}
						<template v-if="model?.metadata?.annotations?.authors">
							, {{ model.metadata.annotations.authors.join(', ') }}
						</template>
					</td>
					<td>{{ card?.authorEmail }}</td>
					<td>{{ model?.metadata?.processed_by }}</td>
					<td>{{ card?.authorInst }}</td>
					<td>{{ card?.license }}</td>
					<td>{{ card?.complexity }}</td>
				</tr>
			</table>
		</section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5]">
			<AccordionTab>
				<template #header>Related publications</template>
				<tera-related-documents
					:documents="documents"
					:asset-type="AssetType.Models"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</AccordionTab>
			<AccordionTab>
				<template #header>Description</template>
				<p v-html="description" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(usage)">
				<template #header>Usage</template>
				<p v-html="usage" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(sourceDataset)">
				<template #header>Source dataset</template>
				<p v-html="sourceDataset" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(provenance)">
				<template #header>Provenance</template>
				<p v-html="provenance" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(schema)">
				<template #header>Schema</template>
				<p v-html="schema" />
			</AccordionTab>
		</Accordion>
		<tera-model-semantic-tables
			:model="model"
			:model-configurations="modelConfigurations"
			@update-model="(modelClone) => emit('update-model', modelClone)"
		/>
	</main>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { AssetType, DocumentAsset, Model, ModelConfiguration } from '@/types/Types';
import { AcceptedExtensions } from '@/types/common';
import * as textUtil from '@/utils/text';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import TeraModelSemanticTables from './tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	highlight: string;
}>();

const emit = defineEmits(['update-model', 'fetch-model']);

function fetchAsset() {
	emit('fetch-model');
}

const card = computed(() => {
	if (props.model.metadata?.card) {
		const cardWithUnknowns = props.model.metadata?.card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});
const description = computed(() =>
	highlightSearchTerms(props.model?.header?.description.concat(' ', card.value?.description ?? ''))
);
const usage = computed(() => card.value?.usage ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const documents = computed(
	() =>
		useProjects()
			.activeProject.value?.assets?.documents?.filter((document: DocumentAsset) =>
				[AcceptedExtensions.PDF, AcceptedExtensions.TXT, AcceptedExtensions.MD].some(
					(extension) => {
						if (document.fileNames && !isEmpty(document.fileNames)) {
							return document.fileNames[0]?.endsWith(extension);
						}
						return false;
					}
				)
			)
			.map((document: DocumentAsset) => ({
				name: document.name,
				id: document.id
			})) ?? []
);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}
</script>

<style scoped>
section {
	margin-left: 1rem;
}

table th {
	text-align: left;
}

table tr > td:empty:before {
	content: '--';
}

td.framework {
	text-transform: capitalize;
}

table.bibliography th,
table.bibliography td {
	font-family: var(--font-family);
	max-width: 15rem;
	padding-right: 1rem;
}

table.bibliography th {
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
}

table.bibliography td {
	font-weight: 400;
	font-size: var(--font-body-small);
}
</style>
