import _ from 'lodash';
import API from '@/api/api';
import type { ChartSetting, ChartSettingType } from '@/types/common';
import { v4 as uuidv4 } from 'uuid';
import { b64DecodeUnicode } from '@/utils/binary';
import { ChartAnnotation } from '@/types/Types';
import { ForecastChartOptions } from './charts';

export interface LLMGeneratedChartAnnotation {
	request: string;
	layerSpec: any;
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
	const previousSettings = settings.filter((setting) => setting.type !== type);
	const selectedSettings = variableSelection.map((variable) => {
		const found = previousSettings.find(
			(setting) => setting.selectedVariables[0] === variable && setting.type === type
		);
		return (
			found ??
			({
				id: uuidv4(),
				name: variable,
				selectedVariables: [variable],
				type
			} as ChartSetting)
		);
	});
	const newSettings: ChartSetting[] = [...previousSettings, ...selectedSettings];
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
