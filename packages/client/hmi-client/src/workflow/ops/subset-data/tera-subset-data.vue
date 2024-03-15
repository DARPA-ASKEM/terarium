<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="SubsetDataTabs.Wizard">
			<tera-drilldown-section>
				<!-- <InputText placeholder="What do you want to do?" /> -->
				<h3>Select geo-boundaries</h3>
				<span>
					<label>Latitude</label>
					<InputText v-model="latitudeStart" placeholder="Start" />
					<InputText v-model="latitudeEnd" placeholder="End" />
				</span>
				<span>
					<label>Longitude</label>
					<InputText v-model="latitudeStart" placeholder="Start" />
					<InputText v-model="latitudeEnd" placeholder="End" />
				</span>
				<code>
					selectSpatialDomain(['{{ latitudeStart }}', '{{ latitudeEnd }}', '{{ longitudeStart }}',
					'{{ longitudeEnd }}'])
				</code>
				<span>
					<Checkbox v-model="isSpatialSkipping" binary />
					<label>Spatial skipping</label>
				</span>
				<div class="number-entry">
					<label class="float-label">Keep every nth datapoint</label>
					<InputNumber v-model="spatialSkipping" :disabled="!isSpatialSkipping" />
				</div>
				<h3>Select temporal slice</h3>
				<span>
					<div>
						<label class="float-label">From</label>
						<Calender v-model="fromDate" showIcon />
					</div>
					<div>
						<label class="float-label">To</label>
						<Calender v-model="toDate" showIcon />
					</div>
				</span>
				<span>
					<Checkbox v-model="isTimeSkipping" binary />
					<label>Time skipping</label>
				</span>
				<div class="number-entry">
					<label class="float-label">Keep every nth time slice</label>
					<InputNumber v-model="timeSkipping" :disabled="!isTimeSkipping" />
				</div>
				<template #footer>
					<Button label="Run" icon="pi pi-play" outlined severity="secondary" />
				</template>
			</tera-drilldown-section>
		</div>
		<div :tabName="SubsetDataTabs.Notebook"></div>
		<template #preview>
			<tera-drilldown-preview
				title="Output"
				:options="outputs"
				@update:selection="onSelection"
				v-model:output="selectedOutputId"
				is-selectable
			>
				<template #footer>
					<Button label="Save as new dataset" outlined severity="secondary" />
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, computed } from 'vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Button from 'primevue/button';
import Calender from 'primevue/calendar';
import Checkbox from 'primevue/checkbox';
import { SubsetDataOperationState } from './subset-data-operation';

const props = defineProps<{
	node: WorkflowNode<SubsetDataOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'select-output', 'close']);

enum SubsetDataTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

const latitudeStart = ref('');
const latitudeEnd = ref('');
const longitudeStart = ref('');
const longitudeEnd = ref('');

const isSpatialSkipping = ref(true);
const spatialSkipping = ref(0);

const isTimeSkipping = ref(true);
const timeSkipping = ref(0);

const fromDate = ref('');
const toDate = ref('');

const selectedOutputId = ref<string>(props.node.active ?? '');

const outputs = computed(() => {
	if (isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const onSelection = (id: string) => {
	emit('select-output', id);
};
</script>

<style scoped>
span {
	display: flex;
	align-items: center;
	gap: var(--gap);

	& > label {
		width: 8rem;
	}
}

:deep(main) {
	gap: var(--gap);
}

code {
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	padding: var(--gap-small);
}

.float-label {
	font-size: var(--font-caption);
}

.number-entry {
	width: 14rem;
}
</style>
