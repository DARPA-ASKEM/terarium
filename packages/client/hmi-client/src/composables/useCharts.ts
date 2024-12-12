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
	createSimulateSensitivityScatter,
	ForecastChartOptions
} from '@/services/charts';
import { flattenInterventionData } from '@/services/intervention-policy';
import { DataArray, extractModelConfigIdsInOrder, extractModelConfigIds } from '@/services/models/simulation-service';
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
import { EnsembleErrorData } from '@/components/workflow/ops/calibrate-ensemble-ciemss/calibrate-ensemble-util';
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

// Get the model variable name for the corresponding model configuration and the ensemble variable name from the mapping
const getModelConfigVariable = (
	mapping: EnsembleVariableMappings,
	ensembleVariableName: string,
	modelConfigId: string
) => mapping.find((d) => d.newName === ensembleVariableName)?.modelConfigurationMappings[modelConfigId] ?? '';

const getModelConfigIdPrefix = (configId: string) => (configId ? `${configId}/` : '');

/**
 * Converts a model variable name to a dataset variable name based on the provided mapping.
 *
 * @param {VariableMappings} mapping - The mapping object that contains the variable mappings.
 * @param {string} modelVariable - The name of the model variable to be converted.
 * @returns {string} - The corresponding dataset variable name, or an empty string if the mapping is empty or not found.
 */
const modelVarToDatasetVar = (mapping: VariableMappings, modelVariable: string) => {
	if (_.isEmpty(mapping)) return '';
	if (isCalibrateMap(mapping[0])) {
		return (mapping as CalibrateMap[]).find((d) => d.modelVariable === modelVariable)?.datasetVariable ?? '';
	}
	if (isCalibrateEnsembleMappingRow(mapping[0])) {
		return (mapping as CalibrateEnsembleMappingRow[]).find((d) => d.newName === modelVariable)?.datasetMapping ?? '';
	}
	return '';
};

// For ensemble charts, add model names to translation map to give more context for the usage in llm annotation generation
// For example, if a variable name is 'model_0/infected', we want to give llm {model_0/infected: 'Model Antwerp Infected after calibration'}
// instead of just { model_0/infected: 'Infected after calibration' } so that it is clear to LLM which model the variable belongs to.
const addModelConfigNameToTranslationMap = (
	translationMap: Record<string, string>,
	pyciemssMap: Record<string, string>,
	modelConfigs: ModelConfiguration[]
) => {
	const newMap = {};
	const modelPrefixToModelconfigIdMap = extractModelConfigIds(pyciemssMap);
	// Build model index (model_#) to model config name mapping with pyciemssMap
	Object.entries(translationMap).forEach(([key, value]) => {
		const modelPrefix = key.split('/')[0];
		const modelConfigName = getModelConfigName(modelConfigs, modelPrefixToModelconfigIdMap[modelPrefix] ?? '');
		const newValue = modelConfigName ? `${modelConfigName} ${value}` : value;
		newMap[key] = newValue;
	});
	return newMap;
};

// Consider provided reference object is ready if it is set to null explicitly or if it's value is available
const isRefReady = (ref: Ref | null) => ref === null || Boolean(ref.value);

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
	// Check if references of the core dependencies are ready to build the chart to prevent multiple re-renders especially
	// on initial page load where data are fetched asynchronously and assigned to the references in different times.
	const isChartReadyToBuild = computed(() => [model, modelConfig, chartData].every(isRefReady));

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
				variables.push(getModelConfigIdPrefix(config.id ?? '') + modelVar);
			});
		} else {
			// If model modelConfigId is provided, include a model variable, otherwise include an ensemble variable
			variables.push(
				modelConfigId
					? // model variable
						getModelConfigIdPrefix(modelConfigId) +
							getModelConfigVariable(<EnsembleVariableMappings>mapping?.value ?? [], ensembleVarName, modelConfigId)
					: // ensemble variable
						(modelVarToDatasetVar(mapping?.value ?? [], ensembleVarName) as string)
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
			colorscheme: [BASE_GREY, PRIMARY_COLOR],
			scale: setting.scale
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
		if (setting.type === ChartSettingType.VARIABLE_ENSEMBLE) {
			options.translationMap = addModelConfigNameToTranslationMap(
				options.translationMap ?? {},
				chartData.value.pyciemssMap,
				<ModelConfiguration[]>modelConfig?.value ?? []
			);
		}
		return generateAndSaveForecastChartAnnotation(setting, query, 'timepoint_id', statLayerVariables, options);
	};

	const groupedInterventionOutputs = computed(() =>
		_.groupBy(flattenInterventionData(interventions?.value ?? []), 'appliedTo')
	);

	// Create intervention charts based on chart settings
	const useInterventionCharts = (chartSettings: ComputedRef<ChartSetting[]>, showSamples = false) => {
		const interventionCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!isChartReadyToBuild.value) return charts;
			const { resultSummary, result } = chartData.value as ChartData;
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
		groundTruthData: ComputedRef<DataArray> | null,
		isCompareDataset = false
	) => {
		const variableCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!isChartReadyToBuild.value || !isRefReady(groundTruthData)) return charts;
			const { result, resultSummary } = chartData.value as ChartData;

			// eslint-disable-next-line
			chartSettings.value.forEach((settings) => {
				console.log(settings);

				const variable = settings.selectedVariables[0];
				const annotations = getChartAnnotationsByChartId(settings.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], variable);
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(settings);

				if (isCompareDataset) {
					options.title = variable;
				}

				console.log('statLayerVariables', statLayerVariables);
				console.log(result);
				console.log(resultSummary);

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
			if (!isChartReadyToBuild.value) return charts;
			const { result, resultSummary } = chartData.value as ChartData;
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
		const ensembleVariableCharts = computed(() => {
			const charts: Record<string, VisualizationSpec[]> = {};
			if (!isChartReadyToBuild.value || !isRefReady(groundTruthData)) return chartData;
			const { result, resultSummary } = chartData.value as ChartData;
			const modelConfigIds = extractModelConfigIdsInOrder(chartData.value?.pyciemssMap ?? {});
			chartSettings.value.forEach((setting) => {
				const annotations = getChartAnnotationsByChartId(setting.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], setting.selectedVariables[0]);
				if (setting.showIndividualModels) {
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
		return ensembleVariableCharts;
	};

	// Create error charts based on chart settings
	const useErrorChart = (chartSettings: ComputedRef<ChartSetting[]>, errorData: ComputedRef<DataArray>) => {
		const errorChartVariables = computed(() => {
			if (!chartSettings.value.length) return [];
			const variables = chartSettings.value
				.map((s) => s.selectedVariables[0])
				.map((variable) => {
					const field = modelVarToDatasetVar(mapping?.value ?? [], variable) as string;
					return { field, label: `${variable}, ${field}` };
				})
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

	// Create ensemble calibrate error charts based on chart settings
	const useEnsembleErrorCharts = (
		chartSettings: ComputedRef<ChartSetting[]>,
		errorData: ComputedRef<EnsembleErrorData>
	) => {
		const getErrorChartVariables = (configId: string) => {
			const variables = chartSettings.value
				.map((s) => s.selectedVariables[0])
				.map((variable) => {
					const modelVarName = configId
						? getModelConfigVariable(<EnsembleVariableMappings>mapping?.value ?? [], variable, configId)
						: variable;
					const field = modelVarToDatasetVar(mapping?.value ?? [], variable) as string;
					return { field, label: `${modelVarName}, ${field}` };
				})
				.filter((v) => !!v.field);
			return variables;
		};

		const errorCharts = computed(() => {
			if (!isChartReadyToBuild.value) return [];
			const data = [errorData.value.ensemble]; // First item is always ensemble error data, and the rest are model error data
			const modelConfigIds = extractModelConfigIdsInOrder(chartData.value?.pyciemssMap ?? {});
			modelConfigIds.forEach((configId) => {
				if (errorData.value[configId]) data.push(errorData.value[configId]);
			});
			const errorChartSpecs = data.map((ed, index) => {
				const spec = createErrorChart(ed, {
					title: '',
					width: chartSize.value.width / data.length - 30, // Note: error chart adds extra 30px padding on top of the provided width so we subtract 30px to make the chart fit the container
					variables: getErrorChartVariables(modelConfigIds[index - 1] ?? ''),
					xAxisTitle: 'Mean absolute (MAE)',
					color: CATEGORICAL_SCHEME[index % CATEGORICAL_SCHEME.length]
				});
				return spec;
			});
			return errorChartSpecs;
		});

		const onExpandErrorChart = (chartSpecIndex: number) => {
			if (!isChartReadyToBuild.value) return {};
			const modelConfigIds = extractModelConfigIdsInOrder(chartData.value?.pyciemssMap ?? {});
			const errorDataKeys = ['ensemble', ...modelConfigIds];
			// Customize the chart size by modifying the spec before expanding the chart
			const spec = createErrorChart(errorData.value[errorDataKeys[chartSpecIndex]], {
				title: '',
				width: window.innerWidth / 1.5,
				height: 230,
				boxPlotHeight: 50,
				areaChartHeight: 150,
				variables: getErrorChartVariables(modelConfigIds[chartSpecIndex - 1] ?? ''),
				xAxisTitle: 'Mean absolute (MAE)',
				color: CATEGORICAL_SCHEME[chartSpecIndex % CATEGORICAL_SCHEME.length]
			});
			return spec as VisualizationSpec;
		};

		return {
			errorCharts,
			onExpandErrorChart
		};
	};

	// Create parameter distribution charts based on chart settings
	const useParameterDistributionCharts = (chartSettings: ComputedRef<ChartSetting[]>) => {
		const parameterDistributionCharts = computed(() => {
			const charts = {};
			if (!isChartReadyToBuild.value) return charts;
			const { result, pyciemssMap } = chartData.value as ChartData;
			// Note that we want to show the parameter distribution at the first timepoint only
			const data = result.filter((d) => d.timepoint_id === 0);
			const labelBefore = 'Before calibration';
			const labelAfter = 'After calibration';
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

	const useWeightsDistributionCharts = () => {
		const WEIGHT_PARAM_NAME = 'weight_param';
		const weightsCharts = computed(() => {
			const charts: VisualizationSpec[] = [];
			if (!isChartReadyToBuild.value) return charts;

			// Model configs are used to get the model config metadata. This order of model configs in arrays are not guaranteed to be the same as the order of model configs in the pyciemss results
			const modelConfigs = <ModelConfiguration[]>modelConfig?.value ?? [];
			// extractModelConfigIdsInOrder ensures that the order of model config IDs are matched with the order of corresponding model index in the pyciemss results
			const modelConfigIds = extractModelConfigIdsInOrder(chartData.value?.pyciemssMap ?? {});

			const data = chartData.value?.result.filter((d) => d.timepoint_id === 0) ?? [];
			const labelBefore = 'Before calibration';
			const labelAfter = 'After calibration';

			const colors = CATEGORICAL_SCHEME.slice(1); // exclude the first color which is for ensemble variable

			modelConfigIds.forEach((configId, index) => {
				const modelConfigName = getModelConfigName(modelConfigs, configId);
				const chartWidth = chartSize.value.width / modelConfigs.length;

				const fieldName = chartData.value?.pyciemssMap[`${getModelConfigIdPrefix(configId)}${WEIGHT_PARAM_NAME}`] ?? '';
				const beforeFieldName = `${fieldName}:pre`;

				const maxBins = 10;
				const barWidth = Math.min((chartWidth - 40) / maxBins, 54);
				const spec = createHistogramChart(data, {
					title: modelConfigName,
					width: chartWidth,
					height: chartSize.value.height,
					xAxisTitle: `Weights`,
					yAxisTitle: 'Count',
					maxBins,
					variables: [
						{ field: beforeFieldName, label: labelBefore, width: barWidth, color: BASE_GREY },
						{ field: fieldName, label: labelAfter, width: barWidth / 2, color: colors[index % colors.length] }
					],
					legendProperties: { direction: 'vertical', columns: 1, labelLimit: chartWidth }
				});
				charts.push(spec);
			});
			return charts;
		});
		return weightsCharts;
	};

	const useSimulateSensitivityCharts = (chartSettings: ComputedRef<ChartSetting[]>) => {
		const sensitivity = computed(() => {
			const timestep = _.last(chartData.value?.result)?.timepoint_id;
			const sliceData = chartData.value?.result.filter((d: any) => d.timepoint_id === timestep) as any[];

			// Translate names ahead of time, because we can't seem to customize titles
			// in vegalite with repeat
			const translationMap = chartData.value?.translationMap;
			const sliceDataTranslated = sliceData.map((obj: any) => {
				const r: any = {};
				Object.keys(obj).forEach((key) => {
					if (translationMap && translationMap[key]) {
						const newKey = translationMap[key];
						r[newKey] = obj[key];
					} else {
						r[key] = obj[key];
					}
				});
				return r;
			});

			// FIXME: Let modeller pick the input variables
			let inputVariables: string[] = [];
			if (model && model.value && model.value.semantics?.ode) {
				const ode = model.value.semantics.ode;
				const initials = ode.initials?.map((initial) => initial.expression) as string[];
				inputVariables = ode.parameters
					?.filter((parameter) => !initials.includes(parameter.id))
					.map((parameter) => parameter.id) as string[];
			}

			const charts: Record<string, VisualizationSpec> = {};

			// eslint-disable-next-line
			chartSettings.value.forEach((settings) => {
				const spec = createSimulateSensitivityScatter(
					{
						data: sliceDataTranslated,
						inputVariables,
						outputVariable: settings.selectedVariables[0]
					},
					{
						width: 150,
						height: 150,
						xAxisTitle: '',
						yAxisTitle: '',
						translationMap: chartData.value?.translationMap || {}
					}
				);
				charts[settings.id] = spec;
			});
			return charts;
		});
		return sensitivity;
	};

	return {
		generateAnnotation,
		getChartAnnotationsByChartId,
		useInterventionCharts,
		useVariableCharts,
		useComparisonCharts,
		useEnsembleVariableCharts,
		useErrorChart,
		useParameterDistributionCharts,
		useWeightsDistributionCharts,
		useSimulateSensitivityCharts,
		useEnsembleErrorCharts
	};
}
