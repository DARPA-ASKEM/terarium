import _ from 'lodash';
import API from '@/api/api';
import {
	ChartSetting,
	ChartSettingEnsembleVariable,
	ChartSettingEnsembleVariableOptions,
	ChartSettingType
} from '@/types/common';
import { v4 as uuidv4 } from 'uuid';
import { b64DecodeUnicode } from '@/utils/binary';
import { ChartAnnotation } from '@/types/Types';
import { ForecastChartOptions } from './charts';

export interface LLMGeneratedChartAnnotation {
	request: string;
	layerSpec: any;
}

export type EnsembleVariableChartSettingOption =
	| 'showIndividualModels'
	| 'relativeToEnsemble'
	| 'showIndividualModelsWithWeight';

export function isChartSettingEnsembleVariable(setting: ChartSetting): setting is ChartSettingEnsembleVariable {
	return (<ChartSettingEnsembleVariable>setting).type === ChartSettingType.VARIABLE_ENSEMBLE;
}

/**
 * Adds a new multi-variable chart setting to the provided settings array if it doesn't already exist.
 * Note that the order of the variables doesn't matter. e.g. chart settings with selectedVariables, ['a', 'b'] is treated same as the one with ['b', 'a'].
 *
 * @param settings - The array of existing chart settings.
 * @param type - The type of the chart setting to be added.
 * @param selectedVariables - The array of selected variables for the new chart setting.
 * @returns The updated array of chart settings.
 */
export function addMultiVariableChartSetting(
	settings: ChartSetting[],
	type: ChartSettingType,
	selectedVariables: string[]
) {
	const existingSetting = settings.find(
		(setting) => setting.type === type && _.isEqual(new Set(setting.selectedVariables), new Set(selectedVariables))
	);
	if (existingSetting || _.isEmpty(selectedVariables)) return settings;
	const newSetting: ChartSetting = {
		id: uuidv4(),
		name: selectedVariables.join(', '),
		selectedVariables,
		type,
		scale: ''
	} as ChartSetting;
	return [...settings, newSetting];
}

// Extract ensemble variable chart options from the chart settings for each variable and merge into single option.
export function getEnsembleChartSettingOptions(chartSettings: ChartSetting[]) {
	// default options
	const options: ChartSettingEnsembleVariableOptions = {
		showIndividualModels: false,
		relativeToEnsemble: false,
		showIndividualModelsWithWeight: undefined // only applicable for the simulate ensemble otherwise undefined
	};
	// Merge options from all ensemble variables since each variable setting has its own options but all controlled by the single parent scope UI.
	chartSettings.forEach((s) => {
		if (!isChartSettingEnsembleVariable(s)) return;
		options.showIndividualModels = options.showIndividualModels || s.showIndividualModels;
		options.relativeToEnsemble = options.relativeToEnsemble || s.relativeToEnsemble;
		options.showIndividualModelsWithWeight = options.showIndividualModelsWithWeight || s.showIndividualModelsWithWeight;
	});
	return options;
}

export function createNewChartSetting(
	name: string,
	type: ChartSettingType,
	selectedVariables: string[],
	options: ChartSettingEnsembleVariableOptions
): ChartSetting {
	const setting: ChartSetting = {
		id: uuidv4(),
		name,
		selectedVariables,
		type,
		scale: ''
	};
	if (isChartSettingEnsembleVariable(setting)) Object.assign(setting, options);
	return setting;
}

/**
 * Updates the given chart settings based on the selected variables and return it as new settings.
 * This function assumes that the given chart settings are for single variable charts.
 *
 * @param {ChartSetting[]} settings - The current array of chart settings.
 * @param {string[]} variableSelection - An array of selected variables to update the chart settings with.
 * @param {ChartSettingType} type - The type of chart setting to update.
 * @returns {ChartSetting[]} The updated array of chart settings.
 */
export function updateChartSettingsBySelectedVariables(
	settings: ChartSetting[],
	type: ChartSettingType,
	variableSelection: string[]
) {
	// Note: New ensemble settings will have the same options as the existing ensemble variables.
	const existingEnsembleOptions = getEnsembleChartSettingOptions(settings);
	// previous settings without the settings of the given type
	const previousSettings = settings.filter((setting) => setting.type !== type);
	// selected settings for the given type
	const selectedSettings = variableSelection.map((variable) => {
		const found = settings.find((setting) => setting.selectedVariables[0] === variable && setting.type === type);
		return found ?? createNewChartSetting(variable, type, [variable], existingEnsembleOptions);
	});
	const newSettings: ChartSetting[] = [...previousSettings, ...selectedSettings];
	return newSettings;
}

export function updateEnsembleVariableChartSettingOption(
	settings: ChartSetting[],
	option: EnsembleVariableChartSettingOption,
	value: boolean
) {
	const newSettings = settings.map((setting) => {
		if (isChartSettingEnsembleVariable(setting)) {
			setting[option] = value;
		}
		return setting;
	});
	return newSettings;
}

/**
 * Filters chart settings based on the specified type and variable selection.
 * Assume that this is used for single variable charts only.
 *
 * @param {ChartSetting[]} settings - The array of chart settings to filter.
 * @param {ChartSettingType} type - The type of chart setting to filter by.
 * @param {string[]} variableSelection - The array of variable selections to filter by.
 * @returns {ChartSetting[]} The filtered array of chart settings.
 */
export function filterChartSettingsByVariables(
	settings: ChartSetting[],
	type: ChartSettingType,
	variableSelection: string[]
) {
	// previous settings without the settings of the given type
	const previousSettings = settings.filter((setting) => setting.type !== type);
	const selected = settings.filter(
		(setting) => setting.type === type && variableSelection.includes(setting.selectedVariables[0])
	);
	const newSettings: ChartSetting[] = [...previousSettings, ...selected];
	return newSettings;
}

/**
 * Deletes the chart settings with the given id and returns the new settings.
 *
 * @param {ChartSetting[]} settings - The current array of chart settings.
 * @param {string} id - The id of the chart setting to delete.
 * @returns {ChartSetting[]} The updated array of chart settings.
 */
export function removeChartSettingById(settings: ChartSetting[], id: string) {
	return settings.filter((setting) => setting.id !== id);
}

export async function saveAnnotation(
	annotationLayerSpec: LLMGeneratedChartAnnotation,
	nodeId: string,
	chartId: string
) {
	const annotation: ChartAnnotation = {
		id: uuidv4(),
		description: annotationLayerSpec.request,
		nodeId,
		outputId: '',
		chartId,
		layerSpec: annotationLayerSpec.layerSpec,
		llmGenerated: true,
		metadata: {}
	};
	const { data } = await API.post('/chart-annotations', annotation);
	return data as ChartAnnotation;
}

export async function deleteAnnotation(annotationId: string) {
	const { data } = await API.delete(`/chart-annotations/${annotationId}`);
	return data;
}

export async function fetchAnnotations(nodeId: string) {
	const { data } = await API.post(`/chart-annotations/search`, { nodeId });
	return data as ChartAnnotation[];
}

export async function generateForecastChartAnnotation(
	request: string,
	timeField: string,
	variables: string[],
	options: Partial<ForecastChartOptions>
): Promise<LLMGeneratedChartAnnotation> {
	const axisTitle = {
		x: options.xAxisTitle ?? '',
		y: options.yAxisTitle ?? ''
	};
	const translateMap = _.pick(options.translationMap ?? {}, variables);
	const preamble = `
		Here is the information of the existing target chart spec where you need to add the annotations:
    - The existing chart follows a similar pattern as the above Example Chart Spec like:
        {{
          ...
          "transform": [
            {{
              "fold": ${JSON.stringify(variables)},
              "as": ["variableField', "valueField"]
            }}
          ],
          "layer": [
            {{
              ...
              "encoding": {{
                "x": {{"field": "${timeField}", "type": "quantitative", "axis": {{"title": "${axisTitle.x}"}}}},
                "y": {{"field": "valueField", "type": "quantitative", "axis": {{"title": "${axisTitle.y}"}}}}
              }}
            }}
            ...
          ]
        }}
    - Assume all unknown variables except the time field are for the y-axis and are renamed to the valueField.
    - Make sure possible values for 'valueField' are ${JSON.stringify(variables)} and try best to translate the variables mentioned from the request to the variables for the 'valueField'.
    - Leverage this variable to human readable name mapping: ${JSON.stringify(translateMap)} if needed.
	`;
	const instruction = `
    Give me the layer object to be added to the existing chart spec based on the following user request.

    ${request}
	`;
	const { data } = await API.post(
		`/gollm/chart-annotation?mode=SYNC`,
		{ preamble, instruction },
		{
			headers: {
				'Content-Type': 'application/json'
			}
		}
	);
	const str = b64DecodeUnicode(data.output);
	const result = JSON.parse(str);
	const layerSpec = result.response?.layer[0] ?? null;
	return {
		request,
		layerSpec
	};
}
