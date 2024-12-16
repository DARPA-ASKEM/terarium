<template>
	<Accordion multiple :active-index="currentActiveIndicies">
		<AccordionTab v-if="!isEmpty(clonedState.equations)">
			<template #header>
				<header>
					Equation Images
					<span class="sub-text">({{ countEquations }} selected)</span>
				</header>
			</template>
			<tera-asset-block
				v-for="(equation, i) in clonedState.equations"
				:key="i"
				:is-included="equation.includeInProcess"
				@update:is-included="onUpdateInclude(equation)"
			>
				<template #header>
					<h5>{{ equation.name }}</h5>
				</template>
				<Image id="img" :src="equation.asset.metadata?.url" :alt="''" preview />
			</tera-asset-block>
		</AccordionTab>
		<AccordionTab v-if="!isEmpty(clonedState.figures)">
			<template #header>
				<header>
					Figure Images
					<span class="sub-text">({{ countFigures }} selected)</span>
				</header>
			</template>
			<tera-asset-block
				v-for="(figure, i) in clonedState.figures"
				:key="i"
				:is-included="figure.includeInProcess"
				@update:is-included="onUpdateInclude(figure)"
			>
				<template #header>
					<h5>{{ figure.name }}</h5>
				</template>
				<Image id="img" :src="figure.asset.metadata?.url" :alt="''" preview />
			</tera-asset-block>
		</AccordionTab>
		<AccordionTab v-if="!isEmpty(clonedState.tables)">
			<template #header>
				<header>
					Table Images
					<span class="sub-text">({{ countTables }} selected)</span>
				</header>
			</template>
			<tera-asset-block
				v-for="(table, i) in clonedState.tables"
				:key="i"
				:is-included="table.includeInProcess"
				@update:is-included="onUpdateInclude(table)"
			>
				<template #header>
					<h5>{{ table.name }}</h5>
				</template>
				<Image id="img" :src="table.asset.metadata?.url" :alt="''" preview />
			</tera-asset-block>
		</AccordionTab>
		<AccordionTab v-if="!isEmpty(document?.text)">
			<template #header>
				<header>
					Text <span class="sub-text">({{ countWords }} words)</span>
				</header>
			</template>
			<p>{{ document?.text }}</p>
		</AccordionTab>
	</Accordion>
</template>
<script setup lang="ts">
import type { DocumentAsset, DocumentExtraction } from '@/types/Types';
import { DocumentOperationState } from '@/components/workflow/ops/document/document-operation';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { computed, ref } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import { AssetBlock } from '@/types/workflow';
import Image from 'primevue/image';
import TeraAssetBlock from '../widgets/tera-asset-block.vue';

const props = defineProps<{
	state: DocumentOperationState;
	document: DocumentAsset | null | undefined;
}>();

const emit = defineEmits(['update']);

const currentActiveIndicies = ref([0, 1, 2]);

const clonedState = ref(cloneDeep(props.state));

const countEquations = computed(() => {
	if (!clonedState.value.equations) return '';
	const selected = clonedState.value.equations.filter((e) => e.includeInProcess).length;
	return `${selected}/${clonedState.value.equations.length}`;
});

const countFigures = computed(() => {
	if (!clonedState.value.equations) return '';
	const selected = clonedState.value.figures.filter((f) => f.includeInProcess).length;
	return `${selected}/${clonedState.value.figures.length}`;
});

const countTables = computed(() => {
	if (!clonedState.value.equations) return '';
	const selected = clonedState.value.tables.filter((t) => t.includeInProcess).length;
	return `${selected}/${clonedState.value.tables.length}`;
});

const countWords = computed(() => {
	if (!props.document) return 0;
	return props.document.text?.split(' ').filter((word) => word !== '').length ?? 0;
});

function onUpdateInclude(asset: AssetBlock<DocumentExtraction>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update', clonedState.value);
}
</script>

<style scoped>
:deep(.p-accordion-content > :not(:last-child)) {
	margin-bottom: var(--gap-2);
}

.sub-text {
	font-size: var(--font-caption);
}
</style>
