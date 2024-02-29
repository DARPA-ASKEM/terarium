<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="Tabs.Wizard">
			<tera-drilldown-section>
				<Dropdown
					:editable="true"
					disabled
					class="input"
					v-model="questionString"
					type="text"
					:placeholder="kernelStatus ? 'Please wait...' : 'What do you want to do?'"
					@keydown.enter="submitQuestion"
				/>
				<Panel v-if="llmAnswer" header="Answer" toggleable>
					<template #togglericon="{ collapsed }">
						<i :class="collapsed ? 'pi pi-chevron-down' : 'pi pi-chevron-up'" />
					</template>
					<p>{{ llmAnswer }}</p>
				</Panel>
				<div class="p-datatable-wrapper">
					<table class="p-datatable-table p-datatable-scrollable-table">
						<thead class="p-datatable-thead">
							<tr>
								<th></th>
								<th v-for="model in modelsToCompare" :key="model.id" class="text-lg">
									{{ model.header.name }}
								</th>
							</tr>
						</thead>
						<tbody v-if="fields" class="p-datatable-tbody">
							<tr>
								<td class="field">Diagram</td>
								<td v-for="(model, index) in modelsToCompare" :key="index">
									<tera-model-diagram :model="model" :is-editable="false" is-preview />
								</td>
							</tr>
							<template v-for="field in fields" :key="field">
								<tr>
									<td class="field">{{ formatField(field) }}</td>
									<td v-for="(card, index) in modelCardsToCompare" :key="index">
										<template v-if="typeof card[field] === 'object'">
											<template v-for="(value, j) in Object.values(card[field])">
												<template v-if="Array.isArray(value)">
													{{ value.join(', ') }}
												</template>
												<div class="value" v-else :key="j">
													{{ value }}
												</div>
											</template>
										</template>
										<template v-if="Array.isArray(card[field])">
											{{ card[field].join(', ') }}
										</template>
									</td>
								</tr>
							</template>
						</tbody>
					</table>
				</div>
			</tera-drilldown-section>
		</div>
		<div :tabName="Tabs.Notebook"></div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import Panel from 'primevue/panel';
import Dropdown from 'primevue/dropdown';
import { WorkflowNode } from '@/types/workflow';
import { getModel } from '@/services/model';
import type { Model } from '@/types/Types';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import { compareModels } from '@/services/goLLM';
import { ModelComparisonOperationState } from './model-comparison-operation';

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const questionString = ref('');
const llmAnswer = ref('');
const kernelStatus = ref('');
const isKernelReady = ref(false);
const modelsToCompare = ref<Model[]>([]);
const modelCardsToCompare = computed(() =>
	modelsToCompare.value.map(({ metadata }) => metadata?.gollmCard)
);
const fields = computed(
	() =>
		[
			...new Set(modelCardsToCompare.value.reduce((acc, card) => acc.concat(Object.keys(card)), []))
		] as string[]
);
const cellWidth = computed(() => `${85 / modelsToCompare.value.length}vw`);

const kernelManager = new KernelSessionManager();

async function addModelForComparison(modelId: string) {
	const model = await getModel(modelId);
	if (model) modelsToCompare.value.push(model);
}

function formatField(field: string) {
	const result = field
		.replace(/([A-Z])/g, ' $1')
		.trim()
		.toLowerCase();
	return result.charAt(0).toUpperCase() + result.slice(1);
}

function submitQuestion() {
	logger.info(`WIP`);
}

// FIXME: Placeholder
const buildJupyterContext = () => {
	if (!isEmpty(modelsToCompare.value)) {
		logger.warn('Cannot build Jupyter context without a model');
		return null;
	}
	return {
		context: 'mira_model_edit',
		language: 'python3',
		context_info: {
			id: props.node.inputs?.[0].value?.[0] ?? ''
		}
	};
};

onMounted(async () => {
	props.node.inputs.forEach((input) => {
		if (input.status === 'connected') addModelForComparison(input.id);
	});

	// filter modelsToCompare to be a list of ids
	const modelIds = props.node.inputs
		.filter((input) => input.status === 'connected')
		.map((input) => input.value?.[0]);
	llmAnswer.value = (await compareModels(modelIds)) ?? '';

	try {
		const jupyterContext = buildJupyterContext();
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
			isKernelReady.value = true;
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
});

onUnmounted(() => {
	kernelManager.shutdown();
});
</script>

<style scoped>
table {
	& td,
	th {
		vertical-align: top;
		text-align: left;
		padding: 0 var(--gap) var(--gap) var(--gap-small);
		max-width: v-bind('cellWidth');
		overflow: auto;
		text-overflow: ellipsis;
	}

	& td {
		border-top: 1px solid var(--surface-border-light);
	}

	& td:first-child {
		width: 10%;
		padding: var(--gap-small) 0;
		font-weight: 600;
	}

	& td:not(:first-child) {
		padding: var(--gap-small);
		padding-right: var(--gap);
	}

	& .value {
		padding-bottom: 1rem;
	}
}

.input:deep(input) {
	background-image: url('@assets/svg/icons/message.svg');
	background-size: 1rem;
	background-position: 12px;
	background-repeat: no-repeat;
	text-indent: 30px;
}
</style>
