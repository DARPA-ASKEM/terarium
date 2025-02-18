import _ from 'lodash';
import API from '@/api/api';
import { v4 as uuidv4 } from 'uuid';
import { b64DecodeUnicode } from '@/utils/binary';
import { ChartAnnotation, ChartAnnotationType } from '@/types/Types';
import { ForecastChartOptions } from '@/services/charts';
import { ChartSetting } from '@/types/common';

export interface LLMGeneratedChartAnnotation {
	request: string;
	layerSpec: any;
}

interface PromptPreambleInput {
	variables: string[];
	timeField: string;
	translateMap: Record<string, string>;
	axisTitle: { x: string; y: string };
}

const buildPromptPreamble = ({ variables, timeField, axisTitle, translateMap }: PromptPreambleInput) => ({
	[ChartAnnotationType.ForecastChart]: `
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
	`,

	[ChartAnnotationType.QuantileForecastChart]: `
	Here is the information of the existing target chart spec where you need to add the annotations:
		{
			...
			"layer": [
				{
					"mark": "errorband",
					"encoding": {
							"x": { "field": "x", "type": "quantitative", "axis": { "title": "${axisTitle.x}" } },
							"y": { "field": "upper", "type": "quantitative", "axis": { "title": "${axisTitle.y}" } },
							"y2": { "field": "lower", "type": "quantitative", "axis": { "title": "${axisTitle.y}" } },
							"color": { "field": "variable", "type": "nominal" },
							"opacity": { "field": "quantile", "type": "quantitative" }
					}
				}
			]
		}
	And here are some information that needs to be considered:
	- data is records with following columns: x, upper, lower, variable, quantile (x is the time field)
	- When not instructed otherwise, assume that quantile is 0.5 (median) and upper and lower are the same value.
	- Possible values for quantile are from 0.5 to 0.99
	- Possible values for variable are ${JSON.stringify(variables)}
	- Leverage this variable to human readable name mapping: ${JSON.stringify(translateMap)} for translating the variables mentioned from the request to the variables for the 'variable' field.
	`
});

export async function saveAnnotation(
	annotationLayerSpec: LLMGeneratedChartAnnotation,
	nodeId: string,
	chartId: string,
	chartType: ChartAnnotationType = ChartAnnotationType.ForecastChart
) {
	const annotation: ChartAnnotation = {
		id: uuidv4(),
		description: annotationLayerSpec.request,
		chartType,
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

export async function generateChartAnnotation(
	request: string,
	chartType: ChartAnnotationType,
	timeField: string,
	variables: string[],
	options: Partial<ForecastChartOptions>
): Promise<LLMGeneratedChartAnnotation> {
	const axisTitle = {
		x: options.xAxisTitle ?? '',
		y: options.yAxisTitle ?? ''
	};
	const translateMap = _.pick(options.translationMap ?? {}, variables);
	const preamble = buildPromptPreamble({ variables, timeField, axisTitle, translateMap })[chartType];
	const { data } = await API.post(
		`/gollm/chart-annotation?mode=SYNC`,
		{ preamble, instruction: request, chartType },
		{
			headers: {
				'Content-Type': 'application/json'
			}
		}
	);
	const str = b64DecodeUnicode(data.output);
	const result = JSON.parse(str);
	const layerSpec = result.response ?? null;
	return {
		request,
		layerSpec
	};
}

/**
 * Get the chart annotation type based on the chart setting.
 * @param setting - The chart setting.
 * @returns - The chart annotation type.
 */
export function getChartAnnotationType(setting: ChartSetting) {
	if (setting.showQuantiles) {
		return ChartAnnotationType.QuantileForecastChart;
	}
	return ChartAnnotationType.ForecastChart;
}
