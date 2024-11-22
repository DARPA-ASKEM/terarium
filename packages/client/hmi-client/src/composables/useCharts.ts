import _ from 'lodash';
import { mean, variance } from 'd3';
import { computed, ComputedRef, Ref } from 'vue';
import { VisualizationSpec } from 'vega-embed';
import {
	applyForecastChartAnnotations,
	AUTOSIZE,
	CATEGORICAL_SCHEME,
	createErrorChart,
	createForecastChart,
	createHistogramChart,
	createInterventionChartMarkers,
	ForecastChartOptions
} from '@/services/charts';
import { flattenInterventionData } from '@/services/intervention-policy';
import { DataArray } from '@/services/models/simulation-service';
import { ChartSetting, ChartSettingEnsembleVariable, ChartSettingType } from '@/types/common';
import { Intervention, Model, ModelConfiguration } from '@/types/Types';
import { displayNumber } from '@/utils/number';
import { getUnitsFromModelParts, getVegaDateOptions } from '@/services/model';
import { CalibrateMap, isCalibrateMap } from '@/services/calibrate-workflow';
import { isChartSettingEnsembleVariable } from '@/services/chart-settings';
import { CalibrateEnsembleMappingRow } from '@/components/workflow/ops/calibrate-ensemble-ciemss/calibrate-ensemble-ciemss-operation';
import {
	isSimulateEnsembleMappingRow,
	SimulateEnsembleMappingRow
} from '@/components/workflow/ops/simulate-ensemble-ciemss/simulate-ensemble-ciemss-operation';
import { useChartAnnotations } from './useChartAnnotations';

const BASE_GREY = '#AAB3C6';
const PRIMARY_COLOR = CATEGORICAL_SCHEME[0];

export interface ChartData {
	result: DataArray;
	resultSummary: DataArray;
	pyciemssMap: Record<string, string>;
	translationMap: Record<string, string>;
}
export type VariableMappings = CalibrateMap[] | EnsembleVariableMappings;
export type EnsembleVariableMappings = CalibrateEnsembleMappingRow[] | SimulateEnsembleMappingRow[];

const modelVarToDatasetVar = (mapping: VariableMappings, modelVariable: string) => {
	if (_.isEmpty(mapping) || isSimulateEnsembleMappingRow(mapping[0])) return '';
	const found = (mapping as CalibrateMap[] | CalibrateEnsembleMappingRow[]).find((d) =>
		isCalibrateMap(d) ? d.modelVariable === modelVariable : d.newName === modelVariable
	);
	if (!found) return '';
	return isCalibrateMap(found) ? found.datasetVariable : found.datasetMapping;
};

// Get the model configuration id to variable name mappings for the given ensemble variable
const getModelConfigMappings = (mapping: VariableMappings, ensembleVariableName: string) => {
	const modelConfigMappings = (mapping as EnsembleVariableMappings).find(
		(d) => d.newName === ensembleVariableName
	)?.modelConfigurationMappings;
	return modelConfigMappings ?? {};
};

// Get the model variable name for the corresponding model configuration and the ensemble variable name from the mapping
const getModelConfigVariable = (mapping: VariableMappings, ensembleVariableName: string, modelConfigId: string) =>
	getModelConfigMappings(mapping, ensembleVariableName)[modelConfigId] ?? '';

// const getVariableNameModelPrifix = (modelId: string) => modelId ? `${modelId}/` : '';
const getVariableNameModelPrefix = (modelId: string) => (modelId ? `model_0/` : '');

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
	model: Ref<Model | null> | null,
	modelConfig: Ref<ModelConfiguration | null> | null,
	chartData: Ref<ChartData | null>,
	chartSize: Ref<{ width: number; height: number }>,
	interventions: Ref<Intervention[]> | null,
	mapping: Ref<VariableMappings> | null
) {
	// Setup annotations
	const { getChartAnnotationsByChartId, generateAndSaveForecastChartAnnotation } = useChartAnnotations(nodeId);

	const getUnit = (paramId: string) => {
		if (!model?.value) return '';
		return getUnitsFromModelParts(model.value)[paramId] || '';
	};

	const createEnsembleVariableChartOptions = (setting: ChartSettingEnsembleVariable, modelConfigId = '') => {
		const ensembleVariableName = setting.selectedVariables[0];
		// If calibrate and modelConfigId is empty, build ensemble variable chart.
		// If simulation and modelConfigId is empty, show all variables in one chart.
		const variable = modelConfigId
			? getModelConfigVariable(mapping?.value ?? [], ensembleVariableName, modelConfigId) // model variable
			: (modelVarToDatasetVar(mapping?.value ?? [], ensembleVariableName) as string); // ensemble variable

		const options: ForecastChartOptions = {
			title: modelConfigId || ensembleVariableName,
			legend: true,
			width: chartSize.value.width,
			height: chartSize.value.height,
			translationMap: {},
			xAxisTitle: '',
			yAxisTitle: '',
			autosize: AUTOSIZE.FIT,
			colorscheme: [BASE_GREY, PRIMARY_COLOR]
		};

		// TODO: 1. Multi variable, combine all variables in one chart and use loops to build layer variables 2. Simulation, no pre data.
		// Observations label

		const varNameKey = `${getVariableNameModelPrefix(modelConfigId)}${variable}`;
		const sampleLayerVariables = [
			`${chartData.value?.pyciemssMap[varNameKey]}:pre`,
			`${chartData.value?.pyciemssMap[varNameKey]}`
		];
		const statLayerVariables = [
			`${chartData.value?.pyciemssMap[varNameKey]}_mean:pre`,
			`${chartData.value?.pyciemssMap[varNameKey]}_mean`
		];
		// TODO: Since translationMap differes based on the chart configs. Build translationMap outside when building chartData and pass along with the chartData.
		options.translationMap = {
			[statLayerVariables[0]]: `${modelConfigId ? ensembleVariableName : 'Ensemble'} before calibration`,
			[statLayerVariables[1]]: `${modelConfigId ? ensembleVariableName : 'Ensemble'} after calibration`,
			[varNameKey]: 'Observations'
		};
		return { statLayerVariables, sampleLayerVariables, options };
	};
	// Create options for forecast charts based on chart settings and model configuration
	const createForecastChartOptions = (setting: ChartSetting) => {
		if (isChartSettingEnsembleVariable(setting)) return createEnsembleVariableChartOptions(setting);
		const variables = setting.selectedVariables;
		const dateOptions = getVegaDateOptions(model?.value ?? null, modelConfig?.value || null);
		const options: ForecastChartOptions = {
			title: '',
			legend: true,
			width: chartSize.value.width,
			height: chartSize.value.height,
			translationMap: chartData.value?.translationMap || {},
			xAxisTitle: getUnit('_time') || 'Time',
			yAxisTitle: _.uniq(variables.map(getUnit).filter((v) => !!v)).join(',') || '',
			dateOptions,
			colorscheme: [BASE_GREY, PRIMARY_COLOR]
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
		const { statLayerVariables, options } = createForecastChartOptions(setting);
		return generateAndSaveForecastChartAnnotation(setting, query, 'timepoint_id', statLayerVariables, options);
	};

	const groupedInterventionOutputs = computed(() =>
		_.groupBy(flattenInterventionData(interventions?.value ?? []), 'appliedTo')
	);

	// Create intervention charts based on chart settings
	const useInterventionCharts = (chartSettings: ComputedRef<ChartSetting[]>, showSamples = false) => {
		const interventionCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!chartData.value) return charts;
			const { resultSummary, result } = chartData.value;
			// intervention chart spec
			chartSettings.value.forEach((setting) => {
				const variable = setting.selectedVariables[0];
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(setting);
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

				if (groupedInterventionOutputs.value[variable]) {
					// add intervention annotations (rules and text)
					forecastChart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[variable]));
				}

				charts[setting.id] = forecastChart;
			});
			return charts;
		});
		return interventionCharts;
	};

	// Create variable charts based on chart settings
	const useVariableCharts = (
		chartSettings: ComputedRef<ChartSetting[]>,
		groundTruthData: ComputedRef<DataArray> | null
	) => {
		const variableCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!chartData.value) return charts;
			const { result, resultSummary } = chartData.value;

			chartSettings.value.forEach((settings) => {
				const variable = settings.selectedVariables[0];
				const annotations = getChartAnnotationsByChartId(settings.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], variable);
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(settings);
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
			const charts: Record<string, VisualizationSpec> = {};
			if (!chartData.value) return charts;
			const { result, resultSummary } = chartData.value;
			chartSettings.value.forEach((setting) => {
				const selectedVars = setting.selectedVariables;
				const { statLayerVariables, sampleLayerVariables, options } = createForecastChartOptions(setting);
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

	const useEnsembleVariableCharts = (
		chartSettings: ComputedRef<ChartSettingEnsembleVariable[]>,
		groundTruthData: ComputedRef<DataArray> | null
	) => {
		const calibrateEnsembleVariableCharts = computed(() => {
			const charts: Record<string, VisualizationSpec[]> = {};
			if (!chartData.value) return charts;
			const { result, resultSummary } = chartData.value;
			chartSettings.value.forEach((setting) => {
				const annotations = getChartAnnotationsByChartId(setting.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], setting.selectedVariables[0]);
				if (setting.showIndividualModels) {
					// Build small multiples charts for each model configuration variable
					const modelConfigIds = Object.keys(
						getModelConfigMappings(mapping?.value || [], setting.selectedVariables[0])
					);
					const smallMultiplesCharts = ['', ...modelConfigIds].map((modelConfigId, index) => {
						const { sampleLayerVariables, statLayerVariables, options } = createEnsembleVariableChartOptions(
							setting,
							modelConfigId
						);
						options.width = chartSize.value.width / (modelConfigIds.length + 1);
						options.legendProperties = { direction: 'vertical', columns: 1, labelLimit: options.width };
						options.colorscheme = [BASE_GREY, CATEGORICAL_SCHEME[index % CATEGORICAL_SCHEME.length]];
						const smallChart = applyForecastChartAnnotations(
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
						return smallChart;
					});
					charts[setting.id] = smallMultiplesCharts;
				} else {
					// Build a single ensemble chart
					const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(setting);
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
					charts[setting.id] = [chart];
				}
			});
			return charts;
		});
		return calibrateEnsembleVariableCharts;
	};

	// Create error charts based on chart settings
	const useErrorChart = (chartSettings: ComputedRef<ChartSetting[]>, errorData: ComputedRef<DataArray>) => {
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
						{ field: beforeFieldName, label: labelBefore, width: 54, color: BASE_GREY },
						{ field: fieldName, label: labelAfter, width: 24, color: PRIMARY_COLOR }
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
		useEnsembleVariableCharts,
		useErrorChart,
		useParameterDistributionCharts
	};
}
