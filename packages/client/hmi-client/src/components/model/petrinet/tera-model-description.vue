<template>
	<main>
		<section class="info">
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab header="Description">
					<section class="description">
						<tera-show-more-text :text="description" :lines="5" />
					</section>
				</AccordionTab>
				<AccordionTab header="Additional information">
					<section class="additional-information">
						<article v-if="!isEmpty(provenance)">
							<h5>Provenance</h5>
							<p v-html="provenance" />
						</article>
						<article v-if="!isEmpty(schema)">
							<h5>Schema</h5>
							<p v-html="schema" />
						</article>
						<article v-if="!isEmpty(sourceDataset)">
							<h5>Source dataset</h5>
							<p v-html="sourceDataset" />
						</article>
						<article v-if="!isEmpty(usage)">
							<h5>Usage</h5>
							<p v-html="usage" />
						</article>
					</section>
				</AccordionTab>
			</Accordion>
			<section class="details">
				<ul>
					<li>Bam</li>
					<li>Bam</li>
					<li>Bam</li>
					<li>Bam</li>
				</ul>
				<!-- <table class="bibliography">
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
			</table> -->
				<tera-related-documents
					:documents="documents"
					:asset-type="AssetType.Models"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</section>
		</section>
		<Accordion multiple :active-index="[0, 1, 2, 3]" v-bind:lazy="true">
			<AccordionTab header="Diagram">
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig.isPreview"
					:model-configuration="modelConfigurations[0]"
					@update-model="updateModelContent"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation
					:model="model"
					:is-editable="!featureConfig.isPreview"
					@model-updated="emit('model-updated')"
				/>
			</AccordionTab>
			<AccordionTab header="Model observables">
				<tera-model-observable
					:model="model"
					:is-editable="!featureConfig.isPreview"
					@update-model="updateModelContent"
				/>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models" />
				</DataTable>
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets" />
				</DataTable>
				<DataTable :value="relatedTerariumDocuments">
					<Column field="name" header="Documents" />
				</DataTable>
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
import { computed, ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { AssetType, DocumentAsset, Model, Dataset, ModelConfiguration } from '@/types/Types';
import { FeatureConfig, AcceptedExtensions, ResultType } from '@/types/common';
import * as textUtil from '@/utils/text';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelObservable from '@/components/model/petrinet/tera-model-observable.vue';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import TeraModelSemanticTables from './tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	highlight: string;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-model', 'fetch-model', 'update-configuration', 'model-updated']);

const teraModelDiagramRef = ref();

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

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

function fetchAsset() {
	emit('fetch-model');
}

function updateModelContent(updatedModel: Model) {
	emit('update-model', updatedModel);
}

function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	emit('update-configuration', updatedConfiguration);
}
</script>

<style scoped>
.info {
	display: flex;
	width: 100%;
	gap: 2rem;

	& > * {
		/* width: 50%; */
		flex: 1;
	}
	/* width: 100%; */
	/* justify-content: space-between; */
}

.description,
.additional-information {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-left: 1.5rem;
}

.details {
	padding-top: 1rem;
	padding-right: 1rem;
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
