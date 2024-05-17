<template>
	<section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]" v-bind:lazy="true" class="mb-0">
			<AccordionTab>
				<template #header>
					<header id="Description">Description</header>
				</template>
				<section v-if="!isGeneratingCard" class="description">
					<SelectButton
						v-model="descriptionType"
						class="p-button-xsm"
						:options="descriptionOptions"
					/>
					<tera-show-more-text :text="description" :lines="5" />
					<p v-if="modelType"><label>Model type</label>{{ modelType }}</p>
					<p v-if="fundedBy"><label>Funded by</label>{{ fundedBy }}</p>
					<p v-if="authors"><label>Authors</label>{{ authors }}</p>
					<p v-if="uses?.DirectUse"><label>Direct use</label>{{ uses.DirectUse }}</p>
					<p v-if="uses?.OutOfScopeUse"><label>Out of scope use</label>{{ uses.OutOfScopeUse }}</p>
					<p v-if="biasAndRiskLimitations">
						<label>Bias and Risk Limitations</label>{{ biasAndRiskLimitations }}
					</p>
					<p v-if="evaluation"><label>Evaluation</label>{{ evaluation }}</p>
					<p v-if="technicalSpecifications">
						<label>Technical Specifications</label>{{ technicalSpecifications }}
					</p>
					<p v-if="!isEmpty(glossary)"><label>Glossary</label>{{ glossary.join(', ') }}</p>
					<p v-if="!isEmpty(moreInformation)">
						<label>More Information</label>
						<a
							v-for="(link, index) in moreInformation"
							:key="index"
							:href="link"
							rel="noopener noreferrer"
						>
							{{ link }}
						</a>
					</p>
					<p v-if="!isEmpty(provenance)"><label>Provenance</label>{{ provenance }}</p>
					<p v-if="!isEmpty(schema)"><label>Schema</label>{{ schema }}</p>
					<p v-if="!isEmpty(sourceDataset)"><label>Source dataset</label>{{ sourceDataset }}</p>
					<p v-if="!isEmpty(usage)"><label>Usage</label>{{ usage }}</p>
					<p v-if="!isEmpty(strengths)"><label>Strengths</label>{{ strengths }}</p>
					<p v-if="!isEmpty(assumptions)"><label>Assumptions</label>{{ assumptions }}</p>
				</section>
				<section v-else>
					<tera-progress-spinner is-centered>Generating description... </tera-progress-spinner>
				</section>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Diagram">Diagram</header>
				</template>
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					:model-configuration="modelConfigurations?.[0]"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Provenance">Provenance</header>
				</template>
				<tera-related-documents
					class="m-2"
					:documents="documents"
					:asset-type="AssetType.Model"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Model-eqautions">Model equations</header>
				</template>
				<tera-model-equation
					:model="model"
					:is-editable="false"
					@model-updated="emit('model-updated')"
				/>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)">
				<template #header>
					<header id="Associated-resources">Associated resources</header>
				</template>
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
			class="mt-0"
			:readonly="featureConfig?.isPreview"
		/>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { FeatureConfig, ResultType } from '@/types/common';
import type { Dataset, Model, ModelConfiguration, ProjectAsset } from '@/types/Types';
import { AssetType } from '@/types/Types';
import SelectButton from 'primevue/selectbutton';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import { isDataset, isDocument, isModel } from '@/utils/data-util';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraModelSemanticTables from '@/components/model/tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	featureConfig?: FeatureConfig;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model', 'fetch-model', 'update-configuration', 'model-updated']);

const teraModelDiagramRef = ref();
const descriptionType = ref('TA1');
const descriptionOptions = ref(['TA1', 'TA4']);

// FIXME: expand Card typing definition?
const card = computed<any>(() => {
	// Display the GoLLM card if the description is set to TA4 (true).
	if (descriptionType.value === 'TA4' && props.model.metadata?.gollmCard) {
		return props.model.metadata?.gollmCard;
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
const description = computed(
	() =>
		card.value?.ModelDetails?.model_description ??
		card.value?.description ??
		props.model?.header?.description ??
		''
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
const strengths = computed(() => card.value?.strengths ?? '');
const assumptions = computed(() => card.value?.assumptions ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const authors = computed(() => {
	const authorsArray = props.model?.metadata?.annotations?.authors ?? [];
	if (card.value?.ModelCardAuthors) authorsArray.unshift(card.value?.ModelCardAuthors);
	else if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);
	return authorsArray.join(', ');
});

const documents = computed<{ name: string; id: string }[]>(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.map((projectAsset: ProjectAsset) => ({
				name: projectAsset.assetName,
				id: projectAsset.assetId
			})) ?? []
);

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

function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	emit('update-configuration', updatedConfiguration);
}
</script>

<style scoped>
.description {
	display: flex;
	gap: var(--gap-small);
	flex-direction: column;
	grid-template-columns: max-content 1fr;

	& label {
		font-weight: bold;
		margin-right: var(--gap-small);

		&:after {
			content: '.';
		}
	}
}
</style>
