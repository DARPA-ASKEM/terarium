<template>
	<tbody class="p-datatable-tbody">
		<tr v-for="({ name }, i) in modelConfigurations" :key="i">
			<!--TODO: This td is a placeholder, row selection doesn't work-->
			<td class="p-selection-column p-frozen-column">
				<div class="p-checkbox p-component">
					<div class="p-hidden-accessible">
						<input type="checkbox" tabindex="0" aria-label="Row Unselected" />
					</div>
					<div class="p-checkbox-box p-component">
						<span class="p-checkbox-icon"></span>
					</div>
				</div>
			</td>
			<td
				class="p-frozen-column second-frozen"
				tabindex="0"
				@keyup.enter="
					modelConfigInputValue = cloneDeep(modelConfigurations[i].name)
					// cellEditStates[i].name = true;
				"
				@click="
					modelConfigInputValue = cloneDeep(modelConfigurations[i].name)
					// cellEditStates[i].name = true;
				"
			>
				<InputText
					v-if="cellEditStates[i]?.name"
					v-model.lazy="modelConfigInputValue"
					v-focus
					@focusout="emit('update-name', i)"
					@keyup.stop.enter="emit('update-name', i)"
					class="cell-input"
				/>
				<span v-else class="editable-cell">
					{{ name }}
				</span>
			</td>
			<td v-for="(id, j) in [...baseStates, ...baseTransitions]" :key="j">
				<section class="editable-cell" @click="openValueModal(id, i)">
					<span>{{ id }}<i class="pi pi-table" /></span>
					<Button
						class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
						icon="pi pi-ellipsis-v"
					/>
				</section>
			</td>
		</tr>
	</tbody>
	<Teleport to="body">
		<tera-modal
			v-if="openValueConfig"
			@modal-mask-clicked="openValueConfig = false"
			@model-submit="openValueConfig = false"
		>
			<template #header>
				<h4>{{ modalVal.id }}</h4>
				<span>Configure the matrix values</span>
				<div class="flex align-items-center">
					<Checkbox
						inputId="matrixShouldEval"
						v-model="matrixShouldEval"
						:binary="true"
						label="Evaluate expressions?"
					/>
					<label for="matrixShouldEval" class="ml-2"> Evaluate Expressions? </label>
				</div>
			</template>
			<template #default>
				<!-- TODO: Implement value tabs for the modal once we are ready
					<TabView v-model:activeIndex="activeIndex">
					<TabPanel v-for="(extraction, i) in extractions" :key="i">
						<template #header>
							<span>{{ extraction.name }}</span>
						</template>
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" :key="'name' + i" v-model="extraction.name" />
						</div>
						<div>
							<label for="name">Matrix</label>
							<tera-stratified-value-matrix
								:model-configuration="modelConfigurations[modalVal.configIndex]"
								:id="modalVal.id"
								:stratified-model-type="stratifiedModelType"
								:node-type="modalVal.nodeType"
							/>
						</div>
					</TabPanel>
				</TabView> -->
				<tera-stratified-value-matrix
					:model-configuration="modelConfigurations[modalVal.configIndex]"
					:id="modalVal.id"
					:stratified-model-type="stratifiedModelType"
					:node-type="modalVal.nodeType"
					:should-eval="matrixShouldEval"
				/>
			</template>
			<template #footer>
				<Button label="OK" @click="openValueConfig = false" />
				<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { cloneDeep } from 'lodash';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
// import TabPanel from 'primevue/tabpanel';
// import TabView from 'primevue/tabview';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import { ModelConfiguration } from '@/types/Types';
import TeraStratifiedValueMatrix from '@/components/models/tera-stratified-value-matrix.vue';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
import { FeatureConfig } from '@/types/common';

const props = defineProps<{
	featureConfig: FeatureConfig;
	modelConfigurations: ModelConfiguration[];
	stratifiedModelType: StratifiedModelType;
	calibrationConfig?: boolean;
	tableHeaders: { name: string; colspan: number }[];
	cellEditStates: any[];
	baseStates: any;
	baseTransitions: any;
}>();

const emit = defineEmits([
	'new-model-configuration',
	'update-name',
	'update-value',
	'enter-value-cell'
]);

const modelConfigInputValue = ref<string>('');
// const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });
const matrixShouldEval = ref(true);

const activeIndex = ref(0);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

function openValueModal(id: string, configIndex: number) {
	if (!props.featureConfig.isPreview) {
		const nodeType = props.baseStates.includes(id) ? NodeType.State : NodeType.Transition;

		activeIndex.value = 0;
		openValueConfig.value = true;
		modalVal.value = { id, configIndex, nodeType };
	}
}
</script>

<style scoped>
.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

/** TODO: Apply to all tables in theme or create second table rules?  */
.p-datatable-thead th {
	text-transform: none !important;
	color: var(--text-color-primary) !important;
	font-size: var(--font-size-small) !important;
	padding-left: 1rem !important;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.cell-menu {
	visibility: hidden;
}

.cell-input {
	width: calc(100%);
	height: 4rem;
}

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
}

td:has(.cell-input) {
	padding: 2px !important;
	max-width: 4rem;
}

.p-datatable:deep(td) {
	cursor: pointer;
}

.p-datatable:deep(td:focus) {
	background-color: var(--primary-color-lighter);
}

.p-frozen-column {
	left: 0px;
	white-space: nowrap;
}

.p-frozen-column,
th {
	background: transparent;
}

.second-frozen {
	left: 48px;
}

th:hover .cell-menu,
td:hover .cell-menu {
	visibility: visible;
}

.p-tabview {
	display: flex;
	gap: 1rem;
	margin-bottom: 1rem;
	justify-content: space-between;
}

.p-tabview:deep(> *) {
	width: 50vw;
	height: 65vh;
	overflow: auto;
}

.p-tabview:deep(.p-tabview-nav) {
	flex-direction: column;
}

.p-tabview:deep(label) {
	display: block;
	font-size: var(--font-caption);
	margin-bottom: 0.25rem;
}

.p-tabview:deep(.p-tabview-nav-container, .p-tabview-nav-content) {
	width: 30%;
}

.p-tabview:deep(.p-tabview-panels) {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	background-color: var(--surface-ground);
}

.p-tabview:deep(.p-tabview-panel) {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.p-tabview:deep(.p-tabview-nav li) {
	border-left: 3px solid transparent;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-header:nth-last-child(n + 3)) {
	border-bottom: 1px solid var(--surface-border-light);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight) {
	border-left: 3px solid var(--primary-color);
	background: var(--surface-highlight);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight .p-tabview-nav-link) {
	background: none;
}

.p-tabview:deep(.p-inputtext) {
	width: 100%;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-ink-bar) {
	display: none;
}

.distribution-cell {
	display: flex;
	flex-direction: column;
}

.distribution-range {
	white-space: nowrap;
}

.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}

.capitalize {
	text-transform: capitalize !important;
	font-size: var(--font-body-medium) !important;
}
</style>
