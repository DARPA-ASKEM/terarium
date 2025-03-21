<template>
	<div class="chart-settings-panel-anchor">
		<transition>
			<div class="chart-settings-panel" v-if="activeSettings !== null">
				<header :class="{ shadow: false }">
					<Button :icon="`pi pi-times`" @click="$emit('close')" text rounded size="large" />
					<h4 class="line-wrap">{{ activeSettings.name }}</h4>
				</header>
				<div class="content items-wrapper">
					<section v-if="chartAnnotations !== undefined" class="annotation-items">
						<h5>Annotations</h5>
						<tera-input-text
							v-model="generateAnnotationQuery"
							:icon="'pi pi-sparkles'"
							:placeholder="'What do you want to annotate?'"
							:disabled="!!isGeneratingAnnotation"
							@keyup.enter="createAnnotationDebounced"
							@keyup.esc="cancelGenerateAnnotation"
							class="annotation-input"
						/>
						<div v-for="annotation in chartAnnotations" :key="annotation.id" class="annotation-item">
							{{ annotation.description }}
							<span class="btn-wrapper">
								<Button icon="pi pi-trash" rounded text @click="$emit('delete-annotation', annotation.id)" />
							</span>
						</div>
						<Divider />
					</section>
					<section class="items-wrapper">
						<h5>Options</h5>
						<tera-checkbox label="Use log scale" :model-value="useLog" @update:model-value="toggleLogScale($event)" />
						<tera-checkbox
							label="Hide in node"
							:model-value="isHiddenInNode"
							@update:model-value="toggleHideInNode($event)"
						/>
						<Divider />
					</section>
					<section v-if="isColorSelectionEnabled">
						<h5 class="mb-3">Color Selection</h5>
						<input type="color" :value="activeSettings?.primaryColor ?? ''" @change="onColorChange($event)" />
						<Divider />
					</section>
					<section v-if="activeSettings?.type === ChartSettingType.VARIABLE_COMPARISON" class="items-wrapper">
						<h5>Comparison method</h5>
						<div>
							<RadioButton
								:model-value="smallMultiplesRadioValue"
								@update:model-value="onSmallMultiplesRadioButtonChange"
								inputId="all-charts"
								value="all-charts"
							/>
							<label for="all-charts" class="ml-2">All in one chart</label>
						</div>
						<div>
							<RadioButton
								:model-value="smallMultiplesRadioValue"
								@update:model-value="onSmallMultiplesRadioButtonChange"
								inputId="small-multiples"
								value="small-multiples"
							/>
							<label for="small-multiples" class="ml-2">Small multiples</label>
						</div>
						<div class="pl-5 items-wrapper">
							<tera-checkbox
								label="Same Y axis for all"
								:disabled="!comparisonSettings?.smallMultiples"
								:model-value="isShareYAxis"
								@update:model-value="toggleShareYAxis($event)"
							/>
							<tera-checkbox
								label="Show before and after"
								:disabled="!comparisonSettings?.smallMultiples"
								:model-value="showBeforeAfter"
								@update:model-value="toggleShowBeforeAfter($event)"
							/>
						</div>
						<Divider />
						<h5>Normalize</h5>
						<tera-checkbox
							label="Normalize data by total strata population"
							:model-value="normalizeData"
							@update:model-value="toggleNormalizeData($event)"
						/>
						<slot v-if="normalizeData" name="normalize-content"></slot>
						<Divider />
						<h5>Color Selection</h5>
						<tera-chart-settings-item
							class="tera-chart-settings-item"
							v-for="v of variables"
							:key="v.id"
							:settings="v"
							:areButtonsEnabled="false"
						>
							<template #main>
								<input type="color" :value="v.primaryColor ?? ''" @change="onComparisonChange(v.name, $event)" />
							</template>
						</tera-chart-settings-item>
						<Divider />
					</section>
					<section v-if="isChartLabelsOptionEnabled" class="items-wrapper">
						<h5>Chart Labels</h5>
						<template v-if="activeSettings.type !== ChartSettingType.SENSITIVITY">
							<tera-input-text
								class="chart-label-input"
								label="Title"
								placeholder="No title"
								v-model="chartLabelTitle"
								@blur="() => onChartLabelInputBlur('title')"
							/>
							<tera-input-text
								class="chart-label-input"
								label="X axis"
								placeholder="No x axis"
								v-model="chartLabelXAxis"
								@blur="() => onChartLabelInputBlur('xAxisTitle')"
							/>
							<tera-input-text
								class="chart-label-input"
								label="Y axis"
								placeholder="No y axis"
								v-model="chartLabelYAxis"
								@blur="() => onChartLabelInputBlur('yAxisTitle')"
							/>
						</template>
						<tera-input-number
							class="chart-label-input"
							label="Font size"
							v-model="chartLabelFontSize"
							@focusout="() => onChartLabelInputBlur('fontSize')"
						/>
						<Divider />
					</section>
				</div>
			</div>
		</transition>
	</div>
</template>

<script setup lang="ts">
import _, { cloneDeep } from 'lodash';
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';
import RadioButton from 'primevue/radiobutton';
import { ChartSetting, ChartSettingType, ChartSettingComparison, ChartLabelOptions } from '@/types/common';
import { ChartAnnotation, ChartAnnotationType } from '@/types/Types';
import Divider from 'primevue/divider';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import teraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import { getComparisonVariableColors } from '@/services/chart-settings';
import { DEFAULT_FONT_SIZE } from '@/services/charts';
import { getChartAnnotationType } from '@/services/chart-annotation';

const props = defineProps<{
	activeSettings: ChartSetting | null;
	annotations?: ChartAnnotation[];
	comparisonSelectedOptions?: { [settingId: string]: string[] };
	/**
	 * We receives generateAnnotation as a functor from the parent to access the parent scope directly. This allows us to utilize dependencies defined in the parent component without passing them all as props, which can be cumbersome.
	 * Additionally, it enables us to handle post-generation actions (like resetting loading state or clearing input) after function completion.
	 * @param setting ChartSetting
	 * @param query llm query to generate annotation
	 */
	generateAnnotation?: (setting: ChartSetting, query: string) => Promise<ChartAnnotation | null>;

	/**
	 * Getter function to get chart labels for the active setting.
	 */
	getChartLabels?: (setting: ChartSetting) => ChartLabelOptions;
}>();

const emit = defineEmits(['close', 'update-settings', 'delete-annotation', 'create-annotation']);

// Log scale
const useLog = computed<boolean>(() => props.activeSettings?.scale === 'log');
const toggleLogScale = (useLogScale: boolean) => {
	emit('update-settings', { scale: useLogScale ? 'log' : '' });
};
// Hide in node
const isHiddenInNode = computed<boolean>(() => !!props.activeSettings?.hideInNode);
const toggleHideInNode = (hideInNode: boolean) => {
	emit('update-settings', { hideInNode: !!hideInNode });
};

// === Settings for comparison method ===
const comparisonSettings = computed(() => props.activeSettings as ChartSettingComparison | null);
// Small multiples
const smallMultiplesRadioValue = computed(() =>
	comparisonSettings.value?.smallMultiples ? 'small-multiples' : 'all-charts'
);
const onSmallMultiplesRadioButtonChange = (value: 'all-charts' | 'small-multiples') => {
	emit('update-settings', { smallMultiples: value === 'small-multiples' });
};
// Share Y axis
const isShareYAxis = computed(() => Boolean(comparisonSettings.value?.shareYAxis));
const toggleShareYAxis = (value: boolean) => emit('update-settings', { shareYAxis: value });
// Show before and after
const showBeforeAfter = computed(() => Boolean(comparisonSettings.value?.showBeforeAfter));
const toggleShowBeforeAfter = (value: boolean) => emit('update-settings', { showBeforeAfter: value });

type Items = { name: string; id: string; primaryColor: string };

const variables = computed(() => {
	const items: Items[] = [];
	if (!props.activeSettings) return items;
	const activeSettings = cloneDeep(props.activeSettings as ChartSettingComparison | null);
	if (activeSettings?.type !== ChartSettingType.VARIABLE_COMPARISON) return [];
	activeSettings.selectedVariables.forEach((value) => {
		const item = {
			name: value,
			id: activeSettings.id,
			primaryColor: getComparisonVariableColors(activeSettings)[value] ?? ''
		};
		items.push(item);
	});
	return items;
});

const onComparisonChange = (name, event) => {
	const activeSettings = cloneDeep(props.activeSettings as ChartSettingComparison | null);
	if (activeSettings?.type === ChartSettingType.VARIABLE_COMPARISON) {
		if (!activeSettings.variableColors) activeSettings.variableColors = {};
		activeSettings.variableColors[name] = event.target?.value;
		emit('update-settings', { variableColors: activeSettings.variableColors });
	}
};
// ======================================

// Normalize
const normalizeData = computed(() => Boolean(comparisonSettings.value?.normalize));
const toggleNormalizeData = (value: boolean) => emit('update-settings', { normalize: value });

// Primary color
const isColorSelectionEnabled = computed(() => {
	const type = props.activeSettings?.type;
	if (type) {
		return ![ChartSettingType.ERROR_DISTRIBUTION, ChartSettingType.VARIABLE_COMPARISON].includes(type);
	}
	return false;
});
const onColorChange = (event) => {
	emit('update-settings', { primaryColor: event.target?.value });
};

// ========== Chart Labels =========
// Note: Error chart isn't supported for chart labels configuration yet. Some refactoring needed for the error charts to support chart labels.
const ChartLabelsSupportedTypes = [
	ChartSettingType.VARIABLE,
	ChartSettingType.VARIABLE_OBSERVABLE,
	ChartSettingType.VARIABLE_COMPARISON,
	ChartSettingType.VARIABLE_ENSEMBLE,
	ChartSettingType.DISTRIBUTION_COMPARISON,
	ChartSettingType.INTERVENTION,
	ChartSettingType.SENSITIVITY
];
const isChartLabelsOptionEnabled = computed(() => {
	if (!props.activeSettings) return false;
	return ChartLabelsSupportedTypes.includes(props.activeSettings.type);
});

const chartLabelTitle = ref<string>('');
const chartLabelXAxis = ref<string>('');
const chartLabelYAxis = ref<string>('');
const chartLabelFontSize = ref<number>(DEFAULT_FONT_SIZE);
const chartLabelsFromSettings = computed(
	() =>
		props.activeSettings && { ...props.getChartLabels?.(props.activeSettings), fontSize: props.activeSettings.fontSize }
);
watch(
	() => props.activeSettings,
	(newVal, oldVal) => {
		// If the active setting is the same, do nothing
		if (newVal?.id === oldVal?.id) return;
		// Init ref values with the values from the settings when the active setting changes
		chartLabelTitle.value = chartLabelsFromSettings.value?.title ?? '';
		chartLabelXAxis.value = chartLabelsFromSettings.value?.xAxisTitle ?? '';
		chartLabelYAxis.value = chartLabelsFromSettings.value?.yAxisTitle ?? '';
		chartLabelFontSize.value = chartLabelsFromSettings.value?.fontSize ?? DEFAULT_FONT_SIZE;
	},
	{ immediate: true }
);

const CHART_LABEL_UPDATE_DELAY = 1500;
const updateLabelSettings = _.debounce(() => {
	const existing = chartLabelsFromSettings.value;
	const updated = {
		title: chartLabelTitle.value,
		xAxisTitle: chartLabelXAxis.value,
		yAxisTitle: chartLabelYAxis.value,
		fontSize: chartLabelFontSize.value
	};
	if (_.isEqual(existing, updated)) return;
	emit('update-settings', updated);
}, CHART_LABEL_UPDATE_DELAY);

const onChartLabelInputBlur = (field: 'title' | 'xAxisTitle' | 'yAxisTitle' | 'fontSize') => {
	const valRef = {
		title: chartLabelTitle,
		xAxisTitle: chartLabelXAxis,
		yAxisTitle: chartLabelYAxis,
		fontSize: chartLabelFontSize
	}[field];
	if (!valRef.value) {
		// Replace empty input with default value on blur
		const setting = _.clone(props.activeSettings) as ChartSetting;
		setting[field] = undefined;
		const defaultValue = field === 'fontSize' ? DEFAULT_FONT_SIZE : props.getChartLabels?.(setting)?.[field];
		valRef.value = defaultValue ?? '';
	}
	updateLabelSettings();
};

watch([chartLabelTitle, chartLabelXAxis, chartLabelYAxis, chartLabelFontSize], updateLabelSettings, {
	immediate: true
});
// =================================

// ========== Chart Annotations =========
const chartAnnotations = computed(() => {
	if (props.annotations === undefined || !props.activeSettings) {
		return undefined;
	}
	const chartAnnotationType = getChartAnnotationType(props.activeSettings);
	return props.annotations
		.filter((annotation) => annotation.chartId === props.activeSettings?.id)
		.filter(
			(a) => (a.chartType ?? ChartAnnotationType.ForecastChart) /** fallback to default type */ === chartAnnotationType
		);
});
const isGeneratingAnnotation = ref(false);
const generateAnnotationQuery = ref<string>('');

const createAnnotation = async () => {
	if (props.generateAnnotation === undefined || props.activeSettings === null) {
		return;
	}
	isGeneratingAnnotation.value = true;
	const newAnnotation = await props.generateAnnotation(props.activeSettings, generateAnnotationQuery.value);
	isGeneratingAnnotation.value = false;
	generateAnnotationQuery.value = '';
	emit('create-annotation', newAnnotation);
};
// Note: For some reason, @keyup.enter event on <tera-input-text> is fired twice. Let's introduce a debounced function to make sure the function is called only once.
const createAnnotationDebounced = _.debounce(createAnnotation, 100);

const cancelGenerateAnnotation = () => {
	generateAnnotationQuery.value = '';
};
// ======================================
</script>

<style scoped>
.tera-chart-settings-item {
	background: var(--surface-100);
}
.chart-settings-panel-anchor {
	position: fixed;
	top: 7.5rem;
	right: var(--gap-8);
	width: 360px;
	height: calc(100% - 7.5rem);
	pointer-events: none;
	overflow: hidden;
}
.chart-settings-panel {
	position: relative;
	overflow-y: auto;
	top: 0;
	z-index: 3;
	margin-top: 50px;
	height: calc(100% - 51px); /* 51px = margin-top + border */
	width: 100%;
	background: #fff;
	border-top-left-radius: var(--border-radius-bigger);
	left: 2px;
	border: solid 1px var(--surface-border-light);
	pointer-events: all;
	&.v-enter-active,
	&.v-leave-active {
		transition: left 0.15s ease-out;
		left: 30%;
	}
	&.v-enter-from,
	&.v-leave-to {
		left: 100%;
	}

	header {
		position: sticky;
		top: 0;
		z-index: 3;
		display: flex;
		align-items: center;
		flex-direction: row-reverse;
		justify-content: space-between;
		padding: var(--gap-2);
		padding-left: var(--gap-4);
		gap: var(--gap-4);
		background-color: rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(3px);
		border-top-left-radius: var(--border-radius-bigger);
		&.shadow {
			box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.1);
		}
		button {
			height: 2.5rem;
		}
		.line-wrap {
			white-space: wrap;
			word-wrap: break-all;
			overflow: hidden;
			text-overflow: ellipsis;
			width: 100%;
		}
	}
	.content {
		padding: var(--gap-4);
		background: var(--surface-0);
	}

	.items-wrapper {
		display: flex;
		flex-direction: column;
		gap: var(--gap-2);
	}

	.annotation-input:deep(main) {
		padding: var(--gap-2-5) var(--gap-2);
	}
	.annotation-items {
		display: flex;
		padding-bottom: var(--gap-1);
		flex-direction: column;
		gap: var(--gap-2);

		.annotation-item {
			position: relative;
			padding: var(--gap-3);
			padding-left: var(--gap-4);
			padding-right: var(--gap-9);
			background: var(--surface-50);
			border-left: 4px solid var(--gray-600);
		}
		.btn-wrapper {
			position: absolute;
			top: 0;
			right: var(--gap-2);
			display: flex;
			flex-direction: column;
			justify-content: center;
			height: 100%;
		}
	}
	.chart-label-input {
		display: flex;
		flex-direction: column;
	}
	.chart-label-input :deep(label) {
		align-self: auto;
	}
}
</style>
