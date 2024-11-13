import _ from 'lodash';
import { mean, variance } from 'd3';
import { computed, ComputedRef, Ref } from 'vue';
import {
	applyForecastChartAnnotations,
	createErrorChart,
	createForecastChart,
	createHistogramChart,
	createInterventionChartMarkers,
	ForecastChartOptions
} from '@/services/charts';
import { flattenInterventionData } from '@/services/intervention-policy';
import { DataArray } from '@/services/models/simulation-service';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { Intervention, Model, ModelConfiguration } from '@/types/Types';
import { displayNumber } from '@/utils/number';
import { getUnitsFromModelParts, getVegaDateOptions } from '@/services/model';
import { CalibrateMap } from '@/services/calibrate-workflow';
import { modelVarToDatasetVar } from '@/components/workflow/ops/calibrate-ciemss/calibrate-utils';
import { useChartAnnotations } from './useChartAnnotations';

interface ChartData {
	result: DataArray;
	resultSummary: DataArray;
	pyciemssMap: Record<string, string>;
	translationMap: Record<string, string>;
}

/**
 * Composable to manage the creation and configuration of various types of charts used in operator nodes and drilldown.
 *
 * @param nodeId - The ID of the node.
 * @param model - The model reference.
 * @param modelConfig - The model configuration reference.
 * @param chartData - The chart data reference.
 * @param chartSize - The chart size reference.
 * @param interventions - Optional reference to interventions.
 *
 * @returns An object containing methods to create and manage different types of charts.
 */
export function useCharts(
	nodeId: string,
	model: Ref<Model | null>,
	modelConfig: Ref<ModelConfiguration | null>,
	chartData: Ref<ChartData | null>,
	chartSize: Ref<{ width: number; height: number }>,
	interventions?: Ref<Intervention[]> | null
) {
	// Setup annotations
	const { getChartAnnotationsByChartId, generateAndSaveForecastChartAnnotation } = useChartAnnotations(nodeId);

	const getUnit = (paramId: string) => {
		if (!model.value) return '';
		return getUnitsFromModelParts(model.value)[paramId] || '';
	};

	// Create options for forecast charts based on chart settings and model configuration
	const createForecastChartOptions = (setting: ChartSetting, translationMap: Record<string, string>) => {
		const variables = setting.selectedVariables;
		const dateOptions = getVegaDateOptions(model.value, modelConfig.value);
		const options: ForecastChartOptions = {
			title: '',
			legend: true,
			width: chartSize.value.width,
			height: chartSize.value.height,
			translationMap,
			xAxisTitle: getUnit('_time') || 'Time',
			yAxisTitle: _.uniq(variables.map(getUnit).filter((v) => !!v)).join(',') || '',
			dateOptions,
			colorscheme: ['#AAB3C6', '#1B8073']
		};

		let sampleLayerVariables = [
			`${chartData.value?.pyciemssMap[variables[0]]}:pre`,
			`${chartData.value?.pyciemssMap[variables[0]]}`
		];
		let statLayerVariables = [
			`${chartData.value?.pyciemssMap[variables[0]]}_mean:pre`,
			`${chartData.value?.pyciemssMap[variables[0]]}_mean`
		];

		if (setting.type === ChartSettingType.VARIABLE_COMPARISON) {
			statLayerVariables = variables.map((d) => `${chartData.value?.pyciemssMap[d]}_mean`);
			sampleLayerVariables = variables.map((d) => `${chartData.value?.pyciemssMap[d]}`);
			delete options.colorscheme;
		}
		return { statLayerVariables, sampleLayerVariables, options };
	};

	// Generate annotations for a chart
	const generateAnnotation = async (setting: ChartSetting, query: string) => {
		if (!chartData.value) return null;
		const { statLayerVariables, options } = createForecastChartOptions(setting, chartData.value.translationMap);
		return generateAndSaveForecastChartAnnotation(setting, query, 'timepoint_id', statLayerVariables, options);
	};

	const groupedInterventionOutputs = computed(() =>
		_.groupBy(flattenInterventionData(interventions?.value ?? []), 'appliedTo')
	);

	// Create intervention charts based on chart settings
	const useInterventionCharts = (chartSettings: ComputedRef<ChartSetting[]>, showSamples = false) => {
		const interventionCharts = computed(() => {
			const charts: Record<string, any> = {};
			if (!chartData.value) return charts;
			const { resultSummary, translationMap, result } = chartData.value;
			// intervention chart spec
			chartSettings.value.forEach((setting) => {
				const variable = setting.selectedVariables[0];
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(
					setting,
					translationMap
				);
				const forecastChart = createForecastChart(
					{
						data: showSamples ? result : [],
						variables: sampleLayerVariables,
						timeField: 'timepoint_id',
						groupField: 'sample_id'
					},
					{
						data: resultSummary,
						variables: statLayerVariables,
						timeField: 'timepoint_id'
					},
					null,
					options
				);
				// add intervention annotations (rules and text)
				forecastChart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[variable]));
				charts[setting.id] = forecastChart;
			});
			return charts;
		});
		return interventionCharts;
	};

	// Create variable charts based on chart settings
	const useVariableCharts = (
		chartSettings: ComputedRef<ChartSetting[]>,
		groundTruthData: ComputedRef<DataArray> | null,
		mapping: ComputedRef<CalibrateMap[]> | null
	) => {
		const variableCharts = computed(() => {
			const charts: Record<string, any> = {};
			if (!chartData.value) return charts;
			const { result, resultSummary, translationMap } = chartData.value;

			chartSettings.value.forEach((settings) => {
				const variable = settings.selectedVariables[0];
				const annotations = getChartAnnotationsByChartId(settings.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], variable);
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(
					settings,
					translationMap
				);
				const chart = applyForecastChartAnnotations(
					createForecastChart(
						{
							data: result,
							variables: sampleLayerVariables,
							timeField: 'timepoint_id',
							groupField: 'sample_id'
						},
						{
							data: resultSummary,
							variables: statLayerVariables,
							timeField: 'timepoint_id'
						},
						groundTruthData && {
							data: groundTruthData.value,
							variables: datasetVar ? [datasetVar] : [],
							timeField: modelVarToDatasetVar(mapping?.value || [], 'timepoint_id')
						},
						options
					),
					annotations
				);
				_.keys(groupedInterventionOutputs.value).forEach((key) => {
					if (settings.selectedVariables.includes(key)) {
						chart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[key]));
					}
				});
				charts[settings.id] = chart;
			});
			return charts;
		});
		return variableCharts;
	};

	// Create comparison charts based on chart settings
	const useComparisonCharts = (chartSettings: ComputedRef<ChartSetting[]>) => {
		const comparisonCharts = computed(() => {
			const charts: Record<string, any> = {};
			if (!chartData.value) return charts;
			const { result, resultSummary, translationMap } = chartData.value;
			chartSettings.value.forEach((setting) => {
				const selectedVars = setting.selectedVariables;
				const { statLayerVariables, sampleLayerVariables, options } = createForecastChartOptions(
					setting,
					translationMap
				);
				const annotations = getChartAnnotationsByChartId(setting.id);

				const chart = applyForecastChartAnnotations(
					createForecastChart(
						{
							data: result,
							variables: sampleLayerVariables,
							timeField: 'timepoint_id',
							groupField: 'sample_id'
						},
						{
							data: resultSummary,
							variables: statLayerVariables,
							timeField: 'timepoint_id'
						},
						null,
						options
					),
					annotations
				);
				if (interventions?.value) {
					_.keys(groupedInterventionOutputs.value).forEach((key) => {
						if (selectedVars.includes(key)) {
							chart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[key]));
						}
					});
				}
				charts[setting.id] = chart;
			});
			return charts;
		});
		return comparisonCharts;
	};

	// Create error charts based on chart settings
	const useErrorChart = (
		chartSettings: ComputedRef<ChartSetting[]>,
		errorData: ComputedRef<DataArray>,
		mapping: ComputedRef<CalibrateMap[]> | null
	) => {
		const errorChartVariables = computed(() => {
			if (!chartSettings.value.length) return [];
			const variables = chartSettings.value
				.map((s) => s.selectedVariables[0])
				.map((variable) => ({
					field: modelVarToDatasetVar(mapping?.value ?? [], variable) as string,
					label: variable
				}))
				.filter((v) => !!v.field);
			return variables;
		});

		const errorChart = computed(() => {
			if (errorData.value.length === 0) return {};
			const spec = createErrorChart(errorData.value, {
				title: '',
				width: chartSize.value.width,
				variables: errorChartVariables.value,

				xAxisTitle: 'Mean absolute (MAE)'
			});
			return spec;
		});

		const onExpandErrorChart = () => {
			if (errorData.value.length === 0) return {};
			// Customize the chart size by modifying the spec before expanding the chart
			const spec = createErrorChart(errorData.value, {
				title: '',
				width: window.innerWidth / 1.5,
				height: 230,
				boxPlotHeight: 50,
				areaChartHeight: 150,
				variables: errorChartVariables.value,
				xAxisTitle: 'Mean absolute (MAE)'
			});
			return spec as any;
		};

		return {
			errorChart,
			onExpandErrorChart
		};
	};

	// Create parameter distribution charts based on chart settings
	const useParameterDistributionCharts = (chartSettings: ComputedRef<ChartSetting[]>) => {
		const parameterDistributionCharts = computed(() => {
			if (!chartData.value) return {};
			const { result, pyciemssMap } = chartData.value;
			// Note that we want to show the parameter distribution at the first timepoint only
			const data = result.filter((d) => d.timepoint_id === 0);
			const labelBefore = 'Before calibration';
			const labelAfter = 'After calibration';
			const charts = {};
			chartSettings.value.forEach((setting) => {
				const param = setting.selectedVariables[0];
				const fieldName = pyciemssMap[param];
				const beforeFieldName = `${fieldName}:pre`;
				const histogram = createHistogramChart(data, {
					title: `${param}`,
					width: chartSize.value.width,
					height: chartSize.value.height,
					xAxisTitle: `${param}`,
					yAxisTitle: 'Count',
					maxBins: 10,
					variables: [
						{ field: beforeFieldName, label: labelBefore, width: 54, color: '#AAB3C6' },
						{ field: fieldName, label: labelAfter, width: 24, color: '#1B8073' }
					]
				});
				const toDisplayNumber = (num?: number) => (num ? displayNumber(num.toString()) : '');
				const stat = {
					header: [labelBefore, labelAfter],
					mean: [mean(data, (d) => d[beforeFieldName]), mean(data, (d) => d[fieldName])].map(toDisplayNumber),
					variance: [variance(data, (d) => d[beforeFieldName]), variance(data, (d) => d[fieldName])].map(
						toDisplayNumber
					)
				};
				charts[setting.id] = { histogram, ...stat };
			});
			return charts;
		});
		return parameterDistributionCharts;
	};

	return {
		generateAnnotation,
		getChartAnnotationsByChartId,
		useInterventionCharts,
		useVariableCharts,
		useComparisonCharts,
		useErrorChart,
		useParameterDistributionCharts
	};
}
