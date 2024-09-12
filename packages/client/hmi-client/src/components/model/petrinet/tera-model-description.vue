<template>
	<section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]" v-bind:lazy="true" class="mb-0">
			<AccordionTab header="Description">
				<section v-if="!isGeneratingCard" class="description">
					<tera-show-more-text :text="description" :lines="5" />
					<p v-if="summary"><label>Model summary</label>{{ summary }}</p>
					<p v-if="modelType"><label>Model type</label>{{ modelType }}</p>
					<p v-if="fundedBy"><label>Funded by</label>{{ fundedBy }}</p>
					<p v-if="authors"><label>Authors</label>{{ authors }}</p>
					<p v-if="uses?.DirectUse"><label>Direct use</label>{{ uses.DirectUse }}</p>
					<p v-if="uses?.OutOfScopeUse"><label>Out of scope use</label>{{ uses.OutOfScopeUse }}</p>
					<p v-if="biasAndRiskLimitations"><label>Bias and Risk Limitations</label>{{ biasAndRiskLimitations }}</p>
					<p v-if="evaluation"><label>Evaluation</label>{{ evaluation }}</p>
					<p v-if="technicalSpecifications"><label>Technical Specifications</label>{{ technicalSpecifications }}</p>
					<p v-if="!isEmpty(glossary)"><label>Glossary</label>{{ glossary.join(', ') }}</p>
					<p v-if="!isEmpty(moreInformation)">
						<label>More Information</label>
						<a v-for="(link, index) in moreInformation" :key="index" :href="link" rel="noopener noreferrer">
							{{ link }}
						</a>
					</p>
				</section>
				<section v-else>
					<tera-progress-spinner is-centered>Generating description... </tera-progress-spinner>
				</section>
			</AccordionTab>
			<AccordionTab header="Diagram">
				<tera-model-diagram ref="teraModelDiagramRef" :model="model" :feature-config="featureConfig" />
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation :model="model" :is-editable="false" @model-updated="emit('model-updated')" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models" />
				</DataTable>
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets" />
				</DataTable>
			</AccordionTab>
		</Accordion>
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
import type { Author, Dataset, Model } from '@/types/Types';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import { isDataset, isModel } from '@/utils/data-util';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

const props = defineProps<{
	model: Model;
	featureConfig?: FeatureConfig;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model', 'model-updated']);
const teraModelDiagramRef = ref();

const card = computed<any>(() => props.model.metadata?.gollmCard ?? null);
const summary = computed(() => card.value?.summary?.modelSummary ?? '');
const description = computed(() => card.value?.details?.modelDescription ?? props.model?.header?.description ?? '');

const biasAndRiskLimitations = computed(() => card.value?.biasRisksLimitations?.modelBiasRisksLimitations ?? '');
const modelType = computed(() => card.value?.details?.modelType ?? props.model.header.schema_name ?? '');
const fundedBy = computed(() => card.value?.details?.fundedBy ?? '');
const evaluation = computed(() => card.value?.testing?.testingDataFactorsMetrics ?? '');
const technicalSpecifications = computed(() => card.value?.specs?.modelSpecs ?? '');
const glossary = computed(() => card.value?.glossary ?? []);
const moreInformation = computed(() => card.value?.moreInformation?.links ?? []);

const uses = computed(() => card.value?.uses ?? null);
const authors = computed(() => {
	const authorsSet: Set<string> = new Set();
	if (props.model?.metadata?.annotations?.authors)
		props.model.metadata.annotations.authors.forEach((author: Author) => authorsSet.add(author.name));
	if (card.value?.authors) card.value.authors.forEach((author: string) => authorsSet.add(author));
	return [...authorsSet].join(', ');
});

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const relatedTerariumModels = computed(() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]);
const relatedTerariumDatasets = computed(() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]);
</script>

<style scoped>
.description {
	display: flex;
	gap: var(--gap-small);
	flex-direction: column;
	grid-template-columns: max-content 1fr;
	margin-left: var(--gap-6);

	& label {
		font-weight: bold;
		margin-right: var(--gap-small);

		&:after {
			content: '.';
		}
	}
}

/* add space beneath when accordion content is visible*/
:deep(.p-toggleable-content) {
	padding-bottom: var(--gap-3);
}
</style>
