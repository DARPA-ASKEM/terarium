<template>
	<main>
		<section>
			<Message icon="none">
				This page describes the model. Use the content switcher above to see the diagram and manage
				configurations.
			</Message>
			<table>
				<tr>
					<th>Framework</th>
					<th>Model version</th>
					<th>Date created</th>
					<th>Created by</th>
					<th>Source</th>
				</tr>
				<tr>
					<td class="framework">{{ model?.header.schema_name }}</td>
					<td>{{ model?.header.model_version }}</td>
					<td>{{ model?.metadata?.processed_at }}</td>
					<td>{{ model?.metadata?.annotations?.authors?.join(', ') }}</td>
					<td>{{ model?.metadata?.processed_by }}</td>
				</tr>
			</table>
		</section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
			<AccordionTab>
				<template #header>Related publications</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>Description</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>Parameters</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>State variables</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>Observables</template>
			</AccordionTab>
			<AccordionTab>
				<template #header>Transitions</template>
			</AccordionTab>
			<AccordionTab>
				<template #header> Other concepts </template>
			</AccordionTab>
			<AccordionTab>
				<template #header> Time </template>
			</AccordionTab>
		</Accordion>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import { getRelatedArtifacts } from '@/services/provenance';
import { ResultType } from '@/types/common';
import { Model, ProvenanceType } from '@/types/Types';

const props = defineProps<{ model: Model }>();

const relatedTerariumArtifacts = ref<ResultType[]>([]);

onMounted(async () => {
	relatedTerariumArtifacts.value = await getRelatedArtifacts(
		props.model.id,
		ProvenanceType.ModelRevision
	);
});
</script>

<style scoped>
section {
	margin-left: 1rem;
}

.p-message.p-message-info {
	max-width: 70rem;
}

.p-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	border-color: var(--primary-color);
	border-radius: var(--border-radius);
	border-width: 0px 0px 0px 6px;
	color: var(--text-color-primary);
}

table th {
	padding-right: 2rem;
	font-family: var(--font-family);
	font-weight: 500;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	text-align: left;
}

table td {
	padding-right: 50px;
	font-family: var(--font-family);
	font-weight: 400;
	font-size: var(--font-body-small);
}

table tr > td:empty:before {
	content: '--';
}

table .framework {
	text-transform: capitalize;
}
</style>
