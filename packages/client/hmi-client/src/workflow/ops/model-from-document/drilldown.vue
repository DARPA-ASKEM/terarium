<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section :is-loading="assetLoading">
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
					<span>{{ equation.asset.text }}</span>
				</tera-asset-block>
				<template #footer>
					<Button label="Run" @click="onRun"></Button>
				</template>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview> </tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { AssetBlock, WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { onMounted, ref, watch } from 'vue';
import { getDocumentAsset, getEquationFromImageUrl } from '@/services/document-assets';
import { DocumentAsset, ExtractionAssetType } from '@/types/Types';
import { cloneDeep, isEmpty } from 'lodash';
import Image from 'primevue/image';
import { equationsToAMR } from '@/services/knowledge';
import Button from 'primevue/button';
import { EquationFromImageBlock, ModelFromDocumentState } from './model-from-document-operation';

const emit = defineEmits(['close', 'update-state']);
const props = defineProps<{
	node: WorkflowNode<ModelFromDocumentState>;
}>();

const clonedState = ref(cloneDeep(props.node.state));
const document = ref<DocumentAsset | null>();
const assetLoading = ref(false);

onMounted(async () => {
	const documentId = props.node.inputs?.[0]?.value?.[0];
	assetLoading.value = true;
	if (documentId) {
		document.value = await getDocumentAsset(documentId);
		const equations = document.value?.assets?.filter(
			(a) => a.assetType === ExtractionAssetType.Equation
		);

		const state = cloneDeep(props.node.state);

		// equations that not been run in image -> equation
		const nonRunEquations = equations?.filter((e) => {
			const foundEquation = state.equations.find((eq) => eq.asset.fileName === e.fileName);
			if (!foundEquation) return true;

			return !foundEquation.asset.text;
		});

		if (isEmpty(nonRunEquations)) {
			assetLoading.value = false;
			return;
		}
		const promises = nonRunEquations?.map(async (e, i) => {
			const equationText = (await getEquationFromImageUrl(documentId, e.fileName)) ?? '';
			const equationBlock: EquationFromImageBlock = {
				...e,
				text: equationText
			};

			const assetBlock: AssetBlock<EquationFromImageBlock> = {
				name: `Equation ${i + 1}`,
				includeInProcess: true,
				asset: equationBlock
			};

			return assetBlock;
		});

		if (!promises) return;

		const newEquations = await Promise.all(promises);
		state.equations = state.equations.concat(newEquations);
		emit('update-state', state);
	}
	assetLoading.value = false;
});

function onUpdateInclude(asset: AssetBlock<EquationFromImageBlock>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update-state', clonedState.value);
}

async function onRun() {
	const equations = clonedState.value.equations
		.filter((e) => e.includeInProcess && e.asset.text)
		.map((e) => e.asset.text);
	const res = await equationsToAMR('mathml', equations, 'petrinet');
	console.log(res);
}

watch(
	() => props.node.state,
	() => {
		clonedState.value = cloneDeep(props.node.state);
	},
	{ deep: true }
);
</script>

<style scoped></style>
