import type { ChartSetting, ChartSettingType } from '@/types/common';
import { v4 as uuidv4 } from 'uuid';

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
