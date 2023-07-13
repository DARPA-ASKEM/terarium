<template>
	<!--TODO: Once we implement the unique border design remove the p-datatable-gridlines class and make a custom series of classes to support the borders we want-->
	<div
		v-if="!isEmpty(configurations) && !isEmpty(tableHeaders)"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header model-configuration"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead class="p-datatable-thead">
					<tr>
						<th class="p-frozen-column"></th>
						<th class="p-frozen-column second-frozen"></th>
						<th v-for="({ name, colspan }, i) in tableHeaders" :colspan="colspan" :key="i">
							{{ name }}
						</th>
					</tr>
					<tr>
						<th class="p-frozen-column" />
						<th class="p-frozen-column second-frozen">Select all</th>
						<th
							v-for="({ target }, i) in configurations[0]?.semantics?.ode.rates"
							:header="target"
							:key="i"
						>
							{{ target }}
						</th>
						<th
							v-for="({ target }, i) in configurations[0]?.semantics?.ode.initials"
							:header="target"
							:key="i"
						>
							{{ target }}
						</th>
						<th v-for="(key, i) in runConfigsKeys" :header="key" :key="i">
							{{ key }}
							<div>
								{{ getMax(key) }}
							</div>
							<div>
								{{ getMin(key) }}
							</div>
						</th>
						<!--TODO: Insert new th loops for time and observables here-->
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="({ configuration, name }, i) in modelConfigs" :key="i">
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
						<td class="p-frozen-column second-frozen">
							<span>
								{{ name }}
							</span>
						</td>
						<td
							v-for="(rate, j) of configuration?.semantics?.ode.rates"
							class="p-editable-column"
							:key="j"
						>
							<section class="editable-cell">
								<span>{{ rate.expression }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
								/>
							</section>
						</td>
						<td v-for="(initial, j) of configuration?.semantics?.ode.initials" :key="j">
							<section class="editable-cell">
								<span>{{ initial.expression }}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
								/>
							</section>
						</td>
						<td
							v-for="(key, j) of runConfigsKeys"
							:key="j"
							:style="{ background: getGradientStr(key) }"
						>
							<section class="editable-cell">
								<span>{{
									modelConfigs[i].configuration.semantics.ode.parameters[j]?.value ?? 0
								}}</span>
								<Button
									class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small cell-menu"
									icon="pi pi-ellipsis-v"
								/>
							</section>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>
<script setup lang="ts">
import { watch, ref, computed, onMounted } from 'vue';
import Button from 'primevue/button';
import { ModelConfiguration, Model } from '@/types/Types';
import { getModelConfigurations } from '@/services/model';

const props = defineProps<{
	model: Model;
	calibrationConfig?: boolean;
	runConfigs: any;
}>();

const modelConfigs = ref<ModelConfiguration[]>([]);

// const selectedModelConfig = ref();
const extractions = ref<any[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigs.value?.map((m) => m.configuration) ?? []
);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => {
	if (configurations.value?.[0]?.semantics) {
		const headerNames = Object.keys(configurations.value[0]?.semantics.ode) ?? [];
		const result: { name: string; colspan: number }[] = [];

		for (let i = 0; i < headerNames.length; i++) {
			if (configurations.value?.[0]?.semantics?.ode[headerNames[i]]) {
				result.push({
					name: headerNames[i],
					colspan: configurations.value?.[0]?.semantics?.ode[headerNames[i]].length
				});
			}
		}
		return result;
	}
	return [];
});

async function initializeConfigSpace() {
	modelConfigs.value = [];
	modelConfigs.value = (await getModelConfigurations(props.model.id)) as ModelConfiguration[];

	extractions.value = ['Default'];
}

watch(
	() => props.model,
	() => initializeConfigSpace(),
	{ deep: true }
);

onMounted(() => {
	initializeConfigSpace();
});

const runConfigsKeys = computed(() => Object.keys(props.runConfigs));

function getMax(key) {
	const dataLen = props.runConfigs[key].length;
	return props.runConfigs[key][dataLen - 1];
}

function getMin(key) {
	return props.runConfigs[key][0];
}

function getGradientStr(key) {
	const dataLen = props.runConfigs[key].length;
	const interval = 0.1;
	const r = 255 - 27;
	const g = 255 - 128;
	const b = 255 - 115;
	const min = getMin(key);
	const max = getMax(key);
	let output = 'linear-gradient(90deg';
	for (let i = 0; i <= 1; i += interval) {
		const val = props.runConfigs[key][Math.floor((dataLen - 1) * i)];
		const valPercentage = Math.max((val - min) / (max - min), 0.001);
		output += `, rgba(${255 - Math.floor(r * valPercentage)}, ${
			255 - Math.floor(g * valPercentage)
		}, ${255 - Math.floor(b * valPercentage)}, 1) ${Math.floor(i * 100)}%`;
	}
	output += ')';
	return output;
}
</script>

<style scoped>
.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

.model-configuration {
	margin-bottom: 1rem;
}

.p-datatable-thead th {
	text-transform: capitalize;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.cell-menu {
	visibility: hidden;
}

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	min-width: 3rem;
}

.p-datatable:deep(td) {
	cursor: pointer;
}

.p-frozen-column {
	left: 0px;
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
	height: 50vh;
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
	width: 100%;
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
</style>
