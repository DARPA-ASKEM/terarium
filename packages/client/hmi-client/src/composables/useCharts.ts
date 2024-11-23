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
import {
	CalibrateEnsembleMappingRow,
	isCalibrateEnsembleMappingRow
} from '@/components/workflow/ops/calibrate-ensemble-ciemss/calibrate-ensemble-ciemss-operation';
import { SimulateEnsembleMappingRow } from '@/components/workflow/ops/simulate-ensemble-ciemss/simulate-ensemble-ciemss-operation';
import { getModelConfigName } from '@/services/model-configurations';
import { useChartAnnotations } from './useChartAnnotations';

export interface ChartData {
	result: DataArray;
	resultSummary: DataArray;
	pyciemssMap: Record<string, string>;
	translationMap: Record<string, string>;
}

type EnsembleVariableMappings = CalibrateEnsembleMappingRow[] | SimulateEnsembleMappingRow[];
type VariableMappings = CalibrateMap[] | EnsembleVariableMappings;

const BASE_GREY = '#AAB3C6';
const PRIMARY_COLOR = CATEGORICAL_SCHEME[0];

// Get the model configuration id to variable name mappings for the given ensemble variable
const getModelConfigMappings = (mapping: EnsembleVariableMappings, ensembleVariableName: string) => {
	const modelConfigMappings = mapping.find((d) => d.newName === ensembleVariableName)?.modelConfigurationMappings;
	return modelConfigMappings ?? {};
};

// Get the model variable name for the corresponding model configuration and the ensemble variable name from the mapping
const getModelConfigVariable = (
	mapping: EnsembleVariableMappings,
	ensembleVariableName: string,
	modelConfigId: string
) => getModelConfigMappings(mapping, ensembleVariableName)[modelConfigId] ?? '';

// HACK: Generate fake model id prefix from model configuration index, this is supposed to be model configuration id instead of index number.
// Fix this when Pyciemss is updated to include model configuration id on it's variable name.
const getModelIdPrefix = (modelId: string, modelConfigs: ModelConfiguration[]) =>
	modelId ? `model_${modelConfigs.findIndex((c) => c.id === modelId)}/` : '';
// const getVariableNameModelPrifix = (modelId: string) => modelId ? `${modelId}/` : '';

/**
 * Converts a model variable name to a dataset variable name based on the provided mapping.
 *
 * @param {VariableMappings} mapping - The mapping object that contains the variable mappings.
 * @param {string} modelVariable - The name of the model variable to be converted.
 * @returns {string} - The corresponding dataset variable name, or an empty string if the mapping is empty or not found.
 */
export const modelVarToDatasetVar = (mapping: VariableMappings, modelVariable: string) => {
	if (_.isEmpty(mapping)) return '';
	if (isCalibrateMap(mapping[0])) {
		return (mapping as CalibrateMap[]).find((d) => d.modelVariable === modelVariable)?.datasetVariable ?? '';
	}
	if (isCalibrateEnsembleMappingRow(mapping[0])) {
		return (mapping as CalibrateEnsembleMappingRow[]).find((d) => d.newName === modelVariable)?.datasetMapping ?? '';
	}
	return '';
};

/**
 * Composable to manage the creation and configuration of various types of charts used in operator nodes and drilldown.
 *
 * @param nodeId - The ID of the node.
 * @param model - The model reference.
 * @param modelConfig - The model configurations reference. For ensemble operators, this can be an array of model configurations.
 * @param chartData - The chart data reference.
 * @param chartSize - The chart size reference.
 * @param interventions - Optional reference to interventions.
 *
 * @returns An object containing methods to create and manage different types of charts.
 */
export function useCharts(
	nodeId: string,
	model: Ref<Model | null> | null,
	modelConfig: Ref<ModelConfiguration | ModelConfiguration[] | null> | null,
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

	const createEnsembleVariableChartOptions = (
		setting: ChartSettingEnsembleVariable,
		modelConfigId = '',
		multiVariable = false,
		showBaseLines = false
	) => {
		const ensembleVarName = setting.selectedVariables[0];

		const options: ForecastChartOptions = {
			title: getModelConfigName(<ModelConfiguration[]>modelConfig?.value ?? [], modelConfigId) || ensembleVarName,
			legend: true,
			width: chartSize.value.width,
			height: chartSize.value.height,
			translationMap: chartData.value?.translationMap || {},
			xAxisTitle: '',
			yAxisTitle: '',
			autosize: AUTOSIZE.FIT,
			colorscheme: multiVariable ? CATEGORICAL_SCHEME : [BASE_GREY, PRIMARY_COLOR]
		};

		const variables: string[] = [];
		if (multiVariable) {
			// If multiVariable flag is true, include the ensemble variable and all model variables all together in one chart. modelConfigId is not used in this case.
			variables.push(modelVarToDatasetVar(mapping?.value ?? [], ensembleVarName) as string);
			(<ModelConfiguration[]>modelConfig?.value ?? []).forEach((config) => {
				const modelVar = getModelConfigVariable(
					<EnsembleVariableMappings>mapping?.value ?? [],
					ensembleVarName,
					config.id ?? ''
				);
				variables.push(getModelIdPrefix(config.id ?? '', <ModelConfiguration[]>modelConfig?.value ?? []) + modelVar);
			});
		} else {
			// If model modelConfigId is provided, include a model variable, otherwise include an ensemble variable
			variables.push(
				modelConfigId
					? getModelIdPrefix(modelConfigId, <ModelConfiguration[]>modelConfig?.value ?? []) +
							getModelConfigVariable(<EnsembleVariableMappings>mapping?.value ?? [], ensembleVarName, modelConfigId) // model variable
					: (modelVarToDatasetVar(mapping?.value ?? [], ensembleVarName) as string) // ensemble variable
			);
		}

		const sampleLayerVariables: string[] = [];
		const statLayerVariables: string[] = [];
		variables.forEach((varNameKeys) => {
			const varName = chartData.value?.pyciemssMap[varNameKeys];
			// Add base lines data
			if (showBaseLines) {
				sampleLayerVariables.push(`${varName}:pre`);
				statLayerVariables.push(`${varName}_mean:pre`);
			}
			// Add primary lines data
			sampleLayerVariables.push(`${varName}`);
			statLayerVariables.push(`${varName}_mean`);
		});
		return { statLayerVariables, sampleLayerVariables, options };
	};

	// Create options for forecast charts based on chart settings and model configuration
	const createForecastChartOptions = (setting: ChartSetting) => {
		if (isChartSettingEnsembleVariable(setting)) return createEnsembleVariableChartOptions(setting, '', true, true);
		const variables = setting.selectedVariables;
		const dateOptions = getVegaDateOptions(model?.value ?? null, <ModelConfiguration>modelConfig?.value || null);
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
		// TODO: If ensemble, include each model name to variable for translation map.
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
						getModelConfigMappings(<EnsembleVariableMappings>mapping?.value || [], setting.selectedVariables[0])
					);
					const smallMultiplesCharts = ['', ...modelConfigIds].map((modelConfigId, index) => {
						const { sampleLayerVariables, statLayerVariables, options } = createEnsembleVariableChartOptions(
							setting,
							modelConfigId,
							false,
							true
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
					const { sampleLayerVariables, statLayerVariables, options } = createEnsembleVariableChartOptions(
						setting,
						'',
						false,
						true
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
