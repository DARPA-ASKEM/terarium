import _, { capitalize, cloneDeep } from 'lodash';
import { mean, variance } from 'd3';
import { computed, ComputedRef, ref, Ref, watchEffect } from 'vue';
import { VisualizationSpec } from 'vega-embed';
import {
	applyChartAnnotations,
	AUTOSIZE,
	SENSITIVITY_COLOUR_SCHEME,
	CATEGORICAL_SCHEME,
	createErrorChart,
	createForecastChart,
	createForecastChartAnnotation,
	createHistogramChart,
	createInterventionChartMarkers,
	createQuantilesForecastChart,
	createSimulateSensitivityScatter,
	ForecastChartOptions,
	expressionFunctions,
	GroupedDataArray,
	createSensitivityRankingChart,
	createRankingInterventionsChart,
	BaseChartOptions,
	ForecastChartLayer
} from '@/services/charts';
import { flattenInterventionData } from '@/services/intervention-policy';
import {
	DataArray,
	extractModelConfigIdsInOrder,
	extractModelConfigIds,
	processAndSortSamplesByTimepoint
} from '@/services/models/simulation-service';
import {
	ChartSetting,
	ChartSettingComparison,
	ChartSettingEnsembleVariable,
	ChartSettingSensitivity,
	ChartSettingType,
	SensitivityMethod
} from '@/types/common';
import { ChartAnnotation, Dataset, Intervention, InterventionPolicy, Model, ModelConfiguration } from '@/types/Types';
import { displayNumber } from '@/utils/number';
import {
	getStateVariableStrataEntries,
	getUnitsFromModelParts,
	getVegaDateOptions,
	groupVariablesByStrata
} from '@/services/model';
import { CalibrateMap, isCalibrateMap } from '@/services/calibrate-workflow';
import {
	isChartSettingComparisonVariable,
	isChartSettingEnsembleVariable,
	generateComparisonColorScheme
} from '@/services/chart-settings';
import {
	CalibrateEnsembleMappingRow,
	isCalibrateEnsembleMappingRow
} from '@/components/workflow/ops/calibrate-ensemble-ciemss/calibrate-ensemble-ciemss-operation';
import { SimulateEnsembleMappingRow } from '@/components/workflow/ops/simulate-ensemble-ciemss/simulate-ensemble-ciemss-operation';
import { getModelConfigName, getParameters } from '@/services/model-configurations';
import { EnsembleErrorData } from '@/components/workflow/ops/calibrate-ensemble-ciemss/calibrate-ensemble-util';
import {
	CriteriaOfInterestCard,
	PlotValue,
	RankOption,
	TimepointOption
} from '@/components/workflow/ops/compare-datasets/compare-datasets-operation';
import { DATASET_VAR_NAME_PREFIX } from '@/services/dataset';
import { calculatePercentage } from '@/utils/math';
import { DistributionType } from '@/services/distribution';
import { pythonInstance } from '@/web-workers/python/PyodideController';
import { mean as statsMean, stddev } from '@/utils/stats';
import { getChartAnnotationType } from '@/services/chart-annotation';
import { useChartAnnotations } from './useChartAnnotations';

export interface ChartData {
	result: DataArray;
	resultSummary: DataArray;
	pyciemssMap: Record<string, string>;
	translationMap: Record<string, string>;
	resultGroupByTimepoint?: GroupedDataArray;
	numComparableDatasets?: number;
}

type EnsembleVariableMappings = CalibrateEnsembleMappingRow[] | SimulateEnsembleMappingRow[];
type VariableMappings = CalibrateMap[] | EnsembleVariableMappings;

const BASE_GREY = '#AAB3C6';
const PRIMARY_COLOR = CATEGORICAL_SCHEME[0];

// ======================================= Helper functions ==========================================================

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

/**
 * Calculate the extent of the y-axis based on the provided result data array and variables.
 * @param result The result data array. This can be a simple data array or a grouped data array.
 * @param variables The list of variables to calculate the extent for.
 * @returns The extent of the y-axis as a tuple of [min, max].
 */
const calculateYExtent = (result: DataArray | GroupedDataArray, variables: string[], includeBeforeValues = false) => {
	if (!result.length) return [0, 1] as [number, number];
	const isGroupedData = Array.isArray(Object.values(result[0])[0]);
	return isGroupedData
		? calculateYExtentFromGroupedData(result as GroupedDataArray, variables, includeBeforeValues)
		: calculateYExtentFromData(result as DataArray, variables, includeBeforeValues);
};

/**
 * Calculate the extent of the y-axis based on the provided result summary and variables.
 * @param result The result data array.
 * @param variables The list of variables to calculate the extent for.
 * @returns The extent of the y-axis as a tuple of [min, max].
 */
const calculateYExtentFromData = (result: DataArray, variables: string[], includeBeforeValues = false) => {
	const extent: [number, number] = [Infinity, -Infinity];
	result.forEach((row) => {
		variables.forEach((variable) => {
			extent[0] = Math.min(extent[0], row[variable]);
			extent[1] = Math.max(extent[1], row[variable]);
			if (includeBeforeValues) {
				extent[0] = Math.min(extent[0], row[`${variable}:pre`]);
				extent[1] = Math.max(extent[1], row[`${variable}:pre`]);
			}
		});
	});
	return extent;
};

/**
 * Calculate the extent of the y-axis based on the provided result data and variables.
 * @param result The result data group by timepoint.
 * @param variables The list of variables to calculate the extent for.
 * @returns The extent of the y-axis as a tuple of [min, max].
 */
const calculateYExtentFromGroupedData = (
	result: GroupedDataArray,
	variables: string[],
	includeBeforeValues = false
) => {
	const extent: [number, number] = [Infinity, -Infinity];
	result.forEach((row) => {
		variables.forEach((variable) => {
			extent[0] = Math.min(extent[0], row[variable].at(0) as number);
			extent[1] = Math.max(extent[1], row[variable].at(-1) as number);
			if (includeBeforeValues) {
				extent[0] = Math.min(extent[0], row[`${variable}:pre`].at(0) as number);
				extent[1] = Math.max(extent[1], row[`${variable}:pre`].at(-1) as number);
			}
		});
	});
	return extent;
};

// Consider provided reference object is ready if it is set to null explicitly or if it's value is available
const isRefReady = (reference: Ref | null) => reference === null || Boolean(reference.value);

/**
 * Normalize the stratified model chart data by the total strata population.
 * Only supported for stratified models.
 * @param setting ChartSettingComparison
 * @param data ChartData
 * @param model Model
 * @returns ChartData where the values for the state variables are normalized by the total strata population
 */
const normalizeStratifiedModelChartData = (setting: ChartSettingComparison, data: ChartData, model: Model) => {
	if (!model) return data;
	if (!setting.normalize) return data;

	const { selectedVariablesGroupByStrata, allVariablesGroupByStrata } = groupVariablesByStrata(
		setting.selectedVariables,
		data.pyciemssMap,
		model
	);

	const includeBeforeData = setting.showBeforeAfter && setting.smallMultiples;

	const normalizeSampleData = () => {
		const result: DataArray = [];
		data.result.forEach((row) => {
			const newEntry = { timepoint_id: row.timepoint_id, sample_id: row.sample_id };
			Object.entries(selectedVariablesGroupByStrata).forEach(([group, variables]) => {
				const denominator = allVariablesGroupByStrata[group].reduce((acc, v) => acc + row[data.pyciemssMap[v]], 0);
				variables
					.map((v) => data.pyciemssMap[v])
					.forEach((variable) => {
						newEntry[variable] = !group ? row[variable] : calculatePercentage(row[variable], denominator);
						if (includeBeforeData) {
							newEntry[`${variable}:pre`] = !group
								? row[variable]
								: calculatePercentage(row[`${variable}:pre`], denominator);
						}
					});
			});
			result.push(newEntry);
		});
		return result;
	};

	// If show quantiles is on,  normalize group by timepoint data
	if (setting.showQuantiles) {
		const resultGroupByTimepoint = processAndSortSamplesByTimepoint(normalizeSampleData());
		return { ...data, resultGroupByTimepoint };
	}
	// Else, normalize result and resultSummary data

	// Normalize stat data
	const resultSummary: DataArray = [];
	data.resultSummary.forEach((row) => {
		const newEntry = { timepoint_id: row.timepoint_id };
		Object.entries(selectedVariablesGroupByStrata).forEach(([group, variables]) => {
			const denominator = allVariablesGroupByStrata[group].reduce(
				(acc, v) => acc + row[`${data.pyciemssMap[v]}_mean`],
				0
			);
			variables
				.map((v) => data.pyciemssMap[v])
				.forEach((variable) => {
					const key = `${variable}_mean`;
					newEntry[key] = !group ? row[variable] : calculatePercentage(row[key], denominator);
					if (includeBeforeData) {
						newEntry[`${key}:pre`] = !group ? row[variable] : calculatePercentage(row[`${key}:pre`], denominator);
					}
				});
		});
		resultSummary.push(newEntry);
	});

	// Normalize sample data
	const result = normalizeSampleData();
	return { ...data, result, resultSummary };
};

/** A helper function to create a forecast chart with annotations */
const createForecastChartWithAnnotations = (
	setting: ChartSetting,
	{ result, resultSummary, resultGroupByTimepoint }: ChartData,
	sampleLayerVariables: string[],
	statLayerVariables: string[],
	options: ForecastChartOptions,
	annotations: ChartAnnotation[],
	groundTruthLayer: ForecastChartLayer | null
) => {
	const chartSpec = !setting.showQuantiles
		? createForecastChart(
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
				groundTruthLayer ?? null,
				options
			)
		: createQuantilesForecastChart(
				resultGroupByTimepoint ?? [],
				sampleLayerVariables,
				setting.quantiles ?? [],
				options
			);
	const chartAnnotationType = getChartAnnotationType(setting);
	const annotatedSpec = applyChartAnnotations(chartSpec, annotations, chartAnnotationType);
	return annotatedSpec as VisualizationSpec;
};

const buildYAxisTitle = (variables: string[], getUnitFn: (id: string) => string) =>
	_.uniq(variables.map(getUnitFn).filter((v) => !!v)).join(', ') || '';

// ====================================================================================================================

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
	const { getChartAnnotationsByChartId, generateAndSaveChartAnnotation } = useChartAnnotations(nodeId);

	const getUnit = (paramId: string) => {
		if (!model?.value) return '';
		return getUnitsFromModelParts(model.value)[paramId] || '';
	};

	const getUnitForNormalizedStratifiedModelData = (id: string) => {
		if (!model?.value) return '';
		const strata = getStateVariableStrataEntries(id, model.value);
		if (!strata.length) return getUnit(id);
		// sort strata labels in a same order it appears in the id
		const label = strata
			.map((v) => v.split(':')[1])
			.sort((a, b) => id.indexOf(a) - id.indexOf(b))
			.join('_');
		return `% ${label}`;
	};

	/**
	 * Create a base forecast chart options that's common for different types of forecast charts.
	 * @param setting ChartSetting
	 * @returns ForecastChartOptions
	 */
	const createBaseForecastChartOptions = (setting: ChartSetting) => {
		const variables = setting.selectedVariables;
		const dateOptions = getVegaDateOptions(model?.value ?? null, <ModelConfiguration>modelConfig?.value || null);
		const options: ForecastChartOptions = {
			title: '',
			legend: true,
			width: chartSize.value.width,
			height: chartSize.value.height,
			translationMap: chartData.value?.translationMap || {},
			xAxisTitle: getUnit('_time') || 'Time',
			yAxisTitle: buildYAxisTitle(variables, getUnit),
			dateOptions,
			colorscheme: [BASE_GREY, setting.primaryColor ?? PRIMARY_COLOR],
			scale: setting.scale
		};
		return options;
	};

	const createEnsembleVariableChartOptions = (
		setting: ChartSettingEnsembleVariable,
		modelConfigId = '',
		multiVariable = false,
		showBaseLines = false
	) => {
		setting.hideInNode = true;
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
			colorscheme: multiVariable ? CATEGORICAL_SCHEME : [BASE_GREY, setting.primaryColor ?? PRIMARY_COLOR],
			scale: setting.scale
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

	const createComparisonChartOptions = (
		setting: ChartSettingComparison,
		variableIndex = -1,
		includeAllVars = false
	) => {
		const variables = setting.selectedVariables;
		const options = createBaseForecastChartOptions(setting);

		const sampleLayerVariables: string[] = [];
		const statLayerVariables: string[] = [];

		const colorScheme: string[] = [];

		const varName = variables[variableIndex];
		// If varName is provided (single chart from small multiples), include that specific variable in the chart
		if (varName) {
			if (setting.showBeforeAfter) {
				sampleLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}:pre`);
				statLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}_mean:pre`);
				colorScheme.push(BASE_GREY);
			}
			sampleLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}`);
			statLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}_mean`);
			colorScheme.push(...generateComparisonColorScheme(setting, variableIndex));
		}
		// Otherwise include all selected variables
		else {
			variables.forEach((v) => {
				sampleLayerVariables.push(`${chartData.value?.pyciemssMap[v]}`);
				statLayerVariables.push(`${chartData.value?.pyciemssMap[v]}_mean`);
				if (includeAllVars) {
					// For the options that's provided to the annotation generation, we need to include pre values as well
					sampleLayerVariables.push(`${chartData.value?.pyciemssMap[v]}:pre`);
					statLayerVariables.push(`${chartData.value?.pyciemssMap[v]}_mean:pre`);
				}
			});
			colorScheme.push(...generateComparisonColorScheme(setting));
		}
		options.colorscheme = colorScheme;
		return { statLayerVariables, sampleLayerVariables, options };
	};

	/**
	 *
	 * Create options for forecast charts based on chart settings and model configuration.
	 *
	 * **Note:** default `generateAnnotation` function in `useCharts` uses this function to generate chart variables and translations that needs to generate AI annotation.
	 * If you need a custom way to create a chart option instead of using this, you need to provide a
	 * custom `generateAnnotation` function to `tera-chart-settings-panel` component for the annotation to work.
	 * @param setting ChartSetting
	 * @returns ForecastChartOptions
	 */
	const createForecastChartOptions = (setting: ChartSetting) => {
		if (isChartSettingEnsembleVariable(setting)) return createEnsembleVariableChartOptions(setting, '', true, true);
		if (isChartSettingComparisonVariable(setting)) return createComparisonChartOptions(setting, -1, true);

		const options = createBaseForecastChartOptions(setting);
		const variables = setting.selectedVariables;

		let sampleLayerVariables: string[] = [];
		let statLayerVariables: string[] = [];
		// Variable names for compare dataset charts
		if (!_.isNil(chartData.value?.numComparableDatasets)) {
			const varName = variables[0];
			for (let i = 0; i < chartData.value.numComparableDatasets; i++) {
				const rawVarName = chartData.value?.pyciemssMap[varName];
				const aggSuffix = rawVarName.startsWith(DATASET_VAR_NAME_PREFIX) ? '' : '_mean';
				sampleLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}:${i}`);
				statLayerVariables.push(`${chartData.value?.pyciemssMap[varName]}${aggSuffix}:${i}`);
			}
		}
		// Default variable names for simulate or calibrate operation
		else {
			sampleLayerVariables = [
				`${chartData.value?.pyciemssMap[variables[0]]}:pre`,
				`${chartData.value?.pyciemssMap[variables[0]]}`
			];
			statLayerVariables = [
				`${chartData.value?.pyciemssMap[variables[0]]}_mean:pre`,
				`${chartData.value?.pyciemssMap[variables[0]]}_mean`
			];
		}
		return { statLayerVariables, sampleLayerVariables, options };
	};

	// Generate annotations for a chart
	const generateAnnotation = async (setting: ChartSetting, query: string) => {
		if (!chartData.value) return null;
		const { statLayerVariables, sampleLayerVariables, options } = createForecastChartOptions(setting);
		if (setting.type === ChartSettingType.VARIABLE_ENSEMBLE) {
			options.translationMap = addModelConfigNameToTranslationMap(
				options.translationMap ?? {},
				chartData.value.pyciemssMap,
				<ModelConfiguration[]>modelConfig?.value ?? []
			);
		}
		// In quantile forecast charts, we use sampleLayerVariables instead of statLayerVariables
		const variables = setting.showQuantiles ? sampleLayerVariables : statLayerVariables;
		return generateAndSaveChartAnnotation(setting, query, 'timepoint_id', variables, options);
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

	// Create compare dataset charts based on chart settings
	const useCompareDatasetCharts = (
		chartSettings: ComputedRef<ChartSetting[]>,
		selectedPlotType: ComputedRef<PlotValue>,
		baselineIndex: ComputedRef<number>,
		datasets: Ref<Dataset[]>,
		modelConfigurations: Ref<ModelConfiguration[]>,
		interventionPolicies: Ref<InterventionPolicy[]>
	) => {
		const compareDatasetCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!isChartReadyToBuild.value) return charts;

			// TODO: We will want to in the future check that all the configs and interventions are
			// loaded before rendering the charts, but beware to not break rendering in the case
			// when there are no interventions

			let variableColorMap;

			if (interventionPolicies.value.length > 0) {
				// TODO: create the color map outside of this function and pass `interventionNameColorMap` as parameter
				const { interventionNameColorMap } = getInterventionColorAndScoreMaps(
					datasets,
					modelConfigurations,
					interventionPolicies
				);

				// Match variables with intervention colors
				variableColorMap = datasets.value.map(({ metadata }) => {
					const policy = interventionPolicies.value.find(
						({ id }) => id === metadata?.simulationAttributes?.interventionPolicyId
					);
					if (!policy || !policy.name) return 'black';
					if (interventionNameColorMap[policy.name]) {
						return interventionNameColorMap[policy.name];
					}
					return 'black';
				});
			} else {
				variableColorMap = cloneDeep(CATEGORICAL_SCHEME);
				variableColorMap.splice(baselineIndex.value, 0, 'black');
			}

			chartSettings.value.forEach((settings) => {
				const varName = settings.selectedVariables[0];
				const { statLayerVariables, sampleLayerVariables, options } = createForecastChartOptions(settings);
				options.title = varName;
				options.yAxisTitle = capitalize(selectedPlotType.value);
				options.colorscheme = variableColorMap;

				const annotations = getChartAnnotationsByChartId(settings.id);
				const chart = createForecastChartWithAnnotations(
					settings,
					chartData.value as ChartData,
					sampleLayerVariables,
					statLayerVariables,
					options,
					annotations,
					null
				);
				charts[settings.id] = chart;
			});
			return charts;
		});
		return compareDatasetCharts;
	};

	// Create variable charts based on chart settings
	const useVariableCharts = (
		chartSettings: ComputedRef<ChartSetting[]>,
		groundTruthData: ComputedRef<DataArray> | null
	) => {
		const variableCharts = computed(() => {
			const charts: Record<string, VisualizationSpec> = {};
			if (!isChartReadyToBuild.value || !isRefReady(groundTruthData)) return charts;
			// eslint-disable-next-line
			chartSettings.value.forEach((settings) => {
				const variable = settings.selectedVariables[0];
				const annotations = getChartAnnotationsByChartId(settings.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], variable);
				const { sampleLayerVariables, statLayerVariables, options } = createForecastChartOptions(settings);
				const groundTruthLayer = groundTruthData && {
					data: groundTruthData.value,
					variables: datasetVar ? [datasetVar] : [],
					timeField: modelVarToDatasetVar(mapping?.value || [], 'timepoint_id')
				};
				const chart = createForecastChartWithAnnotations(
					settings,
					chartData.value as ChartData,
					sampleLayerVariables,
					statLayerVariables,
					options,
					annotations,
					groundTruthLayer
				);
				_.keys(groupedInterventionOutputs.value).forEach((key) => {
					if (settings.selectedVariables.includes(key)) {
						// @ts-ignore
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
	const useComparisonCharts = (chartSettings: ComputedRef<ChartSettingComparison[]>, isNodeChart = false) => {
		const comparisonCharts = computed(() => {
			const charts: Record<string, VisualizationSpec[]> = {};
			if (!isChartReadyToBuild.value) return charts;

			chartSettings.value.forEach((setting) => {
				const { result, resultSummary, resultGroupByTimepoint } = normalizeStratifiedModelChartData(
					setting,
					chartData.value as ChartData,
					model?.value as Model
				);

				const selectedVars = setting.selectedVariables;
				const annotations = getChartAnnotationsByChartId(setting.id);
				if (setting.smallMultiples && setting.selectedVariables.length > 1 && !isNodeChart) {
					const sharedYExtent = setting.shareYAxis
						? calculateYExtent(
								setting.showQuantiles ? (resultGroupByTimepoint as GroupedDataArray) : result,
								selectedVars.map((v) => chartData.value?.pyciemssMap[v] ?? ''),
								Boolean(setting.showBeforeAfter)
							)
						: undefined;

					// create multiples
					let width = chartSize.value.width;
					if (selectedVars.length > 1) width = chartSize.value.width / 2;
					if (selectedVars.length > 4) width = chartSize.value.width / 3;
					const height = selectedVars.length <= 1 ? chartSize.value.height : chartSize.value.height / 2;
					charts[setting.id] = selectedVars.map((selectedVar, index) => {
						const { options, sampleLayerVariables, statLayerVariables } = createComparisonChartOptions(setting, index);
						options.width = width;
						options.height = height;
						options.yExtent = sharedYExtent;
						if (setting.normalize) {
							options.yAxisTitle = buildYAxisTitle([selectedVar], getUnitForNormalizedStratifiedModelData);
						}
						return createForecastChartWithAnnotations(
							setting,
							{ result, resultSummary, resultGroupByTimepoint } as ChartData,
							sampleLayerVariables,
							statLayerVariables,
							options,
							annotations,
							null
						);
					});
				} else {
					const { options, sampleLayerVariables, statLayerVariables } = createComparisonChartOptions(setting);
					if (setting.normalize) {
						options.yAxisTitle = buildYAxisTitle(setting.selectedVariables, getUnitForNormalizedStratifiedModelData);
					}
					const chart = createForecastChartWithAnnotations(
						setting,
						{ result, resultSummary, resultGroupByTimepoint } as ChartData,
						sampleLayerVariables,
						statLayerVariables,
						options,
						annotations,
						null
					);
					charts[setting.id] = [chart];
				}
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
			const modelConfigIds = extractModelConfigIdsInOrder(chartData.value?.pyciemssMap ?? {});
			chartSettings.value.forEach((setting) => {
				const annotations = getChartAnnotationsByChartId(setting.id);
				const datasetVar = modelVarToDatasetVar(mapping?.value || [], setting.selectedVariables[0]);
				const groundTruthLayer = groundTruthData && {
					data: groundTruthData.value,
					variables: datasetVar ? [datasetVar] : [],
					timeField: modelVarToDatasetVar(mapping?.value || [], 'timepoint_id')
				};
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
						const smallChart = createForecastChartWithAnnotations(
							setting,
							chartData.value as ChartData,
							sampleLayerVariables,
							statLayerVariables,
							options,
							annotations,
							groundTruthLayer
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
					const chart = createForecastChartWithAnnotations(
						setting,
						chartData.value as ChartData,
						sampleLayerVariables,
						statLayerVariables,
						options,
						annotations,
						groundTruthLayer
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
						{ field: fieldName, label: labelAfter, width: 24, color: setting.primaryColor ?? PRIMARY_COLOR }
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
					extent: [0, 1], // Weights are between 0 and 1
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

	/**
	 * Slices simulation data to get relevant records for sensitivity analysis based on the specified method.
	 *
	 * @param records - Array of simulation records containing timepoint_id, sample_id, and variable values
	 * @param options - Configuration object for slicing data
	 * @param options.method - Method to determine which records to include:
	 *                        - TIMEPOINT: Records at a specific timepoint
	 *                        - PEAK_VALUE: Records where each variable reaches its maximum value
	 *                        - PEAK_TIMEPOINT: Records where each variable reaches its maximum value (using timepoint as sensitivity target)
	 * @param options.timepoint - Required for TIMEPOINT method, specifies which timepoint to analyze
	 * @param options.selectedVariables - Array of variable names to analyze
	 *
	 * @returns Map where keys are variable names and values are arrays of relevant records:
	 *          - For TIMEPOINT: Records at the specified timepoint
	 *          - For PEAK_VALUE/PEAK_TIMEPOINT: Records where each variable reaches its maximum value per sample
	 */
	function getSlicedData(
		records: Record<string, any>[],
		options: { method: SensitivityMethod; timepoint?: number; selectedVariables: string[] }
	): Map<string, Record<string, any>[]> {
		const slicedRecords: Map<string, Record<string, any>[]> = new Map();
		// the records of interest we want are at the indicated timepoint
		if (options.method === SensitivityMethod.TIMEPOINT) {
			options.selectedVariables.forEach((selectedVariable) => {
				const data = records.filter((d) => d.timepoint_id === options.timepoint);
				if (!slicedRecords.has(selectedVariable)) {
					slicedRecords.set(selectedVariable, data);
				}
			});

			// the records of interest we want are the max value of each sample
		} else if (options.method === SensitivityMethod.PEAK_VALUE || options.method === SensitivityMethod.PEAK_TIMEPOINT) {
			options.selectedVariables.forEach((selectedVariable) => {
				// get a map of sample_id to the record with the max value for the selected variable
				const variableMax = records.reduce<Map<number, Record<string, any>>>((acc, record) => {
					const id = record.sample_id;
					// loop over all selected variables and get the max value
					const value = record[selectedVariable] as number;
					if (!acc.has(id) || value > (acc.get(id)![selectedVariable] as number)) {
						acc.set(id, record);
					}
					return acc;
				}, new Map<number, Record<string, any>>());

				// add the max value records to the sliced records map
				if (!slicedRecords.has(selectedVariable)) {
					slicedRecords.set(selectedVariable, [...variableMax.values()]);
				}
			});
		}
		return slicedRecords;
	}

	function createSensitivityBins(
		records: Record<string, any>[],
		selectedVariable: string,
		options: {
			numBins: number;
			unit?: string;
		}
	): Map<string, number[]> {
		if (options.numBins < 1) {
			throw new Error('Number of bins must be at least 1.');
		}

		// Extract the selected variable values and ensure they are numeric
		const samples = records.map((record) => {
			const value = record[selectedVariable];
			const id = record.sample_id;
			if (typeof value !== 'number') {
				throw new Error(`The selected variable "${selectedVariable}" must map to numeric values.`);
			}
			return { id, value };
		});

		// Sort the samples by value in ascending order
		const sortedSamples = [...samples].sort((a, b) => a.value - b.value);

		// Extract values for threshold calculation
		const sortedValues = sortedSamples.map((sample) => sample.value);
		const minValue = sortedValues[0];
		const maxValue = sortedValues[sortedValues.length - 1];

		// Calculate the quantile thresholds
		const thresholds: number[] = [];
		for (let i = 1; i < options.numBins; i++) {
			const quantileIndex = (i / options.numBins) * (sortedValues.length - 1);
			const lowerIndex = Math.floor(quantileIndex);
			const upperIndex = Math.ceil(quantileIndex);

			// Interpolate if necessary
			const quantileValue =
				lowerIndex === upperIndex
					? sortedValues[lowerIndex]
					: sortedValues[lowerIndex] +
						(sortedValues[upperIndex] - sortedValues[lowerIndex]) * (quantileIndex - lowerIndex);
			thresholds.push(quantileValue);
		}

		// Generate labels for each quantile
		const labels: string[] = [];
		let previousThreshold = minValue;
		thresholds.forEach((threshold) => {
			labels.push(
				`[${expressionFunctions.chartNumberFormatter(previousThreshold)}, ${expressionFunctions.chartNumberFormatter(threshold)}] ${options.unit ? `${options.unit}` : ''}`
			);
			previousThreshold = threshold;
		});

		labels.push(
			`[${expressionFunctions.chartNumberFormatter(previousThreshold)}, ${expressionFunctions.chartNumberFormatter(maxValue)}] ${options.unit ? `${options.unit}` : ''}`
		);

		// Assign bins to records and create the result map
		const result = new Map<string, number[]>();
		labels.forEach((label) => result.set(label, []));

		samples.forEach((sample) => {
			let bin = 0; // Default bin if the value is below the first threshold
			for (let i = 0; i < thresholds.length; i++) {
				if (sample.value <= thresholds[i]) {
					bin = i + 1;
					break;
				}
			}
			if (bin === 0) {
				bin = options.numBins; // Assign the last bin if the value exceeds all thresholds
			}
			const label = labels[bin - 1];
			result.get(label)!.push(sample.id);
		});

		return result;
	}

	const useSimulateSensitivityCharts = (chartSettings: ComputedRef<ChartSettingSensitivity[]>) => {
		const sensitivityData = ref<
			Record<string, { lineChart: VisualizationSpec; scatterChart: VisualizationSpec; rankingChart: VisualizationSpec }>
		>({});
		const sensitivityDataLoading = ref(false);
		const rankingScores = ref<Map<string, Map<string, number>>>(new Map());
		const timepoint = ref(0);
		const method = ref(SensitivityMethod.TIMEPOINT);
		let slicedData: Map<string, Record<string, any>[]> = new Map();

		const fetchSensitivityData = async () => {
			// pick the first setting's timepoint for now
			const chartType = chartSettings.value[0].chartType;
			const { result } = chartData.value as ChartData;
			// Translate names ahead of time, because we can't seem to customize titles
			// in vegalite with repeat

			const inputVariables: string[] = chartSettings.value[0].selectedInputVariables ?? [];

			const charts: Record<
				string,
				{ lineChart: VisualizationSpec; scatterChart: VisualizationSpec; rankingChart: VisualizationSpec }
			> = {};
			// eslint-disable-next-line
			for (const settings of chartSettings.value) {
				const translationMap = chartData.value?.translationMap;
				// We only need to translate the first variable's data slice and use it in the scatter/heatmap charts,
				// as the rest are the same
				const dataTranslated = Array.from(slicedData.values())[0]?.map((obj) => {
					const r = {};
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

				const selectedVariable =
					chartData.value?.pyciemssMap[settings.selectedVariables[0]] || settings.selectedVariables[0];
				let unit = getUnit(settings.selectedVariables[0]);
				let sensitivityVariable = selectedVariable;
				if (method.value === SensitivityMethod.PEAK_TIMEPOINT) {
					unit = 'timepoint';
					sensitivityVariable = 'timepoint_id';
				}
				const { options } = createForecastChartOptions(settings);
				const bins = createSensitivityBins(slicedData.get(selectedVariable)!, sensitivityVariable, {
					numBins: 7,
					unit
				});

				options.bins = bins;
				options.colorscheme = SENSITIVITY_COLOUR_SCHEME;
				options.legend = true;
				options.title = `${settings.selectedVariables[0]} sensitivity`;
				options.legendProperties = { direction: 'vertical', columns: 1, labelLimit: 500 };

				// using the same options for ranking chart as forecast chart
				const rankingOptions = _.cloneDeep(options);
				const rankingData = rankingScores.value.get(selectedVariable)!;
				const maxParametersShownCount = 20;
				// Filter out parameters with 0 score, show top 20 parameters
				const foramttedData = Array.from(rankingData)
					.map(([parameter, score]) => ({ parameter, score }))
					.filter((d) => d.score !== 0)
					.sort((a, b) => Math.abs(b.score) - Math.abs(a.score))
					.slice(0, maxParametersShownCount);

				rankingOptions.title = '';
				if (foramttedData.length === maxParametersShownCount) {
					rankingOptions.title = `Top ${maxParametersShownCount} most sensitive parameters displayed.`;
				}

				// total parameters - shown parameters
				const notShownCount = getParameters(modelConfig?.value as ModelConfiguration).length - foramttedData.length;
				if (notShownCount > 0) rankingOptions.title += ` ${notShownCount} parameter(s) not shown.`;

				const rankingSpec = createSensitivityRankingChart(foramttedData, rankingOptions);

				const lineSpec = createForecastChart(
					{
						data: result,
						variables: [selectedVariable],
						timeField: 'timepoint_id',
						groupField: 'sample_id'
					},
					{
						data: result,
						variables: [selectedVariable],
						timeField: 'timepoint_id'
					},
					null,
					options
				);

				// Get the statistical layer
				const statsLayer = lineSpec.layer[1];
				if (statsLayer?.layer) {
					// Remove any unwanted legends
					statsLayer.layer.forEach((sublayer) => {
						if (sublayer.encoding?.color?.legend) {
							sublayer.encoding.color.legend = null;
						}
					});

					// Find the text layers
					const textLayers = statsLayer.layer.filter((l) => l.mark?.type === 'text');
					const lastTwoLayers = textLayers.slice(-2);

					// The white outline layer
					const outlineLayer = lastTwoLayers[0];
					// The colored text layer
					const textLayer = lastTwoLayers[1];

					// Keep outline layer stroke white but remove color encoding
					if (outlineLayer) {
						delete outlineLayer.encoding.color;
					}

					// Copy the exact color encoding from the line layer
					if (textLayer) {
						textLayer.encoding.color = {
							field: 'group',
							type: 'nominal',
							scale: {
								domain: Array.from(bins.keys()),
								range: SENSITIVITY_COLOUR_SCHEME
							}
						};
					}
				}

				// Add sensitivity annotation if using timepoint method
				if (method.value === SensitivityMethod.TIMEPOINT) {
					const annotation = createForecastChartAnnotation('x', timepoint.value, 'Sensitivity analysis', true);
					lineSpec.layer[0].layer.push(annotation.layerSpec);
				}

				const spec = createSimulateSensitivityScatter(
					{
						data: dataTranslated ?? [],
						inputVariables,
						outputVariable: settings.selectedVariables[0]
					},
					{
						width: 150,
						height: 150,
						xAxisTitle: '',
						yAxisTitle: '',
						bins,
						colorscheme: SENSITIVITY_COLOUR_SCHEME,
						chartType
					}
				);

				charts[settings.id] = { lineChart: lineSpec, scatterChart: spec, rankingChart: rankingSpec };
			}

			sensitivityData.value = charts;
		};

		watchEffect(async () => {
			if (!chartData.value || !model?.value || _.isEmpty(chartSettings.value)) return;
			sensitivityDataLoading.value = true;

			const allSelectedVariables = chartSettings.value.map(
				(s) => chartData.value?.pyciemssMap[s.selectedVariables[0]] || s.selectedVariables[0]
			);

			// only run if the ranking scores if keys are not equal to the selectedVariables or timepoint has changed
			const hasAllScores =
				allSelectedVariables.every((v) => rankingScores.value.has(v)) &&
				Array.from(rankingScores.value.keys()).every((k) => allSelectedVariables.includes(k));

			const hasTimepointChanged = timepoint.value !== chartSettings.value[0].timepoint;
			timepoint.value = chartSettings.value[0].timepoint;

			const hasMethodChanged = method.value !== chartSettings.value[0].method;
			method.value = chartSettings.value[0].method;

			if (!hasAllScores || hasTimepointChanged || hasMethodChanged) {
				// only ranked non-constant parameters
				const allParameters =
					getParameters(modelConfig?.value as ModelConfiguration)
						.filter((p) => p.distribution.type !== DistributionType.Constant)
						.map((p) => p.referenceId) ?? [];
				slicedData = getSlicedData(chartData.value.result, {
					method: method.value,
					timepoint: timepoint.value,
					selectedVariables: allSelectedVariables
				});

				rankingScores.value = await pythonInstance.getRankingScores(
					slicedData,
					allSelectedVariables,
					allParameters,
					method.value
				);
			}
			fetchSensitivityData();
			sensitivityDataLoading.value = false;
		});

		return computed(() => ({ data: sensitivityData.value, loading: sensitivityDataLoading.value }));
	};

	const useInterventionRankingCharts = (
		criteriaOfInterestCards: Ref<CriteriaOfInterestCard[]>,
		datasets: Ref<Dataset[]>,
		modelConfigurations: Ref<ModelConfiguration[]>,
		interventionPolicies: Ref<InterventionPolicy[]>
	) =>
		computed(() => {
			// return empty if any of the required data is missing
			if (!chartData.value || !datasets.value || !modelConfigurations.value || !interventionPolicies.value)
				return { rankingCriteriaCharts: [], rankingResultsChart: null };

			const rankingCriteriaCharts: VisualizationSpec[] = [];
			let rankingResultsChart: VisualizationSpec | null = null;

			const allRankedCriteriaValues: { score: number; policyName: string; configName: string }[][] = [];

			const { interventionNameColorMap, interventionNameScoresMap } = getInterventionColorAndScoreMaps(
				datasets,
				modelConfigurations,
				interventionPolicies
			);

			criteriaOfInterestCards.value.forEach((card) => {
				if (!chartData.value || !card.selectedVariable) return;

				const variableKey = `${chartData.value.pyciemssMap[card.selectedVariable]}_mean`;
				let pointOfComparison: Record<string, number> = {};

				if (card.timepoint === TimepointOption.OVERALL) {
					const resultSummary = cloneDeep(chartData.value.resultSummary); // Must clone to avoid modifying the original data

					// Note that the reduce function here only compares the variable of interest
					// so only those key/value pairs will be relevant in the pointOfComparison object.
					// Other keys like timepoint_id (that we aren't using) will be in pointOfComparison
					// but they won't coincide with the value of the variable of interest.
					pointOfComparison = resultSummary.reduce((acc, val) =>
						Object.keys(val).reduce((acc2, key) => {
							if (key.includes(variableKey)) {
								acc2[key] = Math.max(acc[key], val[key]);
							}
							return acc2;
						}, acc)
					);
				} else if (card.timepoint === TimepointOption.FIRST) {
					pointOfComparison = chartData.value.resultSummary[0];
				} else if (card.timepoint === TimepointOption.LAST) {
					pointOfComparison = chartData.value.resultSummary[chartData.value.resultSummary.length - 1];
				}

				const rankingCriteriaValues: { score: number; policyName: string; configName: string }[] = [];

				datasets.value.forEach((dataset, index: number) => {
					const { metadata } = dataset;
					const modelConfiguration: ModelConfiguration = modelConfigurations.value.find(
						({ id }) => id === metadata.simulationAttributes?.modelConfigurationId
					)!;
					const policy: InterventionPolicy = interventionPolicies.value.find(
						({ id }) => id === metadata.simulationAttributes?.interventionPolicyId
					)!;

					const policyName = policy?.name ?? 'no policy';

					if (!modelConfiguration?.name) {
						return;
					}

					rankingCriteriaValues.push({
						score: pointOfComparison[`${variableKey}:${index}`] ?? 0,
						policyName,
						configName: modelConfiguration.name
					});
				});

				const sortedRankingCriteriaValues =
					card.rank === RankOption.MAXIMUM
						? rankingCriteriaValues.sort((a, b) => b.score - a.score)
						: rankingCriteriaValues.sort((a, b) => a.score - b.score);

				const options: BaseChartOptions = {
					title: card.name,
					width: chartSize.value.width,
					height: chartSize.value.height,
					xAxisTitle: 'Rank',
					yAxisTitle: card.selectedVariable
				};
				rankingCriteriaCharts.push(
					createRankingInterventionsChart(sortedRankingCriteriaValues, interventionNameColorMap, options)
				);
				allRankedCriteriaValues.push(sortedRankingCriteriaValues);
			});

			// For each criteria
			allRankedCriteriaValues.forEach((criteriaValues, index) => {
				const rankMutliplier = criteriaOfInterestCards.value[index].rank === RankOption.MINIMUM ? -1 : 1;

				// Calculate mean and stdev for this criteria
				const values = criteriaValues.map((val) => val.score);
				const meanValue = statsMean(values);
				let stdevValue = stddev(values);
				if (stdevValue === 0) stdevValue = 1;

				// For each policy
				Object.keys(interventionNameScoresMap).forEach((policyName) => {
					// For each value of the criteria
					criteriaValues.forEach((criteriaValue) => {
						if (criteriaValue.policyName !== policyName) return; // Skip criteria values that don't belong to this policy
						const scoredPolicyCriteria = rankMutliplier * ((criteriaValue.score - meanValue) / stdevValue);
						interventionNameScoresMap[policyName].push(scoredPolicyCriteria);
					});
				});
			});

			const scoredPolicies = Object.keys(interventionNameScoresMap)
				.map((policyName) => ({
					score: statsMean(interventionNameScoresMap[policyName]),
					policyName,
					configName: ''
				})) // Sort from highest to lowest value
				.sort((a, b) => b.score - a.score);

			const options: BaseChartOptions = {
				title: '',
				width: chartSize.value.width,
				height: chartSize.value.height,
				xAxisTitle: 'Rank',
				yAxisTitle: ''
			};
			rankingResultsChart = createRankingInterventionsChart(scoredPolicies, interventionNameColorMap, options);

			return { rankingCriteriaCharts, rankingResultsChart };
		});

	return {
		generateAnnotation,
		getChartAnnotationsByChartId,
		useInterventionCharts,
		useVariableCharts,
		useComparisonCharts,
		useCompareDatasetCharts,
		useEnsembleVariableCharts,
		useErrorChart,
		useParameterDistributionCharts,
		useWeightsDistributionCharts,
		useSimulateSensitivityCharts,
		useEnsembleErrorCharts,
		useInterventionRankingCharts
	};
}

// TODO: Move this out of this file. Maybe to compare-datasets-utils.ts
export function getInterventionColorAndScoreMaps(
	datasets: Ref<Dataset[]>,
	modelConfigurations: Ref<ModelConfiguration[]>,
	interventionPolicies: Ref<InterventionPolicy[]>
) {
	const interventionNameColorMap: Record<string, string> = {};
	const interventionNameScoresMap: Record<string, number[]> = {};
	let colorIndex = 0;
	datasets.value.forEach((dataset) => {
		const { metadata } = dataset;
		const modelConfiguration = modelConfigurations.value.find(
			({ id }) => id === metadata.simulationAttributes?.modelConfigurationId
		);
		const policy = interventionPolicies.value.find(
			({ id }) => id === metadata.simulationAttributes?.interventionPolicyId
		);

		const policyName = policy?.name ?? 'no policy';

		if (!modelConfiguration?.name) {
			return;
		}

		if (!interventionNameColorMap[policyName]) {
			if (interventionNameScoresMap) interventionNameScoresMap[policyName] = [];
			if (!policy?.name) {
				interventionNameColorMap[policyName] = 'black';
			} else {
				interventionNameColorMap[policyName] = CATEGORICAL_SCHEME[colorIndex];
				colorIndex++;
			}
		}
	});

	return { interventionNameColorMap, interventionNameScoresMap };
}
