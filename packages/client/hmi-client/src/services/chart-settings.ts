import _ from 'lodash';
import API from '@/api/api';
import {
	ChartSetting,
	ChartSettingEnsembleVariable,
	ChartSettingEnsembleVariableOptions,
	ChartSettingSensitivity,
	ChartSettingSensitivityOptions,
	ChartSettingType
} from '@/types/common';
import { v4 as uuidv4 } from 'uuid';
import { b64DecodeUnicode } from '@/utils/binary';
import { ChartAnnotation } from '@/types/Types';
import { CATEGORICAL_SCHEME, ForecastChartOptions } from './charts';

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
	options: ChartSettingEnsembleVariableOptions | ChartSettingSensitivityOptions
): ChartSetting {
	const setting: ChartSetting = {
		id: uuidv4(),
		name,
		selectedVariables,
		type,
		scale: '',
		primaryColor: CATEGORICAL_SCHEME[0]
	};
	if (isChartSettingEnsembleVariable(setting) || setting.type === ChartSettingType.SENSITIVITY)
		Object.assign(setting, options);

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

export function updateSensitivityChartSettingOption(
	settings: ChartSettingSensitivity[],
	options: { selectedVariables?: string[]; selectedInputVariables?: string[]; timepoint?: number }
) {
	// previous settings without the settings of the given type
	const previousSettings = settings.filter((setting) => setting.type !== ChartSettingType.SENSITIVITY);

	const selectedSettings = options.selectedVariables?.map((variable) => {
		const found = settings.find(
			(setting) => setting.selectedVariables[0] === variable && setting.type === ChartSettingType.SENSITIVITY
		);
		if (found) {
			found.selectedInputVariables = options.selectedInputVariables ?? [];
			found.timepoint = options.timepoint ?? 0;
			return found;
		}
		return createNewChartSetting(variable, ChartSettingType.SENSITIVITY, [variable], {
			selectedInputVariables: options.selectedInputVariables ?? [],
			timepoint: options.timepoint ?? 0
		});
	});

	const newSettings: ChartSetting[] = [...previousSettings, ...(selectedSettings ?? [])];
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
	const prompt = `
    You are an agent who is an expert in Vega-Lite chart specs. Provide a Vega-Lite layer JSON object for the annotation that can be added to an existing chart spec to satisfy the provided user request.

    - The Vega-Lite schema version you must use is https://vega.github.io/schema/vega-lite/v5.json.
    - Assume that you don’t know the exact data points from the data.
    - You must give me the single layer object that renders all the necessary drawing objects, including multiple layers within the top layer object if needed.
    - When adding a label, also draw a constant line perpendicular to the axis to which you are adding the label.

    Assuming you are adding the annotations to the following chart spec,
    ---- Example Chart Spec Start -----
    {
      "data": {"url": "data/samples.csv"},
      "transform": [
        {
          "fold": ["price", "cost", "tax", "profit"],
          "as": ["variableField", "valueField"]
        }
      ],
      "layer": [
        {
          "mark": "line",
          "encoding": {
            "x": {"field": "date", "type": "quantitative", "axis": {"title": "Day"}},
            "y": {"field": "valueField", "type": "quantitative", "axis": {"title": "Dollars"}}
          }
        }
      ]
    }
    ---- Example Chart Spec End -----
    Here are some example requests and the answers:

    Request:
    At day 200, add a label 'important'
    Answer:
    {
      "description": "At day 200, add a label 'important'",
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "x": { "datum": 200, "axis": { "title": ""} }
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -5
          },
          "encoding": {
            "x": { "datum": 200, "axis": { "title": ""} }
            "text": {"value": "important"}
          }
        }
      ]
    }

    Request:
    Add a label 'expensive' at price 20
    Answer:
    {
      "description": "Add a label 'expensive' at price 20",
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "y": { "datum": 20, "axis": { "title": ""} }
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -5
          },
          "encoding": {
            "y": { "datum": 20, "axis": { "title": ""} },
            "text": {"value": "expensive"}
          }
        }
      ]
    }

    Request:
    Add a vertical line for the day where the price exceeds 100.
    Answer:
    {
      "description": "Add a vertical line for the day where the price exceeds 100.",
      "transform": [
        {"filter": "datum.valueField > 100"},
        {"aggregate": [{"op": "min", "field": "date", "as": "min_date"}]}
      ],
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "x": {"field": "min_date", "type": "quantitative", "axis": { "title": ""}}
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -10
          },
          "encoding": {
            "x": {"field": "min_date", "type": "quantitative", "axis": { "title": ""}},
            "text": {"value": "Price > 100"}
          }
        }
      ]
    }

    Request:
    Add a vertical line at the day where the price reaches its peak value.
    Answer:
    {
      "description": "Add a vertical line at the day where the price reaches its peak value.",
      "transform": [
        {"filter": "datum.variableField == 'price'"},
        {
          "joinaggregate": [{
          "op": "max",
          "field": "valueField",
          "as": "max_price"
          }]
        },
        {"filter": "datum.valueField >= datum.max_price"}
      ],
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "x": {"field": "date", "type": "quantitative", "axis": { "title": ""}}
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -10
          },
          "encoding": {
            "x": {"field": "date", "type": "quantitative", "axis": { "title": ""}},
            "text": {"value": "Max Price"}
          }
        }
      ]
    }

    Here is the information of the existing target chart spec where you need to add the annotations:
    - The existing chart follows a similar pattern as the above Example Chart Spec like:
        {
          ...
          "transform": [
            {
              "fold": ${JSON.stringify(variables)},
              "as": ["variableField', "valueField"]
            }
          ],
          "layer": [
            {
              ...
              "encoding": {
                "x": {"field": "${timeField}", "type": "quantitative", "axis": {"title": "${axisTitle.x}"}},
                "y": {"field": "valueField", "type": "quantitative", "axis": {"title": "${axisTitle.y}"}}
              }
            }
            ...
          ]
        }
    - Assume all unknown variables except the time field are for the y-axis and are renamed to the valueField.
    - Make sure possible values for 'valueField' are ${JSON.stringify(variables)} and try best to translate the variables mentioned from the request to the variables for the 'valueField'.
    - Leverage this variable to human readable name mapping: ${JSON.stringify(translateMap)} if needed.

    Give me the layer object to be added to the existing chart spec based on the following user request.

    Request:
    ${request}
    Answer
    {
	`;
	const { data } = await API.post(`/gollm/generate-response?mode=SYNC&response-format=json`, prompt, {
		headers: {
			'Content-Type': 'application/json'
		}
	});
	const str = b64DecodeUnicode(data.output);
	const result = JSON.parse(str);
	const layerSpec = JSON.parse(result.response);
	return {
		request,
		layerSpec
	};
}
