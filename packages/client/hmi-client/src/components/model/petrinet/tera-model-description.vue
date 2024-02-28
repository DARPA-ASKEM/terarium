<template>
	<main>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]">
			<AccordionTab header="Description">
				<section v-if="!isGeneratingCard" class="description">
					<tera-show-more-text :text="description" :lines="5" />
					<p v-if="modelType">
						<label class="p-text-secondary mr-2">Model type</label>{{ modelType }}
					</p>
					<template v-if="fundedBy">
						<div class="label-value-pair">
							<p><label class="p-text-secondary mr-2">Funded by</label>{{ fundedBy }}</p>
						</div>
					</template>
					<template v-if="authors">
						<div class="label-value-pair">
							<p><label class="p-text-secondary mr-2">Authors</label>{{ authors }}</p>
						</div>
					</template>
					<template v-if="uses">
						<div class="label-value-pair mt-2">
							<h6>Uses</h6>
							<p><label class="p-text-secondary mr-2">Direct use</label>{{ uses.DirectUse }}</p>
						</div>
						<div class="label-value-pair">
							<p>
								<label class="p-text-secondary mr-2">Out of scope use</label>
								{{ uses.OutOfScopeUse }}
							</p>
						</div>
					</template>

					<template v-if="biasAndRiskLimitations">
						<div class="label-value-pair mt-2">
							<h6>Bias and Risk Limitations</h6>
							<p>{{ biasAndRiskLimitations }}</p>
						</div>
					</template>

					<template v-if="evaluation">
						<div class="label-value-pair mt-2">
							<h6>Evaluation</h6>
							<p>{{ evaluation }}</p>
						</div>
					</template>

					<template v-if="technicalSpecifications">
						<div class="label-value-pair mt-2">
							<h6>Technical Specifications</h6>
							<p>{{ technicalSpecifications }}</p>
						</div>
					</template>

					<template v-if="!isEmpty(glossary)">
						<div class="label-value-pair mt-2">
							<h6>Glossary</h6>
							<p>{{ glossary.join(', ') }}</p>
						</div>
					</template>

					<template v-if="!isEmpty(moreInformation)">
						<div class="label-value-pair mt-2">
							<h6>More Information</h6>
							<a
								v-for="(link, index) in moreInformation"
								:href="link"
								target="_blank"
								rel="noopener noreferrer"
								:key="index"
								>{{ link }}</a
							>
						</div>
					</template>

					<template v-if="!isEmpty(provenance)">
						<div class="label-value-pair mt-2">
							<h6>Provenance</h6>
							<p v-html="provenance" />
						</div>
					</template>
					<template v-if="!isEmpty(schema)">
						<div class="label-value-pair mt-2">
							<h6>Schema</h6>
							<p v-html="schema" />
						</div>
					</template>
					<template v-if="!isEmpty(sourceDataset)">
						<div class="label-value-pair mt-2">
							<h6>Source dataset</h6>
							<p v-html="sourceDataset" />
						</div>
					</template>
					<template v-if="!isEmpty(usage)">
						<div class="label-value-pair mt-2">
							<h6>Usage</h6>
							<p v-html="usage" />
						</div>
					</template>
				</section>
				<section v-else>
					<tera-progress-spinner is-centered> Generating card... </tera-progress-spinner>
				</section>
			</AccordionTab>
			<AccordionTab header="Diagram">
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					:model-configuration="modelConfigurations?.[0]"
					@update-model="updateModelContent"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab header="Provenance">
				<tera-related-documents
					class="m-2"
					:documents="documents"
					:asset-type="AssetType.Model"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					@model-updated="emit('model-updated')"
				/>
			</AccordionTab>
			<AccordionTab header="Model observables">
				<tera-model-observable
					:model="model"
					:is-editable="!featureConfig?.isPreview"
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
import { AcceptedExtensions, FeatureConfig, ResultType } from '@/types/common';
import type { Dataset, DocumentAsset, Model, ModelConfiguration } from '@/types/Types';
import { AssetType } from '@/types/Types';
import * as textUtil from '@/utils/text';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelObservable from '@/components/model/petrinet/tera-model-observable.vue';
import { isDataset, isDocument, isModel } from '@/utils/data-util';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraModelSemanticTables from './tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	highlight?: string;
	featureConfig?: FeatureConfig;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model', 'fetch-model', 'update-configuration', 'model-updated']);

const teraModelDiagramRef = ref();

const card = computed(() => {
	// prioritize gollm_card over skema card
	if (props.model.metadata?.gollmCard) {
		return props.model.metadata.gollmCard;
	}

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
	highlightSearchTerms(
		card.value?.ModelDetails?.model_description ??
			card.value?.description ??
			props.model?.header?.description ??
			''
	)
);

const biasAndRiskLimitations = computed(
	() => card.value?.BiasRisksLimitations?.bias_risks_limitations ?? ''
);
const modelType = computed(
	() => card.value?.ModelDetails?.ModelType ?? props.model.header.schema_name ?? ''
);
const fundedBy = computed(() => card.value?.ModelDetails?.FundedBy ?? '');
const evaluation = computed(() => card.value?.Evaluation?.TestingDataFactorsMetrics ?? '');
const technicalSpecifications = computed(
	() => card.value?.TechnicalSpecifications?.model_specs ?? ''
);
const glossary = computed(() => card.value?.Glossary?.terms ?? []);
const moreInformation = computed(() => card.value?.MoreInformation?.links ?? []);

const uses = computed(() => card.value?.Uses ?? null);
const usage = computed(() => card.value?.usage ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const authors = computed(() => {
	const authorsArray = props.model?.metadata?.annotations?.authors ?? [];

	if (card.value?.ModelCardAuthors) authorsArray.unshift(card.value?.ModelCardAuthors);
	else if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);

	return authorsArray.join(', ');
});

const documents = computed(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.filter((document: DocumentAsset) =>
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
.overview {
	display: flex;
	width: 100%;
	gap: 2rem;

	& > * {
		flex: 1;
	}
}

.description,
.additional-information {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-left: 1.5rem;
}
.details-column {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
}

.framework {
	text-transform: capitalize;
}
</style>
